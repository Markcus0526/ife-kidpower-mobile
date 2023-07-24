package org.unicefkidpower.schools;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import org.unicefkidpower.schools.ble.BleManager;
import org.unicefkidpower.schools.context.UserContext;
import org.unicefkidpower.schools.define.CommonUtils;
import org.unicefkidpower.schools.define.KPConstants;
import org.unicefkidpower.schools.helper.KidsPlaceHelper;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.model.ReportConfiguration;
import org.unicefkidpower.schools.model.UserManager;
import org.unicefkidpower.schools.model.Utils;
import org.unicefkidpower.schools.server.NetworkManager;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.UserService;

import java.util.Date;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class SplashActivity extends BaseActivity {
	private final String TAG						= "SplashActivity";

	private final int LOADINGVIEW_TIMEOUT			= 2000;
	private final int REQUEST_ENABLE_BT				= 1;
	private final int REQUEST_ENABLE_LOCATION		= 2;
	private final int REQUEST_PHONE_STATE 			= 3;

	private final String LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION;
	private final String PHONE_PERMISSION = Manifest.permission.READ_PHONE_STATE;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				runOnUiThread(goNextRunnable);
			}
		}
	};

	private Runnable goNextRunnable = new Runnable() {
		@Override
		public void run() {
			String currentLanguage = UserContext.sharedInstance().getAppLocale().getLanguage();
			if (currentLanguage.equalsIgnoreCase("nl")) {
				goNext();
			} else {
				goNext();
//				if (KidsPlaceHelper.getInstance().isKidsPlaceRunning(SplashActivity.this) || BuildConfig.DEBUG) {
//					goNext();
//				} else {
//					KidsPlaceHelper.getInstance().startKidsPlace(SplashActivity.this);
//				}
			}
		}
	};


	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		Logger.log("", "\n---- New launched : %s ----", new Date().toString());
		FlurryAgent.onStartSession(this, "Splash Activity");

		initEnvironment();
	}


	@Override
	protected void onResume() {
		super.onResume();
	}

	private void reportDeviceInformation() {
		String newDeviceModel = Utils.getDeviceModel();
		String newIMEI = Utils.getIMEINumber(SplashActivity.this);
		String newVersionCode = "" + CommonUtils.getVersionCode(SplashActivity.this);
		String newIMSI = Utils.getIMSINumber(SplashActivity.this);
		String newICCID = Utils.getICCIDNumber(SplashActivity.this);
		String newUDID = Utils.getUDID(SplashActivity.this);
		String newOS = Utils.getOperatingSystem(SplashActivity.this);

		ReportConfiguration reportConfiguration = UserContext.sharedInstance().getLastConfiguration();
		if (!reportConfiguration.deviceModel.equalsIgnoreCase(newDeviceModel)
				|| !reportConfiguration.IMEI.equalsIgnoreCase(newIMEI)
				|| !reportConfiguration.versionCode.equalsIgnoreCase(newVersionCode)
				|| !reportConfiguration.IMSI.equalsIgnoreCase(newIMSI)
				|| !reportConfiguration.ICCID.equalsIgnoreCase(newICCID)
				|| !reportConfiguration.UDID.equalsIgnoreCase(newUDID))
		{
			final ReportConfiguration lastConfig = new ReportConfiguration(newDeviceModel,
					newIMEI,
					newVersionCode,
					newIMSI,
					newICCID,
					newUDID,
					newOS);

			ServerManager.sharedInstance().userDevices(newDeviceModel,
					newIMEI,
					newVersionCode,
					newIMSI,
					newICCID,
					newUDID,
					newOS,
					new RestCallback<UserService.ResUserDevices>() {
						@Override
						public void success(UserService.ResUserDevices resUserDevices, Response response) {
							Logger.log("kSchool", "userDevices Success");
							UserContext.sharedInstance().setLastConfiguration(lastConfig);
						}

						@Override
						public void failure(RetrofitError retrofitError, String message) {
							Logger.error("kSchool", "userDevices Failed");
						}
					});
		} else {
		}

		handler.sendEmptyMessageDelayed(0, LOADINGVIEW_TIMEOUT);
	}

	protected void initEnvironment() {
		NetworkManager.initialize(getApplicationContext());
		UIManager.initialize(getApplicationContext());
		UserManager.initialize(getApplicationContext());
		UserContext.initialize(getApplicationContext());
		UserContext.sharedInstance().setAppLocale(UserContext.sharedInstance().getAppLocale(SplashActivity.this));

		// Initialize version helper
		UpdateVersionHelper.sharedInstance(SplashActivity.this);
	}

	@Override
	protected boolean shouldCheckForUpdate() {
		return false;
	}

	protected void goNext() {
		Intent intent;
		if (UserContext.sharedInstance().isLoggedIn()) {
			intent = new Intent(this, LoginActivity.class);
		} else {
			intent = new Intent(this, WelcomeActivity.class);
		}

		intent.putExtra("from", SplashActivity.class.getName());
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.fade, R.anim.alpha);
	}

	@Override
	protected void onStart() {
		super.onStart();

		FlurryAgent.init(this, KPConstants.FLURRY_API_KEY);
		FlurryAgent.onStartSession(this);

		checkBluetooth();
	}

	@Override
	protected void onStop() {
		FlurryAgent.onEndSession(this);
		super.onStop();
	}

	@Override
	protected boolean isUseEvent() {
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_CANCELED) {
				finish();
				return;
			} else if (resultCode == Activity.RESULT_OK) {
				checkLocationPermission();
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == REQUEST_ENABLE_LOCATION) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				checkLocationPermission();
			} else {
				finish();
			}
		} else if (requestCode == REQUEST_PHONE_STATE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				reportDeviceInformation();
			} else {
				finish();
			}
		}
	}

	private void checkBluetooth() {
		BleManager.initialize(getApplicationContext(), ((KidpowerApplication) getApplication()).getBluetoothCrashResolver());
		if (!BleManager.sharedInstance().isBleSupported()) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}

		if (!BleManager.sharedInstance().isBleAvailable()) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}

		if (!BuildConfig.DEBUG) {
			boolean isBleEnabled;

			isBleEnabled = BleManager.sharedInstance().isBleEnabled();

			if (isBleEnabled) {
				checkLocationPermission();
			} else {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		} else {
			checkLocationPermission();
		}
	}

	private void checkLocationPermission() {
		if (ContextCompat.checkSelfPermission(SplashActivity.this, LOCATION_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(SplashActivity.this, new String[]{LOCATION_PERMISSION}, REQUEST_ENABLE_LOCATION);
		} else {
			checkPhoneStatePermission();
		}
	}

	private void checkPhoneStatePermission() {
		if (ContextCompat.checkSelfPermission(SplashActivity.this, PHONE_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(SplashActivity.this, new String[]{PHONE_PERMISSION}, REQUEST_PHONE_STATE);
		} else {
			reportDeviceInformation();
		}
	}

}
