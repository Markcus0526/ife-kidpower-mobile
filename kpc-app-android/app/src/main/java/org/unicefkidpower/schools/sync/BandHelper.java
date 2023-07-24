package org.unicefkidpower.schools.sync;

import android.app.Activity;
import android.os.Handler;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.ble.BleError;
import org.unicefkidpower.schools.ble.BleManager;
import org.unicefkidpower.schools.event.EventManager;
import org.unicefkidpower.schools.event.SEvent;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.powerband.PowerBandDevice;

/**
 * Created by Dayong on 8/4/2016.
 */

public abstract class BandHelper {
	protected String TAG = "BandHelper";

	protected static final int PHASE_NONE = -1;
	protected static final int PHASE_CONNECTING = 0;
	protected static final int PHASE_RUN_ACTION = 1;
	protected static final int PHASE_DISCONNECTING = 9;
	protected static final int PHASE_FINISHED = 10;

	static final String BAND_ACTION_CONNECTING_TIMEOUT = "BAND_ACTION_CONNECTION_TIMEOUT";
	static final String BAND_ACTION_COMMAND = "BAND_ACTION_NEXT";
	static final String BAND_ACTION_DONE = "BAND_ACTION_DONE";
	static final String BAND_ACTION_TIMEOUT = "BAND_ACTION_TIMEOUT";

	private static final int TIMEOUT_COMMAND = 120 * 1000;
	private static final int TIMEOUT_CONNECTING = 120 * 1000;
	private static final int TIMEOUT_DISCONNECTING = 15 * 1000;

	protected boolean _isSuccess = false;
	protected int _phase;

	protected Activity _activity;
	protected OnBandActionListener _callback;
	protected PowerBandDevice _band = null;

	protected Handler _handler;
	protected Runnable _runnableConnecting;
	protected Runnable _runnableCommand;
	protected Runnable _runnableDisconnect;
	protected Object result;

	protected int errorCode = BleError.BE_GENERAL;
	protected String errorMsg = null;

	boolean isRegistered = false;

	public BandHelper(Activity activity, OnBandActionListener callback) {
		_activity = activity;
		_callback = callback;

		_phase = PHASE_NONE;
		_isSuccess = false;

	}

	///////////////////////// public members ///////////////////////

	public void setBand(PowerBandDevice band) {
		_band = band;
	}

	public void run() {
		if (!isRegistered) {
			isRegistered = true;
			EventManager.sharedInstance().register(this);
		}

		if (_activity == null || _band == null) {
			_onError(BleError.BE_GENERAL, "Parameters should be valid.");
			return;
		}

		_handler = new Handler(_activity.getMainLooper());

		_runnableCommand = new Runnable() {
			@Override
			public void run() {
				Logger.error(TAG, "Command Executing timeout");
				EventManager.sharedInstance().post(BAND_ACTION_TIMEOUT, _band);
			}
		};

		_runnableConnecting = new Runnable() {
			@Override
			public void run() {
				Logger.error(TAG, "Connect timeout");
				EventManager.sharedInstance().post(BAND_ACTION_CONNECTING_TIMEOUT, _band);
			}
		};

		_runnableDisconnect = new Runnable() {
			@Override
			public void run() {
				Logger.error(TAG, "disconnect timeout, didn't disconnect yet, but do callback");
				if (_isSuccess) {
					_callback.success(result);
				} else {
					didFailed(errorCode, errorMsg);
				}
			}
		};

		if (!connect()) {
			_onError(BleError.BE_CONNECT_FAILED, _activity.getString(R.string.registerband_connect_failed));
		}
	}

	protected void runCommand() {
		_lastCommand();
	}

	public void stop() {
		if (_phase == PHASE_NONE ||
				_phase == PHASE_CONNECTING) {
			disconnect(false);
			finalizing();
		} else if (_phase == PHASE_RUN_ACTION) {
			disconnect(false);
		} else if (_phase == PHASE_DISCONNECTING ||
				_phase == PHASE_FINISHED) {
			finalizing();
		}
	}

	/////////////////////////////// protected members ///////////////////////////////////

	protected void finalizing() {
		Logger.log(TAG, "final processing");
		if (isRegistered) {
			EventManager.sharedInstance().unregister(this);
			isRegistered = false;
		}
		if (_handler != null) {
			_handler.removeCallbacks(_runnableConnecting);
			_handler.removeCallbacks(_runnableCommand);
			_handler.removeCallbacks(_runnableDisconnect);
		}
	}

	/**
	 * _nextCommand : run next step's command, should be called.
	 * if there are no needed command any more, don't call this function.
	 * instead call _lastCommand()
	 **/
	protected void _nextCommand() {
		EventManager.sharedInstance().post(BAND_ACTION_COMMAND, _band);
	}

