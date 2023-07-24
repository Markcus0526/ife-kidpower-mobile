package org.unicefkidpower.kid_power.View.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.Model.Structure.KPHCheerInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHDelightInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHMissionInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserTravelLog;
import org.unicefkidpower.kid_power.Model.Structure.MissionLog;
import org.unicefkidpower.kid_power.View.Activities.Main.Passport.InfoDelightFragment;
import org.unicefkidpower.kid_power.View.Activities.Main.Passport.InfoMissionFragment;
import org.unicefkidpower.kid_power.View.Activities.Main.Passport.InfoShareFragment;
import org.unicefkidpower.kid_power.View.Super.SuperTabActivity;
import org.unicefkidpower.kid_power.View.Activities.Main.TravelLog.InfoCheerFragment;
import org.unicefkidpower.kid_power.Model.MicroService.KPHMissionService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.OSDate;
import org.unicefkidpower.kid_power.Misc.UIManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Dayong Li on 3/1/2016.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud Org
 * dayong@CalorieCloud.Org
 */
public class MissionLogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private final float READ_ITEM_TRANSPARENCY		= 0.75f;

	public static final int VIEW_TYPE_HEADER		= 1;
	public static final int VIEW_TYPE_ITEM			= 2;

	protected SuperTabActivity						activity;
	protected List<MissionLog>						missionLogs = null;
	protected View									headerView = null;
	private OnMissionLogItemClickListener			onMissionLogItemClickListener;
	private LayoutInflater							inflater;
	private String									backTitle = "";


	public MissionLogAdapter(Context context, List<MissionLog> logs, String backTitle) {
		this.activity = (SuperTabActivity) context;
		this.inflater = LayoutInflater.from(context);
		this.missionLogs = logs;;
		this.backTitle = backTitle;
	}

	@Override
	public int getItemCount() {
		int count = 0;

		if (missionLogs != null)
			count += missionLogs.size();

		if (headerView != null)
			count++;

		return count;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		if (headerView != null) {
			if (position == 0) {
				return VIEW_TYPE_HEADER;
			} else {
				return VIEW_TYPE_ITEM;
			}
		}

		return VIEW_TYPE_ITEM;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		RecyclerView.ViewHolder holder;

		if (viewType == VIEW_TYPE_HEADER) {
			holder = new HeaderViewHolder(headerView);
		} else {
			View contentView = inflater.inflate(R.layout.item_travellog, parent, false);

			holder = new LogItemViewHolder(contentView);
			holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickedItem(v);
				}
			});
		}

		return holder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		holder.itemView.setTag(position);

		if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
			MissionLog oneLog;
			if (headerView != null) {
				oneLog = missionLogs.get(position - 1);
			} else {
				oneLog = missionLogs.get(position);
			}

			((LogItemViewHolder)holder).setMissionLog(oneLog);
		}
	}

	public void onClickedItem(View v) {
		int position = (Integer)v.getTag();
		if (headerView != null)
			position--;

		MissionLog oneLog = missionLogs.get(position);
		if (oneLog.isUnread()) {
			if (onMissionLogItemClickListener != null) {
				onMissionLogItemClickListener.onClickMissionLogItem(oneLog);
				oneLog.setUnread(false);

				// To update state on Global variable.
				List<KPHUserTravelLog> travelLogsAll = KPHMissionService.sharedInstance().userTravelLogs();
				for (KPHUserTravelLog travelLogItem : travelLogsAll) {
					if (travelLogItem.getId() == oneLog.getId()) {
						travelLogItem.setRead(true);
					}
				}

				v.setAlpha(READ_ITEM_TRANSPARENCY);
			}
		}

		if (oneLog.getDelightType().equalsIgnoreCase(KPHUserTravelLog.TYPE_CUSTOM_CHEER) ||
				oneLog.getType() == KPHUserTravelLog.LogTypeFlag_Cheer) {
			InfoCheerFragment frag = new InfoCheerFragment();
			frag.setBackTitle(backTitle);
			frag.showTabBar(false);
			frag.setInformation(
					getDelightImageDrawable(oneLog),
					oneLog.getSender(),
					"",
					false,
					oneLog.getType() == MissionLog.Log_Snapshot
			);
			activity.showNewFragment(frag);
			return;
		}

		KPHDelightInformation delInfo = KPHMissionService.sharedInstance().getDelightInformationById(oneLog.getDelightId());
		if (delInfo != null) {
			if (delInfo.getType().equals(KPHConstants.DELIGHT_POSTCARD)) {
				InfoShareFragment frag = new InfoShareFragment();
				frag.setBackTitle(backTitle);
				frag.showTabBar(false);
				frag.setDelightInformation(delInfo);
				activity.showNewFragment(frag);
			} else {
				InfoDelightFragment frag = new InfoDelightFragment();
				frag.setBackTitle(backTitle);
				frag.showTabBar(false);
				frag.setDelightInformation(delInfo, null);
				activity.showNewFragment(frag);
			}

			return;
		}

		if (oneLog.getType() == KPHUserTravelLog.LogTypeFlag_MissionStart ||
				oneLog.getType() == KPHUserTravelLog.LogTypeFlag_MissionFinish) {
			KPHMissionInformation missionInfo = KPHMissionService.sharedInstance().getMissionInformationById(oneLog.getMissionId());

			InfoMissionFragment frag = new InfoMissionFragment();
			frag.setBackTitle(backTitle);
			frag.showTabBar(false);
			frag.setMissionInformation(missionInfo, oneLog.getType() == KPHUserTravelLog.LogTypeFlag_MissionFinish);

			activity.showNewFragment(frag);
		} else {
			KPHDelightInformation delightInformation = new KPHDelightInformation();
			delightInformation.setName(getDelightName(oneLog));
			delightInformation.setDescription(getDelightDescription(oneLog));

			InfoDelightFragment frag = new InfoDelightFragment();
			frag.setBackTitle(backTitle);
			frag.showTabBar(false);
			frag.setDelightInformation(delightInformation, getDelightImageDrawable(oneLog));
			activity.showNewFragment(frag);
		}
	}

	public void setOnMissionLogItemClickListener(OnMissionLogItemClickListener onMissionLogItemClickListener) {
		this.onMissionLogItemClickListener = onMissionLogItemClickListener;
	}

	public void setHeaderView(View view) {
		headerView = view;
	}

	public class HeaderViewHolder extends RecyclerView.ViewHolder {
		public HeaderViewHolder(View itemView) {
			super(itemView);
		}
	}

	public class LogItemViewHolder extends RecyclerView.ViewHolder {
		private ImageView		ivDelight;
		private KPHTextView		txtDelightDescription, txtName, txtTimestamp;

		public LogItemViewHolder(View itemView) {
			super(itemView);

			ivDelight = (ImageView) itemView.findViewById(R.id.iv_delight);
			txtDelightDescription = (KPHTextView) itemView.findViewById(R.id.txt_delight_description);
			txtName = (KPHTextView) itemView.findViewById(R.id.txt_delight_name);
			txtTimestamp = (KPHTextView) itemView.findViewById(R.id.txt_timestamp);
		}

		public void setMissionLog(MissionLog oneLog) {
			ivDelight.setImageDrawable(getDelightImageDrawable(oneLog));
			txtTimestamp.setText(getTimestampText(oneLog));
			txtName.setText(getDelightName(oneLog));
			txtDelightDescription.setText(getDelightDescription(oneLog));

			if (!oneLog.isUnread()) {
				itemView.setAlpha(READ_ITEM_TRANSPARENCY);
			} else {
				itemView.setAlpha(1.0f);
			}
		}
	}

	public interface OnMissionLogItemClickListener {
		void onClickMissionLogItem(MissionLog missionLog);
	}

	public static Drawable getDelightImageDrawable(MissionLog oneLog) {
		Drawable drawable;

		if (oneLog.getDelightType().equalsIgnoreCase(KPHUserTravelLog.TYPE_CUSTOM_CHEER)) {
			KPHCheerInformation cheerInfo = KPHMissionService.sharedInstance().getCheerInformation(oneLog.getCheerId());
			int cheerId = (int) cheerInfo.getId();
			if (oneLog.getType() == MissionLog.Log_Snapshot)
				cheerId = (int) oneLog.getDelightId();

			drawable = KPHUserService.sharedInstance().getCustomCheerDrawable(cheerId);
		} else {
			KPHDelightInformation delInfo = KPHMissionService.sharedInstance().getDelightInformationById(oneLog.getDelightId());

			if (delInfo != null)
				drawable = delInfo.getImageDrawable();
			else if (!TextUtils.isEmpty(oneLog.getCheerAvatar())) {
				drawable = KPHUserService.sharedInstance().getAvatarDrawable(oneLog.getCheerAvatar());
			} else if (oneLog.getType() == KPHUserTravelLog.LogTypeFlag_MissionStart ||
					oneLog.getType() == KPHUserTravelLog.LogTypeFlag_MissionFinish) {
				drawable = UIManager.sharedInstance().getImageDrawable(R.drawable.bl_video_generic);
			} else {
				drawable = null;
			}
		}

		if (drawable == null)
			drawable = UIManager.sharedInstance().getImageDrawable(R.drawable.souvenir_placeholder);

		return drawable;
	}


	public String getDelightName(MissionLog oneLog) {
		if (oneLog.getDelightType().equalsIgnoreCase(KPHUserTravelLog.TYPE_CUSTOM_CHEER) ||
				oneLog.getType() == KPHUserTravelLog.LogTypeFlag_Cheer) {
			if (oneLog.getType() == MissionLog.Log_Snapshot) {
				return "You unlocked a new Cheer!";
			} else {
				return oneLog.getSender() + " cheered you!";
			}
		} else if (oneLog.getType() == KPHUserTravelLog.LogTypeFlag_MissionStart ||
				oneLog.getType() == KPHUserTravelLog.LogTypeFlag_MissionFinish) {
			KPHMissionInformation missionInfo = KPHMissionService.sharedInstance().getMissionInformationById(oneLog.getMissionId());
			if (missionInfo != null)
				return missionInfo.name();
		}

		return oneLog.getDelightName();
	}


	public static String getTimestampText(MissionLog oneLog) {
		Date earnedDate = OSDate.fromUTCString(oneLog.getLogDate());

		SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

		try {
			return dateFormat.format(earnedDate) + " at " + timeFormat.format(earnedDate);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}


	public static String getDelightDescription(MissionLog oneLog) {
		if (oneLog.getDelightType().equalsIgnoreCase(KPHUserTravelLog.TYPE_CUSTOM_CHEER) ||
				oneLog.getDelightType().equalsIgnoreCase(KPHUserTravelLog.TYPE_CHEER)) {
			if (oneLog.getType() == MissionLog.Log_Snapshot) {
				return "Cheer Unlocked";
			} else {
				return "Cheer Received";
			}
		} else {
			KPHDelightInformation delInfo = KPHMissionService.sharedInstance().getDelightInformationById(oneLog.getDelightId());
			String delType;

			if (delInfo != null) {
				delType = delInfo.getType();
			} else {
				delType = oneLog.getDelightType();
			}

			if (delType == null) {
				return "Stamp unlocked";
			} else if (delType.equals(KPHConstants.DELIGHT_POSTCARD)) {
				return "Postcard unlocked";
			} else if (delType.equals(KPHConstants.DELIGHT_DISTANCE)) {
				return "Distance unlocked";
			} else if (delType.equals(KPHConstants.DELIGHT_RUTF)) {
				return "PACKETS unlocked";
			} else if (delType.equals(KPHConstants.DELIGHT_KPP)) {
				return "POINTS unlocked";
			} else if (delType.equals(KPHConstants.DELIGHT_STAMP)) {
				return "Souvenir Unlocked";
			} else if (oneLog.getType() == KPHUserTravelLog.LogTypeFlag_MissionStart) {
				return "Mission Started";
			} else if (oneLog.getType() == KPHUserTravelLog.LogTypeFlag_MissionFinish) {
				return "Mission Complete";
			} else {
				return "STAMP unlocked";
			}
		}
	}

}
