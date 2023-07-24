package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble;

import android.bluetooth.BluetoothDevice;

public interface BleScannerListener {
	public void deviceScanned(BluetoothDevice device, int rssi, byte[] scanRecord);
	public boolean shouldCheckDevice(BluetoothDevice device, int rssi, byte[] scanRecord);
}
