package org.unicefkidpower.kid_power.View.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.Model.MicroService.KPHMissionService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.Structure.KPHCheer;
import org.unicefkidpower.kid_power.Model.Structure.KPHCheerInformation;
import org.unicefkidpower.kid_power.R;

import java.util.List;


/**
 * Created by Ruifeng Shi on 10/2/2015.
 */
public class CheerIconListAdapter extends BaseAdapter {
	private final String			TAG = "CheerIconListAdapter";

	private static final String		sCheerFolderPath = "cheers/";

	private Context					contextInstance;
	private List<KPHCheer>			cheersList;

	public CheerIconListAdapter(Context context, List<KPHCheer> cheers) {
		this.contextInstance = context;
		this.cheersList = cheers;
	}

	@Override
	public int getCount() {
		return cheersList.size();
	}

	@Override
	public Object getItem(int position) {
		return cheersList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return cheersList.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(contextInstance).inflate(
					R.layout.item_custom_cheer, parent, false
			);

			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		String sDrawablePath = "";
		KPHCheerInformation cheerInformation = KPHMissionService.sharedInstance().getCheerInformation(
				cheersList.get(position).getId()
		);

		if (cheerInformation != null) {
			sDrawablePath = sCheerFolderPath + cheerInformation.getImageName() + "_" + ResolutionSet.getScreenDensityString(contextInstance);
		}

		int avatarSize = (ResolutionSet.getScreenSize(contextInstance, true).x - contextInstance.getResources().getDimensionPixelSize(R.dimen.dimen_margin_10) * 2) / 3;
		int avatarPadding = avatarSize / 5;

		convertView.setLayoutParams(new GridView.LayoutParams(avatarSize, avatarSize * 15 / 16));

		switch (position % 3) {
			case 0:
				convertView.setPadding(avatarPadding, avatarPadding, avatarPadding / 3, 0);
				break;
			case 1:
				convertView.setPadding(avatarPadding * 2 / 3, avatarPadding, avatarPadding * 2 / 3, 0);
				break;
			case 2:
				convertView.setPadding(avatarPadding / 3, avatarPadding, avatarPadding, 0);
				break;
		}

		Drawable drawable = UIManager.sharedInstance().loadAssetDrawableFromResourceName(sDrawablePath);
		if (drawable == null) {
			Logger.log(TAG, "Drawable is null. Tray avatar : " + KPHUserService.sharedInstance().getUserData().getAvatarId());
			holder.ivCheerIcon.setImageDrawable(
					KPHUserService.sharedInstance().getAvatarDrawable(
							KPHUserService.sharedInstance().getUserData().getAvatarId()
					)
			);
		} else {
			Logger.log(TAG, "Drawable is not null : " + sDrawablePath);
			holder.ivCheerIcon.setImageDrawable(drawable);
		}

		return convertView;
	}

	public int getEstimatedHeight() {
		int avatarSize = (ResolutionSet.getScreenSize(contextInstance, true).x - contextInstance.getResources().getDimensionPixelSize(R.dimen.dimen_margin_10) * 2) / 3;
		int avatarHeight = avatarSize * 15 / 16;

		return avatarHeight * ((getCount() + 2) / 3) + contextInstance.getResources().getDimensionPixelSize(R.dimen.dimen_margin_30);
	}

	public class ViewHolder {
		public ImageView ivCheerIcon = null;

		public ViewHolder(View vwItem) {
			ivCheerIcon = (ImageView) vwItem.findViewById(R.id.iv_cheer_icon);
		}
	}

}
