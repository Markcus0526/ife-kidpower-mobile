package org.unicefkidpower.kid_power.View.Activities.Main.Passport;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.KPHNotificationUtil;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.OSDate;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble.BleManager;
import org.unicefkidpower.kid_power.Model.MicroService.GoogleFit.GoogleFitService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHAnalyticsService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHMissionService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.Structure.KPHDelightInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHDelightUnlocked;
import org.unicefkidpower.kid_power.Model.Structure.KPHMission;
import org.unicefkidpower.kid_power.Model.Structure.KPHMissionInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHTracker;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMissionStats;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserStats;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserTravelLog;
import org.unicefkidpower.kid_power.Model.Structure.MissionLog;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.Activities.Main.More.PermissionDialogFragment;
import org.unicefkidpower.kid_power.View.Activities.Main.TravelLog.TravelLogMainFragment;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.SelectDeviceFragment;
import org.unicefkidpower.kid_power.View.Adapters.FamilyAccountSwitchListAdapter;
import org.unicefkidpower.kid_power.View.Adapters.MissionLogAdapter;
import org.unicefkidpower.kid_power.View.Adapters.PassportListAdapter;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHFamilyAccountSwitchButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHFamilyAccountSwitchView;
import org.unicefkidpower.kid_power.View.CustomControls.KPHGradientRecyclerView;
import org.unicefkidpower.kid_power.View.CustomControls.KPHImageLabelTextButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHImageTextButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.CustomControls.ProgressBarView;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;
import org.unicefkidpower.kid_power.View.Super.SuperDialogFragment;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.client.Response;


/**
 * Created by Ruifeng Shi on 9/4/2015.
 */
public class PassportMainFragment extends SuperFragment {
	/***********************************************************************************************
	 * Constants
	 */
	protected static final String TAG = "PassportMainFragment";

	private static final String TAG_ACTIVE_MISSION_FRAGMENT = "TAG_ACTIVE_MISSION_FRAGMENT";
	private static final String TAG_NEW_MISSIONS_FRAGMENT = "TAG_NEW_MISSIONS_FRAGMENT";
	private static final String TAG_COMPLETE_MISSIONS_FRAGMENT = "TAG_COMPLETE_MISSIONS_FRAGMENT";

	private final int MISSION_CLICK_MINIMUM_INTERVAL = 1000;            // 1 second
	private final int SYNC_CLICK_MINIMUM_INTERVAL = 500;

	private final int LOAD_CHAIN_1_USERDATA = 1;
	private final int LOAD_CHAIN_2_MISSION_DETAILED_LOG = 2;
	private final int LOAD_CHAIN_3_ALL_SUPPORTED_MISSION = 3;
	private final int LOAD_CHAIN_4_ALL_USER_MISSIONS = 4;
	private final int LOAD_CHAIN_5_USER_MISSION_PROGRESS_STATS = 5;
	private final int LOAD_CHAIN_6_CURRENT_TRACKER = 6;
	private final int LOAD_CHAIN_7_GOOGLE_FIT_INFORMATION = 7;
	/**
	 * End of 'Constants'
	 **********************************************************************************************/


	/***********************************************************************************************
	 * UI Elements
	 */
	private View contentView = null;
	private ImageView smallAvatarImageView = null;

	// List Header View. Contains main informations and tab buttons.
	private LinearLayout listHeaderView = null;
	// child controls of 'listHeaderView'
	private ImageView mainAvatarView = null;
	private KPHTextView deviceNotMatchLayout = null;
	private KPHTextView noCorrespondingMissionsTextView = null;

	private HorizontalScrollView mainInfoScrollView = null;
	private LinearLayout mainInfoContentLayout = null;
	private boolean initializedWidth = false;
	private KPHImageLabelTextButton packetsTextView = null;
	private KPHImageLabelTextButton missionsCompletedTextView = null;
	private KPHImageLabelTextButton powerPointsTextView = null;

	private KPHTextView usernameTextView = null;
	private KPHImageTextButton startButton = null;
	private LinearLayout missionSummaryLayout = null;
	// child controls of 'missionSummaryLayout'
	private ImageView completeBackgroundImageView = null;
	private ImageView missionCompleteImageView = null;
	private TextView timeTextView = null;
	private TextView missionStatusTextView = null;
	private TextView titleTextView = null;
	private TextView missionCompleteNameTextView = null;
	private KPHImageTextButton summaryPacketButton = null;
	private KPHImageTextButton summaryPowerPointButton = null;
	private RelativeLayout placeLayout = null;
	private ProgressBarView missionPercentProgressView = null;
	private View cellDividerView = null;
	private LinearLayout delightListLayout = null;
	// end of "child controls of 'missionSummaryLayout'"
	// end of "child controls of 'listHeaderView'"


	private KPHGradientRecyclerView missionsListView = null;

	private KPHButton activeTabButton = null;
	private KPHButton newTabButton = null;
	private KPHButton completeTabButton = null;

	private KPHButton creditsButton = null;                // Should create dynamically

	private KPHFamilyAccountSwitchButton switchAccountButton = null;

	private PopupWindow familyAccountSwitchWindow = null;
	private int familyAccountWindowWidth = 0;
	private KPHFamilyAccountSwitchView familyAccountSwitchView = null;
	private FamilyAccountSwitchListAdapter familyAccountSwitchListAdapter = null;
	private OnAccountSwitchButtonListener accountSwitchButtonListener = new OnAccountSwitchButtonListener();
	/**
	 * End of 'UI Elements'
	 **********************************************************************************************/


	/***********************************************************************************************
	 * Adapters
	 */
	private MissionLogAdapter travelLogsAdapter = null;            // For active missions
	private List<MissionLog> travelLogs = new ArrayList<>();

	private PassportListAdapter newPassportListAdapter = null;            // For new missions
	private ArrayList<KPHMission> newMissionArray = new ArrayList<>();

	private PassportListAdapter completePassportListAdapter = null;            // For complete missions
	private ArrayList<KPHMission> completeMissionArray = new ArrayList<>();
	/*
	 * End of "Adapters"
	 **********************************************************************************************/


	/***********************************************************************************************
	 * Variables
	 */
	private long missionLastClickTimestamp = 0;
	private long syncLastClickTimestamp = 0;

	private BandAction bandAction = BandAction.Band_Never;
	private String tabCurrentFragmentTag = "";
	private boolean isAutoStartMission = false;

	private int userIdForThisDevice = 0;

	private final int RETRY_COUNT_LIMIT = 5;
	private int retryCount = RETRY_COUNT_LIMIT;


	/**
	 * Variable to indicate if load chain is on progress or not.
	 *
	 * @Warning DO NOT directly access this variable.
	 * @see {@link #setLoadChainFlag()}, {@link #clearLoadChainFlag()}, {@link #isLoadChainRunning()}
	 */
	private Boolean loadChainRunning = false;
	/*
	 * End of Variables
	 **********************************************************************************************/


	enum BandAction {
		Band_Never,
		Band_Sync,
		Band_StartMission,
	}


