package org.unicefkidpower.kid_power.Misc;

import android.Manifest;

import org.unicefkidpower.kid_power.BuildConfig;

/**
 * Created by Ruifeng Shi on 9/22/2015.
 */
public class KPHConstants {
	/***********************************************************************************************
	 * Stripe API Keys
	 */
	public static String STRIPE_PUBLISHABLE_API_KEY;

	public static final String STRIPE_PUBLISHABLE_API_KEY_LIVE		= "pk_live_ciymHpqeaDVXPUn8r5851vLH";
	public static final String STRIPE_PUBLISHABLE_API_KEY_TEST		= "pk_test_40bvvufHYFGfQvOo6MWW6yKr";

	static {
		if (BuildConfig.IS_PRODUCTION) {
			STRIPE_PUBLISHABLE_API_KEY = STRIPE_PUBLISHABLE_API_KEY_LIVE;
		} else {
			STRIPE_PUBLISHABLE_API_KEY = STRIPE_PUBLISHABLE_API_KEY_TEST;
		}
	}

	private static final String FACBOOK_KEYHASH			= "Mdxm7SFzFwIvR9qoTYxBmKOAYRw=";
	/**
	 * End of 'Stripe API Keys'
	 **********************************************************************************************/


	/***********************************************************************************************
	 * Google License Keys
	 */
	public static final String BASE64_PUBLIC_SUBKEY1	= "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsar3t3gPvjDERDe+i4FSmKav4cvfi";
	public static final String BASE64_PUBLIC_SUBKEY2	= "CqcWL2/0LvLxBa047KrACckKLe6hOC8QINBio5HwgNl2YT2L+9sOGxinE1dRyZmzZ72taLN8S";
	public static final String BASE64_PUBLIC_SUBKEY3	= "s7ftbtdcUe8Y9mj8Bc80MMX4IkwKnK05nAYyZZ6w1TMOyab5Yi0A59kvCnN4w8S7YbSvK4ARZ";
	public static final String BASE64_PUBLIC_SUBKEY4	= "C31NSNX+2Hz+/dWJ3z+Ngw1IwW+3Awpu01GNrQ9sR5BX2cx0DqxwM490o9lZ2RbhuRmzicGmp";
	public static final String BASE64_PUBLIC_SUBKEY5	= "Yzci4+y7EQlfp6hinO2LEK5QnTYVKGqeQru75G22O3FP0Ow+EDZ7SvzlD8+FZ4Sczy3iSNKAj";
	public static final String BASE64_PUBLIC_SUBKEY6	= "m8bdJzuBk3w/7Dle+54dQIDAQAB";
	/**
	 * End of 'Google License Keys'
	 **********************************************************************************************/


	/***********************************************************************************************
	 * SKU Constants
	 */
	public static final String SKU_BUY_CREDIT						= "org.unicefkidpower.kph.credit_1";
	public static final String SKU_BUY_2							= "org.unicefkidpower.kph.credit_2";
	public static final String SKU_BUY_3							= "org.unicefkidpower.kph.credit_3";

	public static final String CURRENCY_TYPE_USD					= "USD";
	public static final String CURRENCY_TYPE_CREDITS				= "credits";
	/**
	 * End of 'SKU Constants'
	 **********************************************************************************************/

	/***********************************************************************************************
	 * APPSFLYER DEV KEY
	 */
	public static final String APPSFLYER_DEV_KEY					= "V2tWaGVBTv5dgtHvaFpnUm";
	/**
	 * End of 'APPSFLYER DEV KEY'
	 **********************************************************************************************/


	/***********************************************************************************************
	 * Preference Keys
	 */
	public static final String PREFERENCE_KEY						= "KPH_PREFERENCE";
	public static final String PREF_AUTH_USERNAME					= "userName";
	public static final String PREF_AUTH_PASSWORD					= "password";

	public static final String PREF_AUTH_FBID						= "fbId";
	public static final String PREF_AUTH_EMAIL						= "email";
	public static final String PREF_AUTH_ACCESSTOKEN				= "accessToken";

	public static final String PREF_USER_TOKEN						= "user_token";
	public static final String PREF_USER_INFO						= "user_info";
	public static final String PREF_CATCH_TRACKER_DIALOG_YEAR		= "catch_tracker_dialog_year";
	public static final String PREF_CATCH_TRACKER_DIALOG_MONTH		= "catch_tracker_dialog_month";
	public static final String PREF_CATCH_TRACKER_DIALOG_DAY		= "catch_tracker_dialog_day";
	public static final String PREF_CHILD_RESTRICTED				= "child_restricted";
	public static final String PREF_DEVICE_INFORMATION				= "device_information";
	public static final String PREF_SEEN_TUTORIAL					= "seen_tutorial";
	public static final String PREF_SIGNED_IN						= "signed_in";
	public static final String PREF_LAST_LINKED_TIME				= "last_link_GF_time";
	/**
	 * End of 'Preference Keys'
	 **********************************************************************************************/


