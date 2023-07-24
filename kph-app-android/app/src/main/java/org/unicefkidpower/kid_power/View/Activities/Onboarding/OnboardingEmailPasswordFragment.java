package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.ErrorMessageDict;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHEditText;
import org.unicefkidpower.kid_power.View.CustomControls.KPHSegmentedGroup;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

import java.util.Date;

/**
 * Created by Ruifeng Shi on 8/29/2015.
 */
public class OnboardingEmailPasswordFragment extends SuperFragment {
	// UI Elements
	private View				contentView					= null;

	private KPHTextView			txtUsername					= null;
	private KPHTextView			txtPasswordDescription		= null;
	private KPHTextView			txtEmailError				= null;
	private KPHTextView			txtPasswordError			= null;
	private KPHTextView			txtEmail					= null;

	private KPHEditText			editEmail					= null;
	private KPHEditText			editPassword				= null;
	private ImageView			ivAvatar					= null;
	private KPHSegmentedGroup	segmentedGender				= null;
	private KPHButton			btnNext						= null;
	// End of 'UI Elements'


	// Variables
	public String				sUsername					= "";
	public String				sAvatarId					= "";
	public String				sEmail						= "";
	public String				sPassword					= "";
	public String				sGender						= "";
	public Date					aBirthday					= new Date();
	public boolean				isCreatingFamilyAccount		= false;
	// End of 'Variables'


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		txtUsername = (KPHTextView) contentView.findViewById(R.id.txt_username);
		txtPasswordDescription = (KPHTextView) contentView.findViewById(R.id.txt_password);

		if (sUsername != null) {
			txtUsername.setText(sUsername);
			txtPasswordDescription.setText(getSafeContext().getString(R.string.create_a_password_for, sUsername));
		}

