package org.unicefkidpower.kid_power.View.Activities.Main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import org.unicefkidpower.kid_power.BuildConfig;
import org.unicefkidpower.kid_power.Misc.BillingUtil.IabHelper;
import org.unicefkidpower.kid_power.Misc.BillingUtil.IabResult;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.KPHNotificationUtil;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.GoogleFit.GoogleFitService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHAnalyticsService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHMissionService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.Model.Structure.KPHDeviceInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserTravelLogResponse;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.Friends.FriendsMainFragment;
import org.unicefkidpower.kid_power.View.Activities.Main.More.MoreMainFragment;
import org.unicefkidpower.kid_power.View.Activities.Main.Passport.PassportMainFragment;
import org.unicefkidpower.kid_power.View.Activities.Main.TravelLog.TravelLogMainFragment;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.CatchTrackerFragment;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.EditProfileFragment;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.GoogleFitWrapperActivity;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.OnboardingActivity;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.OwnDeviceTrackerFragment;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.RegisterBandFragment;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.SplashActivity;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTabItem;
import org.unicefkidpower.kid_power.View.CustomControls.KPHVideoLayout;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;
import org.unicefkidpower.kid_power.View.Super.SuperTabActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.client.Response;

import static java.lang.Thread.sleep;

/**
 * Created by Dayong Li on 11/11/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class MainActivity extends SuperTabActivity {
	public		static	final String			TAG = "MainActivity";

	// Constants
	public		static	final int				REQCODE_SELECT_GOOGLEFIT_DIRECTLY	= 11;
	public		static	final int				REQCODE_SYNC_GOOGLEFIT				= 12;

	public		static	final int				INDEX_FRAGMENT_PASSPORT				= 0;
	public		static	final int				INDEX_FRAGMENT_TRAVEL_LOG			= 1;
	public		static	final int				INDEX_FRAGMENT_FRIENDS				= 2;
	public		static	final int				INDEX_FRAGMENT_MORE					= 3;
	public		static	final String			NOTIFICATION_STRING					= "notification_string";
	// End of "Constants"


	public		static KPHVideoLayout			currentPlayingVideoLayout			= null;

	protected long								heartBeatTimeStamp					= 0;


	// Tab Items
	private		KPHTabItem						passport_TabButton					= null;
	private		KPHTabItem						travelLog_TabButton					= null;
	private		KPHTabItem						friends_TabButton					= null;
	private		KPHTabItem						more_TabButton						= null;
	// End of "Tab Items"

	// Layouts
	private RelativeLayout						layoutSuccessNotification			= null;

	// Auto start mission flag
	private boolean								isAutoStartMission					= false;

	// Fragments
	public PassportMainFragment					passportMainFragment				= null;
	public TravelLogMainFragment				travelLogMainFragment				= null;
	public FriendsMainFragment					friendsMainFragment					= null;
	public MoreMainFragment						moreMainFragment					= null;
	// End of Fragments

	private IabHelper							iabHelper							= null;
	private IntentFilter						intentFilter						= null;

	private final int							RETRY_COUNT_LIMIT					= 3;
	private int									retryCount							= RETRY_COUNT_LIMIT;


	/***********************************************************************************************
	 * Count time to show catch tracker dialog
	 */
	private final int							CATCH_TRACKER_TIMER_INTERVAL		= (int)(1.5 * 1000);
	public CatchTrackerFragment					catchTrackerFragment				= null;
	private TimerTask							catchTrackerTimerTask				= null;
	private Timer								catchTrackerTimer					= null;
	/**
	 * End of 'Count time to show catch tracker dialog'
	 **********************************************************************************************/

	public static MainActivity					mainActivityInstance				= null;


	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			switch (action) {
				case KPHBroadcastSignals.BROADCAST_SIGNAL_RESET_MAINACTIVITY: {
					boolean isAutoStart = intent.getBooleanExtra(OnboardingActivity.EXTRA_SHOW_AUTOSTART_MISSION, false);
					resetActivity(isAutoStart);
					break;
				}
				case KPHBroadcastSignals.BROADCAST_SIGNAL_USER_NOTIFICATION: {
					String notifStr = intent.getStringExtra(NOTIFICATION_STRING);
					KPHNotificationUtil.sharedInstance().showSuccessNotification(MainActivity.this, notifStr);
					break;
				}
				case KPHBroadcastSignals.BROADCAST_SIGNAL_TAB_ITEM_BADGE_UPDATED: {
					updateTabItemBadges();
					break;
				}
				case KPHBroadcastSignals.BROADCAST_SIGNAL_CATCH_GOOGLE_FIT: {
					gotoCatchGoogleFit();
					break;
				}
				case KPHBroadcastSignals.BROADCAST_SIGNAL_CATCH_KID_POWER_BAND: {
					gotoCatchKidPowerBand();
					break;
				}
				case KPHBroadcastSignals.BROADCAST_SIGNAL_GOTO_ACTIVITY_TRACKER: {
					gotoActivityTrackerFragment();
					break;
				}
				case KPHBroadcastSignals.BROADCAST_SIGNAL_GOTO_GET_IN_TOUCH: {
					gotoGetInTouchFragment();
					break;
				}
			}
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mainActivityInstance = this;

		KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_AGE_VERIFY_COMPLETE);
		reportDeviceInformation();

		if (getIntent().getExtras() != null) {
			currentTabIndex = getIntent().getExtras().getInt(EXTRA_INITIAL_FRAGMENT_INDEX, INDEX_FRAGMENT_PASSPORT);
			isAutoStartMission = getIntent().getExtras().getBoolean(OnboardingActivity.EXTRA_SHOW_AUTOSTART_MISSION);
			clearExtras();
		}

		if (intentFilter == null) {
			intentFilter = new IntentFilter();
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_RESET_MAINACTIVITY);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_USER_NOTIFICATION);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_TAB_ITEM_BADGE_UPDATED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_CATCH_GOOGLE_FIT);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_CATCH_KID_POWER_BAND);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_GOTO_ACTIVITY_TRACKER);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_GOTO_GET_IN_TOUCH);

			LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
		}

		layoutSuccessNotification = (RelativeLayout) findViewById(R.id.layout_success_notification);
		layoutSuccessNotification.setVisibility(View.INVISIBLE);

		passport_TabButton = (KPHTabItem) findViewById(R.id.tabPassport);
		travelLog_TabButton = (KPHTabItem) findViewById(R.id.tabTravelLog);
		friends_TabButton = (KPHTabItem) findViewById(R.id.tabFollow);
		more_TabButton = (KPHTabItem) findViewById(R.id.tabMore);


		tabBar = findViewById(R.id.layout_tabbar);
		tabBarShadow = findViewById(R.id.tab_bar_shadow);

		passport_TabButton.setParentView(tabBar);
		travelLog_TabButton.setParentView(tabBar);
		friends_TabButton.setParentView(tabBar);
		more_TabButton.setParentView(tabBar);

		passport_TabButton.setOnTabSelectedListener(new KPHTabItem.OnTabSelectedListener() {
			@Override
			public void onTabSelected(View v) {
				onSelectedPassportTab();
			}
		});
		travelLog_TabButton.setOnTabSelectedListener(new KPHTabItem.OnTabSelectedListener() {
			@Override
			public void onTabSelected(View v) {
				onSelectedTravelLogTab();
			}
		});
		friends_TabButton.setOnTabSelectedListener(new KPHTabItem.OnTabSelectedListener() {
			@Override
			public void onTabSelected(View v) {
				onSelectedFriendsTab();
			}
		});
		more_TabButton.setOnTabSelectedListener(new KPHTabItem.OnTabSelectedListener() {
			@Override
			public void onTabSelected(View v) {
				onSelectedMoreTab();
			}
		});

		passportMainFragment = new PassportMainFragment();
		passportMainFragment.setData(isAutoStartMission);
		travelLogMainFragment = new TravelLogMainFragment();
		friendsMainFragment = new FriendsMainFragment();
		moreMainFragment = new MoreMainFragment();

		if (BuildConfig.DEBUG) {
			KPHUserService.sharedInstance().setEnabledPurchases(false);
		} else {
			String base64EncodePublicKey = KPHConstants.BASE64_PUBLIC_SUBKEY1
					+ KPHConstants.BASE64_PUBLIC_SUBKEY2
					+ KPHConstants.BASE64_PUBLIC_SUBKEY3
					+ KPHConstants.BASE64_PUBLIC_SUBKEY4
					+ KPHConstants.BASE64_PUBLIC_SUBKEY5
					+ KPHConstants.BASE64_PUBLIC_SUBKEY6;

			try {
				iabHelper = new IabHelper(this, base64EncodePublicKey);
				iabHelper.enableDebugLogging(true, "KiHome");
				iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
					@Override
					public void onIabSetupFinished(IabResult result) {
						if (!result.isSuccess()) {
							// Oh noes, there was a problem
							KPHUserService.sharedInstance().setEnabledPurchases(false);
						} else {
							// Hooray, IAB is fully set up!
							KPHUserService.sharedInstance().setEnabledPurchases(true);
						}
					}
				});
			} catch (Exception ex) {
				KPHUserService.sharedInstance().setEnabledPurchases(false);
			}
		}


		ArrayList<ArrayList<SuperFragment>> tabArraysList = new ArrayList<>();

		ArrayList<SuperFragment> passportFragments = new ArrayList<>();
		ArrayList<SuperFragment> travelLogFragments = new ArrayList<>();
		ArrayList<SuperFragment> friendsFragments = new ArrayList<>();
		ArrayList<SuperFragment> moreFragments = new ArrayList<>();

		passportFragments.add(passportMainFragment);
		travelLogFragments.add(travelLogMainFragment);
		friendsFragments.add(friendsMainFragment);
		moreFragments.add(moreMainFragment);

		tabArraysList.add(passportFragments);
		tabArraysList.add(travelLogFragments);
		tabArraysList.add(friendsFragments);
		tabArraysList.add(moreFragments);

		initializeTabContents(tabArraysList, currentTabIndex);
		tabChangedListener = new OnTabChangedListener() {
			@Override
			public void onTabChanged(int index) {
				highlightTab(currentTabIndex);
			}
		};
		highlightTab(currentTabIndex);

		PhoneStateListener phoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				if (state == TelephonyManager.CALL_STATE_RINGING) {
					Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_CALLSTATE_RINGING);
					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
				} else if (state == TelephonyManager.CALL_STATE_IDLE) {
					Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_CALLSTATE_IDLE);
					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
				}

				super.onCallStateChanged(state, incomingNumber);
			}
		};

		TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		if (mgr != null) {
			mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		}

		fetchTravelLogUnreadItemCount();
	}


	@Override
	protected void onResume() {
		super.onResume();

		catchTrackerTimerTask = new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (needToShowCatchTrackerDialog()) {
							showDialogFragment(catchTrackerFragment);
						}
					}
				});
			}
		};
		catchTrackerTimer = new Timer();
		catchTrackerTimer.schedule(catchTrackerTimerTask, CATCH_TRACKER_TIMER_INTERVAL, CATCH_TRACKER_TIMER_INTERVAL);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	@Override
	protected void onPause() {
		super.onPause();

		catchTrackerTimer.cancel();
		catchTrackerTimer = null;

		catchTrackerTimerTask.cancel();
		catchTrackerTimerTask = null;

		heartBeatTimeStamp = 0;
	}

	@Override
	protected void onDestroy() {
		mainActivityInstance = null;

		try {
			if (iabHelper != null)
				iabHelper.dispose();
		} catch (RuntimeException e) {
			e.printStackTrace();
		} finally {
			iabHelper = null;
		}

		if (receiver != null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
			intentFilter = null;
		}

		super.onDestroy();
	}


	private void reportDeviceInformation() {
		// removed the permission of phone device as the requirement by 08-06-2017
//		int permissionResult = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_STATE);
//		if (permissionResult == PackageManager.PERMISSION_DENIED) {
//			ActivityCompat.requestPermissions(MainActivity.this, new String[] {android.Manifest.permission.READ_PHONE_STATE}, KPHConstants.PERMISSION_REQUEST_PHONE_STATE);
//			return;
//		}

		// device, imei, iccid, imsi, udid, versionCode, userId, pushId
		KPHDeviceInformation deviceInformation = KPHDeviceInformation.getCurrentDeviceInformation(MainActivity.this);
		KPHDeviceInformation prevInformation = KPHUserService.sharedInstance().loadLatestDeviceInformation();

		if (deviceInformation.isSameInformation(prevInformation)) {
			Logger.log(TAG, "No change for device information. Not uploading.");
			return;
		}

		retryCount = RETRY_COUNT_LIMIT;
		uploadDeviceInformation(deviceInformation);
	}


	private void uploadDeviceInformation(final KPHDeviceInformation info) {
		KPHUserService.sharedInstance().createDeviceRecord(info, new onActionListener() {
			@Override
			public void completed(Object object) {
				KPHDeviceInformation newInformation = (KPHDeviceInformation)object;
				KPHUserService.sharedInstance().saveLatestDeviceInformation(newInformation);

				Logger.log(TAG, "DeviceInformation reported successfully. "
						+ newInformation.device
						+ "," + newInformation.imei
						+ "," + newInformation.iccid
						+ "," + newInformation.imsi
						+ "," + newInformation.udid
						+ "," + newInformation.versionCode
						+ "," + newInformation.userId
						+ "," + newInformation.pushId
				);
			}

			@Override
			public void failed(int code, String message) {
				if (retryCount < 0)
					return;

				retryCount--;
				uploadDeviceInformation(info);
			}
		});
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
			case KPHConstants.PERMISSION_REQUEST_STORAGE: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_STORAGE_PERMISSION_SUCCESS);
					LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
				} else {
					Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_STORAGE_PERMISSION_FAILED);
					LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
				}
				break;
			}
			case KPHConstants.PERMISSION_REQUEST_PHONE_STATE: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					reportDeviceInformation();
				} else {
					// Do nothing
					Logger.log(TAG, "ReadPhoneState permission denied.");
				}
				break;
			}
			// other 'case' lines to check for other
			// permissions this app might request
		}
	}


	private void highlightTab(int index) {
		passport_TabButton.setHighlighted(false);
		travelLog_TabButton.setHighlighted(false);
		friends_TabButton.setHighlighted(false);
		more_TabButton.setHighlighted(false);

		if (index == INDEX_FRAGMENT_PASSPORT)
			passport_TabButton.setHighlighted(true);
		else if (index == INDEX_FRAGMENT_TRAVEL_LOG)
			travelLog_TabButton.setHighlighted(true);
		else if (index == INDEX_FRAGMENT_FRIENDS)
			friends_TabButton.setHighlighted(true);
		else if (index == INDEX_FRAGMENT_MORE)
			more_TabButton.setHighlighted(true);
	}


	@Override
	public int getContainerViewId() {
		return R.id.layout_fragment;
	}


	public void onSelectedPassportTab() {
		selectTab(INDEX_FRAGMENT_PASSPORT, true);
	}

	public void onSelectedTravelLogTab() {
		selectTab(INDEX_FRAGMENT_TRAVEL_LOG, true);
	}

	public void onSelectedFriendsTab() {
		selectTab(INDEX_FRAGMENT_FRIENDS, true);
	}

	public void onSelectedMoreTab() {
		selectTab(INDEX_FRAGMENT_MORE, true);
	}

	public IabHelper getInAppBillingHelper() {
		return iabHelper;
	}


	public void resetActivity(boolean isAutoStartMission) {
		currentTabIndex = INDEX_FRAGMENT_PASSPORT;
		passportMainFragment.setData(isAutoStartMission);

		// Cannot commit fragment transaction after saveInstanceState. So called with delay.
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				highlightTab(currentTabIndex);
				// Remove other fragments for refresh
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
				for (int i = 0; i < fragmentsListArray.size(); i++) {
					ArrayList<SuperFragment> fragmentsList = fragmentsListArray.get(i);
					for (int j = fragmentsList.size() - 1; j >= 0; j--) {
						if (i == INDEX_FRAGMENT_PASSPORT && j == 0)
							continue;

						SuperFragment fragmentItem = fragmentsList.get(j);
						transaction.remove(fragmentItem);

						if (j != 0) {
							fragmentsList.remove(j);
						}
					}
				}
				transaction.show(passportMainFragment);
				transaction.commit();

				passportMainFragment.reloadAllInformation();
			}
		}, 100);
	}

	public void restartActivity() {
		Intent intent = new Intent(MainActivity.this, SplashActivity.class);
		intent.putExtra(SplashActivity.EXTRA_IS_RESTART_APP, true);
		startActivity(intent);

		finish();
	}

	public KPHTabItem getTabItem(int id) {
		switch (id) {
			case INDEX_FRAGMENT_PASSPORT:
				return passport_TabButton;

			case INDEX_FRAGMENT_TRAVEL_LOG:
				return travelLog_TabButton;

			case INDEX_FRAGMENT_FRIENDS:
				return friends_TabButton;

			case INDEX_FRAGMENT_MORE:
				return more_TabButton;
		}

		return null;
	}

	public void fetchTravelLogUnreadItemCount() {
		RestService.get().getUserTravelLog(
				KPHUserService.sharedInstance().getUserData().getId(),
				new RestCallback<KPHUserTravelLogResponse>() {
					@Override
					public void success(KPHUserTravelLogResponse kphUserTravelLogResponse, Response response) {
						KPHMissionService.sharedInstance().setUnreadTravelLogCount(
								kphUserTravelLogResponse.getUnreadCount()
						);
						updateTabItemBadges();
					}

					@Override
					public void failure(RestError restError) {}
				}
		);
	}

	private void updateTabItemBadges() {
		travelLog_TabButton.setBadgeValue(KPHMissionService.sharedInstance().getUnreadTravelLogCount());
	}


	// Need to go to the google fit / Kid power band connection
	private void gotoCatchGoogleFit() {
		// Go to google fit connection
		Bundle extra = new Bundle();
		extra.putInt(GoogleFitWrapperActivity.EXTRA_USERID, KPHUserService.sharedInstance().getUserData().getId());
		extra.putInt(GoogleFitWrapperActivity.EXTRA_ACTION, GoogleFitService.ACTION_CONNECT);
		extra.putString(GoogleFitWrapperActivity.EXTRA_SYNCDATE, "");

		GoogleFitWrapperActivity.showGoogleFitWrapperActivity(MainActivity.this, extra, REQCODE_SELECT_GOOGLEFIT_DIRECTLY);
	}

	private void gotoCatchKidPowerBand() {
		KPHUserData userData = KPHUserService.sharedInstance().getUserData();

		if (KPHUtils.sharedInstance().checkGPSProviderEnabled(MainActivity.this, true)) {
			RegisterBandFragment fragment = new RegisterBandFragment();
			fragment.setData(userData.getId(), userData.getAvatarId(), userData.getHandle());
			showNewFragment(fragment);
		}
	}


	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// Log the last heart beat time stamp
		heartBeatTimeStamp = Calendar.getInstance().getTimeInMillis();
		return super.dispatchTouchEvent(ev);
	}


	private boolean needToShowCatchTrackerDialog() {
		if (catchTrackerFragment == null)
			return false;

		if (catchTrackerFragment.isAdded())
			return false;

		if (!KPHUserService.sharedInstance().catchTrackerDialogIntervalExpired())
			return false;

		// If already have tracker linked, no need to show
		int trackerType = KPHUserService.sharedInstance().loadCurrentTrackerType();
		if (trackerType != KPHUserService.TRACKER_TYPE_NONE)
			return false;

		if (isShowingProgressDialog()) {
			heartBeatTimeStamp = Calendar.getInstance().getTimeInMillis();
			return false;
		}

		// If passport, edit profile fragments are not shown, no need to show dialog
		Fragment topFragment = getTopFragment();
		if (topFragment != null &&
				!(topFragment instanceof PassportMainFragment) &&
				!(topFragment instanceof EditProfileFragment)) {
			heartBeatTimeStamp = Calendar.getInstance().getTimeInMillis();
			return false;
		}

		// Now, it is on the PassportMainFragment/EditProfileFragment. Need to calculate the time.
		if (heartBeatTimeStamp == 0) {        // Heart beat is not initialized yet
			heartBeatTimeStamp = Calendar.getInstance().getTimeInMillis();
			return false;
		}

		if (Calendar.getInstance().getTimeInMillis() - heartBeatTimeStamp < CATCH_TRACKER_TIMER_INTERVAL)
			return false;

		return true;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQCODE_SELECT_GOOGLEFIT_DIRECTLY && resultCode == RESULT_OK) {
			// Google Fit connection
			if (data.getIntExtra(GoogleFitWrapperActivity.OUT_EXTRA_RETCODE, -1) == GoogleFitService.GOOGLE_FIT_ERROR_NONE) {
				// Success to connect
				KPHNotificationUtil.sharedInstance().showSuccessNotification(MainActivity.this, R.string.sync_band_linked_caption);

				Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_TRACKER_SELECTED);
				intent.putExtra(KPHConstants.PROFILE_DEVICE_SELECTED, true);
				LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
			} else {
				String errMessage = data.getStringExtra(GoogleFitWrapperActivity.OUT_EXTRA_ERROR_MESSAGE);
				showErrorDialog(errMessage);
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	// Method which is called when the user selects Activity Tracker item on Sync Google Fit Fragment
	private void gotoActivityTrackerFragment() {
		ArrayList<String> classesArray = new ArrayList<>();
		classesArray.add("org.unicefkidpower.kid_power.View.Activities.Main.More.MoreMainFragment");
		classesArray.add("org.unicefkidpower.kid_power.View.Activities.Onboarding.EditProfileFragment");
		classesArray.add("org.unicefkidpower.kid_power.View.Activities.Onboarding.OwnDeviceTrackerFragment");

		try {
			showFragmentsForClasses(INDEX_FRAGMENT_MORE, classesArray);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	// Method which is called when the user selects Get In Touch item on Sync Google Fit Fragment
	private void gotoGetInTouchFragment() {
		ArrayList<String> classesArray = new ArrayList<>();
		classesArray.add("org.unicefkidpower.kid_power.View.Activities.Main.More.MoreMainFragment");
		classesArray.add("org.unicefkidpower.kid_power.View.Activities.Main.More.HelpMainFragment");

		try {
			showFragmentsForClasses(INDEX_FRAGMENT_MORE, classesArray);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	/**
	 * Method to show fragments on any tab
	 * @param tabIndex : Tab index to show
	 * @param classesToShow : Class name array to show. Throws exception if the class is not found or not instance of SuperFragment
	 *
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void showFragmentsForClasses(int tabIndex, ArrayList<String> classesToShow)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		int validIndex = 0;

		ArrayList<SuperFragment> fragmentsList = fragmentsListArray.get(tabIndex);
		for (int i = 0; i < fragmentsList.size(); i++) {
			if (i >= classesToShow.size()) {
				break;
			}

			SuperFragment fragmentItem = fragmentsList.get(i);
			Class classItem = Class.forName(classesToShow.get(i));

			if (!classItem.isInstance(fragmentItem)) {
				break;
			}

			validIndex = i;
		}



		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		// Remove needless fragments
		for (int i = fragmentsList.size() - 1; i >= validIndex + 1; i--) {
			SuperFragment fragmentItem = fragmentsList.get(i);

			if (fragmentItem.isAdded())
				transaction.remove(fragmentItem);

			fragmentsList.remove(i);
		}


		// Add new fragments
		for (int i = validIndex + 1; i < classesToShow.size(); i++) {
			Class classItem = Class.forName(classesToShow.get(i));

			SuperFragment fragmentItem = (SuperFragment)classItem.newInstance();
			transaction.add(getContainerViewId(), fragmentItem);

			if (i == classesToShow.size() - 1) {
				if (fragmentItem instanceof OwnDeviceTrackerFragment) {
					KPHUserData userData = KPHUserService.sharedInstance().getUserData();
					((OwnDeviceTrackerFragment)fragmentItem).setUsername(userData.getHandle());
				}

				transaction.show(fragmentItem);
			} else {
				transaction.hide(fragmentItem);
			}

			fragmentsList.add(fragmentItem);
		}

		try {
			transaction.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (validIndex == classesToShow.size() - 1) {
			onSelectedMoreTab();
		} else {
			highlightTab(INDEX_FRAGMENT_MORE);
			currentTabIndex = INDEX_FRAGMENT_MORE;
		}
	}


	public void enableCatchTrackerDialog() {
		catchTrackerFragment = new CatchTrackerFragment();
	}


}
