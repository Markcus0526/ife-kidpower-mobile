package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

/**
 * Created by Dayong Li on 9/29/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public abstract class BandCommand {

	// device time
	public static final byte CODE_SET_DEVICETIME = (byte) 0x01;

	// set personal information
	public static final byte CODE_SET_PERSONALINFORMATION = (byte) 0x02;

	// getV1 daily summary data
	public static final byte CODE_GET_DAILYSUMMARYDATA = (byte) 0x07;

	// getV1 MAC address
	public static final byte CODE_GET_MACADDRESS = (byte) 0x22;

	// getV1 software version (FirmwareVersion) data
	public static final byte CODE_GET_FIRMWAREVERSION = (byte) 0x27;

	// getV1 daily data(detailed data)
	public static final byte CODE_GET_DAILYDATA = (byte) 0x43;

	// getV1 storage information
	public static final byte CODE_GET_STORAGEINFORMATION = (byte) 0x46;

	// set name
	public static final byte CODE_SET_USERNAME = (byte) 0x66;

	// getV1 name
	public static final byte CODE_GET_USERNAME = (byte) 0x67;

	// set message
	public static final byte CODE_SET_MESSAGE = (byte) 0x68;

	//getV1 inspiring message
	public static final byte COMMAND_GET_INSPIRINGMSG = (byte) 0x69;

	//getV1 inspiring message
	public static final byte COMMAND_SET_CALORIEMISSIONGOAL = (byte) 0x70;

	//getV1 inspiring message
	public static final byte COMMAND_GET_CALORIEMISSIONGOAL = (byte) 0x71;

	//getV1 inspiring message
	public static final byte COMMAND_SET_STATICIMAGE = (byte) 0x72;

	//getV1 inspiring message
	public static final byte COMMAND_SEND_CELEBRATION = (byte) 0x73;

	//getV1 inspiring message
	public static final byte COMMAND_SET_TOTAL_POWER_POINT = (byte) 0x75;

	//getV1 inspiring message
	public static final byte COMMAND_GET_TOTAL_POWER_POINT = (byte) 0x76;

	public byte _code;

	public int getBlockCount() {
		return 1;
	}

	public abstract byte[] getBytes(int no_block);

	public boolean isSupportedFirmware(String version) {
		return true;
	}
}
















