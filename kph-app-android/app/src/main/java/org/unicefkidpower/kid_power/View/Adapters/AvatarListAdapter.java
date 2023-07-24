package org.unicefkidpower.kid_power.View.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.Misc.UIManager;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by Ruifeng Shi on 10/2/2015.
 */
public class AvatarListAdapter extends BaseAdapter {
	private final String			TAG = "AvatarListAdapter";

	// Avatar Size Constants
	private static final int		AVATAR_SIZE_SMALL		= 0x0;
	private static final int		AVATAR_SIZE_MEDIUM		= 0x1;
	private static final int		AVATAR_SIZE_LARGE		= 0x2;

	private Context					contextInstance;
	private String					sAvatarFolderPath = "";
	private ArrayList<String>		avatarFileNameListArray;
	private int						nSelectedItemIndex = -1;


	public AvatarListAdapter(Context c) {
		contextInstance = c;
		collectAvatarImages(AVATAR_SIZE_MEDIUM);
	}

	@Override
	public int getCount() {
		if (avatarFileNameListArray == null) {
			collectAvatarImages(AVATAR_SIZE_MEDIUM);
		}

		// Adds number of columns of grid view to add padding on the bottom
		return avatarFileNameListArray.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		String sDrawablePath = sAvatarFolderPath + "/" + avatarFileNameListArray.get(position);
		Drawable drawable = UIManager.sharedInstance().loadAssetDrawableFromResourceName(sDrawablePath);

		if (convertView == null) {
			holder = new ViewHolder();

			convertView = LayoutInflater.from(contextInstance).inflate(R.layout.layout_avatar, parent, false);

			holder.ivCheck = (ImageView) convertView.findViewById(R.id.iv_check);
			holder.ivMain = (ImageView) convertView.findViewById(R.id.iv_main);
			holder.ivMain.setTag(sDrawablePath);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		int avatarSize = ResolutionSet.getScreenSize(contextInstance, true).x / 3;
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

		holder.ivMain.setImageDrawable(drawable);

		if (position == nSelectedItemIndex) {
			holder.ivCheck.setVisibility(View.VISIBLE);
			holder.ivMain.setAlpha(0.5f);
		} else {
			holder.ivCheck.setVisibility(View.INVISIBLE);
			holder.ivMain.setAlpha(1.0f);
		}

		return convertView;
	}

	private void collectAvatarImages(int avatar_size) {
		String sAvatarSizeName;
		String sScreenDensityName = ResolutionSet.getScreenDensityString(contextInstance);

		switch (avatar_size) {
			case AVATAR_SIZE_LARGE:
				sAvatarSizeName = "large";
				break;
			case AVATAR_SIZE_MEDIUM:
				sAvatarSizeName = "medium";
				break;
			case AVATAR_SIZE_SMALL:
			default:
				sAvatarSizeName = "small";
				break;
		}

		sAvatarFolderPath = "avatars/" + sAvatarSizeName;

		try {
			String[] fileNameArray = contextInstance.getAssets().list(sAvatarFolderPath);
			avatarFileNameListArray = new ArrayList<>();

			for (String filename : fileNameArray) {
				if (filename.contains("_" + sScreenDensityName))
					avatarFileNameListArray.add(filename);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void setSelectedItemIndex(int nIndex) {
		nSelectedItemIndex = nIndex;
	}

	public String getSelectedAvatarId() {
		if (nSelectedItemIndex == -1)
			return "";

		String sAvatarFileName = avatarFileNameListArray.get(nSelectedItemIndex);
		String sAvatarId = sAvatarFileName.substring(0, sAvatarFileName.lastIndexOf("_"));
		sAvatarId = sAvatarId.substring(sAvatarFileName.lastIndexOf("-") + 1);

		return sAvatarId;
	}

	public Drawable getSelectedAvatarDrawable() {
		Drawable selectedAvatarDrawable = null;
		if (nSelectedItemIndex != -1) {
			String sAvatarFileName = avatarFileNameListArray.get(nSelectedItemIndex);
			String sDrawablePath = sAvatarFolderPath + "/" + sAvatarFileName;
			selectedAvatarDrawable = UIManager.sharedInstance().loadAssetDrawableFromResourceName(sDrawablePath);
		}

		if (selectedAvatarDrawable == null) {
			selectedAvatarDrawable = UIManager.sharedInstance().getImageDrawable(
					R.drawable.avatar_placeholder
			);
		}

		return selectedAvatarDrawable;
	}

	public int setAvatarSelection(String sAvatarId) {
		if (avatarFileNameListArray == null) {
			return -1;
		}

		String sSearchKeyword = "-" + sAvatarId + "_";
		for (int i = 0; i < avatarFileNameListArray.size(); i++) {
			String avatarFileName = avatarFileNameListArray.get(i);
			if (avatarFileName.contains(sSearchKeyword)) {
				setSelectedItemIndex(i);
				return i;
			}
		}

		return -1;
	}

	private class ViewHolder {
		public ImageView ivMain;
		public ImageView ivCheck;
	}

}
