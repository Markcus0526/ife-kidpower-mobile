package org.unicefkidpower.kid_power.Model.MicroService.WebService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import org.unicefkidpower.kid_power.BuildConfig;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;

import java.util.concurrent.TimeUnit;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Implementation of Rest Service
 * <p>
 * Created by Dayong Li on 22/08/15.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class RestService {
	private static final String DEVEL_HOST = "https://kph-main-staging.herokuapp.com";
	private static final String PRODUCT_HOST = "https://kph.unicefkidpower.org";

	private static String BASE_URL;

	private static RestEndpoint rtService;

	private static String userToken;
	private static String version;
	private static String userID;

	static {
		if (BuildConfig.IS_PRODUCTION) {
			BASE_URL = PRODUCT_HOST;
		} else {
			BASE_URL = DEVEL_HOST;
		}
		setupRestClient();
	}

	private RestService() {
	}

	public static RestEndpoint get() {
		return rtService;
	}

	public static String getUserToken() {
		if (userToken == null || userToken.length() == 0)
			userToken = KPHUserService.sharedInstance().loadUserToken();
		return userToken;
	}

	public static void setUserToken(String mTokenValue) {
		userToken = mTokenValue;

		KPHUserService.sharedInstance().saveUserToken(mTokenValue);
		version = BuildConfig.VERSION_NAME;
	}

	public static String getUserID() {
		return userID;
	}

	public static boolean getIsUserLoggedIn() {
		return KPHUserService.sharedInstance().loadUserToken().length() > 0;
	}

	public static void setUserID(String userIDValue) {
		userID = userIDValue;
	}

	public static void setupRestClient() {
		Gson gson = new GsonBuilder()
				.setDateFormat("yyyy'-'MM'-'dd")
				.create();

		OkHttpClient okHttpClient = new OkHttpClient();
		okHttpClient.setReadTimeout(60, TimeUnit.SECONDS);
		okHttpClient.setConnectTimeout(60, TimeUnit.SECONDS);

		RestAdapter restAdapter = new RestAdapter.Builder()
				.setLogLevel(RestAdapter.LogLevel.FULL)
				.setEndpoint(BASE_URL)
				.setConverter(new GsonConverter(gson))
				.setRequestInterceptor(new RequestInterceptor() {
					@Override
					public void intercept(RequestFacade request) {
						request.addHeader("x-access-token", getUserToken());
						request.addHeader("version", "2.0");
					}
				})
				.setClient(new OkClient(okHttpClient))
				.build();
		rtService = restAdapter.create(RestEndpoint.class);
	}

}