	/**
	 * onCreateView : Initialize view controls and start refresh information
	 *
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 * @return
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		// Init list header view
		listHeaderView = (LinearLayout) getParentActivity().getLayoutInflater().inflate(R.layout.item_passport_header, null);
		{
			deviceNotMatchLayout = (KPHTextView) listHeaderView.findViewById(R.id.txtBandHasOrNot);
			{
				deviceNotMatchLayout.setMovementMethod(LinkMovementMethod.getInstance());
				deviceNotMatchLayout.setHighlightColor(Color.TRANSPARENT);
				deviceNotMatchLayout.setVisibility(View.GONE);
			}

			noCorrespondingMissionsTextView = (KPHTextView) listHeaderView.findViewById(R.id.txt_no_corresponding_missions);
			noCorrespondingMissionsTextView.setVisibility(View.GONE);

			mainInfoScrollView = (HorizontalScrollView) listHeaderView.findViewById(R.id.main_info_horscrollview);
			mainInfoContentLayout = (LinearLayout) listHeaderView.findViewById(R.id.main_info_horscrollview_content_layout);
			mainInfoContentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					if (initializedWidth)
						return;

					if (mainInfoScrollView.getWidth() > 0 && mainInfoContentLayout.getWidth() > 0) {
						boolean needAutoResize = false;
						if (mainInfoScrollView.getWidth() < mainInfoContentLayout.getWidth()) {
							needAutoResize = true;
						}

						int totalWidth = mainInfoScrollView.getWidth();
						ViewGroup.LayoutParams mainParams = mainInfoContentLayout.getLayoutParams();
						mainParams.width = totalWidth;

						LinearLayout.LayoutParams params1, params2, params3;

						params1 = (LinearLayout.LayoutParams) packetsTextView.getLayoutParams();
						params2 = (LinearLayout.LayoutParams) missionsCompletedTextView.getLayoutParams();
						params3 = (LinearLayout.LayoutParams) powerPointsTextView.getLayoutParams();

						int weight1 = params1.width, weight2 = params2.width, weight3 = params3.width;
						int weightSum = weight1 + weight2 + weight3;

						params1.width = totalWidth * weight1 / weightSum;
						packetsTextView.setLayoutParams(params1);

						params2.width = totalWidth * weight2 / weightSum;
						missionsCompletedTextView.setLayoutParams(params2);

						params3.width = totalWidth * weight3 / weightSum;
						powerPointsTextView.setLayoutParams(params3);

						if (needAutoResize) {
							packetsTextView.autoFitSize(params1.width);
							missionsCompletedTextView.autoFitSize(params2.width);
							powerPointsTextView.autoFitSize(params3.width);
						}

						initializedWidth = true;
					}
				}
			});

			packetsTextView = (KPHImageLabelTextButton) listHeaderView.findViewById(R.id.ivPacket);
			missionsCompletedTextView = (KPHImageLabelTextButton) listHeaderView.findViewById(R.id.ivMission);
			powerPointsTextView = (KPHImageLabelTextButton) listHeaderView.findViewById(R.id.ivPowerpoint);

			usernameTextView = (KPHTextView) listHeaderView.findViewById(R.id.txtUsername);

			startButton = (KPHImageTextButton) listHeaderView.findViewById(R.id.btnLinkBand);
			startButton.setGravity(Gravity.CENTER);
			startButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickedStart();
				}
			});
			startButton.setVisibility(View.GONE);


			missionSummaryLayout = (LinearLayout) listHeaderView.findViewById(R.id.layout_mission_summary);
			{
				completeBackgroundImageView = (ImageView) missionSummaryLayout.findViewById(R.id.iv_country_shape);
				missionCompleteImageView = (ImageView) missionSummaryLayout.findViewById(R.id.img_MissionComplete);
				timeTextView = (KPHTextView) missionSummaryLayout.findViewById(R.id.txt_CompletionTime);
				missionStatusTextView = (KPHTextView) missionSummaryLayout.findViewById(R.id.txt_Completion_Status);
				titleTextView = (KPHTextView) missionSummaryLayout.findViewById(R.id.txt_Title);
				missionCompleteNameTextView = (KPHTextView) missionSummaryLayout.findViewById(R.id.txt_mission_name);
				summaryPacketButton = (KPHImageTextButton) missionSummaryLayout.findViewById(R.id.ivPacket_Detail);
				summaryPowerPointButton = (KPHImageTextButton) missionSummaryLayout.findViewById(R.id.ivPowerpoint_Detail);
				placeLayout = (RelativeLayout) missionSummaryLayout.findViewById(R.id.layout_place);

				missionPercentProgressView = (ProgressBarView) missionSummaryLayout.findViewById(R.id.mission_progress);
				missionPercentProgressView.setVisibility(View.VISIBLE);

				cellDividerView = missionSummaryLayout.findViewById(R.id.cell_divider);
				cellDividerView.setVisibility(View.GONE);

				delightListLayout = (LinearLayout) missionSummaryLayout.findViewById(R.id.layout_delight_list);
			}

			mainAvatarView = (ImageView) listHeaderView.findViewById(R.id.ivAvatar);
		}


		smallAvatarImageView = (ImageView) contentView.findViewById(R.id.ivSmallAvatar);

		missionsListView = (KPHGradientRecyclerView) contentView.findViewById(R.id.main_listview);
		missionsListView.setHeaderView(listHeaderView);
		missionsListView.setLayoutManager(new LinearLayoutManager(getSafeContext(), LinearLayoutManager.VERTICAL, false));

		activeTabButton = (KPHButton) listHeaderView.findViewById(R.id.btn_active_tab);
		activeTabButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchMissionTab(TAG_ACTIVE_MISSION_FRAGMENT, false);
			}
		});

		newTabButton = (KPHButton) listHeaderView.findViewById(R.id.btn_new_tab);
		newTabButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchMissionTab(TAG_NEW_MISSIONS_FRAGMENT, false);
			}
		});

		completeTabButton = (KPHButton) listHeaderView.findViewById(R.id.btn_complete_tab);
		completeTabButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchMissionTab(TAG_COMPLETE_MISSIONS_FRAGMENT, false);
			}
		});

		creditsButton = (KPHButton) contentView.findViewById(R.id.btn_credits);
		creditsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedCredits();
			}
		});

		switchAccountButton = (KPHFamilyAccountSwitchButton) contentView.findViewById(R.id.btn_family_account_switcher);
		switchAccountButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (familyAccountSwitchWindow == null)
					return;

				if (accountSwitchButtonListener != null)
					accountSwitchButtonListener.onClick(v);
			}
		});

		if (intentFilter == null) {
			intentFilter = new IntentFilter();

			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_USER_MISSION_LIST_CHANGED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_USER_DATA_UPDATED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_AVATAR_UPDATED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_UPDATE_PASSPORT_UI);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_STARTING_USERMISSION);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_GOTO_USERMISSION);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_CREDIT_BALANCE_UPDATED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_LOCATION_PERMISSION_SUCCESS);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_LOCATION_PERMISSION_FAILED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_TRAVEL_LOG_UPDATED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_TRACKER_SELECTED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_DEVICE_CHANGED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_TRAVEL_LOG_READ_1);

			LocalBroadcastManager.getInstance(getSafeContext()).registerReceiver(receiver, intentFilter);
		}

		// Init adapters
		travelLogsAdapter = new MissionLogAdapter(getSafeContext(), travelLogs, getSafeContext().getString(R.string.passport));
		travelLogsAdapter.setOnMissionLogItemClickListener(new MissionLogAdapter.OnMissionLogItemClickListener() {
			@Override
			public void onClickMissionLogItem(MissionLog missionLog) {
				retryCount = RETRY_COUNT_LIMIT;
				onMarkedMissionLog(missionLog);
			}
		});
		travelLogsAdapter.setHeaderView(listHeaderView);

		newPassportListAdapter = new PassportListAdapter(getSafeContext(), newMissionArray);
		newPassportListAdapter.setHeaderView(listHeaderView);
		newPassportListAdapter.setOnItemClickedListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = (Integer) v.getTag() - 1;
				onMissionClicked(newMissionArray.get(position));
			}
		});

		completePassportListAdapter = new PassportListAdapter(getSafeContext(), completeMissionArray);
		completePassportListAdapter.setHeaderView(listHeaderView);
		completePassportListAdapter.setOnItemClickedListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = (Integer) v.getTag() - 1;
				onMissionClicked(completeMissionArray.get(position));
			}
		});

		reloadAllInformation();

		return contentView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		LocalBroadcastManager.getInstance(getSafeContext()).unregisterReceiver(receiver);
		intentFilter = null;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_passport;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);

		if (isLoadChainRunning()) {
			Logger.log(TAG, "isLoadChainRunning");
			if (!hidden && getContext() != null) {
				showProgressDialog();
			}
		}
	}

	public void setData(boolean isAutoStart) {
		isAutoStartMission = isAutoStart;
	}


	/**
	 * Method to reload whole page. This should be called only once when the page is firstly created.
	 */
	public void reloadAllInformation() {
		showProgressDialog();
		missionsListView.setVisibility(View.INVISIBLE);
		runLoadChain(LOAD_CHAIN_1_USERDATA);
	}


