package org.unicefkidpower.kid_power.Application;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.appsflyer.AppsFlyerLib;
import com.crashlytics.android.Crashlytics;
import com.radiusnetworks.bluetooth.BluetoothCrashResolver;

import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Model.MicroService.KPHAnalyticsService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHMissionService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Dayong Li on 11/11/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class KPHApplication extends MultiDexApplication {
	private static final		String 							TAG = "KPHApplication";
	private						BluetoothCrashResolver			bluetoothCrashResolver = null;
	public static				KPHApplication					instance = null;


	public static KPHApplication sharedInstance() {
		if (instance == null) {
			instance = new KPHApplication();
		}

		return instance;
	}


	@Override
	public void onCreate() {
		super.onCreate();

		instance = KPHApplication.this;


		Fabric.with(this, new Crashlytics());

		KPHAnalyticsService.sharedInstance().initializeSwrveAnalytics(this);
		KPHMissionService.sharedInstance().loadData(getApplicationContext());

		// Initialize AppsFlyer
		AppsFlyerLib.getInstance().setCollectAndroidID(false);
		AppsFlyerLib.getInstance().setCollectIMEI(false);
		AppsFlyerLib.getInstance().startTracking(KPHApplication.this, KPHConstants.APPSFLYER_DEV_KEY);
		// End of 'Initialize AppsFlyer'


		bluetoothCrashResolver = new BluetoothCrashResolver(this);
		bluetoothCrashResolver.start();


		// Setup handler for uncaught exceptions.
		Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException(Thread thread, Throwable e)
			{
				handleUncaughtException(thread, e);
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

	public void handleUncaughtException (Thread thread, Throwable e) {
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);

		// not all Android versions will print the stack trace automatically
		e.printStackTrace(printWriter);
		String s = writer.toString();

		Logger.error(TAG, s);

		System.exit(1); // kill off the crashed app
	}

	@Override
	protected void attachBaseContext(Context context) {
		super.attachBaseContext(context);
		MultiDex.install(this);
	}
}
