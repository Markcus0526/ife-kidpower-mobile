package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

/**
 * Created by Dayong Li on 9/29/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdSetPersonalInformationParser extends BandCommandParser {
	public static CmdSetPersonalInformationParser _instance;

	private CmdSetPersonalInformationParser() {
		success_code = BandCommandResponse.CODE_SET_PERSONALINFORMATION_SUCCESS;
		failed_code = BandCommandResponse.CODE_SET_PERSONALINFORMATION_FAILED;
	}

	public static CmdSetPersonalInformationParser sharedInstance() {
		if (_instance == null)
			_instance = new CmdSetPersonalInformationParser();
		return _instance;
	}

	@Override
	public int parse(byte code, byte[] response) {
		_response = new CmdSetPersonalInformationRes();
		;

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

	public class CmdSetPersonalInformationRes extends BandCommandResponse {
		@Override
		public byte getCommandCode() {
			return success_code;
		}
	}

}
