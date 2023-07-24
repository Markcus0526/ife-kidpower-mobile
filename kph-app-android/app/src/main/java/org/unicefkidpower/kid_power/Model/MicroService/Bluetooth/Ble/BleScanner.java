package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;

import com.radiusnetworks.bluetooth.BluetoothCrashResolver;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.EventManager;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.SEvent;
import org.unicefkidpower.kid_power.Misc.Logger;

import java.util.ArrayList;
import java.util.List;


public class BleScanner {
	private static final String 			TAG = "BleScanner";

	public static final String 				kBLEScannerDeviceNotification = "blescanner connected peripheral notification";

	private static final int 				SDK_VERSION_BLE_SEPARATION = 100; // use devprecated function because new function is not good

	private static BleScanner				instance = null;
	private Context							contextInstance;
	private BluetoothAdapter 				bluetoothAdapter;
	private BluetoothLeScanner 				bluetoothLeScanner;
	private BluetoothCrashResolver 			bluetoothCrashResolver;

	public BleScannerListener				listner = null;
	private boolean							isStarted = false;
	private boolean							isForceStopped = true;

	protected static final boolean			USE_REAPEAT_SCANNING = true;

	protected static final int				SCANNING_ONCE_PERIOD = 5 * 1000; // milliseconds
	protected static final int				SCANNING_INTERVAL = 2 * 1000; // milliseconds

	private ArrayList<BluetoothDevice>		scannedDevices;

	private Handler				handlerInstance;
	private boolean				startPhase = true;

	private Object				scanCallback = null;

	private Runnable continueRunnable = new Runnable() {
		@Override
		public void run() {
			if (USE_REAPEAT_SCANNING) {
				if (!startPhase) {
					stopScanLocally();
					if (!isForceStopped)
						handlerInstance.postDelayed(this, SCANNING_INTERVAL);
					startPhase = true;
				} else {
					if (!isForceStopped) {
						startScanLocally();
						handlerInstance.postDelayed(this, SCANNING_ONCE_PERIOD);
					}
					startPhase = false;
				}
			}
		}
	};

	public static BleScanner initialize(Context context, BluetoothCrashResolver bluetoothCrashResolver) {
		if (instance == null)
			instance = new BleScanner(context, bluetoothCrashResolver);
		return instance;
	}

	public static BleScanner sharedInstance() {
		return instance;
	}

	private BleScanner(Context context, BluetoothCrashResolver bluetoothCrashResolver) {
		this.contextInstance = context;
		this.bluetoothCrashResolver = bluetoothCrashResolver;
		scannedDevices = new ArrayList<>();
		final BluetoothManager mBluetoothManager = (BluetoothManager) this.contextInstance.getSystemService(Context.BLUETOOTH_SERVICE);
		bluetoothAdapter = mBluetoothManager.getAdapter();
		if (android.os.Build.VERSION.SDK_INT >= SDK_VERSION_BLE_SEPARATION) {
			bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
		}

		handlerInstance = new Handler(context.getMainLooper());
	}

	public boolean start() {
		//Logger.log(TAG, "BleScanner start()");
		if (!EventManager.sharedInstance().isRegistered(this)) {
			EventManager.sharedInstance().register(this);
		}

		scannedDevices.clear();

		isForceStopped = false;
		startPhase = true;

		if (USE_REAPEAT_SCANNING) {
			handlerInstance.post(continueRunnable);
			return true;
		} else {
			return startScanLocally();
		}
	}

	public void stop() {
		if (EventManager.sharedInstance().isRegistered(this)) {
			EventManager.sharedInstance().unregister(this);
		}

		stopScanLocally();
		isForceStopped = true;
	}

	public boolean isStopped() {
		return isForceStopped;
	}

