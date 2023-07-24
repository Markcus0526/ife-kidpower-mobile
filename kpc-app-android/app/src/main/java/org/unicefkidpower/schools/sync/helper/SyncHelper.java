package org.unicefkidpower.schools.sync.helper;

import android.app.Activity;

import org.unicefkidpower.schools.ble.BleError;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.helper.OSDate;
import org.unicefkidpower.schools.model.DailyActivityData;
import org.unicefkidpower.schools.powerband.PowerBandDevice;
import org.unicefkidpower.schools.powerband.command.BandCommandResponse;
import org.unicefkidpower.schools.powerband.command.CmdDailyDetailedGet.CmdDailyDetailedRes;
import org.unicefkidpower.schools.powerband.command.CmdDailySummaryGet.CmdDailySummaryGetRes;
import org.unicefkidpower.schools.powerband.command.CmdDeviceFirmwareGet;
import org.unicefkidpower.schools.powerband.command.CmdDeviceTimeGet;
import org.unicefkidpower.schools.powerband.command.CmdSetTotalPowerPoint;
import org.unicefkidpower.schools.sync.BandHelper;
import org.unicefkidpower.schools.sync.OnBandActionListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Dayong on 8/11/2016.
 */

public class SyncHelper extends BandHelper {
	private final boolean NEED_SET_POWERPOINTS = true;

	enum Sub_Step {
		Get_Device_Version,
		Get_Device_Time,
		Get_Daily_Detail,
		Get_Daily_Summary,
		Set_Time,
		Set_Name,
		Set_TotalPowerPoints,
	}

	public static class SyncResult {
		public String							version;
		public long								drift;
		public long								steps;
		public long								points;
		public long								rutf;
		public ArrayList<DailyActivityData>		datas;
	}


	private Sub_Step							_step				= null;

	private String								_name				= "";
	private int									_daysNeededSync		= 0;
	private int									_syncingDay			= 0;
	private Date								_current			= null;
	private int									_powerPoint			= 0;

	private SyncResult							_result				= null;
	private DailyActivityData					_activity			= null;


	public SyncHelper(Activity activity, OnBandActionListener callback) {
		super(activity, callback);
		TAG = "SyncHelper";
	}

	public BandHelper setParameter(Date current, int daysForSync, String name, int powerPoint) {
		this._current = current;
		this._daysNeededSync = daysForSync;
		this._syncingDay = daysForSync;
		this._name = name;
		this._powerPoint = powerPoint;

		return this;
	}

	@Override
	public void run() {
		Logger.log(TAG, "start Syncing name:%s device:%s(%s) for %d days", _name,
				_band.peripheral().getMACAddress(), _band.peripheral().getCode(), _syncingDay);

		Logger.log(TAG, "connecting to device");
		_step = Sub_Step.Get_Device_Version;

		_result = new SyncResult();
		_result.datas = new ArrayList<>();

		super.run();
	}

