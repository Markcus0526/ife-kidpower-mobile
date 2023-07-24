package org.unicefkidpower.kid_power.Model.MicroService.Facebook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONObject;
import org.unicefkidpower.kid_power.BuildConfig;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.OSDate;
import org.unicefkidpower.kid_power.R;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Ruifeng Shi on 3/9/2017.
 */

public class FacebookService {
	private static final String			TAG							=	"FacebookService";
	public static final int				LOGIN_RESULT_CODE_SUCCESS	=	0;
	public static final int				LOGIN_RESULT_CODE_FAILED	=	-1;

	private Context						appContext					=	null;
	private CallbackManager				callbackManager				=	null;
	private LoginManager				loginManager				=	null;
	private OnProfileResultListener		profileListener				=	null;
	private AccessToken					accessToken					=	null;

	/**
	 * Singleton class instance
	 */
	private static FacebookService facebookService					=	null;


	public static FacebookService getSharedInstance(Context applicationContext) {
		if (facebookService == null)
			facebookService = new FacebookService(applicationContext);

		return facebookService;
	}


	public FacebookService(Context appContext) {
		this.appContext = appContext;
		initCallbacks();
	}


	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}


	public void clearAccountData() {
		LoginManager.getInstance().logOut();
	}

	public void getProfile(Activity activity, OnProfileResultListener listener) {
		if (listener == null)
			return;

		this.profileListener = listener;
		loginManager.logInWithReadPermissions(activity, Arrays.asList("public_profile", "email", "user_birthday"));
	}


	// Private methods
	private void initCallbacks() {
		callbackManager = CallbackManager.Factory.create();

		loginManager = LoginManager.getInstance();
		loginManager.registerCallback(callbackManager, loginCallbacks);
	}


	private FacebookCallback loginCallbacks = new FacebookCallback<LoginResult>() {
		@Override
		public void onSuccess(LoginResult loginResult) {
			accessToken = loginResult.getAccessToken();
			GraphRequest request = GraphRequest.newMeRequest(
					accessToken,
					new GraphRequest.GraphJSONObjectCallback() {
						@Override
						public void onCompleted(JSONObject object, GraphResponse response) {
							if (object == null) {
								if (profileListener != null)
									profileListener.onFailure(LOGIN_RESULT_CODE_FAILED, appContext.getString(R.string.failed_retrieve_fb_profile));
								return;
							}

							try {
								String email = object.optString("email");
								String fbId = object.optString("id");

								String gender = object.optString("gender");
								if (gender.equals("male")) {
									gender = KPHConstants.GENDER_MALE;
								} else if (gender.equals("female")) {
									gender = KPHConstants.GENDER_FEMALE;
								} else {
									gender = KPHConstants.GENDER_SKIP;
								}

								String birthday = object.optString("birthday");
								if (birthday.length() > 0) {
									Date dtValue = OSDate.fromStringWithFormat(birthday, "d/M/y", false);
									if (dtValue != null) {
										Calendar cal = Calendar.getInstance();
										cal.setTime(dtValue);
										birthday = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
									} else {
										birthday = "";
									}
								}

								if (profileListener != null)
									profileListener.onSuccess(accessToken.getToken().toString(),
											email,
											gender,
											birthday,
											fbId);
							} catch (Exception ex) {
								ex.printStackTrace();

								if (BuildConfig.IS_PRODUCTION) {
									if (profileListener != null)
										profileListener.onFailure(LOGIN_RESULT_CODE_FAILED, appContext.getString(R.string.failed_retrieve_fb_profile));
								} else {
									if (profileListener != null)
										profileListener.onFailure(LOGIN_RESULT_CODE_FAILED, ex.getMessage());
								}
							}
						}
					});

			Bundle parameters = new Bundle();
			parameters.putString("fields", "id,name,email,gender,birthday");
			request.setParameters(parameters);
			request.executeAsync();
		}

		@Override
		public void onCancel() {
			if (profileListener != null) {
				profileListener.onCancelled();
			}
		}

		@Override
		public void onError(FacebookException error) {
			if (profileListener != null) {
				profileListener.onFailure(LOGIN_RESULT_CODE_FAILED, appContext.getString(R.string.failed_retrieve_fb_profile));
			}
		}
	};


	public interface OnProfileResultListener {
		void onSuccess(String accessToken, String email, String gender, String birthday, String fbId);
		void onFailure(int errorCode, String message);
		void onCancelled();
	}


}
