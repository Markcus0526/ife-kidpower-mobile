package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Dayong Li on 11/11/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class KPHUploadActivityData {
	@SerializedName("deviceId")
	public String deviceId;

	@SerializedName("utcOffset")
	public String utcOffset;

	@SerializedName("dates")
	public List<KPHDailyDetailData.DetailItem> dates;

	public KPHUploadActivityData() {}
}
