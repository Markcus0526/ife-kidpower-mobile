package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

/**
 * Created by Dayong Li on 9/29/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class BandCommandParser {
	public byte success_code = 0;
	public byte failed_code = 0;

	public static final int PARSE_FINISHED = 0;
	public static final int PARSE_WAITING = 1;
	public static final int PARSE_CODE_INCORRECT = 2;
	public static final int PARSE_ERROR = 3;

	public int _index;
	public BandCommandResponse _response;

	public int parse(byte code, byte[] response) {
		// checksum
		int length = response.length;
		byte checksum = 0;

		for (int i = 0; i < length; ++i) {
			if (i < length - 1) {
				checksum += response[i];
			}
		}

		if (length < 16 || (0xFF & response[length - 1]) != (checksum & 0xFF)) {
			// error data
			return PARSE_ERROR;
		}
		return PARSE_FINISHED;
	}

	public byte getSuccessCode() {
		return success_code;
	}

	public byte getFailedCode() {
		return failed_code;
	}
}
