package org.caloriecloud.android.util;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;

public class GoogleApiSharedClient {

    private static final String TAG = GoogleApiSharedClient.class.getSimpleName();
    private static GoogleApiSharedClient mInstance = null;
    private GoogleApiClient mClient;

    private GoogleApiSharedClient(AppCompatActivity activity) {
        this.mClient = new GoogleApiClient.Builder(activity)
            .addApi(Fitness.HISTORY_API)
            .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
            .enableAutoManage(activity, new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    Log.d(TAG, "Google Fit Connection Failed!");
                }
            })
            .build();
    }

    private GoogleApiSharedClient(Context context) {
        this.mClient = new GoogleApiClient.Builder(context)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(TAG, "Google Fit Connection Successful!");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                            Log.i(TAG, "Google Fit Connection lost.  Cause: Network Lost.");
                        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                            Log.i(TAG, "Google Fit Connection lost.  Reason: Service Disconnected");
                        }
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG, "Google Fit Connection Failed!");
                    }
                })
                .build();
    }

    public static GoogleApiSharedClient getInstance(AppCompatActivity activity) {
        if (mInstance == null) {
            mInstance = new GoogleApiSharedClient(activity);
        }
        return mInstance;
    }

    public static GoogleApiSharedClient getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new GoogleApiSharedClient(context);
        }
        return mInstance;
    }

    public GoogleApiClient getClient() {
        return this.mClient;
    }

    public void setClient(GoogleApiClient client) {
        this.mClient = client;
    }
}
