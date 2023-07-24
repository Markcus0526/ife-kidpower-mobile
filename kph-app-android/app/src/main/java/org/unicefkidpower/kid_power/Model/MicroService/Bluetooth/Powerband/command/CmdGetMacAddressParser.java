package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

/**
 * Created by Dayong Li on 9/29/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdGetMacAddressParser extends BandCommandParser {
	private static CmdGetMacAddressParser _instance;

	private CmdGetMacAddressParser() {
		success_code = BandCommandResponse.CODE_GET_MACADDRESS_SUCCESS;
		failed_code = BandCommandResponse.CODE_GET_MACADDRESS_FAILED;
	}

	public static CmdGetMacAddressParser sharedInstance() {
		if (_instance == null)
			_instance = new CmdGetMacAddressParser();
		return _instance;
	}

	@Override
	public int parse(byte code, byte[] response) {
		_response = new CmdGetMacAddressRes();

		int ret = super.parse(code, response);
		if (ret != PARSE_FINISHED)
			return ret;

		if (code == success_code) {
			long byte1 = ((response[1] & 0xFF) << 24);
			long byte2 = ((response[2] & 0xFF) << 16);
			long byte3 = ((response[3] & 0xFF) << 8);
			long byte4 = (response[4] & 0xFF);
			long byte5 = (response[5] & 0xFF);
			long byte6 = (response[6] & 0xFF);

			String address = byte1 + byte2 + byte3 + byte4 + byte5 + byte6 + "";

			((CmdGetMacAddressRes) _response).setMacAddress(address);
			_response._success = true;
		} else {
			((CmdGetMacAddressRes) _response).setMacAddress(null);
			_response._success = false;
		}
		return PARSE_FINISHED;
	}

	public class CmdGetMacAddressRes extends BandCommandResponse {
		protected String _address;

		public CmdGetMacAddressRes() {
			_address = null;
		}

		public CmdGetMacAddressRes(String _address) {
			_address = _address;
		}

		public void setMacAddress(String _address) {
			_address = _address;
		}

		public String getMacAddress() {
			return _address;
		}

		@Override
		public byte getCommandCode() {
			return success_code;
		}
	}

}
