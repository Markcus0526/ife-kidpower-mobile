package org.unicefkidpower.schools.server.apimanage;

import org.json.JSONException;
import org.json.JSONObject;

//import org.parceler.Parcel;

/**
 * Created by Ruifeng Shi on 22/08/15.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

//@Parcel
public class RestError {
	String name = "";
	String message = "";

	public RestError() {
	}

	public RestError(JSONObject jsonError) {
		try {
			this.name = jsonError.getString("name");
			JSONObject jsonMessage = jsonError.getJSONObject("error");
			this.message = jsonMessage.getString("message");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public RestError(String name, String message) {
		this.name = name;
		this.message = message;
	}

	public String getName() {
		return name;
	}

	public String getMessage() {
		return this.message;
	}
}
