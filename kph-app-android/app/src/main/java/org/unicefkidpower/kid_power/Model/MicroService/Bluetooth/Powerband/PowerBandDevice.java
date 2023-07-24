package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble.BleManager;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble.BlePeripheral;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.EventManager;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.SEvent;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.BandCommandResponse;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdAniCelebDisplaySend;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetCalorieMissionGoal;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetDailyData;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetDailySummaryData;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetFirmwareVersion;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetFirmwareVersionParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetMacAddress;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetStorageInformation;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetTotalPowerPoint;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetUserName;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdSetCalorieMissionGoal;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdSetDeviceTime;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdSetMessage;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdSetPersonalInformation;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdSetTotalPowerPoint;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdSetUserName;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdStaticImageSet;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.service.BatteryService;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.service.DataService;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.service.DeviceInformationService;
import org.unicefkidpower.kid_power.Misc.Logger;

import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Created by donal_000 on 1/13/2015.
 */
public class PowerBandDevice {
	private static final String TAG = "PowerBandDevice";

	public static final boolean DISABLE_ADAPTER_WHEN_FAILED = true;

	public static final int DEVICE_NOTCONNECTED = 0;
	public static final int DEVICE_CONNECTING = 1;
	public static final int DEVICE_CONNECTED = 2;
	public static final int DEVICE_DISCONNECTED = 3;
	public static final int DEVICE_PERFORMING = 4;
	public static final int DEVICE_FINISHED = 5;
	//public static final int DEVICE_UPLOADFAILED = 10;

	public static final String kPowerBandDeviceDataUpdatedNotification = "power band device data updated notification";
	public static final String kPowerBandDeviceConnectedNotification = "power band device connected notification";
	public static final String kPowerBandDeviceDisconnectedNotification = "power band device disconnected notification";

	protected BlePeripheral _peripheral;
	protected DataService _dataService;
	protected DeviceInformationService _deviceInfoService;
	protected BatteryService _batteryService;

	private int _batteryLevel;

	private int mDeviceState = DEVICE_NOTCONNECTED;

	private String fwVersion;

