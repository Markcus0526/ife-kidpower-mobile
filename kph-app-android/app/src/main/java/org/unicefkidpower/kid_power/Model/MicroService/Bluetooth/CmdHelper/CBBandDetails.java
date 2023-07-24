package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper;

import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamBandDetails;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.BandCommandResponse;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetFirmwareVersionParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetUserNameParser;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.onBandActionListener;

/**
 * Created by Dayong Li on 9/28/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class CBBandDetails extends CBBandCommandHelper {
	private String					TAG = "CBBandDetails";

	protected static final int 		SUBPHASE_GET_FIRMWARE = 0;
	protected static final int 		SUBPHASE_CHECK_NAME = 1;

	protected int					phase;

	CBBandDetailsResult result = new CBBandDetailsResult();

	public CBBandDetails(CBParamBandDetails param, final onBandActionListener callback) {
		super(param.context, param.deviceCode, callback);

		this.phase = SUBPHASE_START;
	}

	@Override
	protected synchronized boolean nextCommand(BandCommandResponse object) {

		if (phase == SUBPHASE_START) {
			phase = SUBPHASE_GET_FIRMWARE;
			device.getFirmwareVersion(this);
			return true;
		} else if (phase == SUBPHASE_GET_FIRMWARE) {
			int fwVersion;

			Logger.log(TAG, "nextCommand : STEP1 : getV1 Firmware version");

			if (object != null && object instanceof CmdGetFirmwareVersionParser.CmdGetFirmwareVersionRes) {
				CmdGetFirmwareVersionParser.CmdGetFirmwareVersionRes versionRes = (CmdGetFirmwareVersionParser.CmdGetFirmwareVersionRes) object;
				String version = versionRes.getFirmwareVersion();

				try {
					fwVersion = Integer.valueOf(version);
				} catch (Exception ex) {
					fwVersion = 0x0;
				}
			} else {
				fwVersion = 0x0;
			}

			result.setFirmwareVersion(fwVersion);
			Logger.log(TAG, "nextCommand : STEP1 : version:\"" + fwVersion + "\"");

			phase = SUBPHASE_CHECK_NAME;
			device.getUserName(this);
			return true;
		} else if (phase == SUBPHASE_CHECK_NAME) {
			String userName = "";
			if (object != null && object instanceof CmdGetUserNameParser.CmdGetUserNameRes) {
				userName = ((CmdGetUserNameParser.CmdGetUserNameRes) object).getUserName();
			}
			Logger.log(TAG, "nextCommand : STEP2 : name=\"%s\"", userName);
			result.userName = userName;

			// TODO go to final step
			Logger.log(TAG, "nextCommand : FINAL STEP");
			returnObject = result;
			return false;
		}
		return false;
	}

	public class CBBandDetailsResult {
		public CBBandDetailsResult() {
			type = "Power Band";
		}

		String userName;
		int fwVersion;
		String version;
		String type;

		protected void setFirmwareVersion(int firmwareVersion) {
			String str = "" + firmwareVersion;

			fwVersion = firmwareVersion;

			byte byte1 = (Byte.valueOf("" + str.charAt(0)));
			byte byte2 = (Byte.valueOf("" + str.charAt(1)));
			byte byte3 = (Byte.valueOf("" + str.charAt(2)));
			byte byte4 = 0;

			version = String.format("%H.%H.%H%H", byte1, byte2, byte3, byte4);
		}

		public String getVersion() {
			return version;
		}

		public String getUserName() {
			return userName;
		}

		public String getType() {
			return type;
		}
	}
}
