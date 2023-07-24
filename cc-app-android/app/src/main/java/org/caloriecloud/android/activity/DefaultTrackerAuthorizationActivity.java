package org.caloriecloud.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import org.caloriecloud.android.R;
import org.caloriecloud.android.util.APIManager;
import org.caloriecloud.android.util.CCStatics;
import org.caloriecloud.android.util.GoogleApiSharedClient;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DefaultTrackerAuthorizationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = DefaultTrackerAuthorizationActivity.class.getSimpleName();
    private Context mContext;
    private GoogleApiClient mClient;

    @OnClick(R.id.authorizeButton)
    void authorizeButtonPressed() {
        mClient = GoogleApiSharedClient.getInstance(this).getClient();
        if (!mClient.isConnectionCallbacksRegistered(this)) {
            mClient.registerConnectionCallbacks(this);
        }

        if (!mClient.isConnected()) {
            mClient.connect();
        }
        else {
            postDefaultTracker();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        postDefaultTracker();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            Log.i(TAG, "Google Fit Connection lost.  Cause: Network Lost.");
        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            Log.i(TAG, "Google Fit Connection lost.  Reason: Service Disconnected");
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_tracker_auth);
        ButterKnife.bind(this);
        mContext = this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mClient != null) {
            mClient.unregisterConnectionCallbacks(this);
        }
    }

    private void postDefaultTracker() {
        APIManager.setDefaultSource(CCStatics.getSavedUser(mContext).getUserId(), CCStatics.getSavedUser(mContext).getAccessToken(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        APIManager.setDefaultSource(CCStatics.getSavedUser(mContext).getUserId(), CCStatics.getSavedUser(mContext).getAccessToken(), new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    if (response.isSuccessful()) {
                                        CCStatics.setGoogleFitEnabled(mContext, true);

                                        startActivity(new Intent(mContext, PreDashboardActivity.class));
                                        DefaultTrackerAuthorizationActivity.this.finish();
                                    }
                                }
                                finally {
                                    response.body().close();
                                }
                            }
                        });
                    }
                }
                finally {
                    response.body().close();
                }
            }
        });
    }
}
