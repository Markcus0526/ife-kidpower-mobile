package org.unicefkidpower.kid_power.View.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.Model.Structure.KPHCheerInformation;
import org.unicefkidpower.kid_power.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ruifeng Shi on 10/2/2015.
 */
public class UnlockedCheerListAdapter extends BaseAdapter {
	private static final String					sCheerFolderPath = "cheers/";
	private Context								contextInstance;
	private ArrayList<KPHCheerInformation>		cheerInformationList;

	public UnlockedCheerListAdapter(Context context, List<KPHCheerInformation> cheerInformationList) {
		this.contextInstance = context;
		this.cheerInformationList = new ArrayList<>(cheerInformationList);
	}

	@Override
	public int getCount() {
		return cheerInformationList.size();
	}

	@Override
	public Object getItem(int position) {
		return cheerInformationList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return cheerInformationList.get(position).getId();
	}

	public class ViewHolder {
		ImageView ivCheer = null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		String sDrawablePath = "";

		for (KPHCheerInformation cheerInformation : cheerInformationList) {
			if (cheerInformation.getId() == cheerInformationList.get(position).getId()) {
				sDrawablePath = sCheerFolderPath + cheerInformation.getImageName() + "_" + ResolutionSet.getScreenDensityString(contextInstance);
				break;
			}
		}

		Drawable drawable = UIManager.sharedInstance().loadAssetDrawableFromResourceName(sDrawablePath);

		if (convertView == null) {
			convertView = LayoutInflater.from(contextInstance).inflate(R.layout.item_custom_cheer, parent, false);

			holder = new ViewHolder();
			holder.ivCheer = (ImageView) convertView.findViewById(R.id.iv_cheer_icon);
			holder.ivCheer.setTag(sDrawablePath);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		int avatarSize = (ResolutionSet.getScreenSize(contextInstance, true).x - contextInstance.getResources().getDimensionPixelSize(R.dimen.dimen_32dp) * 2) / 3;
		int avatarPadding = avatarSize / 5;

		convertView.setLayoutParams(new GridView.LayoutParams(avatarSize, avatarSize * 15 / 16));

		switch (position % 3) {
			case 0:
				convertView.setPadding(
						avatarPadding,
						avatarPadding,
						avatarPadding / 3,
						0
				);
				break;
			case 1:
				convertView.setPadding(
						avatarPadding * 2 / 3,
						avatarPadding,
						avatarPadding * 2 / 3,
						0
				);
				break;
			case 2:
				convertView.setPadding(
						avatarPadding / 3,
						avatarPadding,
						avatarPadding,
						0
				);
				break;
		}

		holder.ivCheer.setImageDrawable(drawable);

		return convertView;
	}

	public int getEstimatedHeight() {
		int avatarSize = (ResolutionSet.getScreenSize(contextInstance, true).x - contextInstance.getResources().getDimensionPixelSize(R.dimen.dimen_32dp) * 2) / 3;
		int avatarHeight = avatarSize * 15 / 16;

		return avatarHeight * ((getCount() + 2) / 3) + contextInstance.getResources().getDimensionPixelSize(R.dimen.dimen_margin_30);
	}
}
