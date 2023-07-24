package org.unicefkidpower.kid_power.Model.MicroService.WebService;

import org.json.JSONException;
import org.json.JSONObject;

//import org.parceler.Parcel;

/**
 * Created by Dayong Li on 22/08/15.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

//@Parcel
public class RestError {
	String name = "";
	String message = "";

	public RestError() {}

	public RestError(JSONObject jsonError) {
		try {
			if (jsonError.getString("name") != null)
				this.name = jsonError.getString("name");
			else
				this.name = "";

			JSONObject jsonMessage = jsonError.getJSONObject("error");
			if (jsonMessage != null && jsonMessage.getString("message") != null)
				this.message = jsonMessage.getString("message");
			else
				this.message = "";

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public RestError(String name, String message) {
		if (name != null)
			this.name = name;
		else
			this.name = "";

		if (this.message != null)
			this.message = message;
		else
			this.message = "";
	}

	public String getName() {
		return name;
	}

	public String getMessage() {
		return this.message;
	}
}
