package org.unicefkidpower.kid_power.Misc;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Dayong Li on 11/11/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class Logger {
	public static final String TAG = "KiHome";
	public static final String LOG_FILE = "kh.log";


	public static void log(String tag, String format, Object... args) {
		try {
			android.util.Log.w(TAG + " : " + tag, String.format(format, args));
			logToFile(tag, "OK", String.format(format, args));
		} catch (Exception e) {
		}
	}


	public static void error(String tag, String format, Object... args) {
		try {
			android.util.Log.e(TAG + " : " + tag, String.format(format, args));
			logToFile(tag, "ERROR", String.format(format, args));
		} catch (Exception e) {
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
