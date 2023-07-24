package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

import com.jaredrummler.android.device.DeviceName;

import org.json.JSONObject;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.OSDate;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.GoogleFit.GoogleFitService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHAnalyticsService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.Structure.KPHTracker;
import org.unicefkidpower.kid_power.Model.Structure.TrackerSyncResult;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;


/**
 * Created by Dayong Li on 10/16/2016.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class GoogleFitWrapperActivity extends SuperActivity {
	private static final String	 	TAG								= "GoogleFitWrapperActivity";

	public static final String		EXTRA_USERID					= "userId";
	public static final String		EXTRA_ACTION					= "action";
	public static final String		EXTRA_SYNCDATE					= "syncDate";
	public static final String		EXTRA_SHOWPROGRESS				= "showProgress";

	public static final String		OUT_EXTRA_RETCODE				= "RetCode";
	public static final String		OUT_EXTRA_ERROR_MESSAGE			= "ErrorMessage";
	public static final String		OUT_EXTRA_TRACKERSYNCRESULT		= "TrackerSyncResult";

	protected int					retryCount						= SelectTrackerSuperFragment.BACKEND_RETRY_COUNT;

	private GoogleFitService		googleFitService				= null;
	private Handler					timeoutHandler					= new Handler();
	private final int				GOOGLE_TIMEOUT					= 400000 * 1000;

	private int						userId							= 0;
	private int						actionType						= GoogleFitService.ACTION_NONE;
	private Date					lastSyncDate					= null;
	private boolean					showProgress					= false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_register_googlefit);

		// Initialize data from extras
		{
			Bundle bundle = getIntent().getExtras();

			userId = bundle.getInt(EXTRA_USERID, 0);
			actionType = bundle.getInt(EXTRA_ACTION, GoogleFitService.ACTION_NONE);
			showProgress = bundle.getBoolean(EXTRA_SHOWPROGRESS, true);

			String syncDateStr = bundle.getString(EXTRA_SYNCDATE, "");
			lastSyncDate = OSDate.fromStringWithFormat(syncDateStr, "yyyy-MM-dd HH:mm:ss", false);

			if (userId == 0) {
				Logger.error(TAG, "onCreate : GoogleFitWrapperActivity Should set UserId");
				return;
			}

			if (lastSyncDate == null && actionType == GoogleFitService.ACTION_SYNC) {
				Logger.error(TAG, "onCreate : GoogleFitWrapperActivity should be set Last sync date");
				return;
			}
		}

		new Thread(networkStatusRunnable).start();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (googleFitService != null) {
			googleFitService.disconnectToGoogleFit();
			googleFitService = null;
		}
	}

	Runnable networkStatusRunnable = new Runnable() {
		@Override
		public void run() {
			if (showProgress) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showProgressDialog(false);
					}
				});
			}

			boolean result = KPHUtils.sharedInstance().isOnline();
			runOnUiThread(new StartWorkRunnable(result));
		}
	};


	public class StartWorkRunnable implements Runnable {
		public boolean isOnline = false;

		public StartWorkRunnable(boolean isOnline) {
			super();
			this.isOnline = isOnline;
		}

		@Override
		public void run() {
			if (isOnline) {
				Logger.log(TAG, "Online, start google fit");
				startGoogleFitNow();
			} else {
				Logger.log(TAG, "Offline, need to finish");

				Intent intent = new Intent();
				intent.putExtra(OUT_EXTRA_RETCODE, GoogleFitService.GOOGLE_FIT_ERROR_NETWORK_ERROR);
				intent.putExtra(OUT_EXTRA_ERROR_MESSAGE, getString(R.string.google_connection_failed));
				setResult(RESULT_OK, intent);
				onClickedBackSystemButton();
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN &&
				keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}


	private void startGoogleFitNow() {
		googleFitService = new GoogleFitService(GoogleFitWrapperActivity.this, actionType, lastSyncDate);
		googleFitService.setServiceCallbacks(googleFitServiceCallbacks);

		timeoutHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (GoogleFitWrapperActivity.this.isDestroyed())
					return;

				Logger.log(TAG, "startGoogleFitNow : Timeout occurred on GoogleFitWrapperActivity!");

				Intent intent = new Intent();
				intent.putExtra(OUT_EXTRA_RETCODE, GoogleFitService.GOOGLE_FIT_ERROR_NETWORK_ERROR);
				intent.putExtra(OUT_EXTRA_ERROR_MESSAGE, getString(R.string.google_connection_failed));
				setResult(RESULT_OK, intent);
				onClickedBackSystemButton();
			}
		}, GOOGLE_TIMEOUT);

		Logger.log(TAG, "startGoogleFitNow : Timeout handler started on GoogleFitWrapperActivity!");
	}


	private GoogleFitService.GoogleFitServiceCallbacks googleFitServiceCallbacks = new GoogleFitService.GoogleFitServiceCallbacks() {
		@Override
		public void onConnectionResult(int errorCode, String errorMessage) {
			Logger.log(TAG, "startGoogleFitNow : connection result=%s", errorMessage);

			googleFitService.disconnectToGoogleFit();

			if (errorCode == GoogleFitService.GOOGLE_FIT_ERROR_NONE) {
				googleFitConnected();
			} else {
				Intent intent = new Intent();
				intent.putExtra(OUT_EXTRA_RETCODE, errorCode);
				intent.putExtra(OUT_EXTRA_ERROR_MESSAGE, errorMessage);
				setResult(RESULT_OK, intent);
				onClickedBackSystemButton();
			}
		}

		@Override
		public void onSyncResult(int errorCode, String errorMessage, Object object) {
			Logger.log(TAG, "startGoogleFitNow : sync result=%s", errorMessage);

			googleFitService.disconnectToGoogleFit();

			if (errorCode == GoogleFitService.GOOGLE_FIT_ERROR_NONE) {
				syncGoogleFit(object);
			} else if (errorCode == GoogleFitService.GOOGLE_FIT_ERROR_NO_SYNCED_DATA) {
				syncGoogleFit(null);
			} else {
				Intent intent = new Intent();
				intent.putExtra(OUT_EXTRA_RETCODE, errorCode);
				intent.putExtra(OUT_EXTRA_ERROR_MESSAGE, errorMessage);
				setResult(RESULT_OK, intent);
				onClickedBackSystemButton();
			}
		}
	};


	private void googleFitConnected() {
		Map<String, String> payload = new HashMap<>();
		payload.put("type", KPHTracker.TRACKER_TYPE_NAME_GOOGLEFIT);
		KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_TRACKER_LINK_START, payload);

		KPHUserService.sharedInstance().setTrackerToUser(
				DeviceName.getDeviceName(),
				KPHUtils.sharedInstance().getDeviceIdentifier(),
				userId,
				KPHTracker.TRACKER_TYPE_NAME_GOOGLEFIT,
				KPHTracker.TRACKER_VERSION_NAME_GOOGLEFIT,
				setTrackerActionListener
		);
	}


	private void syncGoogleFit(Object object) {
		dismissProgressDialog();

		if (object != null) {
			TrackerSyncResult syncedResult = (TrackerSyncResult) object;

			JSONObject jsonResult = syncedResult.encodeToJSON();
			String jsonResultStr = jsonResult.toString();

			Logger.log(TAG, "Sync Google Fit Result : Object is not null");

			Intent intent = new Intent();
			intent.putExtra(OUT_EXTRA_RETCODE, GoogleFitService.GOOGLE_FIT_ERROR_NONE);
			intent.putExtra(OUT_EXTRA_TRACKERSYNCRESULT, jsonResultStr);
			setResult(RESULT_OK, intent);
		} else {
			Logger.log(TAG, "Sync Google Fit Result : Object is null");

			Intent intent = new Intent();
			intent.putExtra(OUT_EXTRA_RETCODE, GoogleFitService.GOOGLE_FIT_ERROR_NO_SYNCED_DATA);
			setResult(RESULT_OK, intent);
		}

		onClickedBackSystemButton();
	}


	private onActionListener setTrackerActionListener = new onActionListener() {
		@Override
		public void completed(Object object) {
			dismissProgressDialog();

			if (!(object instanceof KPHTracker)) {
				return;
			}

			Logger.log(TAG, "Set tracker success");

			// Log GF tracker linked timestamp
			KPHUserService.sharedInstance().saveGFTime();

			Map<String, String> payload = new HashMap<>();
			payload.put("type", KPHTracker.TRACKER_TYPE_NAME_GOOGLEFIT);
			KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_TRACKER_LINK_SUCCESS, payload);

			Intent intent = new Intent();
			intent.putExtra(OUT_EXTRA_RETCODE, GoogleFitService.GOOGLE_FIT_ERROR_NONE);
			setResult(Activity.RESULT_OK, intent);
			onClickedBackSystemButton();
		}

		@Override
		public void failed(int code, String message) {
			if (code == -2 && retryCount > 0) {
				// if network error
				retryCount--;

				try {
					sleep(1000);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}

				Logger.log(TAG, "Set tracker failed : retry");
				googleFitConnected();
			} else {
				Logger.log(TAG, "Set tracker failed : return");

				Map<String, String> payload = new HashMap<>();
				payload.put("type", KPHTracker.TRACKER_TYPE_NAME_GOOGLEFIT);
				KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_TRACKER_LINK_ERROR, payload);

				Intent intent = new Intent();
				intent.putExtra(OUT_EXTRA_RETCODE, GoogleFitService.GOOGLE_FIT_ERROR_TIMEOUT);
				intent.putExtra(OUT_EXTRA_ERROR_MESSAGE, message);
				setResult(RESULT_OK, intent);
				onClickedBackSystemButton();
			}
		}
	};


	public static void showGoogleFitWrapperActivity(SuperActivity parentActivity, Bundle bundle, int requestCode) {
		parentActivity.pushNewActivityAnimated(
				GoogleFitWrapperActivity.class,
				AnimConst.ANIMDIR_NONE,
				0,
				bundle,
				requestCode);
	}
}
