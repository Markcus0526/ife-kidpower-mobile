package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

import org.unicefkidpower.kid_power.Misc.Logger;

/**
 * Created by Dayong Li on 9/29/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdSetUserName extends BandCommand {
	private static final String TAG = "CmdSetUserName";

	protected static final int HALF_NAME_LENGTH = 12;
	protected static final int MAX_NAME_LENGTH = 24;

	public static final int FIRST_PCS = 0x1;
	public static final int SECOND_PCS = 0x2;

	protected String _name;
	protected String first_part;
	protected String second_part;

	public CmdSetUserName(String name) {
		_code = CODE_SET_USERNAME;
		_name = checkName(name);
	}

	@Override
	public int getBlockCount() {
		if (_name.length() <= HALF_NAME_LENGTH) {
			first_part = _name;
			return 1;
		} else if (_name.length() <= MAX_NAME_LENGTH) {
			first_part = _name.substring(0, HALF_NAME_LENGTH);
			second_part = _name.substring(HALF_NAME_LENGTH - 1);
			return 2;
		}

		return 0;
	}

	@Override
	public byte[] getBytes(int block) {
		if (_name == null) {
			Logger.log(TAG, "Name is null, return false");
			return null;
		}

		if (_name.length() > MAX_NAME_LENGTH) {
			Logger.log(TAG, "TestingDevice.setUserName(%s), name.length(%d) > %d, return false", MAX_NAME_LENGTH, _name, _name.length());
			return null;
		}

		byte[] command = new byte[16];
		int checksum = 0;

		// initialize code value
		for (int i = 1; i < 15; ++i) {
			command[i] = 0;
		}

		int pos = 0;
		if (block == 0) {
			command[pos++] = _code;         // 0x66
			command[pos++] = FIRST_PCS;     // ID
			command[pos++] = (byte) _name.length();//length

			byte[] bytesForName = first_part.getBytes();
			for (int i = 0; i < first_part.length(); i++) {
				command[pos++] = bytesForName[i];
			}
		} else if (block == 1) {
			command[pos++] = _code;         // 0x66
			command[pos++] = SECOND_PCS;     // ID

			byte[] bytesForName = second_part.getBytes();
			for (int i = 0; i < second_part.length(); i++) {
				command[pos++] = bytesForName[i];
			}
		}

		//set checksum
		for (int i = 0; i < 15; ++i) {
			checksum += command[i];
		}

		command[15] = (byte) (checksum & 255);
		return command;
	}

	protected String checkName(String name) {
		if (name.length() > MAX_NAME_LENGTH)
			name = name.substring(0, MAX_NAME_LENGTH);

		// TODO : validate name

		return name.toUpperCase();
	}
}
