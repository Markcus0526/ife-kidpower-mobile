package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

import org.unicefkidpower.kid_power.Misc.OSDate;

import java.util.Date;

/**
 * Created by Dayong Li on 9/29/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdSetDeviceTime extends BandCommand {
	protected Date _date;

	public CmdSetDeviceTime(Date dt) {
		_date = dt;
		_code = CODE_SET_DEVICETIME;
	}

	@Override
	public byte[] getBytes(int block) {

		String deviceTimeString = new OSDate(_date).toStringWithFormat("yyyy-MM-dd HH:mm:ss");

		String[] dateSplitObj = deviceTimeString.split(" ");
		String[] dateStringObj = dateSplitObj[0].split("-");
		String[] timeStringObj = dateSplitObj[1].split(":");
		int year = Integer.parseInt(dateStringObj[0]) - 2000;
		int month = Integer.parseInt(dateStringObj[1]);
		int day = Integer.parseInt(dateStringObj[2]);
		int hh = Integer.parseInt(timeStringObj[0]);
		int mm = Integer.parseInt(timeStringObj[1]);
		int ss = Integer.parseInt(timeStringObj[2]);


		byte[] var1 = new byte[16];
		byte var2 = 0;
		var1[0] = _code;
		var1[1] = (byte) convert(year);
		var1[2] = (byte) convert(month);
		var1[3] = (byte) convert(day);
		var1[4] = (byte) convert(hh);
		var1[5] = (byte) convert(mm);
		var1[6] = (byte) convert(ss);
		for (int i = 7; i < 15; ++i) {
			var1[i] = 0;
		}
		for (int i = 0; i < 15; ++i) {
			var2 += var1[i];
		}
		var1[15] = (byte) (var2 & 255);

		return var1;
	}

	public static int convert(int n) {
		return Integer.valueOf(String.valueOf(n), 16);
	}

}
