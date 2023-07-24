package org.unicefkidpower.schools.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.unicefkidpower.schools.KidpowerApplication;
import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.ResolutionSet;
import org.unicefkidpower.schools.helper.OSDate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by donal_000 on 1/21/2015.
 */
public class UiUtils {
	private static UiUtils ourInstance = new UiUtils();

	public static UiUtils getInstance() {
		return ourInstance;
	}

	public static void showKeyboard(Context context, EditText editText) {
		InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (editText == null) {
			return;
		} else {
			mgr.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
		}
	}

	public static void showKeyboard(Context context, View view) {
		InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.toggleSoftInputFromWindow(view.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
	}

	public static void hideKeyboard(Activity activity) {
		// Check if no view has focus:
		if (activity == null)
			return;

		View view = null;
		try {
			view = activity.getCurrentFocus();
		} catch (Exception ex) {
			return;
		}

		if (view != null) {
			InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	public static void hideKeyboard(Context context, EditText editText) {
		InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	public static String getSyncingDateStringWithDate(Date dt) {
		if (dt == null)
			return "";
		OSDate date = new OSDate(dt);
		return date.toStringWithFormat("LLL d"); // "Jan 12";
	}

	public static int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	public Drawable getAvatarDrawable(String avatarId) {
		Drawable drawable = null;
		String sAvatarFilePath;
		String sScreenDensityName = ResolutionSet.getScreenDensityString(
				KidpowerApplication.getAppContext().getApplicationContext()
		);

		sAvatarFilePath = "avatars/large/avatar-large-" +
				avatarId + "_" + sScreenDensityName + ".png";

		try {
			InputStream inputStream = KidpowerApplication.getAppContext().getApplicationContext()
					.getAssets().open(sAvatarFilePath);
			drawable = Drawable.createFromStream(inputStream, null);
		} catch (IOException e) {
			drawable = KidpowerApplication.getAppContext().getApplicationContext().getResources()
					.getDrawable(R.drawable.no_avatar);
		}

		return drawable;
	}

	public int getNumberOfAvatars() {
		int nNumberOfAvatars = 0;
		String[] avatarFileList;
		String sAvatarFolderPath = "avatars/large";

		try {
			avatarFileList = KidpowerApplication.getAppContext().getApplicationContext().getAssets()
					.list(sAvatarFolderPath);
			if (avatarFileList.length > 0) {
				nNumberOfAvatars = avatarFileList.length / 5;   //Dividing into 5 because there are five same images for mdpi, hdpi, xhdpi, xxhdpi and xxxhdpi
			}
		} catch (IOException e) {
			nNumberOfAvatars = 0;
		}

		return nNumberOfAvatars;
	}

	public static int getPixelFromDips(Context context, float pixels) {
		// Get the screen's density scale
		final float scale = context.getResources().getDisplayMetrics().density;

		// Convert the dps to pixels, based on density scale

		return (int) (pixels * scale + 0.5f);

	}
}
