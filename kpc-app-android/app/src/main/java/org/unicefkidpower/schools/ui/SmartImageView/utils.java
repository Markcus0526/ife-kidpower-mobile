package org.unicefkidpower.schools.ui.SmartImageView;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.view.Display;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class utils {
	public static int getHighQualityValue() {
		return 0;
	}

	public static int getLowQualityValue() {
		return 1;
	}

	public static void logHeap() {
		Double allocated = new Double(Debug.getNativeHeapAllocatedSize()) / new Double((1048576));
		Double available = new Double(Debug.getNativeHeapSize()) / 1048576.0;
		Double free = new Double(Debug.getNativeHeapFreeSize()) / 1048576.0;
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);
	}

	public static long getAvailableMemorySize(Context app_context) {
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		ActivityManager activityManager = (ActivityManager) app_context.getSystemService(Service.ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);

		return mi.availMem;
	}

	public static boolean isLowMemory(Context app_context) {
		// If available memory is smaller than 2MB, this function return true
		if (getAvailableMemorySize(app_context) < 2 * 1024 * 1024)
			return true;

		return false;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void setImageOnButton(Button button, Drawable drawable, int nWidth, int nHeight) {
		drawable.setBounds(0, 0, nWidth, nHeight);
		button.setBackgroundDrawable(drawable);
	}

	public static int getDefWidth() {
		return 480;
	}

	public static int getDefHeight() {
		return 800;
	}

	public static double getFontSizeRate(Context appContext) {
		double fRateX = getScrWidth(appContext) / (float) getDefWidth();
		double fRateY = getScrHeight(appContext) / (float) getDefHeight();
		if (fRateX > fRateY)
			return fRateX;
		else
			return fRateY;
	}

	public static long clearCachedFiles(Context appContext) {
		long bytesDeleted = 0;

		File dir = getExternalCacheDir(appContext);
		if (dir == null)
			return 0;

		File[] files = dir.listFiles();
		if (files == null)
			return 0;

		for (File file : files) {
			bytesDeleted += file.length();
			file.delete();
		}

		return bytesDeleted;
	}

	public static void openBrowser(Context context, String url) {
		try {
			if (!url.contains("http://") && !url.contains("https://"))
				url = "http://" + url;

			Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			context.startActivity(browser);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static byte[] Bitmap2Jpeg(Bitmap src) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		src.compress(Bitmap.CompressFormat.JPEG, 50, os);

		byte[] array = os.toByteArray();
		return array;
//		return BitmapFactory.decodeByteArray(array, 0, array.length);
	}

	public static boolean isExternalStorageRemovable() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return Environment.isExternalStorageRemovable();
		}

		return true;
	}

	public static File getExternalCacheDir(Context context) {
		if (hasExternalCacheDir()) {
			return context.getExternalCacheDir();
		}

		// Before Froyo we need to construct the external cache dir ourselves
		final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
		return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
	}

	public static boolean hasExternalCacheDir() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}

	public static int getApiVersion() {
		return Build.VERSION.SDK_INT;
	}

	public static int getScrWidth(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		return display.getWidth();
	}

	public static int getScrHeight(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		return display.getHeight();
	}

	public static Bitmap loadBmpFromUrl(String szUrl) {
		URL url = null;
		Bitmap bmp = null;
		ByteArrayOutputStream stream = null;
		byte[] arrImgData = null;

		try {
			url = new URL(szUrl);
			bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return bmp;
	}

	public static boolean saveRememPassFlag(Context appContext, boolean bFlag) {
		boolean bSuccess = true;
		SharedPreferences prefs = null;

		try {
			prefs = appContext.getSharedPreferences("hiyou_userinfo", Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putBoolean("remember_pass", bFlag);
			edit.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
			bSuccess = false;
		}

		return bSuccess;

	}

	public static boolean getRememPassFlag(Context appContext) {
		SharedPreferences prefs = appContext.getSharedPreferences("hiyou_userinfo", Context.MODE_PRIVATE);
		return prefs.getBoolean("remember_pass", true);
	}

	public static boolean saveNotifID(Context appContext, int nID) {
		boolean bSuccess = true;
		SharedPreferences prefs = null;

		try {
			prefs = appContext.getSharedPreferences("hiyou_userinfo", Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("notif_id", nID);
			edit.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
			bSuccess = false;
		}

		return bSuccess;

	}

	public static int loadNotifID(Context appContext) {
		SharedPreferences prefs = appContext.getSharedPreferences("hiyou_userinfo", Context.MODE_PRIVATE);
		return prefs.getInt("notif_id", 0);
	}

	public static boolean saveBrowseType(Context appContext, int nBrowseType) {
		boolean bSuccess = true;
		SharedPreferences prefs = null;

		try {
			prefs = appContext.getSharedPreferences("hiyou_browsetype", Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("browsetype", nBrowseType);
			edit.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
			bSuccess = false;
		}

		return bSuccess;
	}

	public static int loadBrowseType(Context appContext) {
		SharedPreferences prefs = appContext.getSharedPreferences("hiyou_browsetype", Context.MODE_PRIVATE);
		return prefs.getInt("browsetype", getHighQualityValue());
	}

	public static int getLineCount(TextView view, String szText, int nWidth) {
		int nCharCount = view.getPaint().breakText(szText, 0, szText.length(), true, nWidth, null);
		int nLineCount = szText.length() / nCharCount;
		if (szText.length() > nCharCount * nLineCount)
			nLineCount++;

		return nLineCount;
	}

	public static int fitLineCount(TextView view, String szText, int nWidth) {
		int nCharCount = view.getPaint().breakText(szText, 0, szText.length(), true, nWidth, null);
		int nLineCount = szText.length() / nCharCount;
		if (szText.length() > nCharCount * nLineCount)
			nLineCount++;
		view.setLines(nLineCount);
		return nLineCount;
	}

	public static String encodePostBody(Bundle parameters, String boundary) {
		if (parameters == null) return "";
		StringBuilder sb = new StringBuilder();

		for (String key : parameters.keySet()) {
			sb.append("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n" + parameters.get(key));
			sb.append("\r\n" + "--" + boundary + "\r\n");
		}

		return sb.toString();
	}

	public static String encodeUrl(Bundle parameters) {
		if (parameters == null)
			return "";

		StringBuilder sb = new StringBuilder();
		boolean first = true;

		for (String key : parameters.keySet()) {
			if (first) first = false;
			else sb.append("&");
			sb.append(URLEncoder.encode(key) + "=" + URLEncoder.encode(parameters.getString(key)));
		}

		return sb.toString();
	}

	public static Bundle decodeUrl(String s) {
		Bundle params = new Bundle();
		if (s != null) {
			String array[] = s.split("&");
			for (String parameter : array) {
				String v[] = parameter.split("=");
				// YG: in case param has no value
				if (v.length == 2) {
					params.putString(URLDecoder.decode(v[0]), URLDecoder.decode(v[1]));
				} else {
					params.putString(URLDecoder.decode(v[0]), " ");
				}
			}
		}
		return params;
	}

	/**
	 * Parse a URL query and fragment parameters into a key-value bundle.
	 *
	 * @param url the URL to parse
	 * @return a dictionary bundle of keys and values
	 */
	public static Bundle parseUrl(String url) {
		// hack to prevent MalformedURLException
		url = url.replace("fbconnect", "http");

		try {
			URL u = new URL(url);
			Bundle b = decodeUrl(u.getQuery());
			b.putAll(decodeUrl(u.getRef()));
			return b;
		} catch (MalformedURLException e) {
			return new Bundle();
		}
	}

	private static String read(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
		for (String line = r.readLine(); line != null; line = r.readLine()) {
			sb.append(line);
		}
		in.close();
		return sb.toString();
	}

	public static void showKeyboardFromText(EditText editText, Context context) {
		InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
	}

	public static void hideKeyboardFromText(EditText editText, Context context) {
		InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	public static boolean saveNewProductId(Context appContext, long uid) {
		ArrayList<Long> newProductIDs = getOldProductIdArray(appContext);
		if (!newProductIDs.contains(uid)) {
			newProductIDs.add(uid);
			return saveOldProductIdArray(appContext, newProductIDs);
		}

		return true;
	}

	public static boolean saveNewProductIds(Context appContext, ArrayList<Long> arrIDs) {
		ArrayList<Long> newProductIDs = getOldProductIdArray(appContext);
		for (int i = 0; i < arrIDs.size(); i++) {
			if (!newProductIDs.contains(arrIDs.get(i)))
				newProductIDs.add(arrIDs.get(i));
		}

		saveOldProductIdArray(appContext, newProductIDs);

		return true;
	}

	private static boolean saveOldProductIdArray(Context appContext, ArrayList<Long> arrIDs) {
		boolean bSuccess = true;
		SharedPreferences prefs = null;

		try {
			JSONArray arrJSONObj = new JSONArray();
			for (int i = 0; i < arrIDs.size(); i++) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("Value", arrIDs.get(i));
				arrJSONObj.put(jsonObj);
			}

			prefs = appContext.getSharedPreferences("hiyou_userinfo", Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putString("newpid_array", arrJSONObj.toString());
			edit.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
			bSuccess = false;
		}

		return bSuccess;
	}

	public static ArrayList<Long> getOldProductIdArray(Context appContext) {
		ArrayList<Long> arrNewPIDs = new ArrayList<Long>();

		SharedPreferences prefs = appContext.getSharedPreferences("hiyou_userinfo", Context.MODE_PRIVATE);
		String szNewIDs = prefs.getString("newpid_array", "");

		try {
			if (!szNewIDs.equals("")) {
				JSONArray arrNewIDs = new JSONArray(szNewIDs);
				for (int i = 0; i < arrNewIDs.length(); i++) {
					JSONObject objItem = arrNewIDs.getJSONObject(i);
					arrNewPIDs.add(objItem.getLong("Value"));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return arrNewPIDs;
	}

	public static boolean saveNotifiedProductId(Context appContext, long uid) {
		ArrayList<Long> newProductIDs = getNotifiedProductIdArray(appContext);
		if (!newProductIDs.contains(uid)) {
			newProductIDs.add(uid);
			return saveNotifiedProductIdArray(appContext, newProductIDs);
		}

		return true;
	}

	private static boolean saveNotifiedProductIdArray(Context appContext, ArrayList<Long> arrIDs) {
		boolean bSuccess = true;
		SharedPreferences prefs = null;

		try {
			JSONArray arrJSONObj = new JSONArray();
			for (int i = 0; i < arrIDs.size(); i++) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("Value", arrIDs.get(i));
				arrJSONObj.put(jsonObj);
			}

			prefs = appContext.getSharedPreferences("hiyou_userinfo", Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putString("notified_pid_array", arrJSONObj.toString());
			edit.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
			bSuccess = false;
		}

		return bSuccess;
	}

	public static ArrayList<Long> getNotifiedProductIdArray(Context appContext) {
		ArrayList<Long> arrNewPIDs = new ArrayList<Long>();

		SharedPreferences prefs = appContext.getSharedPreferences("hiyou_userinfo", Context.MODE_PRIVATE);
		String szNewIDs = prefs.getString("notified_pid_array", "");

		try {
			if (!szNewIDs.equals("")) {
				JSONArray arrNewIDs = new JSONArray(szNewIDs);
				for (int i = 0; i < arrNewIDs.length(); i++) {
					JSONObject objItem = arrNewIDs.getJSONObject(i);
					arrNewPIDs.add(objItem.getLong("Value"));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return arrNewPIDs;
	}

	public static String getOldProductIdString(Context appContext) {
		String szResult = "";
		ArrayList<Long> arrOldIDs = getOldProductIdArray(appContext);
		for (int i = 0; i < arrOldIDs.size(); i++) {
			if (!szResult.equals(""))
				szResult += "_";

			szResult += arrOldIDs.get(i);
		}

		return szResult;
	}

	public static void saveNotifActivityID(Context appContext, int nID) {
		if (isExistNotifActivityID(appContext, nID))
			return;

		ArrayList<Integer> arrNewNotifIDs = new ArrayList<Integer>();
		SharedPreferences prefs = appContext.getSharedPreferences("hiyou_userinfo", Context.MODE_PRIVATE);
		String szNewIDs = prefs.getString("notifid_array", "");

		try {
			if (!szNewIDs.equals("")) {
				JSONArray arrOldIDs = new JSONArray(szNewIDs);
				for (int i = 0; i < arrOldIDs.length(); i++) {
					JSONObject objItem = arrOldIDs.getJSONObject(i);
					arrNewNotifIDs.add(objItem.getInt("Value"));
				}
			}

			arrNewNotifIDs.add(nID);

			JSONArray arrNewIDs = new JSONArray();
			for (int i = 0; i < arrNewNotifIDs.size(); i++) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("Value", nID);
				arrNewIDs.put(jsonObj);
			}

			SharedPreferences.Editor edit = prefs.edit();
			edit.putString("notifid_array", arrNewIDs.toString());
			edit.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}


		return;
	}

	public static boolean isExistNotifActivityID(Context appContext, int nID) {
		ArrayList<Integer> arrNewPIDs = new ArrayList<Integer>();

		SharedPreferences prefs = appContext.getSharedPreferences("hiyou_userinfo", Context.MODE_PRIVATE);
		String szNewIDs = prefs.getString("notifid_array", "");

		try {
			if (!szNewIDs.equals("")) {
				JSONArray arrNewIDs = new JSONArray(szNewIDs);
				for (int i = 0; i < arrNewIDs.length(); i++) {
					JSONObject objItem = arrNewIDs.getJSONObject(i);
					arrNewPIDs.add(objItem.getInt("Value"));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return arrNewPIDs.contains(nID);
	}

	public static Bitmap getCroppedRoundBitmap(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);

		canvas.drawOval(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}
}


