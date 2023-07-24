package org.unicefkidpower.schools.model;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.jaredrummler.android.device.DeviceName;

import org.unicefkidpower.schools.BuildConfig;
import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.context.UserContext;
import org.unicefkidpower.schools.define.KPConstants;
import org.unicefkidpower.schools.define.config;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.helper.OSDate;
import org.unicefkidpower.schools.powerband.command.CmdNameSet;
import org.unicefkidpower.schools.server.apimanage.TeamService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {
	private final static String TAG = "Utils";
	public static final long LOG_FILE_MAX_LENGTH = 10 * 1024 * 1024;			// 10 MB
	public static final int VALUE_LENGTH = 9;

	public static float parseHeight(String height) {
		return Float.parseFloat(height);
	}

	public static float parseWeight(String weight) {
		return Float.parseFloat(weight);
	}

	public static float parseStride(String stride) {
		return Float.parseFloat(stride);
	}

	public static float parseMiles(String miles) {
		if (miles == null)
			return 0.f;

		return Float.parseFloat(miles);
	}

	public static int parseSteps(String steps) {
		return Integer.parseInt(steps);
	}

	public static int parsePackets(String packets) {
		return Integer.parseInt(packets);
	}

	public static int parsePowerPoints(String powerPoints) {
		return Integer.parseInt(powerPoints);
	}

	public static String parsePacketWithCurrencyFormat(int packet) {
		String szPacket = "";

		DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
		if (packet != 0) {
			String szBuf = Integer.toString(packet);
			if (szBuf.length() >= 10) {
				szPacket = decimalFormat.format(packet / 1000) + "K";
			} else {
				szPacket = decimalFormat.format(packet);
			}
		} else {
			szPacket = "-";
		}

		return szPacket;
	}

	public static String parsePowerpointWithCurrencyFormat(int powerpoint) {
		String szPowerPoint = "";

		DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
		if (powerpoint != 0) {
			String szBuf = Integer.toString(powerpoint);
			if (szBuf.length() >= VALUE_LENGTH) {
				szPowerPoint = decimalFormat.format(powerpoint / 1000) + "K";
			} else {
				szPowerPoint = decimalFormat.format(powerpoint);
			}
		} else {
			szPowerPoint = "-";
		}

		return szPowerPoint;
	}

	public static String parseStepWithCurrencyFormat(int step) {
		String szStep = "";

		DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
		if (step != 0) {
			String szBuf = Integer.toString(step);
			if (szBuf.length() >= VALUE_LENGTH) {
				szStep = decimalFormat.format(step / 1000) + "K";
			} else {
				szStep = decimalFormat.format(step);
			}
		} else {
			szStep = "-";
		}

		return szStep;
	}

	public static int getAverageStepsPerDay(TeamService.ResTeamSummary teamSummary) {
		if (teamSummary.totalDays > 0) {
			return teamSummary.totalSteps / teamSummary.totalDays;
		} else {
			return teamSummary.totalSteps;
		}
	}

	public static String parseMileWithCurrencyFormat(float mile) {
		String szMile = "";

		DecimalFormat decimalFormat = new DecimalFormat("###,###,###.#");
		if (mile != 0) {
			String szBuf = Integer.toString((int) mile);
			if (szBuf.length() >= VALUE_LENGTH) {
				szMile = decimalFormat.format(mile / 1000) + "K";
			} else {
				szMile = decimalFormat.format(mile);
			}
		} else {
			szMile = "-";
		}

		return szMile;
	}

	public static String parseKilometerWithCurrencyFormatFromMile(float mile) {
		String szKilometer = "";

		DecimalFormat decimalFormat = new DecimalFormat("###,###,###.#");

		float kilometer = (float) (mile * 1.60934);
		if (kilometer != 0) {
			String szBuf = Integer.toString((int) kilometer);
			if (szBuf.length() >= VALUE_LENGTH) {
				szKilometer = decimalFormat.format(kilometer / 1000) + "K";
			} else {
				szKilometer = decimalFormat.format(kilometer);
			}
		} else {
			szKilometer = "-";
		}

		return szKilometer;
	}

	public static String parseMileWithCurrencyFormat(String strMile) {
		String szMile = "";
		float mile = parseMiles(strMile);

		DecimalFormat decimalFormat = new DecimalFormat("###,###,###.#");
		if (mile != 0) {
			String szBuf = Integer.toString((int) mile);
			if (szBuf.length() >= VALUE_LENGTH) {
				szMile = decimalFormat.format(mile / 1000) + "K";
			} else {
				szMile = decimalFormat.format(mile);
			}
		} else {
			szMile = "-";
		}

		return szMile;
	}

	public static String parseKilometerWithCurrencyFormatFromMile(String strMile) {
		String szMile = "";
		float kilometer = (float) (parseMiles(strMile) * 1.60934);

		DecimalFormat decimalFormat = new DecimalFormat("###,###,###.#");
		if (kilometer != 0) {
			String szBuf = Integer.toString((int) kilometer);
			if (szBuf.length() >= VALUE_LENGTH) {
				szMile = decimalFormat.format(kilometer / 1000) + "K";
			} else {
				szMile = decimalFormat.format(kilometer);
			}
		} else {
			szMile = "-";
		}

		return szMile;
	}

	public static Date parseStartDate(String startDate) {
		return OSDate.fromStringWithFormat(startDate, KPConstants.FORMAT_JSON_DATETIME);
	}

	public static Date parseEndDate(String endDate) {
		return OSDate.fromStringWithFormat(endDate, KPConstants.FORMAT_JSON_DATETIME);
	}

	public static Date parseUpdatedAt(String updatedAt) {
		return OSDate.fromStringWithFormat(updatedAt, KPConstants.FORMAT_JSON_DATETIME);
	}

	public static Date parseCreatedAt(String createdAt) {
		return OSDate.fromStringWithFormat(createdAt, KPConstants.FORMAT_JSON_DATETIME);
	}

	public static Date parseLastSyncDateDetail(String lastSyncDateDetail) {
		return OSDate.fromStringWithFormat(lastSyncDateDetail, KPConstants.FORMAT_DATE);
	}

	public static Date parseLastSyncDateSummary(String lastSyncDateSummary) {
		return OSDate.fromStringWithFormat(lastSyncDateSummary, KPConstants.FORMAT_DATE);
	}

	public static String toJsonStringWithDate(Date dt) {
		if (dt == null)
			return "";
		OSDate date = new OSDate(dt);
		String strRet = date.toStringWithFormat(KPConstants.FORMAT_JSON_DATETIME);
		return strRet;
	}

	static String _getStudentName(String studentName) {
		if (studentName == null)
			return "";

		String szName = "", orgName = "";
		orgName = studentName.trim();
		//orgName = orgName.toUpperCase();
		orgName = orgName.replace("ñ", "n");
		orgName = orgName.replace("Ñ", "N");

		for (int i = 0; i < orgName.length(); i++) {
			char ch = orgName.charAt(i);
			if (Character.isLetter(ch)
					|| Character.isSpaceChar(ch)
					|| Character.isDigit(ch)
					|| ch == ' '
					|| ch == '-'
				// || ch == '.'
				// || ch == '\''
					) {
				szName += orgName.charAt(i);
			}
		}
		// limit 12 characters only
		int max_length = CmdNameSet.NAME_LENGTH;

		if (szName.length() > max_length)
			return szName.substring(0, max_length);
		else
			return szName;
	}

	public static String getStudentName(String studentName) {
		return _getStudentName(studentName);
	}

	public static String getIMSINumber(Context context) {
		try {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			return telephonyManager.getSubscriberId();
		} catch (Exception ex) {
			return "";
		}
	}

	public static String getICCIDNumber(Context context) {
		try {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			return telephonyManager.getSimSerialNumber();
		} catch (Exception ex) {
			return "";
		}
	}

	public static String getUDID(Context context) {
		try {
			String id = android.provider.Settings.System.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
			return id;
		} catch (Exception ex) {
			return "";
		}
	}

	public static String getIMEINumber(Context context) {
		try {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			return telephonyManager.getDeviceId();
		} catch (Exception ex) {
			return "";
		}
	}

	public static String getDeviceModel() {
		String manufacture = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model == null)
			model = "";

		return capitalize(manufacture) + " " + model;
	}

	public static String getOperatingSystem(Context context) {
		String result = "";

		result = Build.VERSION.RELEASE + ", " + Build.VERSION.SDK_INT;

		return result;
	}


	public static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	public static String getVersionName(Context context) {
		PackageInfo pInfo;
		try {
			pInfo = context.getApplicationContext().getPackageManager().getPackageInfo(context.getApplicationContext().getPackageName(), 0);
			return pInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String getVersionCode(Context context) {
		PackageInfo pInfo;
		try {
			pInfo = context.getApplicationContext().getPackageManager().getPackageInfo(context.getApplicationContext().getPackageName(), 0);
			return String.valueOf(pInfo.versionCode);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}


	private static boolean compositingNow = false;

	public static boolean sendMessage(Activity context, boolean addLog) {
		if (context == null)
			return false;

		if (compositingNow)
			return true;
		compositingNow = true;

		String subject = "";
		String username = UserContext.sharedInstance().lastUserName();
		if (username.length() > 0) {
			try {
				subject = context.getString(R.string.kid_power_feedback_from) + " " + username +
						String.format(" (%s for Android)",
								context.getPackageManager().getPackageInfo(
										context.getPackageName(), 0
								).versionName
						);
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			try {
				subject = String.format(
						context.getString(R.string.feedback_email_subject),
						context.getPackageManager().getPackageInfo(
								context.getPackageName(), 0
						).versionName
				);
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
		}

		if (subject == null || subject.trim().length() == 0) {
			subject = context.getString(R.string.kid_power_feedback);
		}


		String body = context.getString(
				R.string.feedback_email_body_greetings,
				DeviceName.getDeviceName(),
				Build.VERSION.RELEASE
		);

		File zipFile = null;
		if (addLog) {
			// check STORAGE permission
			if (ContextCompat.checkSelfPermission(context,
					Manifest.permission.WRITE_EXTERNAL_STORAGE)
					!= PackageManager.PERMISSION_GRANTED) {
				compositingNow = false;
				return false;
			}

			zipFile = createLogFile();
			if (zipFile == null) {
				return false;
			}
		}


		// send file using email
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
		emailIntent.setData(Uri.parse("mailto:" + KPConstants.HELP_CENTER_EMAIL));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, body);
		if (zipFile != null) {
			Uri uri = Uri.fromFile(zipFile);
			emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
		}

		context.startActivity(emailIntent);
		compositingNow = false;

		return true;
	}

	public static File getLogFile() {
		File zipFile = null;

		zipFile = createLogFile();
		if (zipFile == null) {
			return null;
		} else {
			return zipFile;
		}
	}

	protected static File createLogFile() {
		try {
			removeLogFile();

			String file_name;
			OSDate date = new OSDate();

			file_name = "[" + date.toStringWithFormat("yyyy-MM-dd") + "]";
			file_name += "KidPowerSchool version" + BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")";
			file_name = file_name.replace(' ', '_');

			File logFile = new File(Environment.getExternalStorageDirectory(), Logger.LOG_FILE);
			File outputFile = new File(Environment.getExternalStorageDirectory(), file_name + ".log");
			File zipFile = new File(Environment.getExternalStorageDirectory(), file_name + ".zip");

			if (logFile.exists()) {
				copyFile(logFile, outputFile);
				zip(outputFile.getAbsolutePath(), zipFile.getAbsolutePath());
			}

			return zipFile;
		} catch (IOException e) {
			Log.e("SEND_LOG", "createLogFile: exception occupation");
			e.printStackTrace();
			return null;
		}
	}

	protected static void copyFile(File logFile, File outputFile) throws IOException {
		String line;

		BufferedReader reader = new BufferedReader(new FileReader(logFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

		OSDate now = new OSDate(new Date());

		line = String.format("  Report Date : %s", now.toStringWithFormat("yyyy-MM-dd HH:mm:ss"));
		writer.write(line + "\r\n");
		line = String.format("  Manufacturer : %s(%s)", Build.MANUFACTURER, Build.MODEL);
		writer.write(line + "\r\n");
		line = String.format("  Device : %s(%s)", Build.PRODUCT, Build.DEVICE);
		writer.write(line + "\r\n");
		line = String.format("  Production : %s", (config.IS_PRODUCT ? "Yes" : "No"));
		writer.write(line + "\r\n");

		line = String.format("  SDK : %s, OS : %s(%s)", Build.VERSION.SDK_INT, Build.VERSION.RELEASE, Build.VERSION.CODENAME);
		writer.write(line + "\r\n");

		while ((line = reader.readLine()) != null) {
			writer.write(line + "\r\n");
		}

		reader.close();
		writer.flush();
		writer.close();
	}

	protected static void removeLogFile() {
		File dir = new File(Environment.getExternalStorageDirectory() + "");
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				if (children[i].contains("KidPowerSchool_version")) {
					new File(dir, children[i]).delete();
				}
			}
		}
	}

	protected static void CopyStream(String inputFile, String outputFile) throws IOException {
		File tmpFile = new File(Environment.getExternalStorageDirectory(),
				outputFile + ".log");

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));

		// write device & os information to help bug fixing
		String line;

		OSDate now = new OSDate(new Date());

		line = String.format("  Report Date : %s", now.toStringWithFormat("yyyy-MM-dd HH:mm:ss"));
		writer.write(line + "\r\n");
		line = String.format("  Manufacturer : %s(%s)", Build.MANUFACTURER, Build.MODEL);
		writer.write(line + "\r\n");
		line = String.format("  Device : %s(%s)", Build.PRODUCT, Build.DEVICE);
		writer.write(line + "\r\n");
		line = String.format("  Production : %s", (config.IS_PRODUCT ? "Yes" : "No"));
		writer.write(line + "\r\n");

		line = String.format("  SDK : %s, OS : %s(%s)", Build.VERSION.SDK_INT, Build.VERSION.RELEASE, Build.VERSION.CODENAME);
		writer.write(line + "\r\n");

		writer.write("\r\n");

		boolean isRetrofit, isApp;
		// copy & filter app's log
		while ((line = reader.readLine()) != null) {
			isRetrofit = line.contains("Retrofit");
			if (isRetrofit) {
				if (!line.contains("--->") && !line.contains("\":\"") && !line.contains("<---"))
					continue;

				if (line.contains("password"))
					continue;
			} else {
				isApp = line.contains(Logger.TAG);
				if (!isApp)
					continue;
			}

			writer.write(line + "\r\n");
		}

		reader.close();
		writer.flush();
		writer.close();


		// File length limitation
		long resultSize = tmpFile.length();
		if (resultSize > LOG_FILE_MAX_LENGTH) {
			File tmpFile2 = new File(Environment.getExternalStorageDirectory(),
					outputFile + "2.log");
			if (tmpFile2.exists())
				tmpFile2.delete();

			reader = new BufferedReader(new FileReader(tmpFile));
			writer = new BufferedWriter(new FileWriter(tmpFile2));

			// Skip stream
			long sizeToSkip = resultSize - LOG_FILE_MAX_LENGTH;
			while (sizeToSkip > 0) {
				long skipStepSize = reader.skip(sizeToSkip);
				sizeToSkip -= skipStepSize;
			}

			while ((line = reader.readLine()) != null) {
				writer.write(line + "\r\n");
			}

			reader.close();
			writer.flush();
			writer.close();

			tmpFile.delete();

			// Move file to tmpFile again.
			reader = new BufferedReader(new FileReader(tmpFile2));
			writer = new BufferedWriter(new FileWriter(tmpFile));

			while ((line = reader.readLine()) != null) {
				writer.write(line + "\r\n");
			}

			reader.close();
			writer.flush();
			writer.close();

			tmpFile2.delete();

			Logger.log(TAG, "File created successfully : " + tmpFile.length());
		}
	}


	protected static void zip(String _origin, String _zipFile) {
		int BUFFER = 2048;
		try {
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(_zipFile);

			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

			byte data[] = new byte[BUFFER];

			Log.v("Compress", "Adding: " + _origin);
			FileInputStream fi = new FileInputStream(_origin);
			origin = new BufferedInputStream(fi, BUFFER);
			ZipEntry entry = new ZipEntry(_origin.substring(_origin.lastIndexOf("/") + 1));
			out.putNextEntry(entry);
			int count;
			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			origin.close();

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
