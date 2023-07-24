package org.unicefkidpower.schools.ble;

import android.bluetooth.BluetoothDevice;

public interface BleScannerListener {
	void deviceScanned(BluetoothDevice device, int rssi, byte[] scanRecord);
	boolean shouldCheckDevice(BluetoothDevice device, int rssi, byte[] scanRecord);
}