	protected synchronized void runCommand() {
		if (_step == Sub_Step.Get_Device_Version) {
			Logger.log(TAG, "phase1, getting firmware version");

			_band.getFirmwareVersion(new PowerBandDevice.WriteCommandCallback() {
				@Override
				public void onWrite(boolean success, BandCommandResponse response) {
					if (success) {
						CmdDeviceFirmwareGet.CmdGetFirmwareVersionRes res =
								(CmdDeviceFirmwareGet.CmdGetFirmwareVersionRes) response;

						_result.version = res.getFirmwareVersion();
						Logger.log(TAG, "success getting firmware version : %s", _result.version);

						_step = Sub_Step.Get_Device_Time;
						_nextCommand();
					} else {
						_onError(BleError.BE_COMMAND_FAILED, "Getting Firmware Version");
					}
				}
			});
		} else if (_step == Sub_Step.Get_Device_Time) {
			Logger.log(TAG, "phase2, getting device time");

			_band.getDeviceTime(new PowerBandDevice.WriteCommandCallback() {
				@Override
				public void onWrite(boolean success, BandCommandResponse response) {
					if (success) {
						CmdDeviceTimeGet.CmdDeviceTimeGetRes res = (CmdDeviceTimeGet.CmdDeviceTimeGetRes) response;
						CmdDeviceTimeGet.DeviceDateTime dateTime = res.getDeviceDateTime();

						_result.drift = calcDrift(dateTime);
						Logger.log(TAG, "success getting device time, drift = %d", _result.drift);

						_step = Sub_Step.Get_Daily_Detail;
						_nextCommand();
					} else {
						_onError(BleError.BE_COMMAND_FAILED, "Getting device time");
					}
				}
			});
		} else if (_step == Sub_Step.Get_Daily_Detail) {
			Logger.log(TAG, "phase3-1, getting detail data for %d/%d day ago",
					_syncingDay, _daysNeededSync);

			_callback.reportForDaily(false, _syncingDay, _daysNeededSync);

			_band.getDailyDetailedData(_syncingDay, new PowerBandDevice.WriteCommandCallback() {
				@Override
				public void onWrite(boolean success, BandCommandResponse response) {
					if (success) {
						CmdDailyDetailedRes res = (CmdDailyDetailedRes) response;
						res.setBeforeDay(_syncingDay, _current);

						String dateString = String.format("%04d-%02d-%02d", res.year, res.month + 1, res.day);

						Logger.log(TAG, "phase3-1, got detail data for %s(%d ago)",
								dateString, _syncingDay);

						_activity = new DailyActivityData();
						_activity.date = dateString;
						_activity.data = res.getDailyData();

						_step = Sub_Step.Get_Daily_Summary;
						_nextCommand();
					} else {
						_onError(BleError.BE_COMMAND_FAILED, "Getting detail data");
					}
				}
			});
		} else if (_step == Sub_Step.Get_Daily_Summary) {
			Logger.log(TAG, "phase3-2, getting summary data for %d/%d day ago",
					_syncingDay, _daysNeededSync);

			_band.getDailySummaryData(_syncingDay, new PowerBandDevice.WriteCommandCallback() {
				@Override
				public void onWrite(boolean success, BandCommandResponse response) {
					if (success) {
						Logger.log(TAG, "phase3-2, got summary data for %d ago", _syncingDay);

						CmdDailySummaryGetRes res = (CmdDailySummaryGetRes) response;

						_activity.summary = res.getDailySummaryData();
						_result.datas.add(_activity);

						_callback.reportForDaily(true, _syncingDay, _daysNeededSync);

						// checking end
						_syncingDay--;
						if (_syncingDay >= 0) {
							// syncing again
							_step = Sub_Step.Get_Daily_Detail;
							_nextCommand();
						} else {
							_step = Sub_Step.Set_Time;
							_nextCommand();
						}
					} else {
						_onError(BleError.BE_COMMAND_FAILED, "Getting summary data");
					}
				}
			});
		} else if (_step == Sub_Step.Set_Time) {
			result = _result;

			Date now = new Date();
			Logger.log(TAG, "phase4, setting time(%s)", now.toString());
			_band.setDeviceTime(now,
					new PowerBandDevice.WriteCommandCallback() {
						@Override
						public void onWrite(boolean success, BandCommandResponse response) {
							if (success) {
								_step = Sub_Step.Set_Name;
								_nextCommand();
							} else {
								_onError(BleError.BE_COMMAND_FAILED, "Setting Time");
							}
						}
					});
		} else if (_step == Sub_Step.Set_Name) {
			Logger.log(TAG, "phase5, setting user name : %s", _name);
			_band.setName(_name, new PowerBandDevice.WriteCommandCallback() {
				@Override
				public void onWrite(boolean success, BandCommandResponse response) {
					if (success) {
						_step = Sub_Step.Set_TotalPowerPoints;
						_nextCommand();
					} else {
						_onError(BleError.BE_COMMAND_FAILED, "Setting Name command did failed.");
					}
				}
			});
		} else if (_step == Sub_Step.Set_TotalPowerPoints) {
			// Calculate new ppts
			double totalCalories = 0;
			for (int i = 0; i < _result.datas.size(); i++) {
				DailyActivityData dailyActivityData = _result.datas.get(i);
				if (dailyActivityData.summary.calories == 0) {
					totalCalories += dailyActivityData.summary.steps * 0.4f;
				} else {
					totalCalories += dailyActivityData.summary.calories;
				}
			}

			_result.points = _powerPoint + (int)(totalCalories / 50);

			Logger.log(TAG, "phase6, setting total power points. Org : %d, New : %d, totalCalories : %f", _powerPoint, _result.points, totalCalories);

			if (NEED_SET_POWERPOINTS) {
				if (!CmdSetTotalPowerPoint.SupportedFirmwareVersion(_result.version)) {
					_lastCommand();
					Logger.error(TAG, "setTotalPowerPoints failed, not supported firmware : " + _result.version);
					return;
				} else {
					Logger.log(TAG, "firmware is supported : " + _result.version);
					_band.setTotalPowerPoints((int) _result.points, new PowerBandDevice.WriteCommandCallback() {
						@Override
						public void onWrite(boolean success, BandCommandResponse response) {
							if (success) {
								Logger.log(TAG, "setting total power points succeeded");
								_lastCommand();
							} else {
								Logger.log(TAG, "setting total power points failed");
								_onError(BleError.BE_COMMAND_FAILED, "Setting Total power points command did failed.");
							}
						}
					});
				}
			} else {
				Logger.log(TAG, "No need to set powerpoints");
				_lastCommand();
				return;
			}
		}
	}

	protected long calcDrift(CmdDeviceTimeGet.DeviceDateTime dateTime) {
		if (dateTime == null)
			return 0;

		long drift;
		try {
			String strDate = String.format("20%02d/%02d/%02d %02d:%02d:%02d",
					dateTime.year, dateTime.month, dateTime.day,
					dateTime.hour, dateTime.minute, dateTime.second);

			SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
			drift = OSDate.betweenDates(new Date(), format.parse(strDate)) / 1000;

			Logger.log(TAG, "Drift : %d seconds (deviceTime=%s)", drift, strDate);
		} catch (ParseException e) {
			drift = 0;
		}
		return drift;
	}
}
