package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;

import org.unicefkidpower.kid_power.Misc.Logger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by donal_000 on 12/1/2014.
 */
public class BlePeripheral {
	private static final String			TAG = "BlePeripheral";

	private Context						contextInstance;
	private BluetoothAdapter			bluetoothAdapterInstance;
	private String						bluetoothDeviceAddress;
	private BluetoothGatt				bluetoothGatt;
	private int							connectionState = STATE_DISCONNECTED;
	private BluetoothDevice				bluetoothDeviceInstance;
	private int							rssiValue;
	private String						serialNo;
	public long							scannedTime;


	public static final int				STATE_DISCONNECTED			= 0;
	public static final int				STATE_CONNECTING			= 1;
	public static final int				STATE_CONNECTED				= 2;

	public static final int				CONNECTION_TIMEOUT			= 20;
	public static final int				DISCOVERING_TIMEOUT			= 60;
	public static final int				DISCONNECTION_TIMEOUT		= 20;

	public BlePeripheralDelegate		delegate;

	private String						addressValue;
	private String						nameValue;
	private byte[]						scanRecordArray;

	private Handler						timeoutHandler;
	private Runnable					timeoutRunnable;
	private Runnable					timeoutDiscoverRunnable;
	private Runnable					timeoutDisconnectRunnable;


	public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

	// Implements callback methods for GATT events that the app cares about.  For example,
	// connection change and services discovered.
	private final BluetoothGattCallback gattCallbackInstance = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				connectionState = STATE_CONNECTED;
				timeoutHandler.removeCallbacks(timeoutRunnable);

				if (delegate != null)
					delegate.gattConnected(BlePeripheral.this);

				//Logger.log(TAG, "Connected to GATT server.");
				// Attempts to discover services after successful connection.
				Logger.log(TAG, "onConnectionStateChange : Attempting to start service discovery:" + bluetoothGatt.discoverServices());

				timeoutHandler.postDelayed(timeoutDiscoverRunnable, TimeUnit.SECONDS.toMillis(DISCOVERING_TIMEOUT));

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

				connectionState = STATE_DISCONNECTED;
				timeoutHandler.removeCallbacks(timeoutRunnable);
				timeoutHandler.removeCallbacks(timeoutDisconnectRunnable);

				Logger.log(TAG, "onConnectionStateChange : Disconnected from GATT server.");

				if (delegate != null)
					delegate.gattDisconnected(BlePeripheral.this);

