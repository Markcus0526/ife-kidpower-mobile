package org.unicefkidpower.kid_power.View.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
 * Created by Ruifeng Shi on 1/31/2016.
 */
public class FamilyAccountSwitchListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private LayoutInflater				inflater = null;

	private Context						context;
	private KPHUserData					userData;
	private ArrayList<KPHUserSummary>	familyAccounts = new ArrayList<>();

	private View.OnClickListener		itemClickListener = null;


	public FamilyAccountSwitchListAdapter(
			Context context,
			KPHUserData userData
	) {
		super();

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.context = context;
		this.userData = userData;

		this.familyAccounts.add(new KPHUserSummary(userData));

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
	}

	public KPHUserSummary getItem(int position) {
		return familyAccounts.get(position);
	}

	public void setItemClickedListener(View.OnClickListener listener) {
		itemClickListener = listener;
	}

	public int getEstimatedHeight() {
		return context.getResources().getDimensionPixelSize(R.dimen.dimen_navigation_bar_height) * familyAccounts.size();
	}

	public class FamilyAccountSwitchListViewHolder extends RecyclerView.ViewHolder {
		private ImageView		ivAvatar;
		private KPHTextView		txtUsername;
		private LinearLayout	layoutLoggedIn, layoutSwitch;

		public FamilyAccountSwitchListViewHolder(View itemView) {
			super(itemView);

			ivAvatar = (ImageView) itemView.findViewById(R.id.ivAvatar);
			txtUsername = (KPHTextView) itemView.findViewById(R.id.txt_family_account_name);
			layoutLoggedIn = (LinearLayout) itemView.findViewById(R.id.layout_logged_in);
			layoutSwitch = (LinearLayout) itemView.findViewById(R.id.layout_switch);
		}


		public void setFamilyAccount(KPHUserSummary familyAccount) {
			// Set Avatar Image
			Drawable drawable;

			String avatarId = familyAccount.getAvatarId();
			if (avatarId == null || avatarId.length() == 0) {
				ivAvatar.setImageDrawable(
						UIManager.sharedInstance().getImageDrawable(R.drawable.avatar_placeholder)
				);
				ivAvatar.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP);
			} else {
				drawable = KPHUserService.sharedInstance().getAvatarDrawable(familyAccount.getAvatarId());

				ivAvatar.setImageDrawable(drawable);
				ivAvatar.clearColorFilter();
			}

			// Set Family Account Username
			txtUsername.setText(familyAccount.getHandle());

			if (familyAccount.getId() == userData.getId()) {
				disableSwitch();
			} else {
				enableSwitch();
			}
		}

		public void enableSwitch() {
			ivAvatar.setAlpha(1.0f);
			txtUsername.setAlpha(1.0f);
			layoutSwitch.setVisibility(View.VISIBLE);
			layoutLoggedIn.setVisibility(View.GONE);

			this.itemView.setEnabled(true);
		}

		public void disableSwitch() {
			ivAvatar.setAlpha(0.5f);
			txtUsername.setAlpha(0.5f);
			layoutSwitch.setVisibility(View.GONE);
			layoutLoggedIn.setVisibility(View.VISIBLE);

			this.itemView.setEnabled(false);
		}
	}


	@Override
	public int getItemCount() {
		return familyAccounts.size();
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View contentView = inflater.inflate(R.layout.item_family_account, parent, false);
		if (itemClickListener != null)
			contentView.setOnClickListener(itemClickListener);

		return new FamilyAccountSwitchListViewHolder(contentView);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		holder.itemView.setTag(position);
		((FamilyAccountSwitchListViewHolder)holder).setFamilyAccount(familyAccounts.get(position));
	}

}
