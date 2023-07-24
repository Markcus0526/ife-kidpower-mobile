package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

/**
 * Created by Dayong Li on 3/2/2016.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdSetTotalPowerPointParser extends BandCommandParser {
	public static CmdSetTotalPowerPointParser _instance;

	private CmdSetTotalPowerPointParser() {
		success_code = BandCommandResponse.CODE_SET_TOTAL_POWER_POINT_SUCCESS;
		failed_code = BandCommandResponse.CODE_SET_TOTAL_POWER_POINT_FAILURE;
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
