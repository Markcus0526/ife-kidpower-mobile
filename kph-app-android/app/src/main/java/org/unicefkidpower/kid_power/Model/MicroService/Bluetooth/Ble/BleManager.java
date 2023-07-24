package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.radiusnetworks.bluetooth.BluetoothCrashResolver;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.kid_power.Application.KPHApplication;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.EventManager;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.SEvent;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;

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
	private static final String				TAG = "BleManager";

	public static final int			CHECK_BLUETOOTH_RESULT_FAILURE		= -1;
	public static final int			CHECK_BLUETOOTH_RESULT_SUCCESS		= 0;
	public static final int			CHECK_BLUETOOTH_RESULT_NONE			= 1;

	public static int				BT_ERROR_NO							= 0;
	public static int				BT_ERROR_UNKNOWN					= -1;
	public static int				BT_ERROR_NO_SERVICE					= -2;
	public static int				BT_ERROR_ENABLE_TIMEOUT				= -3;
	public static int				BT_ERROR_NO_ANY_DEVICES				= -4;
	public static int				BT_ERROR_NOT_FOUND					= -5;
	public static int				BT_ERROR_CONNECT_TIMEOUT			= -6;
	public static int				BT_ERROR_CONNECT_FAILED				= -7;
	public static int				BT_ERROR_COMMAND_FAILED				= -8;
	public static int				BT_ERROR_COMMAND_TIMEOUT			= -9;
	public static int				BT_ERROR_COMMAND_WRITE_FAILED		= -10;
	public static int				BT_ERROR_LINK_FAILED				= -11;
	public static int				BT_ERROR_SYNC_FAILED				= -12;


	public static final int			REQUEST_ENABLE_BLE					= 1;

	protected static BleManager		instance							= null;


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
	 *  \brief Posted when BLEPeripheral fails to connectToGoogleFit
	 *
	 *  userInfo keys:
	 *  - kBLEManagerPeripheralKey
	 *  - kBLEManagerManagerKey
	 */
	public static final String kBLEManagerPeripheralConnectionFailedNotification = "blemanager peripheral connection failed notification";
	public static final String kBLEManagerPeripheralServiceDiscovered = "blemanager service discovered";
	public static final String kBLEManagerPeripheralDataAvailable = "blemanager data available";
	public static final String kBLEManagerPeripheralRssiUpdated = "blemanager rssi updated";
	public static final String kBLEManagerPeripheralDescriptorWrite = "peripheral descriptor write";

	/*!
	 *  \brief  Notification posted when Bluetooth state changes
	 */
	public static final String kBLEManagerStateChanged = "blemanager state changed";

	protected BluetoothAdapter			bluetoothAdapter;
	protected Context					contextInstance;
	protected boolean					adapterDisabledManually;
	protected ArrayList<BlePeripheral>	scannedPeripherals;
	protected ArrayList<UUID>			servicesArray;
	protected boolean					scanStarted;
	protected boolean					mustStartScan;
	protected Handler					stopHandler;
	protected Runnable					stopRunnable;
	protected Handler					enableHandler;


	public static class CharacteristicData {
		public BlePeripheral peripheral;
		public BluetoothGattCharacteristic characteristic;
		public byte[] value;
	}

	public static class DescriptorData {
		public BlePeripheral peripheral;
		public BluetoothGattDescriptor descriptor;
		public boolean success;
	}


	public static BleManager initialize(Context context, BluetoothCrashResolver bluetoothCrashResolver) {
		if (instance == null) {
			instance = new BleManager(context, bluetoothCrashResolver);
		}

		return instance;
	}

	public static boolean isBleInitialized() {
		return instance != null;
	}

	public static BleManager sharedInstance() {
		if (instance == null) {
			instance = new BleManager(KPHApplication.sharedInstance(), KPHApplication.sharedInstance().getBluetoothCrashResolver());
		}
		return instance;
	}

	private BleManager(Context context, BluetoothCrashResolver bluetoothCrashResolver) {
		if (!EventManager.sharedInstance().isRegistered(this)) {
			EventManager.sharedInstance().register(this);
		}

		this.contextInstance = context;
		checkBluetoothAdapter();
		bluetoothCrashResolver.setUpdateNotifier(this);
		BleScanner.initialize(context, bluetoothCrashResolver);

		adapterDisabledManually = false;

		scannedPeripherals = new ArrayList<>();
		scanStarted = false;
		mustStartScan = false;

		stopHandler = new Handler(context.getMainLooper());
		stopRunnable = new Runnable() {
			@Override
			public void run() {
				if (scanStarted) {
					stopScan();
				}
			}
		};

		enableHandler = new Handler(context.getMainLooper());
	}

	private void checkBluetoothAdapter() {
		try {
			final BluetoothManager bluetoothManager = (BluetoothManager) contextInstance.getSystemService(Context.BLUETOOTH_SERVICE);
			bluetoothAdapter = bluetoothManager.getAdapter();
		} catch (RuntimeException ex) {
			Logger.error(TAG, "checkBluetoothAdapter : exception(%s)", ex.toString());
		}

		if (bluetoothAdapter == null) {
			Logger.error(TAG, "checkBluetoothAdapter : bluetooth adapter is null, bluetooth is not available");
		} else {
			//Logger.log(TAG, "checkBluetoothAdapter - bluetooth adapter - bluetooth is available");
		}
	}

	public BluetoothAdapter bluetoothAdapter() {
		return bluetoothAdapter;
	}

	public boolean isBleAvailable() {
		if (!isBleSupported(contextInstance))
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

	public void stopAdapter() {
		if (bluetoothAdapter != null) {
			bluetoothAdapter.disable();
			adapterDisabledManually = false;
		}
	}

	public void disableAdapter() {
		if (bluetoothAdapter != null) {
			bluetoothAdapter.disable();
			adapterDisabledManually = true;
		}
	}

	public void enableAdapter() {
		if (bluetoothAdapter != null)
			bluetoothAdapter.enable();
		else {
			checkBluetoothAdapter();
			if (bluetoothAdapter != null)
				bluetoothAdapter.enable();
		}
	}

	public static boolean isBleSupported(Context context) {
		return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
	}


	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onEvent(SEvent e) {
		if (EventManager.isEvent(e, SEvent.EVENT_BLUETOOTH_STATE_CHANGED)) {
			Integer obj = (Integer) e.object;
			int state = obj.intValue();

			//Logger.log(TAG, String.format("BleManager : bluetooth state changed : %d", state));

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
					startScanLocally();
				}
			} else if (state == BluetoothAdapter.STATE_TURNING_OFF) {
				adapterDisabledManually = false;
			}

			EventManager.sharedInstance().post(kBLEManagerStateChanged, obj);
		}
	}

	public boolean scanForPeripheralsWithServices(ArrayList<UUID> services, boolean allowDuplicates) {
		if (scanStarted) {
			Logger.log(TAG, "scanForPeripheralsWithServices : already started scanning");
			return true;
		}

		// check
		if (!isBleSupported(contextInstance))
			return false;

		mustStartScan = true;

		if (!isBleEnabled()) {
			enableAdapter();
			return true;
		}

		this.servicesArray = services;

		return startScanLocally();
	}

	public boolean restartScanForPeripherals() {
		Logger.log(TAG, "restartScanForPeripherals : restart");

		if (!isBleSupported(contextInstance))
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

	private boolean startScanLocally() {
		scannedPeripherals.clear();

		BleScanner.sharedInstance().listner = this;
		boolean isStarted = BleScanner.sharedInstance().start();

		scanStarted = true;

		stopHandler.postDelayed(stopRunnable, 100 * 1000);
		return isStarted;
	}

	public void stopScan() {
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

				EventManager.sharedInstance().post(kBLEManagerDiscoveredPeripheralNotification, existPeripheral);
			} else {
				existPeripheral = new BlePeripheral(contextInstance, device.getAddress(), rssi, scanRecord);
				existPeripheral.delegate = this;
				existPeripheral.scannedTime = currentTime;
				list.add(existPeripheral);

				EventManager.sharedInstance().post(kBLEManagerDiscoveredPeripheralNotification, existPeripheral);
			}
		}
	}

	@Override
	public boolean shouldCheckDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
		if (this.servicesArray == null) {

			if (device.getName() == null) {
				Logger.error(TAG, "shouldCheckDevice : Unsupported band was discovered(name=\"%s\", address=\"%s\")"
						, device.getName(), device.getAddress());
				return false;
			}
			if (device.getName().contains("Power Band")) {
				Logger.log(TAG, "shouldCheckDevice : Power Band was discovered successful (name=\"%s\", address=\"%s\")",
						device.getName(), device.getAddress());
				return true;
			}
			Logger.error(TAG, "shouldCheckDevice : discovered Peripheral (name=\"%s\", address=\"%s\")",
					device.getName(), device.getAddress());
			return false;

		} else {
			ArrayList<UUID> uuids = parseUuids(scanRecord);
			boolean isPeripheral = false;
			for (UUID uuid : uuids) {
				if (servicesArray.contains(uuid)) {
					isPeripheral = true;
					break;
				}
			}
			if (isPeripheral) {
				Logger.log(TAG, "shouldCheckDevice : discovered PowerBand by Service (name=\"%s\", address=\"%s\")",
						device.getName(), device.getAddress());
			} else {
				Logger.error(TAG, "shouldCheckDevice : An unsupported device found by Service (name=\"%s\", address=\"%s\")",
						device.getName(), device.getAddress());
			}
			return isPeripheral;
		}
	}

	private ArrayList<UUID> parseUuids(byte[] advertisedData) {
		ArrayList<UUID> uuids = new ArrayList<>();

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
		boolean ret = peripheral.connect();
		if (!ret) {
			Logger.error(TAG, "connectPeripheral : peripheral(address=\"%s\", name=\"%s\") returned false",
					peripheral.address(), peripheral.name());
		}
		return ret;
	}

	public void disconnectPeripheral(BlePeripheral peripheral) {
		Logger.log(TAG, "disconnectPeripheral : peripheral(address=\"%s\", name=\"%s\") calling peripheral.disconnect()",
				peripheral.address(), peripheral.name());
		peripheral.disconnect();
	}

	@Override
	public void gattConnected(BlePeripheral peripheral) {
		EventManager.sharedInstance().post(kBLEManagerConnectedPeripheralNotification, peripheral);
	}

	@Override
	public void gattDisconnected(BlePeripheral peripheral) {
		EventManager.sharedInstance().post(kBLEManagerDisconnectedPeripheralNotification, peripheral);
	}

	@Override
	public void gattServicesDiscovered(BlePeripheral peripheral) {
		EventManager.sharedInstance().post(kBLEManagerPeripheralServiceDiscovered, peripheral);
	}

	@Override
	public void gattDataAvailable(BlePeripheral peripheral, BluetoothGattCharacteristic characteristic, byte[] value) {
		CharacteristicData data = new CharacteristicData();
		data.peripheral = peripheral;
		data.characteristic = characteristic;
		data.value = value;

		EventManager.sharedInstance().post(kBLEManagerPeripheralDataAvailable, data);
	}

	@Override
	public void gattReadRemoteRssi(BlePeripheral peripheral, int rssi) {
		EventManager.sharedInstance().post(kBLEManagerPeripheralRssiUpdated, peripheral);
	}

	@Override
	public void gattDescriptorWrite(BlePeripheral peripheral, BluetoothGattDescriptor descriptor, boolean status) {
		DescriptorData data = new DescriptorData();
		data.peripheral = peripheral;
		data.descriptor = descriptor;
		data.success = status;

		EventManager.sharedInstance().post(kBLEManagerPeripheralDescriptorWrite, data);
	}

	public static String getLongUuidFromShortUuid(String shortUuid) {
		return String.format("0000%s-0000-1000-8000-00805f9b34fb", shortUuid);
	}


	@Override
	public void dataUpdated() {
	}

	@Override
	public void scanStopped() {
		stopScan();
	}


	public static int checkInitBluetooth(SuperActivity activity) {
		if (!BleManager.isBleSupported(activity.getApplicationContext())) {
			return CHECK_BLUETOOTH_RESULT_FAILURE;
		}

		if (!BleManager.isBleInitialized()) {
			BleManager.initialize(activity.getApplicationContext(), ((KPHApplication) activity.getApplication()).getBluetoothCrashResolver());
		}

		if (!BleManager.sharedInstance().isBleAvailable()) {
			return CHECK_BLUETOOTH_RESULT_FAILURE;
		}

		if (!BleManager.sharedInstance().isBleEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			activity.startActivityForResult(enableBtIntent, BleManager.REQUEST_ENABLE_BLE);
			return CHECK_BLUETOOTH_RESULT_NONE;
		} else {
			return CHECK_BLUETOOTH_RESULT_SUCCESS;
		}
	}

}
