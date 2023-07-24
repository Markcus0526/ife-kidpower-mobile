package org.unicefkidpower.schools.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

/**
 * Created by donal_000 on 12/1/2014.
 */
public interface BlePeripheralDelegate {
	void gattConnected(BlePeripheral peripheral);
	void gattDisconnected(BlePeripheral peripheral);
	void gattServicesDiscovered(BlePeripheral peripheral);
	void gattDataAvailable(BlePeripheral peripheral, BluetoothGattCharacteristic characteristic, byte[] value);
	void gattReadRemoteRssi(BlePeripheral peripheral, int rssi);
	void gattDescriptorWrite(BlePeripheral peripheral, BluetoothGattDescriptor descriptor, boolean status);
}
