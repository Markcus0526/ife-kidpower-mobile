package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ruifeng Shi on 3/6/2017.
 */

public class KPHLogoutResult {
	@SerializedName("status")
	private String status;


	public KPHLogoutResult() {
		status = "";
	}

	public KPHLogoutResult(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
}