		txtEmail = (KPHTextView) contentView.findViewById(R.id.txt_email);
		editEmail = (KPHEditText) contentView.findViewById(R.id.edit_email);
		editEmail.setText(sEmail);
		editEmail.setOnKeyListener(new KPHEditText.onKeyListener() {
			@Override
			public void onKeyDownEnter() {
				if (!KPHUtils.sharedInstance().isEmailValid(editEmail.getText().toString())) {
					txtEmailError.setText(R.string.email_syntax_invalid);
					txtEmailError.setVisibility(View.VISIBLE);
				} else {
					txtEmailError.setText("");
					txtEmailError.setVisibility(View.GONE);
				}
			}
		});
		editEmail.setOnFocusChangeListener(
				new View.OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (!hasFocus) {
							if (!KPHUtils.sharedInstance().isEmailValid(editEmail.getText().toString())) {
								txtEmailError.setText(R.string.email_syntax_invalid);
								txtEmailError.setVisibility(View.VISIBLE);
							} else {
								txtEmailError.setText("");
								txtEmailError.setVisibility(View.GONE);
							}
						}
					}
				}
		);

		editPassword = (KPHEditText) contentView.findViewById(R.id.edit_password);
		editPassword.setText(sPassword);
		editPassword.setOnKeyListener(new KPHEditText.onKeyListener() {
			@Override
			public void onKeyDownEnter() {
				if (!KPHUtils.sharedInstance().isPasswordValid(editPassword.getText().toString())) {
					txtPasswordError.setText(R.string.password_invalid);
					txtPasswordError.setVisibility(View.VISIBLE);
				} else {
					txtPasswordError.setText("");
					txtPasswordError.setVisibility(View.GONE);
				}
			}
		});
		editPassword.setOnFocusChangeListener(
				new View.OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (!hasFocus) {
							if (!KPHUtils.sharedInstance().isPasswordValid(editPassword.getText().toString())) {
								txtPasswordError.setText(R.string.password_invalid);
								txtPasswordError.setVisibility(View.VISIBLE);
							} else {
								txtPasswordError.setText("");
								txtPasswordError.setVisibility(View.GONE);
							}
						}
					}
				}
		);

		txtEmailError = (KPHTextView) contentView.findViewById(R.id.txt_email_invalid_error);
		txtPasswordError = (KPHTextView) contentView.findViewById(R.id.txt_password_invalid_error);
		ivAvatar = (ImageView) contentView.findViewById(R.id.ivAvatar);

		setupAvatarImageView();

		// init gender radio buttons
		segmentedGender = (KPHSegmentedGroup) contentView.findViewById(R.id.segmented_gender);

		if (sGender.equalsIgnoreCase(KPHConstants.GENDER_SKIP)) {
			segmentedGender.check(R.id.radio_skip);
		} else if (sGender.equalsIgnoreCase(KPHConstants.GENDER_MALE)) {
			segmentedGender.check(R.id.radio_male);
		} else if (sGender.equalsIgnoreCase(KPHConstants.GENDER_FEMALE)) {
			segmentedGender.check(R.id.radio_female);
		}


		segmentedGender.setOnCheckedChangeListener(
				new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						switch (checkedId) {
							case R.id.radio_skip:
								sGender = KPHConstants.GENDER_SKIP;
								break;
							case R.id.radio_male:
								sGender = KPHConstants.GENDER_MALE;
								break;
							case R.id.radio_female:
								sGender = KPHConstants.GENDER_FEMALE;
								break;
						}
					}
				}
		);

		if (isCreatingFamilyAccount) {
			txtEmail.setVisibility(View.GONE);
			editEmail.setVisibility(View.GONE);
			txtEmailError.setVisibility(View.GONE);
		}

		btnNext = (KPHButton) contentView.findViewById(R.id.btn_next);
		btnNext.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickedNext();
					}
				}
		);

		return contentView;
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_onboarding_email_password;
	}

	@Override
	public void onClickedBackSystemButton() {
		super.onClickedBackSystemButton();

		Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_PROFILE_INFO_GO_BACK);
		intent.putExtra("email", editEmail.getText().toString());
		intent.putExtra("password", editPassword.getText().toString());
		intent.putExtra("gender", sGender);
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
	}

	public void setAvatarId(String avatarId) {
		sAvatarId = avatarId;
	}

	protected void setupAvatarImageView() {
		if (KPHUserService.sharedInstance().getUserData() != null && sAvatarId.length() == 0) {
			sAvatarId = KPHUserService.sharedInstance().getUserData().getAvatarId();
		}
		if (sAvatarId.length() != 0) {
			Drawable avatar = KPHUserService.sharedInstance().getAvatarDrawable(sAvatarId);

			if (avatar != null) {
				ivAvatar.setImageDrawable(avatar);
			}
		}
	}

	private void onClickedNext() {
		// Check if the email is valid
		if (!KPHUtils.sharedInstance().isEmailValid(editEmail.getText().toString()) &&
				!isCreatingFamilyAccount) {
			showBrandedDialog(getString(R.string.please_check_your_email_address));
			editEmail.requestFocus();
			return;
		} else {
			// Hides invalid email error message if the email is valid
			txtEmailError.setText("");
			txtEmailError.setVisibility(View.GONE);
		}

		// Check if the password is valid
		if (editPassword.getText().toString().length() == 0) {
			showBrandedDialog(getString(R.string.your_password_cant_be_blank));
			editPassword.requestFocus();
			return;
		} else if (!KPHUtils.sharedInstance().isPasswordValid(editPassword.getText().toString())) {
			showBrandedDialog(getSafeContext().getString(R.string.password_invalid));
			editPassword.requestFocus();
			return;
		} else {
			// Hides invalid email error message if the email is valid
			txtPasswordError.setText("");
			txtPasswordError.setVisibility(View.GONE);
		}

		sEmail = editEmail.getText().toString();
		sPassword = editPassword.getText().toString();

		showProgressDialog();
		if (isCreatingFamilyAccount) {
			if (((OnboardingActivity)getParentActivity()).newUserIdentifier == 0) {
				// Create child account
				KPHUserData parentData = KPHUserService.sharedInstance().getUserData();
				KPHUserService.sharedInstance().createSubAccount(
						parentData.getId(),
						sUsername,
						sPassword,
						"",         // Friendly Name
						sGender,
						aBirthday,
						sAvatarId,
						new CreateSubAccountListener()
				);
			} else {
				// Update child account
				UpdateUserDataListener updateUserData_listener = new UpdateUserDataListener();
				updateUserData_listener.userId = ((OnboardingActivity)getParentActivity()).newUserIdentifier;

				KPHUserService.sharedInstance().updateUserData(
						((OnboardingActivity)getParentActivity()).newUserIdentifier,
						sUsername,
						"",
						"",
						sGender,
						aBirthday,
						sAvatarId,
						updateUserData_listener
				);
			}
		} else {
			if (KPHUserService.sharedInstance().getUserData() == null) {
				// Not registered yet
				KPHUserService.sharedInstance().signup(
						sUsername,
						sEmail,
						sPassword,
						"",
						sGender,
						aBirthday,
						sAvatarId,
						new SignupListener()
				);
			} else {
				int userId = KPHUserService.sharedInstance().getUserData().getId();
				UpdateUserDataListener updateUserData_listener = new UpdateUserDataListener();
				updateUserData_listener.userId = userId;

				KPHUserService.sharedInstance().updateUserData(
						userId,
						sUsername,
						sEmail,
						"",
						sGender,
						aBirthday,
						sAvatarId,
						updateUserData_listener
				);
			}
		}
	}


	private class CreateSubAccountListener implements onActionListener {
		public CreateSubAccountListener() {
			super();
		}

		@Override
		public void completed(Object object) {
			dismissProgressDialog();

			if (object != null) {
				KPHUserData familyAccount = (KPHUserData) object;

				if (getParentActivity() instanceof OnboardingActivity) {
					OnboardingActivity onboardingActivity = (OnboardingActivity)getParentActivity();
					onboardingActivity.newUserIdentifier = familyAccount.getId();
					onboardingActivity.newEmail = sEmail;
					onboardingActivity.newPassword = sPassword;
				}

				Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_EMAIL_PASSWORD_SELECTED);
				LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
			} else {
				failed(0, getSafeContext().getString(R.string.default_error));
			}
		}

		@Override
		public void failed(int code, String message) {
			if (getParentActivity() == null)
				return;

			showBrandedDialog(ErrorMessageDict.serverResponse2UserFriendlyMessage(message));
		}
	}


