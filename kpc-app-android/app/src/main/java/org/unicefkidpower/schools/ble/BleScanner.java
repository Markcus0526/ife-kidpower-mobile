package org.unicefkidpower.schools.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;

import com.radiusnetworks.bluetooth.BluetoothCrashResolver;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.schools.event.SEvent;
import org.unicefkidpower.schools.helper.Logger;

import java.util.ArrayList;
import java.util.List;


public class BleScanner {
	public static final String			TAG								= "BleScanner";

	private static final int			SDK_VERSION_BLE_SEPARATION		= 100;

	protected static final boolean		USE_REAPEAT_SCANNING			= true;
	protected static final int			SCANNING_ONCE_PERIOD			= 5 * 1000; // milliseconds
	protected static final int			SCANNING_INTERVAL				= 2 * 1000; // milliseconds

	private static BleScanner			sharedInstance					= null;
	public BleScannerListener			listener						= null;

	private Context						contextInstance;

	private BluetoothAdapter			bluetoothAdapter;
	private BluetoothLeScanner			bluetoothLeScanner;
	private BluetoothCrashResolver		bluetoothCrashResolver;
	private boolean						isStarted						= false;
	private boolean						isForceStopped					= true;
	private ArrayList<BluetoothDevice>	scannedDevices;

	private Handler						searchRepeatHandler;
	private boolean						startPhase						= true;

	private Object						scanCallback					= null;

	private Runnable mSearchOnceRunnable = new Runnable() {
		@Override
		public void run() {
			if (USE_REAPEAT_SCANNING) {
				if (!startPhase) {
					_stopScanLocally();
					if (!isForceStopped)
						searchRepeatHandler.postDelayed(this, SCANNING_INTERVAL);
					startPhase = true;
				} else {
					if (!isForceStopped) {
						_startScanLocally();
						searchRepeatHandler.postDelayed(this, SCANNING_ONCE_PERIOD);
					}
					startPhase = false;
				}
			}
		}
	};

	private BleScanner(Context context, BluetoothCrashResolver bluetoothCrashResolver) {
		contextInstance = context;
		this.bluetoothCrashResolver = bluetoothCrashResolver;
		scannedDevices = new ArrayList<>();
		final BluetoothManager mBluetoothManager = (BluetoothManager) contextInstance.getSystemService(Context.BLUETOOTH_SERVICE);
		bluetoothAdapter = mBluetoothManager.getAdapter();
		if (android.os.Build.VERSION.SDK_INT >= SDK_VERSION_BLE_SEPARATION) {
			bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
		}

		searchRepeatHandler = new Handler(context.getMainLooper());

		EventBus.getDefault().register(this);
	}

	public static BleScanner initialize(Context context, BluetoothCrashResolver bluetoothCrashResolver) {
		if (sharedInstance == null)
			sharedInstance = new BleScanner(context, bluetoothCrashResolver);
		return sharedInstance;
	}


	public static BleScanner sharedInstance() {
		return sharedInstance;
	}


	/**
	 * Start searching Kid Power Bands
	 *
	 * @return
	 */
	public boolean start() {
		scannedDevices.clear();

		isForceStopped = false;
		startPhase = true;

		if (USE_REAPEAT_SCANNING) {
			searchRepeatHandler.post(mSearchOnceRunnable);
			return true;
		} else {
			return _startScanLocally();
		}
	}


	/**
	 * Stop current searching progress
	 */
	public void stop() {
		// Logger.logWithFlurry(TAG, "BleScanner stop()");
		isForceStopped = true;
	}


	public boolean isStopped() {
		return isForceStopped;
	}

