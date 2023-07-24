package org.unicefkidpower.schools.helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import org.unicefkidpower.schools.BuildConfig;
import org.unicefkidpower.schools.UIManager;
import org.unicefkidpower.schools.model.User;
import org.unicefkidpower.schools.model.UserManager;

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
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.unicefkidpower.schools.define.KPConstants.HELP_CENTER_EMAIL;

public class Logger {
	public static final String			TAG				= "kSchool";
	public static final String			LOG_FILE		= "kschools.log";


	public static void log(String tag, String format, Object... args) {
		try {
			android.util.Log.w(TAG + ": " + tag, String.format(format, args));
			logToFile(tag, "OK", String.format(format, args));
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	public static void error(String tag, String format, Object... args) {
		try {
			android.util.Log.e(TAG + ": " + tag, String.format(format, args));
			logToFile(tag, "ERROR", String.format(format, args));
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}


	//public static final String HELP_CENTER_EMAIL = "dayong@caloriecloud.org";//"hello@unicefkidpower.org";
	private static boolean isSendingEMail = false;

	public static void sendLogWithEMail(final Context context) {
		if (isSendingEMail || context == null)
			return;

		Logger.log("LOG_EMAIL", "\nSending log...###########.......#########.........\n");
		Handler handler = new Handler(context.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				sendLogCat(context);
			}
		});
	}

	protected static void sendLogCat(Context context) {
		if (context == null)
			return;

		Logger.log("LOG_EMAIL", "\nSending log Cat ...--------------.......--------------.........\n");

		String file_name;
		try {
			UIManager.sharedInstance().showProgressDialog(context, null, "Sending Log", true);
		} catch (Exception e) {
			Logger.error("SendLog", "Cannot create Progress Dialog");
			return;
		}
		isSendingEMail = true;

		User user = UserManager.sharedInstance()._currentUser;

		OSDate date = new OSDate();
		file_name = date.toStringWithFormat("yyyy-MM-dd") + " ";
		file_name += "KidPower School Ver" + BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")";
		if (user != null) {
			file_name += " From " + user._email;
		}
		file_name += Build.MODEL;
		file_name = file_name.replace(' ', '_');

		// save logcat in file
		File orgFile = new File(Environment.getExternalStorageDirectory(), LOG_FILE);
		File outputFile = new File(Environment.getExternalStorageDirectory(), file_name + ".log");
		File zipFile = new File(Environment.getExternalStorageDirectory(), file_name + ".zip");

		try {
			String command = "logcat -v time -f " + orgFile.getAbsolutePath();
			//String command = "logcat | grep --line-buffered " + TAG + ">" + orgFile.getAbsolutePath();
			Runtime.getRuntime().exec(command);

			CopyStream(orgFile.getAbsolutePath(), file_name);
			zip(outputFile.getAbsolutePath(), zipFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		String subject = "Kid Power School feedback ";
		try {
			subject += user != null ? ("from " + user._email) : "";
			subject += String.format(" (%s for Android)", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String body = String.format("\n%s %s / Android(%s)\n",
				Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE);

		//send file using email
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
		emailIntent.setData(Uri.parse("mailto:" + HELP_CENTER_EMAIL));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, body);
		Uri uri = Uri.fromFile(zipFile);
		emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
		context.startActivity(emailIntent);

		isSendingEMail = false;
		UIManager.sharedInstance().dismissProgressDialog();
	}


	private static void CopyStream(String inputFile, String outputFile) throws IOException {
		File tmpFile = new File(Environment.getExternalStorageDirectory(), outputFile + ".log");

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

		line = String.format("  SDK : %s, OS : %s(%s)", Build.VERSION.SDK_INT, Build.VERSION.RELEASE, Build.VERSION.CODENAME);
		writer.write(line + "\r\n");

		writer.write("\r\n");

		boolean isRetrofit;
		// copy & filter app's log
		while ((line = reader.readLine()) != null) {
			isRetrofit = line.contains("Retrofit");
			if (isRetrofit) {
				if (!line.contains("--->") && !line.contains("\":\"") && !line.contains("<---"))
					continue;

				if (line.contains("password"))
					continue;
			} else {
				boolean isApp;
				isApp = line.contains(TAG);

				boolean isBle = false;
				//isBle = line.contains("Gatt");
				if (!isApp && !isBle)
					continue;
			}

			writer.write(line + "\r\n");
		}

		reader.close();
		writer.flush();
		writer.close();
	}

	public static void zip(String _origin, String _zipFile) {
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


	private static void logToFile(String tag, String type, String format) {
		try {
			// Gets the log file from the root of the primary storage. If it does
			// not exist, the file is created.
			File logFile = new File(Environment.getExternalStorageDirectory(), LOG_FILE);
			if (!logFile.exists()) {
				logFile.createNewFile();
			} else {
				if (logFile.length() > 50 * 1024 * 1024) {
					// File is bigger than 50MB
					logFile.delete();
					logFile.createNewFile();
				}
			}


			// Write the message to the log with a timestamp
			OSDate date = new OSDate();
			BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
			writer.write(String.format("%s [%s(%s)] : %s\r\n", date.toStringWithFormat("yyyy-MM-dd HH:mm:ss.SSS"), tag, type, format));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
