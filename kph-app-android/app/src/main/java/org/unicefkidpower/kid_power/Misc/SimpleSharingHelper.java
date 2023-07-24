package org.unicefkidpower.kid_power.Misc;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;

import org.unicefkidpower.kid_power.View.Super.SuperActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dayong Li on 11/11/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class SimpleSharingHelper {
	public static final String CONTENT_URI = "content://org.unicefkidpower.kid_power/";

	// Class used as a struct to hold all information
	public static class ShareBundle {
		public String title; // title for sharing dialog

		public String content;
		public String subject;
		public String twitter_content;
		public String url;
		public String image;
	}

	public static void share(final SuperActivity activity, final ShareBundle shareBundle) {
		shareBundle.image = CONTENT_URI + shareBundle.image;
		List<Intent> shareIntents = generateSharingIntents(activity, shareBundle);
		if (shareIntents != null && !shareIntents.isEmpty()) {
			Intent chooser =
					Intent.createChooser(shareIntents.remove(0), "Share");
			chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,
					shareIntents.toArray(new Parcelable[]{}));

			activity.startActivity(chooser);
		}
	}

	public static final String MMS_COMPOSIER = "com.android.mms";
	public static final String EMAIL_COMPOSIER = "com.android.email";
	public static final String GMAIL_COMPOSIER = "com.google.android.gm";
	public static final String TWITTER_APP = "twitter.android.DMActivity";
	public static final String TWEET_COMPOSIER = "twitter.android.composer";
	public static final String FACEBOOK_COMPOSIER = "facebook.composer";
	public static final String LINKEDIN_COMPOSIER = "linkedin";

	public static List<Intent> generateSharingIntents(Activity activity, final ShareBundle bundle) {
		List<Intent> intents = new ArrayList<>();

		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("text/plain");

		PackageManager pm = activity.getPackageManager();
		if (pm == null) return null;
		List<ResolveInfo> resInfo = pm.queryIntentActivities(share, 0);
		if (resInfo == null) return null;
		System.out.println("resinfo: " + resInfo);

		try {
			Intent mmsIntent = getShareIntent(pm, resInfo, MMS_COMPOSIER,
					bundle.content, null, null);
			if (mmsIntent != null) intents.add(mmsIntent);

			Intent emailComposer = getShareIntent(pm, resInfo, EMAIL_COMPOSIER,
					bundle.content, bundle.subject, null);
			if (emailComposer != null) intents.add(emailComposer);

			Intent gmComposer = getShareIntent(pm, resInfo, GMAIL_COMPOSIER,
					bundle.content, bundle.subject, bundle.image);
			if (gmComposer != null) intents.add(gmComposer);

			Intent liComposer = getShareIntent(pm, resInfo, LINKEDIN_COMPOSIER,
					bundle.content, null, null);
			if (liComposer != null) intents.add(liComposer);

			Intent tweetComposer = getShareIntent(pm, resInfo, TWEET_COMPOSIER,
					bundle.twitter_content, null, bundle.image);
			if (tweetComposer != null) intents.add(tweetComposer);

			Intent twitterIntent = getShareIntent(pm, resInfo, TWITTER_APP,
					bundle.content, bundle.subject, bundle.image);
			if (twitterIntent != null) intents.add(twitterIntent);

			if (false) {
				Intent facebookIntent = getShareIntent(pm, resInfo, FACEBOOK_COMPOSIER,
						bundle.content, null, bundle.image);
				if (facebookIntent != null) intents.add(facebookIntent);
			}
		} catch (Exception e) {
			return null;
		}
		return intents;
	}

	/**
	 * @param resInfo    : resolved activity list
	 * @param identifier : application id
	 * @param text       : text for sharing, should be valid
	 * @param subject    : subject for sharing, optional
	 * @param image      : image url for sharing, optional
	 * @return : return sharing intent
	 */
	private static Intent getShareIntent(PackageManager pm, List<ResolveInfo> resInfo, String identifier,
										 String text, String subject, String image) {
		// gets the list of intents that can be loaded.
		if (pm == null || resInfo == null || resInfo.isEmpty()) {
			return null;
		}
		for (ResolveInfo info : resInfo) {
			if (info.activityInfo.packageName.toLowerCase().contains(identifier.toLowerCase())
					|| info.activityInfo.name.toLowerCase().contains(identifier.toLowerCase())) {

				Intent share = new Intent(Intent.ACTION_SEND);
				//share.setType("text/plain");
				share.setType("text/*");

				//share.setPackage(info.activityInfo.packageName);
				share.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));

				share.putExtra(Intent.EXTRA_TEXT, text);
				if (subject != null) {
					share.putExtra(Intent.EXTRA_SUBJECT, subject);
				}
				if (image != null) {
					share.setType("image/*");
					Uri theUri = Uri.parse(image);
					share.putExtra(Intent.EXTRA_STREAM, theUri);
				}

				return share;
			}
		}
		return null;
	}
}
