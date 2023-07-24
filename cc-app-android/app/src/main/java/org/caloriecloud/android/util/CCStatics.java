package org.caloriecloud.android.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.evernote.android.job.JobRequest;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.caloriecloud.android.BuildConfig;
import org.caloriecloud.android.R;
import org.caloriecloud.android.model.User;
import org.caloriecloud.android.sync.ActivityDataSyncJob;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;

public class CCStatics {

    /* URLs */
    public final static String BASE_URL = BuildConfig.ENV;
    public final static String LOGIN_URL = BASE_URL + "/api/v2/login";
    public final static String USERS_URL = BASE_URL + "/api/v2/users/";
    public final static String LAST_SYNC_DATE_URL = BASE_URL + "/api/v2/users/lsd/";
    public final static String LAST_SYNC_DATE_DEVICE_URL = BASE_URL + "/api/v2/getLastSyncDate/user/";
    public final static String ACTIVITY_SUMMARIES_URL = BASE_URL + "/api/v2/activitySummaries";
    public final static String LOGOUT_URL = BASE_URL + "/api/v2/logout";
    public final static String DEFAULT_SOURCE_URL = BASE_URL + "/api/v2/users/setDefaultDevice";
    public final static String DEFAULT_DEVICE_URL = BASE_URL + "/api/v2/userDevices";
    public final static String PASSWORD_RESET_URL = BASE_URL + "/api/v2/sendResetLink";
    public final static String ABOUT_URL = "http://caloriecloud.org/android-about-us";
    public final static String JOIN_CHALLENGE_URL = BASE_URL + "/api/v2/challenges/challengeCode";
    public final static String CREATE_ACCOUNT_URL = BASE_URL + "/api/v2/users";
    public final static String ACCOUNT_CREATION_EMAIL_URL = BASE_URL + "/api/v2/accountCreationEmail";
    public final static String REGIONS_BY_CHALLENGE_URL = BASE_URL + "/api/v2/regions/getAllRegionsByChallengeId/";
    public final static String JOIN_TEAM_URL = BASE_URL + "/api/v2/teamMembers/byChallenge";

    /* Fragments */
    public static final String FRAGMENT_DASHBOARD = "fragmentDashboard";
    public static final String FRAGMENT_ABOUT_US = "fragmentAboutUs";
    public static final String FRAGMENT_TRACKER = "fragmentTracker";
    public static final String FRAGMENT_SUPPORT = "fragmentSupport";
    public static final String FRAGMENT_ONBOARDING = "fragmentOnboarding";
    public static final String FRAGMENT_LOGOUT = "fragmentLogout";

    /* Challenge Info */
    public static final int CHALLENGE_SINGLE_TEAM = 1;
    public static final int CHALLENGE_MULTIPLE_TEAMS = 2;
    public static final int CHALLENGE_REGIONS_TEAMS = 3;

    /* Activity Requests Codes */
    public static final int EXISTING_ACCOUNT_REQUEST_CODE = 100;
    public static final int EXISTING_ACCOUNT_RESULT_CODE_SUCCESS = 200;

    /* MediaTypes */
    public final static MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /* Methods */
    public static boolean isFirstLaunch(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean("firstRun", true);
    }

    public static void setFirstLaunch(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean("firstRun", false).apply();
    }

    public static boolean wasGoogleFitEnabled(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean("wasGoogleFitEnabled", false);
    }

