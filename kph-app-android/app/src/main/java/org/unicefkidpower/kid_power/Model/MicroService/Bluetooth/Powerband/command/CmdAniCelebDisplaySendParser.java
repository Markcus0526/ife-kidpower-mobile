package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

/**
 * Created by Dayong Li on 9/29/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdAniCelebDisplaySendParser extends BandCommandParser {

	public static CmdAniCelebDisplaySendParser _instance;

	private CmdAniCelebDisplaySendParser() {
		success_code = BandCommandResponse.CODE_SEND_ANIMATEDCELEBRATE_SUCCESS;
		failed_code = BandCommandResponse.CODE_SEND_ANIMATEDCELEBRATE_FAILURE;
	}

	public static CmdAniCelebDisplaySendParser sharedInstance() {
		if (_instance == null)
			_instance = new CmdAniCelebDisplaySendParser();
		return _instance;
	}

	@Override
	public int parse(byte code, byte[] response) {
		_response = new CmdAniCelebDisplaySendRes();

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

	public class CmdAniCelebDisplaySendRes extends BandCommandResponse {
		@Override
		public byte getCommandCode() {
			return success_code;
		}
	}

}
