package org.unicefkidpower.schools.sync;

import android.content.Context;
import android.os.Handler;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.schools.ble.BleManager;
import org.unicefkidpower.schools.ble.BlePeripheral;
import org.unicefkidpower.schools.event.EventManager;
import org.unicefkidpower.schools.event.SEvent;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.server.apimanage.CommandService.FilteringDevice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.unicefkidpower.schools.ble.BleError;

/**
 * Created by Dayong on 8/6/2016.
 */
public class BandFinder {
	static final String TAG = "BandFinder";

	static final int TIMEOUT_FOR_ENABLING = 60 * 1000;                    // should connect within 60 seconds
	static final int TIMEOUT_FOR_CHECKING_BT = 120 * 1000;                // should connect within 60 seconds

	public static final int TIMEOUT_FOR_SEARCHING = 30 * 1000;                    // default searching time, exclude enabling, checking


	static final int STATE_NONE = 0;
	static final int STATE_ENABLING = 1;
	static final int STATE_CHECKING = 2;
	static final int STATE_SCANNING = 3;

	static final String L_EVENT_SEARCHING_DONE = "L_EVENT_SEARCHING_DONE";
	static final String L_EVENT_BT_ENABLING_TIMEOUT = "L_EVENT_BT_ENABLING_TIMEOUT";
	static final String L_EVENT_BT_CHECKING_TIMEOUT = "L_EVENT_BT_CHECKING_TIMEOUT";

	static BandFinder _shared_finder;
	static BleManager _shared_manager;


	public static BandFinder sharedFinder() {
		if (_shared_finder == null)
			_shared_finder = new BandFinder();

		return _shared_finder;
	}

	public interface OnDiscoveredBandListener {
		void onDiscovered(BlePeripheral newPeripheral,
						  List<BlePeripheral> scannedItems);

		void onError(int error,
					 String message);

		void onEnd(List<BlePeripheral> scannedItems,
				   boolean byUserRequest);
	}

	private boolean _registered;
	private int state;
	private Context _context;
	private OnDiscoveredBandListener _listener;

	private List<FilteringDevice> filterDevices;

	private int nRetryForRestart;
	private Handler _handler;

	private int timeoutForScanning;

	private Runnable _runnableForEnabling = new Runnable() {
		@Override
		public void run() {
			Logger.log(TAG, "timeout for enabling ble");
			EventManager.sharedInstance().post(L_EVENT_BT_ENABLING_TIMEOUT);
		}
	};

	private Runnable _runnableForCheckingBT = new Runnable() {
		@Override
		public void run() {
			Logger.error(TAG, "time for checking BT");
			_handler.removeCallbacks(_runnableForCheckingBT);

			if (BleManager.sharedInstance().getScannedPeripherals().size() > 0) {
				return;
			}

			// blocked
			if (nRetryForRestart > 0) {
				Logger.error(TAG, "couldn't find any device, will restart ble");

				// had to restart scanning
				nRetryForRestart--;
				BleManager.sharedInstance().restartScanForPeripherals();

				// add trigger again.
				_handler.postDelayed(_runnableForCheckingBT, TIMEOUT_FOR_CHECKING_BT);
			} else {
				EventManager.sharedInstance().post(L_EVENT_BT_CHECKING_TIMEOUT);
			}
		}
	};

	Runnable _runnableForSearching = new Runnable() {
		@Override
		public void run() {
			Logger.log(TAG, "Searching done");
			_handler.removeCallbacks(_runnableForSearching);
			EventManager.sharedInstance().post(L_EVENT_SEARCHING_DONE);
		}
	};

	private BandFinder() {
		_registered = false;
		state = STATE_NONE;
		_shared_manager = BleManager.sharedInstance();
	}

	public void search(Context context, List<FilteringDevice> filterBands, int timeout, OnDiscoveredBandListener listener) {
		_context = context;
		_listener = listener;
		filterDevices = filterBands;

		if (!_registered) {
			EventManager.sharedInstance().register(this);
			_registered = true;
		}

		if (state != STATE_NONE) {
			Logger.error(TAG, "Already searching now");
			return;
		}

		_handler = new Handler(_context.getMainLooper());
		nRetryForRestart = 1;

		boolean enabledBle = _shared_manager.isBleEnabled();
		if (enabledBle) {
			_start_scan();
		} else {
			Logger.log(TAG, "Enabling Bluetooth Watchdog running");

			state = STATE_ENABLING;
			_handler.postDelayed(_runnableForEnabling, TIMEOUT_FOR_ENABLING);
			_shared_manager.enableAdapter();
		}

		timeoutForScanning = timeout;
		if (timeoutForScanning != 0) {
			_handler.postDelayed(_runnableForSearching, timeoutForScanning);
		}
	}

