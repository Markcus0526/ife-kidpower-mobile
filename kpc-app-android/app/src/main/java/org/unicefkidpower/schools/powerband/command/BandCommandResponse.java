package org.unicefkidpower.schools.powerband.command;

import org.unicefkidpower.schools.ble.BlePeripheral;

/**
 * Created by donal_000 on 1/13/2015.
 */
public class BandCommandResponse {
	public static final byte CODE_SET_DEVICETIME_SUCCESS = (byte) 0x01;
	public static final byte CODE_SET_DEVICETIME_FAILED = (byte) 0x81;

	public static final byte CODE_SET_PERSONALINFORMATION_SUCCESS = (byte) 0x02;
	public static final byte CODE_SET_PERSONALINFORMATION_FAILED = (byte) 0x82;

	public static final byte CODE_GET_DAILYSUMMARYDATA_SUCCESS = (byte) 0x07;
	public static final byte CODE_GET_DAILYSUMMARYDATA_FAILED = (byte) 0x87;

	public static final byte CODE_GET_FIRMWAREVERSION_SUCCESS = (byte) 0x27;
	public static final byte CODE_GET_FIRMWAREVERSION_FAILED = (byte) 0xA7;

	public static final byte CODE_GET_DEVICETIME_SUCCESS = (byte) 0x41;
	public static final byte CODE_GET_DEVICETIME_FAILED = (byte) 0xC1;

	public static final byte CODE_GET_DAILYDATA_SUCCESS = (byte) 0x43;
	public static final byte CODE_GET_DAILYDATA_FAILED = (byte) 0xC3;

	public static final byte CODE_GET_STORAGEINFORMATION_SUCCESS = (byte) 0x46;
	public static final byte CODE_GET_STORAGEINFORMATION_FAILURE = (byte) 0xC6;

	public static final byte CODE_SET_NAME_SUCCESS = (byte) 0x66;
	public static final byte CODE_SET_NAME_FAILURE = (byte) 0xE6;

	public static final byte CODE_SET_MESSAGE_SUCCESS = (byte) 0x68;
	public static final byte CODE_SET_MESSAGE_FAILED = (byte) 0xE8;

	public static final byte CODE_SET_CALORIEMISSIONGOAL_SUCCESS = (byte) 0x70;
	public static final byte CODE_SET_CALORIEMISSIONGOAL_FAILURE = (byte) 0xE0;

	public static final byte CODE_GET_CALORIEMISSIONGOAL_SUCCESS = (byte) 0x71;
	public static final byte CODE_GET_CALORIEMISSIONGOAL_FAILURE = (byte) 0xE1;

	public static final byte CODE_SET_STATICIMAGE_SUCCESS = (byte) 0x72;
	public static final byte CODE_SET_STATICIMAGE_FAILURE = (byte) 0xE2;

	public static final byte CODE_SEND_ANIMATEDCELEBRATE_SUCCESS = (byte) 0x73;
	public static final byte CODE_SEND_ANIMATEDCELEBRATE_FAILURE = (byte) 0xE3;

	public static final byte CODE_SET_TOTAL_POWER_POINT_SUCCESS = (byte) 0x75;
	public static final byte CODE_SET_TOTAL_POWER_POINT_FAILURE = (byte) 0xC5;


	public boolean _success;
	public BlePeripheral _peripheral;

	public boolean isSuccess() {
		return _success;
	}

	public BlePeripheral peripheral() {
		return _peripheral;
	}

	public void setPeripheral(BlePeripheral peripheral) {
		_peripheral = peripheral;
	}

	public byte getCommandCode() {
		return 0;
	}
}
