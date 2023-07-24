package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper;

import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamLinkBand;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.BandCommandResponse;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetCalorieMissionGoalParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetFirmwareVersionParser;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.onBandActionListener;

import java.util.Date;

/**
 * Created by Dayong Li on 9/28/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class CBBandLinkBand extends CBBandCommandHelper {
	private String					TAG = "CBBandLinkBand";

	protected static final int 		SUBPHASE_GET_FIRMWARE = 0;
	protected static final int 		SUBPHASE_SET_DATETIME = 1;
	protected static final int 		SUBPHASE_SET_USERNAME = 2;

	protected static final int 		SUBPHASE_GET_GOAL = 3;
	protected static final int 		SUBPHASE_SET_GOAL = 4;
	protected static final int 		SUBPHASE_DISABLE_GOAL = 5;

	protected int					phase;
	protected String				name;
	protected int					current_goal;

	public CBBandLinkBand(CBParamLinkBand param, final onBandActionListener callback) {
		super(param.context, param.deviceCode, callback);

		name = param.name;
		current_goal = param.goal;
		phase = SUBPHASE_START;
	}

	@Override
	protected synchronized boolean nextCommand(BandCommandResponse object) {
		if (phase == SUBPHASE_START) {
			Logger.log(TAG, "nextCommand : STEP1 : getV1 Firmware version");
			phase = SUBPHASE_GET_FIRMWARE;
			device.getFirmwareVersion(this);
			return true;
		} else if (phase == SUBPHASE_GET_FIRMWARE) {
			String firmVersion = "";
			if (object instanceof CmdGetFirmwareVersionParser.CmdGetFirmwareVersionRes) {
				CmdGetFirmwareVersionParser.CmdGetFirmwareVersionRes res = (CmdGetFirmwareVersionParser.CmdGetFirmwareVersionRes) object;
				firmVersion = res.getFirmwareVersion();
			}
			Logger.log(TAG, "nextCommand : STEP2 : Firmware version = %s, next step is Setting Device Time", firmVersion);
			phase = SUBPHASE_SET_DATETIME;
			device.setDeviceTime(new Date(), this);
			return true;
		} else if (phase == SUBPHASE_SET_DATETIME) {
			Logger.log(TAG, "nextCommand : STEP3 : getting mission goal");

			phase = SUBPHASE_GET_GOAL;
			device.getCalorieMissionGoal(this);
			return true;
		} else if (phase == SUBPHASE_GET_GOAL) {
			Logger.log(TAG, "nextCommand : STEP4 : done getting goal command");

			boolean onCurrentMission = false;

			if (object != null && object instanceof CmdGetCalorieMissionGoalParser.CmdGetCalorieMissionGoalRes) {
				CmdGetCalorieMissionGoalParser.CmdGetCalorieMissionGoalRes res = (CmdGetCalorieMissionGoalParser.CmdGetCalorieMissionGoalRes) object;
				do {
					if (!res.enabledGoal())
						break;

					if (res.getGoalCalories() != current_goal)
						break;

					onCurrentMission = true;
				} while (false);
			}

			if (onCurrentMission) {
				// skip reset goal command, go directly to set name phase
				Logger.log(TAG, "nextCommand : STEP4 : setting user name as %s", name);
				phase = SUBPHASE_SET_USERNAME;
				device.setUserName(name, this);
			} else if (current_goal != 0) {
				Logger.log(TAG, "nextCommand : STEP4 : setting goal %d", current_goal);
				phase = SUBPHASE_SET_GOAL;
				device.setCalorieMissionGoal(current_goal, this);
			} else {
				Logger.log(TAG, "nextCommand : STEP4 : resetting goal mode");
				phase = SUBPHASE_DISABLE_GOAL;
				device.resetCalorieMissionGoal(this);
			}
			return true;
		} else if (phase == SUBPHASE_DISABLE_GOAL || phase == SUBPHASE_SET_GOAL) {
			Logger.log(TAG, "nextCommand : STEP5 : setting user name as %s", name);
			phase = SUBPHASE_SET_USERNAME;
			device.setUserName(name, this);
			return true;
		} else if (phase == SUBPHASE_SET_USERNAME) {
			Logger.log(TAG, "nextCommand : FINAL STEP : completed linking band");
			phase = SUBPHASE_TERMINATING;
			//_device.sendAnimatedCelebrationDisplay(this);
			return false;
		}

		return false;
	}
}
