package org.unicefkidpower.kid_power.Misc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import org.unicefkidpower.kid_power.Model.MicroService.KPHAnalyticsService;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dayong Li on 11/11/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class SharingHelper extends BroadcastReceiver {
	private static final int SDK_VERSION_PENDING_ENABLE = 22; // use devprecated function because new function is not good

	public static final String CONTENT_URI = "content://org.unicefkidpower.kid_power/";

	private static String sTargetChosenReceiveAction;
	private static SuperActivity activity;

	@Override
    public void onReceive(Context context, Intent intent) {
		ComponentName target = intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT);
		if (target != null) {
			setShareIntent(target.getClassName());
		}
	}

	static SharingHelper sharedInstance = null;
	static final boolean SHARE_USING_INTENT = true;

	public static SharingHelper getInstance() {
		if (sharedInstance == null) {
			sharedInstance = new SharingHelper();
		}
		return sharedInstance;
	}

	// Class used as a struct to hold all information
	public static class ShareBundle {
		public int requestCode;
		public String subject;
		public String content;
		public String twitter_content;
		public String url;
		public String assetImage;
		public Uri image;
		public String title;
	}

	public static void share(SuperActivity context, final ShareBundle shareBundle) {
		activity = context;

		sTargetChosenReceiveAction = context.getPackageName() + "/" + SharingHelper.class.getName() + "_ACTION";
		context.registerReceiver(new SharingHelper(), new IntentFilter(sTargetChosenReceiveAction));

		shareBundle.image = getLocalBitmapUri(context, shareBundle.assetImage + ".png");
		onSharingViaIntent(context, shareBundle);
	}


	private static void onSharingViaIntent(Activity context, final ShareBundle shareBundle) {
		PackageManager pm = context.getPackageManager();

		Intent filterIntend = new Intent(Intent.ACTION_SEND);
		filterIntend.setType("image/*");

		List<ResolveInfo> resInfo = pm.queryIntentActivities(filterIntend, 0);
		List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();

		Intent defaultIntent = null;

		for (ResolveInfo ri : resInfo) {
			String packageName = ri.activityInfo.packageName.toLowerCase();
			String activityName = ri.activityInfo.name.toLowerCase();

			String contents = null, subject = null;
			Uri image = null;

			/*if (packageName.contains("com.adsk") ||
					packageName.contains("com.samsung") ||
					packageName.contains("com.dropbox") ||
					packageName.contains("com.mfluent") ||
					packageName.contains("com.tripadvisor") ||
					packageName.contains("com.evernote") ||
					packageName.contains("com.sec") ||
					packageName.contains("flipboard") ||
					packageName.contains("com.tmall") ||
					packageName.contains("com.skype") ||
					packageName.contains("com.cleanmaster") ||
					packageName.contains("com.pinterest") ||
					packageName.contains("com.linkedin") ||
					packageName.contains("com.hp")) {
				continue;
			}

			if (packageName.contains("com.android")) {
				if (packageName.contains("bluetooth") ||
						packageName.contains("nfc")) {
					continue;
				}

				if (activityName.contains("mms") ||
						packageName.contains("email")) {
					contents = shareBundle.content;
					subject = shareBundle.subject;
					image = shareBundle.image;
				}
			} else if (packageName.contains("com.google")) {
				if (packageName.contains("gateway") ||
						packageName.contains("hangout") ||
						packageName.contains("maps") ||
						packageName.contains("keep") ||
						packageName.contains("apps.plus") ||
						packageName.contains("apps.doc") ||
						activityName.contains("picasa") ||
						packageName.contains("photos")) {
					continue;
				}

				if (packageName.contains("gm")) {
					contents = shareBundle.content;
					subject = shareBundle.subject;
					image = shareBundle.image;
				} else if (packageName.contains("messaging")) {
					contents = shareBundle.content;
					subject = shareBundle.subject;
					image = shareBundle.image;
				} else {
					contents = shareBundle.content;
					subject = shareBundle.subject;
				}
			} else if (packageName.contains("com.twitter")) {
				if (activityName.contains("composer")) {
					contents = shareBundle.twitter_content;
					if (contents.length() > 117) {
						contents = contents.substring(0, 116);
					}
					image = shareBundle.image;
				} else {
					contents = shareBundle.content;
					subject = shareBundle.subject;
					image = shareBundle.image;
				}
			} else if (packageName.contains("com.facebook")) {
				// Warning: Facebook IGNORES our text.
				// They say "These fields are intended for users to express themselves.
				// Pre-filling these fields erodes the authenticity of the user voice."
				// One workaround is to use the Facebook SDK to post, but that doesn't allow the user to choose how they want to share.
				// We can also make a custom landing page, and the link will show the <meta content ="..."> text from that page with our link in Facebook.
				contents = shareBundle.content;
				image = shareBundle.image;
			} else if (packageName.contains("com.tencent")) {
				if (packageName.contains("compose")) {
					subject = shareBundle.subject;
					contents = shareBundle.content;
					image = shareBundle.image;
				}
				//else if (packageName.contains("mm")) {
				//    subject = shareBundle.subject;
				//    contents = shareBundle.content;
				//    image = shareBundle.image;
				//}
				else {
					continue;
				}
			} else if (packageName.contains("com.slack")) {
				subject = shareBundle.subject;
				contents = shareBundle.content;
				image = shareBundle.image;
			} else if (packageName.contains("kik.android")) {
				subject = shareBundle.subject;
				contents = shareBundle.content;
				image = shareBundle.image;
			} else {
				subject = shareBundle.subject;
				contents = shareBundle.content;
				image = shareBundle.image;
			}*/
			if (packageName.contains("com.google")) {
				if (packageName.contains("gateway") ||
						packageName.contains("hangout") ||
						packageName.contains("maps") ||
						packageName.contains("keep") ||
						packageName.contains("apps.plus") ||
						packageName.contains("apps.doc") ||
						activityName.contains("picasa") ||
						packageName.contains("photos")) {
					continue;
				}

				if (packageName.contains("gm")) {
					contents = shareBundle.content;
					subject = shareBundle.subject;
					image = shareBundle.image;
				} else {
					continue;
				}
			} else {
				continue;
			}

			Intent intent = new Intent();
			intent.setComponent(new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name));
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("text/*");

			if (subject != null) {
				intent.putExtra(Intent.EXTRA_SUBJECT, subject);
			}
			if (contents != null) {
				intent.putExtra(Intent.EXTRA_TEXT, contents);
			}
			if (image != null) {
				intent.setType("image/*");
				intent.putExtra(Intent.EXTRA_STREAM, image);
			}

			if (defaultIntent == null) {
				defaultIntent = intent;
			} else {
				intentList.add(new LabeledIntent(intent, ri.activityInfo.packageName, ri.loadLabel(pm), ri.icon));
			}
		}

		if (defaultIntent == null) {
			// create default intent;
			defaultIntent = new Intent(Intent.ACTION_SEND);
			defaultIntent.setType("image/*");
			if (shareBundle.image != null) {
				defaultIntent.putExtra(Intent.EXTRA_STREAM, shareBundle.image);
			}
			defaultIntent.putExtra(Intent.EXTRA_SUBJECT, shareBundle.subject);
			defaultIntent.putExtra(Intent.EXTRA_TEXT, shareBundle.content);
		}



		if (android.os.Build.VERSION.SDK_INT < SDK_VERSION_PENDING_ENABLE) {
			Intent openInChooser = Intent.createChooser(defaultIntent, shareBundle.title);

			// convert intentList to array
			LabeledIntent[] extraIntents = null;
			if (intentList.size() > 0) {
				extraIntents = intentList.toArray(new LabeledIntent[intentList.size()]);
			}

			openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
			context.startActivityForResult(openInChooser, shareBundle.requestCode);

			KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_SHARE);
		} else {
			Intent intent = new Intent(SharingHelper.sTargetChosenReceiveAction);
			intent.setPackage(activity.getPackageName());
			final PendingIntent callback = PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
			Intent openInChooser = Intent.createChooser(defaultIntent, shareBundle.title, callback.getIntentSender());
			// convert intentList to array
			LabeledIntent[] extraIntents = null;
			if (intentList.size() > 0) {
				extraIntents = intentList.toArray(new LabeledIntent[intentList.size()]);
			}
			openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);

			context.startActivity(openInChooser);
		}
	}

	private static void setShareIntent(String classname) {
		String className = classname.toLowerCase();
		String appName = KPHConstants.SWRVE_SHARE;

		if (className.contains("mms"))
			appName = KPHConstants.SWRVE_SHARE + ".mms";
		else if (className.contains("gm"))
			appName = KPHConstants.SWRVE_SHARE + ".gm";
		else if (className.contains("messaging"))
			appName = KPHConstants.SWRVE_SHARE + ".messaging";
		else if (className.contains("twitter"))
			appName = KPHConstants.SWRVE_SHARE + ".twitter";
		else if (className.contains("facebook"))
			appName = KPHConstants.SWRVE_SHARE + ".facebook";
		else if (className.contains("tencent"))
			appName = KPHConstants.SWRVE_SHARE + ".tencent";
		else if (className.contains("slack"))
			appName = KPHConstants.SWRVE_SHARE + ".slack";
		else if (className.contains("kik"))
			appName = KPHConstants.SWRVE_SHARE + ".kik";
		else
			appName = KPHConstants.SWRVE_SHARE + ".others";

		KPHAnalyticsService.sharedInstance().logEvent(appName);
	}

	private static Uri getLocalBitmapUri(Context context, String image) {
		// Extract Bitmap from ImageView drawable
		AssetManager assetManager = context.getAssets();
		InputStream istr = null;
		try {
			istr = assetManager.open(image);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Bitmap bitmap = BitmapFactory.decodeStream(istr);

		// Store image to default external storage directory
		Uri bmpUri = null;
		try {
			File file =  new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_PICTURES), "kp_share.png");
			file.getParentFile().mkdirs();
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.close();
			bmpUri = Uri.fromFile(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bmpUri;
	}

}
