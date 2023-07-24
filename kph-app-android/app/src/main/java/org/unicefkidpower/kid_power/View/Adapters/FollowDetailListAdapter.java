package org.unicefkidpower.kid_power.View.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Model.Structure.KPHMission;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMission;
import org.unicefkidpower.kid_power.View.CustomControls.KPHImageTextButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.Model.Structure.KPHMissionInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMissionStats;
import org.unicefkidpower.kid_power.Model.MicroService.KPHMissionService;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.UIManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by Ruifeng Shi on 9/22/2015.
 */
public class FollowDetailListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private final String TAG					= "FollowDetailListAdapter";

	private final int VIEW_TYPE_HEADER			= 1;
	private final int VIEW_TYPE_ITEM			= 2;

	private ArrayList<KPHUserMissionStats>		userMissionStats = new ArrayList<>();
	private LayoutInflater						inflater = null;
	private View								headerView = null;


	public FollowDetailListAdapter(Context context) {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Set the list of mission stats for this user.
	 *
	 * @param userMissionStats Array of mission stats.
	 */
	public void setUserMissionStats(List<KPHUserMissionStats> userMissionStats) {
		if (userMissionStats == null)
			return;

		this.userMissionStats = new ArrayList<>();

		// Move the current mission ahead of the list.
		KPHUserMissionStats currentMission = null;
		for (KPHUserMissionStats missionStats : userMissionStats) {
			if (missionStats == null || !(missionStats.isStartedMission() || missionStats.isCompletedMission())) {
				continue;
			}

			if (missionStats.isStartedMission() && !missionStats.isCompletedMission()) {
				currentMission = missionStats;
			} else {
				this.userMissionStats.add(missionStats);
			}
		}

		Collections.sort(
				this.userMissionStats,
				new Comparator<KPHUserMissionStats>() {
					@Override
					public int compare(KPHUserMissionStats lhs, KPHUserMissionStats rhs) {
						return (int) rhs.getMissionId() - (int) lhs.getMissionId();
					}
				}
		);

		if (currentMission != null) {
			this.userMissionStats.add(0, currentMission);
		}

		notifyDataSetChanged();
	}


	public void setHeaderView(View headerView) {
		this.headerView = headerView;
	}


	@Override
	public int getItemViewType(int position) {
		if (position == 0)
			return VIEW_TYPE_HEADER;

		return VIEW_TYPE_ITEM;
	}


	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		RecyclerView.ViewHolder holder = null;

		if (viewType == VIEW_TYPE_HEADER) {
			holder = new HeaderViewHolder(headerView);
		} else if (viewType == VIEW_TYPE_ITEM) {
			View contentView = inflater.inflate(R.layout.item_follow_mission, parent, false);
			holder = new MissionItemHolder(contentView);
		}

		return holder;
	}


	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
			((MissionItemHolder) holder).setMissionStats(userMissionStats.get(position - 1));
		}
	}


	@Override
	public int getItemCount() {
		return userMissionStats.size() + 1;
	}


	public class HeaderViewHolder extends RecyclerView.ViewHolder {
		public HeaderViewHolder(View itemView) {
			super(itemView);
		}
	}


	public class MissionItemHolder extends RecyclerView.ViewHolder {
		public View					background;
		public LinearLayout			layoutMissionInfo;
		public ImageView			ivMissionCountry;
		public KPHTextView			txtMissionName;
		public KPHTextView			txtStatus;
		public KPHImageTextButton	btnPacket;
		public KPHImageTextButton	btnPowerPoint;

		public MissionItemHolder(View itemView) {
			super(itemView);

			background = itemView.findViewById(R.id.layout_mission);
			ivMissionCountry = (ImageView) itemView.findViewById(R.id.ivMissionCountry);
			txtMissionName = (KPHTextView) itemView.findViewById(R.id.txtMissionName);
			txtStatus = (KPHTextView) itemView.findViewById(R.id.txtStatus);
			layoutMissionInfo = (LinearLayout) itemView.findViewById(R.id.layout_missionInfo);
			btnPacket = (KPHImageTextButton) itemView.findViewById(R.id.ivPacket);
			btnPowerPoint = (KPHImageTextButton) itemView.findViewById(R.id.ivPowerpoint);
			layoutMissionInfo.setVisibility(View.GONE);
		}

		public void setMissionStats(KPHUserMissionStats missionStats) {
			KPHMissionInformation mission = KPHMissionService.sharedInstance().getMissionInformationById(
					missionStats.getMissionId()
			);
			KPHUserMission orgMissionInfo = KPHMissionService.sharedInstance().userMissionById(missionStats.getMissionId());

			Drawable drawable = mission.getCountryDrawable();
			if (drawable != null)
				ivMissionCountry.setImageDrawable(drawable);
			txtMissionName.setText(mission.name());

			ivMissionCountry.setAlpha(1.0f);

			background.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_main_background));
			layoutMissionInfo.setVisibility(View.GONE);

			String status = "";
			if (missionStats.isCompletedMission()) {
				if (orgMissionInfo != null) {
					status = orgMissionInfo.getCalloutMessage();
					Logger.log(TAG, "Mission status CalloutMessage : " + status);
				} else {
					status = "Mission Complete!";
				}

				btnPacket.setNumericText(missionStats.getMissionPackets());
				btnPowerPoint.setNumericText((int) missionStats.getMissionCalories() / 50);
			} else if (missionStats.isStartedMission()) {
				int percent = missionStats.getProgress();
				int packetGoal = missionStats.getMissionGoal() / 50 / 10;
				status = percent + "% toward " + packetGoal + " packet goal";

				background.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_green));

				btnPacket.setText(missionStats.getMissionPackets() + "/" + packetGoal);
				btnPowerPoint.setText((int) missionStats.getMissionCalories() / 50 + "/" + missionStats.getMissionGoal() / 50);
				layoutMissionInfo.setVisibility(View.VISIBLE);
			} else {
				if (orgMissionInfo != null) {
					status = orgMissionInfo.getCalloutMessage();
					Logger.log(TAG, "Mission status CalloutMessage : " + status);
				} else {
					if (missionStats.isUnlockedMission()) {
						status = "Start here!";
					} else {
						status = "new!";
					}
				}
			}
/*
			} else if (missionStats.isUnlockedMission()) {
				status = "Start here!";
			} else {
				status = "new!";
			}
*/
			txtStatus.setText(status);
		}
	}
}
