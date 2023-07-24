package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import org.unicefkidpower.kid_power.BuildConfig;
import org.unicefkidpower.kid_power.Misc.AlertDialogHelper;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.EventManager;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.Model.Structure.KPHFacebook;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHVersion;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.client.Response;

/**
 * Created by Ruifeng Shi on 8/24/2015.
 */

public class SplashActivity extends SuperActivity {
	public static final String			EXTRA_IS_RESTART_APP	= "extra_is_restart_app";
	private final int					DELAY_FOR_RESTART		= 2 * 1000;

	// for checking license
	private final int					SPLASH_TIMEOUT			= 500;


	private Handler						nextHandler				= null;
	private String						username				= "";
	private String						password				= "";


	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					"org.unicefkidpower.kph",
					PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (PackageManager.NameNotFoundException ex) {
			ex.printStackTrace();
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}

		Logger.log("", "\n---- New launched : %s ----", new Date().toString());

		boolean isRestartApp = getIntent().getBooleanExtra(EXTRA_IS_RESTART_APP, false);
		if (isRestartApp) {
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					gotoMainScreen();
				}
			};

			Timer timer = new Timer();
			timer.schedule(task, DELAY_FOR_RESTART);		// Note : need delay for about 2 seconds
		} else {
			initEnvironment();
			nextHandler.sendEmptyMessageDelayed(0, SPLASH_TIMEOUT);
		}
	}


	private String correctVersionContent(String orgContents) {
		String result = orgContents;

		int index = result.toLowerCase().indexOf("what's new".toLowerCase());
		if (index > 0) {
			if (result.charAt(index - 1) != ' ') {
				String part1 = result.substring(0, index);
				String part2 = result.substring(index);

				result = part1 + "  " + part2;
			}
		}

		return result;
	}


	protected void checkVersion() {
		KPHUserService.sharedInstance().getVersion(new RestCallback<KPHVersion>() {
			@Override
			public void failure(RestError restError) {
				loginIfAvailable();
			}

			@Override
			public void success(KPHVersion kphVersion, Response response) {
				int current_version = BuildConfig.VERSION_CODE;
				if (current_version >= kphVersion.version) {
					loginIfAvailable();
					return;
				}

				showQuestionDialog(getString(R.string.update_available), correctVersionContent(kphVersion.content),
						getString(R.string.ok), getString(R.string.ignore), new AlertDialogHelper.AlertListener() {
							@Override
							public void onPositive() {
								Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(KPHConstants.APP_STORE_URL));
								startActivity(browse);
								System.exit(0);
							}

							@Override
							public void onNegative() {
								loginIfAvailable();
							}

							@Override
							public void onCancelled() {
								loginIfAvailable();
							}
						});
			}
		});
	}

	protected void loginIfAvailable() {
		if (username.length() > 0) {
			if (password.length() > 0) {
				KPHUserService.sharedInstance().signin(username, password, new RestCallback<KPHUserData>() {
					@Override
					public void failure(RestError restError) {
						gotoWelcomeScreen();
					}

					@Override
					public void success(KPHUserData userData, Response response) {
						RestService.setUserToken(userData.getAccessToken());
						KPHUserService.sharedInstance().shouldReloadUserData();
						KPHUserService.sharedInstance().setChildRestrictedFlag(false);
						KPHUserService.sharedInstance().saveSignInFlag();
						KPHUserService.sharedInstance().saveLoginData(userData.getHandle(), password);
						KPHUserService.sharedInstance().saveUserData(userData);

						gotoMainScreen();
					}
				});
			} else {
				final String fbId = KPHUserService.sharedInstance().loadFBId();
				final String email = KPHUserService.sharedInstance().loadEmail();
				final String accessToken = KPHUserService.sharedInstance().loadAccessToken();

				KPHFacebook facebookField = new KPHFacebook();
				facebookField.handle = username;
				facebookField.facebook.fbId = fbId;
				facebookField.facebook.email = email;
				facebookField.facebook.accessToken = accessToken;

				KPHUserService.sharedInstance().signinFBAccount(facebookField, new onActionListener() {
					@Override
					public void completed(Object object) {
						KPHUserData userData = (KPHUserData)object;

						RestService.setUserToken(userData.getAccessToken());
						KPHUserService.sharedInstance().shouldReloadUserData();
						KPHUserService.sharedInstance().setChildRestrictedFlag(false);
						KPHUserService.sharedInstance().saveSignInFlag();
						KPHUserService.sharedInstance().saveLoginData(userData.getHandle(), "");
						KPHUserService.sharedInstance().saveUserData(userData);
						KPHUserService.sharedInstance().saveFBData(fbId, email, accessToken);

						gotoMainScreen();
					}

					@Override
					public void failed(int code, String message) {
						gotoWelcomeScreen();
					}
				});
			}
		} else {
			gotoWelcomeScreen();
		}
	}

	private void gotoMainScreen() {
		pushNewActivityAnimated(MainActivity.class, AnimConst.ANIMDIR_NONE);
		popOverCurActivityNonAnimated();
	}

	private void gotoWelcomeScreen() {
		pushNewActivityAnimated(WelcomeActivity.class, AnimConst.ANIMDIR_NONE);
		popOverCurActivityNonAnimated();
	}

	protected void initEnvironment() {
		UIManager.initialize(getApplicationContext());
		KPHUtils.initialize(getApplicationContext());

		EventManager.initalize(getApplicationContext());

		username = KPHUserService.sharedInstance().loadUsername();
		password = KPHUserService.sharedInstance().loadUserPassword();

		if (username == null || username.length() == 0) {
			KPHUserService.sharedInstance().clearUserData();
		}

		nextHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 0) {
					checkVersion();
				}
			}
		};
	}

}
