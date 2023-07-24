package org.unicefkidpower.kid_power.View.Activities.Main.Passport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.OSDate;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.Model.MicroService.KPHMissionService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.Structure.KPHDelightInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHDelightUnlocked;
import org.unicefkidpower.kid_power.Model.Structure.KPHMissionInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMissionStats;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserTravelLog;
import org.unicefkidpower.kid_power.Model.Structure.MissionLog;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.Adapters.MissionLogAdapter;
import org.unicefkidpower.kid_power.View.CustomControls.KPHImageTextButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.CustomControls.ProgressBarView;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by Dayong Li on 11/5/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class MissionDetailsFragment extends SuperFragment {
	private final int					RETRY_COUNT_LIMIT		= 10;
	private int							retryCount				= RETRY_COUNT_LIMIT;

	// Constants
	protected static final String		TAG						= "MissionDetailsFragment";

	// UI Controls
	protected View						contentView				= null;

	private LinearLayout				layoutTravelLog			= null;

	protected long						missionId				= 0;
	protected KPHMissionInformation		missionInfo				= null;
	protected KPHUserMissionStats		missionStats			= null;
	protected MissionLogAdapter			missionLogAdapter		= null;
	protected List<MissionLog>			missionLogs				= new ArrayList<>();

	private IntentFilter				intentFilter			= null;


	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (getActivity() == null) {
				// Already detached
				return;
			}

			String action = intent.getAction();
			if (action.equals(KPHBroadcastSignals.BROADCAST_SIGNAL_TRAVEL_LOG_UPDATED)) {
				if (getParentActivity() == null)
					return;

				dismissProgressDialog();

				if (getParentActivity() instanceof MainActivity) {
					((MainActivity) getParentActivity()).getTabItem(MainActivity.INDEX_FRAGMENT_TRAVEL_LOG)
							.setBadgeValue(KPHMissionService.sharedInstance().getUnreadTravelLogCount());
				}

				List<KPHUserTravelLog> travelLogs = KPHMissionService.sharedInstance().userTravelLogs();
				filterMissionDetailedLog(travelLogs);
			}
		}
	};


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		missionLogAdapter = new MissionLogAdapter(getSafeContext(), missionLogs, getSafeContext().getString(R.string.mission));
		missionLogAdapter.setOnMissionLogItemClickListener(new MissionLogAdapter.OnMissionLogItemClickListener() {
			@Override
			public void onClickMissionLogItem(MissionLog missionLog) {
				retryCount = RETRY_COUNT_LIMIT;
				onClickedMissionLogItem(missionLog);
			}
		});

		// Travel Log List
		layoutTravelLog = (LinearLayout) contentView.findViewById(R.id.list_travellog);
		layoutTravelLog.setVisibility(View.VISIBLE);

		if (intentFilter == null) {
			intentFilter = new IntentFilter();
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_TRAVEL_LOG_UPDATED);
			LocalBroadcastManager.getInstance(getSafeContext()).registerReceiver(receiver, intentFilter);
		}

		return contentView;
	}


	@Override
	public void onResume() {
		super.onResume();

		retryCount = RETRY_COUNT_LIMIT;
		loadMissionDetailedLog(KPHUserService.sharedInstance().getUserData().getId());
		if (missionLogs == null) {
			missionLogs = new ArrayList<>();
		}
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();

		LocalBroadcastManager.getInstance(getSafeContext()).unregisterReceiver(receiver);
		intentFilter = null;
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_mission_details;
	}


	private void onClickedMissionLogItem(final MissionLog missionLog) {
		KPHMissionService.sharedInstance().markTravelLogItemAsRead(missionLog, new KPHMissionService.OnTravelLogMarkListener() {
			@Override
			public void onSuccess() {}
			@Override
			public void onFailure() {
				if (retryCount < 0)
					return;

				retryCount--;
				onClickedMissionLogItem(missionLog);
			}
		});
	}


	public void notifyDataSetChanged() {
		if (getView() != null) {
			retryCount = RETRY_COUNT_LIMIT;
			loadMissionDetailedLog(KPHUserService.sharedInstance().getUserData().getId());
			if (missionLogs == null) {
				missionLogs = new ArrayList<>();
			}
		}
	}


	/**
	 * Get Travel Log from Server
	 *
	 * @param userId Identifier of current user
	 */
	private void loadMissionDetailedLog(final int userId) {
		KPHMissionService missionService = KPHMissionService.sharedInstance();
		if (missionService == null || userId == 0)
			return;

		List<KPHUserTravelLog> logs = missionService.userTravelLogs();
		if (logs == null) {
			// maybe 1st load
			showProgressDialog();
		} else {
			filterMissionDetailedLog(logs);
		}

		KPHMissionService.sharedInstance().loadUserTravelLog(userId, new KPHMissionService.OnLoadUserTravelLog() {
			@Override
			public void onSuccess() {
				Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_TRAVEL_LOG_UPDATED);
				LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
			}
			@Override
			public void onFailure(String message) {
				if (retryCount < 0) {
					showErrorDialog(message);
					return;
				}

				retryCount--;
				loadMissionDetailedLog(userId);
			}
		});
	}

	/**
	 * filter travel log for this mission only
	 *
	 * @param travelLogs
	 */
	protected void filterMissionDetailedLog(List<KPHUserTravelLog> travelLogs) {
		dismissProgressDialog();

		if (travelLogs == null) {
			Logger.error(TAG, "refresh Mission List : occurred critical bug");
			return;
		}

		missionLogs.clear();

		boolean hasStarted = false, hasCompleted = false;
		for (KPHUserTravelLog srcLog : travelLogs) {
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

			missionLogs.add(new MissionLog(srcLog));
		}

		loadTravelLogs();
	}

	public void setMissionAndUserMissionStats(long missionId, KPHMissionInformation missionInfo, KPHUserMissionStats userMissionStats) {
		this.missionId = missionId;
		this.missionInfo = missionInfo;
		missionStats = userMissionStats;
	}

	protected void loadTravelLogs() {
		layoutTravelLog.removeAllViews();
		layoutTravelLog.addView(getMissionSummaryView());

		for (int i = 0; i < missionLogs.size(); i++) {
			RecyclerView.ViewHolder holder = missionLogAdapter.createViewHolder(null, MissionLogAdapter.VIEW_TYPE_ITEM);
			missionLogAdapter.bindViewHolder(holder, i);
			if (holder.itemView != null)
				layoutTravelLog.addView(holder.itemView);
		}
	}

	protected View getMissionSummaryView() {
		View summaryView = LayoutInflater.from(getSafeContext()).inflate(R.layout.item_mission_summary, null);

		ArrayList<ImageView> stampImageViewArray = new ArrayList<>();
		ImageView ivCompleteBg = (ImageView) summaryView.findViewById(R.id.iv_country_shape);
		ImageView ivMissionComplete = (ImageView) summaryView.findViewById(R.id.img_MissionComplete);
		TextView txtTime = (KPHTextView) summaryView.findViewById(R.id.txt_CompletionTime);
		TextView tvMissionStatus = (KPHTextView) summaryView.findViewById(R.id.txt_Completion_Status);
		TextView txtTitle = (KPHTextView) summaryView.findViewById(R.id.txt_Title);
		TextView txtMissionCompleteName = (KPHTextView) summaryView.findViewById(R.id.txt_mission_name);
		KPHImageTextButton tvSummaryPacket = (KPHImageTextButton) summaryView.findViewById(R.id.ivPacket);
		KPHImageTextButton tvSummaryPowerpoint = (KPHImageTextButton) summaryView.findViewById(R.id.ivPowerpoint);
		RelativeLayout layout_place = (RelativeLayout) summaryView.findViewById(R.id.layout_place);
		ProgressBarView missionPercent = (ProgressBarView) summaryView.findViewById(R.id.mission_progress);
		missionPercent.setVisibility(View.VISIBLE);
		View cell_divider = summaryView.findViewById(R.id.cell_divider);
		cell_divider.setVisibility(View.GONE);

		LinearLayout layoutDelightList = (LinearLayout) summaryView.findViewById(R.id.layout_delight_list);
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

			layoutDelightList.addView(vwDelightRow);
		}

		// Configure summary view
		KPHMissionInformation missionInfo = KPHMissionService.sharedInstance().getMissionInformationById(missionStats.getMissionId());
		KPHUserMissionStats missionStats = KPHMissionService.sharedInstance().userMissionStateById(this.missionStats.getMissionId());
		if (missionInfo == null || missionStats == null)
			return summaryView;

		List<KPHDelightUnlocked> stamps = missionStats.getDelightsUnlocked();
		ArrayList<Integer> unlockedDelights = new ArrayList<>();
		// filter only mission related delight
		if (stamps != null) {
			for (KPHDelightUnlocked delight : stamps) {
				unlockedDelights.add((int) delight.getDelightId());
			}
		}

		int percent = missionStats.getProgress();
		if (missionStats.isCompletedMission()) {

			// set MissionComplete Image
			ivMissionComplete.setImageDrawable(missionInfo.getCompleteDrawable());
			ivMissionComplete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (getParentActivity() == null)
						return;

					KPHMissionInformation missionInfo = KPHMissionService.sharedInstance().getMissionInformationById(missionId);

					InfoMissionFragment frag = new InfoMissionFragment();
					frag.setBackTitle(getSafeContext().getString(R.string.mission));
					frag.showTabBar(false);
					frag.setMissionInformation(missionInfo, true);
					getParentActivity().showNewFragment(frag);
				}
			});
			tvMissionStatus.setText("Mission complete!");
			percent = 100;// fixed for completed mission which not arrival goal.
			// set StartTime
			Date date = OSDate.fromUTCString(missionStats.getCompletedAt());

			SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
			SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

			try {
				txtTime.setText("MISSION COMPLETE! on " + dateFormat.format(date) + " at " + timeFormat.format(date));
			} catch (Exception e) {
				e.printStackTrace();
			}

			int packetGoal = missionStats.getMissionGoal() / 50 / 10;
			if (missionStats.getMissionPackets() < packetGoal) {
				Logger.error(TAG, "found incorrect completed mission, packet:%d(%d)", missionStats.getMissionPackets(), packetGoal);
			} else {
				packetGoal = missionStats.getMissionPackets();
			}
			tvSummaryPacket.setText("" + packetGoal);

			int pointGoal = missionStats.getMissionGoal() / 50;
			if (missionStats.getMissionPowerPoint() < pointGoal) {
				Logger.error(TAG, "found incorrect completed mission, point:%d(%d)", missionStats.getMissionPowerPoint(), pointGoal);
			} else {
				pointGoal = missionStats.getMissionPowerPoint();
			}
			tvSummaryPowerpoint.setText("" + pointGoal);

			unlockedDelights = missionInfo.getDelights();
		} else {
			ivMissionComplete.setVisibility(View.GONE);
			txtTime.setVisibility(View.GONE);
			layout_place.setVisibility(View.GONE);

			int packetGoal = missionStats.getMissionGoal() / 50 / 10;
			tvMissionStatus.setText(percent + "% toward " + packetGoal + " packet goal");

			tvSummaryPacket.setText(missionStats.getMissionPackets() + "/" + packetGoal);
			tvSummaryPowerpoint.setText(missionStats.getMissionPowerPoint() + "/" + missionStats.getMissionGoal() / 50);
		}

		// set Complete Background Image
		ivCompleteBg.setImageDrawable(missionInfo.getCountryDrawable());

		missionPercent.setPercentageFilled(percent);
		// set Stamp Images

		{
			int count = unlockedDelights.size();
			if (count > stampImageViewArray.size())
				count = stampImageViewArray.size();

			for (int i = 0; i < count; i++) {
				int delight = unlockedDelights.get(i);
				KPHDelightInformation delInfo = KPHMissionService.sharedInstance().getDelightInformationById(delight);

				if (delInfo == null || !delInfo.isMissionIdEquals(missionStats.getMissionId()))
					continue;

				Drawable drawable = delInfo.getImageDrawable();
				if (drawable != null)
					stampImageViewArray.get(i).setImageDrawable(drawable);
				stampImageViewArray.get(i).setTag(delInfo);
				stampImageViewArray.get(i).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Object tagObject = v.getTag();

						if (getParentActivity() == null)
							return;

						if (tagObject != null && tagObject instanceof KPHDelightInformation) {
							KPHDelightInformation delight = (KPHDelightInformation) tagObject;
							if (delight.getType().equals(KPHConstants.DELIGHT_POSTCARD)) {
								InfoShareFragment frag = new InfoShareFragment();
								frag.setBackTitle(getSafeContext().getString(R.string.mission));
								frag.showTabBar(false);
								frag.setDelightInformation(delight);
								getParentActivity().showNewFragment(frag);
							} else {
								InfoDelightFragment frag = new InfoDelightFragment();
								frag.setBackTitle(getSafeContext().getString(R.string.mission));
								frag.showTabBar(false);
								frag.setDelightInformation(delight, null);
								getParentActivity().showNewFragment(frag);
							}
						}
					}
				});
			}
		}

		// set Title
		txtTitle.setText(missionStats.getMissionName().toUpperCase());

		// set MissionCompleteName
		txtMissionCompleteName.setText(missionStats.getMissionName());

		return summaryView;
	}
}
