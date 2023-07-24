package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

import org.unicefkidpower.kid_power.Misc.Logger;

/**
 * Created by Dayong Li on 9/29/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdSetMessage extends BandCommand {
	private static final String TAG = "CmdSetMessage";

	protected int _slot;
	protected String _message;

	public CmdSetMessage(int slot, String message) {
		_code = BandCommand.CODE_SET_MESSAGE;
		_slot = slot;
		_message = message;
	}

	@Override
	public byte[] getBytes(int block) {
		if (_message == null) {
			Logger.log(TAG, "Message is null, return");
			return null;
		}

		if (_message.length() > 11) {
			Logger.log(TAG, "setMessage(%d, %s), message.length(%d) > 11, return null", _slot, _message, _message.length());
			return null;
		}

		if (_slot < 0 || _slot > 3) {
			Logger.log(TAG, "setMessage(%d, %s), not index in 0 ~ 31, return null", _slot, _message);
			return null;
		}

		byte[] command = new byte[16];
		int checksum = 0;
		command[0] = _code;
		command[1] = (byte) _slot;
		for (int i = 2; i < 15; ++i) {
			command[i] = 0;
		}

		byte[] bytesForName = _message.getBytes();
		for (int i = 0; i < _message.length(); i++) {
			command[i + 2] = bytesForName[i];
		}

		for (int i = 0; i < 15; ++i) {
			checksum += command[i];
		}

		command[15] = (byte) (checksum & 255);
		return command;
	}
}