	public void stop() {
		try {
			Logger.log(TAG, "stopping scanning");

			if (_registered) {
				EventManager.sharedInstance().unregister(this);
				_registered = false;
			}

			if (state != STATE_NONE && state != STATE_ENABLING) {
				Logger.log(TAG, "stop scanning");
				_shared_manager.stopScan(1);
			}

			//Logger.log(TAG, "Remove all threads");
			if (_handler != null) {
				_handler.removeCallbacks(_runnableForEnabling);
				_handler.removeCallbacks(_runnableForCheckingBT);
				_handler.removeCallbacks(_runnableForSearching);
			}

			state = STATE_NONE;
			if (_listener != null) {
				// Logger.log(TAG, "BandFinder stopped by Request");
				List<BlePeripheral> scannedPeripheral = Collections.synchronizedList(_shared_manager.getScannedPeripherals());
				_listener.onEnd(doFilter(scannedPeripheral), true);
			}
		} catch (Exception e) {
			Logger.error(TAG, "error" + e.toString());
			e.printStackTrace();
		}
	}


	List<BlePeripheral> doFilter(List<BlePeripheral> scannedPeripherals) {
		if (scannedPeripherals == null)
			return null;

		if (filterDevices == null)
			return scannedPeripherals;
		else {
			synchronized (scannedPeripherals) {
				ArrayList<BlePeripheral> filteredPeripherals = new ArrayList<>();

				for (BlePeripheral peripheral : scannedPeripherals) {
					if (_findPeripheral(peripheral)) {
						continue;
					}

					filteredPeripherals.add(peripheral);
				}
				return filteredPeripherals;
			}
		}
	}

	boolean _findPeripheral(BlePeripheral peripheral) {
		if (filterDevices == null || peripheral == null)
			return false;

		for (FilteringDevice device : filterDevices) {
			if (peripheral.getMACAddress().equals(device.deviceId))
				return true;
		}

		return false;
	}

	void _start_scan() {
		boolean scanStarted = _shared_manager.scanForPeripheralsWithServices(null, true);
		if (scanStarted) {
			state = STATE_CHECKING;
			Logger.log(TAG, "started scan peripheral");

			// check ble adapter is block or not
			_handler.postDelayed(_runnableForCheckingBT, TIMEOUT_FOR_CHECKING_BT);
		} else {
			onError(BleError.BE_DISABLED, "couldn't start scan for peripheral");
		}
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent e) {
		if (EventManager.isEvent(e, BleManager.kBLEManagerStateChanged)) {
			// Logger.log(TAG, "step1, ble state was changed");
			if (_shared_manager.isBleEnabled()) {
				// Logger.log(TAG, "step1, ble was enabled");
				_handler.removeCallbacks(_runnableForEnabling);
				_start_scan();
			}
		} else if (EventManager.isEvent(e, BleManager.kBLEManagerDiscoveredPeripheralNotification)) {
			// Logger.log(TAG, "discovered peripheral");
			BlePeripheral peripheral = (BlePeripheral) e.object;

			Logger.log(TAG, "discovered device for %s(%s)", peripheral.getMACAddress(), peripheral.getCode());
			// Stop searching
			_handler.removeCallbacks(_runnableForCheckingBT);

			state = STATE_SCANNING;

			List<BlePeripheral> scannedPeripheral = Collections.synchronizedList(_shared_manager.getScannedPeripherals());
			List<BlePeripheral> filtered = doFilter(scannedPeripheral);
			if (filtered != null &&
					!filtered.isEmpty() &&
					_listener != null) {
				_listener.onDiscovered(peripheral, filtered);
			}
		} else if (EventManager.isEvent(e, L_EVENT_SEARCHING_DONE)) {
			stop();
			if (_listener != null) {
				Logger.log(TAG, "BandFinder stopped by TIMEOUT");
				List<BlePeripheral> scannedPeripheral = Collections.synchronizedList(_shared_manager.getScannedPeripherals());
				_listener.onEnd(doFilter(scannedPeripheral), false);
			}
		} else if (EventManager.isEvent(e, L_EVENT_BT_ENABLING_TIMEOUT)) {
			onError(BleError.BE_OFF, "Bluetooth couldn't be enabled now.");
		} else if (EventManager.isEvent(e, L_EVENT_BT_CHECKING_TIMEOUT)) {
			onError(BleError.BE_DISABLED, "Bluetooth can't find any peripherals now");
		}
	}


	void onError(int error_code, String message) {
		Logger.error(TAG, message);

		state = STATE_NONE;
		if (_listener != null) {
			_listener.onError(error_code, message);
		}
	}

}
