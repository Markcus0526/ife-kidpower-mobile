package org.unicefkidpower.schools.powerband;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.schools.ble.BleManager;
import org.unicefkidpower.schools.ble.BlePeripheral;
import org.unicefkidpower.schools.event.SEvent;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.powerband.command.BandCommandResponse;
import org.unicefkidpower.schools.powerband.command.CmdAniCelebDisplay;
import org.unicefkidpower.schools.powerband.command.CmdDailyDetailedGet;
import org.unicefkidpower.schools.powerband.command.CmdDailySummaryGet;
import org.unicefkidpower.schools.powerband.command.CmdDeviceFirmwareGet;
import org.unicefkidpower.schools.powerband.command.CmdDeviceTimeGet;
import org.unicefkidpower.schools.powerband.command.CmdDeviceTimeSet;
import org.unicefkidpower.schools.powerband.command.CmdMessageSet;
import org.unicefkidpower.schools.powerband.command.CmdNameSet;
import org.unicefkidpower.schools.powerband.command.CmdPersonalInfoSet;
import org.unicefkidpower.schools.powerband.command.CmdSetTotalPowerPoint;
import org.unicefkidpower.schools.powerband.command.CmdStorageInfoGet;
import org.unicefkidpower.schools.powerband.service.BatteryService;
import org.unicefkidpower.schools.powerband.service.DataService;
import org.unicefkidpower.schools.powerband.service.DeviceInformationService;

import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Created by donal_000 on 1/13/2015.
 */
public class PowerBandDevice {
	public static final String TAG = "PowerBandDevice";

	public static final boolean DISABLE_ADAPTER_WHEN_FAILED = true;

	public static final int DEVICE_NOTCONNECTED = 0;
	public static final int DEVICE_CONNECTING = 1;
	public static final int DEVICE_CONNECTED = 2;
	public static final int DEVICE_DISCONNECTED = 3;
	public static final int DEVICE_SYNCING = 4;
	public static final int DEVICE_SYNCFINISHED = 5;
	public static final int DEVICE_UPLOADFAILED = 10;

	public static final String kPowerBandDeviceDataUpdatedNotification = "power band device data updated notification";
	public static final String kPowerBandDeviceConnectedNotification = "power band device connected notification";
	public static final String kPowerBandDeviceDisconnectedNotification = "power band device disconnected notification";

	protected BlePeripheral					_peripheral;
	protected DataService					_dataService;
	protected DeviceInformationService		_deviceInfoService;
	protected BatteryService				_batteryService;

	private int _batteryLevel;

	private int mDeviceState = DEVICE_NOTCONNECTED;

	public PowerBandDevice(BlePeripheral peripheral) {
		_peripheral = peripheral;

		_dataService = new DataService();
		_batteryService = new BatteryService();
		_deviceInfoService = new DeviceInformationService();

		EventBus.getDefault().register(this);

		if (peripheral != null) {
			if (peripheral.connectionState() == BlePeripheral.STATE_CONNECTED) {
				if (mDeviceState == DEVICE_NOTCONNECTED) {
					_peripheralConnected(peripheral);
				}
			}
		}
	}

	public boolean isKidPowerBand() {
		if (_peripheral == null)
			return false;
		return _peripheral.name().contains(BleManager.POWERBAND);
	}

	public boolean isCalorieCloudBand() {
		if (_peripheral == null)
			return false;
		return _peripheral.isCalorieCloudBand();
	}

	public int deviceState() {
		return mDeviceState;
	}

	public String address() {
		if (_peripheral == null)
			return null;
		return _peripheral.address();
	}

	public String name() {
		if (_peripheral == null)
			return "";
		else
			return _peripheral.name();
	}

	public String code() {
		return _peripheral.getCode();
	}

	public String deviceId() {
		return _peripheral.getMACAddress();
	}

	public BlePeripheral peripheral() {
		return _peripheral;
	}

	public int batteryLevel() {
		return _batteryLevel;
	}

	public int rssi() {
		return _peripheral.rssi();
	}

	public void disconnect() {
		if (mDeviceState == DEVICE_CONNECTING ||
				mDeviceState == DEVICE_CONNECTED ||
				mDeviceState == DEVICE_SYNCING) {
			Logger.log(TAG, "PowerBandDevice.disconnect() mDeviceState(%d) => BAND_DISCONNECTED", mDeviceState);
			mDeviceState = DEVICE_DISCONNECTED;
		}
		//EventBus.getDefault().unregister(this);
	}

