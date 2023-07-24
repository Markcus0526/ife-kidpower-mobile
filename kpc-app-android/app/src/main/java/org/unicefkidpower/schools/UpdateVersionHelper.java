package org.unicefkidpower.schools;

import android.app.Activity;

import com.winsontan520.wversionmanager.library.WVersionManager;

import org.unicefkidpower.schools.define.KPConstants;
import org.unicefkidpower.schools.server.apimanage.APIManager;

/**
 * Created by Dayong Li on 1/1/2016.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud Org
 * Dayong@CalorieCloud.Org
 */
public class UpdateVersionHelper {
	protected static UpdateVersionHelper		_instance;
	protected Activity							currentActivity;
	protected WVersionManager					versionManager;


	public static UpdateVersionHelper initialize(Activity activity) {
		if (_instance == null)
			_instance = new UpdateVersionHelper(activity);
		return _instance;

	}

	public static UpdateVersionHelper sharedInstance(Activity activity) {
		if (_instance == null)
			initialize(activity);

		_instance.setActivity(activity);

		return _instance;
	}

	public UpdateVersionHelper(Activity activity) {
		currentActivity = activity;

		versionManager = new WVersionManager(activity);
		versionManager.setVersionContentUrl(APIManager.provideHostUrl() + APIManager.VERSION_CHECK); // your update content url, see the response format below
		versionManager.setReminderTimer(KPConstants.CHECK_UPDATEVERSION_INTERVAL);
	}

	public void setActivity(Activity activity) {
		if (currentActivity != activity)
			versionManager.setActivity(activity);
	}

	public void doCheck() {
		versionManager.setVersionContentUrl(APIManager.provideHostUrl() + APIManager.VERSION_CHECK); // your update content url, see the response format below
		versionManager.setReminderTimer(KPConstants.CHECK_UPDATEVERSION_INTERVAL);
		versionManager.checkVersion();
	}
}
