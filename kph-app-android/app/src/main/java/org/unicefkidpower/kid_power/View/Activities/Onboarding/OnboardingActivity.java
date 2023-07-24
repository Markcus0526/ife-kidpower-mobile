package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.view.inputmethod.InputMethodManager;

import org.unicefkidpower.kid_power.Misc.Config;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.OSDate;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;

import java.util.Calendar;
import java.util.Date;

import static java.lang.Thread.sleep;


/**
 * Created by Ruifeng Shi on 8/27/2015.
 */
public class OnboardingActivity extends SuperActivity {
	public static final String		TAG									= "OnboardingActivity";

	public static final String		EXTRA_FROM_ACTIVITY					= "from_activity";

	public static final String		EXTRA_SHOW_AUTOSTART_MISSION		= "show_autostart_mission";
	public static final String		EXTRA_COMPLETING_PARENT_PROFILE		= "is_completing_parent_profile";
	public static final String		EXTRA_SHOW_BACK_BUTTON				= "show_back_button";


	/**
	 * Variable to specify which activity has shown this activity
	 */
	public static final int			FROM_UNSPECIFIED_ACTIVITY			= 0;
	public static final int			FROM_WELCOME_ACTIVITY				= 1;
	public static final int			FROM_CHILD_RESTRICTION_ACTIVITY		= 2;

	private int						fromActivity = FROM_UNSPECIFIED_ACTIVITY;
	// End of 'Variable to specify which activity has shown this activity'

	/**
	 * Variable to specify if the first fragment has back button or not
	 */
	private boolean					showBackButton = true;

	/**
	 * Variable to specify current flow type
	 */
	public boolean					isCompletingParentProfile	= false;
	// End of 'Variable to specify current flow type'


	/**
	 * Variable to specify if this activity should show the parent account creation dialog
	 */
	public boolean					isShownParentAccountCreationDialog = false;


	/**
	 * Variable to specify which page to show on parent account creation dialog
	 */
	public int						parentAccountDefaultPage = 0;


	/**
	 * Fragment variables
	 */
	public OnboardingUserNameFragment			fragUserName				= null;
	public SetAvatarFragment					fragSetAvatar				= null;
	public OnboardingAccountTypeFragment		fragmentAccountType			= null;
	public OnboardingChooseBirthdayFragment		fragChooseBirthday			= null;
	public OnboardingEmailPasswordFragment		fragEmailPwd				= null;
	public SelectDeviceFragment					fragSelectDevice			= null;
	// End of 'Fragment variables'


	/**
	 * Account Information
	 */
	public int				newUserIdentifier		= 0;
	public String			newUsername				= "";
	public String			newEmail				= "";
	public String			newPassword				= "";
	public String			newAvatarId				= "";
	public Date				newBirthday				= null;
	public String			newGender				= KPHConstants.GENDER_SKIP;
	// End of 'Account Information'


