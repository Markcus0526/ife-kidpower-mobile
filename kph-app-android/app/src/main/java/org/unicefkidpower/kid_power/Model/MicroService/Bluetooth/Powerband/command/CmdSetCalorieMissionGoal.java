package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

import org.unicefkidpower.kid_power.Misc.Logger;

/**
 * Created by Dayong Li on 9/29/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdSetCalorieMissionGoal extends BandCommand {
	private static final String TAG = "CmdSetCalorieMissionGoal";

	public static final byte ACTION_SET = 0x01;
	public static final byte ACTION_CHANGE = 0x02;
	public static final byte ACTION_TURNOFF = 0x03;

	public byte _action;
	public short _goal;

	public CmdSetCalorieMissionGoal(byte action, int goal) {
		_code = COMMAND_SET_CALORIEMISSIONGOAL;
		_action = action;
		_goal = (short) goal;
	}

	@Override
	public byte[] getBytes(int block) {
		if (_action < ACTION_SET || _action > ACTION_TURNOFF) {
			Logger.log(TAG, "Action is invalid, return false");
			return null;
		}

		byte[] command = new byte[16];
		int checksum = 0;
		command[0] = _code;
		for (int i = 1; i < 15; ++i) {
			command[i] = 0;
		}

		command[1] = _action;
		command[2] = (byte) (_goal >> 0x8);
		command[3] = (byte) (_goal & 0xFF);

		for (int i = 0; i < 15; ++i) {
			checksum += command[i];
		}

		command[15] = (byte) (checksum & 255);
		return command;
	}

}
