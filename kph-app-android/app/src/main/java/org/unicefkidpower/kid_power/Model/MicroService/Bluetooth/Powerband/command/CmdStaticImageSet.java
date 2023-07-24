package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

import org.unicefkidpower.kid_power.Misc.Logger;

/**
 * Created by Dayong Li on 9/29/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdStaticImageSet extends BandCommand {
	private static final String TAG = "CmdStaticImageSet";

	public static final int image_length = 14;

	byte[] _image;

	public CmdStaticImageSet(byte[] image) {
		_code = COMMAND_SET_STATICIMAGE;
		_image = image;
	}

	@Override
	public byte[] getBytes(int block) {
		byte[] command = new byte[16];
		int checksum = 0;

		if (_image == null ||
				_image.length < image_length) {
			Logger.log(TAG, "Action is invalid, return false");
			return null;
		}

		command[0] = _code;
		for (int i = 1; i < 15; ++i) {
			command[i] = 0;
		}

		for (int i = 0; i < image_length; ++i) {
			command[i + 1] = _image[i];
		}

		for (int i = 0; i < 15; ++i) {
			checksum += command[i];
		}

		command[15] = (byte) (checksum & 255);
		return command;
	}
}