	protected void _lastCommand() {
		EventManager.sharedInstance().post(BAND_ACTION_DONE, _band);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent e) {
		if (EventManager.isEvent(e, PowerBandDevice.kPowerBandDeviceDisconnectedNotification)) {
			if (e.object != _band || _band == null) {
				Logger.error(TAG, "band disconnected : but not for this %s:%s",
						e.object == null ? "null" : ((PowerBandDevice) e.object).deviceId(),
						_band == null ? "null" : _band.deviceId());
			} else {
				Logger.log(TAG, "band disconnected - isSuccess = " + _isSuccess);

				_phase = PHASE_FINISHED;
				_handler.removeCallbacks(_runnableDisconnect);

				if (_isSuccess) {
					// disconnected
					_callback.success(result);
				} else {
					didFailed(errorCode, errorMsg);
				}
				finalizing();
			}
		} else if (EventManager.isEvent(e, PowerBandDevice.kPowerBandDeviceConnectedNotification)) {
			if (e.object != _band || _band == null) {
				Logger.error(TAG, "band connected : but not for this %s:%s",
						e.object == null ? "null" : ((PowerBandDevice) e.object).deviceId(),
						_band == null ? "null" : _band.deviceId());
			} else {
				Logger.log(TAG, "band connected");
				_handler.removeCallbacks(_runnableConnecting);
				_callback.connected();
				_phase = PHASE_RUN_ACTION;
				_nextCommand();
			}
		} else if (EventManager.isEvent(e, BAND_ACTION_CONNECTING_TIMEOUT)) {
			if (e.object != _band || _band == null) {
				Logger.error(TAG, "band connecting timeout : not for this");
			} else {
				_onError(BleError.BE_CONNECT_TIMEOUT, "could not connect, timeout");
			}
		} else if (EventManager.isEvent(e, BAND_ACTION_COMMAND)) {
			if (e.object != _band || _band == null) {
				Logger.error(TAG, "band action command : not for this %s:%s",
						e.object == null ? "null" : ((PowerBandDevice) e.object).deviceId(),
						_band == null ? "null" : _band.deviceId());
			} else {
				// Logger.log(TAG, "run command");
				// remove old triggered timer
				_handler.removeCallbacks(_runnableCommand);
				// trigger new timer
				_handler.postDelayed(_runnableCommand, TIMEOUT_COMMAND);
				runCommand();
			}
		} else if (EventManager.isEvent(e, BAND_ACTION_DONE)) {
			if (e.object != _band || _band == null) {
				Logger.error(TAG, "band action done : but not for this %s:%s",
						e.object == null ? "null" : ((PowerBandDevice) e.object).deviceId(),
						_band == null ? "null" : _band.deviceId());
			} else {
				Logger.log(TAG, "done command, disconnecting band");
				_handler.removeCallbacks(_runnableCommand);

				_isSuccess = true;
				disconnect(true);
			}
		} else if (EventManager.isEvent(e, BAND_ACTION_TIMEOUT)) {
			if (e.object != _band || _band == null) {
				Logger.error(TAG, "band action timeout, but not for this %s:%s",
						e.object == null ? "null" : ((PowerBandDevice) e.object).deviceId(),
						_band == null ? "null" : _band.deviceId());
			} else {
				_onError(BleError.BE_COMMAND_TIMEOUT, "Action timeout");
			}
		}
	}

	protected boolean connect() {
		boolean ret = BleManager.sharedInstance().connectPeripheral(_band.peripheral());
		if (!ret) {
			Logger.log(TAG, "connectPeripheral(%s) - returned false", _band.address());
			_band.setDeviceState(PowerBandDevice.DEVICE_DISCONNECTED);
			return false;
		}

		_handler.postDelayed(_runnableConnecting, TIMEOUT_CONNECTING);

		_band.setDeviceState(PowerBandDevice.DEVICE_CONNECTING);
		_phase = PHASE_CONNECTING;

		return true;
	}

	protected void disconnect(boolean shouldTriggerCallBack) {
		if (_band.deviceState() != PowerBandDevice.DEVICE_DISCONNECTED) {
			Logger.log(TAG, "will disconnect, trigger callback %s", shouldTriggerCallBack ? "YES" : "NO");

			_phase = PHASE_DISCONNECTING;
			_band.disconnect();
			BleManager.sharedInstance().disconnectPeripheral(_band.peripheral());

			if (shouldTriggerCallBack) {
				_handler.postDelayed(_runnableDisconnect, TIMEOUT_DISCONNECTING);
			}
		} else {
			Logger.error(TAG, "Already disconnected, trigger callback %s", shouldTriggerCallBack ? "YES" : "NO");

			_phase = PHASE_FINISHED;
			if (shouldTriggerCallBack) {
				if (_isSuccess) {
					// disconnected
					_callback.success(result);
				} else {
					didFailed(errorCode, errorMsg);
				}
			}
		}
	}

	protected void _onError(int code, String message) {
		errorCode = code;
		errorMsg = message;

		Logger.error(TAG, "error code : %s, message : %s", errorCode, errorMsg);

		// disconnect
		if (_phase == PHASE_RUN_ACTION) {
			disconnect(true);
		} else {
			finalizing();
			didFailed(errorCode, errorMsg);
		}
	}

	void didFailed(int code, String message) {
		if (code == BleError.BE_GENERAL) {
			message = _activity.getString(R.string.sync_unknown_error);
		}
		_callback.failed(code, message);
	}
}
