package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

/**
 * Created by Dayong Li on 9/29/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdGetFirmwareVersionParser extends BandCommandParser {
	public static String UNKOWN_VERSION = "FFFF";
	private static CmdGetFirmwareVersionParser _instance;

	private CmdGetFirmwareVersionParser() {
		success_code = BandCommandResponse.CODE_GET_FIRMWAREVERSION_SUCCESS;
		failed_code = BandCommandResponse.CODE_GET_FIRMWAREVERSION_FAILED;
	}

	public static CmdGetFirmwareVersionParser sharedInstance() {
		if (_instance == null)
			_instance = new CmdGetFirmwareVersionParser();
		return _instance;
	}

	@Override
	public int parse(byte code, byte[] response) {
		_response = new CmdGetFirmwareVersionRes();

		int ret = super.parse(code, response);
		if (ret != PARSE_FINISHED)
			return ret;

		if (code == success_code) {
			int pos = 1;
			String version = new String(response, pos, 3);

			((CmdGetFirmwareVersionRes) _response).setFirmwareVersion(version);
			_response._success = true;
		} else {
			((CmdGetFirmwareVersionRes) _response).setFirmwareVersion(null);
			_response._success = false;
		}
		return PARSE_FINISHED;
	}

	public class CmdGetFirmwareVersionRes extends BandCommandResponse {
		protected String _version;

		public CmdGetFirmwareVersionRes() {
			_version = "";
		}

		public void setFirmwareVersion(String _version) {
			try {
				this._version = _version;
			} catch (Exception ex) {
				this._version = UNKOWN_VERSION;
			}
		}

		public String getFirmwareVersion() {
			return _version;
		}

		@Override
		public byte getCommandCode() {
			return success_code;
		}
	}

}
