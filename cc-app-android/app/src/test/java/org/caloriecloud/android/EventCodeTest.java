package org.caloriecloud.android;

import org.caloriecloud.android.util.CCStatics;
import org.caloriecloud.android.util.OkHttpSharedClient;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.junit.Assert.assertEquals;

public class EventCodeTest {

    @Test
    public void daysLeftTest() {

        SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        Date endDate;
        int daysLeft = -1;

        try {
            endDate = serverDateFormat.parse("2016-11-22T00:00:00.000Z");
            daysLeft = CCStatics.getDateDiff(new Date(), endDate, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        assertEquals(daysLeft, -5);

    }

    @Test
    public void createUserEmailTest() {

        OkHttpClient requestClient = OkHttpSharedClient.getInstance().getClient();
        SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

        try {
            Date endDate = serverDateFormat.parse("2016-11-22T00:00:00.000Z");
            int daysLeft = CCStatics.getDateDiff(new Date(), endDate, TimeUnit.MILLISECONDS);
            int contentType;

            if (daysLeft < 0) {
                // Challenge has already started
                contentType = 1;
            } else {
                // Challenge will start
                contentType = 2;
            }

            final FormBody accountBody = new FormBody.Builder()
                    .add("contentType", Integer.toString(contentType))
                    .add("daysLeft", Integer.toString(daysLeft))
                    .add("endDate", "2016-12-01T00:00:00.000Z")
                    .build();

            final Request accountCreationEmailRequest = new Request.Builder()
                    .url(CCStatics.ACCOUNT_CREATION_EMAIL_URL)
                    .post(accountBody)
                    .build();

            requestClient.newCall(accountCreationEmailRequest)
                    .enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                System.out.print(response.body().string());

                            } finally {
                                response.body().close();
                            }

                        }

                    });

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
