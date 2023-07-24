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

public class SyncRestService {
	private static final String DEVEL_HOST = "https://kph-sync-staging.herokuapp.com";
	private static final String PRODUCT_HOST = "https://kphsync.unicefkidpower.org";

	private static String BASE_URL;
	private static SyncRestEndpoint rtService_v1, rtService_v2;

	private static String userToken;
	private static String version;

	static {
		if (BuildConfig.IS_PRODUCTION) {
			BASE_URL = PRODUCT_HOST;
		} else {
			BASE_URL = DEVEL_HOST;
		}

		setupRestClient();
	}

	private SyncRestService() {
	}

	public static SyncRestEndpoint getV1() {
		return rtService_v1;
	}

	public static SyncRestEndpoint getV2() {
		return rtService_v2;
	}

	public static String getUserToken() {
		if (userToken == null || userToken.length() == 0) {
			userToken = KPHUserService.sharedInstance().loadUserToken();
			version = BuildConfig.VERSION_NAME;
		}
		return userToken;
	}

	public static void setUserToken(String mTokenValue) {
		userToken = mTokenValue;

		KPHUserService.sharedInstance().saveUserToken(mTokenValue);
		version = BuildConfig.VERSION_NAME;
	}

	public static void setupRestClient() {
		Gson gson = new GsonBuilder()
				.setDateFormat("yyyy'-'MM'-'dd")
				.create();

		OkHttpClient okHttpClient = new OkHttpClient();
		okHttpClient.setReadTimeout(300, TimeUnit.SECONDS);
		okHttpClient.setConnectTimeout(60, TimeUnit.SECONDS);

		// Rest service version 1.0
		RestAdapter restAdapter_v1 = new RestAdapter.Builder()
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
		rtService_v1 = restAdapter_v1.create(SyncRestEndpoint.class);


		// Rest service version 2.0
		RestAdapter restAdapter_v2 = new RestAdapter.Builder()
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
		rtService_v2 = restAdapter_v2.create(SyncRestEndpoint.class);
	}

}
