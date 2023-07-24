package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.unicefkidpower.kid_power.Misc.Config;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.Facebook.FacebookService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.Model.Structure.KPHFacebook;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;
import org.unicefkidpower.kid_power.View.Super.SuperNormalSizeDialogFragment;

import retrofit.client.Response;

/**
 * Created by Ruifeng Shi on 3/9/2017.
 */

public class LoginFragment extends SuperNormalSizeDialogFragment {
	private final String			TAG						= "LoginFragment";

	private View					rootView				= null;
	private ScrollView				mainScrollView			= null;
	private RelativeLayout			mainScrollLayout		= null;
	private LinearLayout			mainContentLayout		= null;

	private KPHButton				loginButton				= null;
	private KPHTextView				resetPwdTextView		= null;

	private KPHTextView				fbLoginLabel			= null;
	private KPHButton				fbLoginButton			= null;

	private EditText				usernameEdit			= null;
	private EditText				passwordEdit			= null;

	private FacebookService			facebookService			= null;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = super.onCreateView(inflater, container, savedInstanceState);

		initControls();

		return rootView;
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_login;
	}

	@Override
	public void startAction() {}

	private void initControls() {
		ImageButton closeButton = (ImageButton)rootView.findViewById(R.id.btnClose);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedClose();
			}
		});

		loginButton = (KPHButton) rootView.findViewById(R.id.btnLogin);
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onLoginButtonClicked();
			}
		});

		usernameEdit = (EditText) rootView.findViewById(R.id.editUsername);
		passwordEdit = (EditText) rootView.findViewById(R.id.editPassword);

		SpannableString ss = new SpannableString(getString(R.string.trouble_logging_in_reset_your_password));
		ClickableSpan clickableSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onForgotPasswordButtonClicked();
			}
			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setUnderlineText(true);
				ds.setColor(Color.WHITE);
			}
		};
		ss.setSpan(clickableSpan, 20, 39, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		resetPwdTextView = (KPHTextView) rootView.findViewById(R.id.txt_reset_password);
		resetPwdTextView.setText(ss);
		resetPwdTextView.setMovementMethod(LinkMovementMethod.getInstance());
		resetPwdTextView.setHighlightColor(Color.TRANSPARENT);

		fbLoginButton = (KPHButton) rootView.findViewById(R.id.btn_facebook_login);
		fbLoginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onClickedFacebookLogin();
			}
		});


		mainScrollLayout = (RelativeLayout) rootView.findViewById(R.id.main_scrollcontent_layout);

		mainScrollView = (ScrollView) rootView.findViewById(R.id.svContent);
		mainScrollView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				if (mainScrollLayout.getHeight() < mainScrollView.getHeight() && mainScrollLayout.getHeight() != 0) {
					ViewGroup.LayoutParams params = mainScrollLayout.getLayoutParams();
					if (params == null) {
						params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mainScrollView.getHeight());
					} else {
						params.height = mainScrollView.getHeight();
					}

					Logger.log(TAG, "Set height : " + params.height);

					mainScrollLayout.setLayoutParams(params);
				}
			}
		});


		if (!Config.USE_FACEBOOK_FEATURE) {
			mainContentLayout = (LinearLayout) rootView.findViewById(R.id.main_content_layout);
			fbLoginLabel = (KPHTextView) rootView.findViewById(R.id.txt_created_with_facebook);

			mainContentLayout.setPadding(mainContentLayout.getPaddingLeft(), 0, mainContentLayout.getPaddingRight(), mainContentLayout.getPaddingBottom());
			fbLoginLabel.setVisibility(View.GONE);
			fbLoginButton.setVisibility(View.GONE);
		}
	}


	private void onClickedClose() {
		LoginFragment.this.dismiss();
	}


	private void onLoginButtonClicked() {
		String userName = usernameEdit.getText().toString();
		final String password = passwordEdit.getText().toString();

		if (userName.isEmpty()) {
			UIManager.sharedInstance().showToastMessage(getSafeContext(), getString(R.string.empty_user_name_alert));
			return;
		}

		if (password.isEmpty()) {
			UIManager.sharedInstance().showToastMessage(getSafeContext(), getString(R.string.empty_password_alert));
			return;
		}

		showProgressDialog(getString(R.string.signing_in));
		KPHUserService.sharedInstance().signin(userName, password, new RestCallback<KPHUserData>() {
			@Override
			public void success(KPHUserData userData, Response response) {
				dismissProgressDialog();
				loginSuccess(userData, password, null);
			}

			@Override
			public void failure(RestError restError) {
				dismissProgressDialog();

				if (restError == null || restError.getName().equalsIgnoreCase("Error")) {
					showErrorDialog(getString(R.string.invalid_login));
				} else {
					getParentActivity().showAlertDialog(restError.getName(), KPHUtils.sharedInstance().getNonNullMessage(restError), null);
				}
			}
		});
	}

	/**
	 * Called when "Forgot username or password?" button has been clicked
	 */
	private void onForgotPasswordButtonClicked() {
		ResetPasswordFragment resetPasswordFragment = new ResetPasswordFragment();
		resetPasswordFragment.setShouldShowEmailBox(true);
		resetPasswordFragment.setUsername(usernameEdit.getText().toString());
		getParentActivity().showDialogFragment(resetPasswordFragment);
	}


	private void onClickedFacebookLogin() {
		if (facebookService == null)
			facebookService = FacebookService.getSharedInstance(getSafeContext().getApplicationContext());

		facebookService.getProfile(getParentActivity(), new FacebookService.OnProfileResultListener() {
			@Override
			public void onSuccess(String accessToken, String email, String gender, String birthday, String fbId) {
				if (email.length() == 0) {
					showErrorDialog(getString(R.string.email_not_permitted_access));
					return;
				}

				final KPHFacebook facebook = new KPHFacebook();
				facebook.facebook.accessToken = accessToken;
				facebook.facebook.email = email;
				facebook.facebook.fbId = fbId;

				showProgressDialog();
				KPHUserService.sharedInstance().signinFBAccount(facebook, new onActionListener() {
					@Override
					public void completed(Object object) {
						dismissProgressDialog();
						loginSuccess((KPHUserData)object, "", facebook);
					}

					@Override
					public void failed(int code, String message) {
						showErrorDialog(message);
					}
				});
			}

			@Override
			public void onFailure(int errorCode, String errorMessage) {
				Toast.makeText(getSafeContext(), errorMessage, Toast.LENGTH_LONG).show();
			}

			@Override
			public void onCancelled() {
				Toast.makeText(getSafeContext(), R.string.cancelled, Toast.LENGTH_LONG).show();
			}
		});
	}

	private void loginSuccess(KPHUserData userData, String password, KPHFacebook facebookField) {
		RestService.setUserToken(userData.getAccessToken());
		KPHUserService.sharedInstance().shouldReloadUserData();
		KPHUserService.sharedInstance().setChildRestrictedFlag(false);
		KPHUserService.sharedInstance().saveSignInFlag();
		KPHUserService.sharedInstance().saveLoginData(userData.getHandle(), password);
		KPHUserService.sharedInstance().saveUserData(userData);

		if (facebookField != null) {
			KPHUserService.sharedInstance().saveFBData(facebookField.facebook.fbId, facebookField.facebook.email, facebookField.facebook.accessToken);
		}

		if (getParentActivity() == null)
			return;

		SuperActivity parentActivity = getParentActivity();
		parentActivity.pushNewActivityAnimated(
				MainActivity.class,
				SuperActivity.AnimConst.ANIMDIR_NONE,
				Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION,
				0
		);
		parentActivity.overridePendingTransition(0, 0);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (facebookService != null)
			facebookService.onActivityResult(requestCode, resultCode, data);
	}

}
