package org.caloriecloud.android.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evernote.android.job.JobManager;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.gms.common.api.GoogleApiClient;
import com.zendesk.sdk.support.ContactUsButtonVisibility;
import com.zendesk.sdk.support.SupportActivity;

import org.caloriecloud.android.R;
import org.caloriecloud.android.fragment.AboutFragment;
import org.caloriecloud.android.fragment.DashboardFragment;
import org.caloriecloud.android.fragment.TrackersFragment;
import org.caloriecloud.android.sync.ActivityDataSyncJob;
import org.caloriecloud.android.ui.CCToolbar;
import org.caloriecloud.android.util.APIManager;
import org.caloriecloud.android.util.CCStatics;
import org.caloriecloud.android.util.GoogleApiSharedClient;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    private final static String TAG = MainActivity.class.getSimpleName();
    private String mActiveFragmentTag;
    private JobManager mJobManager;
    private GoogleApiClient mClient;
    private Context mContext;

    int[] pageNames = {
            R.string.dashboard,
            R.string.tracker,
            R.string.about,
            R.string.support,
            R.string.logout
    };

    @BindView(R.id.toolbar)
    CCToolbar mToolbar;

    @BindView(R.id.mainFrame)
    LinearLayout mainFrame;

    private NavigationView mNavigationView;

    @BindView(R.id.drawer)
    DrawerLayout mDrawerLayout;

    public void authorizeButtonPressed() {
        mClient = GoogleApiSharedClient.getInstance(MainActivity.this).getClient();

        if (!mClient.isConnectionCallbacksRegistered(this)) {
            mClient.registerConnectionCallbacks(this);
        }

        if (!mClient.isConnected()) {
            mClient.connect();
        }
    }


    private DrawerLayout.DrawerListener mDrawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            mainFrame.setTranslationX(slideOffset * drawerView.getWidth());
            mDrawerLayout.bringChildToFront(drawerView);
            mDrawerLayout.requestLayout();
        }

        @Override
        public void onDrawerOpened(View drawerView) {

        }

        @Override
        public void onDrawerClosed(View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mContext = this;

        initNavigationDrawer();

        setSupportActionBar(mToolbar);

        // Load My Stats by default
        loadFragment(0);

        // Get Activity Data Sync Job Manager
        mJobManager = JobManager.instance();

        // Begin activity tracking if user is logged in
        if (CCStatics.getSavedUser(mContext) != null && CCStatics.wasGoogleFitEnabled(mContext)) {

            // Schedule Repeating
            if (mJobManager.getAllJobRequestsForTag(ActivityDataSyncJob.TAG).size() == 0) {
                toggleActivityDataSyncEngine(true);
            }
        }

        mDrawerLayout.addDrawerListener(mDrawerListener);

    }

    public void loadChallengeInfo(final ReadableMap challenge) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, ChallengeInfoActivity.class).putExtra("challenge", challenge.toHashMap()));
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mClient != null) {
            mClient.unregisterConnectionCallbacks(this);
        }

        mDrawerLayout.removeDrawerListener(mDrawerListener);

        super.onDestroy();
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


    public void initNavigationDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        mNavigationView = (NavigationView) drawer.findViewById(R.id.navigation_view);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                loadFragment(item.getOrder());
                return true;
            }
        });

        View header = mNavigationView.getHeaderView(0);
        TextView userEmail = (TextView) header.findViewById(R.id.userEmail);
        userEmail.setText(CCStatics.getSavedUser(mContext).getScreenName());

        mToolbar.setNavigationIcon(R.drawable.ic_menu_white);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
                    mDrawerLayout.closeDrawer(mNavigationView);
                } else {
                    mDrawerLayout.openDrawer(mNavigationView);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void detachActiveFragment() {
        Fragment activeFragment = getFragmentManager().findFragmentByTag(mActiveFragmentTag);
        if (activeFragment == null) {
            return;
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.detach(activeFragment);
        transaction.commit();
    }

    public void attachFragment(String fragmentTag) {
        detachActiveFragment();
        mActiveFragmentTag = fragmentTag;

        Fragment fragment = null;

        switch (fragmentTag) {
            case CCStatics.FRAGMENT_DASHBOARD:
                fragment = (DashboardFragment) getFragmentManager().findFragmentByTag(fragmentTag);
                if (fragment == null) {
                    fragment = new DashboardFragment();
                }
                break;

            case CCStatics.FRAGMENT_ABOUT_US:
                fragment = (AboutFragment) getFragmentManager().findFragmentByTag(fragmentTag);
                if (fragment == null) {
                    fragment = new AboutFragment();
                }
                break;

            case CCStatics.FRAGMENT_TRACKER:
                fragment = (TrackersFragment) getFragmentManager().findFragmentByTag(fragmentTag);
                if (fragment == null) {
                    fragment = new TrackersFragment();
                }
                break;

            case CCStatics.FRAGMENT_LOGOUT:
                break;
        }

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment, fragmentTag);
        fragmentTransaction.attach(fragment);
        fragmentTransaction.commit();

    }

    public void loadFragment(int position) {
        switch (position) {
            case 0:
                attachFragment(CCStatics.FRAGMENT_DASHBOARD);
                break;

            case 1:
                attachFragment(CCStatics.FRAGMENT_TRACKER);
                break;

            case 2:
                attachFragment(CCStatics.FRAGMENT_ABOUT_US);
                break;

            case 3:
                new SupportActivity.Builder().withCategoriesCollapsed(false).showConversationsMenuButton(false).withContactUsButtonVisibility(ContactUsButtonVisibility.OFF).withArticlesForSectionIds(Long.valueOf(getString(R.string.ZENDESK_FAQ_ID))).show(this);
                break;

            case 4:
                new AlertDialog.Builder(this, R.style.DialogTheme)
                        .setTitle(R.string.logout)
                        .setMessage(R.string.logout_message)
                        .setNeutralButton(R.string.no, null)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialogInterface, int i) {

                                APIManager.logoutUser(CCStatics.getSavedUser(mContext).getUserId(), CCStatics.getSavedUser(mContext).getAccessToken(), new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        try {
                                            if (response.isSuccessful()) {
                                                CCStatics.logoutUser(mContext);

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mDrawerLayout.closeDrawers();
                                                        dialogInterface.dismiss();
                                                    }
                                                });

                                                toggleActivityDataSyncEngine(false);
                                                startActivity(new Intent(mContext, LoginActivity.class));
                                                MainActivity.this.finish();
                                            }
                                        }
                                        finally {
                                            response.body().close();
                                        }
                                    }
                                });

                            }
                        }).create().show();
                break;
        }


        if (position != 3) {
            mToolbar.setTitle(getString(pageNames[position]));
        }

        mDrawerLayout.closeDrawer(mNavigationView);
    }

    public void toggleActivityDataSyncEngine(Boolean trackingEnabled) {
        if (trackingEnabled) {
            mJobManager.schedule(CCStatics.createRepeatingActivityDataSyncJobRequest());
        } else {
            mJobManager.cancelAll();
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

                                        final AlertDialog successDialog = new AlertDialog.Builder(mContext, R.style.DialogTheme)
                                                .setTitle(R.string.success)
                                                .setMessage("Your phone is now your connected tracker! The first sync may take a little while. In the meantime, why not enjoy getting active?")
                                                .setCancelable(true)
                                                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        TrackersFragment trackerFragment = (TrackersFragment) getFragmentManager().findFragmentByTag(mActiveFragmentTag);
                                                        trackerFragment.reloadData();

                                                        dialogInterface.dismiss();
                                                    }
                                                })
                                                .create();

                                        successDialog.show();
                                    }
                                });

                            }

                            response.body().close();
                        }
                    });
                }
            }
        });
    }
}

