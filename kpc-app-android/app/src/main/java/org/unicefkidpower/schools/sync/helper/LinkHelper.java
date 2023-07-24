package org.unicefkidpower.schools.sync.helper;

import android.app.Activity;

import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.ble.BleError;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.powerband.PowerBandDevice;
import org.unicefkidpower.schools.powerband.command.BandCommandResponse;
import org.unicefkidpower.schools.powerband.command.CmdDeviceFirmwareGet;
import org.unicefkidpower.schools.sync.BandHelper;
import org.unicefkidpower.schools.sync.OnBandActionListener;

import java.util.Date;

/**
 * Created by Dayong on 8/11/2016.
 */

public class LinkHelper extends BandHelper {
	enum Sub_Step {
		Get_Firmware_Version,
		Set_Person_Info,
		Set_Message1,
		Set_TeamMessage,
		Set_Time,
		Set_Name,
		Set_TotalPowerPoints,
	}

	public static class LinkResult {
		public String version;
		public String previous;
	}

	public static final String PERSONAL_MESSAGE = "KEEP GOING!";

	public static final int SLOT_FOR_MESSAGE = 2;
	public static final int SLOT_FOR_TEAM_MESSAGE = 3;

	String name;
	int height;
	int weight;
	int stride;
	String team_message;
	int total_points;

	Sub_Step _step;

	LinkResult _result;

	public LinkHelper(Activity activity, OnBandActionListener callback) {
		super(activity, callback);
		TAG = "LinkHelper";
	}

	public BandHelper setParameter(String name, int height, int weight, int stride, String teamMessage, int totalPowerPoints) {
		this.name = name;
		this.height = height;
		this.weight = weight;
		this.stride = stride;
		this.team_message = teamMessage;
		this.total_points = totalPowerPoints;

		return this;
	}

	@Override
	public void run() {

		Logger.log(TAG, "start linking band : deviceId=%s(%s)",
				_band.peripheral().getMACAddress(), _band.peripheral().getCode());

		_step = Sub_Step.Get_Firmware_Version;

		_result = new LinkResult();
		super.run();
	}

	protected synchronized void runCommand() {
		if (_step == Sub_Step.Get_Firmware_Version) {
			Logger.log(TAG, "phase1, getting firmware version");

			_callback.updateStatus(_activity.getString(R.string.registerband_updating_configuration));

			_band.getFirmwareVersion(new PowerBandDevice.WriteCommandCallback() {
				@Override
				public void onWrite(boolean success, BandCommandResponse response) {
					if (success) {
						CmdDeviceFirmwareGet.CmdGetFirmwareVersionRes res =
								(CmdDeviceFirmwareGet.CmdGetFirmwareVersionRes) response;

						_result.version = res.getFirmwareVersion();
						Logger.log(TAG, "firmware version : %s", _result.version);

						_step = Sub_Step.Set_Person_Info;
						_nextCommand();
					} else {
						_onError(BleError.BE_COMMAND_FAILED, "Getting Firmware Version");
					}
				}
			});
		} else if (_step == Sub_Step.Set_Person_Info) {
			Logger.log(TAG, "phase2, setting personal information : height=%d, weight=%d, stride=%d",
					height, weight, stride);

			_callback.updateStatus(_activity.getString(R.string.registerband_updating_configuration));

			_band.setPersonalInformation(0, 10, height, weight, stride,
					new PowerBandDevice.WriteCommandCallback() {
						@Override
						public void onWrite(boolean success, BandCommandResponse response) {
							if (success) {
								_step = Sub_Step.Set_Message1;
								_nextCommand();
							} else {
								_onError(BleError.BE_COMMAND_FAILED, "Setting Personal Information");
							}
						}
					});
		} else if (_step == Sub_Step.Set_Message1) {
			Logger.log(TAG, "phase3, setting message(%s)", PERSONAL_MESSAGE);

			_callback.updateStatus(_activity.getString(R.string.registerband_updating_message1));

			_band.setMessage(SLOT_FOR_MESSAGE, PERSONAL_MESSAGE,
					new PowerBandDevice.WriteCommandCallback() {
						@Override
						public void onWrite(boolean success, BandCommandResponse response) {
							if (success) {
								_step = Sub_Step.Set_TeamMessage;
								_nextCommand();
							} else {
								_onError(BleError.BE_COMMAND_FAILED, "Setting Message");
							}
						}
					});

		} else if (_step == Sub_Step.Set_TeamMessage) {
			Logger.log(TAG, "phase4, setting team message(%s)", team_message);
			_callback.updateStatus(_activity.getString(R.string.registerband_updating_message2));

			_band.setMessage(SLOT_FOR_TEAM_MESSAGE, team_message,
					new PowerBandDevice.WriteCommandCallback() {
						@Override
						public void onWrite(boolean success, BandCommandResponse response) {
							if (success) {
								_step = Sub_Step.Set_Time;
								_nextCommand();
							} else {
								_onError(BleError.BE_COMMAND_FAILED, "Setting Team Message");
							}
						}
					});
		} else if (_step == Sub_Step.Set_Time) {
			Date now = new Date();
			Logger.log(TAG, "phase5, setting time(%s)", now.toString());
			_callback.updateStatus(_activity.getString(R.string.registerband_updating_time));
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
			Logger.log(TAG, "phase6, setting user name : %s", name);
			_callback.updateStatus(_activity.getString(R.string.registerband_updating_name));

			_band.setName(name, new PowerBandDevice.WriteCommandCallback() {
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
			_callback.updateStatus(_activity.getString(R.string.registerband_updating_totalpoints));
			_band.setTotalPowerPoints(total_points, new PowerBandDevice.WriteCommandCallback() {
				@Override
				public void onWrite(boolean success, BandCommandResponse response) {
					if (success) {
						result = _result;
						_lastCommand();
					} else {
						_onError(BleError.BE_COMMAND_FAILED, "Setting Total Power Points command did failed.");
					}
				}
			});
		}
	}
}