	/**
	 * Configures Passport List Items : user statistics, missions list
	 */
	public void updateUI() {
		dismissProgressDialogWithDelay(500);

		if (KPHUserService.sharedInstance().getUserData() == null) {
			Logger.error(TAG, "UserData is null. Return(updateUI)");
			return;
		}

		missionsListView.setVisibility(View.VISIBLE);

		setAccountSwitchButton();
		setCreditText();
		setUserStats();
		setCurrentMissionStats();

		// Set new / complete missions count on tabs
		{
			updateNewMissionsArray();
			updateCompleteMissionsArray();
			updateTabTexts();
		}

		switchMissionTab(tabCurrentFragmentTag, true);

		if (isAutoStartMission) {
			onClickedStart();
			// This value setter must be called after "onClickedStart" method. Please DO NOT change the sequence.
			isAutoStartMission = false;
		}

		int trackerType = KPHUserService.sharedInstance().loadCurrentTrackerType();
		if (trackerType == KPHUserService.TRACKER_TYPE_NONE && userIdForThisDevice == 0) {
			if (getParentActivity() != null && getParentActivity() instanceof MainActivity) {
				((MainActivity) getParentActivity()).enableCatchTrackerDialog();
			}
		}

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (missionsListView != null && missionsListView.getAdapter() != null) {
					missionsListView.getAdapter().notifyDataSetChanged();
				} else {
					Logger.log(TAG, missionsListView == null ? "MissionsListView is NULL" : "MissionsListView adapter is NULL");
				}
			}
		}, 100);
	}


	private void updateButtonStatus() {
		if (KPHUserService.sharedInstance().getUserData() == null) {
			Logger.error(TAG, "UserData is null. Return(PassportMainFragment:setButtonStatus)");
			return;
		}

		if (startButton == null)
			return;

		int trackerType = KPHUserService.sharedInstance().loadCurrentTrackerType();
		if (trackerType == KPHUserService.TRACKER_TYPE_NONE) {
			deviceNotMatchLayout.setVisibility(View.GONE);

			startButton.setVisibility(View.VISIBLE);
			startButton.setEnabled(true);
			startButton.setText(getSafeContext().getString(R.string.start_counting_steps));
			startButton.setCustomImage(null);
		} else {
			if (trackerType == KPHUserService.TRACKER_TYPE_GOOGLEFIT) {
				KPHTracker curTracker = KPHUserService.sharedInstance().currentTracker();

				if (curTracker.getDeviceId().equalsIgnoreCase(KPHUtils.sharedInstance().getDeviceIdentifier())) {
					KPHUserMissionStats activeUserMission = KPHMissionService.sharedInstance().getActiveUserMission();
					if (activeUserMission == null) {
						// No active mission. Need to show message
						deviceNotMatchLayout.setText(R.string.get_active_and_save_lives);
						deviceNotMatchLayout.setVisibility(View.VISIBLE);

						startButton.setVisibility(View.GONE);
						startButton.setEnabled(false);
					} else {
						deviceNotMatchLayout.setVisibility(View.GONE);

						startButton.setVisibility(View.VISIBLE);
						startButton.setEnabled(true);
					}
				} else {
					Logger.error(TAG, "updateButtonStatus : deviceType=GoogleFit, but device ID is not equal");

					startButton.setVisibility(View.GONE);
					startButton.setEnabled(false);

					// Set span
					{
						String deviceNotMatchGuide = String.format(getString(R.string.google_fit_device_not_match), curTracker.getName());
						String learnMore = getSafeContext().getString(R.string.learn_more);
						int startIndex = deviceNotMatchGuide.indexOf(learnMore);
						int endIndex = startIndex + learnMore.length();

						ClickableSpan learnMoreSpan = new ClickableSpan() {
							@Override
							public void onClick(View widget) {
								onClickedLearnMore();
							}

							@Override
							public void updateDrawState(TextPaint ds) {
								super.updateDrawState(ds);
								ds.setColor(deviceNotMatchLayout.getCurrentTextColor());
							}
						};

						SpannableString ss = new SpannableString(deviceNotMatchGuide);
						ss.setSpan(learnMoreSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

						deviceNotMatchLayout.setText(ss);
					}

					deviceNotMatchLayout.setVisibility(View.VISIBLE);
				}

				startButton.setText(getSafeContext().getString(R.string.sync_band_caption));
				startButton.setCustomImage(null);
			} else if (trackerType == KPHUserService.TRACKER_TYPE_HEALTHKIT) {
				Logger.log(TAG, "updateButtonStatus : deviceType=HeathKit");

				startButton.setVisibility(View.GONE);
				startButton.setEnabled(false);
				startButton.setText(getSafeContext().getString(R.string.sync_band_caption));
				startButton.setCustomImage(null);

				// Set span
				{
					KPHTracker curTracker = KPHUserService.sharedInstance().currentTracker();

					String deviceNotMatchGuide = String.format(getString(R.string.health_kit_attached_learnmore), curTracker.getName());
					String learnMore = getSafeContext().getString(R.string.learn_more);
					int startIndex = deviceNotMatchGuide.indexOf(learnMore);
					int endIndex = startIndex + learnMore.length();

					ClickableSpan learnMoreSpan = new ClickableSpan() {
						@Override
						public void onClick(View widget) {
							onClickedLearnMore();
						}

						@Override
						public void updateDrawState(TextPaint ds) {
							super.updateDrawState(ds);
							ds.setColor(deviceNotMatchLayout.getCurrentTextColor());
						}
					};

					SpannableString ss = new SpannableString(deviceNotMatchGuide);
					ss.setSpan(learnMoreSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

					deviceNotMatchLayout.setText(ss);
				}

				deviceNotMatchLayout.setVisibility(View.VISIBLE);
			} else if (trackerType == KPHUserService.TRACKER_TYPE_KIDPOWERBAND) {
				startButton.setVisibility(View.VISIBLE);
				startButton.setEnabled(true);
				deviceNotMatchLayout.setVisibility(View.GONE);

				if (KPHMissionService.sharedInstance().shouldShowAutoStartMissionDialog()) {
					startButton.setText(getSafeContext().getText(R.string.sync_more_band_caption));
					startButton.setCustomImage(null);
				} else {
					startButton.setText(getSafeContext().getString(R.string.sync_band_caption));
					startButton.setCustomImage(null);
				}
			}
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == BleManager.REQUEST_ENABLE_BLE) {
			if (resultCode == Activity.RESULT_OK && BleManager.sharedInstance().isBleEnabled()) {
				checkLocationPermission();
			}
		}
	}


	private void checkInitBluetooth() {
		int bluetoothCheckResult = BleManager.checkInitBluetooth(getParentActivity());
		if (bluetoothCheckResult == BleManager.CHECK_BLUETOOTH_RESULT_FAILURE) {
			showErrorDialog(getString(R.string.bt_not_supported));
		} else if (bluetoothCheckResult == BleManager.CHECK_BLUETOOTH_RESULT_SUCCESS) {
			checkLocationPermission();
		} else if (bluetoothCheckResult == BleManager.CHECK_BLUETOOTH_RESULT_NONE) {
			// Not decided yet. Wait until onActivityResult
		}
	}


	private void checkLocationPermission() {
		if (!KPHUtils.sharedInstance().checkGPSProviderEnabled(getParentActivity(), true)) {
			return;
		}

		if (ContextCompat.checkSelfPermission(getParentActivity(), KPHConstants.LOCATION_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
			PermissionDialogFragment permissionDialog = new PermissionDialogFragment();
			permissionDialog.setRequestHandler(new PermissionDialogFragment.RequestPermissionHandler() {
				@Override
				public void onRequest() {
					Logger.log(TAG, "checkLocationPermission : send request location permission.");
					ActivityCompat.requestPermissions(getParentActivity(),
							new String[] {KPHConstants.LOCATION_PERMISSION},
							KPHConstants.PERMISSION_REQUEST_LOCATION);
				}

				@Override
				public void onClose() {
					Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_LOCATION_PERMISSION_FAILED);
					LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
				}
			});

			getParentActivity().showDialogFragment(permissionDialog);
			bandAction = BandAction.Band_Sync;
		} else {
			doSyncAction();
			Logger.log(TAG, "checkLocationPermission : have already location permission");
		}
	}


	void doSyncAction() {
		// go to syncing dialog
		Logger.log(TAG, "doSyncAction : Syncing");

		SyncTrackerFragment fragment = new SyncTrackerFragment();
		fragment.setDismissListener(new SuperDialogFragment.SuperDialogDismissListener() {
			@Override
			public void onDismiss() {
				if (isLoadChainRunning()) {
					showProgressDialog();
				}
			}
		});
		getParentActivity().showDialogFragment(fragment);
	}


	private void onMarkedMissionLog(final MissionLog missionLog) {
		Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_TRAVEL_LOG_READ_2);
		intent.putExtra(TravelLogMainFragment.TRAVEL_LOG_ID, missionLog.getMissionId());
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);

		Logger.log(TAG, "Mission ID to mark read : " + missionLog.getMissionId());

		KPHMissionService.sharedInstance().markTravelLogItemAsRead(missionLog, new KPHMissionService.OnTravelLogMarkListener() {
			@Override
			public void onSuccess() {
			}

			@Override
			public void onFailure() {
				if (retryCount < 0)
					return;

				retryCount--;
				onMarkedMissionLog(missionLog);
			}
		});
	}


	public void gotoMissionDetails(KPHMission mission) {
		long missionId = mission.getId();
		Logger.log(TAG, "Mission Selected : " + missionId);

		KPHMissionInformation missionInfo = KPHMissionService.sharedInstance().getMissionInformationById(missionId);
		KPHUserMissionStats userMissionStats = KPHMissionService.sharedInstance().userMissionStateById(missionId);

		if (userMissionStats != null && userMissionStats.isStartedMission()) {
			MissionDetailsFragment frag = new MissionDetailsFragment();
			frag.setMissionAndUserMissionStats(missionId, missionInfo, userMissionStats);
			getParentActivity().showNewFragment(frag);
		} else {
			MissionInfoFragment frag = new MissionInfoFragment();
			frag.setMissionInformation(missionInfo, userMissionStats);
			getParentActivity().showDialogFragment(frag);
		}
	}

	public void onClickedCredits() {
		if (!KPHUserService.sharedInstance().enabledPurchases()) {
			showErrorDialog(getSafeContext().getString(R.string.disabled_purchases));
			return;
		}

		BuyCreditFragment frag = new BuyCreditFragment();
		getParentActivity().showDialogFragment(frag);
	}


	public void setAccountSwitchButton() {
		if (familyAccountSwitchWindow == null) {
			familyAccountSwitchWindow = new PopupWindow(getParentActivity());

			familyAccountSwitchView = new KPHFamilyAccountSwitchView(getSafeContext());

			familyAccountSwitchWindow.setContentView(familyAccountSwitchView);
			familyAccountSwitchWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			familyAccountSwitchWindow.setFocusable(true);
			familyAccountSwitchWindow.setAnimationStyle(R.style.kph_style_popup_from_top_left_to_bottom_right);
		}


		familyAccountSwitchListAdapter = new FamilyAccountSwitchListAdapter(
				getParentActivity(), KPHUserService.sharedInstance().getUserData()
		);
		familyAccountSwitchListAdapter.setItemClickedListener(new OnFamilyAccountItemClickListener());
		familyAccountSwitchView.setFamilyAccountListAdapter(familyAccountSwitchListAdapter);
		familyAccountWindowWidth = ResolutionSet.getScreenSize(getSafeContext(), false).x - getResources().getDimensionPixelSize(R.dimen.dimen_margin_10) * 2;
		familyAccountSwitchWindow.setWidth(familyAccountWindowWidth);
	}


	public void setCreditText() {
		String strCredit;
		KPHUserData userData = KPHUserService.sharedInstance().getUserData();

		if (userData == null) {
			strCredit = getSafeContext().getString(R.string.credits);
		} else {
			int numCredit = userData.getCreditBalance();
			if (numCredit == 1)
				strCredit = String.format(getSafeContext().getString(R.string.format_credit), numCredit);
			else
				strCredit = String.format(getSafeContext().getString(R.string.format_credits), numCredit);
		}

		creditsButton.setText(strCredit);
	}

	public void setAvatarIcon() {
		KPHUserData userData = KPHUserService.sharedInstance().getUserData();
		Drawable avatar = KPHUserService.sharedInstance().getAvatarDrawable(
				userData.getAvatarId()
		);
		if (avatar != null) {
			mainAvatarView.setImageDrawable(avatar);
			smallAvatarImageView.setImageDrawable(avatar);
		}
	}


	public void setUserStats() {
		KPHUserData userData = KPHUserService.sharedInstance().getUserData();
		KPHUserStats userStats = userData.getUserStats();

		// Set values to UI elements
		setAvatarByAvatarId(smallAvatarImageView, userData.getAvatarId());
		setAvatarByAvatarId(mainAvatarView, userData.getAvatarId());

		usernameTextView.setText(userData.getHandle());

		if (userStats != null) {
			packetsTextView.setValue(String.valueOf(userStats.getTotalPackets()));
			missionsCompletedTextView.setValue(String.valueOf(userStats.getMissionsCompleted()));
			powerPointsTextView.setValue(String.valueOf(userStats.getTotalPowerPoints()));
		}

		updateButtonStatus();
		// End of 'Set values to UI elements'
	}


	public void setCurrentMissionStats() {
		KPHUserMissionStats activeMissionStats = KPHMissionService.sharedInstance().getActiveUserMission();
		if (activeMissionStats != null) {
			activeTabButton.setVisibility(View.VISIBLE);
			if (TextUtils.isEmpty(tabCurrentFragmentTag))
				tabCurrentFragmentTag = TAG_ACTIVE_MISSION_FRAGMENT;
		} else {
			activeTabButton.setVisibility(View.GONE);
			if (TextUtils.isEmpty(tabCurrentFragmentTag) || tabCurrentFragmentTag.equals(TAG_ACTIVE_MISSION_FRAGMENT))
				tabCurrentFragmentTag = TAG_NEW_MISSIONS_FRAGMENT;
		}

		if (!tabCurrentFragmentTag.equals(TAG_ACTIVE_MISSION_FRAGMENT)) {
			missionSummaryLayout.setVisibility(View.GONE);
			return;
		}

		if (activeMissionStats == null)
			return;

		long missionId = activeMissionStats.getMissionId();
		KPHMissionInformation missionInfo = KPHMissionService.sharedInstance().getMissionInformationById(missionId);


		ArrayList<ImageView> stampImageViewArray = new ArrayList<>();
		delightListLayout.removeAllViews();
		int countStamp = missionInfo.getDelights().size();

		int rowCount = (countStamp + 2) / 3;

		for (int i = 0; i < rowCount; i++) {
			View vwDelightRow = LayoutInflater.from(getSafeContext()).inflate(R.layout.item_delight_list, null);

			int delightSize = ResolutionSet.getScreenSize(getSafeContext(), true).x / 3 * 2 / 3;
			int padding = (ResolutionSet.getScreenSize(getSafeContext(), true).x - 3 * delightSize) / 8;

			ImageView ivDelight1 = (ImageView) vwDelightRow.findViewById(R.id.iv_delight_1);
			ImageView ivDelight2 = (ImageView) vwDelightRow.findViewById(R.id.iv_delight_2);
			ImageView ivDelight3 = (ImageView) vwDelightRow.findViewById(R.id.iv_delight_3);

			if (i == 0) {
				ivDelight1.setImageResource(R.drawable.postcard_placeholder);
			}

			if (i != rowCount - 1) {
				stampImageViewArray.add(ivDelight1);
				stampImageViewArray.add(ivDelight2);
				stampImageViewArray.add(ivDelight3);
			} else {
				switch (countStamp % 3) {
					case 0:
						stampImageViewArray.add(ivDelight1);
						stampImageViewArray.add(ivDelight2);
						stampImageViewArray.add(ivDelight3);
						break;
					case 1:
						stampImageViewArray.add(ivDelight1);
						ivDelight2.setImageDrawable(null);
						ivDelight3.setImageDrawable(null);
						break;
					case 2:
						stampImageViewArray.add(ivDelight1);
						stampImageViewArray.add(ivDelight2);
						ivDelight3.setImageDrawable(null);
						break;
				}
			}

			RelativeLayout layoutItem = (RelativeLayout) vwDelightRow.findViewById(R.id.layout_item);
			LinearLayout layoutList = (LinearLayout) layoutItem.findViewById(R.id.layout_list);

			if (i == 0) {
				layoutItem.setPadding(padding, 0, padding, 0);
			} else {
				layoutItem.setPadding(padding, padding * 2, padding, 0);
			}

			RelativeLayout.LayoutParams lpLayoutList = (RelativeLayout.LayoutParams) layoutList.getLayoutParams();
			lpLayoutList.height = delightSize;
			layoutList.setLayoutParams(lpLayoutList);

			delightListLayout.addView(vwDelightRow);
		}

		List<KPHDelightUnlocked> stamps = activeMissionStats.getDelightsUnlocked();
		ArrayList<Integer> unlockedDelights = new ArrayList<>();
		// filter only mission related delight
		if (stamps != null) {
			for (KPHDelightUnlocked delight : stamps) {
				unlockedDelights.add((int) delight.getDelightId());
			}
		}

		int percent = activeMissionStats.getProgress();
		if (activeMissionStats.isCompletedMission()) {
			// set MissionComplete Image
			missionCompleteImageView.setImageDrawable(missionInfo.getCompleteDrawable());
			missionCompleteImageView.setTag(missionId);
			missionCompleteImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					long missionId = (Long) v.getTag();
					KPHMissionInformation missionInfo = KPHMissionService.sharedInstance().getMissionInformationById(missionId);

					InfoMissionFragment frag = new InfoMissionFragment();
					frag.setBackTitle(getSafeContext().getString(R.string.passport));
					frag.showTabBar(false);
					frag.setMissionInformation(missionInfo, true);
					getParentActivity().showNewFragment(frag);
				}
			});
			missionStatusTextView.setText("Mission complete!");
			percent = 100;                // fixed for completed mission which not arrival goal.
			// set StartTime
			Date date = OSDate.fromUTCString(activeMissionStats.getCompletedAt());

			SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
			SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

			try {
				timeTextView.setText("MISSION COMPLETE! on " + dateFormat.format(date) + " at " + timeFormat.format(date));
			} catch (Exception e) {
				e.printStackTrace();
			}

			int packetGoal = activeMissionStats.getMissionGoal() / 50 / 10;
			if (activeMissionStats.getMissionPackets() < packetGoal) {
				Logger.error(TAG, "found incorrect completed mission, packet:%d(%d)", activeMissionStats.getMissionPackets(), packetGoal);
			} else {
				packetGoal = activeMissionStats.getMissionPackets();
			}
			summaryPacketButton.setText("" + packetGoal);

			int pointGoal = activeMissionStats.getMissionGoal() / 50;
			if (activeMissionStats.getMissionPowerPoint() < pointGoal) {
				Logger.error(TAG, "found incorrect completed mission, point:%d(%d)", activeMissionStats.getMissionPowerPoint(), pointGoal);
			} else {
				pointGoal = activeMissionStats.getMissionPowerPoint();
			}
			summaryPowerPointButton.setText("" + pointGoal);

			unlockedDelights = missionInfo.getDelights();
		} else {
			missionCompleteImageView.setVisibility(View.GONE);
			timeTextView.setVisibility(View.GONE);
			placeLayout.setVisibility(View.GONE);

			int packetGoal = activeMissionStats.getMissionGoal() / 50 / 10;
			missionStatusTextView.setText(percent + "% toward " + packetGoal + " packet goal");

			summaryPacketButton.setText(activeMissionStats.getMissionPackets() + "/" + packetGoal);
			summaryPowerPointButton.setText(activeMissionStats.getMissionPowerPoint() + "/" + activeMissionStats.getMissionGoal() / 50);
		}

		// set Complete Background Image
		completeBackgroundImageView.setImageDrawable(missionInfo.getCountryDrawable());

		missionPercentProgressView.setPercentageFilled(percent);

		// set Stamp Images
		{
			int count = unlockedDelights.size();
			if (count > stampImageViewArray.size())
				count = stampImageViewArray.size();

			for (int i = 0; i < count; i++) {
				int delight = unlockedDelights.get(i);
				KPHDelightInformation delInfo = KPHMissionService.sharedInstance().getDelightInformationById(delight);

				if (delInfo == null || !delInfo.isMissionIdEquals(activeMissionStats.getMissionId()))
					continue;

				Drawable drawable = delInfo.getImageDrawable();
				if (drawable != null)
					stampImageViewArray.get(i).setImageDrawable(drawable);
				stampImageViewArray.get(i).setTag(delInfo);
				stampImageViewArray.get(i).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						KPHDelightInformation delight = (KPHDelightInformation) v.getTag();
						if (delight.getType().equals(KPHConstants.DELIGHT_POSTCARD)) {
							InfoShareFragment frag = new InfoShareFragment();
							frag.setBackTitle(getSafeContext().getString(R.string.passport));
							frag.showTabBar(false);
							frag.setDelightInformation(delight);
							getParentActivity().showNewFragment(frag);
						} else {
							InfoDelightFragment frag = new InfoDelightFragment();
							frag.setBackTitle(getSafeContext().getString(R.string.passport));
							frag.showTabBar(false);
							frag.setDelightInformation(delight, null);
							getParentActivity().showNewFragment(frag);
						}
					}
				});
			}
		}

		// set Title
		titleTextView.setText(activeMissionStats.getMissionName().toUpperCase());

		// set MissionCompleteName
		missionCompleteNameTextView.setText(activeMissionStats.getMissionName());
	}


	public void setAvatarByAvatarId(ImageView imgView, String avatarId) {
		if (avatarId.length() != 0) {
			Drawable drawable = KPHUserService.sharedInstance().getAvatarDrawable(avatarId);
			if (drawable != null)
				imgView.setImageDrawable(drawable);
			else {
				imgView.setImageDrawable(
						UIManager.sharedInstance().getImageDrawable(R.drawable.avatar_placeholder)
				);
			}
		}
	}


	private IntentFilter intentFilter = null;
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (getActivity() == null) {
				// Already detached
				return;
			}

			String action = intent.getAction();
			switch (action) {
				// In case when avatar is changed.
				case KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_AVATAR_UPDATED:
					setAvatarIcon();
					break;

				case KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_USER_DATA_UPDATED:
					setCreditText();
					runLoadChain(LOAD_CHAIN_2_MISSION_DETAILED_LOG);
					break;

				case KPHBroadcastSignals.BROADCAST_SIGNAL_USER_MISSION_LIST_CHANGED:
					runLoadChain(LOAD_CHAIN_5_USER_MISSION_PROGRESS_STATS);
					break;

				case KPHBroadcastSignals.BROADCAST_SIGNAL_UPDATE_PASSPORT_UI:
					updateUI();
					break;

				case KPHBroadcastSignals.BROADCAST_SIGNAL_STARTING_USERMISSION: {
					long missionId = intent.getLongExtra("MissionId", -1);
					if (missionId == -1)
						return;

					KPHUserMissionStats syncingMission = KPHMissionService.sharedInstance().userMissionStateById(missionId);
					if (syncingMission == null)
						return;

					StartMissionFragment frag = new StartMissionFragment();
					frag.setSyncingMission(syncingMission);
					getParentActivity().showDialogFragment(frag);
					break;
				}

				case KPHBroadcastSignals.BROADCAST_SIGNAL_GOTO_USERMISSION: {
					tabCurrentFragmentTag = TAG_ACTIVE_MISSION_FRAGMENT;
					runLoadChain(LOAD_CHAIN_1_USERDATA);
					break;
				}

				case KPHBroadcastSignals.BROADCAST_SIGNAL_CREDIT_BALANCE_UPDATED:
					setCreditText();
					break;

				case KPHBroadcastSignals.BROADCAST_SIGNAL_LOCATION_PERMISSION_SUCCESS:
					Logger.log(TAG, "BroadcastReceiver : Grant Location Permission succeed.");
					if (bandAction == BandAction.Band_Sync) {
						Logger.log(TAG, "BroadcastReceiver : linkOrSync");
						doSyncAction();
					}
					break;

				case KPHBroadcastSignals.BROADCAST_SIGNAL_LOCATION_PERMISSION_FAILED:
					Logger.log(TAG, "BroadcastReceiver : Grant Location Permission failed.");
					showErrorDialog(context.getString(R.string.permission_location_did_not_grant));
					break;

				case KPHBroadcastSignals.BROADCAST_SIGNAL_TRAVEL_LOG_UPDATED:
					if (getParentActivity() instanceof MainActivity) {
						((MainActivity) getParentActivity()).getTabItem(MainActivity.INDEX_FRAGMENT_TRAVEL_LOG)
								.setBadgeValue(KPHMissionService.sharedInstance().getUnreadTravelLogCount());

						Logger.log(TAG, "BroadcastReceiver : Travel log updated");
						if (tabCurrentFragmentTag.equals(TAG_ACTIVE_MISSION_FRAGMENT)) {
							updateTravelLogs();
						}
					}
					break;


				case KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_TRACKER_SELECTED:            // Tracker is connected
					// Initialize current tracker
				{
					KPHTracker registeredTracker = KPHUserService.sharedInstance().getTempTracker();
					if (registeredTracker == null) {
						// Unexpected Error. Registered tracker must be set once device is connected and successfully attached to user
						return;
					}

					KPHUserService.sharedInstance().setCurrentTracker(registeredTracker);
				}
				case KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_DEVICE_CHANGED:            // Tracker is disconnected
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							runLoadChain(LOAD_CHAIN_2_MISSION_DETAILED_LOG);
						}
					}, 80);
					break;
				case KPHBroadcastSignals.BROADCAST_SIGNAL_TRAVEL_LOG_READ_1:
					updateTravelLogs();
					break;
			}
		}
	};


	private void switchMissionTab(String fragmentTag, boolean forceSwitch) {
		if (fragmentTag == null)
			return;

		if (!forceSwitch && fragmentTag.equals(tabCurrentFragmentTag))
			return;

		tabCurrentFragmentTag = fragmentTag;

		activeTabButton.setBackgroundColor(Color.TRANSPARENT);
		newTabButton.setBackgroundColor(Color.TRANSPARENT);
		completeTabButton.setBackgroundColor(Color.TRANSPARENT);

		switch (fragmentTag) {
			case TAG_ACTIVE_MISSION_FRAGMENT:
				newTabButton.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_main_background));
				completeTabButton.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_main_background));

				updateTravelLogs();
				missionsListView.setDrawingGradient(true);
				break;

			case TAG_NEW_MISSIONS_FRAGMENT:
				newTabButton.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_blue));
				activeTabButton.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_main_background));
				completeTabButton.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_main_background));
				missionsListView.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_blue));

				updateNewMissionsArray();
				setNewMissionsAdapter();
				missionsListView.setDrawingGradient(false);
				break;

			case TAG_COMPLETE_MISSIONS_FRAGMENT:
				completeTabButton.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_blue));
				activeTabButton.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_main_background));
				newTabButton.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_main_background));
				missionsListView.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_blue));

				updateCompleteMissionsArray();
				setCompleteMissionsAdapter();
				missionsListView.setDrawingGradient(false);
				break;
		}

		updateTabTexts();

		if (fragmentTag.equals(TAG_ACTIVE_MISSION_FRAGMENT)) {
			missionSummaryLayout.setVisibility(View.VISIBLE);
			noCorrespondingMissionsTextView.setVisibility(View.GONE);
		} else {
			missionSummaryLayout.setVisibility(View.GONE);
			if (fragmentTag.equals(TAG_NEW_MISSIONS_FRAGMENT) && (newMissionArray == null || newMissionArray.size() == 0)) {
				noCorrespondingMissionsTextView.setVisibility(View.VISIBLE);
				noCorrespondingMissionsTextView.setText(R.string.no_new_mission_text);
			} else if (fragmentTag.equals(TAG_COMPLETE_MISSIONS_FRAGMENT) && (completeMissionArray == null || completeMissionArray.size() == 0)) {
				noCorrespondingMissionsTextView.setVisibility(View.VISIBLE);
				noCorrespondingMissionsTextView.setText(R.string.no_complete_mission_text);
			} else {
				noCorrespondingMissionsTextView.setVisibility(View.GONE);
			}
		}
	}

	private void updateTabTexts() {
		String newTabTitle;
		if (newMissionArray.size() > 0) {
			newTabTitle = KPHUtils.sharedInstance().getApplicationContext().getString(R.string.new_number, newMissionArray.size());
		} else {
			newTabTitle = KPHUtils.sharedInstance().getApplicationContext().getString(R.string.new_string);
		}

		newTabButton.setText(newTabTitle);


		String completeTabTitle;
		if (completeMissionArray.size() > 0) {
			completeTabTitle = KPHUtils.sharedInstance().getApplicationContext().getString(R.string.complete_number, completeMissionArray.size());
		} else {
			completeTabTitle = KPHUtils.sharedInstance().getApplicationContext().getString(R.string.complete);
		}
		completeTabButton.setText(completeTabTitle);
	}

	public void onClickedStart() {
		int trackerType = KPHUserService.sharedInstance().loadCurrentTrackerType();
		if (trackerType == KPHUserService.TRACKER_TYPE_NONE) {
			KPHUserData userData = KPHUserService.sharedInstance().getUserData();

			SelectDeviceFragment fragment = new SelectDeviceFragment();
			fragment.showTabBar(false);
			fragment.setApplyToChilds(true);
			fragment.setData(userData.getId(), userData.getHandle(), userData.getAvatarId());
			getParentActivity().showNewFragment(fragment);
		} else {
			// In case of google fit.
			long currentTimestamp = SystemClock.uptimeMillis();
			if (syncLastClickTimestamp != 0 && (currentTimestamp - syncLastClickTimestamp < SYNC_CLICK_MINIMUM_INTERVAL))
				return;

			syncLastClickTimestamp = currentTimestamp;

			KPHTracker tracker = KPHUserService.sharedInstance().currentTracker();
			if (tracker != null && tracker.getDeviceType().equalsIgnoreCase(KPHTracker.TRACKER_TYPE_NAME_HEALTHKIT)) {
				// Do nothing. Unreachable code.
			} else if (tracker != null && KPHMissionService.sharedInstance().shouldShowAutoStartMissionDialog()) {
				ProfileMissionAutoStartFragment dialogFragment = new ProfileMissionAutoStartFragment();
				dialogFragment.setAutoStartFlag(isAutoStartMission);
				getParentActivity().showDialogFragment(dialogFragment);
			} else {
				if (trackerType == KPHUserService.TRACKER_TYPE_GOOGLEFIT) {
					doSyncAction();
				} else {
					checkInitBluetooth();
				}
			}
		}
	}

	public void onClickedLearnMore() {
		KPHTracker curTracker = KPHUserService.sharedInstance().currentTracker();

		SyncWithOwnDeviceFragment fragment = new SyncWithOwnDeviceFragment();
		fragment.setData(curTracker.getName(), KPHUserService.sharedInstance().loadCurrentTrackerType());
		getParentActivity().showDialogFragment(fragment);
	}


	private void updateTravelLogs() {
		List<KPHUserTravelLog> travelLogsAll = KPHMissionService.sharedInstance().userTravelLogs();
		if (travelLogsAll == null) {
			Logger.error(TAG, "refresh Mission List : occurred critical bug");
			return;
		}

		KPHUserMissionStats activeMissionStats = KPHMissionService.sharedInstance().getActiveUserMission();
		if (activeMissionStats == null) {
			return;
		}

		long missionId = activeMissionStats.getMissionId();

		travelLogs.clear();

		boolean hasStarted = false, hasCompleted = false;
		for (KPHUserTravelLog srcLog : travelLogsAll) {
			if (srcLog.getMissionId() != missionId) {
				continue;
			}

			if (srcLog.isStartedLog()) {
				if (hasStarted) {
					// has already started log
					continue;
				} else {
					hasStarted = true;
				}
			}

			if (srcLog.isCompletedLog()) {
				if (hasCompleted) {
					//has already completed log
					continue;
				} else {
					hasCompleted = true;
				}
			}

			travelLogs.add(new MissionLog(srcLog));
		}

		missionsListView.setAdapter(travelLogsAdapter);
	}


	private void updateNewMissionsArray() {
		ArrayList<KPHMission> tempNewMissionsList = KPHMissionService.sharedInstance().getSortedNewMissionsList();

		newMissionArray.clear();
		newMissionArray.addAll(tempNewMissionsList);
	}

	private void setNewMissionsAdapter() {
		if (newMissionArray.size() == 0) {
			noCorrespondingMissionsTextView.setVisibility(View.VISIBLE);
			noCorrespondingMissionsTextView.setText(R.string.no_new_mission_text);
		} else {
			noCorrespondingMissionsTextView.setVisibility(View.GONE);
		}

		// Initialize mission list
		missionsListView.setAdapter(newPassportListAdapter);
	}


	private void updateCompleteMissionsArray() {
		ArrayList<KPHMission> tempCompleteMissionsList = KPHMissionService.sharedInstance().getSortedCompleteMissionsList();

		completeMissionArray.clear();
		completeMissionArray.addAll(tempCompleteMissionsList);
	}

	private void setCompleteMissionsAdapter() {
		if (completeMissionArray.size() == 0) {
			noCorrespondingMissionsTextView.setVisibility(View.VISIBLE);
			noCorrespondingMissionsTextView.setText(R.string.no_complete_mission_text);
		} else {
			noCorrespondingMissionsTextView.setVisibility(View.GONE);
		}

		missionsListView.setAdapter(completePassportListAdapter);
	}


	private void onMissionClicked(KPHMission mission) {
		if (mission == null)
			return;

		long currentTimestamp = SystemClock.uptimeMillis();
		if (currentTimestamp - missionLastClickTimestamp > MISSION_CLICK_MINIMUM_INTERVAL) {
			missionLastClickTimestamp = currentTimestamp;
			gotoMissionDetails(mission);
		}
	}


	/***********************************************************************************************
	 * Methods to loading data chain
	 */
	private void setLoadChainFlag() {
		Logger.log(TAG, "setLoadChainFlag");
		synchronized (loadChainRunning) {
			if (!loadChainRunning) {
				loadChainRunning = true;
			}
		}
	}

	private void clearLoadChainFlag() {
		Logger.log(TAG, "clearLoadChainFlag");
		synchronized (loadChainRunning) {
			if (loadChainRunning) {
				loadChainRunning = false;
			}
		}
	}

	private boolean isLoadChainRunning() {
		Logger.log(TAG, "isLoadChainRunning");

		boolean result;
		synchronized (loadChainRunning) {
			result = loadChainRunning;
		}
		return result;
	}

	private void runLoadChain(int chainIndex) {
		setLoadChainFlag();

		SuperActivity mainActivity = getParentActivity();
		if (mainActivity == null) {
			Logger.log(TAG, "Main activity is null. runLoadChain");
			clearLoadChainFlag();
			return;
		}

		if (mainActivity.getTopFragment() instanceof PassportMainFragment)
			showProgressDialog();

		Logger.log(TAG, "runLoadChain : " + chainIndex);

		if (chainIndex == LOAD_CHAIN_1_USERDATA) {
			// 1st Chain. Load user data
			retryCount = RETRY_COUNT_LIMIT;
			loadData_Chain1_UserData(KPHUserService.sharedInstance().getUserData().getId());
		} else if (chainIndex == LOAD_CHAIN_2_MISSION_DETAILED_LOG) {
			// 2nd Chain. Load mission detailed log
			retryCount = RETRY_COUNT_LIMIT;
			loadData_Chain2_MissionDetailedLog();
		} else if (chainIndex == LOAD_CHAIN_3_ALL_SUPPORTED_MISSION) {
			// 3rd Chain. Load all supported missions
			retryCount = RETRY_COUNT_LIMIT;
			loadData_Chain3_SupportedMissions();
		} else if (chainIndex == LOAD_CHAIN_4_ALL_USER_MISSIONS) {
			// 4th Chain. Load all user missions
			retryCount = RETRY_COUNT_LIMIT;
			loadData_Chain4_AllUserMissions();
		} else if (chainIndex == LOAD_CHAIN_5_USER_MISSION_PROGRESS_STATS) {
			// 5th Chain. Load user mission progress stats
			retryCount = RETRY_COUNT_LIMIT;
			loadData_Chain5_UserMissionProgressStats();
		} else if (chainIndex == LOAD_CHAIN_6_CURRENT_TRACKER) {
			// 6th Chain. Load current tracker information
			retryCount = RETRY_COUNT_LIMIT;
			loadData_Chain6_CurrentTracker(KPHUserService.sharedInstance().getUserData().getId());
		} else if (chainIndex == LOAD_CHAIN_7_GOOGLE_FIT_INFORMATION) {
			// 7th Chain. Load current device google fit information
			retryCount = RETRY_COUNT_LIMIT;
			loadData_Chain7_GoogleFitInformation();
		}
	}


	private void loadData_Chain1_UserData(final int userId) {
		Logger.log(TAG, "loadDataChain1");

		KPHUserService.sharedInstance().fetchUserData(userId, new onActionListener() {
			@Override
			public void completed(Object object) {
				Logger.log(TAG, "loadDataChain1------Success");

				KPHUserData kphUserData = (KPHUserData) object;
				if (kphUserData != null) {
					KPHUserService.sharedInstance().saveUserData(kphUserData);
					runLoadChain(LOAD_CHAIN_2_MISSION_DETAILED_LOG);
				}
			}

			@Override
			public void failed(int code, String message) {
				if (retryCount < 0) {
					Logger.log(TAG, "loadDataChain1------Failure:Finish");
					showErrorDialog(message);
					clearLoadChainFlag();
				} else {
					Logger.log(TAG, "loadDataChain1------Failure:Retry");
					retryCount--;
					loadData_Chain1_UserData(userId);
				}
			}
		});
	}

	private void loadData_Chain2_MissionDetailedLog() {
		Logger.log(TAG, "loadDataChain2");

		KPHMissionService missionService = KPHMissionService.sharedInstance();
		int userId = KPHUserService.sharedInstance().getUserData().getId();

		missionService.loadUserTravelLog(userId, new KPHMissionService.OnLoadUserTravelLog() {
			@Override
			public void onSuccess() {
				Logger.log(TAG, "loadDataChain2------Success-------Travel Log Update Signal Broadcast");
				LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(
						new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_TRAVEL_LOG_UPDATED)
				);

				runLoadChain(LOAD_CHAIN_3_ALL_SUPPORTED_MISSION);
			}

			@Override
			public void onFailure(String message) {
				if (retryCount < 0) {
					Logger.log(TAG, "loadDataChain2------Failed:Finish");
					showErrorDialog(message);
					clearLoadChainFlag();
				} else {
					Logger.log(TAG, "loadDataChain2------Failed:Retry");
					retryCount--;
					loadData_Chain2_MissionDetailedLog();
				}
			}
		});
	}

	private void loadData_Chain3_SupportedMissions() {
		Logger.log(TAG, "loadDataChain3");
		KPHMissionService.sharedInstance().fetchSupportedMissionsList(new onActionListener() {
			@Override
			public void completed(Object object) {
				Logger.log(TAG, "loadDataChain3------Success");
				runLoadChain(LOAD_CHAIN_4_ALL_USER_MISSIONS);
			}

			@Override
			public void failed(int code, String message) {
				if (retryCount < 0) {
					Logger.log(TAG, "loadDataChain3------Failed:Finished");
					showErrorDialog(message);
					clearLoadChainFlag();
				} else {
					Logger.log(TAG, "loadDataChain3------Failed:Retry");
					retryCount--;
					loadData_Chain3_SupportedMissions();
				}
			}
		});
	}

	private void loadData_Chain4_AllUserMissions() {
		Logger.log(TAG, "loadDataChain4");
		KPHMissionService.sharedInstance().fetchUserMissionsAllForUser(KPHUserService.sharedInstance().getUserData().getId(), new onActionListener() {
			@Override
			public void completed(Object object) {
				Logger.log(TAG, "loadDataChain4------Success");
				runLoadChain(LOAD_CHAIN_5_USER_MISSION_PROGRESS_STATS);
			}

			@Override
			public void failed(int code, String message) {
				if (retryCount < 0) {
					Logger.log(TAG, "loadDataChain4------Failed:Finish");
					showErrorDialog(message);
					clearLoadChainFlag();
				} else {
					Logger.log(TAG, "loadDataChain4------Failed:Retry");
					retryCount--;
					loadData_Chain4_AllUserMissions();
				}
			}
		});
	}

	private void loadData_Chain5_UserMissionProgressStats() {
		Logger.log(TAG, "loadDataChain5");
		KPHMissionService.sharedInstance().getUserMissionProgress(new RestCallback<List<KPHUserMissionStats>>() {
			@Override
			public void success(List<KPHUserMissionStats> kphUserMissionStats, Response response) {
				Logger.log(TAG, "loadDataChain5------Success");
				runLoadChain(LOAD_CHAIN_6_CURRENT_TRACKER);
			}

			@Override
			public void failure(RestError restError) {
				if (retryCount < 0) {
					Logger.log(TAG, "loadDataChain5------Failed:Finish");
					showErrorDialog(KPHUtils.sharedInstance().getNonNullMessage(restError));
					clearLoadChainFlag();
				} else {
					Logger.log(TAG, "loadDataChain5------Failed:Retry");
					retryCount--;
					loadData_Chain5_UserMissionProgressStats();
				}
			}
		});
	}

	private void loadData_Chain6_CurrentTracker(final int userId) {
		Logger.log(TAG, "loadDataChain6");

		KPHUserService.sharedInstance().getCurrentTrackerByUserId(userId, new onActionListener() {
			@Override
			public void completed(Object object) {
				Logger.log(TAG, "loadDataChain6------Success");

				KPHTracker curTracker = null;
				if (object != null)
					curTracker = (KPHTracker) object;

				KPHUserService.sharedInstance().setCurrentTracker(curTracker);
				runLoadChain(LOAD_CHAIN_7_GOOGLE_FIT_INFORMATION);
			}

			@Override
			public void failed(int code, String message) {
				if (retryCount < 0) {
					Logger.log(TAG, "loadDataChain6------Failed:Finish");
					showErrorDialog(message);
					clearLoadChainFlag();
				} else {
					Logger.log(TAG, "loadDataChain6------Failed:Retry");
					retryCount--;
					loadData_Chain6_CurrentTracker(userId);
				}
			}
		});
	}

	private void loadData_Chain7_GoogleFitInformation() {
		Logger.log(TAG, "loadDataChain7");
		GoogleFitService.checkIfAttached(new GoogleFitService.OnAttachedListener() {
			@Override
			public void onSuccess(int userid) {
				Logger.log(TAG, "loadDataChain7------Success");
				clearLoadChainFlag();

				userIdForThisDevice = userid;
				Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_UPDATE_PASSPORT_UI);
				LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
			}

			@Override
			public void onFailure(String message) {
				if (retryCount < 0) {
					Logger.log(TAG, "loadDataChain7------Failed:Finish");
					clearLoadChainFlag();

					userIdForThisDevice = 0;
					Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_UPDATE_PASSPORT_UI);
					LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
				} else {
					Logger.log(TAG, "loadDataChain7------Failed:Retry");
					retryCount--;
					loadData_Chain7_GoogleFitInformation();
				}
			}
		});
	}
	/*
	 * End of "Method to start loading data chain"
	 **********************************************************************************************/


	private class OnFamilyAccountItemClickListener implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			if (familyAccountSwitchWindow == null)
				return;

			int position = (Integer) view.getTag();
			familyAccountSwitchWindow.dismiss();

			showProgressDialog();
			KPHUserService.sharedInstance().signinOtherUser(
					familyAccountSwitchListAdapter.getItem(position).getId(),
					new OnFamilyAccountLoginActionListener()
			);
		}

		class OnFamilyAccountLoginActionListener implements onActionListener {
			@Override
			public void completed(Object object) {
				dismissProgressDialog();
				KPHNotificationUtil.sharedInstance().showSuccessNotification(getParentActivity(),
						getString(
								R.string.logged_in_as,
								KPHUserService.sharedInstance().getUserData().getHandle()
						)
				);

				if (getParentActivity() != null && getParentActivity() instanceof MainActivity) {
					((MainActivity) getParentActivity()).restartActivity();
				}
			}

			@Override
			public void failed(int code, String message) {
				dismissProgressDialog();
				if (getParentActivity() != null)
					getParentActivity().showAlertDialog(getString(R.string.switch_account_error_title), message, null);
			}
		}
	}

	private class OnAccountSwitchButtonListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_UI_FAMILY_ICON);

			int[] nCoordinates = new int[]{0, ResolutionSet.getStatusBarHeight(getSafeContext())};

			int nHeight = familyAccountSwitchView.getEstimatedHeight();
			int nMaximumAvailableHeight = ResolutionSet.getScreenSize(getSafeContext(), false).y -
					ResolutionSet.getStatusBarHeight(getSafeContext()) -
					getResources().getDimensionPixelSize(R.dimen.dimen_navigation_bar_height);

			if (nHeight >= nMaximumAvailableHeight) {
				nHeight = nMaximumAvailableHeight;
			}

			familyAccountSwitchWindow.setHeight(nHeight);
			familyAccountSwitchWindow.showAtLocation(
					familyAccountSwitchView,
					Gravity.NO_GRAVITY,
					getResources().getDimensionPixelSize(R.dimen.dimen_margin_10),
					nCoordinates[1] + getResources().getDimensionPixelSize(R.dimen.dimen_navigation_bar_height) - 10
			);
		}
	}

}