	public void setDeviceState(int deviceState) {
		this.mDeviceState = deviceState;
		EventBus.getDefault().post(new SEvent(kPowerBandDeviceDataUpdatedNotification, this));
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent e) {
		if (BleManager.kBLEManagerConnectedPeripheralNotification.equalsIgnoreCase(e.name)) {
			_peripheralConnected((BlePeripheral) e.object);
		} else if (BleManager.kBLEManagerDisconnectedPeripheralNotification.equalsIgnoreCase(e.name)) {
			_peripheralDisconnected((BlePeripheral) e.object);
		} else if (BleManager.kBLEManagerPeripheralServiceDiscovered.equalsIgnoreCase(e.name)) {
			_retrievedCharacteristics((BlePeripheral) e.object);
		} else if (BleManager.kBLEManagerPeripheralDataAvailable.equalsIgnoreCase(e.name)) {
			BleManager.CharacteristicData data = (BleManager.CharacteristicData) e.object;
			_readCharacteristic(data.peripheral, data.characteristic, data.value);
		} else if (BleManager.kBLEManagerPeripheralRssiUpdated.equalsIgnoreCase(e.name)) {
			_rssiUpdated((BlePeripheral) e.object);
		} else if (DeviceInformationService.kDeviceInformationReadSerialNumber.equalsIgnoreCase(e.name)) {
			_readDeviceSerialNumber((BlePeripheral) e.object);
		} else if (BatteryService.kBatteryServiceReadBatteryLevel.equalsIgnoreCase(e.name)) {
			_updatedBatteryLevel((BlePeripheral) e.object);
		} else if (BleManager.kBLEManagerPeripheralDescriptorWrite.equalsIgnoreCase(e.name)) {
			BleManager.DescriptorData data = (BleManager.DescriptorData) e.object;
			_descriptorWrite(data.peripheral, data.descriptor, data.success);
		} else if (DataService.kLocalCharacteristicNotificationFailed.equalsIgnoreCase(e.name)) {
			_notificationFailed((BlePeripheral) e.object);
		} else if (DataService.kLocalWriteDataFailed.equalsIgnoreCase(e.name)) {
			_writeFailed((BlePeripheral) e.object);
		}
	}

	protected void _peripheralConnected(BlePeripheral peripheral) {
		if (peripheral != _peripheral || peripheral == null) {
			Logger.error(TAG, "peripheralConnected : not for this");
			return;
		}

		if (mDeviceState == DEVICE_NOTCONNECTED) {
			Logger.log(TAG, "PowerBandDevice._peripheralConnected (%s), state == NOT_CONNECTED => CONNECTING", peripheral.address());
			mDeviceState = DEVICE_CONNECTING;
		} else {
			Logger.log(TAG, "PowerBandDevice._peripheralConnected (%s), state (%d) != NOT_CONNECTED", peripheral.address(), mDeviceState);
		}

		EventBus.getDefault().post(new SEvent(kPowerBandDeviceDataUpdatedNotification, this));
	}

	protected void _peripheralDisconnected(BlePeripheral peripheral) {
		if (peripheral != _peripheral || peripheral == null) {
			Logger.error(TAG, "Peripheral disconnected, but peripheral isn't same, %s:%s",
					_peripheral.getMACAddress(), peripheral.getMACAddress());
			return;
		}

		EventBus.getDefault().unregister(this);
		Logger.log(TAG, "PowerBandDevice._peripheralDisconnected (%s)", peripheral.address());

		if (mDeviceState == DEVICE_NOTCONNECTED) {
			mDeviceState = DEVICE_DISCONNECTED;
			Logger.error(TAG, "PowerBandDevice._peripheralDisconnected (%s), state => BAND_DISCONNECTED", peripheral.address());
		} else if (mDeviceState == DEVICE_CONNECTING) {
			Logger.error(TAG, "PowerBandDevice._peripheralDisconnected, mBandState == BAND_CONNECTING");
			//SyncManager.sharedInstance().disconnect(this);
			mDeviceState = DEVICE_DISCONNECTED;
			_peripheral.disconnect();
		} else if (mDeviceState == DEVICE_CONNECTED) {
			Logger.error(TAG, "PowerBandDevice._peripheralDisconnected, mBandState == BAND_CONNECTED, change state to DISCONNECTED");
			mDeviceState = DEVICE_DISCONNECTED;
			Logger.error(TAG, "PowerBandDevice._peripheralDisconnected (%s), state => BAND_DISCONNECTED", peripheral.address());
		} else if (mDeviceState == DEVICE_SYNCING) {
			mDeviceState = DEVICE_DISCONNECTED;
		} else if (mDeviceState == DEVICE_SYNCFINISHED) {
			//
		} else if (mDeviceState == DEVICE_UPLOADFAILED) {
			//
		}

		EventBus.getDefault().post(new SEvent(kPowerBandDeviceDisconnectedNotification, this));
	}

