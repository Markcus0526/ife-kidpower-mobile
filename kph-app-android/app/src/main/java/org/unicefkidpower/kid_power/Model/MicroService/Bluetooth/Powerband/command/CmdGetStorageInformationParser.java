package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

/**
 * Created by Dayong Li on 9/29/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdGetStorageInformationParser extends BandCommandParser {
	private static CmdGetStorageInformationParser _instance;

	private CmdGetStorageInformationParser() {
		success_code = BandCommandResponse.CODE_GET_STORAGEINFORMATION_SUCCESS;
		failed_code = BandCommandResponse.CODE_GET_STORAGEINFORMATION_FAILURE;
	}

	public static CmdGetStorageInformationParser sharedInstance() {
		if (_instance == null)
			_instance = new CmdGetStorageInformationParser();
		return _instance;
	}

	@Override
	public int parse(byte code, byte[] response) {
		_response = new CmdGetStorageInformationRes();

		int ret = super.parse(code, response);
		if (ret != PARSE_FINISHED)
			return ret;

		if (code == success_code) {
			long byte1 = ((response[1] & 0xFF) << 24);
			long byte2 = ((response[2] & 0xFF) << 16);
			long byte3 = ((response[3] & 0xFF) << 8);
			long byte4 = (response[4] & 0xFF);
			long storagebytes = byte1 + byte2 + byte3 + byte4;

			((CmdGetStorageInformationRes) _response).setStorageInformation(storagebytes);
			_response._success = true;
		} else {
			((CmdGetStorageInformationRes) _response).setStorageInformation(0);
			_response._success = false;
		}
		return PARSE_FINISHED;
	}

	public class CmdGetStorageInformationRes extends BandCommandResponse {
		protected long _storageInformation;

		public CmdGetStorageInformationRes() {
			_storageInformation = 0;
		}

		public CmdGetStorageInformationRes(long storageInformation) {
			_storageInformation = storageInformation;
		}

		public void setStorageInformation(long storageInformation) {
			_storageInformation = storageInformation;
		}

		public long getStorageInformation() {
			return _storageInformation;
		}

		@Override
		public byte getCommandCode() {
			return success_code;
		}
	}

}
