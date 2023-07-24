package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

import org.unicefkidpower.kid_power.Misc.BillingUtil.Purchase;

/**
 * Created by Dayong Li on 11/11/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class KPHCreditPurchase {
	@SerializedName("orderId")
	String mOrderId;

	@SerializedName("packageName")
	String mPackageName;

	@SerializedName("productId")
	String mSku;

	@SerializedName("purchaseTime")
	long mPurchaseTime;

	@SerializedName("purchaseState")
	int mPurchaseState;

	@SerializedName("developerPayload")
	String mDeveloperPayload;

	@SerializedName("purchaseToken")
	String mToken;

	public KPHCreditPurchase(Purchase purchase) {
		mOrderId = purchase.getOrderId();
		mPackageName = purchase.getPackageName();
		mSku = purchase.getSku();
		mPurchaseTime = purchase.getPurchaseTime();
		mPurchaseState = purchase.getPurchaseState();
		mDeveloperPayload = purchase.getDeveloperPayload();
		mToken = purchase.getToken();
	}
}
