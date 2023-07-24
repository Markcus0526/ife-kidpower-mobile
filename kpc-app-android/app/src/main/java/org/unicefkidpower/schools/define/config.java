package org.unicefkidpower.schools.define;

/**
 * Created by donal_000 on 1/15/2015.
 */
public class config {
	public static final boolean IS_PRODUCT = true;//(BuildConfig.DEBUG) ? false : true;

	public static final boolean IS_GWS_EDITION = false;

	public static final boolean USE_LOGIC = true;
	public static final boolean USE_RESOLUTIONSET = false;
	public static final boolean USE_GLOBALLISTENER = true;

	private static final int SWRVE_STAGING_APP_ID = 6345;
	private static final String SWRVE_STAGING_API_KEY = "Q7Q7vWC1rE0T638AXAaQ";

	private static final int SWRVE_PRODUCTION_APP_ID = 6452;
	private static final String SWRVE_PRODUCTION_API_KEY = "Y5gogb5UlN5XTiPouggA";

	public static int SWRVE_APP_ID;
	public static String SWRVE_API_KEY;

	static {
		if (IS_PRODUCT) {
			SWRVE_APP_ID = SWRVE_PRODUCTION_APP_ID;
			SWRVE_API_KEY = SWRVE_PRODUCTION_API_KEY;
		} else {
			SWRVE_APP_ID = SWRVE_STAGING_APP_ID;
			SWRVE_API_KEY = SWRVE_STAGING_API_KEY;
		}
	}

	public static final String ZENDESK_URL = "https://unicefkidpower.zendesk.com";
	public static final String ZENDESK_APPID = "71555d7564bdc951ca694f42045b5fa2ccf189729e66ebf4";
	public static final String ZENDESK_CLIENTID = "mobile_sdk_client_b9238b8b81a7e3492154";
}
