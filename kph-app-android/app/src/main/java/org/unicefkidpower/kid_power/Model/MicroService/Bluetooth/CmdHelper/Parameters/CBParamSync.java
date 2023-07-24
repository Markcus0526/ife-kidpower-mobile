package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters;

import android.content.Context;

import java.util.Date;

/**
 * Created by Dayong Li on 4/22/2016..
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class CBParamSync {
	public static CBParamSync makeParams(Context context, String deviceCode, Date lastSyncDate,
										 int days, String name, int pps) {
		CBParamSync param = new CBParamSync();

		param.context = context;
		param.deviceCode = deviceCode;
		param.days = days;
		param.name = name;
		param.lastSyncDate = lastSyncDate;
		param.totalPowerPoints = pps;

		return param;
	}

	public Context context;
	public String deviceCode;
	public int days;
	public String name;
	public Date lastSyncDate;
	public int totalPowerPoints;
}