	/***********************************************************************************************
	 * Permission management constants
	 */
	public static final int PERMISSION_REQUEST_LOCATION		= 123;
	public static final int PERMISSION_REQUEST_STORAGE		= 125;
	public static final int PERMISSION_REQUEST_PHONE_STATE	= 127;

	public static final int PERM_STORAGE_NEED_GRANT			= 0;
	public static final int PERM_STORAGE_DID_NOT_GRANT		= 1;
	/**
	 * End of 'Permission management constants'
	 **********************************************************************************************/


	/***********************************************************************************************
	 * URL constants
	 */
	public static final String URL_UNICEFKIDPOWER_ORG		= "http://unicefkidpower.org";
	public static final String URL_PRIVACY_POLICY			= "http://unicefkidpower.org/privacy-policy";

	public static final String WELCOME_VIDEO_PATH			= "video/welcome-video.mp4";
	public static final String APP_STORE_URL				= "https://play.google.com/store/apps/details?id=org.unicefkidpower.kph";

	public static final String SHARING_URL					= "http://www.unicefkidpower.org";
	public static final String HELP_CENTER_EMAIL			= "hello@unicefkidpower.org";
	public static final String URL_FAQ						= "http://appfaq.unicefkidpower.org";
	public static final String GET_KIDPOWER_BAND_URL		= "https://unicefkidpower.org/store/";
	public static final String GOOGLE_FIT_STORE_URL			= "market://details?id=com.google.android.apps.fitness";
	public static final String GOOGLE_FIT_PAGE_URL			= "https://play.google.com/store/apps/details?id=com.google.android.apps.fitness&hl=en";
	/**
	 * End of 'URL Constants'
	 **********************************************************************************************/



	/***********************************************************************************************
	 * SWRVE Event constants
	 */
	public static final String SWRVE_UI_FRIENDS							= "ui.friends";
	public static final String SWRVE_UI_PASSPORT						= "ui.passport";
	public static final String SWRVE_UI_TRAVEL_LOG						= "ui.travel_log";
	public static final String SWRVE_UI_MORE							= "ui.more";
	public static final String SWRVE_UI_FAMILY_ICON						= "ui.family_account.icon";
	public static final String SWRVE_UI_FAMILY_SWITCH					= "ui.family_account.switch";

	public static final String SWRVE_CHEER_SENT							= "cheer.sent";

	public static final String SWRVE_AGE_VERIFY_START					= "age_verification.start";
	public static final String SWRVE_AGE_VERIFY_COMPLETE				= "age_verification.complete";

	public static final String SWRVE_FRIENDS_FOLLOW						= "friends.follow";
	public static final String SWRVE_FRIENDS_UNFOLLOW					= "friends.unfollow";

	public static final String SWRVE_TRACKER_LINK_START					= "tracker.link.start";
	public static final String SWRVE_TRACKER_LINK_SUCCESS				= "tracker.link.success";
	public static final String SWRVE_TRACKER_LINK_ERROR					= "tracker.link.error";
	public static final String SWRVE_TRACKER_LINK_ERROR_OK				= "tracker.link.error.ok";

	public static final String SWRVE_TRACKER_SYNC_START					= "tracker.sync.start";
	public static final String SWRVE_TRACKER_SYNC_SUCCESS				= "tracker.sync.success";
	public static final String SWRVE_TRACKER_SYNC_ERROR					= "tracker.sync.error";
	public static final String SWRVE_TRACKER_SYNC_ERROR_OK				= "tracker.sync.error.ok";
	public static final String SWRVE_TRACKER_SYNC_NODATA				= "tracker.sync.nodata";

	public static final String SWRVE_MISSION_START						= "mission.start";
	public static final String SWRVE_MISSION_COMPLETE					= "mission.complete";

	public static final String SWRVE_SHARE								= "share";
	/**
	 * End of 'SWRVE Event Constants'
	 **********************************************************************************************/


	/***********************************************************************************************
	 * Location permission constant
	 **********************************************************************************************/
	public static final String LOCATION_PERMISSION			= Manifest.permission.ACCESS_COARSE_LOCATION;


	/***********************************************************************************************
	 * Misc constants
	 */
	public static final int USERNAME_MAX_LENGTH				= 20;
	public static final int USERNAME_MIN_VALID_LENGTH		= 3;
	public static final int PASSWORD_MIN_LENGTH				= 6;

	public static final String PROFILE_DEVICE_TYPE			= "device_type";
	public static final String PROFILE_DEVICE_SELECTED		= "is_linked";

	public static final String FLURRY_API_KEY				= "D8S9G56GVWWDXYF65M9W";

	public static final String GENDER_SKIP					= "undefined";
	public static final String GENDER_MALE					= "male";
	public static final String GENDER_FEMALE				= "female";

	public static final String DELIGHT_POSTCARD				= "postcard";
	public static final String DELIGHT_STAMP				= "stamp";
	public static final String DELIGHT_RUTF					= "rutf";
	public static final String DELIGHT_KPP					= "kpp";
	public static final String DELIGHT_WEEK					= "time-week";
	public static final String DELIGHT_DISTANCE				= "distance";

	public static final String LOGOUT_SUCCESS				= "Logged out!";

}
