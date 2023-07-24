package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

/**
 * Created by Dayong Li on 3/2/2016.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdGetTotalPowerPointParser extends BandCommandParser {
	private static CmdGetTotalPowerPointParser _instance;

	private CmdGetTotalPowerPointParser() {
		success_code = BandCommandResponse.CODE_GET_TOTAL_POWER_POINT_SUCCESS;
		failed_code = BandCommandResponse.CODE_GET_TOTAL_POWER_POINT_FAILURE;
	}

	public static CmdGetTotalPowerPointParser sharedInstance() {
		if (_instance == null)
			_instance = new CmdGetTotalPowerPointParser();
		return _instance;
	}

	@Override
	public int parse(byte code, byte[] response) {
		_response = new CmdGetTotalPowerPointRes();

		int ret = super.parse(code, response);
		if (ret != PARSE_FINISHED)
			return ret;

		if (code == success_code) {
			int pos = 2;
			int byte1 = Math.abs((response[pos + 0] & 0xFF));
			int byte2 = Math.abs((response[pos + 1] & 0xFF));
			int byte3 = Math.abs((response[pos + 2] & 0xFF));
			int powerPoint = (byte1 << 16) + (byte2 << 8) + byte3;

			((CmdGetTotalPowerPointRes) _response).setPowerPoint(powerPoint);
			_response._success = true;
		} else {
			_response._success = false;
		}
		return PARSE_FINISHED;
	}

	public class CmdGetTotalPowerPointRes extends BandCommandResponse {
		protected long _powerPoint;

		public CmdGetTotalPowerPointRes() {
			_powerPoint = 0;
		}

		public void setPowerPoint(long powerPoint) {
			_powerPoint = powerPoint;
		}

		@Override
		public byte getCommandCode() {
			return success_code;
		}

		public long getPowerPoint() {
			return _powerPoint;
		}
	}
}
