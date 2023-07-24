package org.unicefkidpower.schools.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.greenrobot.eventbus.EventBus;
import org.unicefkidpower.schools.event.SEvent;
import org.unicefkidpower.schools.helper.Logger;


/**
 * Created by donal_000 on 1/8/2015.
 */
public class NetworkListener extends BroadcastReceiver {
	private static final String TAG = "NetworkListener";

	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		Boolean isConnected = false;
		if (null != activeNetwork) {
			// wifi network
			if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
				Logger.log(TAG, "active network = wifi");
				isConnected = activeNetwork.isConnected();
			}
			// mobile data
			else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
				Logger.log(TAG, "active network = mobile");
				isConnected = activeNetwork.isConnected();
			} else {
				Logger.log(TAG, "active network != wifi && active network != mobile");
				isConnected = false;
			}
		} else {
			Logger.log(TAG, "there is no active network!");
			isConnected = false;
		}

		EventBus.getDefault().post(new SEvent(SEvent.EVENT_NETWORK_STATE_CHANGED, isConnected));
	}
}
