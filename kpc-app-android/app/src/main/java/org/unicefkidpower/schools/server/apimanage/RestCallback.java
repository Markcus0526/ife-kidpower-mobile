package org.unicefkidpower.schools.server.apimanage;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.unicefkidpower.schools.KidpowerApplication;
import org.unicefkidpower.schools.helper.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedInput;

/**
 * Implementation of RTCallback to filter Rest Service Error
 * Created by Ruifeng Shi on 22/08/15.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public abstract class RestCallback<T> implements Callback<T> {
	/**
	 * Use with to retrieve a {@link
	 * ConnectivityManager} for handling management of
	 * network connections.
	 *
	 * @see ConnectivityManager
	 */

	public static final String REST_TAG = "REST";

	public static final String CONNECTIVITY_SERVICE = "connectivity";

	/**
	 * Error constants defined by CalorieCloud
	 */
	// 4XXXX : Client errors
	public static final int REST_ERROR_NO_NETWORK = 40001;

	public static final int REST_ERROR_SUCCESS = 200;

	public abstract void failure(RetrofitError retrofitError, String message);

	@Override
	public void failure(RetrofitError error) {
		TypedInput body = null;
		Response response = error.getResponse();

		if (response != null)
			body = error.getResponse().getBody();

		if (response == null || body == null) {
			if (!isNetworkAvailable()) {
				Logger.error(REST_TAG, "Network connection error: Network is down");
				failure(error, "Your network is not available now. Please check your network connection, try again.");
			} else {
				Logger.error(REST_TAG, "Network connection error: Host is unreachable");
				failure(error, "Host is unreachable now. Please check your network connection, try again.");
			}
			return;
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(body.in()));
			StringBuilder out = new StringBuilder();
			String newLine = System.getProperty("line.separator");
			String line;
			while ((line = reader.readLine()) != null) {
				out.append(line);
//                out.append(newLine);
			}

			RestError restError = new RestError(new JSONObject(out.toString()));
			failure(error, restError.getMessage());
			Logger.error(REST_TAG, restError.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(body.in()));
				StringBuilder out = new StringBuilder();
				String newLine = System.getProperty("line.separator");
				String line;
				while ((line = reader.readLine()) != null) {
					out.append(line);
				}

				failure(error, out.toString());
				Logger.error(REST_TAG, out.toString());
			} catch (IOException e1) {
				Logger.error(REST_TAG, "Unknown Error");
				failure(error, "");
				e1.printStackTrace();
			}
		}
	}

	public static boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) KidpowerApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

}
