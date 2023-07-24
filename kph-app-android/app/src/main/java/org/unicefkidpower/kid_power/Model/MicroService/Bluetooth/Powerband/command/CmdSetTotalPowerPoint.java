package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

import org.unicefkidpower.kid_power.Misc.Logger;

/**
 * Created by Dayong Li on 3/2/2016.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdSetTotalPowerPoint extends BandCommand {
	private static final String TAG = "CmdSetTotalPowerPoint";
	protected static int SUPPORT_VERSION = 300;

	protected int _power_point;

	public static boolean SupportedFirmwareVersion(double version) {
		return version > SUPPORT_VERSION;
	}

	public CmdSetTotalPowerPoint(int totalPPs) {
		_code = BandCommand.COMMAND_SET_TOTAL_POWER_POINT;
		_power_point = totalPPs;
	}

	@Override
	public byte[] getBytes(int block) {
		if (_power_point < 0) {
			Logger.log(TAG, "setTotalPowerPoint : invalid power point value", _power_point);
			return null;
		}

		byte[] command = new byte[16];
		int checksum = 0;
		command[0] = _code;
		command[1] = 0;
		command[2] = (byte) (_power_point >> 0x10);
		command[3] = (byte) ((_power_point >> 0x08) & 0xFF);
		command[4] = (byte) (_power_point & 0xFF);

		for (int i = 5; i < 15; ++i) {
			command[i] = 0;
		}

		for (int i = 0; i < 15; ++i) {
			checksum += command[i];
		}

		command[15] = (byte) (checksum & 255);
		return command;
	}
}
