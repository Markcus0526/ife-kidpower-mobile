package org.unicefkidpower.kid_power.Model.MicroService;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.jaredrummler.android.device.DeviceName;

import org.json.JSONObject;
import org.unicefkidpower.kid_power.Application.KPHApplication;
import org.unicefkidpower.kid_power.BuildConfig;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.OSDate;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.Model.Structure.KPHCheerInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHCreditReceipt;
import org.unicefkidpower.kid_power.Model.Structure.KPHCreditVerify;
import org.unicefkidpower.kid_power.Model.Structure.KPHDeLinkTrackerResponse;
import org.unicefkidpower.kid_power.Model.Structure.KPHDeviceInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHFacebook;
import org.unicefkidpower.kid_power.Model.Structure.KPHTracker;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserSummary;
import org.unicefkidpower.kid_power.Model.Structure.KPHVersion;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import retrofit.client.Response;

/**
 * Created by Dayong Li on 9/23/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class KPHUserService {
	// Singleton instance
	protected static KPHUserService		sharedInstance				= null;
	private static String				TAG							= "KPHUserService";

	// Constants
	public static final int				USER_MINIMUM_AGE			= 13;
	public static final int				USER_MINIMUM_ADULT_AGE		= 18;

	public static final int				TRACKER_TYPE_NONE			= 0;
	public static final int				TRACKER_TYPE_KIDPOWERBAND	= 1;
	public static final int				TRACKER_TYPE_GOOGLEFIT		= 2;
	public static final int				TRACKER_TYPE_HEALTHKIT		= 3;


	// Private members
	protected boolean			enabledPurchases					= false;
	protected KPHUserData		userData							= null;

	protected KPHTracker		currentTracker						= null;
	protected KPHTracker		tempTracker							= null;


	public static KPHUserService sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new KPHUserService();

		return sharedInstance;
	}


	public KPHUserService() {
	}

	public void saveFBData(String fbId, String email, String accessToken) {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(KPHConstants.PREF_AUTH_FBID, fbId);
		editor.putString(KPHConstants.PREF_AUTH_EMAIL, email);
		editor.putString(KPHConstants.PREF_AUTH_ACCESSTOKEN, accessToken);

		editor.apply();
	}


	public void clearFBData() {
		saveFBData("", "", "");
	}

	public String loadFBId() {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);
		return sharedPreferences.getString(KPHConstants.PREF_AUTH_FBID, "");
	}

	public String loadEmail() {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);
		return sharedPreferences.getString(KPHConstants.PREF_AUTH_EMAIL, "");
	}

	public String loadAccessToken() {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);
		return sharedPreferences.getString(KPHConstants.PREF_AUTH_ACCESSTOKEN, "");
	}


	/**
	 * Saves login info to the app context
	 *
	 * @param username : Username
	 * @param password : Password
	 */
	public void saveLoginData(String username, String password) {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(KPHConstants.PREF_AUTH_USERNAME, username);
		editor.putString(KPHConstants.PREF_AUTH_PASSWORD, password);

		editor.apply();
	}


	public void clearLoginData() {
		saveLoginData("", "");
		clearFBData();
	}


	public void saveSignInFlag() {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(KPHConstants.PREF_SIGNED_IN, true);
		editor.apply();
	}


	public boolean loadSignInFlag() {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);
		return sharedPreferences.getBoolean(KPHConstants.PREF_SIGNED_IN, false);
	}


	/**
	 * Gets the user username saved in the app context
	 *
	 * @return Returns the user username saved in the app context
	 */
	public String loadUsername() {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);
		return sharedPreferences.getString(KPHConstants.PREF_AUTH_USERNAME, "");
	}


	/**
	 * Gets the user password saved in the app context
	 *
	 * @return Returns the user password saved in the app context
	 */
	public String loadUserPassword() {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);
		return sharedPreferences.getString(KPHConstants.PREF_AUTH_PASSWORD, "");
	}


	public void saveCatchTrackerDialogDate(Date date) {
		int year, month, day;

		Calendar calendar = Calendar.getInstance();
		if (date != null) {
			calendar.setTime(date);
			year = calendar.get(Calendar.YEAR);
			month = calendar.get(Calendar.MONTH);
			day = calendar.get(Calendar.DAY_OF_MONTH);
		} else {
			// This is for clear
			year = month = day = 0;
		}


		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(KPHConstants.PREF_CATCH_TRACKER_DIALOG_YEAR, year);
		editor.putInt(KPHConstants.PREF_CATCH_TRACKER_DIALOG_MONTH, month);
		editor.putInt(KPHConstants.PREF_CATCH_TRACKER_DIALOG_DAY, day);
		editor.apply();
	}


	/**
	 * Method to save the last timestamp to link/sync GF tracker
	 */
	public void saveGFTime() {
		long currentTime = Calendar.getInstance().getTimeInMillis();
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putLong(KPHConstants.PREF_LAST_LINKED_TIME, currentTime);
		editor.apply();
	}


	/**
	 * Method to get the last timestamp to link/sync GF tracker
	 * @return Last timestamp
	 * @see  {java.util.Calendar.getInstance().getTimeInMillis()}
	 */
	public long loadGFTime() {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);
		return sharedPreferences.getLong(KPHConstants.PREF_LAST_LINKED_TIME, 0);
	}


	private final long CATCH_TRACKER_DIALOG_INTERVAL = 14 * 24 * 60 * 60 * 1000;            // 14 days(2 weeks)
	public boolean catchTrackerDialogIntervalExpired() {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);

		int year = sharedPreferences.getInt(KPHConstants.PREF_CATCH_TRACKER_DIALOG_YEAR, 0);
		int month = sharedPreferences.getInt(KPHConstants.PREF_CATCH_TRACKER_DIALOG_MONTH, 0);
		int day = sharedPreferences.getInt(KPHConstants.PREF_CATCH_TRACKER_DIALOG_DAY, 0);

		if (year == 0)
			return true;

		Calendar lastShownCalendar = Calendar.getInstance();
		lastShownCalendar.set(Calendar.YEAR, year);
		lastShownCalendar.set(Calendar.MONTH, month);
		lastShownCalendar.set(Calendar.DAY_OF_MONTH, day);

		Calendar todayCalendar = Calendar.getInstance();

		long millisDiff = todayCalendar.getTimeInMillis() - lastShownCalendar.getTimeInMillis();

		return (millisDiff > CATCH_TRACKER_DIALOG_INTERVAL);
	}

	public void clearCatchTrackerDialogDate() {
		saveCatchTrackerDialogDate(null);
	}

	/**
	 * Saves user data to the app context
	 *
	 * @param userData User Data
	 */
	public void saveUserData(KPHUserData userData) {
		this.userData = userData;

		KPHAnalyticsService.sharedInstance().updateUserData(userData);

		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		Gson gson = new GsonBuilder().setDateFormat("MM-dd-yyyy hh:mm:ss").create();
		String json = gson.toJson(userData);
		editor.putString(KPHConstants.PREF_USER_INFO, json);

		editor.apply();

		// This is for fabric user identifier
		logUser(userData);
	}

	/**
	 * Loads user data from app context
	 *
	 * @return Saved user data
	 */
	private KPHUserData loadUserData() {
		Logger.log(TAG, "loadUserData : Started load user data");

		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);

		Gson gson = new GsonBuilder().setDateFormat("MM-dd-yyyy hh:mm:ss").create();
		String json = sharedPreferences.getString(KPHConstants.PREF_USER_INFO, "");

		try {
			userData = gson.fromJson(json, KPHUserData.class);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		}

		Logger.log(TAG, "loadUserData : Finished load user data");

		return userData;
	}


	public KPHUserData getUserData() {
		if (userData == null)
			loadUserData();

		return userData;
	}


	public void clearUserData() {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.remove(KPHConstants.PREF_USER_INFO);
		editor.apply();

		userData = null;
	}


	public void shouldReloadUserData() {
		KPHMissionService service = KPHMissionService.sharedInstance();
		if (service != null) {
			service.clearUsersInformation();
		}
	}


	public Long seenTutorial() {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);
		return sharedPreferences.getLong(KPHConstants.PREF_SEEN_TUTORIAL, 0);
	}


	public void setSawTutorial(Long timeStamp) {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putLong(KPHConstants.PREF_SEEN_TUTORIAL, timeStamp);

		editor.apply();
	}

	/**
	 * Fetch user data from server
	 *
	 * @param userId
	 */
	public void fetchUserData(int userId, final onActionListener listener) {
		RestService.get().getUser(
				userId,
				new RestCallback<KPHUserData>() {
					@Override
					public void failure(RestError restError) {
						if (listener != null)
							listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
					}

					@Override
					public void success(KPHUserData kphUserData, Response response) {
						if (kphUserData == null) {
							if (listener != null)
								listener.failed(-1, "Unknown Error");
							return;
						}

						saveUserData(kphUserData);
						if (listener != null)
							listener.completed(kphUserData);
					}
				}
		);
	}

	public boolean getChildRestrictedFlag() {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);
		return sharedPreferences.getBoolean(KPHConstants.PREF_CHILD_RESTRICTED, false);
	}

	public void setChildRestrictedFlag(boolean bFlag) {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(KPHConstants.PREF_CHILD_RESTRICTED, bFlag);
		editor.apply();
	}

	public void saveUserToken(String userToken) {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(KPHConstants.PREF_USER_TOKEN, userToken);
		editor.apply();
	}

	public String loadUserToken() {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);
		return sharedPreferences.getString(KPHConstants.PREF_USER_TOKEN, "");
	}

	////////////////////////////////////////////////

	public void getVersion(final RestCallback<KPHVersion> listener) {
		RestService.get().version(listener);
	}
	////////////////////////////////////////////////

	public void signinFBAccount(KPHFacebook facebook, final onActionListener listener) {
		RestService.get().loginFBAccount(facebook,
				new RestCallback<KPHUserData>() {
					@Override
					public void failure(RestError restError) {
						listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
					}

					@Override
					public void success(KPHUserData kphUserData, Response response) {
						listener.completed(kphUserData);
					}
				});
	}

	public void signin(final String username, final String password, final RestCallback<KPHUserData> listener) {
		RestService.get().login(username, password, new RestCallback<KPHUserData>() {
			@Override
			public void success(KPHUserData userData, Response response) {
				listener.success(userData, response);
			}

			@Override
			public void failure(RestError restError) {
				listener.failure(restError);
			}
		});
	}

	public void signinOtherUser(long userId, final onActionListener listener) {
		RestService.get().loginOtherUser(
				userId,
				new RestCallback<KPHUserData>() {
					@Override
					public void failure(RestError restError) {
						listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
					}

					@Override
					public void success(KPHUserData kphUserData, Response response) {
						shouldReloadUserData();
						saveUserData(kphUserData);
						RestService.setUserToken(kphUserData.getAccessToken());
						listener.completed(kphUserData);
					}
				}
		);
	}

	public void signinOtherUser(long userId, final String password, final onActionListener listener) {
		RestService.get().loginOtherUser(
				userId,
				password,
				new RestCallback<KPHUserData>() {
					@Override
					public void failure(RestError restError) {
						listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
					}

					@Override
					public void success(KPHUserData kphUserData, Response response) {
						KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_UI_FAMILY_SWITCH);
						shouldReloadUserData();

						saveUserData(kphUserData);
						saveLoginData(
								kphUserData.getHandle(),
								password
						);
						RestService.setUserToken(kphUserData.getAccessToken());
						listener.completed(kphUserData);
					}
				}
		);
	}


	public void signupFBAccount(KPHFacebook facebook,
								RestCallback<KPHUserData> signupFB_listener) {
		RestService.get().signupFBAccount(facebook, signupFB_listener);
	}

	public void signup(
			String handle,
			String email,
			final String password,
			String friendlyName,
			String gender,
			Date dob,
			String avatarId,
			final onActionListener listener) {
		if (gender.length() == 0 || gender.equalsIgnoreCase(KPHConstants.GENDER_SKIP)) {
			RestService.get().signup(
					handle,
					email,
					password,
					friendlyName,
					new OSDate(dob).getDateOfBirth(),
					avatarId,
					new RestCallback<KPHUserData>() {
						@Override
						public void success(KPHUserData kphUserData, Response response) {
							listener.completed(kphUserData);
						}

						@Override
						public void failure(RestError restError) {
							listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
						}
					}
			);
		} else {
			RestService.get().signup(
					handle,
					email,
					password,
					friendlyName,
					gender,
					new OSDate(dob).getDateOfBirth(),
					avatarId,
					new RestCallback<KPHUserData>() {
						@Override
						public void success(KPHUserData kphUserData, Response response) {
							listener.completed(kphUserData);
						}

						@Override
						public void failure(RestError restError) {
							listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
						}
					}
			);
		}
	}

	public void createSubAccount(
			int parentId,
			String handle,
			String password,
			String friendlyName,
			String gender,
			Date dob,
			String avatarId,
			final onActionListener listener) {
		if (gender.length() == 0 || gender.equalsIgnoreCase(KPHConstants.GENDER_SKIP)) {
			RestService.get().createSubAccount(
					parentId,
					handle,
					password,
					friendlyName,
					new OSDate(dob).getDateOfBirth(),
					avatarId,
					new RestCallback<KPHUserData>() {
						@Override
						public void success(KPHUserData kphUserData, Response response) {
							listener.completed(kphUserData);
						}

						@Override
						public void failure(RestError restError) {
							listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
						}
					}
			);
		} else {
			RestService.get().createSubAccount(
					parentId,
					handle,
					password,
					friendlyName,
					gender,
					new OSDate(dob).getDateOfBirth(),
					avatarId,
					new RestCallback<KPHUserData>() {
						@Override
						public void success(KPHUserData kphUserData, Response response) {
							listener.completed(kphUserData);
						}

						@Override
						public void failure(RestError restError) {
							listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
						}
					}
			);
		}
	}

	/**
	 * Updates user data on the server, and saves it to shared preferences
	 *
	 * @param email        New email
	 * @param friendlyName New friendly name
	 * @param gender       New gender
	 * @param dob          New date of birth
	 * @param avatarId     New Avatar identifier
	 * @param listener     On Complete Listener
	 */
	public void updateUserData(
			int userId,
			String username,
			String email,
			String friendlyName,
			String gender,
			Date dob,
			String avatarId,
			final onActionListener listener
	) {
		if (username == null)
			username = "";

		if (email == null)
			email = "";

		if (friendlyName == null)
			friendlyName = "";

		if (gender == null)
			gender = "";

		if (avatarId == null)
			avatarId = "";


		if (email.length() == 0) {
			// This is child account
			RestService.get().updateUser(
					userId,
					username,
					friendlyName,
					gender,
					new OSDate(dob).getDateOfBirth(),
					avatarId,
					new RestCallback<KPHUserData>() {
						@Override
						public void failure(RestError restError) {
							listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
						}

						@Override
						public void success(KPHUserData updatedUserData, Response response) {
							listener.completed(updatedUserData);
						}
					}
			);
		} else {
			RestService.get().updateUser(
					userId,
					username,
					email,
					friendlyName,
					gender,
					new OSDate(dob).getDateOfBirth(),
					avatarId,
					new RestCallback<KPHUserData>() {
						@Override
						public void failure(RestError restError) {
							listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
						}

						@Override
						public void success(KPHUserData updatedUserData, Response response) {
							listener.completed(updatedUserData);
						}
					}
			);
		}

	}


	public void updateUserPassword(
			int userId,
			String password,
			final onActionListener listener
	) {
		RestService.get().updatePassword(
				userId,
				password,
				new RestCallback<KPHUserData>() {
					@Override
					public void failure(RestError restError) {
						listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
					}

					@Override
					public void success(KPHUserData kphUserData, Response response) {
						listener.completed(kphUserData);
					}
				});
	}

	public void setCurrentTracker(KPHTracker tracker) {
		currentTracker = tracker;
	}

	public KPHTracker getTempTracker() {
		return tempTracker;
	}

	public KPHTracker currentTracker() {
		return currentTracker;
	}

	public int loadCurrentTrackerType() {
		if (currentTracker == null)
			return TRACKER_TYPE_NONE;

		int type;
		if (currentTracker.getDeviceType() != null &&
				currentTracker.getDeviceType().equalsIgnoreCase(KPHTracker.TRACKER_TYPE_NAME_GOOGLEFIT)) {
			type = TRACKER_TYPE_GOOGLEFIT;
		} else if (currentTracker.getDeviceType() != null &&
				currentTracker.getDeviceType().equalsIgnoreCase(KPHTracker.TRACKER_TYPE_NAME_HEALTHKIT)) {
			type = TRACKER_TYPE_HEALTHKIT;
		} else {
			type = TRACKER_TYPE_KIDPOWERBAND;
		}

		return type;
	}


	/**
	 * for link to band and backend
	 */
	public void setTrackerToUser(String deviceName,
								 String deviceId,
								 int userId,
								 String type,
								 String version,
								 final onActionListener listener) {
		RestService.get().setTracker(
				deviceName,
				deviceId,
				userId,
				type,
				version,
				new RestCallback<KPHTracker>() {
					@Override
					public void failure(RestError restError) {
						listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
					}

					@Override
					public void success(KPHTracker kphTracker, Response response) {
						tempTracker = kphTracker;
						listener.completed(tempTracker);
					}
				}
		);
	}

	/**
	 * unlink tracker
	 *
	 * @param userId   : for user
	 * @param deviceId : band device id
	 * @param listener : for result
	 */
	public void unLinkTracker(int userId, String deviceId, final onActionListener listener) {
		RestService.get().deLinkTracker(userId, deviceId, new RestCallback<KPHDeLinkTrackerResponse>() {
			@Override
			public void failure(RestError restError) {
				listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
			}

			@Override
			public void success(KPHDeLinkTrackerResponse response, Response response2) {
				currentTracker = null;
				listener.completed(response.getStatus());
			}
		});
	}

	/**
	 * getV1 current tracker for user
	 *
	 * @param userId   : user id
	 * @param listener : call back
	 */
	public void getCurrentTrackerByUserId(int userId, final onActionListener listener) {
		RestService.get().getCurrentTrackerByUserId(userId, new RestCallback<KPHTracker>() {
			@Override
			public void failure(RestError restError) {
				listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
			}

			@Override
			public void success(KPHTracker kphTracker, Response response) {
				listener.completed(kphTracker);
			}
		});
	}


	public void saveLatestDeviceInformation(KPHDeviceInformation deviceInformation) {
		JSONObject jsonObject = deviceInformation.encodeToJSON();

		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(KPHConstants.PREF_DEVICE_INFORMATION, jsonObject.toString());
		editor.apply();
	}


	public KPHDeviceInformation loadLatestDeviceInformation() {
		SharedPreferences sharedPreferences = KPHUtils.sharedInstance().getApplicationContext()
				.getSharedPreferences(KPHConstants.PREFERENCE_KEY, Context.MODE_PRIVATE);
		String deviceinformation = sharedPreferences.getString(KPHConstants.PREF_DEVICE_INFORMATION, "");

		try {
			JSONObject jsonDevice = new JSONObject(deviceinformation);
			return KPHDeviceInformation.decodeFromJSON(jsonDevice);
		} catch (Exception ex) {
			ex.printStackTrace();
			return new KPHDeviceInformation();
		}
	}


	public void createDeviceRecord(KPHDeviceInformation deviceInformation, final onActionListener listener) {
		RestService.get().createDeviceRecord(
				deviceInformation.device,
				deviceInformation.imei,
				deviceInformation.iccid,
				deviceInformation.imsi,
				deviceInformation.udid,
				deviceInformation.versionCode,
				deviceInformation.userId,
				deviceInformation.pushId,
				new RestCallback<KPHDeviceInformation>() {
					@Override
					public void failure(RestError restError) {
						if (listener != null) {
							listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
						}
					}

					@Override
					public void success(KPHDeviceInformation deviceInformation, Response response) {
						if (listener != null) {
							listener.completed(deviceInformation);
						}
					}
				}
		);
	}


	/**
	 * Verifies credit card info is correct so that can verify the owner is adult
	 *
	 * @param cardNo   16 digit card number
	 * @param cvc      Card Verification Code
	 * @param expMonth Expiry Month
	 * @param expYear  Expiry Year
	 * @param listener Callback listener
	 */
	public void verifyAdult(
			final String cardNo,
			final String cvc,
			final int expMonth,
			final int expYear,
			final String zipCode,
			final onActionListener listener) {
/* This is deprecated. Commented by Dayong. 2017.04.28
		Card card = new Card(
				cardNo,
				expMonth,
				expYear,
				cvc
		);

		Context context = KPHUtils.sharedInstance().getApplicationContext();

		if (!card.validateNumber()) {
			listener.failed(-1, context.getString(R.string.age_verification_error_invalid_card_number));
			return;
		}

		if (!card.validateCVC()) {
			listener.failed(-1, context.getString(R.string.age_verification_error_invalid_cvc));
			return;
		}

		if (!card.validateExpiryDate()) {
			listener.failed(-1, context.getString(R.string.age_verification_error_invalid_expiry_date));
			return;
		}

		try {
			Stripe stripe = new Stripe(KPHConstants.STRIPE_PUBLISHABLE_API_KEY);
			stripe.createToken(
					card,
					new TokenCallback() {
						@Override
						public void onError(Exception error) {
							listener.failed(-1, error.getMessage());
						}

						@Override
						public void onSuccess(Token token) {
							RestService.get().verifyAdult(
									token.getId(),
									zipCode,
									new RestCallback<KPHUserData>() {
										@Override
										public void failure(RestError restError) {
											listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
										}

										@Override
										public void success(KPHUserData s, Response response) {
											setChildRestrictedFlag(false);
											userData.setVerifiedAdult(s.getVerifiedAdult());
											userData.setUserType(s.getUserType());
											saveUserData(userData);
											listener.completed(s);
										}
									});
						}
					});
		} catch (AuthenticationException e) {
			e.printStackTrace();
		}
*/
	}

	/**
	 * Search an user by its handle
	 *
	 * @param username Username (in other words Handle)
	 * @param listener callback function
	 */
	public void searchUsers(String username, final onActionListener listener) {
		int userId = getUserData().getId();
		RestService.get().searchUsers(userId, username, new RestCallback<KPHUserSummary>() {
			@Override
			public void failure(RestError restError) {
				listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
			}

			@Override
			public void success(KPHUserSummary foundUser, Response response) {
				if (foundUser == null) {
					listener.failed(-1, KPHApplication.sharedInstance().getString(R.string.default_error));
				} else {
					listener.completed(foundUser);
				}
			}
		});
	}

	///////////////////////////////////////////////////////////////

	public void verifyPurchaseCredit(KPHCreditReceipt receipt, long userId, final onActionListener listener) {
		if (userData == null) {
			return;
		}

		KPHCreditVerify creditReceipt = new KPHCreditVerify();
		creditReceipt.receipt = receipt;
		creditReceipt.userId = userId;
		creditReceipt.platform = "android";

		RestService.get().verifyPurchase(creditReceipt, new RestCallback<KPHCreditVerify.KPHCreditVerifyResult>() {
			@Override
			public void failure(RestError restError) {
				String message = KPHUtils.sharedInstance().getNonNullMessage(restError);
				int code = -1;
				if (message == null) {
					code = -2;
					message = "Rest Error";
					// network error
				}
				listener.failed(code, message);
			}

			@Override
			public void success(KPHCreditVerify.KPHCreditVerifyResult s, Response response) {
				if (s.status.equals("success")) {
					listener.completed(s.user);
				} else {
					listener.failed(-1, s.status);
				}
			}
		});
	}

	/**
	 * This is for Fabric user identifier
	 */
	private void logUser(KPHUserData userData) {
		// You can call any combination of these three methods
		Crashlytics.setUserIdentifier("" + userData.getId());
		if (userData.getEmail() != null) {
			Crashlytics.setUserEmail(userData.getEmail());
		}
		Crashlytics.setUserName(userData.getHandle());
	}

	public Drawable getAvatarDrawable(String avatarId) {
		String sScreenDensityName = ResolutionSet.getScreenDensityString(KPHUtils.sharedInstance().getApplicationContext());
		String sAvatarFilePath = "avatars/large/avatar-large-" + avatarId + "_" + sScreenDensityName;

		Drawable drawable = UIManager.sharedInstance().loadAssetDrawableFromResourceName(sAvatarFilePath);
		if (drawable == null) {
			Logger.log(TAG, "Avatar drawable is null : " + sAvatarFilePath);
			drawable = UIManager.sharedInstance().getImageDrawable(R.drawable.avatar_placeholder);
		}

		return drawable;
	}


	public Drawable getAvatarSmallDrawable(String avatarId) {
		String sScreenDensityName = ResolutionSet.getScreenDensityString(KPHUtils.sharedInstance().getApplicationContext());
		String sAvatarFilePath = "avatars/small/avatar-small-" + avatarId + "_" + sScreenDensityName;

		Drawable drawable = UIManager.sharedInstance().loadAssetDrawableFromResourceName(sAvatarFilePath);
		if (drawable == null) {
			Logger.log(TAG, "Small avatar drawable is null : " + sAvatarFilePath);
			drawable = UIManager.sharedInstance().getImageDrawable(R.drawable.avatar_placeholder);
		}

		return drawable;
	}

	public Drawable getCustomCheerDrawable(int cheerId) {
		Drawable drawable = null;
		String sCheerFilePath = null;
		String sScreenDensityName = ResolutionSet.getScreenDensityString(KPHUtils.sharedInstance().getApplicationContext());

		KPHCheerInformation cheerInformation = KPHMissionService.sharedInstance().getCheerInformation(cheerId);
		if (cheerInformation != null)
			sCheerFilePath = "cheers/" + cheerInformation.getImageName() + "_" + sScreenDensityName;

		if (!TextUtils.isEmpty(sCheerFilePath)) {
			drawable = UIManager.sharedInstance().loadAssetDrawableFromResourceName(sCheerFilePath);
		}

		if (drawable == null) {
			drawable = UIManager.sharedInstance().getImageDrawable(R.drawable.avatar_placeholder);
		}

		return drawable;
	}


	public boolean isUserFollowingMe(int userId) {
		if (getUserData().getFollowers() == null)
			return false;

		for (KPHUserSummary follower : getUserData().getFollowers()) {
			if (follower.getId() == userId)
				return true;
		}

		return false;
	}


	public boolean isUserBeingFollowedByMe(int userId) {
		if (getUserData().getFollowings() == null)
			return false;

		for (KPHUserSummary follower : getUserData().getFollowings()) {
			if (follower.getId() == userId)
				return true;
		}

		return false;
	}


	public boolean canUserVerifyAge() {
		KPHUserData userData = getUserData();
		Date orgBirthday = userData.getBirthday();
		if (orgBirthday == null)
			return true;

		OSDate birthday = new OSDate(userData.getBirthday());
		return birthday.getYearsPassed() >= KPHUserService.USER_MINIMUM_AGE && userData.getParentId() == 0;

	}


	public int getFamilyAccountsCount() {
		int familyAccountsCount = 0;
		KPHUserData userData = getUserData();
		if (userData.getParent() != null) {
			familyAccountsCount++;
		}
		if (userData.getChildren() != null) {
			familyAccountsCount += userData.getChildren().size();
		}
		if (userData.getSiblings() != null) {
			familyAccountsCount += userData.getSiblings().size();
		}

		return familyAccountsCount;
	}


	public void setEnabledPurchases(boolean enabled) {
		enabledPurchases = enabled;
	}


	public boolean enabledPurchases() {
		return enabledPurchases;
	}


	static boolean compositingNow = false;


	/**
	 * feature : send email to Support team
	 *
	 * @param context : application context
	 */
	public void sendMessage(SuperActivity context, boolean addLogPermission) {
		if (compositingNow)
			return;

		compositingNow = true;

		String subject = "";
		KPHUserData userData = getUserData();
		if (userData != null) {
			try {
				subject = "Kid Power feedback from " + userData.getHandle() +
						String.format(" (%s for Android)",
								context.getPackageManager().getPackageInfo(
										context.getPackageName(), 0
								).versionName
						);
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			try {
				subject = String.format(
						context.getString(R.string.feedback_email_subject),
						context.getPackageManager().getPackageInfo(
								context.getPackageName(), 0
						).versionName
				);
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
		}

		String body = context.getString(
				R.string.feedback_email_body_greetings,
				DeviceName.getDeviceName(),
				KPHUtils.sharedInstance().getAndroidVersion()
		);

		File zipFile = null;

		if (addLogPermission) {
			zipFile = createLogFile();
			if (zipFile == null) {
				Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_LOG_FAILED_REPORT);
				intent.addFlags(KPHConstants.PERM_STORAGE_DID_NOT_GRANT);
				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
			}
		}

		Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_LOG_WRITTEN_REPORT);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

		// Send file using email
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
		emailIntent.setData(Uri.parse("mailto:" + KPHConstants.HELP_CENTER_EMAIL));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, body);
		if (zipFile != null) {
			Uri uri = Uri.fromFile(zipFile);
			emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
		}

		context.startActivity(emailIntent);
		compositingNow = false;
	}


	protected static File createLogFile() {
		try {
			removeLogFile();

			String file_name;
			OSDate date = new OSDate();
			file_name = "[" + date.toStringWithFormat("yyyy-MM-dd") + "]";
			file_name += "KidPower version" + BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")";
			file_name = file_name.replace(' ', '_');

			File logFile = new File(Environment.getExternalStorageDirectory(), Logger.LOG_FILE);
			File outputFile = new File(Environment.getExternalStorageDirectory(), file_name + ".log");
			File zipFile = new File(Environment.getExternalStorageDirectory(), file_name + ".zip");

			if (logFile.exists()) {
				copyFile(logFile, outputFile);
				zip(outputFile.getAbsolutePath(), zipFile.getAbsolutePath());
			}

			return zipFile;
		} catch (IOException e) {
			Log.e("SEND_LOG", "createLogFile: exception occupation");
			e.printStackTrace();
			return null;
		}
	}


	protected static void copyFile(File logFile, File outputFile) throws IOException {
		String line;

		BufferedReader reader = new BufferedReader(new FileReader(logFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

		OSDate now = new OSDate(new Date());

		line = String.format("  Report Date : %s", now.toStringWithFormat("yyyy-MM-dd HH:mm:ss"));
		writer.write(line + "\r\n");
		line = String.format("  Manufacturer : %s(%s)", Build.MANUFACTURER, Build.MODEL);
		writer.write(line + "\r\n");
		line = String.format("  Device : %s(%s)", Build.PRODUCT, Build.DEVICE);
		writer.write(line + "\r\n");
		line = String.format("  Production : %s", (BuildConfig.IS_PRODUCTION ? "Yes" : "No"));
		writer.write(line + "\r\n");

		line = String.format("  SDK : %s, OS : %s(%s)", Build.VERSION.SDK_INT, Build.VERSION.RELEASE, Build.VERSION.CODENAME);
		writer.write(line + "\r\n");

		while ((line = reader.readLine()) != null) {
			writer.write(line + "\r\n");
		}

		reader.close();
		writer.flush();
		writer.close();
	}


	protected static void removeLogFile() {
		File dir = new File(Environment.getExternalStorageDirectory() + "");
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				if (children[i].contains("KidPower_version")) {
					new File(dir, children[i]).delete();
				}
			}
		}
	}

	// not used furture
	protected static void CopyStream(String inputFile, String outputFile) throws IOException {
		File tmpFile = new File(Environment.getExternalStorageDirectory(),
				outputFile + ".log");

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));

		// write device & os information to help bug fixing
		String line;

		OSDate now = new OSDate(new Date());

		line = String.format("  Report Date : %s", now.toStringWithFormat("yyyy-MM-dd HH:mm:ss"));
		writer.write(line + "\r\n");
		line = String.format("  Manufacturer : %s(%s)", Build.MANUFACTURER, Build.MODEL);
		writer.write(line + "\r\n");
		line = String.format("  Device : %s(%s)", Build.PRODUCT, Build.DEVICE);
		writer.write(line + "\r\n");
		line = String.format("  Production : %s", (BuildConfig.IS_PRODUCTION ? "Yes" : "No"));
		writer.write(line + "\r\n");

		line = String.format("  SDK : %s, OS : %s(%s)", Build.VERSION.SDK_INT, Build.VERSION.RELEASE, Build.VERSION.CODENAME);
		writer.write(line + "\r\n");

		writer.write("\r\n");

		/*boolean isRetrofit, isApp;
		// copy & filter app's log
		while ((line = reader.readLine()) != null) {
			isRetrofit = line.contains("Retrofit");
			if (isRetrofit) {
				if (!line.contains("--->") && !line.contains("\":\"") && !line.contains("<---"))
					continue;

				if (line.contains("password"))
					continue;
			} else {
				isApp = line.contains("W/KiHome");
				if (!isApp)
					continue;
			}

			writer.write(line + "\r\n");
		}*/

		reader.close();
		writer.flush();
		writer.close();
	}

	protected static void zip(String _origin, String _zipFile) {
		int BUFFER = 2048;
		try {
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(_zipFile);

			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

			byte data[] = new byte[BUFFER];

			Log.v("Compress", "Adding: " + _origin);
			FileInputStream fi = new FileInputStream(_origin);
			origin = new BufferedInputStream(fi, BUFFER);
			ZipEntry entry = new ZipEntry(_origin.substring(_origin.lastIndexOf("/") + 1));
			out.putNextEntry(entry);
			int count;
			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			origin.close();

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
