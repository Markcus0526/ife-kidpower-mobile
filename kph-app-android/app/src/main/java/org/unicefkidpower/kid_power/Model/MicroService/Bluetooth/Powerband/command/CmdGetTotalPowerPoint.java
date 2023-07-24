package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

/**
 * Created by Dayong Li on 3/2/2016.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdGetTotalPowerPoint extends BandCommand {
	private static final String TAG = "CmdGetTotalPowerPoint";

	public CmdGetTotalPowerPoint() {
		_code = BandCommand.COMMAND_GET_TOTAL_POWER_POINT;
	}

	@Override
	public byte[] getBytes(int block) {
		byte[] command = new byte[16];
		int checksum = 0;
		command[0] = _code;
		for (int i = 1; i < 15; ++i) {
			command[i] = 0;
		}

		for (int i = 0; i < 15; ++i) {
			checksum += command[i];
		}

		command[15] = (byte) (checksum & 0xFF);

		return command;
	}
}
