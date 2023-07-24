package org.unicefkidpower.schools.define;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;

import java.util.regex.Pattern;


public class CommonUtils {
	private static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
			"[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
					"\\@" +
					"[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
					"(" +
					"\\." +
					"[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
					")+"
	);

	private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^[+]?[0-9]{10,13}$");

	public static boolean isValidEmail(String strEmail) {
		return EMAIL_ADDRESS_PATTERN.matcher(strEmail).matches();
	}

	public static boolean isValidPhone(String strPhone) {
		return PHONE_NUMBER_PATTERN.matcher(strPhone).matches();
	}

	public static String getMaskedEmail(String szOrgEmail) {
		String szRet = "";
		if (szOrgEmail == null)
			return szRet;

		boolean bFlag = false;
		for (int i = 0; i < szOrgEmail.length(); i++) {
			char chCurr;
			try {
				chCurr = szOrgEmail.charAt(i);
				if (chCurr != '@' && chCurr != '.') {
					if (bFlag == false) {
						bFlag = true;
						szRet += chCurr;
					} else {
						szRet += '*';
					}
				} else {
					bFlag = false;
					szRet += chCurr;
				}
			} catch (Exception ex) {
				szRet += '*';
				break;
			}
		}

		return szRet;
	}

	public static Bitmap getBitmapFromPath(String szPath, int imageWidth, int imageHeight) {
		Bitmap bmpPhoto = null;
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			Bitmap bitmap = BitmapFactory.decodeFile(szPath, options);

			if (bitmap != null) {
				int nWidth = bitmap.getWidth(), nHeight = bitmap.getHeight();
				int nScaledWidth = 0, nScaledHeight = 0;
				if (nWidth > nHeight) {
					nScaledWidth = imageWidth;
					nScaledHeight = nScaledWidth * nHeight / nWidth;
				} else {
					nScaledHeight = imageHeight;
					nScaledWidth = nScaledHeight * nWidth / nHeight;
				}

				bmpPhoto = Bitmap.createScaledBitmap(bitmap, nScaledWidth, nScaledHeight, false);

				return bmpPhoto;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	public static boolean isEmpty(String str) {
		if (str == null)
			return true;
		if (str.length() == 0)
			return true;
		return false;
	}

	public static int getVersionCode(Context context) {
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pInfo.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static String getVersionName(Context context) {
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}


	public static float getDeviceDensity(Context context) {
		float fDensity = context.getResources().getDisplayMetrics().density;

		return fDensity;
	}


	public static int getColorFromRes(Resources resources, int resid) {
		if (Build.VERSION.SDK_INT >= 23)
			return resources.getColor(resid, null);
		else
			return resources.getColor(resid);
	}


	public static Drawable getDrawableFromRes(Resources resources, int resid) {
		if (Build.VERSION.SDK_INT >= 21)
			return resources.getDrawable(resid, null);
		else
			return resources.getDrawable(resid);
	}

}