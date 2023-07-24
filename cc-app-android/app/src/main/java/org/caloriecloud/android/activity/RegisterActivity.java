package org.caloriecloud.android.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import org.caloriecloud.android.R;
import org.caloriecloud.android.fragment.ConnectTrackerFragment;
import org.caloriecloud.android.fragment.CreateAccountFragment;
import org.caloriecloud.android.fragment.OnboardingAuthorizationFragment;
import org.caloriecloud.android.fragment.PickTeamFragment;
import org.caloriecloud.android.model.Challenge;
import org.caloriecloud.android.ui.CCToolbar;
import org.caloriecloud.android.ui.CCViewPager;
import org.caloriecloud.android.util.APIManager;
import org.caloriecloud.android.util.CCStatics;
import org.caloriecloud.android.util.GoogleApiSharedClient;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    private ScreenSlidePagerAdapter mPagerAdapter;
    private GoogleApiClient mClient;

    private Challenge mChallenge;
    private Context mContext;

    @BindView(R.id.registerPager)
    CCViewPager mViewPager;

    @BindView(R.id.registerToolbar)
    CCToolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        mChallenge = (Challenge) getIntent().getSerializableExtra("challenge");
        mContext = this;

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }

        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        getSupportActionBar().setTitle("CREATE ACCOUNT");

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 1:
                        getSupportActionBar().setTitle("CHOOSE TEAM");
                        break;

                    case 2:
                        break;

                    case 3:
                        break;

                    default:
                        break;

                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE)
                {
                    if (mViewPager.getCurrentItem() == 2)
                    {
                        // Hide the keyboard.
                        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mViewPager.getWindowToken(), 0);
                    }
                }
            }
        });
    }

    public void authorizeButtonPressed() {
        mClient = GoogleApiSharedClient.getInstance(RegisterActivity.this).getClient();

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
    protected void onDestroy() {
        if (mClient != null) {
            mClient.unregisterConnectionCallbacks(this);
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
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

    public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragmentToLoad = null;

            switch (position) {
                case 0:
                    fragmentToLoad = CreateAccountFragment.newInstance(mChallenge);
                    break;

                case 1:
                    if (mChallenge.getTeamSelection() == CCStatics.CHALLENGE_SINGLE_TEAM) {
                        fragmentToLoad = new ConnectTrackerFragment();
                    }
                    else {
                        fragmentToLoad = PickTeamFragment.newInstance(mChallenge);
                    }
                    break;

                case 2:

                    if (mChallenge.getTeamSelection() == CCStatics.CHALLENGE_SINGLE_TEAM) {
                        fragmentToLoad = OnboardingAuthorizationFragment.newInstance(mChallenge);
                    }
                    else {
                        fragmentToLoad = new ConnectTrackerFragment();
                    }

                    break;

                case 3:
                    fragmentToLoad = OnboardingAuthorizationFragment.newInstance(mChallenge);
                    break;
            }

            return fragmentToLoad;
        }

        @Override
        public int getCount() {

            if (mChallenge.getTeamSelection() == CCStatics.CHALLENGE_SINGLE_TEAM) {

                if (CCStatics.wasGoogleFitEnabled(mContext)) {
                    return 2;
                }

                else {
                    return 3;
                }
            }
            else {

                if (CCStatics.wasGoogleFitEnabled(mContext)) {
                    return 3;
                }
                else {
                    return 4;
                }
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CCStatics.EXISTING_ACCOUNT_REQUEST_CODE) {
            if (resultCode == CCStatics.EXISTING_ACCOUNT_RESULT_CODE_SUCCESS) {

                // Challenge has a default team
                if (mChallenge.getTeamSelection() == CCStatics.CHALLENGE_SINGLE_TEAM) {

                    APIManager.joinTeam(CCStatics.getSavedUser(this).getUserId(), mChallenge.get_id(), mChallenge.getDefaultTeamId(), CCStatics.getSavedUser(this).getAccessToken(), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            if (response.isSuccessful()) {
                                Intent loadTracker = new Intent(mContext, MainActivity.class);

                                startActivity(loadTracker);
                                finish();
                            }
                            else {
                                String errorResponse = response.body().string();
                                if (errorResponse.equals("Validation error")) {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            new AlertDialog.Builder(mContext, R.style.DialogTheme)
                                                    .setTitle(R.string.error)
                                                    .setMessage(R.string.already_part_of_challenge)
                                                    .setCancelable(false)
                                                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            Intent loadTracker = new Intent(mContext, MainActivity.class);
                                                            dialogInterface.dismiss();
                                                            startActivity(loadTracker);
                                                            finish();
                                                        }
                                                    })
                                                    .create().show();
                                        }
                                    });

                                }
                                else {
                                    showErrorToast(errorResponse);
                                }
                            }

                            response.body().close();
                        }
                    });
                }
                // Challenge contains teams/regions
                else {

                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);

                }
            }
        }
    }

    private void showErrorToast(final String errorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void postDefaultTracker() {
        if (CCStatics.getSavedUser(mContext) != null) {
            APIManager.setDefaultSource(CCStatics.getSavedUser(mContext).getUserId(), CCStatics.getSavedUser(mContext).getAccessToken(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    if (response.isSuccessful()) {
                        APIManager.setDefaultDevice(CCStatics.getSavedUser(mContext).getUserId(), CCStatics.getSavedUser(mContext).getAccessToken(), new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, final Response response) throws IOException {

                                if (response.isSuccessful()) {
                                    CCStatics.setGoogleFitEnabled(mContext, true);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                        new AlertDialog.Builder(mContext, R.style.DialogTheme)
                                                .setTitle(R.string.success)
                                                .setMessage(R.string.auth_success_message)
                                                .setCancelable(true)
                                                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                        startActivity(new Intent(mContext, MainActivity.class));
                                                        RegisterActivity.this.finish();
                                                    }
                                                })
                                                .create().show();
                                        }
                                    });

                                }
                                response.body().close();
                            }
                        });
                        response.body().close();
                    }
                }
            });
        }
    }
}
