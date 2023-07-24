package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.service;

import android.bluetooth.BluetoothGattService;

import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble.BlePeripheral;

/**
 * Created by donal_000 on 12/24/2014.
 */
public abstract class BleService {
	protected BluetoothGattService _service;
	protected BlePeripheral _peripheral;

	public BlePeripheral peripheral() {
		return _peripheral;
	}

	public BluetoothGattService service() {
		return _service;
	}

	public abstract void setService(BlePeripheral peripheral, BluetoothGattService service);
}
