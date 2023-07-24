package org.caloriecloud.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.caloriecloud.android.R;
import org.caloriecloud.android.activity.MainActivity;
import org.caloriecloud.android.sync.ActivityDataSyncEngine;
import org.caloriecloud.android.ui.CCButton;
import org.caloriecloud.android.ui.CCTypefaceSpan;
import org.caloriecloud.android.util.APIManager;
import org.caloriecloud.android.util.CCStatics;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TrackerFragment extends android.app.Fragment {

    @BindView(R.id.authorizeButton)
    CCButton authorizeButton;

    @BindView(R.id.connectedSection)
    RelativeLayout connectedSection;

    @BindView(R.id.defaultDeviceLabel)
    TextView defaultDeviceLabel;

    @BindView(R.id.lastSyncedLabel)
    TextView lastSyncedLabel;

    private MainActivity mActivity;

    @OnClick(R.id.manageTrackersButton)
    void manageTrackersButtonPressed() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(CCStatics.BASE_URL + "/#/user-trackers/accessToken/" + CCStatics.getSavedUser(getActivity()).getAccessToken())));
    }

    @OnClick(R.id.visitDashboardButton)
    void visitDashboardButtonPressed() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(CCStatics.BASE_URL + "/#/user-dashboard/accessToken/" + CCStatics.getSavedUser(getActivity()).getAccessToken())));
    }

    @OnClick(R.id.authorizeButton)
    void authorizeButtonPressed() {
        mActivity.authorizeButtonPressed();
    }

    private void getConnectedTracker() {
        APIManager.getLastSyncDateDevice(CCStatics.getSavedUser(getActivity()).getUserId(), CCStatics.getSavedUser(getActivity()).getAccessToken(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) {
                    final JsonElement jsonElement = new JsonParser().parse(response.body().string());
                    final JsonObject jsonResponse = (jsonElement.isJsonObject()) ? jsonElement.getAsJsonObject() : new JsonObject();

                    if (jsonResponse.get("updatedAt") != null) {

                        String lastSync = "Last Synced: \n";
                        String lastSyncDateString = CCStatics.normalizeDateString(jsonResponse.get("updatedAt").getAsString());
                        final SpannableString ss1 = new SpannableString(lastSync + lastSyncDateString);

                        //ss1.setSpan(new CCTypefaceSpan(Typeface.createFromAsset(getActivity().getAssets(), "SourceSansPro_Regular.otf")), 0, lastSync.length(), 0);
                        ss1.setSpan(new CCTypefaceSpan(Typeface.createFromAsset(getActivity().getAssets(), "fonts/SourceSansPro_It.otf")), lastSync.length(), ss1.length(), 0);

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lastSyncedLabel.setText(ss1);
                                connectedSection.setVisibility(View.VISIBLE);
                            }
                        });

                    }
                    else {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lastSyncedLabel.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/SourceSansPro_Regular.otf"));
                                lastSyncedLabel.setText("Not synced yet");
                            }
                        });

                    }

                    if (jsonResponse.get("source") != null) {

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                defaultDeviceLabel.setText(CCStatics.getTrackerDisplayName(jsonResponse.get("source").getAsString()));
                            }
                        });

                    }
                    else {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                defaultDeviceLabel.setText("No connected devices");
                            }
                        });

                    }
                }
            }

        });
    }

    public void reloadData(final Context mContext) {

        if (CCStatics.getSavedUser(mContext) != null) {

            APIManager.getLastSyncDateDevice(CCStatics.getSavedUser(mContext).getUserId(), CCStatics.getSavedUser(mContext).getAccessToken(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    if (response.isSuccessful()) {
                        final JsonElement jsonElement = new JsonParser().parse(response.body().string());
                        final JsonObject jsonResponse = (jsonElement.isJsonObject()) ? jsonElement.getAsJsonObject() : new JsonObject();

                        if (jsonResponse.get("updatedAt") != null) {

                            String lastSync = "Last Synced: \n";
                            String lastSyncDateString = CCStatics.normalizeDateString(jsonResponse.get("updatedAt").getAsString());
                            final SpannableString ss1 = new SpannableString(lastSync + lastSyncDateString);

                            //ss1.setSpan(new CCTypefaceSpan(Typeface.createFromAsset(getActivity().getAssets(), "SourceSansPro_Regular.otf")), 0, lastSync.length(), 0);
                            ss1.setSpan(new CCTypefaceSpan(Typeface.createFromAsset(mContext.getAssets(), "fonts/SourceSansPro_It.otf")), lastSync.length(), ss1.length(), 0);

                        }
                        else {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    lastSyncedLabel.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/SourceSansPro_Regular.otf"));
                                    lastSyncedLabel.setText("Not synced yet");
                                }
                            });

                        }

                        if (jsonResponse.get("source") != null) {

                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    defaultDeviceLabel.setText(CCStatics.getTrackerDisplayName(jsonResponse.get("source").getAsString()));
                                }
                            });

                        }
                        else {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    defaultDeviceLabel.setText("No connected devices");
                                }
                            });

                        }
                    }
                }

            });
        }

        if (CCStatics.wasGoogleFitEnabled(mActivity)) {
            authorizeButton.setVisibility(View.GONE);
        }
        else {
            authorizeButton.setVisibility(View.VISIBLE);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracker, container, false);
        ButterKnife.bind(this, rootView);

        mActivity = (MainActivity) getActivity();

        if (CCStatics.wasGoogleFitEnabled(mActivity)) {
            syncDataNow(mActivity);
        }
        else {
            lastSyncedLabel.setText(R.string.authorization_missing);
            authorizeButton.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getConnectedTracker();
    }

    private void syncDataNow(final Activity mActivity) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectedSection.setVisibility(View.GONE);
                        lastSyncedLabel.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/SourceSansPro_It.otf"));
                        lastSyncedLabel.setText(R.string.syncing_data);
                    }
                });
            }

            @Override
            public Boolean doInBackground(Void...voids) {
                return new ActivityDataSyncEngine(mActivity).syncActivityData();
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                reloadData(mActivity);
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }
}
