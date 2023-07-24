package org.unicefkidpower.kid_power.View.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.View.CustomControls.KPHImageTextButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.Model.Structure.KPHMission;
import org.unicefkidpower.kid_power.Model.Structure.KPHMissionInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMissionStats;
import org.unicefkidpower.kid_power.Model.MicroService.KPHMissionService;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.UIManager;

import java.util.ArrayList;

/**
 * Created by Ruifeng Shi on 9/22/2015.
 */
public class PassportListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private final String TAG				= "PassportListAdapter";

	private final int VIEW_TYPE_HEADER		= 1;
	private final int VIEW_TYPE_ITEM		= 2;

	private ArrayList<KPHMission>			missionArray;
	private LayoutInflater					inflater = null;
	private KPHMissionService				serviceForMission;
	private View							headerView = null;
	private View.OnClickListener			itemClickListener = null;

	public PassportListAdapter(Context context, ArrayList<KPHMission> missionArray) {
		this.serviceForMission = KPHMissionService.sharedInstance();
		this.missionArray = missionArray;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getItemCount() {
		int count = missionArray.size();
		if (headerView != null)
			count++;

		return count;
	}

	@Override
	public int getItemViewType(int position) {
		if (headerView != null) {
			if (position == 0) {
				return VIEW_TYPE_HEADER;
			} else {
				return VIEW_TYPE_ITEM;
			}
		} else {
			return VIEW_TYPE_ITEM;
		}
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		RecyclerView.ViewHolder holder;

		if (viewType == VIEW_TYPE_HEADER) {
			holder = new HeaderViewHolder(headerView);
		} else {
			View contentView = inflater.inflate(R.layout.item_passport_mission, null, false);

			holder = new MissionItemHolder(contentView);
			if (itemClickListener != null)
				holder.itemView.setOnClickListener(itemClickListener);
		}

		return holder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		holder.itemView.setTag(position);

		if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
			if (headerView != null) {
				((MissionItemHolder) holder).setMission(missionArray.get(position - 1));
			} else {
				((MissionItemHolder) holder).setMission(missionArray.get(position));
			}
		}
	}

	public void updateData(ArrayList<KPHMission> missionArray) {
		this.missionArray = missionArray;
		notifyDataSetChanged();
	}

	public void setHeaderView(View v) {
		headerView = v;
	}

	public void setOnItemClickedListener(View.OnClickListener listener) {
		this.itemClickListener = listener;
	}

	public KPHMission getItem(int position) {
		return missionArray.get(position);
	}

	public class HeaderViewHolder extends RecyclerView.ViewHolder {
		public HeaderViewHolder(View itemView) {
			super(itemView);
		}
	}

	public class MissionItemHolder extends RecyclerView.ViewHolder {
		public View						background;
		public ImageView				ivMissionCountry;
		public ImageView				ivDisclosure;
		public KPHTextView				txtMissionName;
		public KPHTextView				txtStatus;
		public KPHImageTextButton		btnPacket;
		public KPHImageTextButton		btnPowerPoint;


		public MissionItemHolder(View itemView) {
			super(itemView);

			background = itemView.findViewById(R.id.layout_mission);
			ivMissionCountry = (ImageView) itemView.findViewById(R.id.ivMissionCountry);
			ivDisclosure = (ImageView) itemView.findViewById(R.id.ivDisclosure);
			txtMissionName = (KPHTextView) itemView.findViewById(R.id.txtMissionName);
			txtStatus = (KPHTextView) itemView.findViewById(R.id.txtStatus);
			btnPacket = (KPHImageTextButton) itemView.findViewById(R.id.ivPacket);
			btnPacket.setVisibility(View.GONE);
			btnPowerPoint = (KPHImageTextButton) itemView.findViewById(R.id.ivPowerpoint);
			btnPowerPoint.setVisibility(View.GONE);
		}

		public void setMission(KPHMission mission) {
			KPHMissionInformation missionInformation = serviceForMission.getMissionInformationById(mission.getId());

			Drawable drawable = missionInformation.getCountryDrawable();
			if (drawable != null)
				ivMissionCountry.setImageDrawable(drawable);
			txtMissionName.setText(missionInformation.name());

			KPHUserMissionStats userMissionStats = serviceForMission.userMissionStateById(missionInformation.missionId());
			if (userMissionStats != null) {
				ivMissionCountry.setAlpha(1.0f);
				ivDisclosure.setAlpha(1.0f);
			} else {
				ivMissionCountry.setAlpha(0.6f);
				ivDisclosure.setAlpha(0.6f);
			}

			background.setBackgroundColor(Color.TRANSPARENT);
			btnPacket.setVisibility(View.GONE);
			btnPowerPoint.setVisibility(View.GONE);

			String status = "";
			if (userMissionStats == null) {
				status = mission.getCalloutMessage();
				Logger.log(TAG, "Callout Message : " + status);
			} else if (userMissionStats.isCompletedMission()) {
				status = mission.getCalloutMessage();
				Logger.log(TAG, "Callout Message : " + status);

				btnPacket.setNumericText(userMissionStats.getMissionPackets());
				btnPacket.setVisibility(View.VISIBLE);
				btnPowerPoint.setNumericText((int) userMissionStats.getMissionCalories() / 50);
				btnPowerPoint.setVisibility(View.VISIBLE);
			} else if (userMissionStats.isStartedMission()) {
				int percent = userMissionStats.getProgress();
				int packetGoal = userMissionStats.getMissionGoal() / 50 / 10;
				status = percent + "% toward " + packetGoal + " packet goal";

				background.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_green));

				btnPacket.setText(userMissionStats.getMissionPackets() + "/" + packetGoal);
				btnPacket.setVisibility(View.VISIBLE);
				btnPowerPoint.setText((int) userMissionStats.getMissionCalories() / 50 + "/" + userMissionStats.getMissionGoal() / 50);
				btnPowerPoint.setVisibility(View.VISIBLE);
			} else {
				status = mission.getCalloutMessage();
				Logger.log(TAG, "Callout Message : " + status);
			}
/*
			} else if (userMissionStats.isUnlockedMission()) {
				status = "Start here!";
			} else {
				status = "new!";
			}
*/
			txtStatus.setText(status);
		}
	}
}
