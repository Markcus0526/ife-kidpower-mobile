package org.caloriecloud.android.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.Gson;

import org.caloriecloud.android.BuildConfig;
import org.caloriecloud.android.R;
import org.caloriecloud.android.model.Challenge;
import org.caloriecloud.android.model.User;
import org.caloriecloud.android.ui.CCEditText;
import org.caloriecloud.android.util.APIManager;
import org.caloriecloud.android.util.CCStatics;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.emailField)
    CCEditText emailField;

    @BindView(R.id.passwordField)
    CCEditText passwordField;

    public final String TAG = this.getClass().getSimpleName();

    private static final int OVERLAY_PERMISSION_REQ_CODE = 1212;

    @OnClick(R.id.forgotPassword)
    void forgotPasswordPressed() {
        final View root = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);

        AlertDialog resetPasswordDialog = new AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle(R.string.forgot_password_title)
                .setMessage(R.string.forgot_password_message)
                .setView(root)
                .setCancelable(true)
                .setPositiveButton(R.string.reset_password, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final CCEditText emailResetField = (CCEditText) root.findViewById(R.id.emailResetField);

                        if (emailField.getText().toString().length() > 0) {
                            emailResetField.setText(emailField.getText().toString());
                        }

                        String brand;

                        if (BuildConfig.FLAVOR.contains("workplace")) {
                            brand = "UNICEF Kid Power Workplace";
                        }
                        else {
                            brand = null;
                        }

                        APIManager.sendPasswordReset(emailResetField.getText().toString(), brand, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {

                                String apiResponse = response.body().string();

                                if (response.isSuccessful()) {
                                    try {
                                        JSONObject jsonResponse = new JSONObject(apiResponse);
                                        showResultToast(jsonResponse.getString("message"));
                                    }
                                    catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                else {
                                    if (apiResponse.equals("User not found!")) {

                                        if (!isValidEmail(emailResetField.getText().toString())) {
                                            showResultToast("User's email is required!");
                                        }
                                        else {
                                            showResultToast("This email is not associated with any existing account");
                                        }
                                    }
                                    else {
                                        showResultToast(apiResponse);
                                    }
                                }

                                response.body().close();
                            }
                        });

                        dialogInterface.dismiss();
                    }
                }).create();

        resetPasswordDialog.show();
        resetPasswordDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @OnClick(R.id.registerEventCode)
    void registerEventCodePressed() {
        final View root = getLayoutInflater().inflate(R.layout.dialog_register, null);

        final AlertDialog registerEventCodeDialog = new AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle(R.string.enter_event_code)
                .setView(root)
                .setCancelable(true)
                .setPositiveButton(R.string.view_challenge, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialogInterface, int i) {
                                final CCEditText eventCodeField = (CCEditText) root.findViewById(R.id.eventCodeField);

                                String eventCode = eventCodeField.getText().toString();

                                if (eventCode.length() > 0) {
                                    final ProgressDialog mLoadingDialog = CCStatics.showProgressDialog(LoginActivity.this, null, getString(R.string.loading));

                                    APIManager.getChallengeWithEventCode(eventCode, new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            e.printStackTrace();
                                            dismissWithCheck(mLoadingDialog);

                                            if (e.getCause().getLocalizedMessage().contains("EAI_NODATA")) {
                                                showNoInternetConnectionToast();
                                            }
                                            else {
                                                showChallengeErrorToast();
                                            }
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            dismissWithCheck(mLoadingDialog);

                                            if (response.isSuccessful()) {
                                                Challenge challenge = new Gson().fromJson(response.body().charStream(), Challenge.class);

                                                SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

                                                try {
                                                    Date endDate = serverDateFormat.parse(challenge.getEndDate());

                                                    // Challenge has already ended
                                                    if ((new Date()).after(endDate)) {
                                                        showChallengeEndedToast();
                                                    }
                                                    else {
                                                        Intent regIntent = new Intent(LoginActivity.this, OnboardingActivity.class);
                                                        regIntent.putExtra("challenge", challenge);

                                                        dialogInterface.dismiss();
                                                        startActivity(regIntent);
                                                        finish();
                                                    }

                                                }
                                                catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                dismissWithCheck(mLoadingDialog);
                                                Log.d(TAG, "Response not successfull");
                                                showChallengeErrorToast();
                                            }

                                            response.body().close();
                                        }
                                    });
                                }
                            }
                        }).create();

        registerEventCodeDialog.show();
        registerEventCodeDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

    }

    /**
     * Login Button Click Listener Function
     */
    @OnClick(R.id.loginButton)
    void loginToCC(){
        if (validateInput()) {
            final ProgressDialog mLoadingDialog = CCStatics.showProgressDialog(LoginActivity.this, null, getString(R.string.logging_in));

            APIManager.loginUser(emailField.getText().toString(), passwordField.getText().toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    dismissWithCheck(mLoadingDialog);
                    showErrorToast();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    dismissWithCheck(mLoadingDialog);

                    try {
                        if (response.isSuccessful()) {
                            JSONObject jsonResponse = new JSONObject(response.body().string());
                            String accessToken = jsonResponse.getString("access_token");
                            int userId = jsonResponse.getInt("id");
                            String email = jsonResponse.getString("email");
                            String screenName = jsonResponse.getString("screenName");

                            CCStatics.setSavedUser(LoginActivity.this, new User(userId, email, screenName, accessToken));

                            Intent intent = null;

                            if (!CCStatics.wasGoogleFitEnabled(LoginActivity.this)) {
                                intent = new Intent(LoginActivity.this, DefaultTrackerAuthorizationActivity.class);
                            }
                            else {
                                intent = new Intent(LoginActivity.this, MainActivity.class);
                            }

                            startActivity(intent);
                            LoginActivity.this.finish();
                        }
                        else {
                            dismissWithCheck(mLoadingDialog);
                            showErrorToast();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        dismissWithCheck(mLoadingDialog);
                        showErrorToast();
                    }
                    finally {
                        response.body().close();
                    }
                }
            });
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        // Check if 'Join With Event Code' button was pressed in FirstTimeLaunchActivity
        if (getIntent().getBooleanExtra("firstTimeJoin", false)) {
            registerEventCodePressed();
        }

        if (BuildConfig.BUILD_TYPE.equals("debug")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                }
            }

        }

    }

    /**
     * Function used to validate login form input
     * @return boolean whether or not the form is valid
     */
    private boolean validateInput() {

        boolean validForm = true;

        // Validate that text is an e-mail address
        if (!isValidEmail(emailField.getText().toString())) {
            validForm = false;
        }
        // Validate that text is longer than 0 characters
        else if (emailField.getText().toString().length() == 0) {
            validForm = false;
        }

        // Validate that text is longer than 0 characters
        if (passwordField.getText().toString().length() == 0) {
            validForm = false;
        }

        return validForm;
    }

    /**
     * Function to show toast message with login error message
     */
    private void showErrorToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, R.string.unauthorized_message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChallengeErrorToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, R.string.challenge_not_found, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoInternetConnectionToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showResultToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChallengeEndedToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, "Sorry! This challenge has already finished", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidEmail(String text) {
        return (text.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "You cannot use our app as you have denied the permission. Please change your Settings!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
