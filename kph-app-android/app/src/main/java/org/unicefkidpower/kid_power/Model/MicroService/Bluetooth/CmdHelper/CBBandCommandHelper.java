package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper;

import android.content.Context;
import android.os.Handler;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble.BleManager;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble.BlePeripheral;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.EventManager;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.SEvent;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.PowerBandDevice;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.BandCommandResponse;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.onBandActionListener;


/**
 * Created by Dayong Li on 9/28/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class CBBandCommandHelper implements PowerBandDevice.WriteCommandCallback {
	private String					TAG = "CBBandCommandHelper";

	protected boolean				isSuccess;
	protected int					phase;
	protected Context				context;
	protected onBandActionListener	callback;
	protected String				deviceCode;


	protected static final int		PHASE_NONE				= -1;
	protected static final int		PHASE_SCANNING			= 0;
	protected static final int		PHASE_CONNECTING		= 1;
	protected static final int		PHASE_COMMAND			= 2;
	protected static final int		PHASE_DISCONNECTING		= 9;
	protected static final int		PHASE_STOPPED			= 10;

	protected static final int		SUBPHASE_START			= -1;
	protected static final int		SUBPHASE_TERMINATING	= 100;
	protected static final int		SUBPHASE_TERMINATE		= 101;

	protected PowerBandDevice		device;

	static final String				kBLEStateScanningNotification = "BLEStateSearchingNotification";
	static final String				kBLEStateScanningTimeoutNotification = "BLEStateSearchingTimeoutNotification";
	static final String				kBLEStateConnectingNotification = "BLEStateConnectingNotification";
	static final String				kBLEStateConnectingTimeoutNotification = "BLEStateConnectingTimeOutNotification";
	static final String				kBLEStateDisconnectingNotification = "BLEStateDisconnectingNotification";
	static final String				kBleStateEnablingTimeout = "BleStateChangedTimeout";

	static final String				kBLECommandBegin = "BLECommandBegin";
	static final String				kBLECommandNext = "BLEEndCommandSuccess";
	static final String				kBLECommandEnd = "BLECommandEnd";
	static final String				kBLECommandTimeOut = "BLEEndCommandTimeOut";

	// main handler for peripheral : enable ble, scanning the peripheral, connecting to the peripheral, executing some commands
	protected Handler				handler;
	protected Runnable				enablingBLERunnable;                   // enable ble
	protected Runnable				scanningTimeoutRunnable;               // scan the peripheral

	public static final int			TIMEOUT_FOR_ENABLING = 60 * 1000;     // enabling timeout value
	public static final int			TIMEOUT_FOR_SCANNING = 120 * 1000;     // 1st scanned timeout value

	protected Runnable				connectingRunnable;                    // connectToGoogleFit to the peripheral
	public static final int			TIMEOUT_FOR_CONNECTING = 120 * 1000;     // connection timeout

	protected Runnable				commandTimeoutRunnable;                // execute commands
	public static final int			TIMEOUT_PER_COMMAND = 120 * 1000;     // command timeout

	protected Handler				handlerForRestartScanning;
	protected Runnable				runnableForRestartScanning;
	public static final int			TIMEOUT_FOR_SCANNING_ANYDEVICE = 60 * 1000; //60 seconds, this means the app can find at least one device within 5 seconds

	protected boolean				waitForStarting = false;

	protected Object				returnObject;



	public CBBandCommandHelper(Context context, String deviceCode, final onBandActionListener callback) {
		this.context = context;
		this.deviceCode = deviceCode;
		this.callback = callback;

		this.isSuccess = false;
		this.phase = PHASE_NONE;
		this.handler = new Handler(this.context.getMainLooper());
		this.handlerForRestartScanning = new Handler(this.context.getMainLooper());
	}

	/**
	 * This is public function for excute
	 */
	public void execute() {
		if (this.phase != PHASE_NONE && this.phase != PHASE_STOPPED) {
			return;
		}

		if (!EventManager.sharedInstance().isRegistered(this)) {
			EventManager.sharedInstance().register(this);
		}

		this.waitForStarting = true;

		boolean enabledBLE = BleManager.sharedInstance().isBleEnabled();
		if (enabledBLE) {
			connectingPeripheral();
		} else {
			Logger.log(TAG, "execute : Enabling Bluetooth Watchdog running");
			this.enablingBLERunnable = new Runnable() {
				@Override
				public void run() {
					Logger.error(TAG, "execute : Enabling Bluetooth Watchdog timeout");
					EventManager.sharedInstance().post(kBleStateEnablingTimeout);
					CBBandCommandHelper.this.waitForStarting = false;
				}
			};

			this.handler.postDelayed(this.enablingBLERunnable, TIMEOUT_FOR_ENABLING);

			// wait for syncing
			BleManager.sharedInstance().enableAdapter();
		}
	}

	/**
	 * This is public function for Stopping command
	 */
	public void stop() {
		this.waitForStarting = false;
		this.handler.removeCallbacks(this.enablingBLERunnable);
		//synchronized (_student)
		if (this.phase == PHASE_STOPPED)
			return;

		if (this.phase == PHASE_SCANNING) {
			this.phase = PHASE_STOPPED;
			BleManager.sharedInstance().stopScan();
			postProcess();
		} else if (this.phase == PHASE_CONNECTING) {
			this.phase = PHASE_STOPPED;
			disconnect();
		} else {
			disconnect();
		}
	}

	/**
	 * This is internal function for on Error.
	 *
	 * @param code    : error code
	 * @param message : error message
	 */
	protected void onError(int code, String message) {
		this.handler.removeCallbacks(this.enablingBLERunnable);
		this.handlerForRestartScanning.removeCallbacks(this.runnableForRestartScanning);
		this.handler.removeCallbacks(this.scanningTimeoutRunnable);

		disconnect();
		Logger.log(TAG, "onError : Error code=%d, message=%s", code, message);

		this.isSuccess = false;

		if (this.callback != null) {
			this.callback.failed(code, message);
		}
	}

	// connecting & disconnect, start/stop

	/**
	 * This is internal function for connecting to the Peripheral
	 */
	protected void connectingPeripheral() {
		boolean scanningStarted = BleManager.sharedInstance().scanForPeripheralsWithServices(null, true);

		if (scanningStarted) {
			this.phase = PHASE_SCANNING;
			EventManager.sharedInstance().post(kBLEStateScanningNotification, this);

			this.scanningTimeoutRunnable = new Runnable() {
				@Override
				public void run() {
					Logger.error(TAG, "connectingPeripheral : Scanning Timeout");
					BleManager.sharedInstance().stopScan();
					EventManager.sharedInstance().post(kBLEStateScanningTimeoutNotification);
				}
			};
			this.handler.postDelayed(this.scanningTimeoutRunnable, TIMEOUT_FOR_SCANNING);

			// check ble adapter is block or not
			this.runnableForRestartScanning = new Runnable() {
				@Override
				public void run() {
					CBBandCommandHelper.this.handlerForRestartScanning.removeCallbacks(CBBandCommandHelper.this.runnableForRestartScanning);

					if (CBBandCommandHelper.this.phase == PHASE_SCANNING) {
						if (BleManager.sharedInstance().getScannedPeripherals().size() == 0) {

							// have to restart ble
							BleManager.sharedInstance().restartScanForPeripherals();
							CBBandCommandHelper.this.handler.removeCallbacks(CBBandCommandHelper.this.scanningTimeoutRunnable);

							// extend timeout
							CBBandCommandHelper.this.waitForStarting = true;
							CBBandCommandHelper.this.handler.postDelayed(CBBandCommandHelper.this.scanningTimeoutRunnable, TIMEOUT_FOR_SCANNING);
						}
					} else {
						//Todo error process
						onError(BleManager.BT_ERROR_NO_ANY_DEVICES, CBBandCommandHelper.this.context.getString(R.string.ble_enabling_failed));
					}
				}
			};
			this.handlerForRestartScanning.postDelayed(this.runnableForRestartScanning, TIMEOUT_FOR_SCANNING_ANYDEVICE);
		} else {
			//Todo error process
			onError(BleManager.BT_ERROR_NO_SERVICE, CBBandCommandHelper.this.context.getString(R.string.ble_scanning_not_started));
		}
	}


	/**
	 * This is internal function for disconnect to the peripheral
	 */
	protected void disconnect() {
		this.phase = PHASE_DISCONNECTING;
		postProcess();
		if (this.device != null) {
			Logger.log(TAG, "disconnect : Disconnecting Peripheral");
			this.device.peripheral().disconnect();
			this.device.disconnect();
		}
	}


	protected void postProcess() {
		if (EventManager.sharedInstance().isRegistered(this)) {
			EventManager.sharedInstance().unregister(this);
		}
	}



	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent e) {
		if (EventManager.isEvent(e, BleManager.kBLEManagerStateChanged)) {
			if (BleManager.sharedInstance().isBleEnabled()) {
				Logger.log(TAG, "onEventMainThread : Bluetooth Enabled, remove enablingBLE watchdog");
				this.handler.removeCallbacks(this.enablingBLERunnable);
				if (this.waitForStarting) {
					connectingPeripheral();
					this.waitForStarting = false;
				} else {
					Logger.error(TAG, "onEventMainThread : Bluetooth enabled, but there isn't nothing to do");
					onError(BleManager.BT_ERROR_ENABLE_TIMEOUT, this.context.getString(R.string.ble_enabling_timeout));
				}
			}
		} else if (EventManager.isEvent(e, kBleStateEnablingTimeout)) {
			onError(BleManager.BT_ERROR_ENABLE_TIMEOUT, this.context.getString(R.string.ble_enabling_timeout));
		} else if (EventManager.isEvent(e, kBLEStateScanningTimeoutNotification)) {
			onError(BleManager.BT_ERROR_NOT_FOUND, this.context.getString(R.string.ble_scanning_not_found));
		} else if (EventManager.isEvent(e, kBLEStateConnectingTimeoutNotification)) {
			onError(BleManager.BT_ERROR_CONNECT_TIMEOUT, this.context.getString(R.string.ble_connecting_timeout));
		} else if (EventManager.isEvent(e, BleManager.kBLEManagerDiscoveredPeripheralNotification)) {
			this.handlerForRestartScanning.removeCallbacks(this.runnableForRestartScanning);
			if (this.device == null) {
				BlePeripheral peripheral = (BlePeripheral) e.object;
				if (peripheral.getCode() != null && peripheral.getCode().equals(this.deviceCode)) {
					// stop searching
					this.handler.removeCallbacks(this.scanningTimeoutRunnable);

					this.connectingRunnable = new Runnable() {
						@Override
						public void run() {
							Logger.error(TAG, "onEventMainThread : Connection Timeout");
							EventManager.sharedInstance().post(kBLEStateConnectingTimeoutNotification);
						}
					};

					this.handler.postDelayed(this.connectingRunnable, TIMEOUT_FOR_CONNECTING);
					BleManager.sharedInstance().stopScan();

					// create device
					this.device = new PowerBandDevice(peripheral);
					Logger.log(TAG, "onEventMainThread : Connecting Device : name=%s, address=%s", peripheral.name(), peripheral.address());
					boolean connecting = BleManager.sharedInstance().connectPeripheral(peripheral);

					if (connecting) {
						this.phase = PHASE_CONNECTING;
						EventManager.sharedInstance().post(kBLEStateConnectingNotification, this);
					} else {
						onError(BleManager.BT_ERROR_CONNECT_FAILED, this.context.getString(R.string.ble_connecting_failed));
					}
				}
			}
		} else if (EventManager.isEvent(e, PowerBandDevice.kPowerBandDeviceConnectedNotification)) {
			// Start executing Command
			if (this.device != null && this.device == e.object) {
				EventManager.sharedInstance().post(kBLECommandBegin, this.device);
			}
		} else if (EventManager.isEvent(e, PowerBandDevice.kPowerBandDeviceDisconnectedNotification)) {
			//finish processing

			if (this.device != null && this.device == e.object) {
				if (this.device.deviceState() == PowerBandDevice.DEVICE_FINISHED) {
					this.phase = PHASE_STOPPED;
				} else {
					onError(BleManager.BT_ERROR_COMMAND_FAILED, this.context.getString(R.string.ble_command_failed));
				}
			}
		} else if (EventManager.isEvent(e, kBLECommandBegin)) {
			this.handler.removeCallbacks(this.connectingRunnable);

			this.commandTimeoutRunnable = new Runnable() {
				@Override
				public void run() {
					Logger.error(TAG, "onEventMainThread : Command Executing timeout");
					EventManager.sharedInstance().post(kBLECommandTimeOut);
				}
			};

			this.handler.postDelayed(this.commandTimeoutRunnable, TIMEOUT_PER_COMMAND);
			//Logger.log(TAG, "starting new execution");
			_preCommand();
		} else if (EventManager.isEvent(e, kBLECommandNext)) {
			// stop old timer
			this.handler.removeCallbacks(this.commandTimeoutRunnable);
			// restart new timer for new command
			this.handler.postDelayed(this.commandTimeoutRunnable, TIMEOUT_PER_COMMAND);

			BandCommandResponse response = e.object != null ? (BandCommandResponse) e.object : null;
			//Logger.log(TAG, "execute next command");

			try {
				if (!nextCommand(response)) {
					// there is no next command, in this case, probably success
					EventManager.sharedInstance().post(kBLECommandEnd, response);
				}
			} catch (Exception ex) {
				onError(BleManager.BT_ERROR_UNKNOWN, ex.getMessage());
			}
		} else if (EventManager.isEvent(e, kBLECommandEnd)) {
			this.handler.removeCallbacks(this.commandTimeoutRunnable);
			this.isSuccess = true;
			_postCommand();
		} else if (EventManager.isEvent(e, kBLECommandTimeOut)) {
			onError(BleManager.BT_ERROR_COMMAND_TIMEOUT, this.context.getString(R.string.ble_command_timeout));
		}
	}


	protected synchronized void _preCommand() {
		// Logger.log(TAG, "staring BLE Command");
		this.phase = PHASE_COMMAND;
		EventManager.sharedInstance().post(kBLECommandNext, null);
	}

	protected synchronized void _postCommand() {
		// Logger.log(TAG, "ending BLE Command");

		this.phase = PHASE_DISCONNECTING;
		if (this.callback != null) {
			this.callback.completed(returnObject);
		}

		disconnect();
	}

	//subclass should be override startCommand & finishCommand
	protected synchronized boolean nextCommand(BandCommandResponse object) {
		return false;
	}

	public void onWrite(boolean success, BandCommandResponse response) {
		Logger.log(TAG, "onWrite : Finish command with(%s)", success ? "Success" : "Failed");

		// finish previous command
		if (success) {
			//execute next command
			EventManager.sharedInstance().post(kBLECommandNext, response);
		} else {
			onError(BleManager.BT_ERROR_COMMAND_WRITE_FAILED, "Bluetooth.onWrite error");
		}
	}
}
