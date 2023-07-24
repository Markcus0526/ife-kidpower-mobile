package org.caloriecloud.android.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.caloriecloud.android.R;
import org.caloriecloud.android.activity.MainActivity;
import org.caloriecloud.android.activity.RegisterActivity;
import org.caloriecloud.android.model.Challenge;
import org.caloriecloud.android.model.Region;
import org.caloriecloud.android.ui.CCSpinnerRegionAdapter;
import org.caloriecloud.android.ui.CCSpinnerTeamAdapter;
import org.caloriecloud.android.ui.CCViewPager;
import org.caloriecloud.android.util.APIManager;
import org.caloriecloud.android.util.CCStatics;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PickTeamFragment extends android.support.v4.app.Fragment{
    public static final String TAG = PickTeamFragment.class.getSimpleName();

    private Challenge mCurrentChallenge;
    private CCSpinnerTeamAdapter teamSpinnerAdapter;
    private CCSpinnerRegionAdapter regionSpinnerAdapter;
    private ArrayList<Region> mRegions;
    private RegisterActivity mActivity;
    private CCViewPager mViewPager;

    @BindView(R.id.challengeLogo)
    ImageView challengeLogo;

    @BindView(R.id.challengeRegions)
    Spinner challengeRegions;

    @BindView(R.id.challengeTeams)
    Spinner challengeTeams;

    @BindView(R.id.stepper)
    ImageView stepper;

    @OnClick(R.id.joinNowButton)
    void joinNowButtonPressed() {

        boolean isFormValid = false;

        switch (mCurrentChallenge.getTeamSelection()) {

            case CCStatics.CHALLENGE_MULTIPLE_TEAMS:

                if (challengeTeams.getSelectedItemPosition() == -1) {
                    showErrorToast(R.string.select_team);
                }
                else {
                    isFormValid = true;
                }
                break;

            case CCStatics.CHALLENGE_REGIONS_TEAMS:

                if (challengeRegions.getSelectedItemPosition() == -1 || challengeTeams.getSelectedItemPosition() == -1) {
                    showErrorToast(R.string.select_region_team);
                }
                else {
                    isFormValid = true;
                }

                break;

        }

        if (isFormValid) {
            final ProgressDialog mLoadingDialog = CCStatics.showProgressDialog(mActivity, null, getString(R.string.loading));

            APIManager.joinTeam(CCStatics.getSavedUser(mActivity).getUserId(), mCurrentChallenge.get_id(), teamSpinnerAdapter.getItem(challengeTeams.getSelectedItemPosition()).get_id(), CCStatics.getSavedUser(mActivity).getAccessToken(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    dismissWithCheck(mLoadingDialog);
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    dismissWithCheck(mLoadingDialog);

                        if (response.isSuccessful()) {

                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (CCStatics.wasGoogleFitEnabled(mActivity)) {
                                        mActivity.postDefaultTracker();
                                    }
                                    else {
                                        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                                    }
                                }
                            });

                        }
                        else {

                            String errorResponse = response.body().string();

                            if (errorResponse.equals("Validation error")) {

                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        new AlertDialog.Builder(mActivity, R.style.DialogTheme)
                                                .setTitle(R.string.error)
                                                .setMessage(R.string.already_part_of_challenge)
                                                .setCancelable(false)
                                                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        Intent loadTracker = new Intent(mActivity, MainActivity.class);
                                                        dialogInterface.dismiss();
                                                        startActivity(loadTracker);
                                                        mActivity.finish();
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
                    }
            });
        }
    }

    public static PickTeamFragment newInstance(Challenge currentChallenge) {
        PickTeamFragment fragment = new PickTeamFragment();
        Bundle bundle = fragment.getArguments();

        if (bundle == null) {
            bundle = new Bundle();
        }

        bundle.putSerializable("challenge", currentChallenge);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentChallenge = (Challenge) getArguments().getSerializable("challenge");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pick_team, container, false);
        ButterKnife.bind(this, rootView);

        mActivity = (RegisterActivity) getActivity();
        mViewPager = (CCViewPager) mActivity.findViewById(R.id.registerPager);

        if (mViewPager.getAdapter().getCount() <= 3 && ((RegisterActivity.ScreenSlidePagerAdapter) mViewPager.getAdapter()).getItem(mViewPager.getAdapter().getCount() - 1).getClass() == OnboardingAuthorizationFragment.class) {
            stepper.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.step1s2));
        }
        else if (mViewPager.getAdapter().getCount() < 3) {
            stepper.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.step1s2));
        }

        String challengeLogoSrc = mCurrentChallenge.getOrganization().getImageSrc();

        if (!challengeLogoSrc.equals("")) {
            Picasso.with(mActivity).load(challengeLogoSrc).into(challengeLogo);
        }

        // Challenge doesn't contain regions
        if (mCurrentChallenge.getTeamSelection() != CCStatics.CHALLENGE_REGIONS_TEAMS) {
            challengeRegions.setVisibility(View.GONE);

            teamSpinnerAdapter = new CCSpinnerTeamAdapter(mActivity, mCurrentChallenge.getTeams());

            challengeTeams.setAdapter(teamSpinnerAdapter);

        }
        // Challenge contains regions
        else {

            challengeRegions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    Region selectedRegion = (Region) challengeRegions.getSelectedItem();
                    selectedRegion.getTeams();

                    teamSpinnerAdapter = new CCSpinnerTeamAdapter(mActivity, selectedRegion.getTeams());

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            challengeTeams.setAdapter(teamSpinnerAdapter);
                        }
                    });

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

            APIManager.getRegionsByChallengeId(mCurrentChallenge.get_id(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            try {
                                mRegions = new Gson().fromJson(response.body().string(), new TypeToken<ArrayList<Region>>(){}.getType());
                                regionSpinnerAdapter = new CCSpinnerRegionAdapter(mActivity, mRegions);

                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        challengeRegions.setAdapter(regionSpinnerAdapter);
                                    }
                                });

                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    finally {
                        response.body().close();
                    }
                }
            });
        }

        return rootView;
    }

    private void showErrorToast(int resourceId) {
        Toast.makeText(mActivity, resourceId, Toast.LENGTH_SHORT).show();
    }

    private void showErrorToast(final String errorMessage) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void dismissWithCheck(Dialog dialog) {
        if (dialog != null) {
            if (dialog.isShowing()) {

                //get the Context object that was used to great the dialog
                Context context = ((ContextWrapper) dialog.getContext()).getBaseContext();

                // if the Context used here was an activity AND it hasn't been finished or destroyed
                // then dismiss it
                if (context instanceof Activity) {

                    // Api >=17
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        if (!((Activity) context).isFinishing() && !((Activity) context).isDestroyed()) {
                            dismissWithTryCatch(dialog);
                        }
                    } else {

                        // Api < 17. Unfortunately cannot check for isDestroyed()
                        if (!((Activity) context).isFinishing()) {
                            dismissWithTryCatch(dialog);
                        }
                    }
                } else
                    // if the Context used wasn't an Activity, then dismiss it too
                    dismissWithTryCatch(dialog);
            }
            dialog = null;
        }
    }

    public void dismissWithTryCatch(Dialog dialog) {
        try {
            dialog.dismiss();
        } catch (final IllegalArgumentException e) {
            // Do nothing.
        } catch (final Exception e) {
            // Do nothing.
        } finally {
            dialog = null;
        }
    }

}


