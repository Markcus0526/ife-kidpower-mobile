package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

/**
 * Created by Dayong Li on 9/29/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdSetPersonalInformation extends BandCommand {
	private static final String TAG = "CmdSetPersonalInformation";

	protected int _gender;
	protected int _age;
	protected int _height;
	protected int _weight;
	protected int _strideLength;

	/**
	 * @param gender       - 0 : femaile, 1 : male
	 * @param age
	 * @param height
	 * @param weight
	 * @param strideLength
	 */
	public CmdSetPersonalInformation(int gender, int age, int height, int weight, int strideLength) {
		_code = CODE_SET_PERSONALINFORMATION;

		_gender = gender;
		_age = age;
		_height = height;
		_weight = weight;
		_strideLength = strideLength;
	}

	@Override
	public byte[] getBytes(int block) {
		byte[] command = new byte[16];
		int checksum = 0;
		command[0] = _code;
		for (int i = 1; i < 15; ++i) {
			command[i] = 0;
		}

		command[1] = (byte) _gender;
		command[2] = (byte) _age;
		command[3] = (byte) _height;
		command[4] = (byte) _weight;
		command[5] = (byte) _strideLength;

		for (int i = 0; i < 15; ++i) {
			checksum += command[i];
		}
		command[15] = (byte) (checksum & 255);

		//
		return command;
	}
}