    public static void setGoogleFitEnabled(Context context, boolean wasGoogleFitEnabled) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean("wasGoogleFitEnabled", wasGoogleFitEnabled).apply();
    }

    public static void setSavedUser(Context context, User user) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userJSON = new Gson().toJson(user);
        sharedPreferences.edit().putString("User", userJSON).apply();
    }

    public static User getSavedUser(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return new Gson().fromJson(sharedPreferences.getString("User", null), User.class);
    }

    public static void logoutUser(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString("User", null).apply();
    }

    public static ProgressDialog showProgressDialog(Context context, String title, String message) {
        ProgressDialog dialog = new ProgressDialog(context, R.style.DialogTheme);
        try {
            dialog.setTitle(title);
            dialog.setMessage(message);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return dialog;
    }

    public static JobRequest createRepeatingActivityDataSyncJobRequest() {
       return new JobRequest.Builder(ActivityDataSyncJob.TAG)
                .setUpdateCurrent(true)
                .setPeriodic(TimeUnit.MINUTES.toMillis(15))
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setPersisted(true)
                .build();
    }

    public static String normalizeDateString(String dateString) {

        SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        serverDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat clientDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
        String outputString = "";

        try {
            Date startDate = serverDateFormat.parse(dateString);
            outputString = clientDateFormat.format(startDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return outputString;
    }

    public static int getDateDiff(java.util.Date date1, java.util.Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return (int) ((timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS)) / 86400000 + 1);

    }

    public static String getTrackerDisplayName(String shortname) {

        String trackerDisplay;

        switch (shortname) {
            case "movesapp":
                trackerDisplay = "Moves";
                break;

            case "fitbit":
                trackerDisplay = "Fitbit";
                break;

            case "jawbone_up":
                trackerDisplay = "Jawbone Up";
                break;

            case "garmin_connect":
                trackerDisplay = "Garmin";
                break;

            case "healthgraph":
                trackerDisplay = "Runkeeper";
                break;

            case "pivotal_living":
                trackerDisplay = "Pivotal Living";
                break;

            case "nikeplus":
                trackerDisplay = "Nike+";
                break;

            case "misfit":
                trackerDisplay = "Misfit";
                break;

            case "striiv":
                trackerDisplay = "Striiv";
                break;

            case "kpb":
                trackerDisplay = "Kid Power Band";
                break;

            case "googlefit":
                trackerDisplay = "Google Fit";
                break;

            case "healthkit":
                trackerDisplay = "Health Kit";
                break;

            default:
                trackerDisplay = shortname;
                break;

        }

        return trackerDisplay;

    }

    public static String connectedDevicesToString(JsonArray connectedDevices) {

        String output = "";
        int count = connectedDevices.size();

        if (count == 1) {
            return getTrackerDisplayName(connectedDevices.get(0).getAsString());
        }
        else if (count == 2) {
            return getTrackerDisplayName(connectedDevices.get(0).getAsString()) + " and " + getTrackerDisplayName(connectedDevices.get(1).getAsString());
        }
        else if (count >= 3) {

            for (int x = 0; x < count; x++) {

                if (x == count - 1) {
                    output += ("and " + getTrackerDisplayName(connectedDevices.get(x).getAsString()));
                }
                else {
                    output += (getTrackerDisplayName(connectedDevices.get(x).getAsString()) + ",");
                }

            }

        }

        return output;
    }

    public static WritableMap convertJsonToMap(JSONObject jsonObject) throws JSONException {
        WritableMap map = new WritableNativeMap();

        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                map.putMap(key, convertJsonToMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                map.putArray(key, convertJsonToArray((JSONArray) value));
            } else if (value instanceof  Boolean) {
                map.putBoolean(key, (Boolean) value);
            } else if (value instanceof  Integer) {
                map.putInt(key, (Integer) value);
            } else if (value instanceof  Double) {
                map.putDouble(key, (Double) value);
            } else if (value instanceof String)  {
                map.putString(key, (String) value);
            } else {
                map.putString(key, value.toString());
            }
        }
        return map;
    }

    public static WritableArray convertJsonToArray(JSONArray jsonArray) throws JSONException {
        WritableArray array = new WritableNativeArray();

        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (value instanceof JSONObject) {
                array.pushMap(convertJsonToMap((JSONObject) value));
            } else if (value instanceof  JSONArray) {
                array.pushArray(convertJsonToArray((JSONArray) value));
            } else if (value instanceof  Boolean) {
                array.pushBoolean((Boolean) value);
            } else if (value instanceof  Integer) {
                array.pushInt((Integer) value);
            } else if (value instanceof  Double) {
                array.pushDouble((Double) value);
            } else if (value instanceof String)  {
                array.pushString((String) value);
            } else {
                array.pushString(value.toString());
            }
        }
        return array;
    }

    public static JSONObject convertMapToJson(ReadableMap readableMap) throws JSONException {
        JSONObject object = new JSONObject();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            switch (readableMap.getType(key)) {
                case Null:
                    object.put(key, JSONObject.NULL);
                    break;
                case Boolean:
                    object.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    object.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    object.put(key, readableMap.getString(key));
                    break;
                case Map:
                    object.put(key, convertMapToJson(readableMap.getMap(key)));
                    break;
                case Array:
                    object.put(key, convertArrayToJson(readableMap.getArray(key)));
                    break;
            }
        }
        return object;
    }

    public static JSONArray convertArrayToJson(ReadableArray readableArray) throws JSONException {
        JSONArray array = new JSONArray();
        for (int i = 0; i < readableArray.size(); i++) {
            switch (readableArray.getType(i)) {
                case Null:
                    break;
                case Boolean:
                    array.put(readableArray.getBoolean(i));
                    break;
                case Number:
                    array.put(readableArray.getDouble(i));
                    break;
                case String:
                    array.put(readableArray.getString(i));
                    break;
                case Map:
                    array.put(convertMapToJson(readableArray.getMap(i)));
                    break;
                case Array:
                    array.put(convertArrayToJson(readableArray.getArray(i)));
                    break;
            }
        }
        return array;
    }



    public static String aboutUsContent = (BuildConfig.FLAVOR.contains("kpw")) ? "<html><meta name=\\\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0\"><head><style type=\"text/css\">@font-face{font-family: 'fonts/SourceSansPro_It'; src: url('fonts/SourceSansPro_It.otf');}@font-face{font-family: 'fonts/SourceSansPro_Bold'; src: url('fonts/SourceSansPro_Bold.otf');}strong{font-family: 'fonts/SourceSansPro_Bold';}p{font-family:'fonts/SourceSansPro_It';}body{padding:5px;color: #666; line-height: 1.7em;}a{font-family: 'fonts/SourceSansPro_It'; color: #00a4e4; text-decoration:none;}</style></head><body> <div> <p>This application is connected to Google Fit. Your activity data will be updated in the background every fifteen minutes or when you open the app. This utilizes the built-in technology in your mobile device without consuming additional battery.</p><p>The UNICEF Kid Power Workplace is a program by UNICEF USA which helps save and protect the world's most vulnerable children.</p><p>Web: <a href=\"http://workplace.unicefkidpower.org\" target=\"_blank\">workplace.unicefkidpower.org</a></p><p>Email: <a href=\"mailto:workplace@unicefkidpower.org\">workplace@unicefkidpower.org</a></p><p>Phone: <a href=\"tel:3033091423\">303-309-1423</a></p><p><strong>Are you in a challenge and need help?</strong> Visit our <a target=\"_blank\" href=\"http://unicefkidpowerworkplace.zendesk.com/hc/en-us\" target=\"_blank\">Help Center</a> or email <a href=\"mailto:workplace@unicefkidpower.org\">workplace@unicefkidpower.org</a>.</p><p><strong>Interested in how RUTF (Ready to Use Therapeutic Food) has revolutionized the treatment of severe acute malnutrition among children?</strong> Read this <a target=\"_blank\" href=\"http://www.unicef.org/media/files/Position_Paper_Ready-to-use_therapeutic_food_for_children_with_severe_acute_malnutrition__June_2013.pdf\" target=\"_blank\">position paper from UNICEF</a>.</p><p><a target=\"_blank\" href=\"http://www.caloriecloud.org/privacy-app/\">Privacy Policy</a></p></div></body></html>" : "<html><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0\"><head><style type=\"text/css\">@font-face{font-family: 'fonts/SourceSansPro_It'; src: url('fonts/SourceSansPro_It.otf');}@font-face{font-family: 'fonts/SourceSansPro_Bold'; src: url('fonts/SourceSansPro_Bold.otf');}strong{font-family: 'fonts/SourceSansPro_Bold';}p{font-family:'fonts/SourceSansPro_It';}body{padding:5px;color: #666; line-height: 1.7em;}a{font-family: 'fonts/SourceSansPro_It'; color: #00a4e4; text-decoration:none;}</style></head><body> <div> <p>This application is connected to Google Fit. Your activity data will be updated in the background every fifteen minutes or when you open the app. This utilizes the built-in technology in your mobile device without consuming additional battery.</p><p>Calorie Cloud provides solutions that connect getting active with helping malnourished children and is registered in the US as a 501c3 non-profit corporation.</p><p>Web: <a href=\"http://www.caloriecloud.org\" target=\"_blank\">caloriecloud.org</a></p><p>Email: <a href=\"mailto:hello@caloriecloud.org\">hello@caloriecloud.org</a></p><p>Phone: <a href=\"tel:3033091423\">303-309-1423</a></p><p><strong>Are you in a challenge and need help?</strong> Visit our <a target=\"_blank\" href=\"http://support.caloriecloud.org/hc/en-us\" target=\"_blank\">Help Center</a> or email <a href=\"mailto:support@caloriecloud.org\">support@caloriecloud.org</a>.</p><p><strong>Interested in how RUTF (Ready to Use Therapeutic Food) has revolutionized the treatment of severe acute malnutrition among children?</strong> Read this <a target=\"_blank\" href=\"http://www.unicef.org/media/files/Position_Paper_Ready-to-use_therapeutic_food_for_children_with_severe_acute_malnutrition__June_2013.pdf\" target=\"_blank\">position paper from UNICEF</a>.</p><p><a target=\"_blank\" href=\"http://www.caloriecloud.org/privacy-app/\">Privacy Policy</a></p></div></body></html>";

}
