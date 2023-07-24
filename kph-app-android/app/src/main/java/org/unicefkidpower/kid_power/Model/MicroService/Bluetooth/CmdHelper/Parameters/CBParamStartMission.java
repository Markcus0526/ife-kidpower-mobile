package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters;

import android.content.Context;

/**
 * Created by Dayong Li on 4/22/2016..
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class CBParamStartMission {
	public static CBParamStartMission makeParams(Context context, String deviceCode, int goalCalories) {
		CBParamStartMission param = new CBParamStartMission();

		param.context = context;
		param.deviceCode = deviceCode;
		param.goalCalories = goalCalories;

		return param;
	}

	public Context context;
	public String deviceCode;
	public int goalCalories;
}
