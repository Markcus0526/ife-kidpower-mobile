package org.unicefkidpower.kid_power.Model.MicroService;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerLib;
import com.swrve.sdk.SwrveIAPRewards;
import com.swrve.sdk.SwrveSDK;
import com.swrve.sdk.config.SwrveConfig;
import com.swrve.sdk.config.SwrveStack;

import org.unicefkidpower.kid_power.Application.KPHApplication;
import org.unicefkidpower.kid_power.BuildConfig;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ruifeng Shi on 5/18/2016.
 */
public class KPHAnalyticsService {
	private static final int			SWRVE_STAGING_APP_ID		= 6432;
	private static final String			SWRVE_STAGING_API_KEY		= "CzUlVEnUEA1jjWUrUldx";
	private static final int			SWRVE_PRODUCT_APP_ID		= 6455;
	private static final String			SWRVE_PRODUCT_API_KEY		= "FwUWBUOpzed6l8edJm3n";

	private static int					SWRVE_APP_ID;
	private static String				SWRVE_API_KEY;


	static {
		if (BuildConfig.IS_PRODUCTION) {
			SWRVE_APP_ID = SWRVE_PRODUCT_APP_ID;
			SWRVE_API_KEY = SWRVE_PRODUCT_API_KEY;
		} else {
			SWRVE_APP_ID = SWRVE_STAGING_APP_ID;
			SWRVE_API_KEY = SWRVE_STAGING_API_KEY;
		}
	}


	private static KPHAnalyticsService ourInstance = new KPHAnalyticsService();

	public static KPHAnalyticsService sharedInstance() {
		return ourInstance;
	}


	public void initializeSwrveAnalytics(Context context) {
		try {
			SwrveConfig config = new SwrveConfig();
			config.setSelectedStack(SwrveStack.EU);
			SwrveSDK.createInstance(context, SWRVE_APP_ID, SWRVE_API_KEY, config);
		} catch (IllegalArgumentException exp) {
			Log.e("SwrveDemo", "Could not initialize the Swrve SDK", exp);
		}
	}


	public void updateUserData(KPHUserData userData) {
		Map<String, String> attributes = new HashMap<>();
		attributes.put("email", userData.getEmail());
		attributes.put("handle", userData.getHandle());
		SwrveSDK.userUpdate(attributes);
	}



	public void logPurchase(String sku, long priceAmount, String currency) {
		SwrveIAPRewards rewards = new SwrveIAPRewards(currency, priceAmount);
		rewards.addItem(KPHConstants.CURRENCY_TYPE_CREDITS, 1);

		// SwrveSDK notify in-app purchase
		SwrveSDK.iap(
				1,
				sku,
				priceAmount / 100000.0,
				currency,
				rewards
		);

		// AppsFlyer notify in-app purchase
		{
			Map<String, Object> payload = new HashMap<>();
			payload.put(AFInAppEventParameterName.CONTENT_ID, sku);
			payload.put(AFInAppEventParameterName.CONTENT_TYPE, KPHConstants.CURRENCY_TYPE_CREDITS);
			payload.put(AFInAppEventParameterName.REVENUE, priceAmount / 100000.0);
			payload.put(AFInAppEventParameterName.CURRENCY, currency);

			AppsFlyerLib.getInstance().trackEvent(KPHApplication.sharedInstance().getApplicationContext(), AFInAppEventType.PURCHASE, payload);
		}
	}

	public void logUnlockMission(long missionId) {
		// Add purchase event to Swrve
		{
			String item = "" + missionId;
			String currency = KPHConstants.CURRENCY_TYPE_CREDITS;
			int cost = 1;
			int quantity = 1;

			SwrveSDK.purchase(item, currency, cost, quantity);
		}

		// AppsFlyer notify in-app purchase
		{
			Map<String, Object> payload = new HashMap<>();
			payload.put(AFInAppEventParameterName.CONTENT_ID, missionId);
			payload.put(AFInAppEventParameterName.CONTENT_TYPE, KPHConstants.CURRENCY_TYPE_CREDITS);
			payload.put(AFInAppEventParameterName.QUANTITY, 1);

			AppsFlyerLib.getInstance().trackEvent(KPHApplication.sharedInstance().getApplicationContext(), AFInAppEventType.SPENT_CREDIT, payload);
		}
	}

	public void logEvent(@NonNull String eventName) {
		logEvent(eventName, null);
	}

	public void logEvent(@NonNull String eventName, @Nullable Map<String, String> payload) {
		// Log for Swrve
		if (payload == null)
			SwrveSDK.event(eventName);
		else
			SwrveSDK.event(eventName, payload);

		// Log for AppsFlyer
		if (payload == null) {
			AppsFlyerLib.getInstance().trackEvent(KPHApplication.sharedInstance().getApplicationContext(), eventName, new HashMap<String, Object>());
		} else {
			Map<String, Object> map = new HashMap<>();
			map.putAll(payload);

			AppsFlyerLib.getInstance().trackEvent(KPHApplication.sharedInstance().getApplicationContext(), eventName, map);
		}
	}

}

