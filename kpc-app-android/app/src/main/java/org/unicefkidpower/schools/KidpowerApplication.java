package org.unicefkidpower.schools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.kiddoware.kidspower.KidsLauncher;
import com.radiusnetworks.bluetooth.BluetoothCrashResolver;
import com.swrve.sdk.SwrveSDK;
import com.swrve.sdk.config.SwrveConfig;
import com.swrve.sdk.config.SwrveStack;

import org.unicefkidpower.schools.adapter.ActivityLifecycleAdapter;
import org.unicefkidpower.schools.define.KPConstants;
import org.unicefkidpower.schools.define.config;
import org.unicefkidpower.schools.model.CityManager;
import org.unicefkidpower.schools.model.GroupManager;
import org.unicefkidpower.schools.model.StudentManager;
import org.unicefkidpower.schools.model.TeamManager;
import org.unicefkidpower.schools.model.UserManager;

import io.fabric.sdk.android.Fabric;

/**
 * Created by donal_000 on 2/9/2015.
 */
public class KidpowerApplication extends Application {
	private static Context				context;
	private static Activity				currentActivity = null;

	public static AlertDialog			currentDialog = null;
	public static CityManager			insCityManager;
	public static GroupManager			insGroupManager = null;
	public static StudentManager		insStudentManager = null;
	public static TeamManager			insTeamManager = null;
	public static UserManager			insUserManager = null;
	private BluetoothCrashResolver		bluetoothCrashResolver = null;


	public static Context getAppContext() {
		return KidpowerApplication.context;
	}

	public static CityManager sharedCityManagerInstance() {
		if (insCityManager == null)
			insCityManager = new CityManager();
		return insCityManager;
	}

	public static GroupManager sharedGroupManagerInstance() {
		if (insGroupManager == null)
			insGroupManager = new GroupManager();
		return insGroupManager;
	}

	public static StudentManager sharedStudentManagerInstance() {
		if (insStudentManager == null)
			insStudentManager = new StudentManager();
		return insStudentManager;
	}

	public static TeamManager sharedTeamManagerInstance() {
		insTeamManager = new TeamManager();
		return insTeamManager;
	}

	public static UserManager initUserManager(Context context) {
		if (insUserManager == null)
			insUserManager = new UserManager(context);
		return insUserManager;
	}

	public static UserManager sharedUserManagerInstance() {
		return insUserManager;
	}

	public static Activity getCurrentActivity() {
		return currentActivity;
	}

	public static void setCurrentActivity(Activity activity) {
		currentActivity = activity;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		KidpowerApplication.context = getApplicationContext();

		Fabric.with(this, new Crashlytics());
		FlurryAgent.setLogEnabled(false);
		FlurryAgent.setLogLevel(Log.VERBOSE);
		FlurryAgent.init(this, KPConstants.FLURRY_API_KEY);

		// Create an sharedInstance of the SDK
		try {
			SwrveConfig cfg = new SwrveConfig();
			cfg.setSelectedStack(SwrveStack.EU);
			SwrveSDK.createInstance(this, config.SWRVE_APP_ID, config.SWRVE_API_KEY, cfg);
		} catch (IllegalArgumentException exp) {
			Log.e("Swrve", "Could not initialize the Swrve SDK", exp);
		}

		bluetoothCrashResolver = new BluetoothCrashResolver(this);
		bluetoothCrashResolver.start();

		// Make application portrait only.
		registerActivityLifecycleCallbacks(new ActivityLifecycleAdapter() {
			@Override
			public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		});
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	public BluetoothCrashResolver getBluetoothCrashResolver() {
		return bluetoothCrashResolver;
	}

	@Override
	protected void attachBaseContext(Context context) {
		super.attachBaseContext(context);
		MultiDex.install(this);
	}
}