				close();
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				timeoutHandler.removeCallbacks(timeoutDiscoverRunnable);
				if (delegate != null)
					delegate.gattServicesDiscovered(BlePeripheral.this);
			} else {
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
										 BluetoothGattCharacteristic characteristic,
										 int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (delegate != null)
					delegate.gattDataAvailable(BlePeripheral.this, characteristic, characteristic.getValue());
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
											BluetoothGattCharacteristic characteristic) {
			if (delegate != null)
				delegate.gattDataAvailable(BlePeripheral.this, characteristic, characteristic.getValue());
		}

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			rssiValue = rssi;
			if (delegate != null)
				delegate.gattReadRemoteRssi(BlePeripheral.this, rssi);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
									  int status) {
			if (delegate != null)
				delegate.gattDescriptorWrite(BlePeripheral.this, descriptor, status == BluetoothGatt.GATT_SUCCESS);
		}
	};

	private BlePeripheral() {
	}

	public BlePeripheral(Context context, String address, int rssi, byte[] scanRecord) {
		this.contextInstance = context;
		this.bluetoothDeviceInstance = null;
		this.addressValue = address;
		this.rssiValue = rssi;
		this.scanRecordArray = scanRecord;

		this.scannedTime = System.currentTimeMillis();

		this.timeoutHandler = new Handler(context.getMainLooper());
		this.timeoutRunnable = new Runnable() {
			@Override
			public void run() {
				if (connectionState() != STATE_CONNECTED) {
					Logger.error(TAG, "BlePeripheral Runnable Timeout - disconnect()");
					disconnect();
					timeoutHandler.removeCallbacks(timeoutDisconnectRunnable);
					if (delegate != null) {
						delegate.gattDisconnected(BlePeripheral.this);
					}
					close();
				}
			}
		};

		this.timeoutDiscoverRunnable = new Runnable() {
			@Override
			public void run() {
				if (connectionState() == STATE_CONNECTED) {
					Logger.error(TAG, "BlePeripheral discovering timeout - disconnect()");
					disconnect();
					timeoutHandler.removeCallbacks(timeoutDisconnectRunnable);
					if (delegate != null) {
						delegate.gattDisconnected(BlePeripheral.this);
					}
					close();
				}
			}
		};

		this.timeoutDisconnectRunnable = new Runnable() {
			@Override
			public void run() {
				if (connectionState() == STATE_CONNECTED) {
					Logger.error(TAG, "BlePeripheral disconnect timeout - delegate.gattDisconnected(this)");
					if (delegate != null)
						delegate.gattDisconnected(BlePeripheral.this);
					close();
				}
			}
		};

		bluetoothAdapterInstance = BleManager.sharedInstance().bluetoothAdapter();
		if (bluetoothAdapterInstance == null) {
			Logger.error(TAG, "BlePeripheral(%s) - blemanager.bluetoothadapter is null", address);
			return;
		}

		this.bluetoothDeviceInstance = bluetoothAdapterInstance.getRemoteDevice(addressValue);
		if (this.bluetoothDeviceInstance == null) {
			Logger.error(TAG, "BlePeripheral(%s) - bluetoothadapter.getremotedevice is null", address);
			return;
		}

		nameValue = this.bluetoothDeviceInstance.getName();
	}

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 *
	 * @return Return true if the connection is initiated successfully. The connection result
	 * is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public boolean connect() {
		if (bluetoothAdapterInstance == null || addressValue == null) {
			Logger.error(TAG, "BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		// Previously connected device.  Try to reconnect.
		if (bluetoothDeviceAddress != null && addressValue.equals(bluetoothDeviceAddress)
				&& bluetoothGatt != null) {
			Logger.log(TAG, "connect : trying to use an existing bluetoothGatt for connection.");
			if (bluetoothGatt.connect()) {
				connectionState = STATE_CONNECTING;
				timeoutHandler.postDelayed(timeoutRunnable, TimeUnit.SECONDS.toMillis(CONNECTION_TIMEOUT));
				return true;
			} else {
				return false;
			}
		}

		final BluetoothDevice device = bluetoothAdapterInstance.getRemoteDevice(addressValue);
		if (device == null) {
			Logger.error(TAG, "Device not found.  Unable to connectToGoogleFit.");
			return false;
		}
		// We want to directly connectToGoogleFit to the device, so we are setting the autoConnect
		// parameter to false.
		//Logger.log(TAG, "Trying to create a new connection.");
		bluetoothGatt = device.connectGatt(contextInstance, false, gattCallbackInstance);
		bluetoothDeviceAddress = addressValue;
		connectionState = STATE_CONNECTING;

		timeoutHandler.postDelayed(timeoutRunnable, TimeUnit.SECONDS.toMillis(CONNECTION_TIMEOUT));

		return true;
	}

	/**
	 * Disconnects an existing connection or cancel a pending connection. The disconnection result
	 * is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public void disconnect() {
		if (bluetoothAdapterInstance == null || bluetoothGatt == null) {
			Logger.error(TAG, "disconnect, BluetoothAdapter not initialized");
			return;
		}
		//Logger.log(TAG, "BlePeripheral bluetoothGatt.disconnect()");
		timeoutHandler.postDelayed(timeoutDisconnectRunnable, TimeUnit.SECONDS.toMillis(DISCONNECTION_TIMEOUT));
		bluetoothGatt.disconnect();
	}

	/**
	 * After using a given BLE device, the app must call this method to ensure resources are
	 * released properly.
	 */
	public void close() {
		if (bluetoothGatt == null) {
			return;
		}
		bluetoothGatt.close();
		bluetoothGatt = null;
	}

	public int connectionState() {
		return connectionState;
	}

	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
	 * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 *
	 * @param characteristic The characteristic to read from.
	 */
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (bluetoothAdapterInstance == null || bluetoothGatt == null) {
			Logger.error(TAG, "readCharacteristic, BluetoothAdapter not initialized");
			return;
		}
		bluetoothGatt.readCharacteristic(characteristic);
	}

	public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (bluetoothAdapterInstance == null || bluetoothGatt == null) {
			Logger.error(TAG, "writeCharacteristic, BluetoothAdaptor or bluetoothGatt is null");
			return false;
		}
		return bluetoothGatt.writeCharacteristic(characteristic);
	}

	/**
	 * Enables or disables notification on a give characteristic.
	 *
	 * @param characteristic Characteristic to act on.
	 * @param enabled        If true, enable notification.  False otherwise.
	 */
	public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (bluetoothAdapterInstance == null) {
			Logger.error(TAG, "setCharacteristicNotification, BluetoothAdapter not initialized");
			return;
		}

		if (bluetoothGatt == null) {
			Logger.error(TAG, "setCharacteristicNotification, BluetoothGatt is null");
			return;
		}

		bluetoothGatt.setCharacteristicNotification(characteristic, enabled);

		if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
			BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
					UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			bluetoothGatt.writeDescriptor(descriptor);
		}
	}

	/**
	 * Retrieves a list of supported GATT services on the connected device. This should be
	 * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
	 *
	 * @return A {@code List} of supported services.
	 */
	public List<BluetoothGattService> getSupportedGattServices() {
		if (bluetoothGatt == null) return null;

		return bluetoothGatt.getServices();
	}

	public String name() {
		return nameValue;
	}

	public String address() {
		return addressValue;
	}

	public String deviceIdByAddress() {
		if (addressValue == null)
			return null;
		return addressValue.replace(":", "");
	}

	public int rssi() {
		return rssiValue;
	}

	public void setRssi(int rssi) {
		rssiValue = rssi;
	}

	public void updateRSSI() {
		if (connectionState == STATE_CONNECTED)
			bluetoothGatt.readRemoteRssi();
	}

	public String serialno() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public String getCode() {
		String name = name();
		if (name == null)
			return "";
		if (name.length() == 0)
			return "";
		if (name.length() < "Power Band".length() + 6)
			return "";
		String code = name.substring(11);
		return code;
	}
}
