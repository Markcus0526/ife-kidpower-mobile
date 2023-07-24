package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.OSDate;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.ErrorMessageDict;
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

import java.util.Calendar;
import java.util.Date;

import retrofit.client.Response;

/**
 * Created by Dayong on 1/16/2017.
 */
public class AccountCreatedActivity extends SuperActivity {
	private final String			TAG						= "AccountCreatedActivity";

	public static final String		EXTRA_USERNAME			= "UserName";
	public static final String		EXTRA_PASSWORD			= "UserPassword";
	public static final String		EXTRA_USERID			= "UserID";
	public static final String		EXTRA_AVATARID			= "AvatarID";
	public static final String		EXTRA_DEVICE_TYPE		= "DeviceType";
	public static final String		EXTRA_DEVICE_SELECTED	= "DeviceSelected";

	public static final String		EXTRA_BIRTHDAY_YEAR		= "year";
	public static final String		EXTRA_BIRTHDAY_MONTH	= "month";
	public static final String		EXTRA_BIRTHDAY_DAY		= "day";

	// Variables
	private boolean					deviceLinked			= false;
	private int						userId					= 0;
	private String					userName				= "";
	private String					password				= "";
	private String					avatarID				= "";
	private Date					birthday				= new Date();
	private int						deviceType				= KPHUserService.TRACKER_TYPE_NONE;

