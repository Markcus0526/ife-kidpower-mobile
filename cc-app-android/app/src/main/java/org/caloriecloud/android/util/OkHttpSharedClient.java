package org.caloriecloud.android.util;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class OkHttpSharedClient {

    private static OkHttpSharedClient mInstance = null;

    private OkHttpClient mClient;

    private OkHttpSharedClient() {
        mClient = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static synchronized OkHttpSharedClient getInstance() {
        if (mInstance == null) {
            mInstance = new OkHttpSharedClient();
        }
        return mInstance;
    }

    public OkHttpClient getClient() {
        return mClient;
    }

    public void setClient(OkHttpClient client) {
        mClient = client;
    }
}
