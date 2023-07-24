package org.unicefkidpower.schools.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.radiusnetworks.bluetooth.BluetoothCrashResolver;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.schools.define.config;
import org.unicefkidpower.schools.event.SEvent;
import org.unicefkidpower.schools.helper.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;


/**
 * Created by donal_000 on 12/1/2014.
 */
public class BleManager implements BleScannerListener, BlePeripheralDelegate, BluetoothCrashResolver.UpdateNotifier {
	public static final String TAG					= "BleManager";
	public static final String POWERBAND			= "Power Band";
	public static final String CALORIECLOUD			= "Calorie Cloud";

	/*!
	 *  \brief Posted when BLEManager receives advertisement from a peripheral.
	 *
	 *  userInfo keys:
	 *  - kBLEManagerPeripheralKey
	 *  - kBLEManagerManagerKey
	 *  - kBLEManagerAdvertisementDataKey
	 */
	public static final String kBLEManagerDiscoveredPeripheralNotification = "blemanager discovered peripheral notification";
	/*!
	 *  \brief  Posted when BLEManager removes old advertising peripherals.
	 *
	 *  userInfo keys:
	 *  - kBLEManagerPeripheralKey
	 *  - kBLEManagerManagerKey
	 *
	 *  This notification posted as a result of calling BLEManager::purgeAdvertisingDevices:
	 */
	public static final String kBLEManagerUndiscoveredPeripheralNotification = "blemanager undiscovered peripheral notification";
	/*!
	 *  \brief  Posted when a BLEPeripheral connects
	 *
	 *  userInfo keys:
	 *  - kBLEManagerPeripheralKey
	 *  - kBLEManagerManagerKey
	 */
	public static final String kBLEManagerConnectedPeripheralNotification = "blemanager connected peripheral notification";
	/*!
	 *  \brief Posted when a BLEPeripheral disconnects
	 *
	 *  userInfo keys:
	 *  - kBLEManagerPeripheralKey
	 *  - kBLEManagerManagerKey
	 */
	public static final String kBLEManagerDisconnectedPeripheralNotification = "blemanager disconnected peripheral notification";
	/*!
	 *  \brief Posted when BLEPeripheral fails to connect
	 *
	 *  userInfo keys:
	 *  - kBLEManagerPeripheralKey
	 *  - kBLEManagerManagerKey
	 */
	public static final String kBLEManagerPeripheralConnectionFailedNotification		= "blemanager peripheral connection failed notification";
	public static final String kBLEManagerPeripheralServiceDiscovered					= "blemanager service discovered";

	public static final String kBLEManagerPeripheralDataAvailable						= "blemanager data available";
	public static final String kBLEManagerPeripheralRssiUpdated							= "blemanager rssi updated";
	public static final String kBLEManagerPeripheralDescriptorWrite						= "peripheral descriptor write";
	/*!
	 *  \brief  Notification posted when Bluetooth state changes
	 */
	public static final String kBLEManagerStateChanged									= "blemanager state changed";


	protected static BleManager				sharedInstance = null;
	protected BluetoothAdapter				bluetoothAdapter;
	protected Context						contextInstance;
	protected boolean						adapterDisabledManually;
	protected ArrayList<BlePeripheral>		scannedPeripherals;
	protected ArrayList<UUID>				services;
	protected boolean						scanStarted;
	protected boolean						mustStartScan;
	protected Handler						stopHandler;
	protected Runnable						stopRunnable;
	protected Handler						enableHandler;


	public static class CharacteristicData {
		public BlePeripheral					peripheral;
		public BluetoothGattCharacteristic		characteristic;
		public byte[]							value;
	}

	public static class DescriptorData {
		public BlePeripheral					peripheral;
		public BluetoothGattDescriptor			descriptor;
		public boolean							success;
	}


	public static BleManager initialize(Context context, BluetoothCrashResolver bluetoothCrashResolver) {
		if (sharedInstance == null)
			sharedInstance = new BleManager(context, bluetoothCrashResolver);

		return sharedInstance;
	}