	protected void _retrievedCharacteristics(BlePeripheral peripheral) {
		if (peripheral != _peripheral || peripheral == null)
			return;

		Logger.log(TAG, "_retrievedCharacteristics, peripheral(%s)", _peripheral.address());
		List<BluetoothGattService> serviceList = peripheral.getSupportedGattServices();
		Iterator i = serviceList.iterator(); // Must be in synchronized block
		while (i.hasNext()) {
			BluetoothGattService service = (BluetoothGattService) i.next();
			if (service.getUuid().equals(BatteryService.batteryServiceID())) {
				_batteryService.setService(peripheral, service);
				Logger.log(TAG, "_retrievedCharacteristics , _peripheral(%s), batteryService", _peripheral.address());
			} else if (service.getUuid().equals(DeviceInformationService.deviceInformationServiceID())) {
				_deviceInfoService.setService(peripheral, service);
				Logger.log(TAG, "_retrievedCharacteristics , _peripheral(%s), deviceInfoService", _peripheral.address());
			} else if (service.getUuid().equals(DataService.dataServiceId())) {
				_dataService.setService(peripheral, service);
				Logger.log(TAG, "_retrievedCharacteristics , _peripheral(%s), dataService", _peripheral.address());
			}
		}

		if (_dataService.service() == null) {
			Logger.error(TAG, "_retrievedCharacteristics , _peripheral(%s), retrievedCharacteristics error", _peripheral.address());
			_peripheral.disconnect();
		}

	}

	protected void _readCharacteristic(BlePeripheral peripheral, BluetoothGattCharacteristic characteristic, byte[] value) {
		if (peripheral != _peripheral || peripheral == null) {
			return;
		}
		// read value for this characteristic
		if (characteristic.getService() == _deviceInfoService.service()) {
			// Logger.log(TAG, "_readCharacteristic : _peripheral(%s), _deviceInfoService", _peripheral.address());
			_deviceInfoService.readCharacteristic(characteristic, value);
		} else if (characteristic.getService() == _batteryService.service()) {
			// Logger.log(TAG, "_readCharacteristic : _peripheral(%s), _batteryService", _peripheral.address());
			_batteryService.readCharacteristic(characteristic, value);
		} else if (characteristic.getService() == _dataService.service()) {
			// Logger.log(TAG, "_readCharacteristic : _peripheral(%s), _dataService", _peripheral.address());
			_dataService.readCharacteristic(characteristic, value);
		}
	}

	protected void _updatedBatteryLevel(BlePeripheral peripheral) {
		if (peripheral == _peripheral) {
			_batteryLevel = _batteryService.batteryLevel;

			EventBus.getDefault().post(new SEvent(kPowerBandDeviceDataUpdatedNotification, this));
		}
	}

	protected void _rssiUpdated(BlePeripheral peripheral) {
		if (peripheral == _peripheral) {
			EventBus.getDefault().post(new SEvent(kPowerBandDeviceDataUpdatedNotification, this));
		}
	}

	protected void _readDeviceSerialNumber(BlePeripheral peripheral) {
		if (peripheral == _peripheral) {
			Logger.log(TAG, "_readDevicesSerialNumber (%s)", peripheral.address());
			//_startAuthentication();
			//_startKeyValidation();
		}
	}

	/*
	 * called when wrote description for notification successfully.
	 */
	protected void _descriptorWrite(BlePeripheral peripheral, BluetoothGattDescriptor descriptor, boolean success) {
		if (peripheral != _peripheral || peripheral == null)
			return;

		boolean ret = _dataService.descriptorWrite(descriptor, success);
		if (ret == false) {
			Logger.error(TAG, "_descriptorWrite, peripheral (%s), descriptor write failed", peripheral.address());
			return;
		}

		Logger.log(TAG, "_descriptorWrite, peripheral (%s), BAND_CONNECTED, will start syncing", peripheral.address());
		mDeviceState = DEVICE_CONNECTED;

		EventBus.getDefault().post(new SEvent(kPowerBandDeviceConnectedNotification, this));
	}

