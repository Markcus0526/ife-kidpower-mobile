package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters;

import android.content.Context;

/**
 * Created by Dayong Li on 4/22/2016..
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class CBParamLinkBand {
	public static CBParamLinkBand makeParams(Context context, String deviceCode,
											 String name, int goal) {
		CBParamLinkBand param = new CBParamLinkBand();

		param.context = context;
		param.deviceCode = deviceCode;
		param.name = name;
		param.goal = goal;

		return param;

	}

	public Context context;
	public String deviceCode;
	public String name;
	public int goal;
}