	public static BleManager sharedInstance() {
		return sharedInstance;
	}


	private BleManager(Context context, BluetoothCrashResolver bluetoothCrashResolver) {
		EventBus.getDefault().register(this);

		contextInstance = context;

		checkBluetoothAdapter();
		bluetoothCrashResolver.setUpdateNotifier(this);
		BleScanner.initialize(context, bluetoothCrashResolver);

		adapterDisabledManually = false;

		scannedPeripherals = new ArrayList<BlePeripheral>();
		scanStarted = false;
		mustStartScan = false;

		stopHandler = new Handler(context.getMainLooper());
		stopRunnable = new Runnable() {
			@Override
			public void run() {
				if (scanStarted) {
					stopHandler.removeCallbacks(stopRunnable);
					stopScan(-1);
				}
			}
		};

		enableHandler = new Handler(context.getMainLooper());
	}


	private void checkBluetoothAdapter() {
		final BluetoothManager bluetoothManager = (BluetoothManager) contextInstance.getSystemService(Context.BLUETOOTH_SERVICE);

		bluetoothAdapter = bluetoothManager.getAdapter();
		if (bluetoothAdapter == null) {
			Logger.error(TAG, "checkBluetoothAdapter - bluetooth adapter is null, bluetooth is not available");
		} else {
			Logger.log(TAG, "checkBluetoothAdapter - bluetooth adapter - bluetooth is available");
		}
	}


	public BluetoothAdapter bluetoothAdapter() {
		if (bluetoothAdapter == null) {
			Logger.error(TAG, "bluetoothAdapter - is Null, will init again");
			checkBluetoothAdapter();

			disableAdapter();
		}

		return bluetoothAdapter;
	}


	public boolean isBleAvailable() {
		if (!isBleSupported())
			return false;

		if (bluetoothAdapter == null)
			return false;

		return true;
	}


	public boolean isBleEnabled() {
		if (bluetoothAdapter == null) {
			checkBluetoothAdapter();
			if (bluetoothAdapter == null)
				return false;
		}

		return bluetoothAdapter.isEnabled();
	}


	public void disableAdapter() {
		if (bluetoothAdapter != null) {
			bluetoothAdapter.disable();
			adapterDisabledManually = true;
		}
	}


	public void enableAdapter() {
		if (bluetoothAdapter != null) {
			bluetoothAdapter.enable();
		} else {
			checkBluetoothAdapter();
			if (bluetoothAdapter != null)
				bluetoothAdapter.enable();
		}
	}


