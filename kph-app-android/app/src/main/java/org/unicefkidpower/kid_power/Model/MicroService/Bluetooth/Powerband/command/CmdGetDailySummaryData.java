package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

/**
 * Created by Dayong Li on 9/29/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdGetDailySummaryData extends BandCommand {
	protected int _days;

	public CmdGetDailySummaryData(int days) {
		_code = CODE_GET_DAILYSUMMARYDATA;
		_days = days;
	}

	@Override
	public byte[] getBytes(int block) {
		byte[] command = new byte[16];

		// getV1 daily total activity data -----------------------------
		command[0] = _code;
		command[1] = (byte) _days;
		for (int i = 2; i < 15; i++)
			command[i] = 0;

		int checksum = 0;
		for (int i = 0; i < 15; i++) {
			checksum += command[i];
		}
		command[15] = (byte) (checksum & 255);
		return command;
	}
}
