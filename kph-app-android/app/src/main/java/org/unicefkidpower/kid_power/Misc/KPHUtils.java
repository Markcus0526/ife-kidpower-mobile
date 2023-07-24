package org.unicefkidpower.kid_power.Misc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.unicefkidpower.kid_power.Application.KPHApplication;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Dayong Li on 11/11/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class KPHUtils {
	private static KPHUtils			instance = null;
	protected Context				context = null;

	public static KPHUtils initialize(Context context) {
		if (instance == null)
			instance = new KPHUtils(context);
		return instance;
	}

	public static KPHUtils sharedInstance() {
		if (instance == null)
			instance = new KPHUtils(KPHApplication.sharedInstance().getApplicationContext());
		return instance;
	}


	private KPHUtils(Context context) {
		this.context = context;
	}


	public Context getApplicationContext() {
		return this.context.getApplicationContext();
	}

	public boolean isOnline() {
		try {
			Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
			return (p1.waitFor() == 0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	public boolean isUsernameValid(String username) {
		boolean isValid = false;

		String expression = "[^a-z A-Z]";

		Pattern pattern = Pattern.compile(expression);
		isValid = !pattern.matcher(username).find();
		return isValid;
	}

	public boolean isEmailValid(String email) {
		boolean isValid = false;

		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";

		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(email);
		if (matcher.matches()) {
			isValid = true;
		}
		return isValid;
	}

	public boolean isPasswordValid(String password) {
		return password.length() >= KPHConstants.PASSWORD_MIN_LENGTH;
	}

	public String getAndroidVersion() {
		try {
			return Build.VERSION.RELEASE;
		} catch (Exception e) {
			e.printStackTrace();

			return "Unknown version";
		}
	}

	public String getVersionName(Context context) {
		PackageInfo pInfo;
		try {
			pInfo = context.getApplicationContext().getPackageManager().getPackageInfo(context.getApplicationContext().getPackageName(), 0);
			return pInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}

	public String getVersionCode(Context context) {
		PackageInfo pInfo;
		try {
			pInfo = context.getApplicationContext().getPackageManager().getPackageInfo(context.getApplicationContext().getPackageName(), 0);
			return String.valueOf(pInfo.versionCode);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}


	public String getDeviceIdentifier() {
		if (this.context == null)
			return null;

		String android_id = Settings.Secure.getString(this.context.getContentResolver(), Settings.Secure.ANDROID_ID);
		return android_id;

/*
		final TelephonyManager tm = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);

		final String tmDevice, tmSerial, androidId;
		tmDevice = "" + tm.getDeviceId();
		tmSerial = "" + tm.getSimSerialNumber();
		androidId = "" + android.provider.Settings.Secure.getString(this.context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

		UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
		return deviceUuid.toString();
*/
	}


	public String getIMEI() {
		TelephonyManager tm = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);
		String result = tm.getDeviceId();
		if (result == null)
			result = "";

		return result;
	}


	public String getICCID() {
		TelephonyManager tm = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);
		String result = tm.getSimSerialNumber();
		if (result == null)
			result = "";

		return result;
	}

	public String getIMSI() {
		return "";
	}

	public String getPushID() {
		return "";
	}

	public void hideKeyboardInView(View view) {
		if (view == null)
			return;

		if (view instanceof EditText) {
			view.clearFocus();
			InputMethodManager imm = (InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		} else  if (view instanceof ViewGroup) {
			int childCount = ((ViewGroup) view).getChildCount();
			for (int i = 0; i < childCount; i++) {
				View childView = ((ViewGroup) view).getChildAt(i);
				hideKeyboardInView(childView);
			}
		}
	}

	public int getUTCOffset() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
				Locale.getDefault());
		Date currentLocalTime = calendar.getTime();
		DateFormat date = new SimpleDateFormat("Z");
		String localTime = date.format(currentLocalTime);
		int hour = Integer.valueOf(localTime.substring(1, 3));
		int min = Integer.valueOf(localTime.substring(3, 5));
		int sign = localTime.charAt(0) == '+' ? (-1) : (1);
		int offsetUTC = (hour * 60 + min) * sign;

		return offsetUTC;
	}


	public String getNonNullMessage(RestError restError) {
		String message = null;
		if (restError != null)
			message = restError.getMessage();

		if (message == null || message.length() == 0)
			message = KPHApplication.sharedInstance().getApplicationContext().getString(R.string.default_error);

		return message;
	}


	public boolean checkGPSProviderEnabled(final SuperActivity activity, boolean forceEnable) {
		LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			if (forceEnable) {
				AlertDialogHelper.showConfirmDialog(
						activity.getString(R.string.gps_disabled),
						activity.getString(R.string.enable_gps_to_search),
						activity,
						new AlertDialogHelper.AlertListener() {
							@Override
							public void onPositive() {
								activity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
							}

							@Override
							public void onNegative() {
							}

							@Override
							public void onCancelled() {
							}
						}
				);
			}

			return false;
		}

		return true;
	}

}
