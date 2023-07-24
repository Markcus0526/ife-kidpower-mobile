package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command;

import org.unicefkidpower.kid_power.Model.Structure.KPHDailyDetailData;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.OSDate;

import java.util.Date;


/**
 * Created by Dayong Li on 9/29/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CmdGetDailyDataParser extends BandCommandParser {
	private static final String TAG = "CmdGetDailyDataParser";
	static final String DateTimeFormatString = "yyyy-MM-dd HH:mm:ss";
	static final String UTCDateFormatString = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	private static CmdGetDailyDataParser _instance;

	KPHDailyDetailData data;

	private CmdGetDailyDataParser() {
		success_code = BandCommandResponse.CODE_GET_DAILYDATA_SUCCESS;
		failed_code = BandCommandResponse.CODE_GET_DAILYDATA_FAILED;
	}

	public static CmdGetDailyDataParser sharedInstance() {
		if (_instance == null)
			_instance = new CmdGetDailyDataParser();
		return _instance;
	}

	@Override
	public int parse(byte code, byte[] response) {
		_response = new CmdGetDailyDataRes();

		int ret = super.parse(code, response);
		if (ret != PARSE_FINISHED)
			return ret;

		int offsetUTC = KPHUtils.sharedInstance().getUTCOffset();

		if (code == success_code) {
			int year = convert(response[2]) + 2000;
			int month = convert(response[3]);
			int day = convert(response[4]);
			String snapData = String.format("%04d-%02d-%02d", year, month, day);

			int index = (0xFF & response[5]);
			if (index == 0) {
				data = new KPHDailyDetailData();
				data.setDate(snapData);
			}

			int totalmin = 15 * index;
			int hour = totalmin / 60;
			int min = totalmin % 60;

			if (((0xFF & response[1]) == 0xF0)) {
				// activity data
				if ((0xFF & response[6]) == 0) {
					int calories = (0x100 * (0xFF & response[8])) + (0xFF & response[7]);
					int steps = (0x100 * (0xFF & response[10])) + (0xFF & response[9]);
					int duration = (0x100 * (0xFF & response[12])) + (0xFF & response[11]);

					if (calories != 0 || duration != 0 || steps != 0) {
						// if it isn't no sleep data
						String time = String.format("%s %02d:%02d:00", snapData, hour, min);
						Date dt = OSDate.fromStringWithFormat(time, DateTimeFormatString, false);
						OSDate utcDate = new OSDate(dt).offsetByHHMMSS(0, offsetUTC, 0);
						KPHDailyDetailData.DetailItem dailyData =
								new KPHDailyDetailData.DetailItem(utcDate.toStringWithFormat(UTCDateFormatString), (double) calories / 100.0d, steps, duration * 60);
						data.addDetailItem(dailyData);
					}
				} else {
					// sleep data
				}

				// time scale index == 95 (the last data for daily data)
				if (index == 95) {
					_response._success = true;
					((CmdGetDailyDataRes) _response).setDailyData(data);
					return PARSE_FINISHED;
				} else {
					_response._success = true;
					return PARSE_WAITING;
				}

			} else if ((0xFF & response[1]) == 0xFF) {
				// there is no data
				//KPHDailyDetailData.DetailItem dailyData =
				//		new KPHDailyDetailData.DetailItem(String.format("0000-00-00T%02d:%02d:00Z", 0, 0), 0.f, 0, 0);
				//data.addDetailItem(dailyData);
				//_response._success = true;
				//((CmdGetDailyDataRes) _response).setDailyData(data);
				_response._success = false;
				return PARSE_FINISHED;
			} else {
				_response._success = false;
				return PARSE_ERROR;
			}
		} else {
			_response._success = false;
			return PARSE_FINISHED;
		}
	}

	public class CmdGetDailyDataRes extends BandCommandResponse {
		protected KPHDailyDetailData _data;

		public void setDailyData(KPHDailyDetailData data) {
			_data = data;
		}

		public KPHDailyDetailData getDailyData() {
			return _data;
		}

		@Override
		public byte getCommandCode() {
			return success_code;
		}
	}

	public static int convert(int n) {
		return Integer.valueOf(String.format("%x", n), 10);
	}
}
