package org.unicefkidpower.schools.powerband.command;

/**
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * Created by dayong li on 6/23/2015.
 */

public class CmdDeviceFirmwareGet extends BandCommand {
	public static String UNKOWN_VERSION = "FFFF";

	public CmdDeviceFirmwareGet() {
		_code = CODE_GET_FIRMWAREVERSION;
	}

	@Override
	public byte[] getBytes(int no_block) {
		byte[] command = new byte[16];

		// get daily total activity data -----------------------------
		command[0] = _code;
		for (int i = 1; i < 15; i++)
			command[i] = 0;

		int checksum = 0;
		for (int i = 0; i < 15; i++) {
			checksum += command[i];
		}
		command[15] = (byte) (checksum & 255);
		return command;
	}

	static public class CmdDeviceFirmwareGetParser extends BandCommandResponseParser {
		private static CmdDeviceFirmwareGetParser _instance;

		private CmdDeviceFirmwareGetParser() {
			success_code = BandCommandResponse.CODE_GET_FIRMWAREVERSION_SUCCESS;
			failure_code = BandCommandResponse.CODE_GET_FIRMWAREVERSION_FAILED;
		}

		public static CmdDeviceFirmwareGetParser sharedInstance() {
			if (_instance == null)
				_instance = new CmdDeviceFirmwareGetParser();
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
	}

	public static class CmdGetFirmwareVersionRes extends BandCommandResponse {
		String _version;

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
			return CODE_GET_FIRMWAREVERSION_SUCCESS;
		}
	}
}
