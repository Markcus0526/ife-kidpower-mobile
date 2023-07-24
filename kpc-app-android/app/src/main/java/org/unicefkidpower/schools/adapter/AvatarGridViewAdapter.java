package org.unicefkidpower.schools.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.ResolutionSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Ruifeng Shi on 1/3/2016.
 */
public class AvatarGridViewAdapter extends RecyclerView.Adapter {
	// Avatar Size Constants
	private static final int				AVATAR_SIZE_SMALL			= 0x0;
	private static final int				AVATAR_SIZE_MEDIUM			= 0x1;
	private static final int				AVATAR_SIZE_LARGE			= 0x2;

	private String							sAvatarFolderPath			= "";
	private ArrayList<String>				avatarFileNameListArray		= null;
	private int								nSelectedItemIndex			= -1;

	private Context							mContext = null;


	public AvatarGridViewAdapter(Context context) {
		super();

		this.mContext = context;
		if (avatarFileNameListArray == null) {
			collectAvatarImages(AVATAR_SIZE_LARGE);
		}
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View holder = LayoutInflater.from(mContext).inflate(R.layout.layout_avatar, parent, false);
		return new ViewHolder(holder);
	}

	@Override
	public long getItemId(int position) {
		setHasStableIds(true);
		return position + 1;
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
		ViewHolder viewHolder = (ViewHolder) holder;
		viewHolder.setAvatarViewHolderClicksListener(
				new ViewHolder.AvatarViewHolderClicks() {
					@Override
					public void onItemSelected(ImageView ivAvatar) {
						setSelectedItemIndex(position);
						notifyDataSetChanged();
					}
				}
		);

		if (position > 0) {
			String sDrawablePath = sAvatarFolderPath + "/" + avatarFileNameListArray.get(position);

			try {
				InputStream isDrawable = mContext.getAssets().open(sDrawablePath);
				viewHolder.ivMain.setImageDrawable(Drawable.createFromStream(isDrawable, null));
			} catch (IOException e) {
				viewHolder.ivMain.setImageResource(R.drawable.no_avatar);
			}
		} else {
			// Insert No Avatar image into the first index of avatars list
			viewHolder.ivMain.setImageResource(R.drawable.no_avatar);
		}

		if (position == nSelectedItemIndex) {
			((ViewHolder) holder).vwFrame.setBackgroundResource(R.drawable.avatar_selected_background);
		} else {
			((ViewHolder) holder).vwFrame.setBackground(null);
		}
	}

	@Override
	public int getItemCount() {
		if (avatarFileNameListArray == null) {
			collectAvatarImages(AVATAR_SIZE_LARGE);
		}

		// Adds number of columns of grid view to add padding on the bottom
		return avatarFileNameListArray.size();
	}

	private void collectAvatarImages(int avatar_size) {
		String sAvatarSizeName = "";
		String sScreenDensityName = ResolutionSet.getScreenDensityString(mContext);

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
			String[] fileNameArray = mContext.getAssets().list(sAvatarFolderPath);
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


	static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		View vwFrame;
		ImageView ivMain = null;

		public AvatarViewHolderClicks mListener;

		public ViewHolder(View v) {
			super(v);

			vwFrame = v;
			ivMain = (ImageView) v.findViewById(R.id.iv_avatar);
			ivMain.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			if (v instanceof ImageView) {
				this.mListener.onItemSelected((ImageView) v);
			}
		}

		public void setAvatarViewHolderClicksListener(AvatarViewHolderClicks listener) {
			this.mListener = listener;
		}

		public interface AvatarViewHolderClicks {
			void onItemSelected(ImageView ivAvatar);
		}
	}
}
