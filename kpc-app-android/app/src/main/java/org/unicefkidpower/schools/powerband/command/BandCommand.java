package org.unicefkidpower.schools.powerband.command;

/**
 * Created by donal_000 on 1/13/2015.
 * Modified by dyl on 12/10/2015
 */
public abstract class BandCommand {
	// set device time
	public static final byte CODE_SET_DEVICETIME = (byte) 0x01;

	// set personal information
	public static final byte CODE_SET_PERSONALINFORMATION = (byte) 0x02;

	// get daily summary data
	public static final byte CODE_GET_DAILYSUMMARYDATA = (byte) 0x07;

	// get software version (FirmwareVersion) data
	public static final byte CODE_GET_FIRMWAREVERSION = (byte) 0x27;

	// get device time
	public static final byte CODE_GET_DEVICETIME = (byte) 0x41;

	// get daily data(detailed data)
	public static final byte CODE_GET_DAILYDATA = (byte) 0x43;

	// get storage information
	public static final byte CODE_GET_STORAGEINFORMATION = (byte) 0x46;

	// set name
	public static final byte CODE_SET_NAME = (byte) 0x66;

	// set message
	public static final byte CODE_SET_MESSAGE = (byte) 0x68;

	//get inspiring message
	public static final byte COMMAND_GET_INSPIRINGMSG = (byte) 0x69;

	//get inspiring message
	public static final byte COMMAND_SET_CALORIEMISSIONGOAL = (byte) 0x70;

	//get inspiring message
	public static final byte COMMAND_GET_CALORIEMISSIONGOAL = (byte) 0x71;

	//get inspiring message
	public static final byte COMMAND_SET_STATICIMAGE = (byte) 0x72;

	//get inspiring message
	public static final byte COMMAND_SEND_CELEBRATION = (byte) 0x73;

	//getV1 inspiring message
	public static final byte COMMAND_SET_TOTAL_POWER_POINT = (byte) 0x75;


	public byte _code;

	public int getBlockCount() {
		return 1;
	}

	public abstract byte[] getBytes(int no_block);
}