	public PowerBandDevice(BlePeripheral peripheral) {
		_peripheral = peripheral;

		_dataService = new DataService();
		_batteryService = new BatteryService();
		_deviceInfoService = new DeviceInformationService();

		EventManager.sharedInstance().register(this);

		if (peripheral != null) {
			if (peripheral.connectionState() == BlePeripheral.STATE_CONNECTED) {
				if (mDeviceState == DEVICE_NOTCONNECTED) {
					_peripheralConnected(peripheral);
				}
			}
		}
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

	public BlePeripheral peripheral() {
		return _peripheral;
	}

	public int batteryLevel() {
		return _batteryLevel;
	}

	public int rssi() {
		return _peripheral.rssi();
	}

	public String getFwVersion() {
		return fwVersion;
	}

	public void setFwVersion(String fwVersion) {
		this.fwVersion = fwVersion;
	}

	public void disconnect() {
		if (mDeviceState == DEVICE_CONNECTING ||
				mDeviceState == DEVICE_CONNECTED ||
				mDeviceState == DEVICE_PERFORMING) {
			//Logger.log(TAG, "PowerBandDevice.disconnect() mDeviceState(%d) => BAND_DISCONNECTED", mDeviceState);
			mDeviceState = DEVICE_DISCONNECTED;
		}

		if (EventManager.sharedInstance().isRegistered(this)) {
			EventManager.sharedInstance().unregister(this);
		}
	}

	public void setDeviceState(int deviceState) {
		this.mDeviceState = deviceState;
		EventManager.sharedInstance().post(kPowerBandDeviceDataUpdatedNotification, this);
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
		if (peripheral != _peripheral || peripheral == null)
			return;

		if (mDeviceState == DEVICE_NOTCONNECTED) {
			Logger.log(TAG, "_peripheralConnected : peripheral(%s), state == BAND_NOTCONNECTED => BAND_CONNECTING", peripheral.address());
			mDeviceState = DEVICE_CONNECTING;
		} else {
			Logger.log(TAG, "_peripheralConnected : peripheral(%s), state (%d) != BAND_NOTCONNECTED", peripheral.address(), mDeviceState);
		}

		EventManager.sharedInstance().post(kPowerBandDeviceDataUpdatedNotification, this);
	}

	protected void _peripheralDisconnected(BlePeripheral peripheral) {
		if (peripheral != _peripheral || peripheral == null)
			return;

		//Logger.log(TAG, "PowerBandDevice._peripheralDisconnected (%s)", peripheral.address());

		if (mDeviceState == DEVICE_NOTCONNECTED) {
			mDeviceState = DEVICE_DISCONNECTED;
			Logger.error(TAG, "_peripheralDisconnected : peripheral(%s), state => BAND_DISCONNECTED", peripheral.address());
		} else if (mDeviceState == DEVICE_CONNECTING) {
			Logger.error(TAG, "_peripheralDisconnected : peripheral(%s), mBandState == BAND_CONNECTING", peripheral.address());
			//SyncManager.sharedInstance().disconnect(this);
			mDeviceState = DEVICE_DISCONNECTED;
			_peripheral.disconnect();
		} else if (mDeviceState == DEVICE_CONNECTED) {
			Logger.error(TAG, "_peripheralDisconnected : peripheral(%s), mBandState == BAND_CONNECTED, change state to DISCONNECTED", peripheral.address());
			mDeviceState = DEVICE_DISCONNECTED;
			Logger.error(TAG, "_peripheralDisconnected : peripheral(%s), state => BAND_DISCONNECTED", peripheral.address());
		} else if (mDeviceState == DEVICE_PERFORMING) {
			Logger.error(TAG, "_peripheralDisconnected : peripheral(%s), mBandState == DEVICE_PERFORMING, change state to DISCONNECTED", peripheral.address());
			mDeviceState = DEVICE_DISCONNECTED;
			Logger.error(TAG, "_peripheralDisconnected : peripheral(%s), state => BAND_DISCONNECTED", peripheral.address());
		}

		EventManager.sharedInstance().post(kPowerBandDeviceDisconnectedNotification, this);
	}

	protected void _retrievedCharacteristics(BlePeripheral peripheral) {
		if (peripheral != _peripheral || peripheral == null)
			return;

		//Logger.log(TAG, "_retrievedCharacteristics, peripheral(%s)", _peripheral.address());
		List<BluetoothGattService> serviceList = peripheral.getSupportedGattServices();
		Iterator i = serviceList.iterator(); // Must be in synchronized block
		while (i.hasNext()) {
			BluetoothGattService service = (BluetoothGattService) i.next();
			if (service.getUuid().equals(BatteryService.batteryServiceID())) {
				_batteryService.setService(peripheral, service);
				Logger.log(TAG, "_retrievedCharacteristics : peripheral(%s), batteryService", _peripheral.address());
			} else if (service.getUuid().equals(DeviceInformationService.deviceInformationServiceID())) {
				_deviceInfoService.setService(peripheral, service);
				Logger.log(TAG, "_retrievedCharacteristics : peripheral(%s), deviceInfoService", _peripheral.address());
			} else if (service.getUuid().equals(DataService.dataServiceId())) {
				_dataService.setService(peripheral, service);
				Logger.log(TAG, "_retrievedCharacteristics : peripheral(%s), dataService", _peripheral.address());
			}
		}

		if (_dataService.service() == null) {
			Logger.error(TAG, "_retrievedCharacteristics : peripheral(%s), retrievedCharacteristics error", _peripheral.address());
			_peripheral.disconnect();
		}

	}

	protected void _readCharacteristic(BlePeripheral peripheral, BluetoothGattCharacteristic characteristic, byte[] value) {
		if (peripheral != _peripheral || peripheral == null) {
			return;
		}
		// read value for this characteristic
		if (characteristic.getService() == _deviceInfoService.service()) {
			Logger.log(TAG, "_readCharacteristic : peripheral(%s), _deviceInfoService", _peripheral.address());
			_deviceInfoService.readCharacteristic(characteristic, value);
		} else if (characteristic.getService() == _batteryService.service()) {
			Logger.log(TAG, "_readCharacteristic : peripheral(%s), _batteryService", _peripheral.address());
			_batteryService.readCharacteristic(characteristic, value);
		} else if (characteristic.getService() == _dataService.service()) {
			Logger.log(TAG, "_readCharacteristic : peripheral(%s), _dataService", _peripheral.address());
			_dataService.readCharacteristic(characteristic, value);
		}
	}

	protected void _updatedBatteryLevel(BlePeripheral peripheral) {
		if (peripheral == _peripheral) {
			_batteryLevel = _batteryService.batteryLevel;

			EventManager.sharedInstance().post(kPowerBandDeviceDataUpdatedNotification, this);
		}
	}

	protected void _rssiUpdated(BlePeripheral peripheral) {
		if (peripheral == _peripheral) {
			EventManager.sharedInstance().post(kPowerBandDeviceDataUpdatedNotification, this);
		}
	}

	protected void _readDeviceSerialNumber(BlePeripheral peripheral) {
		if (peripheral == _peripheral) {
			Logger.log(TAG, "_readDevicesSerialNumber : peripheral(%s)", peripheral.address());
			//_startAuthentication();
			//_startKeyValidation();
		}
	}

	/**
	 * @param peripheral
	 * @param descriptor
	 * @param success
	 */

	protected void _descriptorWrite(BlePeripheral peripheral, BluetoothGattDescriptor descriptor, boolean success) {
		if (peripheral != _peripheral || peripheral == null)
			return;

		boolean ret = _dataService.descriptorWrite(descriptor, success);
		if (ret == false) {
			Logger.error(TAG, "_descriptorWrite : peripheral (%s), descriptor write failed", peripheral.address());
			return;
		}

		//Logger.log("_descriptorWrite, peripheral (%s), BAND_CONNECTED, will start syncing", peripheral.address());
		mDeviceState = DEVICE_CONNECTED;

		EventManager.sharedInstance().post(kPowerBandDeviceConnectedNotification, this);
	}

	protected void _notificationFailed(BlePeripheral peripheral) {
		if (peripheral != _peripheral)
			return;

		Logger.error(TAG, "_notificationFailed : peripheral(%s)", peripheral.address());
		mDeviceState = DEVICE_DISCONNECTED;
		_peripheral.disconnect();

		if (DISABLE_ADAPTER_WHEN_FAILED) {
			BleManager.sharedInstance().disableAdapter();
		}
	}

	protected void _writeFailed(BlePeripheral peripheral) {
		if (peripheral != _peripheral)
			return;

		Logger.error(TAG, "_writeFailed : peripheral(%s)", peripheral.address());
		mDeviceState = DEVICE_DISCONNECTED;
		_peripheral.disconnect();

		if (DISABLE_ADAPTER_WHEN_FAILED) {
			BleManager.sharedInstance().disableAdapter();
		}
	}

	public void getUserName(WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "getUserName : failed, _dataService is null");
			return;
		}
		CmdGetUserName command = new CmdGetUserName();
		_dataService.writeCommand(command, callback);
	}

	public void setUserName(String name, WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "setUserName : failed, _dataService is null");
			return;
		}
		CmdSetUserName command = new CmdSetUserName(name);
		_dataService.writeCommand(command, callback);
	}

	public void setPersonalInformation(int gender, int age, int height, int weight, int strideLength, WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "setPersonalInformation : failed, _dataService is null");
			return;
		}
		CmdSetPersonalInformation command = new CmdSetPersonalInformation(gender, age, height, weight, strideLength);
		_dataService.writeCommand(command, callback);
	}

	public void setMessage(int slot, String message, WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "setMessage : failed, _dataService is null");
			return;
		}
		CmdSetMessage command = new CmdSetMessage(slot, message);
		_dataService.writeCommand(command, callback);
	}

	public void getStorageInformation(WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "getStorageInformation : failed, _dataService is null");
			return;
		}
		CmdGetStorageInformation command = new CmdGetStorageInformation();
		_dataService.writeCommand(command, callback);
	}

	public void getDailySummaryData(int days, WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "getDailySummaryData : failed, _dataService is null");
			return;
		}
		CmdGetDailySummaryData command = new CmdGetDailySummaryData(days);
		_dataService.writeCommand(command, callback);
	}

	// daily detailed data
	public void getDailyData(int days, WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "getDailyData : failed, _dataService is null");
			return;
		}
		CmdGetDailyData command = new CmdGetDailyData(days);
		_dataService.writeCommand(command, callback);
	}

	public void setDeviceTime(Date dt, WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "setDeviceTime : failed, _dataService is null");
			return;
		}
		CmdSetDeviceTime command = new CmdSetDeviceTime(dt);
		_dataService.writeCommand(command, callback);
	}

	// for new api

	public void setCalorieMissionGoal(int goal, WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "setCalorieMissionGoal : failed, _dataService is null");
			return;
		}
		CmdSetCalorieMissionGoal command = new CmdSetCalorieMissionGoal(CmdSetCalorieMissionGoal.ACTION_SET, goal);
		_dataService.writeCommand(command, callback);
	}

	public void changeCalorieMissionGoal(int goal, WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "changeCalorieMissionGoal : failed, _dataService is null");
			return;
		}
		CmdSetCalorieMissionGoal command = new CmdSetCalorieMissionGoal(CmdSetCalorieMissionGoal.ACTION_CHANGE, goal);
		_dataService.writeCommand(command, callback);
	}

	public void resetCalorieMissionGoal(WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "resetCalorieMissionGoal : failed, _dataService is null");
			return;
		}
		CmdSetCalorieMissionGoal command = new CmdSetCalorieMissionGoal(CmdSetCalorieMissionGoal.ACTION_TURNOFF, 0);
		_dataService.writeCommand(command, callback);
	}

	public void getCalorieMissionGoal(WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "getCalorieMissionGoal : failed, _dataService is null");
			return;
		}
		CmdGetCalorieMissionGoal command = new CmdGetCalorieMissionGoal();
		if (!command.isSupportedFirmware(fwVersion)) {
			callback.onWrite(false, null);
			Logger.error(TAG, "getCalorieMissionGoal : failed, not supported firmware : " + fwVersion);
			return;
		}
		_dataService.writeCommand(command, callback);
	}

	public void setStaticImage(byte[] image, WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "setStaticImage : failed, _dataService is null");
			return;
		}
		CmdStaticImageSet command = new CmdStaticImageSet(image);
		_dataService.writeCommand(command, callback);
	}

	public void sendAnimatedCelebrationDisplay(WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "sendAnimatedCelebrationDisplay : failed, _dataService is null");
			return;
		}
		CmdAniCelebDisplaySend command = new CmdAniCelebDisplaySend();
		if (!command.isSupportedFirmware(fwVersion)) {
			callback.onWrite(false, null);
			Logger.error(TAG, "animatedCelebrationDisplay : failed, not supported firmware : " + fwVersion);
			return;
		}
		_dataService.writeCommand(command, callback);
	}

	public void getFirmwareVersion(final WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "getFirmwareVersion : failed, _dataService is null");
			return;
		}
		CmdGetFirmwareVersion command = new CmdGetFirmwareVersion();
		_dataService.writeCommand(command, new WriteCommandCallback() {
			@Override
			public void onWrite(boolean success, BandCommandResponse response) {
				if (response instanceof CmdGetFirmwareVersionParser.CmdGetFirmwareVersionRes) {
					CmdGetFirmwareVersionParser.CmdGetFirmwareVersionRes res = (CmdGetFirmwareVersionParser.CmdGetFirmwareVersionRes) response;
					String firmversion = res.getFirmwareVersion();
					setFwVersion(firmversion);
				}

				callback.onWrite(success, response);
			}
		});
	}

	public void getMacAddress(WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "getMacAddress : failed, _dataService is null");
			return;
		}
		CmdGetMacAddress command = new CmdGetMacAddress();
		_dataService.writeCommand(command, callback);
	}

	public void setTotalPowerPoints(int totals, WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "setTotalPowerPoints : failed, _dataService is null");
			return;
		}

		CmdSetTotalPowerPoint command = new CmdSetTotalPowerPoint(totals);
		if (!command.isSupportedFirmware(fwVersion)) {
			callback.onWrite(false, null);
			Logger.error(TAG, "setTotalPowerPoints failed, not supported firmware : " + fwVersion);
			return;
		}
		_dataService.writeCommand(command, callback);
	}

	public void getTotalPowerPoints(WriteCommandCallback callback) {
		if (_dataService == null) {
			callback.onWrite(false, null);
			Logger.error(TAG, "getTotalPowerPoints : failed, _dataService is null");
			return;
		}
		CmdGetTotalPowerPoint command = new CmdGetTotalPowerPoint();
		if (!command.isSupportedFirmware(fwVersion)) {
			callback.onWrite(false, null);
			Logger.error(TAG, "getTotalPowerPoints : failed, not supported firmware : " + fwVersion);
			return;
		}
		_dataService.writeCommand(command, callback);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * WriteCommandCallback class
	 */
	public interface WriteCommandCallback {
		public void onWrite(boolean success, BandCommandResponse response);
	}
}
