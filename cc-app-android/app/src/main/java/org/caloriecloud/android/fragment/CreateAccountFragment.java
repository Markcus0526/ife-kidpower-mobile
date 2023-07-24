package org.caloriecloud.android.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.caloriecloud.android.R;
import org.caloriecloud.android.activity.ExistingAccountActivity;
import org.caloriecloud.android.activity.RegisterActivity;
import org.caloriecloud.android.model.Challenge;
import org.caloriecloud.android.model.User;
import org.caloriecloud.android.ui.CCViewPager;
import org.caloriecloud.android.util.APIManager;
import org.caloriecloud.android.util.CCStatics;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CreateAccountFragment extends android.support.v4.app.Fragment {

    private CCViewPager mViewPager;
    public static final String TAG = CreateAccountFragment.class.getSimpleName();
    private Challenge mCurrentChallenge;

    private RegisterActivity mActivity;

    @BindView(R.id.usernameField)
    EditText usernameField;

    @BindView(R.id.emailField)
    EditText emailField;

    @BindView(R.id.passwordField)
    EditText passwordField;

    @BindView(R.id.stepper)
    ImageView stepper;

    @OnClick(R.id.already_have_account)
    void existingAccountButtonPressed() {
        mActivity.startActivityForResult(new Intent(mActivity, ExistingAccountActivity.class), CCStatics.EXISTING_ACCOUNT_REQUEST_CODE);
    }

    @OnClick(R.id.createAccountButton)
    void createAccountButtonPressed() {

        // Validate that fields have input values
        if (usernameField.getText().toString().length() > 0 && emailField.getText().toString().length() > 0 && passwordField.getText().toString().length() > 0) {
            // Validate that email field is actually an e-mail address
            if (emailField.getText().toString().matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {

                final ProgressDialog mLoadingDialog = CCStatics.showProgressDialog(mActivity, null, getString(R.string.loading));

                APIManager.createUser(usernameField.getText().toString(), emailField.getText().toString(), passwordField.getText().toString(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        dismissWithCheck(mLoadingDialog);
                        showErrorToast("A problem has occurred. Please try again later!");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        if (response.isSuccessful()) {

                            APIManager.loginUser(emailField.getText().toString(), passwordField.getText().toString(), new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    e.printStackTrace();
                                    dismissWithCheck(mLoadingDialog);
                                    showErrorToast("A problem has occurred. Please try again later!");
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    try {
                                        if (response.isSuccessful()) {
                                            String apiResponse = response.body().string();
                                            JSONObject jsonResponse = new JSONObject(apiResponse);
                                            final String accessToken = jsonResponse.getString("access_token");
                                            final int userId = jsonResponse.getInt("id");
                                            String email = jsonResponse.getString("email");
                                            String screenName = jsonResponse.getString("screenName");

                                            CCStatics.setSavedUser(mActivity, new User(userId, email, screenName, accessToken));

                                            SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                                            SimpleDateFormat clientDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                                            try {
                                                Date startDate = serverDateFormat.parse(mCurrentChallenge.getStartDate());
                                                Date endDate = serverDateFormat.parse(mCurrentChallenge.getEndDate());
                                                int daysLeft = CCStatics.getDateDiff(new Date(), startDate, TimeUnit.MILLISECONDS);
                                                String endDateString = clientDateFormat.format(endDate);
                                                int contentType;

                                                if (daysLeft < 0) {
                                                    // Challenge has already started
                                                    contentType = 1;
                                                }
                                                else {
                                                    // Challenge will start
                                                    contentType = 2;
                                                }

                                                APIManager.createCustomerIOAccount(userId, screenName, endDateString, daysLeft, mCurrentChallenge.getBrand(), contentType, new Callback() {
                                                    @Override
                                                    public void onFailure(Call call, IOException e) {
                                                        e.printStackTrace();
                                                        dismissWithCheck(mLoadingDialog);
                                                        showErrorToast("A problem has occurred. Please try again later!");
                                                    }

                                                    @Override
                                                    public void onResponse(Call call, Response response) throws IOException {
                                                        dismissWithCheck(mLoadingDialog);
                                                        if (response.isSuccessful()) {

                                                            // Challenge has a default team
                                                            if (mCurrentChallenge.getTeamSelection() == CCStatics.CHALLENGE_SINGLE_TEAM) {

                                                                APIManager.joinTeam(userId, mCurrentChallenge.get_id(), mCurrentChallenge.getDefaultTeamId(), accessToken, new Callback() {
                                                                    @Override
                                                                    public void onFailure(Call call, IOException e) {
                                                                        dismissWithCheck(mLoadingDialog);
                                                                        e.printStackTrace();
                                                                    }

                                                                    @Override
                                                                    public void onResponse(Call call, Response response) throws IOException {
                                                                        dismissWithCheck(mLoadingDialog);
                                                                        try {
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
                                                                        }
                                                                        finally {
                                                                            response.body().close();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                            // Challenge contains teams/regions
                                                            else {

                                                                mActivity.runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                                                                    }
                                                                });
                                                            }
                                                        }
                                                        else {
                                                            showErrorToast("A problem has occurred! Please try again later!");
                                                        }
                                                        response.body().close();
                                                    }
                                                });
                                            }
                                            catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        else {
                                            showErrorToast(response.body().string());
                                            response.body().close();
                                        }
                                    }
                                    catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            dismissWithCheck(mLoadingDialog);
                            showErrorToast(response.body().string());
                            response.body().close();
                        }
                    }
                });
            }
        }
    }

    public static CreateAccountFragment newInstance(Challenge currentChallenge) {
        CreateAccountFragment fragment = new CreateAccountFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_create_account, container, false);
        ButterKnife.bind(this, rootView);

        mActivity = (RegisterActivity) getActivity();
        mViewPager = (CCViewPager) mActivity.findViewById(R.id.registerPager);

        if (mViewPager.getAdapter().getCount() <= 3 && ((RegisterActivity.ScreenSlidePagerAdapter) mViewPager.getAdapter()).getItem(mViewPager.getAdapter().getCount() - 1).getClass() == OnboardingAuthorizationFragment.class) {
            stepper.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.step1s2));
        }
        else if (mViewPager.getAdapter().getCount() < 3) {
            stepper.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.step1s2));
        }


        return rootView;
    }

    private void showErrorToast(final String message) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
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
