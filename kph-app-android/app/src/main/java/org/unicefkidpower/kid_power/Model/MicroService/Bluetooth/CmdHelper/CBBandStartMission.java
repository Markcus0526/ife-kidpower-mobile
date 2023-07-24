package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper;

import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamStartMission;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.BandCommandResponse;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.onBandActionListener;

import java.util.Date;

/**
 * Created by Dayong Li on 9/28/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class CBBandStartMission extends CBBandCommandHelper {
	private String					TAG = "CBBandStartMission";

	protected static final int 		SUBPHASE_ENABLE_MISSION = 1;
	protected static final int 		SUBPHASE_CHECK_MISSION = 2;

	protected int					phase;
	protected int					goalCalories;
	protected int					totalPowerPoints;

	public CBBandStartMission(CBParamStartMission param, final onBandActionListener callback) {
		super(param.context, param.deviceCode, callback);

		this.goalCalories = param.goalCalories;
		totalPowerPoints = this.goalCalories / 50;
		phase = SUBPHASE_START;
	}

	@Override
	protected synchronized boolean nextCommand(BandCommandResponse object) {
		if (phase == SUBPHASE_START) {
			Logger.log(TAG, "nextCommand : STEP1 : setDeviceTime");
			phase = SUBPHASE_ENABLE_MISSION;
			device.setDeviceTime(new Date(), this);

			return true;
		} else if (phase == SUBPHASE_ENABLE_MISSION) {
			Logger.log(TAG, "nextCommand : STEP2 : setting Mission Calorie Value : %d", goalCalories);
			phase = SUBPHASE_CHECK_MISSION;
			device.setCalorieMissionGoal(goalCalories, this);

			return true;
		} else if (phase == SUBPHASE_CHECK_MISSION) {
			Logger.log(TAG, "nextCommand : STEP3 : getting Mission Calorie Value");
			phase = SUBPHASE_TERMINATING;
			device.getCalorieMissionGoal(this);

			return true;
		} else if (phase == SUBPHASE_TERMINATING) {
			Logger.log(TAG, "nextCommand : FINAL STEP : completed setting mission calorie");
			phase = SUBPHASE_TERMINATE;
			device.sendAnimatedCelebrationDisplay(this);

			return true;
		}
		return false;
	}
}
