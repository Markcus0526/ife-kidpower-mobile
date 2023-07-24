package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.EventManager;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.SEvent;


/**
 * Created by donal_000 on 12/1/2014.
 */
public class BluetoothAdapterListener extends BroadcastReceiver {
	private static final String TAG = "BluetoothAdapterListener";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
			final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
			switch (state) {
				case BluetoothAdapter.STATE_OFF:
					//Logger.log(TAG, "BluetoothAdapter.STATE_OFF");
					break;
				case BluetoothAdapter.STATE_ON:
					//Logger.log(TAG, "BluetoothAdapter.STATE_ON");
					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
					//Logger.log(TAG, "BluetoothAdapter.STATE_TURNING_OFF");
					break;
				case BluetoothAdapter.STATE_TURNING_ON:
					//Logger.log(TAG, "BluetoothAdapter.STATE_TURNING_ON");
					break;
				default:
					break;
			}

			Integer intstate = state;
			EventManager.sharedInstance().post(SEvent.EVENT_BLUETOOTH_STATE_CHANGED, intstate);
		}
	}
}