	protected void _notificationFailed(BlePeripheral peripheral) {
		if (peripheral != _peripheral)
			return;

		Logger.error(TAG, "PowerBandDevice._notificationFailed, peripheral(%s)", peripheral.address());
		mDeviceState = DEVICE_DISCONNECTED;
		_peripheral.disconnect();

		if (DISABLE_ADAPTER_WHEN_FAILED) {
			BleManager.sharedInstance().disableAdapter();
		}
	}

	protected void _writeFailed(BlePeripheral peripheral) {
		if (peripheral != _peripheral)
			return;

		Logger.error(TAG, "PowerBandDevice._writeFailed, peripheral(%s)", peripheral.address());
		mDeviceState = DEVICE_DISCONNECTED;
		_peripheral.disconnect();

		if (DISABLE_ADAPTER_WHEN_FAILED) {
			BleManager.sharedInstance().disableAdapter();
		}
	}

	public void setName(String name, WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "setName failed, _dataService is null");
			return;
		}
		if (name == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "setName failed, name is null");
			return;
		}
		if (isCalorieCloudBand()) {
			// if this band is GWS band, skip this command
			callback.onWrite(true, null);
			Logger.log(TAG, "CalorieCloud band -> setName skipped");
			return;
		}
		CmdNameSet command = new CmdNameSet(name);
		_dataService.writeCommand(command, callback);
	}

	public void setPersonalInformation(int gender, int age, int height, int weight, int strideLength, WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "setPersonalInformation failed, _dataService is null");
			return;
		}
		CmdPersonalInfoSet command = new CmdPersonalInfoSet(gender, age, height, weight, strideLength);
		_dataService.writeCommand(command, callback);
	}

	public void setMessage(int slot, String message, WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "setMessage failed, _dataService is null");
			return;
		}

		if (isCalorieCloudBand()) {
			// if this band is GWS band, skip this command
			callback.onWrite(true, null);
			Logger.log(TAG, "CalorieCloud band -> setMessage(%d) skipped", slot);
			return;
		}
		CmdMessageSet command = new CmdMessageSet(slot, message);
		_dataService.writeCommand(command, callback);
	}

	public void getStorageInformation(WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "getStorageInformation failed, _dataService is null");
			return;
		}
		if (isCalorieCloudBand()) {
			// if this band is GWS band, skip this command
			CmdStorageInfoGet.CmdStorageInfoGetRes res = CmdStorageInfoGet.getEmptyResponse();

			callback.onWrite(true, res);
			Logger.log(TAG, "CalorieCloud band -> getStorageInformation skipped");
			return;
		}
		CmdStorageInfoGet command = new CmdStorageInfoGet();
		_dataService.writeCommand(command, callback);
	}

	public void getDailySummaryData(int days, WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "getDailySummaryData failed, _dataService is null");
			return;
		}
		CmdDailySummaryGet command = new CmdDailySummaryGet(days);
		_dataService.writeCommand(command, callback);
	}

	// daily detailed data
	public void getDailyDetailedData(int days, WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "getDailyDetailedData failed, _dataService is null");
			return;
		}
		CmdDailyDetailedGet command = new CmdDailyDetailedGet(days);
		_dataService.writeCommand(command, callback);
	}

	// get firmware version
	public void getFirmwareVersion(WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "getDeviceTime failed, _dataService is null");
			return;
		}
		CmdDeviceFirmwareGet command = new CmdDeviceFirmwareGet();
		_dataService.writeCommand(command, callback);
	}

	// get device time
	public void getDeviceTime(WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "getDeviceTime failed, _dataService is null");
			return;
		}
		CmdDeviceTimeGet command = new CmdDeviceTimeGet();
		_dataService.writeCommand(command, callback);
	}

	// set device time
	public void setDeviceTime(Date dt, WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "setDeviceTime failed, _dataService is null");
			return;
		}
		CmdDeviceTimeSet command = new CmdDeviceTimeSet(dt);
		_dataService.writeCommand(command, callback);
	}

	public void sendAnimatedCelebrationDisplay(WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "sendAnimatedCelebrationDisplay failed, _dataService is null");
			return;
		}
		CmdAniCelebDisplay command = new CmdAniCelebDisplay();
		_dataService.writeCommand(command, callback);
	}

	// Set total power points
	public void setTotalPowerPoints(int powerPoints, WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "sendAnimatedCelebrationDisplay failed, _dataService is null");
			return;
		}

		CmdSetTotalPowerPoint command = new CmdSetTotalPowerPoint(powerPoints);
		_dataService.writeCommand(command, callback);
	}

	public interface WriteCommandCallback {
		public void onWrite(boolean success, BandCommandResponse response);
	}
}
