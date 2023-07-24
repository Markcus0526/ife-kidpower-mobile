package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import org.apache.http.HttpStatus;
import org.unicefkidpower.kid_power.Misc.Config;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.Facebook.FacebookService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.ErrorMessageDict;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.Model.Structure.KPHFacebook;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Adapters.CreateParentAccountPagerAdapter;
import org.unicefkidpower.kid_power.View.CustomControls.KPHBrandedDialog;
import org.unicefkidpower.kid_power.View.Super.SuperDialogFragment;

import java.util.Calendar;

import retrofit.client.Response;

/**
 * Created by Ruifeng Shi on 12/16/2016.
 */

public class CreateParentAccountDialogFragment extends SuperDialogFragment {
	private final String						TAG										= "CreateParentAccountDialogFragment";

	public static int							PARENT_ACCOUNT_PAGE_START				= 0;
	public static int							PARENT_ACCOUNT_PAGE_ACCOUNT_TYPE		= 1;
	public static int							PARENT_ACCOUNT_PAGE_USERNAME_PWD		= 2;
	public static int							PARENT_ACCOUNT_PAGE_EMAIL_GENDER_BIRTH	= 3;
	public static int							PARENT_ACCOUNT_PAGE_FINISH				= 4;

	public int									defaultPage								= PARENT_ACCOUNT_PAGE_START;

	public ViewPager							viewPager								= null;
	public CreateParentAccountPagerAdapter		createParentAccountPagerAdapter			= null;
	public DialogInterface.OnKeyListener		onKeyListener							= null;

	private IntentFilter						intentFilter			= null;
	private BroadcastReceiver					receiver				= null;

	private int									dialogHeight			= 0;

	private FacebookService						facebookService			= null;
	private KPHFacebook							facebookField			= null;

	// Child user info data
	private String								childUserName			= "";

	// User info data
	private String								username				= "";
	private String								password				= "";
	private String								email					= "";
	private String								gender					= "";
	private int									birthYear				= 0;
	private int									birthMonth				= 0;
	private int									birthDay				= 0;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View vwRoot = super.onCreateView(inflater, container, savedInstanceState);

		// Initialize page indexes
		if (Config.USE_FACEBOOK_FEATURE) {
			PARENT_ACCOUNT_PAGE_START				= 0;
			PARENT_ACCOUNT_PAGE_ACCOUNT_TYPE		= 1;
			PARENT_ACCOUNT_PAGE_USERNAME_PWD		= 2;
			PARENT_ACCOUNT_PAGE_EMAIL_GENDER_BIRTH	= 3;
			PARENT_ACCOUNT_PAGE_FINISH				= 4;
		} else {
			PARENT_ACCOUNT_PAGE_START				= 0;
			PARENT_ACCOUNT_PAGE_ACCOUNT_TYPE		= 4;
			PARENT_ACCOUNT_PAGE_USERNAME_PWD		= 1;
			PARENT_ACCOUNT_PAGE_EMAIL_GENDER_BIRTH	= 2;
			PARENT_ACCOUNT_PAGE_FINISH				= 3;
		}

		if (createParentAccountPagerAdapter == null)
			createParentAccountPagerAdapter = new CreateParentAccountPagerAdapter(getChildFragmentManager());

		viewPager = (ViewPager) vwRoot.findViewById(R.id.viewPager);
		viewPager.setAdapter(createParentAccountPagerAdapter);

