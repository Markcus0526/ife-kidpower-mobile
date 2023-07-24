package org.unicefkidpower.kid_power.View.Activities.Main.Passport;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.kid_power.Misc.AlertDialogHelper;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Misc.onBandActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble.BleManager;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamStartMission;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.EventManager;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.SEvent;
import org.unicefkidpower.kid_power.Model.MicroService.KPHAnalyticsService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHMissionService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.Structure.KPHTracker;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMission;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMissionStats;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.CustomControls.CustomViewPager;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHSyncMark;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperNormalSizeDialogFragment;


/**
 * Created by Dayong Li on 9/16/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class StartMissionFragment extends SuperNormalSizeDialogFragment {
	private static final String			TAG							= "StartMissionFragment";

	protected String 					eid_Step1ForStartTracker 	= "stepForStartWithTracker";
	protected String					eid_Step2ForEnableMission	= "stepForEnableMission";
	protected String					eid_Step3Final				= "stepForFinal";

	protected static final int			BACKEND_RETRY_COUNT			= 5;
	protected static final int			BLUETOOTH_RETRY_COUNT		= 3;

	protected View						contentView					= null;

	protected LinearLayout				contentLayout				= null;
	protected RelativeLayout			tutorialContainerLayout		= null;
	protected CustomViewPager			viewPager					= null;

	protected KPHSyncMark				startMark					= null;
	protected LinearLayout				resultLayout				= null;

	// for llPage2
	protected KPHTextView				resultStatus				= null;
	protected KPHTextView				resultSummary				= null;
	protected KPHButton					btnGoto						= null;

	protected Boolean					isProcessing				= false;

	protected KPHUserService			userService					= null;
	protected KPHMissionService			missionService				= null;
	protected KPHTracker				curTracker					= null;
	protected KPHUserMissionStats		syncMission					= null;

	protected String					userName					= null;
	protected int						retryCount					= BLUETOOTH_RETRY_COUNT;
	protected int						trackerType					= KPHUserService.TRACKER_TYPE_NONE;


	public void setSyncingMission(KPHUserMissionStats syncMission) {
		this.syncMission = syncMission;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		userService = KPHUserService.sharedInstance();
		missionService = KPHMissionService.sharedInstance();

		if (contentView == null) {
			contentView = super.onCreateView(inflater, container, savedInstanceState);

			contentLayout = (LinearLayout) contentView.findViewById(R.id.llContent);
			tutorialContainerLayout = (RelativeLayout) contentView.findViewById(R.id.llTutorial);
			viewPager = (CustomViewPager) contentView.findViewById(R.id.vwPager);

			startMark = (KPHSyncMark) contentView.findViewById(R.id.startMark);
			resultLayout = (LinearLayout) contentView.findViewById(R.id.llResult);

			resultStatus = (KPHTextView) resultLayout.findViewById(R.id.resultStatus);
			resultSummary = (KPHTextView) resultLayout.findViewById(R.id.resultSummary);
			btnGoto = (KPHButton) resultLayout.findViewById(R.id.btnGoto);

			btnGoto.setText(getSafeContext().getString(R.string.goto_mission));
			btnGoto.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickedGotoMission();
				}
			});

			// set initialize layout
			int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3,
					getResources().getDisplayMetrics());
			contentLayout.setVisibility(View.VISIBLE);
			tutorialContainerLayout.setVisibility(View.GONE);
			viewPager.setAdapter(new TutorialAdapter(getChildFragmentManager()));
			viewPager.setPagingEnabled(true);
			viewPager.setPageMargin(pageMargin);
			viewPager.setCurrentItem(0);

			startMark.setVisibility(View.VISIBLE);
			resultLayout.setVisibility(View.GONE);
		}

		return contentView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_band_start_mission;
	}

	@Override
	public void startAction() {
		String avatar_name = null;
		KPHUserData userData = userService.getUserData();
		if (userData != null) {
			userName = userData.getHandle();
			avatar_name = userData.getAvatarId();
		}

		if (TextUtils.isEmpty(userName))
			userName = "";

		if (avatar_name == null || avatar_name.isEmpty())
			avatar_name = "basketball";

		Drawable avatar = userService.getAvatarDrawable(avatar_name);
		startMark.setAvatar(avatar);
		startMark.setName("Syncing " + userName);

		curTracker = userService.currentTracker();
		trackerType = KPHUserService.sharedInstance().loadCurrentTrackerType();
		startMark.startAnimation(trackerType);

		if (!EventManager.sharedInstance().isRegistered(this)) {
			EventManager.sharedInstance().register(this);
		}

		startProcessing();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BleManager.REQUEST_ENABLE_BLE) {
			boolean isBleEnabled = BleManager.sharedInstance().isBleEnabled();

			if (resultCode == Activity.RESULT_CANCELED || isBleEnabled == false) {
				AlertDialogHelper.showAlertDialog("Bluetooth Error", "Cannot enable ble devices", getParentActivity(), null);
				return;
			} else if (resultCode == Activity.RESULT_OK) {
				startProcessing();
			}
		} else if (requestCode == MainActivity.REQCODE_SELECT_GOOGLEFIT_DIRECTLY) {
			if (resultCode == Activity.RESULT_OK) {
				EventManager.sharedInstance().post(eid_Step2ForEnableMission, curTracker, eid_Step2ForEnableMission);
			} else {
				Logger.error(TAG, "Step1 : failed");
				onSyncingError(-3, "Error occurred", getSafeContext().getString(R.string.sync_googlefit_connection_fail));
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}


	@Override
	public void onDetach() {
		super.onDetach();

		if (EventManager.sharedInstance().isRegistered(this)) {
			EventManager.sharedInstance().unregister(this);
		}

		if (isProcessing) {
			if (KPHUserService.sharedInstance().loadCurrentTrackerType() == KPHUserService.TRACKER_TYPE_KIDPOWERBAND) {
				missionService.stopForBand(getSafeContext());
			}
		}

	}


	protected void startProcessing() {
		synchronized (isProcessing) {
			if (syncMission == null || curTracker == null) {
				onSyncingError(-1, "Error", "Started User Mission is not exist");
				return;
			}

			if (isProcessing)
				return;

			isProcessing = true;

			if (trackerType == KPHUserService.TRACKER_TYPE_GOOGLEFIT) {
				EventManager.sharedInstance().post(eid_Step1ForStartTracker, curTracker, curTracker.getDeviceCode());
			} else {
				retryCount = BLUETOOTH_RETRY_COUNT;

				boolean isBleEnabled = BleManager.sharedInstance().isBleEnabled();
				if (!isBleEnabled) {
					BleManager.sharedInstance().enableAdapter();
				} else {
					EventManager.sharedInstance().post(eid_Step1ForStartTracker, curTracker, curTracker.getDeviceCode());
				}
			}

			KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_TRACKER_SYNC_START);
		}
	}


	private void onClickedGotoMission() {
		if (syncMission == null) {
			dismiss();
			return;
		}

		if (btnGoto.getText().equals(getSafeContext().getString(R.string.goto_mission))) {
			Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_GOTO_USERMISSION);
			intent.putExtra("MissionId", syncMission.getMissionId());
			LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
			dismiss();
		} else {
			dismiss();
		}
	}


	protected void updateUI(String step) {
		if (step.equals(eid_Step1ForStartTracker) || step.equals(eid_Step2ForEnableMission)) {
		} else if (step.equals(eid_Step3Final)) {
			startMark.setName(userName + " " + getContext().getString(R.string.synced) + " ");
			startMark.stopAnimation();

			resultSummary.setVisibility(View.GONE);
			resultLayout.setVisibility(View.VISIBLE);
			setCancelable(true);

			if (missionService.shouldShowTutorial()) {
				missionService.writeSeenTutorialTime();

				contentLayout.setVisibility(View.GONE);
				tutorialContainerLayout.setVisibility(View.VISIBLE);
			}

			synchronized (isProcessing) {
				isProcessing = false;
			}
		}
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent event) {
		try {
			if (EventManager.isEvent(event, BleManager.kBLEManagerStateChanged)) {
				Integer obj = (Integer) event.object;
				int state = obj.intValue();

				if (state == BluetoothAdapter.STATE_ON && BleManager.sharedInstance().isBleEnabled()) {
					//searching band now
					startProcessing();
				} else {
					//
				}
			} else if (EventManager.isEvent(event, eid_Step1ForStartTracker)) {
				updateUI(event.name);

				Logger.log(TAG, "Step1 : enabling mission mode with Band");


				if (trackerType == KPHUserService.TRACKER_TYPE_GOOGLEFIT) {
					Logger.log(TAG, "Step1 : completed connecting google fit");
					EventManager.sharedInstance().post(eid_Step2ForEnableMission, curTracker, eid_Step2ForEnableMission);
				} else {
					CBParamStartMission param = CBParamStartMission.makeParams(getSafeContext(), curTracker.getDeviceCode(), syncMission.getMissionGoal());
					missionService.startMissionForBand(param, new onBandActionListener() {
						@Override
						public void completed(Object object) {
							// Completed link band
							retryCount = BACKEND_RETRY_COUNT;
							Logger.log(TAG, "Step1 : completed connecting band");
							EventManager.sharedInstance().post(eid_Step2ForEnableMission, curTracker, eid_Step2ForEnableMission);
						}

						@Override
						public void failed(int code, String message) {
							if (code == BleManager.BT_ERROR_NO_ANY_DEVICES) {
								Logger.error(TAG, "Step1 : no any devices");
								onSyncingError(-3, "", getSafeContext().getString(R.string.sync_not_found_band));
							} else if (code == BleManager.BT_ERROR_NOT_FOUND) {
								Logger.error(TAG, "Step1 : not found the band");
								onSyncingError(-3, "", getSafeContext().getString(R.string.sync_not_found_band));
							} else if (code == BleManager.BT_ERROR_NO_SERVICE) {
								Logger.error(TAG, "Step1 : not enabled bluetooth smart service");
								onSyncingError(-3, "", getSafeContext().getString(R.string.sync_band_no_service));
							} else if (retryCount >= 0) {
								// try again
								Logger.log(TAG, "Step1 : retry, remain count" + retryCount);
								retryCount--;
								EventManager.sharedInstance().post(eid_Step1ForStartTracker, curTracker, curTracker.getDeviceId());
							} else {
								// failed, goto error state
								Logger.error(TAG, "Step1 : failed");
								onSyncingError(-3, "Error occurred", "Failed to set mission mode.");
							}
						}

						@Override
						public void reportStatus(Object param) {
						}
					});
				}
			} else if (EventManager.isEvent(event, eid_Step2ForEnableMission)) {
				updateUI(event.name);

				Logger.log(TAG, "Step2 : starting the mission");

				missionService.startMission(syncMission.getMissionId(), new onActionListener() {
					@Override
					public void completed(Object object) {
						Logger.log(TAG, "Step2 : completed starting mission");

						KPHUserMission userMission = (KPHUserMission) object;

						KPHUserMissionStats newStatus = missionService.userMissionStatsFromUserMission(userMission);
						missionService.updateUserMissionStatus(newStatus);

						EventManager.sharedInstance().post(eid_Step3Final, userMission, "mission started");

						// Log purchase
						KPHAnalyticsService.sharedInstance().logUnlockMission(syncMission.getMissionId());
					}

					@Override
					public void failed(int code, String message) {
						if (code == -2 && retryCount >= 0) {
							// if network error, try again
							Logger.log(TAG, "Step2 : retry, remain times : " + retryCount);
							retryCount--;
							EventManager.sharedInstance().post(eid_Step2ForEnableMission, curTracker, eid_Step2ForEnableMission);
						} else if (retryCount < 0) {
							Logger.error(TAG, "Step2 : failed staring mission");
							onSyncingError(-1, "Error occurred", "Network error");
						} else {
							// other case maybe error
							Logger.error(TAG, "Step2 : failed with unknown reason");
							onSyncingError(-1, "Error occurred", message);
						}
					}
				});
			} else if (EventManager.isEvent(event, eid_Step3Final)) {
				updateUI(eid_Step3Final);
				Logger.log(TAG, "Step3 : wow, you did it, success starting mission");
				Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_USER_MISSION_LIST_CHANGED);
				LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
			}
		} catch (Exception except) {
			Logger.error(TAG, "Step3 : exception occurred");
			except.printStackTrace();
			onSyncingError(-1, "Error occurred", except.getMessage());
		}
	}

	protected void onSyncingError(int code, String status, String error) {
		startMark.setName(" Not synced");
		startMark.stopAnimation();

		resultLayout.setVisibility(View.VISIBLE);
		resultStatus.setText(status);
		resultSummary.setVisibility(View.VISIBLE);
		resultSummary.setText(error);

		btnGoto.setText(getSafeContext().getString(R.string.my_passport));
	}

	public class TutorialAdapter extends FragmentPagerAdapter {
		private static final int pageCount = 5;

		private final int texts[] = {R.string.slide_mission_help_1,
				R.string.slide_mission_help_2,
				R.string.slide_mission_help_3,
				R.string.slide_mission_help_4,
				R.string.slide_mission_help_5};

		private final int images[] = {
				R.drawable.slidethrough_mission_1,
				R.drawable.slidethrough_mission_2,
				R.drawable.slidethrough_mission_3,
				R.drawable.slidethrough_mission_4,
				R.drawable.slidethrough_mission_5
		};

		TutorialFragment page1;
		TutorialFragment page2;
		TutorialFragment page3;
		TutorialFragment page4;
		TutorialFragment page5;

		public TutorialAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return pageCount;
		}

		@Override
		public Fragment getItem(int position) {

			switch (position) {
				case 0:
					if (page1 == null) {
						page1 = createView(position);
					}
					return page1;
				case 1:
					if (page2 == null) {
						page2 = createView(position);
					}
					return page2;
				case 2:
					if (page3 == null) {
						page3 = createView(position);
					}
					return page3;
				case 3:
					if (page4 == null) {
						page4 = createView(position);
					}
					return page4;
				case 4:
				default:
					if (page5 == null) {
						page5 = createView(position);
					}
					return page5;
			}
		}

		@Override
		public int getItemPosition(Object object) {
			// TODO Auto-generated method stub
			return POSITION_NONE;
		}

		private TutorialFragment createView(int position) {
			TutorialFragment fragment = new TutorialFragment();

			fragment.setPageNumber(position);
			fragment.setOnNextListener(nextListener);

			if (position != pageCount - 1)
				fragment.setContents(images[position], texts[position], R.string.button_next);
			else
				fragment.setContents(images[position], texts[position], R.string.button_started);

			return fragment;
		}

		private TutorialFragment.OnNextListener nextListener = new TutorialFragment.OnNextListener() {
			@Override
			public void onNext(int page) {
				if (page == 4) {
					Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_GOTO_USERMISSION);
					intent.putExtra("MissionId", syncMission.getMissionId());
					LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
					dismiss();
				} else {
					viewPager.setCurrentItem(page + 1);
				}
			}
		};

	}

}
