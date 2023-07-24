package org.unicefkidpower.schools.helper;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SuppressWarnings("serial")
public class OSDate extends Date {
	public OSDate() {
		super();
	}

	public OSDate(Date d) {
		super(d.getTime());
	}

	public static Calendar getDatePart(Date date) {
		Calendar cal = Calendar.getInstance();       // get calendar instance
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
		return getDiffDay(dt_start, dt_end);
	}

	public static long betweenDates(Date dt_start, Date dt_end) {
		if (dt_start == null || dt_end == null)
			return -1;

		Calendar cal_start = Calendar.getInstance();
		Calendar cal_end = Calendar.getInstance();
		cal_start.setTime(dt_start);
		cal_end.setTime(dt_end);

		long end_millis = cal_end.getTimeInMillis();
		long start_millis = cal_start.getTimeInMillis();

		long diff = end_millis - start_millis;
		return diff;
	}

	/**
	 * convert string to Date
	 *
	 * @param szTime   date string ex) 2015-01-02
	 * @param szFormat format of szTime ex) yyyy-MM-dd
	 * @return null if szTime is invalid, true when szTime is valid
	 */
	@SuppressLint("SimpleDateFormat")
	public static Date fromStringWithFormat(String szTime, String szFormat) {
		if (szTime == null || szTime.equals(""))
			return null;

		DateFormat df = null;
		Date dtValue = null;

		try {
			df = new SimpleDateFormat(szFormat);
			dtValue = df.parse(szTime);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return dtValue;
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

	// Date to String conversion
	public String toStringWithFormat(String format) {
		String szResult = "";

		DateFormat df = null;
		df = new SimpleDateFormat(format);
		szResult = df.format(this);
		return szResult;
	}

	public int Year() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(this);
		return cal.get(Calendar.YEAR);
	}

	public int Month() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(this);
		return cal.get(Calendar.MONTH);
	}

	public String MonthString() {
		int month = Month();
		return getMonthString(month);
	}

	public int DayOfMonth() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(this);
		return cal.get(Calendar.DAY_OF_MONTH);
	}

	public String DayString() {
		int day = DayOfMonth();
		int remain = day % 10;

		String tail;
		if (remain == 1) {
			tail = "st";
		} else if (remain == 2) {
			tail = "nd";
		} else if (remain == 3) {
			tail = "rd";
		} else {
			tail = "th";
		}

		return day + tail;
	}

	static String getMonthString(int month) {
		switch (month) {
			case 0:
				return "Jan";
			case 1:
				return "Feb";
			case 2:
				return "Mar";
			case 3:
				return "Apr";
			case 4:
				return "May";
			case 5:
				return "Jun";
			case 6:
				return "Jul";
			case 7:
				return "Aug";
			case 8:
				return "Sep";
			case 9:
				return "Oct";
			case 10:
				return "Nov";
			case 11:
				return "Dec";
			default:
				return "";
		}
	}

}
