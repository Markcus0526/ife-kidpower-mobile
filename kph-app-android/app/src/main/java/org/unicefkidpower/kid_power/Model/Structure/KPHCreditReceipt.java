package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dayong Li on 11/11/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class KPHCreditReceipt {
	@SerializedName("data")
	public KPHCreditPurchase purchase;

	@SerializedName("signature")
	public String signature;
}
