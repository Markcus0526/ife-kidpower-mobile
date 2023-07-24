package org.caloriecloud.android.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.caloriecloud.android.BuildConfig;
import org.caloriecloud.android.R;
import org.caloriecloud.android.model.User;
import org.caloriecloud.android.ui.CCEditText;
import org.caloriecloud.android.util.APIManager;
import org.caloriecloud.android.util.CCStatics;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ExistingAccountActivity extends AppCompatActivity {

    public final String TAG = this.getClass().getSimpleName();
    private Context mContext;

    @BindView(R.id.emailField)
    CCEditText emailField;

    @BindView(R.id.passwordField)
    CCEditText passwordField;

    @OnClick(R.id.forgotPassword)
    void forgotPasswordButtonPressed() {
        final View root = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);

        AlertDialog resetPasswordDialog = new AlertDialog.Builder(mContext, R.style.DialogTheme)
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

    @OnClick(R.id.noAccount)
    void noAccountButtonPressed() {
        ExistingAccountActivity.this.finish();
    }

    @OnClick(R.id.loginButton)
    void loginButtonPressed() {
        if (validateInput()) {
            final ProgressDialog mLoadingDialog = CCStatics.showProgressDialog(mContext, null, getString(R.string.logging_in));

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

                            CCStatics.setSavedUser(mContext, new User(userId, email, screenName, accessToken));

                            setResult(CCStatics.EXISTING_ACCOUNT_RESULT_CODE_SUCCESS);

                            ExistingAccountActivity.this.finish();
                        }
                        else {
                            dismissWithCheck(mLoadingDialog);
                            showErrorToast();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        mLoadingDialog.dismiss();
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
        setContentView(R.layout.activity_existing_account);
        ButterKnife.bind(this);
        mContext = this;
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

    private void showResultToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showErrorToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, R.string.unauthorized_message, Toast.LENGTH_SHORT).show();
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

}