	private IntentFilter	intentFilter			= null;
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			switch (action) {
				case KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_CREATED_USER_NAME:
					fragSetAvatar = new SetAvatarFragment();
					fragSetAvatar.setData(newUsername, newAvatarId);
					showNewFragment(fragSetAvatar);
					break;

				case KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_SET_ICON_CONFIRMED:
					fragChooseBirthday = new OnboardingChooseBirthdayFragment();
					fragChooseBirthday.setUsername(newUsername);
					fragChooseBirthday.setAvatarId(newAvatarId);
					fragChooseBirthday.setBirthday(newBirthday);
					if (isCompletingParentProfile)
						fragChooseBirthday.setIsCreatingFamilyAccount(false);
					else
						fragChooseBirthday.setIsCreatingFamilyAccount(KPHUserService.sharedInstance().getUserData() != null);
					showNewFragment(fragChooseBirthday);
					break;

				case KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_CHOOSE_BIRTHDAY_CONFIRMED:
					// If the user is child, then restrict the app usage
					if (newBirthday == null) {
						Logger.error(TAG, "Birthday is null");
					}

					OSDate birthday = new OSDate(newBirthday);
					if (birthday.getYearsPassed() < KPHUserService.USER_MINIMUM_AGE &&				// User is under min age
							KPHUserService.sharedInstance().getUserData() == null)					// There is no parent data
					{
						// Unreachable now. 2017.02.01
						KPHUserService.sharedInstance().setChildRestrictedFlag(true);
						pushNewActivityAnimated(WelcomeActivity.class, SuperActivity.AnimConst.ANIMDIR_FROM_LEFT);
						popOverCurActivityAnimated();
						return;
					} else {
						if (birthday.getYearsPassed() < KPHUserService.USER_MINIMUM_AGE) {
							LocalBroadcastManager.getInstance(OnboardingActivity.this).sendBroadcast(
									new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_SETUP_EMAIL_SELECTED)
							);
						} else {
							if (Config.USE_FACEBOOK_FEATURE) {
								fragmentAccountType = new OnboardingAccountTypeFragment();
								fragmentAccountType.setData(newUsername, newAvatarId, new OSDate(newBirthday).getDateOfBirth());
								showNewFragment(fragmentAccountType);
							} else {
								LocalBroadcastManager.getInstance(OnboardingActivity.this).sendBroadcast(
										new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_SETUP_EMAIL_SELECTED)
								);
							}
						}
					}
					break;

				case KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_SETUP_EMAIL_SELECTED:
					fragEmailPwd = new OnboardingEmailPasswordFragment();

					fragEmailPwd.sUsername = newUsername;
					fragEmailPwd.sAvatarId = newAvatarId;
					fragEmailPwd.sEmail = newEmail;
					fragEmailPwd.sPassword = newPassword;
					fragEmailPwd.sGender = newGender;
					fragEmailPwd.aBirthday = newBirthday;
					if (isCompletingParentProfile)
						fragEmailPwd.isCreatingFamilyAccount = false;
					else
						fragEmailPwd.isCreatingFamilyAccount = KPHUserService.sharedInstance().getUserData() != null;

					showNewFragment(fragEmailPwd);
					break;

				case KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_EMAIL_PASSWORD_SELECTED:
					// Show Select Device Fragment
					fragSelectDevice = new SelectDeviceFragment();
					fragSelectDevice.showTabBar(false);
					fragSelectDevice.setApplyToChilds(true);
					fragSelectDevice.setData(newUserIdentifier, newUsername, newAvatarId);
					showNewFragment(fragSelectDevice);
					break;

				case KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_TRACKER_SELECTED: {
					boolean connectedDevice = intent.getBooleanExtra(KPHConstants.PROFILE_DEVICE_SELECTED, false);
					int deviceType = KPHUserService.TRACKER_TYPE_NONE;

					dismissProgressDialog();

					if (connectedDevice) {
						deviceType = intent.getIntExtra(KPHConstants.PROFILE_DEVICE_TYPE, KPHUserService.TRACKER_TYPE_NONE);
					}

					Bundle bundle = new Bundle();
					bundle.putString(AccountCreatedActivity.EXTRA_USERNAME, newUsername);
					bundle.putString(AccountCreatedActivity.EXTRA_PASSWORD, newPassword);
					bundle.putInt(AccountCreatedActivity.EXTRA_USERID, newUserIdentifier);
					bundle.putString(AccountCreatedActivity.EXTRA_AVATARID, newAvatarId);

					Calendar cal = Calendar.getInstance();
					cal.setTime(newBirthday);

					bundle.putInt(AccountCreatedActivity.EXTRA_BIRTHDAY_YEAR, cal.get(Calendar.YEAR));
					bundle.putInt(AccountCreatedActivity.EXTRA_BIRTHDAY_MONTH, cal.get(Calendar.MONTH));
					bundle.putInt(AccountCreatedActivity.EXTRA_BIRTHDAY_DAY, cal.get(Calendar.DAY_OF_MONTH));

					bundle.putInt(AccountCreatedActivity.EXTRA_DEVICE_TYPE, deviceType);
					bundle.putBoolean(AccountCreatedActivity.EXTRA_DEVICE_SELECTED, connectedDevice);

					pushNewActivityAnimated(AccountCreatedActivity.class, bundle);
					popOverCurActivityAnimated();
					break;
				}

				case KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_PROFILE_INFO_GO_BACK:
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (intentFilter == null) {
			intentFilter = new IntentFilter();
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_CREATED_USER_NAME);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_CHOOSE_BIRTHDAY_CONFIRMED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_SET_ICON_CONFIRMED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_EMAIL_PASSWORD_SELECTED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_PROFILE_INFO_GO_BACK);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_SETUP_EMAIL_SELECTED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_SETUP_FACEBOOK_SELECTED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_TRACKER_SELECTED);

			LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
		}

		setContentView(R.layout.activity_onboarding);

		// Retrieving intent data
		if (getIntent().getExtras() != null) {
			isCompletingParentProfile = getIntent().getExtras().getBoolean(EXTRA_COMPLETING_PARENT_PROFILE, false);
			fromActivity = getIntent().getExtras().getInt(EXTRA_FROM_ACTIVITY, FROM_UNSPECIFIED_ACTIVITY);
			showBackButton = getIntent().getExtras().getBoolean(EXTRA_SHOW_BACK_BUTTON, true);
		}
		// End of 'Retrieving intent data'


		if (isCompletingParentProfile) {
			KPHUserData parentData = KPHUserService.sharedInstance().getUserData();

			newUserIdentifier = parentData.getId();
			newUsername = parentData.getHandle();
			newEmail = parentData.getEmail();
			newPassword = KPHUserService.sharedInstance().loadUserPassword();
			newAvatarId = parentData.getAvatarId();
			newBirthday = parentData.getBirthday();
			newGender = parentData.getGender();
		}

		fragUserName = new OnboardingUserNameFragment();
		fragUserName.setData(newUsername);
		showNewFragment(fragUserName);

		if (isCompletingParentProfile) {
			// completing the parent profile now. directly go to avatar selecting page.
			if (newAvatarId == null || newAvatarId.length() == 0) {
				// There is no avatar yet. Show select avatar page.
				Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_CREATED_USER_NAME);
				LocalBroadcastManager.getInstance(OnboardingActivity.this).sendBroadcast(intent);
			} else {
				// There is no tracker yet. Show select tracker page.
				Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_EMAIL_PASSWORD_SELECTED);
				LocalBroadcastManager.getInstance(OnboardingActivity.this).sendBroadcast(intent);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public int getContainerViewId() {
		return R.id.layout_content;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		intentFilter = null;
	}

	@Override
	public void onClickedBackSystemButton() {
		if (getTopFragment() != null && getSupportFragmentManager().getBackStackEntryCount() > 1) {
			super.onClickedBackSystemButton();
		} else if (showBackButton) {
			if (isTaskRoot()) {
				KPHUserService.sharedInstance().clearUserData();
				KPHUserService.sharedInstance().clearLoginData();
				KPHUserService.sharedInstance().clearCatchTrackerDialogDate();
			}

			if (fromActivity == FROM_WELCOME_ACTIVITY) {
				pushNewActivityAnimated(WelcomeActivity.class, SuperActivity.AnimConst.ANIMDIR_FROM_LEFT);
			} else if (fromActivity == FROM_CHILD_RESTRICTION_ACTIVITY) {
				pushNewActivityAnimated(ChildRestrictionActivity.class, SuperActivity.AnimConst.ANIMDIR_FROM_LEFT);
			}

			popOverCurActivityAnimated();
		}
	}

	// hide keyboard
	public void hideSoftKeyboard() {
		InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
		if (this.getCurrentFocus() != null && this.getCurrentFocus().getWindowToken() != null)
			inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
	}

	public boolean getShowBackButtonFlag() {
		return showBackButton;
	}

	public void setShowBackButtonFlag(boolean backButtonFlag) {
		showBackButton = backButtonFlag;
		refreshBackButton();
	}

	private void refreshBackButton() {
		if (fragUserName != null)
			fragUserName.refreshBackButton();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case KPHConstants.PERMISSION_REQUEST_LOCATION: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					try {
						sleep(500);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}

					Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_LOCATION_PERMISSION_SUCCESS);
					LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
				} else {
					// didn't grant location permission
					Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_LOCATION_PERMISSION_FAILED);
					LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
				}
				break;
			}
		}
	}
}
