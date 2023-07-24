package org.unicefkidpower.kid_power.Misc;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Dayong Li on 11/11/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class OSDate extends Date {
	public OSDate() {
		super();
	}

	public OSDate(Date d) {
		super(d.getTime());
	}

	public OSDate offsetDay(int days) {
		if (days == 0)
			return this;
		Calendar cal = Calendar.getInstance();
		cal.setTime(this);
		cal.add(Calendar.DAY_OF_YEAR, days);
		Date dt = cal.getTime();
		return new OSDate(dt);
	}

	public OSDate offsetMonth(int months) {
		if (months == 0)
			return this;
		Calendar cal = Calendar.getInstance();
		cal.setTime(this);
		cal.add(Calendar.MONTH, months);
		Date dt = cal.getTime();
		return new OSDate(dt);
	}

	public OSDate offsetYear(int years) {
		if (years == 0)
			return this;
		Calendar cal = Calendar.getInstance();
		cal.setTime(this);
		cal.add(Calendar.YEAR, years);
		Date dt = cal.getTime();
		return new OSDate(dt);
	}

	public OSDate offsetByHHMMSS(int hours, int minutes, int seconds) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(this);
		if (hours != 0) {
			cal.add(Calendar.HOUR_OF_DAY, hours);
		}
		if (minutes != 0) {
			cal.add(Calendar.MINUTE, minutes);
		}
		if (seconds != 0) {
			cal.add(Calendar.SECOND, seconds);
		}
		Date dt = cal.getTime();

		return new OSDate(dt);
	}

	public int compareWithoutHour(Date dt) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(this);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		cal.setTime(dt);
		int year2 = cal.get(Calendar.YEAR);
		int month2 = cal.get(Calendar.MONTH);
		int day2 = cal.get(Calendar.DAY_OF_MONTH);

		if (year == year2) {
			if (month == month2) {
				if (day == day2)
					return 0;
				else if (day < day2)
					return -1;
				else
					return 1;
			} else if (month < month2)
				return -1;
			else
				return 1;
		} else if (year < year2)
			return -1;
		else
			return 1;
	}

	public static Calendar getDatePart(Date date) {
		Calendar cal = Calendar.getInstance();       // getV1 calendar instance
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
		cal.set(Calendar.MINUTE, 0);                 // set minute in hour
		cal.set(Calendar.SECOND, 0);                 // set second in minute
		cal.set(Calendar.MILLISECOND, 0);            // set millisecond in second

		return cal;                                  // return the date part
	}

	/**
	 * This method also assumes endDate >= startDate
	 **/
	public static long daysBetween(Date startDate, Date endDate) {
		Calendar sDate = getDatePart(startDate);
		Calendar eDate = getDatePart(endDate);

		long daysBetween = 0;
		while (sDate.before(eDate)) {
			sDate.add(Calendar.DAY_OF_MONTH, 1);
			daysBetween++;
		}
		return daysBetween;
	}

	// Get difference of day between two date values
	public static int getDiffDay(Date dt_start, Date dt_end) {
		if (dt_start == null || dt_end == null)
			return -1;

		Calendar cal_start = Calendar.getInstance();
		Calendar cal_end = Calendar.getInstance();
		cal_start.setTime(dt_start);
		cal_end.setTime(dt_end);

		cal_start.set(Calendar.MINUTE, 0);
		cal_start.set(Calendar.SECOND, 0);
		cal_start.set(Calendar.HOUR, 0);
		cal_start.set(Calendar.MILLISECOND, 0);

		cal_end.set(Calendar.MINUTE, 0);
		cal_end.set(Calendar.SECOND, 0);
		cal_end.set(Calendar.HOUR, 0);
		cal_end.set(Calendar.MILLISECOND, 0);


		long nHours = 0;
		long end_millis = cal_end.getTimeInMillis();
		long start_millis = cal_start.getTimeInMillis();

		long diff = end_millis - start_millis;
		nHours = diff / 1000 / 60 / 60;

		int nDay = (int) (nHours / 24);

		return nDay;
	}

	public static int daysBetweenDates(Date dt_start, Date dt_end) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt_start);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		cal.setTime(dt_end);
		int year1 = cal.get(Calendar.YEAR);
		int month1 = cal.get(Calendar.MONTH);
		int day1 = cal.get(Calendar.DAY_OF_MONTH);

		if (year == year1 && month == month1 && day == day1)
			return 0;

		return getDiffDay(dt_start, dt_end);
	}

	/**
	 * convert string to Date
	 *
	 * @param szTime   date string ex) 2015-01-02
	 * @param szFormat format of szTime ex) yyyy-MM-dd
	 * @return null if szTime is invalid, true when szTime is valid
	 */
	@SuppressLint("SimpleDateFormat")
	public static Date fromStringWithFormat(String szTime, String szFormat, boolean isUTC) {
		if (szTime == null || szTime.equals(""))
			return null;

		DateFormat df = null;
		Date dtValue = null;

		try {
			df = new SimpleDateFormat(szFormat);
			if (isUTC)
				df.setTimeZone(TimeZone.getTimeZone("UTC"));
			dtValue = df.parse(szTime);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return dtValue;
	}

	/**
	 * convert string to Date
	 *
	 * @param szTime date string ex) 2015-01-02
	 * @return null if szTime is invalid, true when szTime is valid
	 */
	@SuppressLint("SimpleDateFormat")
	public static Date fromString(String szTime) {
		return OSDate.fromStringWithFormat(szTime, "yyyy-MM-dd'T'HH:mm:ss.SSS", false);
	}

	/**
	 * convert string to Date
	 *
	 * @param szTime date string ex) 2015-01-02
	 * @return null if szTime is invalid, true when szTime is valid
	 */
	@SuppressLint("SimpleDateFormat")
	public static Date fromUTCString(String szTime) {
		return OSDate.fromStringWithFormat(szTime, "yyyy-MM-dd'T'HH:mm:ss.SSS", true);
	}


	// Date to String conversion
	public String toStringWithFormat(String format) {
		String szResult = "";

		DateFormat df = null;
		df = new SimpleDateFormat(format);
		szResult = df.format(this);
		return szResult;
	}

	/**
	 * @return
	 * @brief Get years passed until now. Used to calculate ages
	 */
	public int getYearsPassed() {
		Calendar calendar = Calendar.getInstance();
		int year, month, day, age;
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH);
		day = calendar.get(Calendar.DAY_OF_MONTH);

		calendar.setTime(this);

		age = year - calendar.get(Calendar.YEAR);
		if ((month < calendar.get(Calendar.MONTH))
				|| ((month == calendar.get(Calendar.MONTH)) && (day < calendar.get(Calendar.DAY_OF_MONTH)))) {
			--age;
		}

		if (age < 0)
			age = 0;

		return age;
	}

	public String getDateOfBirth() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(this);

		return String.format(
				"%04d-%02d-%02d",
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH)
		);
	}
}
