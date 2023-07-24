package org.unicefkidpower.schools.powerband.service;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import org.greenrobot.eventbus.EventBus;
import org.unicefkidpower.schools.ble.BleManager;
import org.unicefkidpower.schools.ble.BlePeripheral;
import org.unicefkidpower.schools.event.SEvent;
import org.unicefkidpower.schools.helper.Logger;

import java.util.Iterator;
import java.util.UUID;


/**
 * Created by donal_000 on 12/24/2014.
 */
public class BatteryService extends BleService {
	public static final String TAG = "BatteryService";

	public static final String kBatteryServiceReadBatteryLevel = "battery service read battery level";
	public int batteryLevel;

	public static UUID batteryServiceID() {
		return UUID.fromString(BleManager.getLongUuidFromShortUuid("180f"));
	}

	public static UUID batteryLevelID() {
		return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A19"));
	}

	@Override
	public void setService(BlePeripheral peripheral, BluetoothGattService service) {
		this._peripheral = peripheral;
		this._service = service;

		// read whole characteristics
		Iterator ci = service.getCharacteristics().iterator();
		while (ci.hasNext()) {
			BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) ci.next();
			peripheral.readCharacteristic(ch);
		}
	}

	public void readCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
		if (characteristic.getService() == _service) {
			Logger.log(TAG, "readCharacteristic, batteryService");
			if (characteristic.getUuid().equals(BatteryService.batteryLevelID())) {
				Logger.log(TAG, "readCharacteristic, battery_level characteristic");
				if (value.length >= 1) {
					batteryLevel = (int) value[0];
					Logger.log(TAG, "readCharacteristic, battery_level characteristic - %d", batteryLevel);
					EventBus.getDefault().post(new SEvent(kBatteryServiceReadBatteryLevel, _peripheral));
				}
			}
		}
	}
}