	public boolean isBleSupported() {
		return contextInstance.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
	}


	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessageEvent(SEvent e) {
		if (e.name.equals(SEvent.EVENT_BLUETOOTH_STATE_CHANGED)) {
			Integer obj = (Integer) e.object;
			int state = obj.intValue();

			// Logger.log(TAG, String.format("BleManager : bluetooth state changed : %d", state));

			if (state == BluetoothAdapter.STATE_OFF) {
				// stop scanning
				BleScanner.sharedInstance().stop();

				if (mustStartScan) {
					enableAdapter();
				} else {
					if (adapterDisabledManually) {
						enableHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								enableAdapter();
							}
						}, 1 * 1000);
					}
				}
			} else if (state == BluetoothAdapter.STATE_ON) {
				// restart scanning when bluetooth is on
				if (mustStartScan) {
					_startScanLocally();
				}
			}

			EventBus.getDefault().post(new SEvent(kBLEManagerStateChanged, obj));
		}
	}


	public boolean scanForPeripheralsWithServices(ArrayList<UUID> services, boolean allowDuplicates) {
		if (scanStarted) {
			Logger.log(TAG, "already started scanning - mScanStarted = true, return");
			return true;
		}

		// reset stop runnable
		stopHandler.removeCallbacks(stopRunnable);

		// check
		if (!isBleSupported())
			return false;

		mustStartScan = true;

		if (!isBleEnabled()) {
			enableAdapter();
			return true;
		}

		this.services = services;

		return _startScanLocally();
	}

	public boolean restartScanForPeripherals() {
		Logger.log(TAG, "BleManager.restartScanForPeripherals()");

		if (!isBleSupported())
			return false;

		mustStartScan = true;
		if (isBleEnabled()) {
			disableAdapter();
			return true;
		} else {
			enableAdapter();
			return true;
		}
	}

	public boolean didScanStarted() {
		return scanStarted;
	}

	private boolean _startScanLocally() {
		scannedPeripherals.clear();

		BleScanner.sharedInstance().listener = this;
		boolean isStarted = BleScanner.sharedInstance().start();

		scanStarted = true;

		stopHandler.postDelayed(stopRunnable, 10 * 60 * 1000);
		return isStarted;
	}

	public void stopScan(int nKind) {
		Logger.log(TAG, "BleManager.stopScan(%d)", nKind);

		// reset stop runnable
		stopHandler.removeCallbacks(stopRunnable);
		mustStartScan = false;

		BleScanner.sharedInstance().stop();
		scanStarted = false;
	}

	public ArrayList<BlePeripheral> getScannedPeripherals() {
		return scannedPeripherals;
	}

	@Override
	public void deviceScanned(BluetoothDevice device, int rssi, byte[] scanRecord) {
		if (!scanStarted)
			return;

		Long currentTime = System.currentTimeMillis();
		List list = Collections.synchronizedList(scannedPeripherals);
		BlePeripheral existPeripheral = null;

		synchronized (list) {
			Iterator i = list.iterator(); // Must be in synchronized block
			while (i.hasNext()) {
				BlePeripheral peripheral = (BlePeripheral) i.next();
				if (peripheral.address().equalsIgnoreCase(device.getAddress())) {
					existPeripheral = peripheral;
					break;
				}
			}

			if (existPeripheral != null) {
				existPeripheral.scannedTime = currentTime;
				existPeripheral.setRssi(rssi);

				EventBus.getDefault().post(new SEvent(kBLEManagerDiscoveredPeripheralNotification, existPeripheral));
			} else {
				existPeripheral = new BlePeripheral(contextInstance, device.getAddress(), rssi, scanRecord);
				existPeripheral.delegate = this;
				existPeripheral.scannedTime = currentTime;

				list.add(existPeripheral);

				EventBus.getDefault().post(new SEvent(kBLEManagerDiscoveredPeripheralNotification, existPeripheral));
			}
		}
	}


	@Override
	public boolean shouldCheckDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
		if (this.services == null) {
			String deviceName = device.getName();

			if (deviceName == null) {
				return false;
			}

			if (deviceName.contains(POWERBAND)) {
				if (deviceName.length() < POWERBAND.length() + 6) {
					// maybe old band(sacramento), ignore the band.
					Logger.log(TAG, "Power Band was discovered, but wrong name type. name = \"%s\", address = \"%s\"", device.getName(), device.getAddress());
					return false;
				}

				Logger.log(TAG, "name = \"%s\", address = \"%s\"", device.getName(), device.getAddress());

				return true;
			} else if (config.IS_GWS_EDITION) {
				if (deviceName.contains(CALORIECLOUD)) {
					if (deviceName.length() < CALORIECLOUD.length() + 6) {
						Logger.log(TAG, "Calorie Cloud band was discovered, but wrong name type. name = \"%s\", address = \"%s\"", device.getName(), device.getAddress());
						return false;
					}
					Logger.log(TAG, "CC band : name = \"%s\", address = \"%s\"", device.getName(), device.getAddress());
					return true;
				}
			}

			return false;
		} else {
			ArrayList<UUID> uuids = parseUuids(scanRecord);
			boolean isPeripheral = false;

			for (UUID uuid : uuids) {
				if (services.contains(uuid)) {
					isPeripheral = true;
					break;
				}
			}

			if (isPeripheral) {
				Logger.log(TAG, "checking device : discovered PowerBand by Service (name=\"%s\", address=\"%s\"",
						device.getName(), device.getAddress());
			} else {
				Logger.log(TAG, "checking device : An unsupported device found by Service (name=\"%s\", address=\"%s\"",
						device.getName(), device.getAddress());
			}

			return isPeripheral;
		}
	}


	private ArrayList<UUID> parseUuids(byte[] advertisedData) {
		ArrayList<UUID> uuids = new ArrayList<UUID>();

		ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);
		while (buffer.remaining() > 2) {
			byte length = buffer.get();
			if (length == 0) break;

			byte type = buffer.get();
			switch (type) {
				case 0x02: // Partial list of 16-bit UUIDs
				case 0x03: // Complete list of 16-bit UUIDs
					while (length >= 2) {
						uuids.add(UUID.fromString(String.format(
								"%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
						length -= 2;
					}
					break;

				case 0x06: // Partial list of 128-bit UUIDs
				case 0x07: // Complete list of 128-bit UUIDs
					while (length >= 16) {
						long lsb = buffer.getLong();
						long msb = buffer.getLong();
						uuids.add(new UUID(msb, lsb));
						length -= 16;
					}
					break;

				default:
					buffer.position(buffer.position() + length - 1);
					break;
			}
		}

		return uuids;
	}

	public boolean connectPeripheral(BlePeripheral peripheral) {
		if (peripheral == null)
			return false;

		Logger.log(TAG, "connectPeripheral : address = \"%s\", name = \"%s\" - calling peripheral.connect()", peripheral.address(), peripheral.name());
		boolean ret = peripheral.connect();
		if (!ret) {
			Logger.error(TAG, "connectPeripheral : address = \"%s\", name = \"%s\" - calling peripheral.connect() - returned false", peripheral.address(), peripheral.name());
		}
		return ret;
	}

	public void disconnectPeripheral(BlePeripheral peripheral) {
		Logger.log(TAG, "disconnectPeripheral : address = \"%s\", name = \"%s\" - calling peripheral.disconnect()", peripheral.address(), peripheral.name());
		peripheral.disconnect();
	}

	@Override
	public void gattConnected(BlePeripheral peripheral) {
		EventBus.getDefault().post(new SEvent(kBLEManagerConnectedPeripheralNotification, peripheral));
	}

	@Override
	public void gattDisconnected(BlePeripheral peripheral) {
		EventBus.getDefault().post(new SEvent(kBLEManagerDisconnectedPeripheralNotification, peripheral));
	}

	@Override
	public void gattServicesDiscovered(BlePeripheral peripheral) {
		EventBus.getDefault().post(new SEvent(kBLEManagerPeripheralServiceDiscovered, peripheral));
	}

	@Override
	public void gattDataAvailable(BlePeripheral peripheral, BluetoothGattCharacteristic characteristic, byte[] value) {
		CharacteristicData data = new CharacteristicData();
		data.peripheral = peripheral;
		data.characteristic = characteristic;
		data.value = value;
		EventBus.getDefault().post(new SEvent(kBLEManagerPeripheralDataAvailable, data));
	}

	@Override
	public void gattReadRemoteRssi(BlePeripheral peripheral, int rssi) {
		EventBus.getDefault().post(new SEvent(kBLEManagerPeripheralRssiUpdated, peripheral));
	}

	@Override
	public void gattDescriptorWrite(BlePeripheral peripheral, BluetoothGattDescriptor descriptor, boolean status) {
		DescriptorData data = new DescriptorData();
		data.peripheral = peripheral;
		data.descriptor = descriptor;
		data.success = status;
		EventBus.getDefault().post(new SEvent(kBLEManagerPeripheralDescriptorWrite, data));
	}

	public static String getLongUuidFromShortUuid(String shortUuid) {
		return String.format("0000%s-0000-1000-8000-00805f9b34fb", shortUuid);
	}

	@Override
	public void dataUpdated() {}

	@Override
	public void scanStopped() {
		stopScan(-2);
	}
}
