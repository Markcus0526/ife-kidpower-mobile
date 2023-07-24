package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ruifeng Shi on 11/19/2015.
 */
public class KPHDeLinkTrackerResponse {
	@SerializedName("status")
	private String status;


	public KPHDeLinkTrackerResponse() {}

	public KPHDeLinkTrackerResponse(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
}
