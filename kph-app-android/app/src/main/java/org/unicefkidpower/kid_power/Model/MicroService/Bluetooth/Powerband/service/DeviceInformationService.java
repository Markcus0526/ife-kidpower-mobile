package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.service;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble.BleManager;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble.BlePeripheral;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.EventManager;
import org.unicefkidpower.kid_power.Misc.Logger;

import java.util.UUID;

/**
 * Created by donal_000 on 12/24/2014.
 */
public class DeviceInformationService extends BleService {
	private static final String TAG = "DeviceInformationService";

	public static final String kDeviceInformationReadSerialNumber = "device information read serial number";

	public static UUID deviceInformationServiceID() {
		return UUID.fromString(BleManager.getLongUuidFromShortUuid("180a"));
	}

	public static UUID manufacturerNameID() {
		return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A29"));
	}

	public static UUID modelNumberID() {
		return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A24"));
	}

	public static UUID serialNumberID() {
		return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A25"));
	}

	public static UUID hardwareRevisionID() {
		return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A27"));
	}

	public static UUID firmwareRevisionID() {
		return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A26"));
	}

	public static UUID softwareRevisionID() {
		return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A28"));
	}

	public static UUID systemIDID() {
		return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A23"));
	}

	public String manufacturerName;

	public String modelNumber;

	public String serialNumber;

	public String hardwareRevision;

	public String firmwareRevision;

	public String softwareRevision;

	@Override
	public void setService(BlePeripheral peripheral, BluetoothGattService service) {
		this._service = service;
		this._peripheral = peripheral;

		// read whole characteristics
			/*
            Iterator ci = service.getCharacteristics().iterator();
            while(ci.hasNext()) {
                BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic)ci.next();
                peripheral.readCharacteristic(ch);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            */
	}

	public void readCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
		if (characteristic.getUuid().equals(serialNumberID())) {
			Logger.log(TAG, "read serial number --------------- ");
			try {
				String key = new String(value, "utf-8");
				Logger.log(TAG, "read serial number : %s", key);

				String remains = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
				String cleanedkey = "";
				for (int i = 0; i < key.length(); i++) {
					if (remains.contains(key.charAt(i) + ""))
						cleanedkey = cleanedkey + key.charAt(i);
				}

				serialNumber = cleanedkey;
				serialNumber = serialNumber.toUpperCase();
				Logger.log(TAG, "read serial number : %s", serialNumber);

				_peripheral.setSerialNo(serialNumber);


				// start validation - it is called in authenticationCompleteForiDevicesService on iOS,
				// android version have no authenticating process!
				// _startKeyValidation();

				EventManager.sharedInstance().post(kDeviceInformationReadSerialNumber, _peripheral);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
