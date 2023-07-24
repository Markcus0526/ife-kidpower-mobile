package org.unicefkidpower.schools;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.swrve.sdk.SwrveSDK;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.schools.context.UserContext;
import org.unicefkidpower.schools.define.config;
import org.unicefkidpower.schools.event.EventManager;
import org.unicefkidpower.schools.event.SEvent;
import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.model.UserManager;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.UserService;
import org.unicefkidpower.schools.ui.KPButton;
import org.unicefkidpower.schools.ui.KPEditText;
import org.unicefkidpower.schools.ui.UiUtils;

import java.util.HashMap;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Ruifeng Shi on 8/25/2016.
 */
public class LoginActivity extends BaseActivityWithNavBar implements View.OnClickListener {
	public static final String TAG = "Login Activity";

	private static final int MODE_LOGIN = 0x0;
	private static final int MODE_PASSWORD_RESET = 0x1;

	private static final String EVENT_SIGNIN_SUCCESS = "EVENT_SIGNIN_SUCCESS";
	private static final String EVENT_SIGNIN_FAILURE = "EVENT_SIGNIN_FAILURE";
	private static final String EVENT_RESETPASS_SUCCESS = "EVENT_RESETPASS_SUCCESS";
	private static final String EVENT_RESETPASS_FAILURE = "EVENT_RESETPASS_FAILURE";

	private KPEditText editEmail, editPassword;
	private KPButton btnLogin, btnPasswordReset, btnForgotPassword;

	private int mode = MODE_LOGIN;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		editEmail = (KPEditText) findViewById(R.id.edit_email);
		editPassword = (KPEditText) findViewById(R.id.edit_password);

		btnLogin = (KPButton) findViewById(R.id.btn_login);
		btnPasswordReset = (KPButton) findViewById(R.id.btn_password_reset);
		btnForgotPassword = (KPButton) findViewById(R.id.btn_forgot_password);

		btnLogin.setOnClickListener(this);
		btnPasswordReset.setOnClickListener(this);
		btnForgotPassword.setOnClickListener(this);

		ImageView ivDummy = (ImageView) findViewById(R.id.iv_dummy);
		ViewGroup.LayoutParams lpDummy = ivDummy.getLayoutParams();
		lpDummy.width = ResolutionSet.getScreenSize(this, false, true).x;
		lpDummy.height = ResolutionSet.getScreenSize(this, false, true).y -
				ResolutionSet.getStatusBarHeight(this);
		ivDummy.setLayoutParams(lpDummy);

		applyMode(mode);

