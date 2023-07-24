package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters;

import android.content.Context;

/**
 * Created by Dayong Li on 4/22/2016..
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class CBParamBandDetails {
	public static CBParamBandDetails makeParams(Context context, String deviceCode) {
		CBParamBandDetails param = new CBParamBandDetails();

		param.context = context;
		param.deviceCode = deviceCode;

		return param;
	}

	public Context context;
	public String deviceCode;
}
