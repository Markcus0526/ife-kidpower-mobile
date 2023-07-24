package org.unicefkidpower.kid_power.Model.Structure;

import android.graphics.drawable.Drawable;

import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Dayong Li on 10/31/2015.
 * UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class KPHMissionInformation {
	public static final String		key_missionId				= "missionId";
	public static final String		key_title					= "title";
	public static final String		key_description				= "description";
	public static final String		key_introVideoURL			= "introVideoURL";
	public static final String		key_completeVideoURL		= "completeVideoURL";
	public static final String		key_calorieGoal				= "calorieGoal";
	public static final String		key_isNew					= "isNew";
	public static final String		key_creditsRequired			= "creditsRequired";
	public static final String		key_missionCompleteText		= "missionCompleteText";
	public static final String		key_delights				= "delights";

	private long					missionId					= 0;
	private String					name						= "";
	private String					description					= "";
	private String					introVideoURL				= "";
	private String					completeVideoURL			= "";
	private int						calorieGoal					= 0;
	private boolean					isNew						= false;
	private int						creditsRequired				= 1;
	private String					missionCompleteText			= "";
	private ArrayList<Integer>		delights					= null;


	public KPHMissionInformation() {}

	public boolean parseHashMap(HashMap<String, Object> hashMap) {
		String value;

		value = (String) hashMap.get(key_missionId);
		if (value != null) {
			missionId = Integer.parseInt(value);
		}

		value = (String) hashMap.get(key_title);
		if (value != null) {
			name = value;
		}

		value = (String) hashMap.get(key_description);
		if (value != null) {
			description = value;
		}

		value = (String) hashMap.get(key_introVideoURL);
		if (value != null) {
			introVideoURL = value;
		}

		value = (String) hashMap.get(key_completeVideoURL);
		if (value != null) {
			completeVideoURL = value;
		}

		value = (String) hashMap.get(key_calorieGoal);
		if (value != null) {
			calorieGoal = Integer.parseInt(value);
		}

		value = (String) hashMap.get(key_isNew);
		if (value != null) {
			isNew = Boolean.parseBoolean(value);
		}

		value = (String) hashMap.get(key_creditsRequired);
		if (value != null) {
			creditsRequired = Integer.parseInt(value);
		}

		value = (String) hashMap.get(key_missionCompleteText);
		if (value != null) {
			missionCompleteText = value;
		}

		ArrayList<Integer> dels = (ArrayList<Integer>) hashMap.get(key_delights);
		if (dels != null) {
			delights = dels;
		} else {
			delights = new ArrayList<>();
		}

		return true;
	}

	public long missionId() {
		return missionId;
	}

	public float getMissionSortOrder() {
		if (missionId == 901)
			return 1.1f;
		return missionId;
	}

	public String name() {
		return name;
	}

	public String description() {
		return description;
	}

	public String introVideoURL() {
		return introVideoURL;
	}

	public String completeVideoURL() {
		return completeVideoURL;
	}

	public int calorieGoal() {
		return calorieGoal;
	}

	public int creditsRequired() {
		return creditsRequired;
	}

	public String missionCompleteText() {
		return missionCompleteText;
	}

	public Drawable getCountryDrawable() {
		String name = String.format(
				"missions/%03d/%03d-country", missionId, missionId
		);

		return loadDrawable(name);
	}

	public Drawable getVideoDrawable() {
		String name = String.format("missions/%03d/%03d-video-bg", missionId, missionId);

		Drawable drawable = loadDrawable(name);

		if (drawable == null) {
			drawable = UIManager.sharedInstance().getImageDrawable(R.drawable.video_generic_black_overlay);
		}

		return drawable;
	}

	public Drawable getCompleteDrawable() {
		String name = String.format(
				"missions/%03d/%03d-complete", missionId, missionId
		);
		return loadDrawable(name);
	}

	protected Drawable loadDrawable(String drawableFileName) {
		Drawable drawable;

		if (drawableFileName == null || drawableFileName.isEmpty())
			return null;

		drawableFileName = UIManager.sharedInstance().getDrawableResourceNameWithScreenDensitySuffix(missionId, drawableFileName);
		drawable = UIManager.sharedInstance().loadAssetDrawableFromResourceName(drawableFileName);

		if (drawable == null) {
			drawableFileName = drawableFileName.replace("-", "_");
			drawable = UIManager.sharedInstance().loadAssetDrawableFromResourceName(drawableFileName);
		}

		return drawable;
	}

	public ArrayList<Integer> getDelights() {
		return delights;
	}
}
