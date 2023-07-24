package org.unicefkidpower.schools.server;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.schools.event.SEvent;
import org.unicefkidpower.schools.helper.Logger;


/**
 * Created by donal_000 on 1/8/2015.
 */
public class NetworkManager {
	public static final String NM_CONNECTIVITY_CHANGED = "network manager connectivity changed";

	private static NetworkManager _instance;
	protected Context mContext;
	protected boolean _isConnected;

	private NetworkManager() {
		//
	}

	private NetworkManager(Context context) {
		mContext = context;
		_isConnected = false;

		checkConnectivity();
		EventBus.getDefault().register(this);
	}

	public static NetworkManager initialize(Context context) {
		if (_instance == null)
			_instance = new NetworkManager(context);
		return _instance;
	}

	public static NetworkManager sharedInstance() {
		return _instance;
	}

	private void checkConnectivity() {
		if (mContext != null) {
			ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo[] info = connectivity.getAllNetworkInfo();
				if (info != null) {
					for (int i = 0; i < info.length; i++)
						if (info[i].getState() == NetworkInfo.State.CONNECTED ||
								info[i].getState() == NetworkInfo.State.CONNECTING) {
							_isConnected = true;
							break;
						}
				}
			} else {
				_isConnected = false;
			}
		} else {
			_isConnected = true;
		}
	}

	public boolean isConnected() {
		if (!_isConnected) {
			Logger.log("Network", "Network is not connected.");
		}

		return _isConnected;
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onEvent(SEvent e) {
		if (SEvent.EVENT_NETWORK_STATE_CHANGED.equals(e.name)) {
			Boolean connected = (Boolean) e.object;
			_isConnected = connected;
			EventBus.getDefault().post(new SEvent(NM_CONNECTIVITY_CHANGED));
		}
	}
}
