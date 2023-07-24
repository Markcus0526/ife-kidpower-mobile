package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.BandModel.DailySummaryData;

/**
 * Created by Dayong Li on 9/29/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdGetDailySummaryDataParser extends BandCommandParser {
	private static CmdGetDailySummaryDataParser _instance;

	private int mSteps;
	private int mCalories;
	private int mDistance;
	private int mActivityTime;

	private CmdGetDailySummaryDataParser() {
		success_code = BandCommandResponse.CODE_GET_DAILYSUMMARYDATA_SUCCESS;
		failed_code = BandCommandResponse.CODE_GET_DAILYSUMMARYDATA_FAILED;
	}

	public static CmdGetDailySummaryDataParser sharedInstance() {
		if (_instance == null)
			_instance = new CmdGetDailySummaryDataParser();
		return _instance;
	}

	@Override
	public int parse(byte code, byte[] response) {
		_response = new CmdGetDailySummaryDataRes();

		int ret = super.parse(code, response);
		if (ret != PARSE_FINISHED)
			return ret;

		if (code == success_code) {
			if ((0xFF & response[1]) == 0) {
				mSteps = 0x100 * 0x100 * (0xFF & response[6])
						+ 0x100 * (255 & response[7])
						+ (255 & response[8]);
				mCalories = 0x100 * 0x100 * (0xFF & response[12]) +
						0x100 * (255 & response[13]) + (0xFF & response[14]);

				_response._success = true;
				return PARSE_WAITING;
			} else if ((0xFF & response[1]) == 1) {
				mDistance = 0x100 * 0x100 * (0xFF & response[6]) +
						0x100 * (0xFF & response[7]) + (0xFF & response[8]);
				mActivityTime = 0x100 * (0xFF & response[9])
						+ (0xFF & response[10]);

				DailySummaryData mDailySummaryData = new DailySummaryData();
				mDailySummaryData._steps = mSteps;
				mDailySummaryData._calories = mCalories / 100.0;
				mDailySummaryData._distance = mDistance / 100.0; //0.01km
				mDailySummaryData._activeTime = mActivityTime;

				((CmdGetDailySummaryDataRes) _response).setDailySummaryData(mDailySummaryData);
				_response._success = true;
				return PARSE_FINISHED;
			} else {
				_response._success = false;
				return PARSE_ERROR;
			}
		} else {
			_response._success = false;
		}
		return PARSE_FINISHED;
	}

	public class CmdGetDailySummaryDataRes extends BandCommandResponse {
		protected DailySummaryData _data;

		public void setDailySummaryData(DailySummaryData data) {
			_data = data;
		}

		public DailySummaryData getDailySummaryData() {
			return _data;
		}

		@Override
		public byte getCommandCode() {
			return success_code;
		}
	}

}
