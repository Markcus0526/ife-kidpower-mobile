package org.unicefkidpower.kid_power.View.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.unicefkidpower.kid_power.View.CustomControls.KPHImageTextButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.Model.Structure.KPHBlock;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserSummary;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.UIManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by Ruifeng Shi on 10/26/2015.
 */
public class FollowListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	public static int DISPLAY_MODE_SUMMARY				= 0;
	public static int DISPLAY_MODE_SEVEN_DAYS_POINTS	= 1;

	private static final int VIEW_TYPE_FOLLOWER			= 1;
	private static final int VIEW_TYPE_BLOCKED			= 2;

	private List<KPHUserSummary> followersArray			= new ArrayList<>();
	private List<KPHBlock> blockedArray					= new ArrayList<>();

	private int displayMode								= DISPLAY_MODE_SUMMARY;

	private View.OnClickListener itemClickedListener	= null;
	private LayoutInflater inflater						= null;

	public FollowListAdapter(
			Context context,
			List<KPHUserSummary> followersArray,
			List<KPHBlock> blockedArray
	) {
		// Sort array of missions by their IDs
		Collections.sort(followersArray, new Comparator<KPHUserSummary>() {
			@Override
			public int compare(KPHUserSummary leftItem, KPHUserSummary rightItem) {
				return leftItem.getHandle().compareToIgnoreCase(rightItem.getHandle());
			}
		});

		this.followersArray = followersArray;
		if (blockedArray != null)
			this.blockedArray = blockedArray;

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	public void setDisplayMode(int displayMode) {
		this.displayMode = displayMode;

		if (DISPLAY_MODE_SUMMARY == displayMode) {
			Collections.sort(
					followersArray,
					new Comparator<KPHUserSummary>() {
						@Override
						public int compare(KPHUserSummary lhs, KPHUserSummary rhs) {
							return lhs.getHandle().compareToIgnoreCase(rhs.getHandle());
						}
					}
			);
		} else if (DISPLAY_MODE_SEVEN_DAYS_POINTS == displayMode) {
			Collections.sort(
					followersArray,
					new Comparator<KPHUserSummary>() {
						@Override
						public int compare(KPHUserSummary lhs, KPHUserSummary rhs) {
							if (lhs.getSevenDaysPowerPoints() != rhs.getSevenDaysPowerPoints()) {
								return rhs.getSevenDaysPowerPoints() - lhs.getSevenDaysPowerPoints();
							} else {
								return lhs.getHandle().compareToIgnoreCase(rhs.getHandle());
							}
						}
					}
			);
		}
	}

	public KPHUserSummary getItem(int position) {
		return followersArray.get(position);
	}

	@Override
	public int getItemCount() {
		return followersArray.size() + (blockedArray.size() > 0 ? 1 : 0);
	}

	@Override
	public int getItemViewType(int position) {
		if (position < followersArray.size())
			return VIEW_TYPE_FOLLOWER;

		return VIEW_TYPE_BLOCKED;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		RecyclerView.ViewHolder viewHolder = null;

		if (viewType == VIEW_TYPE_FOLLOWER) {
			// List for countries of missions
			View contentView = inflater.inflate(R.layout.item_user, parent, false);
			viewHolder = new FollowItemHolder(contentView);
		} else if (viewType == VIEW_TYPE_BLOCKED) {
			View contentView = inflater.inflate(R.layout.item_blocked, parent, false);
			viewHolder = new BlockedItemHolder(contentView);
		}

		if (viewHolder != null && itemClickedListener != null) {
			viewHolder.itemView.setOnClickListener(itemClickedListener);
		}

		return viewHolder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		holder.itemView.setTag(position);

		if (holder.getItemViewType() == VIEW_TYPE_FOLLOWER) {
			// List for countries of missions
			((FollowItemHolder)holder).setFollower(followersArray.get(position));
		} else {
			((BlockedItemHolder)holder).setBlockedNumber(blockedArray.size());
		}
	}


	public class FollowItemHolder extends RecyclerView.ViewHolder {
		public ImageView				ivAvatar;
		public KPHTextView				txtHandle;
		public KPHTextView				txtMissionState;
		public KPHImageTextButton		btnMissionsCompleted;
		public KPHImageTextButton		btnPowerpoint;
		public KPHImageTextButton		btnPacket;


		public FollowItemHolder(View itemView) {
			super(itemView);

			ivAvatar = (ImageView) itemView.findViewById(R.id.ivAvatar);
			txtHandle = (KPHTextView) itemView.findViewById(R.id.txt_handle);
			txtMissionState = (KPHTextView) itemView.findViewById(R.id.txt_mission_state);
			btnPacket = (KPHImageTextButton) itemView.findViewById(R.id.iv_packet);
			btnMissionsCompleted = (KPHImageTextButton) itemView.findViewById(R.id.iv_missions_completed);
			btnPowerpoint = (KPHImageTextButton) itemView.findViewById(R.id.iv_powerpoint);
		}


		public void setFollower(KPHUserSummary follower) {
			Drawable drawable = KPHUserService.sharedInstance().getAvatarDrawable(follower.getAvatarId());
			if (drawable != null) {
				ivAvatar.setImageDrawable(drawable);
			} else {
				ivAvatar.setImageDrawable(
						UIManager.sharedInstance().getImageDrawable(R.drawable.avatar_placeholder)
				);
			}

			txtHandle.setText(follower.getHandle());
			if (follower.getCurrentMission() != null && follower.getCurrentMission().length() > 0) {
				txtMissionState.setText("in " + follower.getCurrentMission());
				txtMissionState.setVisibility(View.VISIBLE);
			} else {
				txtMissionState.setVisibility(View.GONE);
			}

			if (DISPLAY_MODE_SUMMARY == displayMode) {
				btnPacket.setNumericText(follower.getTotalPackets());
				btnMissionsCompleted.setNumericText(follower.getMissionsCompleted());
				btnPowerpoint.setNumericText(follower.getTotalPoints());

				btnPacket.setVisibility(View.VISIBLE);
				btnMissionsCompleted.setVisibility(View.VISIBLE);
				btnPowerpoint.setVisibility(View.VISIBLE);
			} else if (DISPLAY_MODE_SEVEN_DAYS_POINTS == displayMode) {
				btnPowerpoint.setNumericText(follower.getSevenDaysPowerPoints());

				btnPacket.setVisibility(View.GONE);
				btnMissionsCompleted.setVisibility(View.GONE);
				btnPowerpoint.setVisibility(View.VISIBLE);
			}
		}
	}


	public class BlockedItemHolder extends RecyclerView.ViewHolder {
		public KPHTextView txtBlockedNumber;

		public BlockedItemHolder(View itemView) {
			super(itemView);
			txtBlockedNumber = (KPHTextView) itemView.findViewById(R.id.txt_blocked_number);
		}

		public void setBlockedNumber(int nBlockedNumber) {
			if (txtBlockedNumber != null)
				txtBlockedNumber.setText("(" + nBlockedNumber + ")");
		}
	}


	public void setOnItemClickedListener(View.OnClickListener listener) {
		itemClickedListener = listener;
	}

}
