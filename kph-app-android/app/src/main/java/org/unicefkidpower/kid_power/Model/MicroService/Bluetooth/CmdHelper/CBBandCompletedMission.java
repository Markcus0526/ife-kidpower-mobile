package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper;

import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamSimple;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.BandCommandResponse;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.onBandActionListener;

import java.util.Date;

/**
 * Created by Dayong Li on 9/28/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class CBBandCompletedMission extends CBBandCommandHelper {
	private String					TAG = "CBBandCompletedMission";

	protected static final int 		SUBPHASE_ENABLE_MISSION = 1;
	protected int					phase;

	public CBBandCompletedMission(CBParamSimple param, final onBandActionListener callback) {
		super(param.context, param.deviceCode, callback);

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
			Logger.log(TAG, "nextCommand : STEP2 : reset mission mode");
			phase = SUBPHASE_TERMINATING;
			device.resetCalorieMissionGoal(this);
			return true;
		} else if (phase == SUBPHASE_TERMINATING) {
			Logger.log(TAG, "nextCommand : FINAL STEP : completed reset mission mode");
			phase = SUBPHASE_TERMINATE;
			device.sendAnimatedCelebrationDisplay(this);
		}

		return false;
	}
}
