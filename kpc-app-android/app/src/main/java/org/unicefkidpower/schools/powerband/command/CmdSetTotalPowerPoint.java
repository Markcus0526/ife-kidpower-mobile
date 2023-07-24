package org.unicefkidpower.schools.powerband.command;

import org.unicefkidpower.schools.helper.Logger;

/**
 * Created by Dayong Li on 3/2/2016.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdSetTotalPowerPoint extends BandCommand {
	public static final String TAG = "SetTotalPowerPointCommand";
	protected static int SUPPORT_VERSION = 300;
	protected int _power_point;

	public static boolean SupportedFirmwareVersion(String strVersion) {
		double version = 0;
		try {
			version = Double.parseDouble(strVersion);
		} catch (Exception ex) {
			version = 0;
			ex.printStackTrace();
		}

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

	public static class CmdSetTotalPowerPointParser extends BandCommandResponseParser {
		public static CmdSetTotalPowerPointParser _instance;

		private CmdSetTotalPowerPointParser() {
			success_code = BandCommandResponse.CODE_SET_TOTAL_POWER_POINT_SUCCESS;
			failure_code = BandCommandResponse.CODE_SET_TOTAL_POWER_POINT_FAILURE;
		}

		public static CmdSetTotalPowerPointParser sharedInstance() {
			if (_instance == null)
				_instance = new CmdSetTotalPowerPointParser();
			return _instance;
		}

		@Override
		public int parse(byte code, byte[] response) {
			_response = new CmdSetTotalPowerPointRes();

			int ret = super.parse(code, response);
			if (ret != PARSE_FINISHED)
				return ret;

			if (code == success_code) {
				_response._success = true;
			} else {
				_response._success = false;
			}
			return PARSE_FINISHED;
		}

		public class CmdSetTotalPowerPointRes extends BandCommandResponse {
			@Override
			public byte getCommandCode() {
				return success_code;
			}
		}

	}

}
