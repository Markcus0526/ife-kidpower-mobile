package org.unicefkidpower.kid_power.Model.Structure;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.UIManager;

import java.util.HashMap;

/**
 * Created by Dayong Li on 10/31/2015.
 * UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class KPHDelightInformation {
	private final String				TAG = "KPHDelightInformation";

	public static final String			key_delightId = "delightId";
	public static final String			key_missionId = "missionId";
	public static final String			key_name = "name";
	public static final String			key_description = "description";
	public static final String			key_twitter = "twitter_desc";
	public static final String			key_goal = "goal";
	public static final String			key_type = "type";
	public static final String			key_imageName = "imageName";
	public static final String			key_detailImageName = "detailImageName";
	public static final String			key_shareImageName = "shareImage";
	public static final String			key_videoURL = "videoURL";

	public static final String			key_bgTopColor = "bgTopColor";
	public static final String			key_bgBottomColor = "bgBottomColor";

	private long						delightId = 0;
	private String						name = null;
	private int							goal = 0;
	private String						type = null;
	private long						missionId = 0;
	private String						description = null;

	public String				scope;
	public float				minimum;
	public float				maximum;
	public String				message;
	public boolean				active;
	public String				imgURL;
	public String				detailImgURL;
	public String				shareImgURL;
	public String				bgTopColor;
	public String				bgBottomColor;

	private String				twitter_description = null;
	private String				imageName = null;

	private String				detailImage = null;
	private String				shareImage = null;
	private String				videoURL = null;

	private int clrBgTop = 0;
	private int clrBgBottom = 0;

	public Mission mission;


	public static class Mission {
		public long _id;
		public String name;
		public int goal;
		public int timeToComplete;
		public int pointsPerPacket;
	}


	public KPHDelightInformation() {
	}


	public boolean parseHashMap(HashMap<String, Object> hashMap) {
		String value = (String) hashMap.get(key_delightId);
		if (value != null) {
			delightId = Integer.parseInt(value);
		} else
			return false;

		value = (String) hashMap.get(key_missionId);
		if (value != null) {
			missionId = Integer.parseInt(value);
		}

		value = (String) hashMap.get(key_name);
		if (value != null) {
			name = value;
		}

		value = (String) hashMap.get(key_description);
		if (value != null) {
			description = value;
		}

		value = (String) hashMap.get(key_twitter);
		if (value != null) {
			twitter_description = value;
		}

		value = (String) hashMap.get(key_goal);
		if (value != null) {
			goal = Integer.parseInt(value);
		}

		value = (String) hashMap.get(key_type);
		if (value != null) {
			type = value;
		}

		value = (String) hashMap.get(key_imageName);
		if (value != null) {
			imageName = value;
		}

		value = (String) hashMap.get(key_detailImageName);
		if (value != null) {
			detailImage = value;
		}

		value = (String) hashMap.get(key_shareImageName);
		if (value != null) {
			shareImage = value;
		}

		value = (String) hashMap.get(key_videoURL);
		if (value != null) {
			videoURL = value;
		}

		value = (String) hashMap.get(key_bgTopColor);
		if (value != null) {
			clrBgTop = Color.parseColor(value);
		}
		value = (String) hashMap.get(key_bgBottomColor);
		if (value != null) {
			clrBgBottom = Color.parseColor(value);
		}

		return true;
	}

	public long getDelightId() {
		return delightId;
	}

	public boolean isMissionIdEquals(long missionId) {
		return this.missionId == missionId;
	}

	public long getMissionId() {
		return missionId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getTwitter_description() {
		if (twitter_description == null)
			return description;
		return twitter_description;
	}

	public int getGoal() {
		return goal;
	}

	public String getType() {
		return type;
	}

	public String imageForSharing() {
		String drawableFileName = null;
		if (type.equals(KPHConstants.DELIGHT_POSTCARD)) {
			// POST CARD
			if (shareImage.contains(".")) {
				drawableFileName = String.format("missions/%03d/%s", missionId, shareImage);
			} else {
				drawableFileName = String.format("missions/%03d/%s", missionId, shareImage);
			}
			return drawableFileName;
		} else if (type.equals(KPHConstants.DELIGHT_STAMP)) {
			// Mission Delight
			drawableFileName = String.format("missions/%03d/%s", missionId, imageName);
		} else {
			// Baseline Delight
			drawableFileName = String.format("baseline/%s", imageName);
			drawableFileName = drawableFileName.replace('-', '_');
		}

		return UIManager.sharedInstance().getDrawableResourceNameWithScreenDensitySuffix(missionId, drawableFileName);
	}

	public String getShareImage() {
		return shareImage;
	}

	public String getVideoURL() {
		return videoURL;
	}

	public int getBgTopColor() {
		return clrBgTop;
	}

	public int getBgBottomColor() {
		return clrBgBottom;
	}

	public Drawable getImageDrawable() {
		if (imageName == null)
			return null;

		String drawableFileName = null;
		if (missionId != 0) {
			drawableFileName = String.format("missions/%03d/%s", missionId, imageName);
		} else {
			drawableFileName = String.format("baseline/%s", imageName);
			drawableFileName = drawableFileName.replace('-', '_');
		}

		if (drawableFileName != null)
			drawableFileName = UIManager.sharedInstance().getDrawableResourceNameWithScreenDensitySuffix(missionId, drawableFileName);

		return loadDrawable(drawableFileName);
	}

	public Drawable getDetailImageDrawable() {
		if (detailImage == null)
			return null;

		String drawableFileName = null;
		if (missionId != 0) {
			drawableFileName = String.format("missions/%03d/%s", missionId, detailImage);
		} else {

		}

		if (drawableFileName != null)
			drawableFileName = UIManager.sharedInstance().getDrawableResourceNameWithScreenDensitySuffix(missionId, drawableFileName);

		return loadDrawable(drawableFileName);
	}

	public Drawable getSharingImageDrawable() {
		if (detailImage == null)
			return null;

		String drawableFileName = null;
		if (missionId != 0) {
			drawableFileName = String.format("missions/%03d/%03d_share", missionId, missionId);
		} else {
		}

		return loadDrawable(drawableFileName);
	}


	protected Drawable loadDrawable(String drawableFileName) {
		Drawable drawable = null;
		if (drawableFileName == null || drawableFileName.isEmpty())
			return null;

		drawable = UIManager.sharedInstance().loadAssetDrawableFromResourceName(drawableFileName);

		if (drawable == null) {
			drawableFileName = drawableFileName.replace("-", "_");
			drawable = UIManager.sharedInstance().loadAssetDrawableFromResourceName(drawableFileName);
		}

		return drawable;
	}
}