		if (UserContext.sharedInstance().isLoggedIn()) {
			String strEmail = UserContext.sharedInstance().lastUserName();
			String strPassword = UserContext.sharedInstance().lastUserPassword();
			editEmail.setText(strEmail);
			editPassword.setText(strPassword);

			this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

			onLoginButtonClicked();
		}
	}

	@Override
	protected boolean isUseEvent() {
		return true;
	}

	@Override
	public String getActionBarTitle() {
		return getString(R.string.log_in);
	}

	@Override
	protected boolean shouldCheckForUpdate() {
		return !UserContext.sharedInstance().isLoggedIn();
	}

	private void onLoginButtonClicked() {
		// check email & password
		final String email = editEmail.getText().toString();
		String password = editPassword.getText().toString();

		UiUtils.hideKeyboard(this);

		Logger.log(TAG, "Sign in button clicked - Email: %s", email);

		if (config.USE_LOGIC) {
			// try to login
			UIManager.sharedInstance().showProgressDialog(this, null, getString(R.string.app_onemoment), true);

			ServerManager.sharedInstance().login(email, password, new RestCallback<UserService.ResLoginForSuccess>() {
				@Override
				public void success(UserService.ResLoginForSuccess resLoginForSuccess, Response response) {
					FlurryAgent.setUserId(email);
					FlurryAgent.onEvent("Sign_in");

					Crashlytics.setUserIdentifier(resLoginForSuccess.id + "");
					Crashlytics.setUserEmail(email);
					Crashlytics.setUserName(resLoginForSuccess.firstName + " " + resLoginForSuccess.lastName);

					Map<String, String> attributes = new HashMap<>();
					attributes.put("email", email);
					attributes.put("handle", email);
					SwrveSDK.userUpdate(attributes);

					UIManager.sharedInstance().dismissProgressDialog();
					EventManager.sharedInstance().post(EVENT_SIGNIN_SUCCESS, resLoginForSuccess);
				}

				@Override
				public void failure(RetrofitError retrofitError, String message) {
					UIManager.sharedInstance().dismissProgressDialog();
					EventManager.sharedInstance().post(EVENT_SIGNIN_FAILURE, message);
//					if (retrofitError.getResponse().getStatus() == 401)
//						EventManager.sharedInstance().post(EVENT_SIGNIN_FAILURE, getString(R.string.password_incorrect));
//					else

				}
			});
		} else {
			EventManager.sharedInstance().post(EVENT_SIGNIN_SUCCESS);
		}
	}

	private void onPasswordResetButtonClicked() {
		// check email & password
		final String email = editEmail.getText().toString();

		Logger.log(TAG, "Send me Reset Instructions button clicked - Email: %s", email);

		UiUtils.hideKeyboard(this);

		if (config.USE_LOGIC) {
			// try to login
			UIManager.sharedInstance().showProgressDialog(this, null, getString(R.string.app_onemoment), true);

			Logger.log(TAG, "Attempting to send reset instructions - Email: %s", email);

			ServerManager.sharedInstance().sendResetLink(email, new RestCallback<UserService.ResSendResetLinkForSuccess>() {
				@Override
				public void success(UserService.ResSendResetLinkForSuccess resSendResetLinkForSuccess, Response response) {
					Logger.log(TAG, "Reset instructions have successfully been sent - Email: %s", email);

					UIManager.sharedInstance().dismissProgressDialog();
					EventManager.sharedInstance().post(EVENT_RESETPASS_SUCCESS, resSendResetLinkForSuccess);
				}

				@Override
				public void failure(RetrofitError retrofitError, String message) {
					Logger.error(TAG, "Sending reset instructions has been failed");

					UIManager.sharedInstance().dismissProgressDialog();
					EventManager.sharedInstance().post(EVENT_RESETPASS_FAILURE, message);
				}
			});
		} else {
			EventManager.sharedInstance().post(EVENT_RESETPASS_SUCCESS);
		}
	}

	private void onForgotPasswordButtonClicked() {
		mode = 1 - mode;
		applyMode(mode);
	}

	private void applyMode(int mode) {
		switch (mode) {
			case MODE_LOGIN:
				btnLogin.setVisibility(View.VISIBLE);
				btnPasswordReset.setVisibility(View.GONE);
				btnForgotPassword.setText(R.string.trouble_logging_in_underlined);
				editPassword.setVisibility(View.VISIBLE);
				break;

			case MODE_PASSWORD_RESET:
				btnLogin.setVisibility(View.GONE);
				btnPasswordReset.setVisibility(View.VISIBLE);
				btnForgotPassword.setText(R.string.i_remember_now_underlined);
				editPassword.setVisibility(View.INVISIBLE);
				break;
		}
	}

	protected void onLoginSuccess() {
		String strEmail = editEmail.getText().toString();
		String strPassword = editPassword.getText().toString();

		UserContext.sharedInstance().setLoggedIn(true);
		UserContext.sharedInstance().setSignUp(false);
		UserContext.sharedInstance().setLastUserName(strEmail);
		UserContext.sharedInstance().setLastUserPassword(strPassword);

		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent e) {
		if (EVENT_SIGNIN_SUCCESS.equals(e.name)) {
			// set current user
			UserManager.sharedInstance()._currentUser =
					UserManager.sharedInstance().parseUserForLogin
							((UserService.ResLoginForSuccess) e.object);
			//UserContext.sharedInstance().setCrashUserInfo(user._firstName + " " + user._lastName, user._email);

			onLoginSuccess();
		} else if (EVENT_SIGNIN_FAILURE.equals(e.name)) {
			if (UserContext.sharedInstance().isLoggedIn()) {
				UserContext.sharedInstance().setLoggedIn(false);
				pushNewActivityAnimated(WelcomeActivity.class);
				finish();
			} else {
				String errorMsg = (String) e.object;
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
						errorMsg,
						LoginActivity.this);
			}
		} else if (EVENT_RESETPASS_SUCCESS.equals(e.name)) {
			// set current user
			UserService.ResSendResetLinkForSuccess retData = (UserService.ResSendResetLinkForSuccess) e.object;
			if (retData.status == 200) {
				applyMode(MODE_LOGIN);
			}
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_success),
					retData.message,
					LoginActivity.this);
		} else if (EVENT_RESETPASS_FAILURE.equals(e.name)) {
			String errorMsg = (String) e.object;
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					errorMsg,
					LoginActivity.this);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_login:
				onLoginButtonClicked();
				break;

			case R.id.btn_password_reset:
				onPasswordResetButtonClicked();
				break;

			case R.id.btn_forgot_password:
				onForgotPasswordButtonClicked();
				break;
		}
	}


	@Override
	public void onBackClicked() {
		pushNewActivityAnimated(WelcomeActivity.class);
		finish();
	}


	@Override
	public boolean shouldShowMenu() {
		return false;
	}


	@Override
	public boolean shouldShowBack() {
		return true;
	}
}
