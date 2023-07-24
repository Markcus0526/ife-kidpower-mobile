package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dayong Li on 11/11/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class KPHCreditVerify {
	@SerializedName("receipt")
	public KPHCreditReceipt receipt;

	@SerializedName("userId")
	public long userId;

	@SerializedName("platform")
	public String platform;


	public class KPHCreditVerifyResult {
		@SerializedName("status")
		public String status;

		@SerializedName("user")
		public KPHUserData user;

		public KPHCreditVerifyResult() {}
	}

}
