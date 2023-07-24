package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

/**
 * Created by Dayong Li on 9/29/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdGetUserNameParser extends BandCommandParser {
	private static int HALF_LENGTH = 12;

	private static CmdGetUserNameParser _instance;
	private String subname1;
	private String subname2;

	private CmdGetUserNameParser() {
		success_code = BandCommandResponse.CODE_GET_USERNAME_SUCCESS;
		failed_code = BandCommandResponse.CODE_GET_USERNAME_FAILURE;
	}

	public static CmdGetUserNameParser sharedInstance() {
		if (_instance == null)
			_instance = new CmdGetUserNameParser();
		return _instance;
	}

	@Override
	public int parse(byte code, byte[] response) {
		_response = new CmdGetUserNameRes();

		int ret = super.parse(code, response);
		if (ret != PARSE_FINISHED)
			return ret;

		if (code != success_code) {
			((CmdGetUserNameRes) _response).setUserName(null);
			_response._success = false;
			return PARSE_ERROR;
		}

		byte id = response[1];
		int length = response[2];

		if (id == 1) {
			if (length > HALF_LENGTH) {
				subname1 = new String(response, 3, HALF_LENGTH);
				_response._success = true;
				return PARSE_WAITING;
			} else {
				subname1 = new String(response, 3, length);

				((CmdGetUserNameRes) _response).setUserName(subname1);
				_response._success = true;
				return PARSE_FINISHED;
			}
		} else {
			if (length > HALF_LENGTH) {
				int scale = length / HALF_LENGTH;
				length -= HALF_LENGTH * scale;
			}

			subname2 = new String(response, 3, length);

			((CmdGetUserNameRes) _response).setUserName(subname1 + subname2);
			_response._success = true;
			return PARSE_FINISHED;
		}
	}

	public class CmdGetUserNameRes extends BandCommandResponse {
		protected String username;

		public CmdGetUserNameRes() {
			username = null;
		}

		public CmdGetUserNameRes(String name) {
			this.username = name;
		}

		public void setUserName(String name) {
			this.username = name;
		}

		public String getUserName() {
			return username;
		}

		@Override
		public byte getCommandCode() {
			return success_code;
		}
	}

}
