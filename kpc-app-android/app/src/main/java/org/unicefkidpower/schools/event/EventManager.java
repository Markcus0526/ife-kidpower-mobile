package org.unicefkidpower.schools.event;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.unicefkidpower.schools.KidpowerApplication;


/**
 * Created by donal_000 on 1/12/2015.
 */
public class EventManager {
	public static EventManager		sharedInstance = new EventManager(KidpowerApplication.getAppContext());
	protected Context				contextInstance;

	private EventManager(Context context) {
		contextInstance = context;
	}


	public static EventManager sharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = new EventManager(KidpowerApplication.getAppContext());
		}

		return sharedInstance;
	}

	public static boolean isEvent(SEvent e, String name) {
		if (name.equals(e.name))
			return true;
		return false;
	}

	public void post(String name) {
		EventBus.getDefault().post(new SEvent(name));
	}

	public void post(String name, Object object) {
		EventBus.getDefault().post(new SEvent(name, object));
	}

	public void post(String name, Object object, String msg) {
		EventBus.getDefault().post(new SEvent(name, object, msg));
	}

	public void register(Object objSubscriber) {
		EventBus.getDefault().register(objSubscriber);
	}

	public void unregister(Object objSubscriber) {
		EventBus.getDefault().unregister(objSubscriber);
	}
}
