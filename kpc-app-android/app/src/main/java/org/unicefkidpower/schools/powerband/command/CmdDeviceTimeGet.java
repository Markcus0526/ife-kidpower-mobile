package org.unicefkidpower.schools.powerband.command;

/**
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * Created by dayong li on 6/23/2015.
 */

public class CmdDeviceTimeGet extends BandCommand {
	public CmdDeviceTimeGet() {
		_code = CODE_GET_DEVICETIME;
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

	static public class CmdDeviceTimeGetParser extends BandCommandResponseParser {
		private static CmdDeviceTimeGetParser _instance;

		private CmdDeviceTimeGetParser() {
			success_code = BandCommandResponse.CODE_GET_DEVICETIME_SUCCESS;
			failure_code = BandCommandResponse.CODE_GET_DEVICETIME_FAILED;
		}

		public static CmdDeviceTimeGetParser sharedInstance() {
			if (_instance == null)
				_instance = new CmdDeviceTimeGetParser();
			return _instance;
		}

		@Override
		public int parse(byte code, byte[] response) {
			_response = new CmdDeviceTimeGetRes();

			int ret = super.parse(code, response);
			if (ret != PARSE_FINISHED)
				return ret;

			if (code == success_code) {
				DeviceDateTime deviceDateTime = new DeviceDateTime();

				try {
					deviceDateTime.year = Integer.parseInt(String.format("%x", response[1]));
					deviceDateTime.month = Integer.parseInt(String.format("%x", response[2]));
					deviceDateTime.day = Integer.parseInt(String.format("%x", response[3]));
					deviceDateTime.hour = Integer.parseInt(String.format("%x", response[4]));
					deviceDateTime.minute = Integer.parseInt(String.format("%x", response[5]));
					deviceDateTime.second = Integer.parseInt(String.format("%x", response[6]));
				} catch (Exception e) {

				}
				((CmdDeviceTimeGetRes) _response).setDeviceDateTime(deviceDateTime);
				_response._success = true;
				return PARSE_FINISHED;
			} else {
				_response._success = false;
			}
			return PARSE_FINISHED;
		}
	}

	public static class DeviceDateTime {
		public int year;
		public int month;
		public int day;
		public int hour;
		public int minute;
		public int second;
	}

	static public class CmdDeviceTimeGetRes extends BandCommandResponse {
		protected DeviceDateTime _data;

		public DeviceDateTime getDeviceDateTime() {
			return _data;
		}

		public void setDeviceDateTime(DeviceDateTime data) {
			_data = data;
		}

		@Override
		public byte getCommandCode() {
			return CODE_GET_DEVICETIME_SUCCESS;
		}
	}
}
