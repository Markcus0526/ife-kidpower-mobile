package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dayong Li on 04/26/2016.
 */
public class KPHVersion {
	@SerializedName("_id")
	public int _id;

	@SerializedName("platform")
	public String platform;

	@SerializedName("version")
	public int version;

	@SerializedName("content")
	public String content;

	public KPHVersion() {}
}
