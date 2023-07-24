package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper;

import org.unicefkidpower.kid_power.Model.Structure.KPHDailyDetailData;
import org.unicefkidpower.kid_power.Model.Structure.TrackerSyncResult;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamSync;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.BandCommandResponse;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetDailyDataParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetFirmwareVersionParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetTotalPowerPointParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetUserNameParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdSetTotalPowerPoint;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.OSDate;
import org.unicefkidpower.kid_power.Misc.onBandActionListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Dayong Li on 9/28/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class CBBandSync extends CBBandCommandHelper {
	private String					TAG = "CBBandSync";

	protected static final int 		SUBPHASE_GET_FIRMWARE = 0;
	protected static final int 		SUBPHASE_GET_DAILYACTIVITIES = 1;

	protected static final int 		SUBPHASE_SET_POWERPOINT = 2;
	protected static final int 		SUBPHASE_GET_POWERPOINT = 3;

	protected static final int 		SUBPHASE_CHECK_NAME = 4;
	protected static final int 		SUBPHASE_SET_NAME = 5;

	private int						phase;

	private int						days;
	private String					username;
	private Date					last_sync_date = null;
	private int						goal_pps = 0;
	private OSDate					today;

	private List<KPHDailyDetailData>		activities;
	private KPHDailyDetailData				filterData;// filter by current date, for new pps

	private boolean							supportCallSetPPCommand = false;
	private int								fwVersion = 0xFFFF;


	public CBBandSync(CBParamSync param, final onBandActionListener callback) {
		super(param.context, param.deviceCode, callback);

		this.phase = SUBPHASE_START;
		this.days = param.days + 1;
		this.username = param.name;
		this.goal_pps = param.totalPowerPoints;
		this.last_sync_date = param.lastSyncDate;

		today = new OSDate(new Date());

		activities = new ArrayList<>();
	}

	@Override
	protected synchronized boolean nextCommand(BandCommandResponse object) {
		if (phase == SUBPHASE_START) {
			phase = SUBPHASE_GET_FIRMWARE;
			device.getFirmwareVersion(this);
			return true;
		} else if (phase == SUBPHASE_GET_FIRMWARE) {
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

			supportCallSetPPCommand = CmdSetTotalPowerPoint.SupportedFirmwareVersion(fwVersion);

			Logger.log(TAG, "nextCommand : STEP1 : version=\"" + fwVersion + "\" support SetPowerPoint=" + (supportCallSetPPCommand ? "YES" : "NO"));

			phase = SUBPHASE_GET_DAILYACTIVITIES;
			device.setDeviceTime(new Date(), this);
			return true;
		} else if (phase == SUBPHASE_GET_DAILYACTIVITIES) {
			// next phase
			Logger.log(TAG, "nextCommand : STEP2 : setDeviceTime");

			if (object != null && object instanceof CmdGetDailyDataParser.CmdGetDailyDataRes) {
				KPHDailyDetailData dailyData = ((CmdGetDailyDataParser.CmdGetDailyDataRes) object).getDailyData();
				if (dailyData != null && !dailyData.isEmpty()) {
					dailyData.calculateTotals();

					OSDate synced_date = today.offsetDay(-1 * days);
					dailyData.date = synced_date.toStringWithFormat("yyyy-MM-dd");

					Logger.log(TAG, "nextCommand : STEP2 : %s(%d) daily data(calories=%.2f, distance=%.2f, steps=%d)",
							dailyData.date, days, dailyData.totalCalories, dailyData.totalDuration, dailyData.totalSteps);

					activities.add(dailyData);

					CBBandSyncReport report = new CBBandSyncReport();
					report.type = Report_Type.Sync_Success;
					report.date = synced_date;
					report.calories = dailyData.totalCalories;
					report.duration = dailyData.totalDuration;
					report.steps = dailyData.totalSteps;

					callback.reportStatus(report);
				}
			}

			days--;
			if (days >= 0) {
				OSDate syncing_date = today.offsetDay(-1 * days);
				CBBandSyncReport report = new CBBandSyncReport();
				report.type = Report_Type.Sync_Start;
				report.date = syncing_date;
				callback.reportStatus(report);

				phase = SUBPHASE_GET_DAILYACTIVITIES; // not needed, but to be clarify
				device.getDailyData(days, this);

				return true;
			} else {
				// WoW, You did it, Now we may go to next step

				// calculate new calories and new power points value
				filterData = KPHDailyDetailData.filterCalories(activities, last_sync_date);
				if (filterData == null) {
					filterData = new KPHDailyDetailData();
				}
				int new_pps = filterData.totalPowerPoints;

				Logger.log(TAG, "nextCommand : STEP2 : filter data(curPPS=%d, newPPS=%d(cal=%.2f), totalPPS=%d)",
						goal_pps, new_pps, filterData.totalCalories, goal_pps + new_pps);

				goal_pps += new_pps;//important
				if (supportCallSetPPCommand) {
					Logger.log(TAG, "nextCommand : STEP3 : setPPS(totalPPS=%d)", goal_pps);

					phase = SUBPHASE_SET_POWERPOINT;
					device.setTotalPowerPoints(goal_pps, this);
				} else {
					Logger.log(TAG, "nextCommand : STEP3 : getUserName");
					phase = SUBPHASE_CHECK_NAME;
					device.getUserName(this);
				}
				return true;
			}
		} else if (phase == SUBPHASE_SET_POWERPOINT) {
			Logger.log(TAG, "nextCommand : STEP3 : set powerpoint with %d", goal_pps);
			phase = SUBPHASE_GET_POWERPOINT;
			device.getTotalPowerPoints(this);
			return true;
		} else if (phase == SUBPHASE_GET_POWERPOINT) {
			Logger.log(TAG, "nextCommand : STEP3 : get powerpoint");
			if (object != null && object instanceof CmdGetTotalPowerPointParser.CmdGetTotalPowerPointRes) {
				long pps = ((CmdGetTotalPowerPointParser.CmdGetTotalPowerPointRes) object).getPowerPoint();
				Logger.log(TAG, "nextCommand : STEP3 : Band Total PowerPoint=%d, should be %d", pps, goal_pps);
			}
			phase = SUBPHASE_CHECK_NAME;
			device.getUserName(this);
			return true;
		} else if (phase == SUBPHASE_CHECK_NAME) {
			boolean equalName = false;
			String oldName = "";
			if (object != null && object instanceof CmdGetUserNameParser.CmdGetUserNameRes) {
				oldName = ((CmdGetUserNameParser.CmdGetUserNameRes) object).getUserName();
				if (oldName != null && username.toUpperCase().equals(oldName)) {
					equalName = true;
				}
			}
			if (equalName) {
				Logger.log(TAG, "nextCommand : STEP4 : name is %s, sendAnimatedCelebrationDisplay", oldName);
				phase = SUBPHASE_TERMINATING;
				device.sendAnimatedCelebrationDisplay(this);
			} else {
				Logger.log(TAG, "nextCommand : STEP4 : name is different(%s), so setting current username(%s)", oldName, username);
				phase = SUBPHASE_SET_NAME;
				device.setUserName(username, this);
			}
			return true;
		} else if (phase == SUBPHASE_SET_NAME || phase == SUBPHASE_TERMINATING) {
			Logger.log(TAG, "nextCommand : FINAL STEP : completed syncing");

			phase = SUBPHASE_TERMINATE;

			TrackerSyncResult syncResult = new TrackerSyncResult();
			syncResult.activities = activities;
			if (filterData != null) {
				syncResult.newCalories = filterData.totalCalories;
				syncResult.newDuration = filterData.totalDuration;
				syncResult.newSteps = filterData.totalSteps;
				syncResult.newPowerPoints = filterData.totalPowerPoints;
			}
			returnObject = syncResult;

			return false;
		}
		return false;
	}

	public enum Report_Type {
		Sync_Start,
		Sync_Success,
	}

	public class CBBandSyncReport {
		public Report_Type type;

		public Date date;
		public double calories;
		public double duration;
		public int steps;
	}
}
