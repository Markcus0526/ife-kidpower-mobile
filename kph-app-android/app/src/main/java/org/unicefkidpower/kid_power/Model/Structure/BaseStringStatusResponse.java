package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ruifeng Shi on 1/25/2016.
 */
public class BaseStringStatusResponse {
	@SerializedName("status")
	private String status;


	public BaseStringStatusResponse(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
}
