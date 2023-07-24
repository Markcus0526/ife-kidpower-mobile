package org.unicefkidpower.kid_power.View.Activities.Main.Passport;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.OSDate;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Misc.onBandActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble.BleManager;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.CBBandSync;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamSimple;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamSync;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.EventManager;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.SEvent;
import org.unicefkidpower.kid_power.Model.MicroService.GoogleFit.GoogleFitService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHAnalyticsService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHMissionService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.Structure.KPHCheer;
import org.unicefkidpower.kid_power.Model.Structure.KPHDailyDetailData;
import org.unicefkidpower.kid_power.Model.Structure.KPHDelight;
import org.unicefkidpower.kid_power.Model.Structure.KPHDelightInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHMissionInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHSyncSnapshots;
import org.unicefkidpower.kid_power.Model.Structure.KPHTracker;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMission;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMissionStats;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserStats;
import org.unicefkidpower.kid_power.Model.Structure.TrackerSyncResult;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.GoogleFitWrapperActivity;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHImageTextButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHPacketView;
import org.unicefkidpower.kid_power.View.CustomControls.KPHSyncMark;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperNormalSizeDialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.CBBandSync.Report_Type.Sync_Start;


/**
 * Created by Dayong Li on 9/16/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class SyncTrackerFragment extends SuperNormalSizeDialogFragment {
	private static final String			TAG											= "SyncTrackerFragment";

	private static final int			SYNC_SERVICE_MAX_DAYS_AGO_ACTIVITY			= 29;

	protected static final String		STEP1_FOR_GET_LAST_DAY						= "stepForGetLastDay";
	protected static final String		STEP2_FOR_SYNC_ACTIVITIES_FOR_TRACKER		= "stepForDailyDetailForTracker";
	protected static final String		STEP3_FOR_STAMP_FROM_SERVER					= "stepForStamp";
	protected static final String		STEP4_FOR_SHOW_NEW_INFORMATION				= "stepForShowNewInformation";
	protected static final String		STEP5_FOR_COMPLETED_MISSION_FROM_SERVER		= "stepForMissionComplete";
	protected static final String		STEP6_FOR_RESET_MISSION_FOR_TRACKER			= "stepForResetMissionForTracker";
	protected static final String		STEP_FOR_FINAL								= "stepForFinal";

	protected static final int			BACKEND_RETRY_COUNT			= 5;
	protected static final int			BLUETOOTH_RETRY_COUNT		= 3;

	protected View						contentView					= null;

	protected KPHSyncMark				syncMark					= null;
	protected LinearLayout				llBody						= null;
	protected LinearLayout				llPage1						= null;
	protected LinearLayout				llPage2						= null;

	// for llPage1
	protected KPHTextView				syncPowerPoints				= null;
	protected KPHTextView				syncPPUnits					= null;
	protected KPHPacketView				syncRUTF					= null;

	// for llPage2
	protected KPHTextView				resultStatus				= null;
	protected KPHTextView				resultSummary				= null;
	protected KPHTextView				resultReadmore				= null;
	protected LinearLayout				resultPackets				= null;
	protected KPHPacketView				earnedRUTF					= null;
	protected KPHImageTextButton		delightPlaceholder1			= null;
	protected KPHImageTextButton		delightPlaceholder2			= null;
	protected KPHImageTextButton		delightPlaceholder3			= null;
	protected KPHButton					btnGoto						= null;

	protected int						nRetry						= BACKEND_RETRY_COUNT;
	protected boolean					isProcessing				= false;

	// Parameters for Syncing
	protected String					userName			= "";
	protected String					avatarId			= "";
	protected KPHMissionService			serviceForMission	= null;
	protected KPHTracker				curTracker			= null;
	protected KPHSyncSnapshots			snapshots			= null;

	protected int						needSyncDays		= 0;
	protected Date						lastSyncDate		= null;
	protected TrackerSyncResult			syncedResult		= null;

	protected Handler					showDelayHandler	= null;
	protected Runnable					showDelayRunnable	= null;

	protected int						trackerType			= KPHUserService.TRACKER_TYPE_NONE;

	private IntentFilter				intentFilter		= null;
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (getActivity() == null) {
				// Already detached
				return;
			}

			String action = intent.getAction();
			switch (action) {
				case KPHBroadcastSignals.BROADCAST_SIGNAL_SYNC_GOOGLE_FIT_SYNC_DATE: {
					String datestr = intent.getStringExtra("SyncDate");
					Date syncDate = OSDate.fromStringWithFormat(datestr, "yyyy-MM-dd", false);
					populateSyncingStatus(Sync_Start, syncDate);
					break;
				}
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		serviceForMission = KPHMissionService.sharedInstance();

		if (contentView != null)
			return contentView;

		if (intentFilter == null) {
			intentFilter = new IntentFilter();
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_SYNC_GOOGLE_FIT_SYNC_DATE);

			LocalBroadcastManager.getInstance(getSafeContext()).registerReceiver(receiver, intentFilter);
		}

		needSyncDays = 0;
		lastSyncDate = null;
		syncedResult = null;

		contentView = super.onCreateView(inflater, container, savedInstanceState);

		syncMark = (KPHSyncMark) contentView.findViewById(R.id.syncMark);
		llBody = (LinearLayout) contentView.findViewById(R.id.layoutBody);
		llPage1 = (LinearLayout) contentView.findViewById(R.id.layoutBodyPage1);
		llPage2 = (LinearLayout) contentView.findViewById(R.id.layoutBodyPage2);

		syncPowerPoints = (KPHTextView) llPage1.findViewById(R.id.tvPowerPoints);
		syncPPUnits = (KPHTextView) llPage1.findViewById(R.id.tvPPUnits);
		syncRUTF = (KPHPacketView) llPage1.findViewById(R.id.syncRUTF);

		resultStatus = (KPHTextView) llPage2.findViewById(R.id.resultStatus);
		resultSummary = (KPHTextView) llPage2.findViewById(R.id.resultSummary);
		resultReadmore = (KPHTextView) llPage2.findViewById(R.id.resultReadmore);
		resultPackets = (LinearLayout) llPage2.findViewById(R.id.layoutPackets);
		earnedRUTF = (KPHPacketView) resultPackets.findViewById(R.id.earnedRUTF);
		delightPlaceholder1 = (KPHImageTextButton) resultPackets.findViewById(R.id.unlockedDelight1);
		delightPlaceholder2 = (KPHImageTextButton) resultPackets.findViewById(R.id.unlockedDelight2);
		delightPlaceholder3 = (KPHImageTextButton) resultPackets.findViewById(R.id.unlockedDelight3);

		btnGoto = (KPHButton) contentView.findViewById(R.id.btnGoto);
		btnGoto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedGoTo();
			}
		});

		String readMorePart = getSafeContext().getString(R.string.read_more);
		ClickableSpan readMoreSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onClickedReadMore();
			}
			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setColor(resultReadmore.getCurrentTextColor());
			}
		};

		SpannableString ss = new SpannableString(getSafeContext().getString(R.string.sync_band_not_seem_right_read_more));
		ss.setSpan(readMoreSpan, ss.toString().indexOf(readMorePart), ss.toString().indexOf(readMorePart) + readMorePart.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		resultReadmore.setText(ss);
		resultReadmore.setMovementMethod(LinkMovementMethod.getInstance());
		resultReadmore.setHighlightColor(Color.TRANSPARENT);
		resultReadmore.setVisibility(View.GONE);

		showDelayHandler = new Handler(getSafeContext().getMainLooper());


		curTracker = KPHUserService.sharedInstance().currentTracker();
		trackerType = KPHUserService.sharedInstance().loadCurrentTrackerType();

		return contentView;
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_band_sync;
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BleManager.REQUEST_ENABLE_BLE) {
			boolean isEnabled = BleManager.sharedInstance().isBleEnabled();
			if (resultCode == Activity.RESULT_CANCELED || !isEnabled) {
				getParentActivity().showAlertDialog(getSafeContext().getString(R.string.bluetooth), getSafeContext().getString(R.string.ble_cannot_enabled), null);
				return;
			} else if (resultCode == Activity.RESULT_OK) {
				startProcessing();
			}
		} else if (requestCode == MainActivity.REQCODE_SYNC_GOOGLEFIT) {
			if (resultCode == Activity.RESULT_OK) {
				int retCode = data.getIntExtra(GoogleFitWrapperActivity.OUT_EXTRA_RETCODE, 0);

				Logger.log(TAG, "onActivityResult : Retcode error : " + retCode);

				if (retCode == GoogleFitService.GOOGLE_FIT_ERROR_NONE) {
					String jsonStr = data.getStringExtra(GoogleFitWrapperActivity.OUT_EXTRA_TRACKERSYNCRESULT);
					try {
						JSONObject jsonObject = new JSONObject(jsonStr);
						syncedResult = TrackerSyncResult.decodeFromJSON(jsonObject);
					} catch (Exception ex) {
						ex.printStackTrace();
						onException("onActivityResult STEP2 : syncing with google fit", ex.getMessage());
					}

					// Log last sync/link GF time.
					KPHUserService.sharedInstance().saveGFTime();

					EventManager.sharedInstance().post(STEP3_FOR_STAMP_FROM_SERVER, syncedResult, "");
				} else if (retCode == GoogleFitService.GOOGLE_FIT_ERROR_NO_SYNCED_DATA) {
					this.dismiss();
					showSyncNoDataFragment();
				} else {
					String errorMessage = data.getStringExtra(GoogleFitWrapperActivity.OUT_EXTRA_ERROR_MESSAGE);
					onSyncingError(-1, "Error occurred", errorMessage);
				}
			} else {
				// Unreachable code
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Logger.log(TAG, "onDetach : Start");

		if (EventManager.sharedInstance().isRegistered(this)) {
			EventManager.sharedInstance().unregister(this);
		}

		if (isProcessing) {
			Logger.error(TAG, "onDetach : It will be stop");
			serviceForMission.stopForBand(getSafeContext());
			isProcessing = false;
		}

		if (receiver != null) {
			LocalBroadcastManager.getInstance(getSafeContext()).unregisterReceiver(receiver);
			intentFilter = null;
		}
	}


	private void onClickedGoTo() {
		if (btnGoto.getText().toString().equalsIgnoreCase(getSafeContext().getString(R.string.button_next))) {
			CheerUnlockedFragment fragCheerUnlocked = new CheerUnlockedFragment();
			fragCheerUnlocked.setData(snapshots.unlockedCheerIDs);
			getParentActivity().showDialogFragment(fragCheerUnlocked);
		}

		dismiss();
	}


	protected void startProcessing() {
		if (serviceForMission == null) {
			dismiss();
			return;
		}

		if (curTracker == null)
			return;

		if (isProcessing) {
			Logger.error(TAG, "startProcessing : Syncing was already started");
			return;
		}

		isProcessing = true;

		Logger.log(TAG, "startProcessing : started Syncing");

		nRetry = BACKEND_RETRY_COUNT;
		EventManager.sharedInstance().post(STEP1_FOR_GET_LAST_DAY, curTracker, curTracker.getDeviceCode());

		KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_TRACKER_SYNC_START);
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent event) {
		if (EventManager.isEvent(event, STEP1_FOR_GET_LAST_DAY)) {
			try {
				final KPHTracker tracker = (KPHTracker) event.object;

				Logger.log(TAG, "onEventMainThread : STEP1 : Getting needed sync days");

				updateUI(event.name);
				serviceForMission.getLastSnapshot(tracker.getDeviceId(), new onActionListener() {
					@Override
					public void completed(Object object) {
						KPHSyncSnapshots syncSnapshot = (KPHSyncSnapshots) object;

						if (syncSnapshot != null) {
							// getV1 last Sync date and ahead 15 minutes to prevent loss activity data
							OSDate lastDate = new OSDate(OSDate.fromUTCString(syncSnapshot.lsd));

							if (trackerType == KPHUserService.TRACKER_TYPE_GOOGLEFIT) {
								lastSyncDate = lastDate.offsetByHHMMSS(0, 0, 0);//
							} else {
								lastSyncDate = lastDate.offsetByHHMMSS(0, -15, 0);//
							}

							needSyncDays = OSDate.daysBetweenDates(lastSyncDate, new Date());
						} else {
							needSyncDays = SYNC_SERVICE_MAX_DAYS_AGO_ACTIVITY;
						}

						if (needSyncDays > SYNC_SERVICE_MAX_DAYS_AGO_ACTIVITY)
							needSyncDays = SYNC_SERVICE_MAX_DAYS_AGO_ACTIVITY;

						OSDate syncing_date = (new OSDate(new Date())).offsetDay(-1 * needSyncDays);
						populateSyncingStatus(Sync_Start, syncing_date);

						Logger.log(TAG, "onEventMainThread : STEP1 : last synced date = %s, needed sync days = %d", syncSnapshot == null ? "no date" : lastSyncDate.toString(), needSyncDays + 1);

						// Completed link band
						nRetry = BLUETOOTH_RETRY_COUNT;
						EventManager.sharedInstance().post(STEP2_FOR_SYNC_ACTIVITIES_FOR_TRACKER, tracker, tracker.getDeviceCode());
					}

					@Override
					public void failed(int code, String message) {
						if (code != -2) {
							// other case maybe error
							Logger.error(TAG, "onEventMainThread : STEP1 : Error occurred");
							onSyncingError(code, "Error occurred", message);
						} else {
							if (nRetry > 0) {
								// if network error, try again
								nRetry--;
								Logger.error(TAG, "onEventMainThread : STEP1 : retry again(%d times)", nRetry);
								EventManager.sharedInstance().post(STEP1_FOR_GET_LAST_DAY, curTracker, curTracker.getDeviceCode());
							} else {
								Logger.error(TAG, "onEventMainThread : STEP1 : Network error");
								onSyncingError(-1, "Error occurred", "Network error");
							}
						}
					}
				});
			} catch (Exception e) {
				onException("onEventMainThread : STEP1 : getting syncing days on server", e.getMessage());
			}
		} else if (EventManager.isEvent(event, STEP2_FOR_SYNC_ACTIVITIES_FOR_TRACKER)) {
			try {
				String display_name = KPHUserService.sharedInstance().getUserData().getHandle();
				KPHUserStats userStats = KPHUserService.sharedInstance().getUserData().getUserStats();

				int totalPPS = userStats == null ? 0 : userStats.getTotalPowerPoints();
				Logger.log(TAG, "onEventMainThread : STEP2 : Syncing with band(%s:%s) = %d days, totalPPs = %d", display_name, curTracker.getDeviceCode(), needSyncDays, totalPPS);

				updateUI(event.name);

				if (trackerType == KPHUserService.TRACKER_TYPE_GOOGLEFIT) {
					Bundle bundle = new Bundle();
					bundle.putInt(GoogleFitWrapperActivity.EXTRA_USERID, KPHUserService.sharedInstance().getUserData().getId());
					bundle.putInt(GoogleFitWrapperActivity.EXTRA_ACTION, GoogleFitService.ACTION_SYNC);
					bundle.putString(GoogleFitWrapperActivity.EXTRA_SYNCDATE, new OSDate(lastSyncDate).toStringWithFormat("yyyy-MM-dd HH:mm:ss"));
					bundle.putBoolean(GoogleFitWrapperActivity.EXTRA_SHOWPROGRESS, false);

					GoogleFitWrapperActivity.showGoogleFitWrapperActivity(getParentActivity(), bundle, MainActivity.REQCODE_SYNC_GOOGLEFIT);
				} else {
					CBParamSync param = CBParamSync.makeParams(getSafeContext(), curTracker.getDeviceCode(), lastSyncDate, needSyncDays, display_name, totalPPS);

					serviceForMission.getDailyDetailedForBand(param, new onBandActionListener() {
						@Override
						public void completed(Object object) {
							if (object != null) {
								syncedResult = (TrackerSyncResult) object;
							}

							Logger.log(TAG, "onEventMainThread : STEP2 : success syncing with band");
							// Completed link band, goto next step
							if (syncedResult.activities.size() > 0) {
								nRetry = BACKEND_RETRY_COUNT;
								EventManager.sharedInstance().post(STEP3_FOR_STAMP_FROM_SERVER, syncedResult, "");
							} else {
								snapshots = new KPHSyncSnapshots();
								EventManager.sharedInstance().post(STEP_FOR_FINAL, snapshots, "");
							}
						}

						@Override
						public void failed(int code, String message) {
							if (code == BleManager.BT_ERROR_NO_ANY_DEVICES) {
								Logger.error(TAG, "onEventMainThread : STEP2 : No Devices");
								onSyncingError(-2, "", getSafeContext().getString(R.string.sync_not_found_band));
							} else if (code == BleManager.BT_ERROR_NOT_FOUND) {
								Logger.error(TAG, "onEventMainThread : STEP2 : No Bands found : device code = \"%s\", device Id = \"%s\"",
										curTracker.getDeviceCode(), curTracker.getDeviceId());
								onSyncingError(-3, "", getSafeContext().getString(R.string.sync_not_found_band));
							} else if (code == BleManager.BT_ERROR_NO_SERVICE) {
								Logger.error(TAG, "onEventMainThread : STEP2 : No BLE Service");
								onSyncingError(-4, "", getSafeContext().getString(R.string.sync_band_no_service));
							} else if (nRetry > 0) {
								// try again
								Logger.error(TAG, "onEventMainThread : STEP2 : syncing failed, try again(%d times)", nRetry);
								nRetry--;
								EventManager.sharedInstance().post(STEP2_FOR_SYNC_ACTIVITIES_FOR_TRACKER, curTracker, curTracker.getDeviceCode());
							} else {
								// failed, goto error state
								Logger.error(TAG, "onEventMainThread : STEP2 : syncing failed with band");
								onSyncingError(-5, "Error occurred", "Sync unsuccessful");
							}
						}

						@Override
						public void reportStatus(Object param) {
							if (!(param instanceof CBBandSync.CBBandSyncReport)) {
								return;
							}
							CBBandSync.CBBandSyncReport report = (CBBandSync.CBBandSyncReport) param;
							populateSyncingStatus(report.type, report.date);
						}
					});
				}
			} catch (Exception e) {
				onException("onEventMainThread : STEP2 : syncing with band", e.getMessage());
			}
		} else if (EventManager.isEvent(event, STEP3_FOR_STAMP_FROM_SERVER)) {
			try {
				if (syncedResult == null ||
						syncedResult.activities == null ||
						syncedResult.activities.isEmpty()) {
					Logger.log(TAG, "onEventMainThread : STEP3 : There are EMPTY activity");
					syncedResult = new TrackerSyncResult();
				}

				int newPowerPoints = syncedResult.newPowerPoints;
				Logger.log(TAG,
						"onEventMainThread : STEP3 : Synced with tracker for %d days, newPPS = %d, Calories = %f, steps = %s",
						needSyncDays + 1,
						newPowerPoints,
						syncedResult.newCalories,
						syncedResult.newSteps);
				Logger.log(TAG, "onEventMainThread : STEP3 : upload activities data");

				List<KPHDailyDetailData> activities = syncedResult.activities;

/* Test code. Added by Ruifeng Shi. 2017.04.04 */
/*
				if (syncedResult != null &&
						syncedResult.activities.size() > 0 &&
						syncedResult.activities.get(syncedResult.activities.size() - 1).data.size() > 0) {
					KPHDailyDetailData.DetailItem item = syncedResult.activities
							.get(syncedResult.activities.size() - 1).data
							.get(syncedResult.activities.get(syncedResult.activities.size() - 1).data.size() - 1);

					item.steps = 27000;
					item.calories = 0;

					Logger.log("SyncTrackerFragment", "Step is 10000");
				}
*/

				serviceForMission.uploadActivityDetails(curTracker.getDeviceId(), activities, new onActionListener() {
					@Override
					public void completed(Object object) {
						Logger.log(TAG, "onEventMainThread : STEP3 : uploading to server success");

						if (object == null || !(object instanceof KPHSyncSnapshots)) {
							EventManager.sharedInstance().post(STEP_FOR_FINAL, snapshots, "");
							return;
						}

						snapshots = (KPHSyncSnapshots) object;
						Logger.log(TAG, "onEventMainThread : STEP3 : uploaded to server success, SyncScreens = %s",
								snapshots.syncCompleteScreens != null && !snapshots.syncCompleteScreens.isEmpty() ? "YES" : "NO");

						Logger.log(TAG, "onEventMainThread : STEP3 : newBuzzPoints = %d", snapshots.newBuzzPoints);

						// Sort array of missions by their IDs
						Collections.sort(snapshots.delightsUnlocked, new Comparator<KPHDelight>() {
							@Override
							public int compare(KPHDelight lhs, KPHDelight rhs) {
								return lhs.getId() < rhs.getId() ? -1 : (lhs.getId() == rhs.getId() ? 0 : 1);
							}
						});

						EventManager.sharedInstance().post(STEP4_FOR_SHOW_NEW_INFORMATION, snapshots, "");
					}

					@Override
					public void failed(int code, String message) {
						if (code != -2) {
							// other case maybe error
							Logger.error(TAG, "onEventMainThread : STEP3 : Error occurred");
							onSyncingError(code, "Error occurred", message);
						} else {
							if (nRetry > 0) {
								// if network error, try again
								Logger.error(TAG, "onEventMainThread : STEP3 : failed, try again (% dtimes)", nRetry);
								nRetry--;
								EventManager.sharedInstance().post(STEP3_FOR_STAMP_FROM_SERVER, syncedResult, "");
							} else {
								Logger.error(TAG, "onEventMainThread : STEP3 : Network Error");
								onSyncingError(-1, "Error occurred", "Network error");
							}
						}
					}
				});
			} catch (Exception e) {
				onException("onEventMainThread : STEP3 : uploading activity to server", e.getMessage());
			}
		} else if (EventManager.isEvent(event, STEP4_FOR_SHOW_NEW_INFORMATION)) {
			if (snapshots.newBuzzPoints > 0) {
				updateUI(event.name);
				if (snapshots.newBuzzPoints == 1) {
					syncPPUnits.setText("POWER POINT");
				} else {
					syncPPUnits.setText("POWER POINTS");
				}

				syncPowerPoints.setText("" + snapshots.newBuzzPoints);
				showDelayRunnable = new Runnable() {
					@Override
					public void run() {
						serviceForMission.updateUserMissionFromSnapshot(snapshots);
						snapshots.syncstatus = getSafeContext().getString(R.string.sync_well_done);
						if (snapshots.disablingMission) {
							// if completed this mission, goto completing mission with server
							nRetry = BACKEND_RETRY_COUNT; // reset retry count for backend
							Logger.log(TAG, "onEventMainThread : STEP4 : mission was completed");
							EventManager.sharedInstance().post(STEP5_FOR_COMPLETED_MISSION_FROM_SERVER, Integer.valueOf(snapshots.missionId), "");
						} else {
							// goto final step
							Logger.log(TAG, "onEventMainThread : STEP4 : uploaded with successfully");
							EventManager.sharedInstance().post(STEP_FOR_FINAL, snapshots, "");
						}
					}
				};

				showDelayHandler.postDelayed(showDelayRunnable, 4000);
			} else {
				serviceForMission.updateUserMissionFromSnapshot(snapshots);
				snapshots.syncstatus = getSafeContext().getString(R.string.sync_well_done);

				if (snapshots.disablingMission) {
					// if completed this mission, goto completing mission with server
					nRetry = BACKEND_RETRY_COUNT;//reset retry count for backend
					Logger.log(TAG, "onEventMainThread : STEP4 : mission was completed");
					EventManager.sharedInstance().post(STEP5_FOR_COMPLETED_MISSION_FROM_SERVER, Integer.valueOf(snapshots.missionId), "");
				} else {
					// goto final step
					Logger.log(TAG, "onEventMainThread : STEP4 : uploaded with successfully");
					EventManager.sharedInstance().post(STEP_FOR_FINAL, snapshots, "");
				}
			}
		} else if (EventManager.isEvent(event, STEP5_FOR_COMPLETED_MISSION_FROM_SERVER)) {
			try {
				updateUI(event.name);

				if (snapshots == null || !snapshots.disablingMission) {
					Logger.error(TAG, "onEventMainThread : STEP5 : snapshot is wrong");
					EventManager.sharedInstance().post(STEP_FOR_FINAL, null, "");
					return;
				}

				Logger.log(TAG, "onEventMainThread : STEP5 : Mission Completing start = %d", snapshots.missionId);
				if (snapshots.missionId == 0) {
					// invalid missionID, goto next step
					Logger.error(TAG, "onEventMainThread : STEP5 : invalid mission identifier");
					if (trackerType == KPHUserService.TRACKER_TYPE_KIDPOWERBAND) {
						nRetry = BLUETOOTH_RETRY_COUNT;
						EventManager.sharedInstance().post(STEP6_FOR_RESET_MISSION_FOR_TRACKER, curTracker, curTracker.getDeviceCode());
					}
				} else {
					Logger.log(TAG, "onEventMainThread : STEP5 : call api for completing mission for %d", snapshots.missionId);
					serviceForMission.completeMission(snapshots.missionId, new onActionListener() {
						@Override
						public void completed(Object object) {
							Logger.log(TAG, "onEventMainThread : STEP4 : success");
							if (object instanceof KPHUserMission) {
								KPHUserMissionStats stats = serviceForMission.userMissionStatsFromUserMission((KPHUserMission) object);
								serviceForMission.updateUserMissionStatus(stats);
							}

							Logger.log(TAG, "onEventMainThread : STEP5 : mission was completed, goto disable mission mode");

							// goto step for disabling mission mode to band
							if (trackerType == KPHUserService.TRACKER_TYPE_GOOGLEFIT) {
								snapshots.syncstatus = getSafeContext().getString(R.string.sync_mission_completed);
								EventManager.sharedInstance().post(STEP_FOR_FINAL, snapshots, "");
							} else {
								nRetry = BLUETOOTH_RETRY_COUNT;
								EventManager.sharedInstance().post(STEP6_FOR_RESET_MISSION_FOR_TRACKER, curTracker, curTracker.getDeviceCode());
							}
						}

						@Override
						public void failed(int code, String message) {
							if (code != -2) {
								// other case maybe error
								Logger.error(TAG, "onEventMainThread : STEP5 : Error occurred");
								EventManager.sharedInstance().post(STEP_FOR_FINAL, null, "");
							} else {
								if (nRetry > 0) {
									// if network error, try again
									Logger.error(TAG, "onEventMainThread : STEP5 : failed, try again");
									nRetry--;
									EventManager.sharedInstance().post(STEP5_FOR_COMPLETED_MISSION_FROM_SERVER, snapshots, "");
								} else {
									Logger.error(TAG, "onEventMainThread : STEP5 : Network error");
									onSyncingError(-1, "Error occurred", "Network error");
								}
							}
						}
					});
				}
			} catch (Exception e) {
				onException("onEventMainThread : STEP5 : reset user mission on server", e.getMessage());
			}
		} else if (EventManager.isEvent(event, STEP6_FOR_RESET_MISSION_FOR_TRACKER)) {
			if (trackerType == KPHUserService.TRACKER_TYPE_KIDPOWERBAND) {
				try {
					Logger.log(TAG, "onEventMainThread : STEP6 : disable mission mode");

					// don't remove under line, this is important
					BleManager.sharedInstance().enableAdapter();
					updateUI(event.name);

					CBParamSimple param = CBParamSimple.makeParams(getSafeContext(), curTracker.getDeviceCode());
					serviceForMission.completeMissionForBand(param, new onBandActionListener() {
						@Override
						public void completed(Object object) {
							// Completed link band
							Logger.log(TAG, "onEventMainThread : STEP6 : disabled mission mode");

							snapshots.syncstatus = getSafeContext().getString(R.string.sync_mission_completed);
							EventManager.sharedInstance().post(STEP_FOR_FINAL, snapshots, "");
						}

						@Override
						public void failed(int code, String message) {
							if (code == BleManager.BT_ERROR_NO_ANY_DEVICES) {
								Logger.error(TAG, "onEventMainThread : STEP6 : no any devices");
								onSyncingError(-2, "", getSafeContext().getString(R.string.sync_not_found_band));
							} else if (code == BleManager.BT_ERROR_NOT_FOUND) {
								Logger.error(TAG, "onEventMainThread : STEP6 : no band found");
								onSyncingError(-3, "", getSafeContext().getString(R.string.sync_not_found_band));
							} else if (code == BleManager.BT_ERROR_NO_SERVICE) {
								Logger.error(TAG, "onEventMainThread : STEP6 : ble service error");
								onSyncingError(-4, "", getSafeContext().getString(R.string.sync_band_no_service));
							} else if (nRetry > 0) {
								// try again
								Logger.error(TAG, "onEventMainThread : STEP6 : failed, try again(%d times)", nRetry);
								nRetry--;
								EventManager.sharedInstance().post(STEP6_FOR_RESET_MISSION_FOR_TRACKER, curTracker, curTracker.getDeviceCode());
							} else {
								Logger.error(TAG, "onEventMainThread STEP6 : failed");
								onSyncingError(-6, "Error occurred", "Failed to reset mission mode.");
							}
						}

						@Override
						public void reportStatus(Object param) {}
					});
				} catch (Exception e) {
					onException("onEventMainThread : STEP6 : disabling mission mode on band", e.getMessage());
				}
			}
		} else if (EventManager.isEvent(event, STEP_FOR_FINAL)) {
			try {
				Logger.log(TAG, "onEventMainThread : FINAL : syncing done");

				if (snapshots != null && snapshots.delightsUnlocked != null) {
					ArrayList<KPHDelight> newDelightsUnlocked = new ArrayList<>();
					ArrayList<Long> unlockedCheerIDs = new ArrayList<>();
					for (int i = 0; i < snapshots.delightsUnlocked.size(); i++) {
						if (snapshots.delightsUnlocked.get(i).getType().equalsIgnoreCase(KPHCheer.CHEER_TYPE_CUSTOM)) {
							unlockedCheerIDs.add(snapshots.delightsUnlocked.get(i).getId());
						} else {
							newDelightsUnlocked.add(snapshots.delightsUnlocked.get(i));
						}
					}

					snapshots.delightsUnlocked = newDelightsUnlocked;
					snapshots.unlockedCheerIDs = unlockedCheerIDs;
				}

				updateUI(event.name);

				Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_USER_DATA_UPDATED);
				LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
			} catch (Exception e) {
				onException("onEventMainThread : FINAL", e.getMessage());
			}
		}
	}


	protected void onSyncingError(int code, String status, String error) {
		if (status == null)
			status = "unknown status";
		if (error == null)
			error = "unknown error";

		Logger.error(TAG, "onSyncingError : status = %s, message = %s", status, error);

		Map<String, String> map = new HashMap<> ();
		map.put("error_code", "" + code);
		map.put("error_description", error);
		map.put("error_status", status);
		KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_TRACKER_SYNC_ERROR, map);

		isProcessing = false;

		syncMark.stopAnimation();
		syncRUTF.stopAnimation();

		resultStatus.setText(status);
		resultSummary.setText(error);
		resultPackets.setVisibility(View.GONE);

		if (code == 0) {
			syncMark.setName(userName + " " + getSafeContext().getString(R.string.synced_small));
		} else {
			syncMark.setName(" " + getSafeContext().getString(R.string.not_synced));
		}
		syncMark.setStatus("");

		llBody.setVisibility(View.VISIBLE);
		llPage1.setVisibility(View.GONE);
		llPage2.setVisibility(View.VISIBLE);
		btnGoto.setVisibility(View.VISIBLE);
		btnGoto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_TRACKER_SYNC_ERROR_OK);
			}
		});

		setCancelable(true);
	}


	protected void onSyncingSuccess() {
		Map<String, String> map = new HashMap<>();
		setUISnapshots(snapshots);
		if (snapshots != null && snapshots.delightsUnlocked != null) {
			// this is for temporary add for #117062299
			if (snapshots.packetsUnlocked > 0 || snapshots.delightsUnlocked.size() > 0) {
				serviceForMission.addTravelLogFromSnapshot(snapshots);
			}

			map.put("delights_unlocked", "" + snapshots.delightsUnlocked);
			map.put("packets_unlocked", "" + snapshots.packetsUnlocked);
		}
		KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_TRACKER_SYNC_SUCCESS, map);

		// populate button
		btnGoto.setVisibility(View.VISIBLE);
	}


	protected void onException(String step, String exception) {
		Logger.error(TAG, "onException\n" + exception);
		onSyncingError(-7, getSafeContext().getString(R.string.sync_error_unknown), getSafeContext().getString(R.string.ble_error_unknown));
	}


	protected void updateUI(String step) {
		if (step.equals(STEP1_FOR_GET_LAST_DAY)) {
		} else if (step.equals(STEP2_FOR_SYNC_ACTIVITIES_FOR_TRACKER)) {
		} else if (step.equals(STEP3_FOR_STAMP_FROM_SERVER) ||
				step.equals(STEP4_FOR_SHOW_NEW_INFORMATION)) {
			if (btnGoto.getVisibility() != View.GONE || !isProcessing)
				return;

			syncMark.startAnimation(trackerType);
			syncMark.setName(getSafeContext().getString(R.string.syncing) + " " + userName);
			syncMark.setStatus("");

			syncRUTF.startAnimation();

			llBody.setVisibility(View.VISIBLE);
			llPage1.setVisibility(View.VISIBLE);
			llPage2.setVisibility(View.GONE);
		} else if (step.equals(STEP5_FOR_COMPLETED_MISSION_FROM_SERVER)) {
		} else if (step.equals(STEP6_FOR_RESET_MISSION_FOR_TRACKER)) {
		} else if (step.equals(STEP_FOR_FINAL)) {
			isProcessing = false;

			syncMark.stopAnimation();
			syncMark.setName(userName + " " + getSafeContext().getString(R.string.synced_small));
			syncMark.setStatus("");
			syncRUTF.stopAnimation();

			llBody.setVisibility(View.VISIBLE);
			llPage1.setVisibility(View.GONE);
			llPage2.setVisibility(View.VISIBLE);

			setCancelable(true);

			onSyncingSuccess();
		}
	}


	protected void setUISnapshots(KPHSyncSnapshots snapshots) {
		if (snapshots == null)
			return;

		if (TextUtils.isEmpty(snapshots.syncstatus)) {
			resultStatus.setText(getSafeContext().getString(R.string.sync_well_done));
		} else {
			resultStatus.setText(snapshots.syncstatus);
		}

		resultPackets.setVisibility(View.GONE);

		if (snapshots.syncCompleteScreens != null && snapshots.syncCompleteScreens.size() > 0) {
			KPHSyncSnapshots.SyncScreen screen = snapshots.syncCompleteScreens.get(0);

			Logger.log(TAG, "setUISnapshots : There are sync screens, showing now. id:%d, title:%s, text:%s, button caption:%s",
					screen.screenId, screen.content.h1, screen.content.text, screen.content.buttonText);

			if (screen.screenId == 1) {
				resultStatus.setText(screen.content.h1);
				resultSummary.setText(screen.content.text);
				btnGoto.setText(screen.content.buttonText);
			}
		} else if (snapshots.disablingMission) {
			KPHMissionInformation missionInformation = KPHMissionService.sharedInstance().getMissionInformationById(snapshots.missionId);

			String strSummary;
			if (missionInformation != null) {
				strSummary = missionInformation.missionCompleteText();
			} else {
				strSummary = getSafeContext().getString(R.string.mission_completed);
			}

			resultStatus.setText(getSafeContext().getString(R.string.sync_mission_completed));
			Drawable drawable = missionInformation.getCompleteDrawable();
			if (drawable != null) {
				populateCompletedDrawable(drawable);
			}

			resultSummary.setText(strSummary);
		} else {
			// set RUTF
			int packetUnlocked = snapshots.packetsUnlocked;
			// set unlocked delight
			int delightEarned = snapshots.delightsUnlocked == null ? 0 : snapshots.delightsUnlocked.size();

			resultPackets.setVisibility(View.VISIBLE);

			populateUnlockedDelight1(packetUnlocked, snapshots.delightsUnlocked);
			populateUnlockedDelight2(packetUnlocked, snapshots.delightsUnlocked);
			// set summary text

			String strPacket = null, strDelight = null;
			if (packetUnlocked > 1) {
				strPacket = String.format(getSafeContext().getString(R.string.d_new_packets), packetUnlocked);
			} else if (packetUnlocked > 0) {
				strPacket = getSafeContext().getString(R.string.one_new_packet);
			}

			if (delightEarned > 1) {
				strDelight = String.format(getSafeContext().getString(R.string.d_new_souvenirs), delightEarned);
			} else if (delightEarned > 0) {
				strDelight = getSafeContext().getString(R.string.one_new_souvenir);
			}

			String strSummary;
			if (strPacket != null && strDelight != null) {
				strSummary = getSafeContext().getString(R.string.you_unlocked)
						+ " "
						+ strPacket
						+ " "
						+ getSafeContext().getString(R.string.and)
						+ " "
						+ strDelight
						+ ".";
			} else if (strPacket != null) {
				strSummary = getSafeContext().getString(R.string.you_unlocked)
						+ " "
						+ strPacket
						+ ".";
			} else if (strDelight != null) {
				strSummary = getSafeContext().getString(R.string.you_earned)
						+ " "
						+ strDelight
						+ ".";
			} else {
				if (snapshots.newBuzzPoints == 1) {
					strSummary = getSafeContext().getString(R.string.you_earned_1_pp_nice_work);
				} else if (snapshots.newBuzzPoints > 1) {
					strSummary = String.format(getSafeContext().getString(R.string.you_earned_d_pp_nice_work), snapshots.newBuzzPoints);
				} else
					strSummary = getSafeContext().getString(R.string.sync_no_activities);
			}

			resultSummary.setText(strSummary);
			configureGotoButton();
		}
	}


	private void populateCompletedDrawable(Drawable drawable) {
		resultPackets.setVisibility(View.VISIBLE);

		LinearLayout llDelight2 = (LinearLayout) contentView.findViewById(R.id.llDelight2);
		llDelight2.setVisibility(View.GONE);

		delightPlaceholder1.setVisibility(View.VISIBLE);
		delightPlaceholder1.setCustomImage(drawable);
		delightPlaceholder2.setVisibility(View.GONE);
		delightPlaceholder3.setVisibility(View.GONE);

		earnedRUTF.setVisibility(View.GONE);
	}


	private void populateUnlockedDelight1(int packetUnlocked, List<KPHDelight> unlockedDelight) {
		int delightEarned = 0;
		if (unlockedDelight != null)
			delightEarned = unlockedDelight.size();

		delightPlaceholder1.setVisibility(View.GONE);
		delightPlaceholder2.setVisibility(View.GONE);
		delightPlaceholder3.setVisibility(View.GONE);

		if (packetUnlocked <= 0) {
			if (delightEarned <= 0)
				resultPackets.setVisibility(View.GONE);
			else
				earnedRUTF.setVisibility(View.GONE);
		} else {
			earnedRUTF.setBadgeCount(packetUnlocked);
		}

		if (delightEarned <= 0)
			return;

		showDelight(delightPlaceholder1, 0);
		showDelight(delightPlaceholder2, 1);

		if (packetUnlocked <= 0) {
			showDelight(delightPlaceholder3, 2);
		}
	}


	private void populateUnlockedDelight2(int packetUnlocked, List<KPHDelight> unlockedDelight) {
		int delightEarned = 0;
		if (unlockedDelight != null) {
			delightEarned = unlockedDelight.size();
		}

		LinearLayout llDelight2 = (LinearLayout) contentView.findViewById(R.id.llDelight2);

		int delight_index;
		if (packetUnlocked > 0)
			delight_index = 2;
		else
			delight_index = 3;

		if (delightEarned <= delight_index) {
			llDelight2.setVisibility(View.GONE);
			return;
		}
		llDelight2.setVisibility(View.VISIBLE);

		showDelight((KPHImageTextButton) resultPackets.findViewById(R.id.unlockedDelight4), delight_index);
		showDelight((KPHImageTextButton) resultPackets.findViewById(R.id.unlockedDelight5), ++delight_index);
		showDelight((KPHImageTextButton) resultPackets.findViewById(R.id.unlockedDelight6), ++delight_index);
	}


	private void showDelight(KPHImageTextButton delPlaceholder, int delight_index) {
		KPHDelight delight = null;
		Drawable drawable = null;
		try {
			delight = snapshots.delightsUnlocked.get(delight_index);
			if (delight != null) {
				KPHDelightInformation delightInfo = KPHMissionService.sharedInstance().getDelightInformationById(delight.getId());
				if (delightInfo != null) {
					drawable = delightInfo.getImageDrawable();
				}

				if (drawable == null) {
					// set placeholder image
					drawable = UIManager.sharedInstance().getImageDrawable(R.drawable.souvenir_placeholder);
				}
				delPlaceholder.setVisibility(View.VISIBLE);
				delPlaceholder.setCustomImage(drawable);
			} else {
				delPlaceholder.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			delPlaceholder.setVisibility(View.GONE);
		}
	}


	private void configureGotoButton() {
		if (snapshots != null && snapshots.unlockedCheerIDs != null && snapshots.unlockedCheerIDs != null && snapshots.unlockedCheerIDs.size() > 0) {
			btnGoto.setText(R.string.button_next);
		}
	}


	private void populateSyncingStatus(CBBandSync.Report_Type type, Date date) {
		if (type == Sync_Start) {
			String strDate = new OSDate(date).toStringWithFormat("LLL dd");
			String str = getSafeContext().getString(R.string.syncing_activity_from) + " " + strDate + getOrdinalSuffix(date) + "...";
			syncMark.setStatus(str);
		}
	}


	public static String getOrdinalSuffix(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hunRem = day % 100, tenRem = day % 10;

		if (hunRem - tenRem == 10) {
			return "th";
		}

		switch (tenRem) {
			case 1:
				return "st";
			case 2:
				return "nd";
			case 3:
				return "rd";
			default:
				return "th";
		}
	}

	@Override
	public void startAction() {
		KPHUserService userService = KPHUserService.sharedInstance();
		KPHUserData userData = userService.getUserData();

		if (userData != null) {
			// fetch current user Data
			curTracker = userService.currentTracker();
			userName = userData.getHandle();
			avatarId = userData.getAvatarId();
		} else {
			Logger.error(TAG, "startAction : There isn't any user info");
		}

		if (TextUtils.isEmpty(userName)) {
			userName = "";
		}

		if (TextUtils.isEmpty(avatarId))
			avatarId = getSafeContext().getString(R.string.basketball);

		Drawable avatar = userService.getAvatarDrawable(avatarId);
		if (avatar != null) {
			syncMark.setAvatar(avatar);
		}

		syncMark.setName(userName);
		syncMark.setStatus(getSafeContext().getString(R.string.status_syncing));

		syncMark.startAnimation(trackerType);

		// set initialize layout
		syncMark.setVisibility(View.VISIBLE);
		llBody.setVisibility(View.GONE);
		btnGoto.setVisibility(View.GONE);

		setCancelable(false);

		if (!EventManager.sharedInstance().isRegistered(this)) {
			EventManager.sharedInstance().register(this);
		}

		startProcessing();
	}


	private void showSyncNoDataFragment() {
		SyncNoDataFragment fragment = new SyncNoDataFragment();
		fragment.setData(userName, avatarId);
		getParentActivity().showNewFragment(fragment);
	}


	private void onClickedReadMore() {
		// Go to read more
		dismiss();
		showSyncNoDataFragment();
	}
}