//	public onActionListener createSubAccount_listener = new onActionListener() {
//		@Override
//		public void completed(Object object) {
//			dismissProgressDialog();
//
//			if (object != null) {
//				KPHUserData familyAccount = (KPHUserData) object;
//
//				if (getParentActivity() instanceof OnboardingActivity) {
//					OnboardingActivity onboardingActivity = (OnboardingActivity)getParentActivity();
//					onboardingActivity.newUserIdentifier = familyAccount.getId();
//					onboardingActivity.newEmail = sEmail;
//					onboardingActivity.newPassword = sPassword;
//				}
//
//				Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_EMAIL_PASSWORD_SELECTED);
//				LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
//			} else {
//				failed(0, getSafeContext().getString(R.string.default_error));
//			}
//		}
//
//		@Override
//		public void failed(int code, String message) {
//			if (getParentActivity() == null)
//				return;
//
//			showBrandedDialog(ErrorMessageDict.serverResponse2UserFriendlyMessage(message));
//		}
//	};


	private class SignupListener implements onActionListener {
		public SignupListener() {
			super();
		}

		@Override
		public void completed(Object object) {
			dismissProgressDialog();

			if (object != null) {
				KPHUserData kphUserData = (KPHUserData) object;

				// Save user information
				KPHUserService.sharedInstance().shouldReloadUserData();
				KPHUserService.sharedInstance().saveUserData(kphUserData);
				KPHUserService.sharedInstance().saveLoginData(kphUserData.getHandle(), sPassword);

				RestService.setUserToken(kphUserData.getAccessToken());

				if (getParentActivity() instanceof OnboardingActivity) {
					OnboardingActivity onboardingActivity = (OnboardingActivity)getParentActivity();
					onboardingActivity.newUserIdentifier = kphUserData.getId();
					onboardingActivity.newEmail = sEmail;
					onboardingActivity.newPassword = sPassword;
				}

				Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_EMAIL_PASSWORD_SELECTED);
				LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
			} else {
				failed(0, getSafeContext().getString(R.string.default_error));
			}
		}

		@Override
		public void failed(int code, String message) {
			showBrandedDialog(ErrorMessageDict.serverResponse2UserFriendlyMessage(message));
		}
	}