	protected boolean startScanLocally() {
		synchronized (Thread.currentThread()) {
			if (isStarted && scanCallback != null) {
				if(android.os.Build.VERSION.SDK_INT < SDK_VERSION_BLE_SEPARATION) {
					bluetoothAdapter.stopLeScan((BluetoothAdapter.LeScanCallback) scanCallback);
				} else {
					bluetoothLeScanner.stopScan((ScanCallback) scanCallback);
				}

				Logger.log(TAG, "_startScanLocally : stop and restart");
			}

			scannedDevices.clear();

			if (bluetoothAdapter == null) {
				Logger.error(TAG, "_startScanLocally : bluetoothAdapter=null, not started");
				return false;
			}

			if (!bluetoothAdapter.isEnabled() || bluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
				Logger.error(TAG, "_startScanLocally : bluetoothAdapter.isEnabled()=false, not started");
				return false;
			}

			try {
				if(android.os.Build.VERSION.SDK_INT < SDK_VERSION_BLE_SEPARATION) {
					if (scanCallback == null) {
						scanCallback = new BluetoothAdapter.LeScanCallback() {
							@Override
							public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
								if (isForceStopped) {
									return;
								}

								EventManager.sharedInstance().post(SEvent.EVENT_BAND_SEARCHED_LOWER_VERSION, new LeSearchResult(device, rssi, scanRecord));
								//EventBus.getDefault().post(new SEvent(SEvent.EVENT_BAND_SEARCHED_LOWER_VERSION, new LeSearchResult(device, rssi, scanRecord)));
							}};
					}

					if (bluetoothAdapter.startLeScan((BluetoothAdapter.LeScanCallback) scanCallback)) {
						Logger.log(TAG, "_startScanLocally : started successfully");
						isStarted = true;
						return true;
					} else {
						Logger.error(TAG, "_startScanLocally : failed");
						return false;
					}
				} else {
					if (scanCallback == null) {
						scanCallback = new ScanCallback() {
							@Override
							public void onScanResult(int callbackType, ScanResult result) {
								super.onScanResult(callbackType, result);

								if (isForceStopped) {
									return;
								}

								EventManager.sharedInstance().post(SEvent.EVENT_BAND_SEARCHED_HIGHER_VERSION, result);
								//EventBus.getDefault().post(new SEvent(SEvent.EVENT_BAND_SEARCHED_HIGHER_VERSION, result));
							}

							@Override
							public void onBatchScanResults(List<ScanResult> results) {
								super.onBatchScanResults(results);
							}

							@Override
							public void onScanFailed(int errorCode) {
								super.onScanFailed(errorCode);
								Logger.error(TAG, "_startScanLocally : scanCallback failed, error code=%d", errorCode);
							}
						};
					}

					bluetoothLeScanner.startScan((ScanCallback) scanCallback);

					return true;
				}
			} catch (Exception e) {
				Logger.error(TAG, "_startScanLocally : failed with crash");
				return false;
			}
		}
	}

	protected void stopScanLocally() {
		synchronized (Thread.currentThread()) {
			if (isStarted == false) {
				//Logger.log(TAG, "BleScanner _stopScanLocally() : not started scan locally");
				return;
			}

			isStarted = false;
			if (bluetoothAdapter == null) {
				Logger.error(TAG, "stopScanLocally : bluetoothAdapter=null, not stopped");
				return;
			}
			try {
				if (!bluetoothAdapter.isEnabled()) {
					bluetoothAdapter.enable();
					return;
				}
			} catch (Exception e) {
				Logger.error(TAG, "stopScanLocally : not enabled");
				return;
			}

			try {
				if (scanCallback != null) {
					if(android.os.Build.VERSION.SDK_INT < SDK_VERSION_BLE_SEPARATION) {
						bluetoothAdapter.stopLeScan((BluetoothAdapter.LeScanCallback) scanCallback);
					} else {
						bluetoothLeScanner.stopScan((ScanCallback) scanCallback);
					}
				}
			} catch (Exception e) {
				Logger.error(TAG, "stopScanLocally : crashed");
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
		if (EventManager.isEvent(e, SEvent.EVENT_BAND_SEARCHED_LOWER_VERSION)) {
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

				if (listner != null &&
						listner.shouldCheckDevice(device, result.rssi, scanRecord)) {
					listner.deviceScanned(device, result.rssi, scanRecord);
				}
			}
		} else if (EventManager.isEvent(e, SEvent.EVENT_BAND_SEARCHED_HIGHER_VERSION)) {
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

				if (listner != null &&
						listner.shouldCheckDevice(device, result.getRssi(), result.getScanRecord().getBytes())) {
					listner.deviceScanned(device, result.getRssi(), result.getScanRecord().getBytes());
				}
			}
		}
	}
}
