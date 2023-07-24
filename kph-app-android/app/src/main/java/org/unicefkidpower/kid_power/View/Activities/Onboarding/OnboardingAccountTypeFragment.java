package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.apache.http.HttpStatus;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Model.MicroService.Facebook.FacebookService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.Model.Structure.KPHFacebook;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.CustomControls.KPHBrandedDialog;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

import retrofit.client.Response;

/**
 * Created by Ruifeng Shi on 12/8/2016.
 */

public class OnboardingAccountTypeFragment extends SuperFragment {
	private ImageView				ivAvatar			= null;
	private KPHTextView				txtUsername			= null;

	private String					avatarId			= null;
	private String					username			= null;
	private String					birthday			= null;

	private FacebookService			facebookService		= null;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View contentView = super.onCreateView(inflater, container, savedInstanceState);

		ivAvatar = (ImageView) contentView.findViewById(R.id.ivAvatar);
		txtUsername = (KPHTextView) contentView.findViewById(R.id.txt_username);
		contentView.findViewById(R.id.btn_email).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onEmailButtonClicked();
			}
		});
		contentView.findViewById(R.id.btn_facebook).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onFacebookButtonClicked();
			}
		});

		if (!TextUtils.isEmpty(avatarId)) {
			ivAvatar.setImageDrawable(KPHUserService.sharedInstance().getAvatarDrawable(avatarId));
		}

		txtUsername.setText(TextUtils.isEmpty(username) ? "" : username);

		return contentView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_onboarding_account_type;
	}


	private void onEmailButtonClicked() {
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(
				new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_SETUP_EMAIL_SELECTED)
		);
	}

	private void onFacebookButtonClicked() {
		if (facebookService == null)
			facebookService = FacebookService.getSharedInstance(getSafeContext().getApplicationContext());

		facebookService.clearAccountData();
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
				facebook.handle = username;
				facebook.gender = gender;
				facebook.dob = birthday.length() == 0 ? OnboardingAccountTypeFragment.this.birthday : birthday;

				showProgressDialog();
				KPHUserService.sharedInstance().signupFBAccount(facebook, new RestCallback<KPHUserData>() {
					@Override
					public void failure(RestError restError) {
						showBrandedDialog(KPHUtils.sharedInstance().getNonNullMessage(restError), getSafeContext().getString(R.string.ok), null, null);
					}

					@Override
					public void success(final KPHUserData kphUserData, Response response) {
						dismissProgressDialog();

						if (response.getStatus() == HttpStatus.SC_OK) {
							showBrandedDialog(getString(R.string.already_registered_fb_user), getString(R.string.ok), null, new KPHBrandedDialog.KPHBrandedDialogCallback() {
								@Override
								public void onDefaultButtonClicked() {
									signupFBSuccess(kphUserData, facebook, true);
								}
								@Override
								public void onOtherButtonClicked() {}
							});
						} else {
							signupFBSuccess(kphUserData, facebook, false);
						}
					}
				});
			}

			@Override
			public void onFailure(int errorCode, String errorMessage) {
				showErrorDialog(errorMessage);
			}

			@Override
			public void onCancelled() {
				showErrorDialog(getSafeContext().getString(R.string.cancelled));
			}
		});
	}

	public void setData(String username, String avatarId, String birthday) {
		this.username = username;
		this.avatarId = avatarId;
		this.birthday = birthday;
	}

	private void signupFBSuccess(KPHUserData userData, KPHFacebook facebookField, boolean redirectToPassport) {
		// Save user information
		KPHUserService.sharedInstance().shouldReloadUserData();
		KPHUserService.sharedInstance().saveUserData(userData);
		KPHUserService.sharedInstance().saveLoginData(userData.getHandle(), "");

		RestService.setUserToken(userData.getAccessToken());

		if (facebookField != null) {
			KPHUserService.sharedInstance().saveFBData(
					facebookField.facebook.fbId,
					facebookField.facebook.email,
					facebookField.facebook.accessToken
			);
		}

		SuperActivity parentActivity = getParentActivity();
		if (parentActivity == null)
			return;

		if (parentActivity instanceof OnboardingActivity) {
			OnboardingActivity onboardingActivity = (OnboardingActivity)parentActivity;
			onboardingActivity.newUserIdentifier = userData.getId();
			onboardingActivity.newEmail = userData.getEmail();
			onboardingActivity.newPassword = "";
		}

		if (redirectToPassport) {
			Bundle extra = new Bundle();
			extra.putBoolean(OnboardingActivity.EXTRA_SHOW_AUTOSTART_MISSION, false);
			parentActivity.pushNewActivityAnimated(MainActivity.class, SuperActivity.AnimConst.ANIMDIR_NONE, extra);
			parentActivity.popOverCurActivityAnimated();
		} else {
			Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_EMAIL_PASSWORD_SELECTED);
			LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (facebookService != null) {
			facebookService.onActivityResult(requestCode, resultCode, data);
		}
	}
}