		// Disable swipe gesture on viewpager
		viewPager.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		if (receiver == null) {
			receiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (getActivity() == null) {
						// Already detached
						return;
					}

					String action = intent.getAction();
					switch (action) {
						case KPHBroadcastSignals.BROADCAST_SIGNAL_AGE_RESTRICTION_ERROR_YES:
							// Clicked "Yes" button on start page. Go to account type page
							if (Config.USE_FACEBOOK_FEATURE) {
								onAgeRestrictionErrorYesClicked();
							} else {
								onParentAccountSetupMethodEmailClicked();
							}
							break;
						case KPHBroadcastSignals.BROADCAST_SIGNAL_AGE_RESTRICTION_ERROR_NO:
							// Clicked "No" button on start page. Dismiss create parent creation dialog
							onAgeRestrictionErrorNoClicked();
							break;
						case KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_SETUP_METHOD_EMAIL:
							// Selected email account creation. Go to username/password page
							onParentAccountSetupMethodEmailClicked();
							break;
						case KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_SETUP_METHOD_FACEBOOK:
							// Selected facebook account creation. Signup immediately.
							onParentAccountSetupMethodFacebookClicked();
							break;
						case KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_USERNAME_PASSWORD_NEXT_CLICKED:
							// Selected "Next" on username/password page. Need to go to email/gender/birthday page
							onUsernamePasswordNext();
							break;
						case KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_EMAIL_GENDER_DOB_NEXT_CLICKED:
							// Selected "Next" on email/gender/birthday page. Need to go to finish page
							onEmailGenderBirthNext();
							break;
						case KPHBroadcastSignals.BROADCAST_SIGNAL_BACK_CLICKED:
							onClickedSystemBackButton();
							break;
					}
				}
			};
		}


		if (intentFilter == null) {
			intentFilter = new IntentFilter();
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_AGE_RESTRICTION_ERROR_YES);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_AGE_RESTRICTION_ERROR_NO);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_SETUP_METHOD_EMAIL);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_SETUP_METHOD_FACEBOOK);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_USERNAME_PASSWORD_NEXT_CLICKED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_EMAIL_GENDER_DOB_NEXT_CLICKED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_BACK_CLICKED);

			LocalBroadcastManager.getInstance(getSafeContext()).registerReceiver(receiver, intentFilter);
		}

		return vwRoot;
	}


	@Override
	public void onStart() {
		super.onStart();

		Logger.log(TAG, "onStart");

		Dialog dialog = getDialog();
		if (onKeyListener != null)
			dialog.setOnKeyListener(onKeyListener);

		if (dialog != null && dialog.getWindow() != null) {
			int screenWidth = ResolutionSet.getScreenSize(getSafeContext(), false).x;
			int screenHeight = ResolutionSet.getScreenSize(getSafeContext(), false).y;

			View emailGenderBirthContentView = getParentActivity().getLayoutInflater().inflate(R.layout.fragment_create_parent_account_email_gender_birth, null);
			emailGenderBirthContentView.measure(View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.EXACTLY),
					View.MeasureSpec.makeMeasureSpec(1, View.MeasureSpec.UNSPECIFIED));
			dialogHeight = emailGenderBirthContentView.getMeasuredHeight();

			dialog.getWindow().setLayout(screenWidth, ViewGroup.LayoutParams.MATCH_PARENT);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

			int margins = getResources().getDimensionPixelSize(R.dimen.dimen_margin_10);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dialogHeight);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params.setMargins(margins, margins, margins, margins);
			viewPager.setLayoutParams(params);

			ObjectAnimator slideUp = ObjectAnimator.ofPropertyValuesHolder(
					dialog.getWindow().getDecorView(),
					PropertyValuesHolder.ofFloat("translationY", screenHeight, 0.0f));
			slideUp.setDuration(400);
			slideUp.start();
		}

		setCancelable(false);

		if ((getParentActivity() instanceof OnboardingActivity) && defaultPage == 0) {
			defaultPage = ((OnboardingActivity) getParentActivity()).parentAccountDefaultPage;
		}

		setCurrentViewPagerItem(defaultPage);
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();
		LocalBroadcastManager.getInstance(getSafeContext()).unregisterReceiver(receiver);
		intentFilter = null;
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_create_parent_account;
	}


	private void onClickedSystemBackButton() {
		if (viewPager.getCurrentItem() > 0) {
			setCurrentViewPagerItem(viewPager.getCurrentItem() - 1);
		} else {
			CreateParentAccountDialogFragment.this.dismissDialogFragmentAnimated();
		}
	}


	private void onUsernamePasswordNext() {
		// Username and password are created
		CreateParentAccountUsernamePasswordFragment nameFragment = (CreateParentAccountUsernamePasswordFragment)createParentAccountPagerAdapter.getItem(PARENT_ACCOUNT_PAGE_USERNAME_PWD);
		CreateParentAccountEmailGenderBirthFragment emailFragment = (CreateParentAccountEmailGenderBirthFragment)createParentAccountPagerAdapter.getItem(PARENT_ACCOUNT_PAGE_EMAIL_GENDER_BIRTH);

		if (facebookField == null) {
			username = nameFragment.editUsername.getText().toString();
			password = nameFragment.editPassword.getText().toString();

			setCurrentViewPagerItem(PARENT_ACCOUNT_PAGE_EMAIL_GENDER_BIRTH);
		} else {
			facebookField.handle = nameFragment.editUsername.getText().toString();
			if (facebookField.dob == null || facebookField.dob.trim().length() == 0) {
				emailFragment.setData(facebookField.facebook.email, facebookField.gender);
				setCurrentViewPagerItem(PARENT_ACCOUNT_PAGE_EMAIL_GENDER_BIRTH);
			} else {
				showProgressDialog();
				KPHUserService.sharedInstance().signupFBAccount(facebookField, signupFB_actionListener);
			}
		}
	}


	private void onEmailGenderBirthNext() {
		KPHUserData currentUserData = KPHUserService.sharedInstance().getUserData();

		// Email, gender, date of birth are created
		CreateParentAccountUsernamePasswordFragment nameFragment = (CreateParentAccountUsernamePasswordFragment)createParentAccountPagerAdapter.getItem(PARENT_ACCOUNT_PAGE_USERNAME_PWD);
		CreateParentAccountEmailGenderBirthFragment emailFragment = (CreateParentAccountEmailGenderBirthFragment)createParentAccountPagerAdapter.getItem(PARENT_ACCOUNT_PAGE_EMAIL_GENDER_BIRTH);

		birthYear = emailFragment.editBirthday.getYear();
		birthMonth = emailFragment.editBirthday.getMonth();
		birthDay = emailFragment.editBirthday.getDayOfMonth();

		if (facebookField != null) {
			facebookField.dob = String.format("%04d-%02d-%02d", birthYear, birthMonth, birthDay);

			showProgressDialog();
			KPHUserService.sharedInstance().signupFBAccount(facebookField, signupFB_actionListener);
		} else {
			username = nameFragment.editUsername.getText().toString();
			password = nameFragment.editPassword.getText().toString();
			email = emailFragment.editEmail.getText().toString();
			gender = emailFragment.sGender;

			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, birthYear);
			calendar.set(Calendar.MONTH, birthMonth);
			calendar.set(Calendar.DAY_OF_MONTH, birthDay);

			showProgressDialog();
			if (currentUserData == null) {
				// Newly signup
				KPHUserService.sharedInstance().signup(
						username,
						email,
						password,
						"",
						gender,
						calendar.getTime(),
						"",
						signup_actionListener
				);
			} else {
				KPHUserService.sharedInstance().updateUserData(
						currentUserData.getId(),
						username,
						email,
						"",
						gender,
						calendar.getTime(),
						"",
						update_actionListener
				);
			}
		}
	}


	private void onAgeRestrictionErrorYesClicked() {
		setCurrentViewPagerItem(PARENT_ACCOUNT_PAGE_ACCOUNT_TYPE);
	}

	private void onAgeRestrictionErrorNoClicked() {
		dismissDialogFragmentAnimated();
	}

	public void dismissDialogFragmentAnimated() {
		Dialog dialog = getDialog();
		if (dialog != null && dialog.getWindow() != null) {
			ObjectAnimator slideDown = ObjectAnimator.ofPropertyValuesHolder(
					dialog.getWindow().getDecorView(),
					PropertyValuesHolder.ofFloat("translationY", 0.0f, dialogHeight));
			slideDown.setDuration(200);
			slideDown.addListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {}
				@Override
				public void onAnimationEnd(Animator animation) {
					dismissAllowingStateLoss();
				}
				@Override
				public void onAnimationCancel(Animator animation) {
					dismissAllowingStateLoss();
				}
				@Override
				public void onAnimationRepeat(Animator animation) {}
			});
			slideDown.start();
		}
	}

	private void onParentAccountSetupMethodEmailClicked() {
		facebookField = null;
		setCurrentViewPagerItem(PARENT_ACCOUNT_PAGE_USERNAME_PWD);
	}

	private void onParentAccountSetupMethodFacebookClicked() {
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

				facebookField = new KPHFacebook();
				facebookField.facebook.accessToken = accessToken;
				facebookField.facebook.email = email;
				facebookField.facebook.fbId = fbId;
				facebookField.handle = username;
				facebookField.gender = gender;
				facebookField.dob = birthday;

				CreateParentAccountUsernamePasswordFragment fragment = (CreateParentAccountUsernamePasswordFragment)createParentAccountPagerAdapter.getItem(PARENT_ACCOUNT_PAGE_USERNAME_PWD);
				fragment.setData(true);
				setCurrentViewPagerItem(PARENT_ACCOUNT_PAGE_USERNAME_PWD);
			}

			@Override
			public void onFailure(int errorCode, String errorMessage) {
				showErrorDialog(errorMessage);
			}

			@Override
			public void onCancelled() {
				showErrorDialog(getString(R.string.cancelled));
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (facebookService != null) {
			facebookService.onActivityResult(requestCode, resultCode, data);
		}
	}



	public void initKeyListener() {
		if (onKeyListener != null)
			return;

		onKeyListener = new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
					onClickedSystemBackButton();

				return false;
			}
		};
	}


	/***********************************************************************************************
	 * Action listeners
	 */
	private RestCallback<KPHUserData> signupFB_actionListener = new RestCallback<KPHUserData>() {
		@Override
		public void success(final KPHUserData kphUserData, Response response) {
			dismissProgressDialog();

			if (response.getStatus() == HttpStatus.SC_OK) {
				showBrandedDialog(getString(R.string.already_registered_fb_user), getString(R.string.ok), null, new KPHBrandedDialog.KPHBrandedDialogCallback() {
					@Override
					public void onDefaultButtonClicked() {
						signupSuccess(kphUserData);
					}
					@Override
					public void onOtherButtonClicked() {}
				});
			} else {
				signupSuccess(kphUserData);
			}
		}

		public void failure(RestError restError) {
			showErrorDialog(ErrorMessageDict.serverResponse2UserFriendlyMessage(KPHUtils.sharedInstance().getNonNullMessage(restError)));
		}
	};

	private onActionListener signup_actionListener = new onActionListener() {
		@Override
		public void completed(Object object) {
			dismissProgressDialog();
			signupSuccess((KPHUserData)object);
		}

		@Override
		public void failed(int code, String message) {
			showErrorDialog(ErrorMessageDict.serverResponse2UserFriendlyMessage(message));
		}
	};

	private void signupSuccess(KPHUserData parentData) {
		if (parentData != null) {
			KPHUserService.sharedInstance().saveUserData(parentData);
			KPHUserService.sharedInstance().saveLoginData(username, password);

			setCurrentViewPagerItem(PARENT_ACCOUNT_PAGE_FINISH);

			RestService.setUserToken(parentData.getAccessToken());

			if (facebookField != null) {
				KPHUserService.sharedInstance().saveFBData(facebookField.facebook.fbId, facebookField.facebook.email, facebookField.facebook.accessToken);
				password = "";
			}
		} else {
			showErrorDialog(getSafeContext().getString(R.string.default_error));
		}
	}


	private onActionListener update_actionListener = new onActionListener() {
		@Override
		public void completed(Object object) {
			if (object != null) {
				KPHUserData parentData = (KPHUserData) object;
				KPHUserService.sharedInstance().saveUserData(parentData);

				if (!password.equals(KPHUserService.sharedInstance().loadUserPassword())) {
					// Update parent password
					KPHUserService.sharedInstance().updateUserPassword(
							parentData.getId(),
							password,
							updatePassword_actionListener
					);
				} else {
					dismissProgressDialog();
					KPHUserService.sharedInstance().saveLoginData(username, password);
					setCurrentViewPagerItem(PARENT_ACCOUNT_PAGE_FINISH);
				}
			} else {
				showErrorDialog(getSafeContext().getString(R.string.default_error));
			}
		}

		@Override
		public void failed(int code, String message) {
			showErrorDialog(ErrorMessageDict.serverResponse2UserFriendlyMessage(message));
		}
	};


	private onActionListener updatePassword_actionListener = new onActionListener() {
		@Override
		public void completed(Object object) {
			dismissProgressDialog();
			KPHUserService.sharedInstance().saveLoginData(username, password);
			setCurrentViewPagerItem(PARENT_ACCOUNT_PAGE_FINISH);
		}

		@Override
		public void failed(int code, String message) {
			showErrorDialog(getSafeContext().getString(R.string.default_error));
		}
	};

	private void hideKeyboardInCurrentView() {
		KPHUtils.sharedInstance().hideKeyboardInView(viewPager);
	}

	@Override
	public void startAction() {
		// No action. do nothing
	}

	public void setData(String username) {
		this.childUserName = username;
	}

	private void setCurrentViewPagerItem(int index) {
		hideKeyboardInCurrentView();

		Fragment fragment = createParentAccountPagerAdapter.getItem(index);
		if (fragment != null && fragment instanceof CreateParentAccountSuperFragment) {
			((CreateParentAccountSuperFragment) fragment).setChildUsername(childUserName);
		}

		viewPager.setCurrentItem(index);
		if (getParentActivity() instanceof OnboardingActivity)
			((OnboardingActivity) getParentActivity()).parentAccountDefaultPage = viewPager.getCurrentItem();
	}

}
