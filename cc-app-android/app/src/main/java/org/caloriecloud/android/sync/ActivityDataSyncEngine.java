package org.caloriecloud.android.sync;

import android.content.Context;
import android.os.Looper;
import android.os.NetworkOnMainThreadException;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.caloriecloud.android.util.CCStatics;
import org.caloriecloud.android.util.GoogleApiSharedClient;
import org.caloriecloud.android.util.OkHttpSharedClient;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ActivityDataSyncEngine {

    public final static String TAG = ActivityDataSyncEngine.class.getSimpleName();

    private final Context mContext;
    private GoogleApiClient mClient;
    private OkHttpClient requestClient = OkHttpSharedClient.getInstance().getClient();

    public ActivityDataSyncEngine(Context context) {
        mContext = context;
    }

    @WorkerThread
    public boolean syncActivityData() {

        boolean success = false;

        mClient = GoogleApiSharedClient.getInstance(mContext).getClient();

        if (!mClient.isConnected()) {
            mClient.connect();
        }

        Request lastSyncDateRequest = new Request.Builder()
                .url(CCStatics.LAST_SYNC_DATE_URL + CCStatics.getSavedUser(mContext).getUserId())
                .header("x-access-token", CCStatics.getSavedUser(mContext).getAccessToken())
                .get()
                .build();

        try {
            Response lastSyncDateResponse = requestClient.newCall(lastSyncDateRequest).execute();

            if (lastSyncDateResponse.isSuccessful()) {
                JsonObject jsonResponse = new JsonParser().parse(lastSyncDateResponse.body().string()).getAsJsonObject();
                String startDate = "";

                if (!jsonResponse.get("lsd").isJsonNull()) {
                    startDate = jsonResponse.get("lsd").getAsString();
                }

                DataReadRequest readRequest = getActivityDataFromStartDate(startDate);
                DataReadResult dataReadResult = Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);

                if (dataReadResultToJSON(dataReadResult).has("activity")) {

                    RequestBody jsonBody = RequestBody.create(CCStatics.JSON, dataReadResultToJSON(dataReadResult).toString());

                    if (mContext != null) {
                        Request postActivityRequest = new Request.Builder()
                                .url(CCStatics.ACTIVITY_SUMMARIES_URL)
                                .header("x-access-token", CCStatics.getSavedUser(mContext).getAccessToken())
                                .post(jsonBody)
                                .build();

                        try {
                            Response response = requestClient.newCall(postActivityRequest).execute();
                            String apiResposne = response.body().string();

                            if (response.isSuccessful()) {
                                Log.d(TAG, apiResposne);
                                success = true;
                            }
                            else {
                                Log.d(TAG, apiResposne);
                            }

                            response.body().close();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
            else {
                Log.d(TAG, lastSyncDateResponse.body().string());
            }

            lastSyncDateResponse.body().close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new NetworkOnMainThreadException();
        }

        return success;
    }

    /**
     * Function used to get active calorie data from a starting date
     * @param {@link Date} object for starting point
     * Return a {@link DataReadRequest} for all active calories burned in the past thirty days.
     */

    private static DataReadRequest getActivityDataFromStartDate(String startDateString) {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        Calendar cal = new GregorianCalendar();

        if (startDateString.length() != 0) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            simpleDateFormat.setTimeZone(utc);

            try {
                Date startDate = simpleDateFormat.parse(startDateString);
                Log.d(TAG, "Date Retrieved from Server: " + startDateString);
                Log.d(TAG, "Computed Date: " + startDate.toString());
                cal.setTime(startDate);
            }
            catch (ParseException e) {
                e.printStackTrace();
            }

        } else {
            cal.add(Calendar.DATE, -30);

        }

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startTime = cal.getTimeInMillis();

        long endTime = new GregorianCalendar().getTimeInMillis();

        DateFormat dateFormat = DateFormat.getDateInstance();
        Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(TAG, "Range End: " + dateFormat.format(endTime));

        return new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }

    private Float getBMRData() {
        // Take a time period of a year
        Calendar cal = Calendar.getInstance();
        java.util.Date now = new java.util.Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.YEAR, -1);
        long startTime = cal.getTimeInMillis();

        // Query for the latest BMR reading
        DataReadRequest request = new DataReadRequest.Builder()
                .read(DataType.TYPE_BASAL_METABOLIC_RATE)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .setLimit(1)
                .build();

        DataReadResult dataReadResult =
                Fitness.HistoryApi.readData(mClient, request).await(1, TimeUnit.MINUTES);

        DataSet dataset =
                dataReadResult.getDataSet(DataType.TYPE_BASAL_METABOLIC_RATE);

        Float bmr = null;
        if (!dataset.isEmpty()) {
            // There should only be a single data point
            DataPoint datapoint = dataset.getDataPoints().get(0);
            // There should only be a single field - "Calories Value"
            Field field = datapoint.getDataType().getFields().get(0);
            bmr = datapoint.getValue(field).asFloat();
        }
        else {
            /**
             * Mifflin - St. Jeor Formula to calculate BMR (provided by Google)
             * Men
             *  10 x weight (kg) + 6.25 x height (cm) - 5 x age (y) + 5
             *
             *  Women
             *  10 x weight (kg) + 6.25 x height (cm) - 5 x age (y) - 161
             *
             * Since we can't accurately get age nor gender from Google, we take the average of the two using these defaults
             * 170cm - Height
             * 73kg - Weight
             * 30y - Age
             *
             * */
            bmr = new Float((((10 * 170) + (6.25 * 73) - (5 * 30) + 5) + ((10 * 170) + (6.25 * 73) - (5 * 30) - 161)) / 2);

        }
        return bmr;
    }

    private JsonObject dataReadResultToJSON(DataReadResult dataReadResult) {
        JsonObject jsonObject = new JsonObject();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Float bmr = getBMRData();

        if (mContext != null) {
            if (CCStatics.getSavedUser(mContext) != null) {
                jsonObject.addProperty("userId", CCStatics.getSavedUser(mContext).getUserId());
            }
        }

        JsonArray jsonActivities = new JsonArray();

        for (Bucket bucket : dataReadResult.getBuckets()) {
            List<DataSet> dataSets = bucket.getDataSets();

            DataSet calorieDataSet = dataSets.get(0);
            DataSet stepDataSet = dataSets.get(1);
            JsonObject jsonActivity = new JsonObject();

            if (!calorieDataSet.getDataPoints().isEmpty()) {
                DataPoint calorieDataPoint = calorieDataSet.getDataPoints().get(0);

                if (calorieDataPoint != null) {

                    if (calorieDataPoint.getDataType().getFields().get(0) != null) {

                        if (calorieDataPoint.getValue(calorieDataPoint.getDataType().getFields().get(0)) != null) {

                            if (calorieDataPoint.getValue(calorieDataPoint.getDataType().getFields().get(0)).getFormat() == Field.FORMAT_FLOAT) {

                                if (bmr != null) {

                                    float activeCalories = calorieDataPoint.getValue(calorieDataPoint.getDataType().getFields().get(0)).asFloat() - bmr;

                                    if (activeCalories > 0.00) {
                                        jsonActivity.addProperty("date", dateFormat.format(bucket.getStartTime(TimeUnit.MILLISECONDS)));
                                        jsonActivity.addProperty("source", "googlefit");
                                        jsonActivity.addProperty("calories", (int) activeCalories);
                                        jsonActivities.add(jsonActivity);
                                    }

                                }
                                else {
                                    float activeCalories = calorieDataPoint.getValue(calorieDataPoint.getDataType().getFields().get(0)).asFloat();

                                    if (activeCalories > 0.00) {
                                        jsonActivity.addProperty("date", dateFormat.format(bucket.getStartTime(TimeUnit.MILLISECONDS)));
                                        jsonActivity.addProperty("source", "googlefit");
                                        jsonActivity.addProperty("calories", (int) activeCalories);
                                        jsonActivities.add(jsonActivity);
                                    }
                                }

                            }
                            else {
                                Fabric.getLogger().e(TAG, "Format is: " + calorieDataPoint.getValue(calorieDataPoint.getDataType().getFields().get(0)).getFormat());
                                Fabric.getLogger().e(TAG, "Value is: " + calorieDataPoint.getValue(calorieDataPoint.getDataType().getFields().get(0)).toString());

                            }

                        }
                    }

                }
            }

            if (!stepDataSet.getDataPoints().isEmpty()) {
                DataPoint stepDataPoint = stepDataSet.getDataPoints().get(0);

                if (stepDataPoint != null) {

                    if (stepDataPoint.getDataType().getFields().get(0) != null) {

                        if (stepDataPoint.getValue(stepDataPoint.getDataType().getFields().get(0)) != null) {
                            jsonActivity.addProperty("date", dateFormat.format(bucket.getStartTime(TimeUnit.MILLISECONDS)));
                            jsonActivity.addProperty("source", "googlefit");
                            jsonActivity.addProperty("steps", stepDataPoint.getValue(stepDataPoint.getDataType().getFields().get(0)).asInt());
                            jsonActivities.add(jsonActivity);
                        }
                    }

                }
            }

            if (jsonActivities.size() > 0) {
                jsonObject.add("activity", jsonActivities);
            }
        }

        return jsonObject;
    }
}
