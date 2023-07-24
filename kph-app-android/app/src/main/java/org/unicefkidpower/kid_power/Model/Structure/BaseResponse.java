package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ruifeng Shi on 09/09/15.
 */
public class BaseResponse {
	@SerializedName("status")
	private int status;

	@SerializedName("message")
	private String message;



	public BaseResponse() {}

	public BaseResponse(int status, String message) {
		this.status = status;
		this.message = message;
	}

	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}
}