	protected boolean _startScanLocally() {
		synchronized (Thread.currentThread()) {
			if (isStarted && scanCallback != null) {
				if (android.os.Build.VERSION.SDK_INT < SDK_VERSION_BLE_SEPARATION) {
					bluetoothAdapter.stopLeScan((BluetoothAdapter.LeScanCallback) scanCallback);
				} else {
					bluetoothLeScanner.stopScan((ScanCallback) scanCallback);
				}

				Logger.log(TAG, "BleScanner already started, stop and restart");
			}

			scannedDevices.clear();

			if (bluetoothAdapter == null) {
				Logger.log(TAG, "BleScanner _startScanLocally() - bluetoothAdapter == null, not started");
				return false;
			}

			if (!bluetoothAdapter.isEnabled()) {
				Logger.log(TAG, "BleScanner _startScanLocally() - bluetoothAdapter.isEnabled() == false, not started");
				return false;
			}

			try {
				if (android.os.Build.VERSION.SDK_INT < SDK_VERSION_BLE_SEPARATION) {
					if (scanCallback == null) {
						scanCallback = new BluetoothAdapter.LeScanCallback() {
							@Override
							public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
								if (isForceStopped)
									return;

								EventBus.getDefault().post(new SEvent(SEvent.EVENT_BAND_SEARCHED_LOWER_VERSION, new LeSearchResult(device, rssi, scanRecord)));
							}
						};
					}

					if (bluetoothAdapter.startLeScan((BluetoothAdapter.LeScanCallback) scanCallback)) {
						Logger.log(TAG, "BleScanner _startScanLocally() - started successfully");
						isStarted = true;
						return true;
					} else {
						Logger.log(TAG, "BleScanner _startScanLocally() - failed");
						return false;
					}
				} else {
					if (scanCallback == null) {
						scanCallback = new ScanCallback() {
							@Override
							public void onScanResult(int callbackType, ScanResult result) {
								super.onScanResult(callbackType, result);
								if (isForceStopped)
									return;

								EventBus.getDefault().post(new SEvent(SEvent.EVENT_BAND_SEARCHED_HIGHER_VERSION, result));
							}

							@Override
							public void onBatchScanResults(List<ScanResult> results) {
								super.onBatchScanResults(results);
								Logger.log(TAG, "BleScanner scanCallback - onBatchScanResults");
							}

							@Override
							public void onScanFailed(int errorCode) {
								super.onScanFailed(errorCode);
								Logger.log(TAG, "BleScanner scanCallback - onScanFailed");
							}
						};
					}


					ScanSettings settings = new ScanSettings.Builder()
							.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
							.build();
					bluetoothLeScanner.startScan(null, settings, (ScanCallback) scanCallback);

					return true;
				}
			} catch (Exception e) {
				Logger.log(TAG, "BleScanner _startScanLocally() - failed with crash");
				return false;
			}
		}
	}


	protected void _stopScanLocally() {
		Logger.log(TAG, "BleScanner _stopScanLocally()");

		synchronized (Thread.currentThread()) {
			isStarted = false;

			if (bluetoothAdapter == null) {
				Logger.log(TAG, "BleScanner stopScanLocally() - bluetoothAdapter == null, not stopped");
				return;
			}

			try {
				if (!bluetoothAdapter.isEnabled()) {
					Logger.log(TAG, "BleScanner stopScanLocally() - bluetoothAdapter == null, not stopped");
					bluetoothAdapter.enable();
					return;
				}
			} catch (Exception e) {
				Logger.error(TAG, "BleScanner stopLeScan : not enabled");
				return;
			}

			try {
				if (scanCallback != null) {
					if (android.os.Build.VERSION.SDK_INT < SDK_VERSION_BLE_SEPARATION) {
						bluetoothAdapter.stopLeScan((BluetoothAdapter.LeScanCallback) scanCallback);
					} else {
						bluetoothLeScanner.stopScan((ScanCallback) scanCallback);
					}
				}
			} catch (Exception e) {
				Logger.log(TAG, "BleScanner stopLeScan : crashed");
				bluetoothAdapter.enable();
			}
		}
	}


	public class LeSearchResult {
		Object device = null;
		Object scanRecord = null;
		int rssi = 9;

		LeSearchResult(Object device, int rssi, Object scanRecord) {
			this.device = device;
			this.rssi = rssi;
			this.scanRecord = scanRecord;
		}
	}


	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onEvent(SEvent e) {
		if (e.name.equals(SEvent.EVENT_BAND_SEARCHED_LOWER_VERSION)) {
			LeSearchResult result = (LeSearchResult) e.object;

			BluetoothDevice device = (BluetoothDevice) result.device;
			byte[] scanRecord = (byte[]) result.scanRecord;

			bluetoothCrashResolver.notifyScannedDevice(device, (BluetoothAdapter.LeScanCallback) scanCallback);

			boolean isExist = false;
			for (BluetoothDevice oldDeviceItem : scannedDevices) {
				if (oldDeviceItem.getAddress().equalsIgnoreCase(device.getAddress())) {
					isExist = true;
					break;
				}
			}

			if (!isExist) {
				scannedDevices.add(device);

				if (listener != null &&
						listener.shouldCheckDevice(device, result.rssi, scanRecord)) {
					listener.deviceScanned(device, result.rssi, scanRecord);
				}
			}
		} else if (e.name.equals(SEvent.EVENT_BAND_SEARCHED_HIGHER_VERSION)) {
			ScanResult result = (ScanResult) e.object;

			BluetoothDevice device = result.getDevice();

			bluetoothCrashResolver.notifyScannedDevice(device, (ScanCallback) scanCallback);

			boolean isExist = false;
			for (BluetoothDevice oldDeviceItem : scannedDevices) {
				if (oldDeviceItem.getAddress().equalsIgnoreCase(device.getAddress())) {
					isExist = true;
					break;
				}
			}

			if (!isExist) {
				scannedDevices.add(device);

				if (listener != null &&
						listener.shouldCheckDevice(device, result.getRssi(), result.getScanRecord().getBytes())) {
					listener.deviceScanned(device, result.getRssi(), result.getScanRecord().getBytes());
				}
			}
		}
	}

}