//	private onActionListener signup_listener = new onActionListener() {
//		@Override
//		public void completed(Object object) {
//			dismissProgressDialog();
//
//			if (object != null) {
//				KPHUserData kphUserData = (KPHUserData) object;
//
//				// Save user information
//				KPHUserService.sharedInstance().shouldReloadUserData();
//				KPHUserService.sharedInstance().saveUserData(kphUserData);
//				KPHUserService.sharedInstance().saveLoginData(kphUserData.getHandle(), sPassword);
//
//				RestService.setUserToken(kphUserData.getAccessToken());
//
//				if (getParentActivity() instanceof OnboardingActivity) {
//					OnboardingActivity onboardingActivity = (OnboardingActivity)getParentActivity();
//					onboardingActivity.newUserIdentifier = kphUserData.getId();
//					onboardingActivity.newEmail = sEmail;
//					onboardingActivity.newPassword = sPassword;
//				}
//
//				Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_EMAIL_PASSWORD_SELECTED);
//				LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
//			} else {
//				failed(0, getSafeContext().getString(R.string.default_error));
//			}
//		}
//
//		@Override
//		public void failed(int code, String message) {
//			showBrandedDialog(ErrorMessageDict.serverResponse2UserFriendlyMessage(message));
//		}
//	};



	private class UpdateUserDataListener implements onActionListener {
		public int userId = 0;

		public UpdateUserDataListener() {
			super();
			userId = 0;
		}

		@Override
		public void completed(Object object) {
			KPHUserService.sharedInstance().saveUserData((KPHUserData) object);
			KPHUserService.sharedInstance().updateUserPassword(
					userId,
					sPassword,
					new UpdatePasswordListener());
		}

		@Override
		public void failed(int code, String message) {
			showBrandedDialog(ErrorMessageDict.serverResponse2UserFriendlyMessage(message));
		}
	}


//	private onActionListener updateUserData_listener = new onActionListener() {
//		@Override
//		public void completed(Object object) {
//			KPHUserService.sharedInstance().saveUserData((KPHUserData) object);
//
//			int userId = KPHUserService.sharedInstance().getUserData().getId();
//			KPHUserService.sharedInstance().updateUserPassword(
//					userId,
//					sPassword,
//					updatePassword_listener);
//		}
//
//		@Override
//		public void failed(int code, String message) {
//			showBrandedDialog(ErrorMessageDict.serverResponse2UserFriendlyMessage(message));
//		}
//	};


	private class UpdatePasswordListener implements onActionListener {
		public UpdatePasswordListener() {
			super();
		}

		@Override
		public void completed(Object object) {
			dismissProgressDialog();

			if (object != null) {
				KPHUserData userData = (KPHUserData) object;
				KPHUserService.sharedInstance().saveLoginData(userData.getHandle(), sPassword);

				if (getParentActivity() instanceof OnboardingActivity) {
					OnboardingActivity onboardingActivity = (OnboardingActivity)getParentActivity();
					onboardingActivity.newUserIdentifier = userData.getId();
					onboardingActivity.newEmail = sEmail;
					onboardingActivity.newPassword = sPassword;
				}

				Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_EMAIL_PASSWORD_SELECTED);
				LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
			} else {
				failed(0, getSafeContext().getString(R.string.default_error));
			}
		}

		@Override
		public void failed(int code, String message) {
			showBrandedDialog(ErrorMessageDict.serverResponse2UserFriendlyMessage(message));
		}
	}


//	private onActionListener updatePassword_listener = new onActionListener() {
//		@Override
//		public void completed(Object object) {
//			dismissProgressDialog();
//
//			if (object != null) {
//				KPHUserData userData = (KPHUserData) object;
//				KPHUserService.sharedInstance().saveLoginData(userData.getHandle(), sPassword);
//
//				if (getParentActivity() instanceof OnboardingActivity) {
//					OnboardingActivity onboardingActivity = (OnboardingActivity)getParentActivity();
//					onboardingActivity.newUserIdentifier = userData.getId();
//					onboardingActivity.newEmail = sEmail;
//					onboardingActivity.newPassword = sPassword;
//				}
//
//				Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_EMAIL_PASSWORD_SELECTED);
//				LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
//			} else {
//				failed(0, getSafeContext().getString(R.string.default_error));
//			}
//		}
//
//		@Override
//		public void failed(int code, String message) {
//			showBrandedDialog(ErrorMessageDict.serverResponse2UserFriendlyMessage(message));
//		}
//	};


}
