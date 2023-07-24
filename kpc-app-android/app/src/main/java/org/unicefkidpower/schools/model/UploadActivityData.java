package org.unicefkidpower.schools.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Dayong on 7/20/2016.
 */
public class UploadActivityData {
	@SerializedName("deviceId")
	public String deviceId;
	@SerializedName("dates")
	public List<DailyActivityData> dates;
}
