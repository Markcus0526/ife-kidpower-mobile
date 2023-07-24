package org.unicefkidpower.schools.helper;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;

import com.kiddoware.kidsplace.sdk.KPUtility;

import org.unicefkidpower.schools.R;

/**
 * Created by Ruifeng Shi on 8/26/2016.
 */
public class KidsPlaceHelper {
	private static final String[] kidsPlaceWhitelist = {
			"com.google.android.gms.ui",
			"com.android.vending",
			"com.lge.ime.theme.whiteblue",
			"com.lge.ime.theme.m9theme",
			"com.google.android.inputmethod.latin",
			"om.jb.gokeyboard.theme.timkeyboardforlgg3",
			"com.jb.gokeyboard.theme.tmekeyboardforlg",
			"com.jb.gokeyboard.theme.tmekeyboardforlgg3",
			"com.jb.gokeyboard.theme.timsskeyboardforlggfour",
			"com.jb.gokeyboard.theme.tmekeyboardforlgoptimus",
			"com.lge.ime.theme.bluehydra",
			"com.lge.ime.theme.g3Theme"
	};

	private static KidsPlaceHelper ourInstance = new KidsPlaceHelper();

	public static KidsPlaceHelper getInstance() {
		return ourInstance;
	}

	private KidsPlaceHelper() {
	}

	public boolean isKidsPlaceRunning(Activity activity) {
		return KPUtility.isKidsPlaceRunning(activity);
	}

	public void startKidsPlace(Activity activity) {
		for (String whitelistItem : kidsPlaceWhitelist) {
			addToKidsPlaceWhitelist(activity, whitelistItem);
		}

		updateKidsPlaceWallpaper(activity, R.drawable.kidsplace_wallpaper);
		activity.startActivity(new Intent(activity, com.kiddoware.kidspower.LaunchActivity.class));
	}

	public void updateKidsPlaceWallpaper(Activity activity, int resId) {
		String imageURI = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
				activity.getResources().getResourcePackageName(resId) + '/' +
				activity.getResources().getResourceTypeName(resId) + '/' +
				activity.getResources().getResourceEntryName(resId);
		KPUtility.savePrefernce(activity.getApplicationContext(), "wallpaperUri", imageURI, "string");
	}

	public void updateKidsPlaceUserEmail(Activity activity, String email) {
		KPUtility.savePrefernce(activity.getApplicationContext(), "userEmail", email, "string");
	}

	public void addToKidsPlaceWhitelist(Activity activity, String packageName) {
		KPUtility.addAppToWhiteList(activity, packageName);
		// addToKidsPlace(pkgName);//adds to home screen. test code
	}

	//adds app to home screen and makes it approved.
	public void addToKidsPlace(Activity activity, String packageName) {
		KPUtility.addAppToKidsPlace(activity.getApplicationContext(), packageName, null, null);
	}

	public void setFullscreen(Activity activity, boolean isFullscreen) {
		KPUtility.savePrefernce(
				activity.getApplicationContext(),
				"immersive_mode",
				isFullscreen ? "1" : "0",
				"bool"
		);
	}
}