	private final int				RETRY_COUNT_LIMIT		= 5;
	private int						retryCount				= RETRY_COUNT_LIMIT;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_account_created);

		Bundle bundle = getIntent().getExtras();
		if (bundle == null)
			return;

		deviceLinked = bundle.getBoolean(EXTRA_DEVICE_SELECTED, false);

		userId = bundle.getInt(EXTRA_USERID, 0);
		userName = bundle.getString(EXTRA_USERNAME, "");
		password = bundle.getString(EXTRA_PASSWORD, "");
		avatarID = bundle.getString(EXTRA_AVATARID, "");
		deviceType = bundle.getInt(EXTRA_DEVICE_TYPE, KPHUserService.TRACKER_TYPE_NONE);

		int year = bundle.getInt(EXTRA_BIRTHDAY_YEAR);
		int month = bundle.getInt(EXTRA_BIRTHDAY_MONTH);
		int day = bundle.getInt(EXTRA_BIRTHDAY_DAY);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		birthday = cal.getTime();

		if (!TextUtils.isEmpty(userName)) {
			KPHTextView username = (KPHTextView) findViewById(R.id.txtUsername);
			username.setText(userName);
		}



		KPHButton btnStart = (KPHButton) findViewById(R.id.btnGotoMission);

		ImageView ivAvatar = (ImageView) findViewById(R.id.ivAvatar);
		ImageView ivBand = (ImageView)findViewById(R.id.ivBand);
		ImageView ivGoogleFit = (ImageView)findViewById(R.id.google_fit);
		if (deviceType == KPHUserService.TRACKER_TYPE_KIDPOWERBAND) {
			ivGoogleFit.setVisibility(View.GONE);
			if (deviceLinked) {
				ivBand.setVisibility(View.VISIBLE);

				KPHTextView txtHeader = (KPHTextView) findViewById(R.id.txtHeader);
				txtHeader.setText(R.string.you_are_all_set);

				KPHTextView txtDescription = (KPHTextView) findViewById(R.id.txtDescription);
				txtDescription.setText(this.getString(R.string.go_get_them_now_to_start_mission, userName));

				btnStart.setText(R.string.start_mission);
				btnStart.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onGotoMissionClicked(true);
					}
				});
			} else {
				ivBand.setVisibility(View.GONE);

				KPHTextView txtHeader = (KPHTextView) findViewById(R.id.txtHeader);
				txtHeader.setText(R.string.account_created);

				KPHTextView txtDescription = (KPHTextView) findViewById(R.id.txtDescription);
				txtDescription.setText(this.getString(R.string.not_counting_steps_yet, userName));

				btnStart.setText(R.string.goto_passport);
				btnStart.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onGotoPassportClicked();
					}
				});

				ivAvatar.setPadding(0, 0, 0, 0);
			}
		} else if (deviceType == KPHUserService.TRACKER_TYPE_GOOGLEFIT) {
			ivBand.setVisibility(View.GONE);
			ivGoogleFit.setVisibility(View.VISIBLE);

			KPHTextView txtDescription = (KPHTextView) findViewById(R.id.txtDescription);
			txtDescription.setText(String.format(getString(R.string.if_child_go_get_them), userName));

			if (deviceLinked) {
				btnStart.setText(R.string.start_mission);
				btnStart.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onGotoMissionClicked(true);
					}
				});
			} else {
				btnStart.setText(R.string.goto_passport);
				btnStart.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onGotoPassportClicked();
					}
				});
			}

			ivAvatar.setPadding(0, 0, 0, 0);
		} else {
			// Not selected device yet.
			ivBand.setVisibility(View.GONE);
			ivGoogleFit.setVisibility(View.GONE);

			KPHTextView txtDescription = (KPHTextView) findViewById(R.id.txtDescription);
			txtDescription.setText(String.format(getString(R.string.not_counting_steps_yet), userName));

			btnStart.setText(R.string.goto_passport);
			btnStart.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onGotoPassportClicked();
				}
			});

			ivAvatar.setPadding(0, 0, 0, 0);
		}

		Drawable drawable = KPHUserService.sharedInstance().getAvatarDrawable(avatarID);
		if (drawable != null) {
			ivAvatar.setImageDrawable(drawable);
		} else {
			ivAvatar.setImageDrawable(UIManager.sharedInstance().getImageDrawable(R.drawable.avatar_placeholder));
		}

		final KPHTextView txtLinkAnotherBand = (KPHTextView) findViewById(R.id.txtLinkAnotherBand);

		if (isShowLink())
			txtLinkAnotherBand.setVisibility(View.GONE);

		ClickableSpan linkAnotherBand = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onLinkAnotherBandClicked();
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setColor(txtLinkAnotherBand.getCurrentTextColor());
			}
		};

		String content;
		String strUnderline;

		if (KPHUserService.sharedInstance().loadCurrentTrackerType() == KPHUserService.TRACKER_TYPE_KIDPOWERBAND) {
			content = getString(R.string.or_link_another_band);
			strUnderline = getString(R.string.link_another_band);
		} else {
			content = getString(R.string.or_setup_someone_else);
			strUnderline = getString(R.string.setup_someone_else);
		}

		SpannableString sContent = new SpannableString(content);
		// Learn More Underline
		sContent.setSpan(linkAnotherBand, content.indexOf(strUnderline),
				content.indexOf(strUnderline) + strUnderline.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		txtLinkAnotherBand.setText(sContent);
		txtLinkAnotherBand.setMovementMethod(LinkMovementMethod.getInstance());
		txtLinkAnotherBand.setHighlightColor(Color.TRANSPARENT);
	}

	private boolean isShowLink() {
		KPHUserData currentUser = KPHUserService.sharedInstance().getUserData();
		if (currentUser == null) {
			// Unreachable. If reached, that's logical error
			Logger.log("New onboarding", "showLink_Logical_Error");

			if (birthday == null) {
				return false;
			}

			OSDate osDate = new OSDate(birthday);
			return osDate.getYearsPassed() < KPHUserService.USER_MINIMUM_AGE;
		} else {
			OSDate osDate = new OSDate(currentUser.getBirthday());
			return osDate.getYearsPassed() < KPHUserService.USER_MINIMUM_AGE;
		}
	}


	private void onGotoMissionClicked(boolean autoStart) {
		loginUser(autoStart);
	}


	private void onGotoPassportClicked() {
		Logger.log(TAG, "onGotoPassportClicked");
		loginUser(false);
	}


	private void loginUser(final boolean isAutoStart) {
		showProgressDialog();
		Logger.log(TAG, "loginUser" + isAutoStart);
		if (password == null || password.length() == 0) {
			// Facebook login
			final KPHFacebook facebook = new KPHFacebook();
			facebook.facebook.accessToken = KPHUserService.sharedInstance().loadAccessToken();
			facebook.facebook.email = KPHUserService.sharedInstance().loadEmail();
			facebook.facebook.fbId = KPHUserService.sharedInstance().loadFBId();

			showProgressDialog();
			KPHUserService.sharedInstance().signinFBAccount(facebook, new onActionListener() {
				@Override
				public void completed(Object object) {
					dismissProgressDialog();
					FBLoginSuccess((KPHUserData)object, facebook, isAutoStart);
				}

				@Override
				public void failed(int code, String message) {
					showErrorDialog(message);
				}
			});
		} else {
			KPHUserService.sharedInstance().signin(userName, password, new RestCallback<KPHUserData>() {
				@Override
				public void failure(RestError restError) {
					showErrorDialog(KPHUtils.sharedInstance().getNonNullMessage(restError));
				}

				@Override
				public void success(KPHUserData kphUserData, Response response) {
					dismissProgressDialog();

					RestService.setUserToken(kphUserData.getAccessToken());

					KPHUserService.sharedInstance().shouldReloadUserData();
					KPHUserService.sharedInstance().setChildRestrictedFlag(false);
					KPHUserService.sharedInstance().saveSignInFlag();
					KPHUserService.sharedInstance().saveLoginData(userName, password);
					KPHUserService.sharedInstance().saveUserData(kphUserData);

					gotoMainActivityWithAutoStartMissionFlag(isAutoStart);
				}
			});
		}
	}


	private void FBLoginSuccess(KPHUserData userData, KPHFacebook facebookField, boolean isAutoStart) {
		RestService.setUserToken(userData.getAccessToken());

		KPHUserService.sharedInstance().shouldReloadUserData();
		KPHUserService.sharedInstance().setChildRestrictedFlag(false);
		KPHUserService.sharedInstance().saveSignInFlag();
		KPHUserService.sharedInstance().saveLoginData(userData.getHandle(), "");
		KPHUserService.sharedInstance().saveUserData(userData);

		if (facebookField != null) {
			KPHUserService.sharedInstance().saveFBData(facebookField.facebook.fbId, facebookField.facebook.email, facebookField.facebook.accessToken);
		}

		gotoMainActivityWithAutoStartMissionFlag(isAutoStart);
	}

	private void gotoMainActivityWithAutoStartMissionFlag(boolean isAutoStart) {
		if (MainActivity.mainActivityInstance != null) {
			// Broadcast to reset MainActivity if already exists
			Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_RESET_MAINACTIVITY);
			intent.putExtra(OnboardingActivity.EXTRA_SHOW_AUTOSTART_MISSION, isAutoStart);
			LocalBroadcastManager.getInstance(AccountCreatedActivity.this).sendBroadcast(intent);

			popOverCurActivityAnimated();
		} else {
			Bundle extra = new Bundle();
			extra.putBoolean(OnboardingActivity.EXTRA_SHOW_AUTOSTART_MISSION, isAutoStart);

			pushNewActivityAnimated(
					MainActivity.class,
					AnimConst.ANIMDIR_NONE,
					Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION,
					extra,
					0
			);
			overridePendingTransition(0, 0);
		}

	}


	private void onLinkAnotherBandClicked() {
		// Should show "Me" button once parent account is saved and device is not linked(not 'mDeviceLinked' variable)
		KPHUserData currentUser = KPHUserService.sharedInstance().getUserData();

		// Check if tracker is attached to parent
		boolean hasTracker = false;
		if (currentUser.getId() == userId) {
			// User is registered normally
			showAnotherBandFragment(false);
			return;
		} else {
			// Created user is the child account of the "currentUser"
			// Check if currentUser has tracker
			showProgressDialog();
			checkTrackerForUser(currentUser.getId());
		}
	}


	private void checkTrackerForUser(final int userId) {
		retryCount = RETRY_COUNT_LIMIT;
		KPHUserService.sharedInstance().getCurrentTrackerByUserId(userId, new onActionListener() {
			@Override
			public void completed(Object object) {
				dismissProgressDialog();

				if (object == null) {
					// Current user does not have tracker. Should show "Me" button
					showAnotherBandFragment(true);
				} else {
					// Current user has tracker. Do not need to show "Me" button
					showAnotherBandFragment(false);
				}
			}

			@Override
			public void failed(int code, String message) {
				if (retryCount < 0) {
					Logger.log(TAG, "getCurrentTracker failed : Finish");
					showErrorDialog(ErrorMessageDict.serverResponse2UserFriendlyMessage(message));
				} else {
					retryCount--;
					checkTrackerForUser(userId);
				}
			}
		});
	}


	private void showAnotherBandFragment(boolean showMeButton) {
		AnotherBandFragment autoStartFragment = new AnotherBandFragment();
		autoStartFragment.isShowMeButton = showMeButton;
		showDialogFragment(autoStartFragment);
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN &&
				keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
}
