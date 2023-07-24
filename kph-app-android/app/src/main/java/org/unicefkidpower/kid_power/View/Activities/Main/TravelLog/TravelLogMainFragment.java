package org.unicefkidpower.kid_power.View.Activities.Main.TravelLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Model.MicroService.KPHAnalyticsService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHMissionService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserTravelLog;
import org.unicefkidpower.kid_power.Model.Structure.MissionLog;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.Adapters.MissionLogAdapter;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ruifeng Shi on 9/4/2015.
 */
public class TravelLogMainFragment extends SuperFragment {
	private final String				TAG = "TravelLogMainFragment";
	public static final String			TRAVEL_LOG_ID = "TravelLogID";

	// UI Controls
	protected View						contentView				= null;

	protected RecyclerView				travelLogListView		= null;
	protected MissionLogAdapter			missionLogAdapter		= null;

	protected View						emptyCommentView		= null;

	private IntentFilter				intentFilter			= null;
	protected List<MissionLog>			missionLogs				= new ArrayList<>();

	private final int					RETRY_COUNT_LIMIT		= 10;
	private int							retryCount				= RETRY_COUNT_LIMIT;

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
				filterTravelLogList(travelLogs);
			} else if (action.equals(KPHBroadcastSignals.BROADCAST_SIGNAL_TRAVEL_LOG_READ_2)) {
				long missionId = intent.getLongExtra(TRAVEL_LOG_ID, 0);
				Logger.log(TAG, "Mission ID For Set Read Flag: " + missionId);

				for (MissionLog missionLog : missionLogs) {
					if (missionLog.getMissionId() == missionId) {
						missionLog.setUnread(false);
					}
				}

				missionLogAdapter.notifyDataSetChanged();
			}
		}
	};


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_UI_TRAVEL_LOG);

		// initialize list view item
		missionLogAdapter = new MissionLogAdapter(getSafeContext(), missionLogs, getSafeContext().getString(R.string.travel_log));
		missionLogAdapter.setOnMissionLogItemClickListener(new MissionLogAdapter.OnMissionLogItemClickListener() {
			@Override
			public void onClickMissionLogItem(MissionLog missionLog) {
				retryCount = RETRY_COUNT_LIMIT;
				onMarkedMissionLog(missionLog);
			}
		});


		// Travel Log List
		travelLogListView = (RecyclerView) contentView.findViewById(R.id.list_travellog);
		travelLogListView.setAdapter(missionLogAdapter);
		travelLogListView.setLayoutManager(new LinearLayoutManager(getSafeContext(), LinearLayoutManager.VERTICAL, false));


		// Travel Log Empty View
		emptyCommentView = contentView.findViewById(R.id.layout_travel_log_empty_comment);

		if (intentFilter == null) {
			intentFilter = new IntentFilter();
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_TRAVEL_LOG_UPDATED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_TRAVEL_LOG_READ_2);
			LocalBroadcastManager.getInstance(getSafeContext()).registerReceiver(receiver, intentFilter);
		}

		List<KPHUserTravelLog> travelLogs = KPHMissionService.sharedInstance().userTravelLogs();
		filterTravelLogList(travelLogs);

		return contentView;
	}


	@Override
	public void onResume() {
		super.onResume();

		if (getParentActivity() != null) {
			loadTravelLogsIfNeeded();
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);

		if (getParentActivity() != null && !hidden) {
			loadTravelLogsIfNeeded();
		}
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_travel_log;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		LocalBroadcastManager.getInstance(getSafeContext()).unregisterReceiver(receiver);
		intentFilter = null;
	}


	private void onMarkedMissionLog(final MissionLog missionLog) {
		Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_TRAVEL_LOG_READ_1);
		intent.putExtra(TRAVEL_LOG_ID, missionLog.getMissionId());
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);

		KPHMissionService.sharedInstance().markTravelLogItemAsRead(missionLog, new KPHMissionService.OnTravelLogMarkListener() {
			@Override
			public void onSuccess() {
				Logger.log(TAG, "mark Travel Log success");
			}

			@Override
			public void onFailure() {
				if (retryCount < 0) {
					Logger.log(TAG, "mark Travel Log failed. finish");
					return;
				}

				retryCount--;
				Logger.log(TAG, "mark Travel Log failed. retryCount : " + retryCount);
				onMarkedMissionLog(missionLog);
			}
		});
	}


	private void loadTravelLogsIfNeeded() {
		if (KPHMissionService.sharedInstance().userTravelLogs() != null)
			return;

		retryCount = RETRY_COUNT_LIMIT;
		loadTravelLog();
	}


	/**
	 * Get Travel Log from Server
	 */
	private void loadTravelLog() {
		if (getParentActivity() == null)
			return;

		KPHMissionService missionService = KPHMissionService.sharedInstance();
		if (missionService == null)
			return;

		List<KPHUserTravelLog> logs = missionService.userTravelLogs();
		if (logs == null) {
			// maybe 1st load
			showProgressDialog();
		} else {
			filterTravelLogList(logs);
		}

		KPHUserData userData = KPHUserService.sharedInstance().getUserData();
		KPHMissionService.sharedInstance().loadUserTravelLog(userData.getId(), new KPHMissionService.OnLoadUserTravelLog() {
			@Override
			public void onSuccess() {
				LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(
						new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_TRAVEL_LOG_UPDATED)
				);
			}

			@Override
			public void onFailure(String message) {
				if (retryCount < 0) {
					showErrorDialog(message);
					return;
				}

				retryCount--;
				loadTravelLog();
			}
		});
	}

	/**
	 * filter all travel logs for supported mission
	 *
	 * @param travelLogs
	 */
	private void filterTravelLogList(List<KPHUserTravelLog> travelLogs) {
		missionLogs.clear();
		// filter by user mission
		if (travelLogs != null) {
			for (KPHUserTravelLog log : travelLogs) {
				if (!KPHMissionService.sharedInstance().isSupportedMission(log.getMissionId()))
					continue;

				missionLogs.add(new MissionLog(log));
			}
		}

		// update UI
		missionLogAdapter.notifyDataSetChanged();

		if (missionLogs.size() > 0) {
			showTravelLogList();
		} else {
			showTravelLogEmptyComment();
		}
	}


	protected void showTravelLogEmptyComment() {
		emptyCommentView.setVisibility(View.VISIBLE);
		travelLogListView.setVisibility(View.GONE);
	}

	protected void showTravelLogList() {
		emptyCommentView.setVisibility(View.GONE);
		travelLogListView.setVisibility(View.VISIBLE);
	}

}
