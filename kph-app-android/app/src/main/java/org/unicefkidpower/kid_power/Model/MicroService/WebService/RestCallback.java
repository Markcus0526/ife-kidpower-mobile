package org.unicefkidpower.kid_power.Model.MicroService.WebService;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.unicefkidpower.kid_power.Misc.KPHUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedInput;

/**
 * Implementation of RTRestCallback to filter Rest Service Error
 * Created by Dayong Li on 22/08/15.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public abstract class RestCallback<T> implements Callback<T> {

	/**
	 * Use with to retrieve a {@link
	 * android.net.ConnectivityManager} for handling management of
	 * network connections.
	 *
	 * @see android.net.ConnectivityManager
	 */
	public static final String CONNECTIVITY_SERVICE = "connectivity";

	/**
	 * Error constants defined by CalorieCloud
	 */
	// 4XXXX : Client errors
	public static final int REST_ERROR_NO_NETWORK = 40001;

	public static final int REST_ERROR_SUCCESS = 200;

	public abstract void failure(RestError restError);

	@Override
	public void failure(RetrofitError error) {
		TypedInput body = null;
		Response response = error.getResponse();

		if (response != null)
			body = error.getResponse().getBody();

		if (response == null || body == null) {
			if (!isNetworkAvailable()) {
				failure(
						new RestError(
								"Network error",
								"An internet connection is required. Please check your network connection and try again."
						)
				);
			} else {
				failure(
						new RestError(
								"Network error",
								"Host is unreachable now. Please check your network connection and try again."
						)
				);
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
			if (restError != null) {
				failure(restError);
			} else {
				if (!isNetworkAvailable()) {
					failure(
							new RestError(
									"Network error",
									"Please check your connection and try again."
							)
					);
				} else {
					failure(new RestError("", ""));
				}
			}
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
//                    out.append(newLine);
				}

				RestError restError = new RestError("Error", out.toString());

				if (restError != null) {
					failure(restError);
				} else {
					if (!isNetworkAvailable()) {
						failure(
								new RestError(
										"Network error",
										"Please check your connection and try again."
								)
						);
					} else {
						failure(new RestError("", ""));
					}
				}
			} catch (IOException e1) {
				failure(new RestError("", ""));
				e1.printStackTrace();
			}
		}
	}

	public static boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager
				= (ConnectivityManager) KPHUtils.sharedInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

}
