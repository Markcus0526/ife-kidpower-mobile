package org.unicefkidpower.kid_power.Model.Structure;

import java.util.HashMap;

/**
 * Created by Dayong Li on 10/31/2015.
 * UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class KPHCheerInformation {
	public static final String key_id			= "_id";
	public static final String key_imageName	= "imageName";
	public static final String key_type			= "type";

	private long		_id = 0;
	private String		imageName = null;
	private String		type;

	private String		title;
	private float		minimumApiVersion;
	private String		note;
	private boolean		active;
	private long		mission_id;
	private String		description;
	private String		imgURL;
	private String		detailImgURL;
	private String		shareImgURL;
	private String		bgTopColor;
	private String		bgBottomColor;
	private String		mission;


	public KPHCheerInformation() {}

	public boolean parseHashMap(HashMap<String, Object> hashMap) {
		String value = (String) hashMap.get(key_id);

		if (value != null) {
			_id = Integer.parseInt(value);
		} else {
			return false;
		}

		value = (String) hashMap.get(key_imageName);
		if (value != null) {
			imageName = value;
		}

		value = (String) hashMap.get(key_type);
		if (value != null) {
			type = value;
		}

		return true;
	}

	public long getId() {
		return _id;
	}

	public void setId(long id) {
		this._id = id;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
