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
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserSummary;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.UIManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Ruifeng Shi on 10/26/2015.
 */
public class FamilyAccountListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	public static int DISPLAY_MODE_SUMMARY				= 0;
	public static int DISPLAY_MODE_SEVEN_DAYS_POINTS	= 1;

	private static final int VIEW_TYPE_HEADER			= 1;
	private static final int VIEW_TYPE_ITEM				= 2;

	private Context context								= null;

	private KPHUserData userData						= null;
	private ArrayList<KPHUserSummary> familyAccounts	= new ArrayList<>();

	private int displayMode								= DISPLAY_MODE_SUMMARY;
	private boolean showDisclosure						= false;

	private LayoutInflater inflater						= null;

	private int headerItemCount							= 0;
	private View.OnClickListener itemClickListener		= null;


	public FamilyAccountListAdapter(
			Context inContext,
			KPHUserData inUserData
	) {
		this.context = inContext;
		this.userData = inUserData;

		if (userData.getParent() != null) {
			familyAccounts.add(userData.getParent());
		}

		if (userData.getChildren() != null) {
			familyAccounts.addAll(userData.getChildren());
		}

		if (userData.getSiblings() != null) {
			familyAccounts.addAll(userData.getSiblings());
		}

		Collections.sort(familyAccounts, new Comparator<KPHUserSummary>() {
			@Override
			public int compare(KPHUserSummary lhs, KPHUserSummary rhs) {
				return lhs.getHandle().compareToIgnoreCase(rhs.getHandle());
			}
		});

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setDisplayMode(int displayMode) {
		this.displayMode = displayMode;

		if (DISPLAY_MODE_SUMMARY == displayMode) {
			Collections.sort(
					familyAccounts,
					new Comparator<KPHUserSummary>() {
						@Override
						public int compare(KPHUserSummary lhs, KPHUserSummary rhs) {
							return lhs.getHandle().compareToIgnoreCase(rhs.getHandle());
						}
					}
			);
		} else if (DISPLAY_MODE_SEVEN_DAYS_POINTS == displayMode) {
			Collections.sort(
					familyAccounts,
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


	public void setUserData(KPHUserData userData) {
		this.userData = userData;

		familyAccounts = new ArrayList<>();
		if (userData.getParent() != null) {
			familyAccounts.add(userData.getParent());
		}

		if (userData.getChildren() != null) {
			familyAccounts.addAll(userData.getChildren());
		}

		if (userData.getSiblings() != null) {
			familyAccounts.addAll(userData.getSiblings());
		}

		Collections.sort(familyAccounts, new Comparator<KPHUserSummary>() {
			@Override
			public int compare(KPHUserSummary lhs, KPHUserSummary rhs) {
				return lhs.getHandle().compareToIgnoreCase(rhs.getHandle());
			}
		});

		notifyDataSetChanged();
	}


	public KPHUserSummary getItemAtPosition(int position) {
		return familyAccounts.get(position);
	}


	public void setHeaderItemCount(int count) {
		headerItemCount = count;
	}


	public int getHeaderItemCount() {
		return headerItemCount;
	}


	@Override
	public int getItemCount() {
		return familyAccounts.size() + headerItemCount;
	}


	@Override
	public int getItemViewType(int position) {
		if (position < headerItemCount)
			return VIEW_TYPE_HEADER;

		return VIEW_TYPE_ITEM;
	}


	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		RecyclerView.ViewHolder holder;

		if (viewType == VIEW_TYPE_HEADER) {
			holder = new FamilyAccountHeaderHolder(inflater.inflate(R.layout.layout_family_accounts_list_header, parent, false));
		} else {
			View contentView = inflater.inflate(R.layout.item_user, parent, false);
			holder = new FamilyAccountItemHolder(contentView);
		}

		return holder;
	}


	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		holder.itemView.setTag(position);

		if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
			((FamilyAccountHeaderHolder) holder).setItemText(context.getString(R.string.switch_to));
		} else {
			((FamilyAccountItemHolder) holder).setFamilyAccount(familyAccounts.get(position - getHeaderItemCount()));
		}
	}

	public void setShowDisclosure(boolean flag) {
		showDisclosure = flag;
	}

	public void setOnItemClickListener(View.OnClickListener listener) {
		this.itemClickListener = listener;
	}


	public class FamilyAccountHeaderHolder extends RecyclerView.ViewHolder {
		public KPHTextView itemTextView = null;

		public FamilyAccountHeaderHolder(View itemView) {
			super(itemView);
			itemTextView = (KPHTextView) itemView.findViewById(R.id.txt_sync_band_recommendation);

			if (itemClickListener != null)
				itemView.setOnClickListener(itemClickListener);
		}

		public void setItemText(String itemText) {
			if (itemTextView != null)
				itemTextView.setText(itemText);
		}
	}


	public class FamilyAccountItemHolder extends RecyclerView.ViewHolder {
		private ImageView				ivAvatar;
		private KPHTextView				txtHandle;
		private KPHTextView				txtMissionState;
		private KPHImageTextButton		btnMissionsCompleted, btnPowerpoint, btnPacket;
		private ImageView				ivListArrow;


		public FamilyAccountItemHolder(View itemView) {
			super(itemView);

			if (itemClickListener != null)
				itemView.setOnClickListener(itemClickListener);

			ivAvatar = (ImageView) itemView.findViewById(R.id.ivAvatar);
			txtHandle = (KPHTextView) itemView.findViewById(R.id.txt_handle);
			txtMissionState = (KPHTextView) itemView.findViewById(R.id.txt_mission_state);
			btnPacket = (KPHImageTextButton) itemView.findViewById(R.id.iv_packet);
			btnMissionsCompleted = (KPHImageTextButton) itemView.findViewById(R.id.iv_missions_completed);
			btnPowerpoint = (KPHImageTextButton) itemView.findViewById(R.id.iv_powerpoint);

			ivListArrow = (ImageView) itemView.findViewById(R.id.iv_list_arrow);
			if (showDisclosure)
				ivListArrow.setVisibility(View.VISIBLE);
			else
				ivListArrow.setVisibility(View.GONE);
		}

		public void setFamilyAccount(KPHUserSummary familyAccount) {
			Drawable drawable = KPHUserService.sharedInstance().getAvatarDrawable(familyAccount.getAvatarId());
			if (drawable != null)
				ivAvatar.setImageDrawable(drawable);
			else {
				ivAvatar.setImageDrawable(
						UIManager.sharedInstance().getImageDrawable(R.drawable.avatar_placeholder)
				);
			}

			txtHandle.setText(familyAccount.getHandle());

			txtMissionState.setVisibility(View.GONE);

			btnPacket.setNumericText(familyAccount.getTotalPackets());
			btnMissionsCompleted.setNumericText(familyAccount.getMissionsCompleted());
			btnPowerpoint.setNumericText(familyAccount.getTotalPoints());

			if (DISPLAY_MODE_SUMMARY == displayMode) {
				btnPacket.setNumericText(familyAccount.getTotalPackets());
				btnMissionsCompleted.setNumericText(familyAccount.getMissionsCompleted());
				btnPowerpoint.setNumericText(familyAccount.getTotalPoints());

				btnPacket.setVisibility(View.VISIBLE);
				btnMissionsCompleted.setVisibility(View.VISIBLE);
				btnPowerpoint.setVisibility(View.VISIBLE);
			} else if (DISPLAY_MODE_SEVEN_DAYS_POINTS == displayMode) {
				btnPowerpoint.setNumericText(familyAccount.getSevenDaysPowerPoints());

				btnPacket.setVisibility(View.GONE);
				btnMissionsCompleted.setVisibility(View.GONE);
				btnPowerpoint.setVisibility(View.VISIBLE);
			}
		}
	}

}
