package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters;

import android.content.Context;

/**
 * Created by Dayong Li on 4/22/2016..
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class CBParamSimple {
	public static CBParamSimple makeParams(Context context, String deviceCode) {
		CBParamSimple param = new CBParamSimple();

		param.context = context;
		param.deviceCode = deviceCode;

		return param;
	}

	public Context context;
	public String deviceCode;
}
