package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Ruifeng Shi on 12/9/2016.
 */

public class KPHDateEditText extends KPHEditText {
	public static final int				DATE_FORMAT_YYYY_MM_DD		= 0;
	public static final int				DATE_FORMAT_MM_DD_YYYY		= 1;

	private int							dateFormat					= DATE_FORMAT_YYYY_MM_DD;
	private static final int			MIN_BIRTHYEAR_LIMIT			= 1900;


	public KPHDateEditText(Context context) {
		super(context);
		initialize();
	}

	public KPHDateEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public KPHDateEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	public void initialize() {
		setInputType(InputType.TYPE_CLASS_NUMBER);
		addTextChangedListener(new DateTextWatcher());
	}


	public int getDateFormat() {
		return dateFormat;
	}


	public void setDateFormat(int dateFormat) {
		this.dateFormat = dateFormat;
		if (dateFormat == DATE_FORMAT_YYYY_MM_DD) {
			this.setHint("YYYY / MM / DD");
		} else {
			this.setHint("MM / DD / YYYY");
		}
	}


	public int getYear() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf;

		switch (dateFormat) {
			default:
			case DATE_FORMAT_YYYY_MM_DD:
				sdf = new SimpleDateFormat("yyyy / MM / dd");
				break;

			case DATE_FORMAT_MM_DD_YYYY:
				sdf = new SimpleDateFormat("MM / dd / yyyy");
				break;
		}

		try {
			Date date = sdf.parse(getText().toString());
			calendar.setTime(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return calendar.get(Calendar.YEAR);
	}


	public int getMonth() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf;

		switch (dateFormat) {
			default:
			case DATE_FORMAT_YYYY_MM_DD:
				sdf = new SimpleDateFormat("yyyy / MM / dd");
				break;

			case DATE_FORMAT_MM_DD_YYYY:
				sdf = new SimpleDateFormat("MM / dd / yyyy");
				break;
		}

		try {
			Date date = sdf.parse(getText().toString());
			calendar.setTime(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return calendar.get(Calendar.MONTH);
	}


	public int getDayOfMonth() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf;

		switch (dateFormat) {
			default:
			case DATE_FORMAT_YYYY_MM_DD:
				sdf = new SimpleDateFormat("yyyy / MM / dd");
				break;

			case DATE_FORMAT_MM_DD_YYYY:
				sdf = new SimpleDateFormat("dd / MM / yyyy");
				break;
		}

		try {
			Date date = sdf.parse(getText().toString());
			calendar.setTime(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return calendar.get(Calendar.DAY_OF_MONTH);
	}


	public boolean isDateValid() {
		Calendar calendar = Calendar.getInstance();

		switch (dateFormat) {
			case DATE_FORMAT_YYYY_MM_DD: {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy / MM / dd");
				try {
					Date date = sdf.parse(getText().toString());
					calendar.setTime(date);
				} catch (ParseException e) {
					return false;
				}
				break;
			}

			case DATE_FORMAT_MM_DD_YYYY: {
				SimpleDateFormat sdf = new SimpleDateFormat("MM / dd / yyyy");
				try {
					Date date = sdf.parse(getText().toString());
					calendar.setTime(date);
				} catch (ParseException e) {
					return false;
				}
				break;
			}
		}

		if (calendar.get(Calendar.YEAR) < MIN_BIRTHYEAR_LIMIT) {
			return false;
		} else if (calendar.get(Calendar.YEAR) > Calendar.getInstance().get(Calendar.YEAR)) {
			return false;
		}

		return true;
	}


	public void updateDate(int year, int month, int dayOfMonth) {
		switch (dateFormat) {
			case DATE_FORMAT_YYYY_MM_DD:
				setText(String.format("%04d / %02d / %02d", year, month, dayOfMonth));
				break;

			case DATE_FORMAT_MM_DD_YYYY:
				setText(String.format("%02d / %02d / %04d", month, dayOfMonth, year));
				break;
		}
	}


	private class DateTextWatcher implements TextWatcher {
		private final String	INVALID_FORMAT = "Invalid number format";
		private String			current = "";
		private Calendar		cal = Calendar.getInstance();

		private int				currentYear, currentMonth, currentDay;

		private int[]			daysOfMonthsNormalYear = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
		private int[]			daysOfMonthsLeapYear = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};


		public DateTextWatcher() {
			super();

			Calendar calendar = Calendar.getInstance();
			currentYear = calendar.get(Calendar.YEAR);
			currentMonth = calendar.get(Calendar.MONTH) + 1;
			currentDay = calendar.get(Calendar.DAY_OF_MONTH);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			switch (KPHDateEditText.this.dateFormat) {
				case DATE_FORMAT_YYYY_MM_DD:
					maskYYYYMMDDFormat(s);
					break;

				case DATE_FORMAT_MM_DD_YYYY:
					maskMMDDYYYYFormat(s);
					break;
			}
		}

		@Override
		public void afterTextChanged(Editable s) {}

		private int[] getDaysOfMonths(int year) {
			if (year % 4 != 0) {
				return daysOfMonthsNormalYear;
			} else {
				return daysOfMonthsLeapYear;
			}
		}

		private void maskYYYYMMDDFormat(CharSequence s) {
			String text = s.toString();

			if (!text.equals(current)) {
				try {
					if (text.length() == 2) {
						int year = Integer.parseInt(text);
						boolean shouldAttachSlash = false;

						if (year != 19 && year != 20) {
							year = MIN_BIRTHYEAR_LIMIT + year;
							shouldAttachSlash = true;
						}

						if (shouldAttachSlash) {
							text = String.format("%d / ", year);
						} else {
							text = String.valueOf(year);
						}
					} else if (text.length() == 3) {
						int year = Integer.parseInt(text);

						if (year < MIN_BIRTHYEAR_LIMIT / 10 || year > 201) {
							text = text.substring(0, 2);
						}
					} else if (text.length() == 4) {
						int year = Integer.parseInt(text);
						if (year < MIN_BIRTHYEAR_LIMIT || year > currentYear) {
							text = text.substring(0, 3);
						} else {
							text = String.format("%s / ", text);
						}
					} else if (text.length() == 5) {
						// The same as the case when length 5
						text = text.substring(0, 4) + " / " + text.substring(4);

						int month = Integer.parseInt(text.substring(7));
						int year = Integer.parseInt(text.substring(0, 4));

						if (month > 1) {
							if (!(year == currentYear && month > currentMonth)) {
								text = String.format("%s%02d / ", text.substring(0, 7), month);
							} else {
								text = text.substring(0, 7);
							}
						}
					} else if (text.length() == 6) {    // When month is deleted
						text = text.trim().substring(0, 4);
					} else if (text.length() == 8) {    // When first digit of month is inputted
						int month = Integer.parseInt(text.substring(7));
						int year = Integer.parseInt(text.substring(0, 4));

						if (month > 1) {
							if (!(year == currentYear && month > currentMonth)) {
								text = String.format("%s%02d / ", text.substring(0, 7), month);
							} else {
								text = text.substring(0, 7);
							}
						}
					} else if (text.length() == 9) {    // When last digit of month is inputted
						int month = Integer.parseInt(text.substring(7));
						int year = Integer.parseInt(text.substring(0, 4));

						if (month > 0) {
							if (month <= 12 && !(year == currentYear && month > currentMonth)) {
								text = String.format("%s%02d / ", text.substring(0, 7), month);
							} else {
								int day = month % 10;
								month = month / 10;

								if (!(year == currentYear && month == currentMonth && day > currentDay)) {
									text = String.format("%s01 / %d", text.substring(0, 7), day);
								} else {
									text = text.substring(0, 8);
								}
							}
						} else {    // Ignore the last inputted digit if month is zero
							text = text.substring(0, 8);
						}
					} else if (text.length() == 10) {
						// The same as the case when length 13
						text = text.substring(0, 9) + " / " + text.substring(9);

						int day = Integer.parseInt(text.substring(12));
						int month = Integer.parseInt(text.substring(7, 9));
						int year = Integer.parseInt(text.substring(0, 4));

						if (month > 0) {
							int maxDay = getDaysOfMonths(year)[month - 1];

							if (year == currentYear && month == currentMonth && day > currentDay) {
								text = text.substring(0, 12);
							} else if (day > maxDay / 10) {
								if (day > 0) {
									day = Integer.parseInt(text.substring(12));
									text = String.format("%s%02d", text.substring(0, 12), day);
								}
							}
						}
					} else if (text.length() == 11) {   // When day is deleted
						text = text.trim().substring(0, 9);
					} else if (text.length() == 13) {   // When first digit of day is inputted
						int day = Integer.parseInt(text.substring(12));
						int month = Integer.parseInt(text.substring(7, 9));
						int year = Integer.parseInt(text.substring(0, 4));

						if (month > 0) {
							int maxDay = getDaysOfMonths(year)[month - 1];

							if (year == currentYear && month == currentMonth && day > currentDay) {
								text = text.substring(0, 12);
							} else if (day > maxDay / 10) {
								if (day > 0) {
									day = Integer.parseInt(text.substring(12));
									text = String.format("%s%02d", text.substring(0, 12), day);
								}
							}
						}
					} else if (text.length() == 14) {   // When last digit of day is inputted
						int day = Integer.parseInt(text.substring(12, 14));
						int month = Integer.parseInt(text.substring(7, 9));
						int year = Integer.parseInt(text.substring(0, 4));

						if (month > 0) {
							int maxDay = getDaysOfMonths(year)[month - 1];

							//Ignore if day inputted is invalid
							if (day > maxDay || (year == currentYear && month == currentMonth && day > currentDay)) {
								text = text.substring(0, 13);
							}
						}
					} else if (text.length() > 14) {
						text = text.substring(0, 14);
					}
				} catch (NumberFormatException ex) {
					// Unexpected error occurred. Clear the edit field
					ex.printStackTrace();
					text = "";
				}

				current = text;
				KPHDateEditText.this.setText(current);
				KPHDateEditText.this.setSelection(current.length());
			}
		}

		private void maskMMDDYYYYFormat(CharSequence s) {
			if (!s.toString().equals(current)) {
				String text = s.toString();

				try {
					if (text.length() == 1) {
						int monthStart = Integer.parseInt(text);
						if (monthStart > 1) {
							text = "0" + monthStart;
						}

//						int dayStart = Integer.parseInt(text);
//						if (dayStart > 3) {
//							text = "0" + dayStart;
//						}
					} else if (text.length() == 2) {
						int month = Integer.parseInt(text);
						int day = 0;

						if (month == 0) {
							text = text.substring(0, 1);
						} else if (month > 12) {
							String monthStr = text.substring(0, 1);
							String dayStr = text.substring(1, 2);

							day = Integer.parseInt(dayStr);
							month = Integer.parseInt(monthStr);

							if (day > 3) {
								text = "0" + month + " / " + "0" + day + " / ";
							} else {
								text = "0" + month + " / " + dayStr;
							}
						}

//						int month = 0;
//						int day = Integer.parseInt(text);
//						if (day > 31) {
//							String dayStr = text.substring(0, 1);
//							String monthStr = text.substring(1, 2);
//
//							day = Integer.parseInt(dayStr);
//							month = Integer.parseInt(monthStr);
//
//							if (month > 1) {
//								text = "0" + day + " / " + "0" + month + " / ";
//							} else {
//								text = "0" + day + " / " + month;
//							}
//						}
					} else if (text.length() == 3) {
						text = text.substring(0, 2) + " / " + text.substring(2);

						int separatorIndex1 = text.indexOf("/");

						String monthPartStr = text.substring(0, separatorIndex1).trim();
						String dayPartStr = text.substring(separatorIndex1 + 1).trim();

						int month = Integer.parseInt(monthPartStr);
						int day = Integer.parseInt(dayPartStr);

						if (day > 3) {
							text = monthPartStr + " / " + "0" + day + " / ";
						} else if (day == 3 && month == 2) {
							text = monthPartStr + " / " + "0" + day + " / ";
						} else {
							text = monthPartStr + " / " + dayPartStr;
						}

//						String dayPartStr = text.substring(0, separatorIndex1).trim();
//						String monthPartStr = text.substring(separatorIndex1 + 1).trim();
//
//						int day = Integer.parseInt(dayPartStr);
//						int month = Integer.parseInt(monthPartStr);
//
//						if (month >= 2) {
//							if (!isAvailableMonth4Day(day, month)) {
//								text = text.substring(0, 2);
//							} else {
//								monthPartStr = "0" + monthPartStr;
//								text = dayPartStr + " / " + monthPartStr;
//							}
//						} else {
//							text = dayPartStr + " / " + monthPartStr;
//						}
					} else if (text.length() == 5) {
						text = text.trim().substring(0, 2);
					} else if (text.length() == 6) {
						int separatorIndex1 = text.indexOf("/");

						String monthPartStr = text.substring(0, separatorIndex1).trim();
						String dayPartStr = text.substring(separatorIndex1 + 1).trim();

						int month = Integer.parseInt(monthPartStr);
						int day = Integer.parseInt(dayPartStr);

						if (day > 3) {
							text = monthPartStr + " / " + "0" + day + " / ";
						} else if (day == 3 && month == 2) {
							text = monthPartStr + " / " + "0" + day + " / ";
						} else {
							text = monthPartStr + " / " + dayPartStr;
						}

//						String dayPartStr = text.substring(0, separatorIndex1).trim();
//						String monthPartStr = text.substring(separatorIndex1 + 1).trim();
//
//						int day = Integer.parseInt(dayPartStr);
//						int month = Integer.parseInt(monthPartStr);
//
//						if (month >= 2) {
//							if (!isAvailableMonth4Day(day, month)) {
//								text = text.substring(0, 5);
//							}
//						}
					} else if (text.length() == 7) {
						int separatorIndex1 = text.indexOf("/");

						String monthPartStr = text.substring(0, separatorIndex1).trim();
						String dayPartStr = text.substring(separatorIndex1 + 1).trim();

						int month = Integer.parseInt(monthPartStr);
						int day = Integer.parseInt(dayPartStr);

						if (!isAvailableMonth4Day(day, month)) {
							text = monthPartStr + " / " + "0" + dayPartStr.substring(0, 1) + " / " + dayPartStr.substring(1, 2);
						}


//						String dayPartStr = text.substring(0, separatorIndex1).trim();
//						String monthPartStr = text.substring(separatorIndex1 + 1).trim();
//
//						int day = Integer.parseInt(dayPartStr);
//						int month = Integer.parseInt(monthPartStr);
//
//						if (month < 1 || month > 12) {
//							text = text.substring(0, 6);
//						} else {
//							if (month >= 2) {
//								if (!isAvailableMonth4Day(day, month)) {
//									text = text.substring(0, 6);
//								}
//							} else {
//								// Available for Jan, Nov, Dec
//								if (day == 31 && month == 11) {
//									text = text.substring(0, 6);
//								}
//							}
//						}
					} else if (text.length() == 8) {
						text = text.substring(0, 7) + " / " + text.substring(7);

						int separatorIndex1 = text.indexOf("/");
						int separatorIndex2 = text.indexOf("/", separatorIndex1 + 1);

						String monthPartStr = text.substring(0, separatorIndex1).trim();
						String dayPartStr = text.substring(separatorIndex1 + 1, separatorIndex2).trim();
						String yearPartStr = text.substring(separatorIndex2 + 1).trim();

						Integer.parseInt(dayPartStr);
						Integer.parseInt(monthPartStr);
						int year = Integer.parseInt(yearPartStr);

						if (year < 1) {
							text = text.substring(0, 11);
						} else if (year > 2) {
							yearPartStr = "19" + yearPartStr;
							text = monthPartStr + " / " + dayPartStr + " / " + yearPartStr;
						}


//						String dayPartStr = text.substring(0, separatorIndex1).trim();
//						String monthPartStr = text.substring(separatorIndex1 + 1, separatorIndex2).trim();
//						String yearPartStr = text.substring(separatorIndex2 + 1).trim();
//
//						Integer.parseInt(dayPartStr);
//						Integer.parseInt(monthPartStr);
//						int year = Integer.parseInt(yearPartStr);
//
//						if (year < 1) {
//							text = text.substring(0, 11);
//						} else if (year > 2) {
//							yearPartStr = "19" + yearPartStr;
//							text = dayPartStr + " / " + monthPartStr + " / " + yearPartStr;
//						}
					} else if (text.length() == 10) {
						text = text.trim().substring(0, 7);
					} else if (text.length() >= 11 && text.length() < 15) {
						int separatorIndex1 = text.indexOf("/");
						int separatorIndex2 = text.indexOf("/", separatorIndex1 + 1);

						String monthPartStr = text.substring(0, separatorIndex1).trim();
						String dayPartStr = text.substring(separatorIndex1 + 1, separatorIndex2).trim();
						String yearPartStr = text.substring(separatorIndex2 + 1).trim();

						int month = Integer.parseInt(monthPartStr);
						int day = Integer.parseInt(dayPartStr);
						int year = Integer.parseInt(yearPartStr);

						if (text.length() == 11) {
							if (year < 1) {
								text = text.substring(0, 11);
							} else if (year > 2) {
								text = text.substring(0, 10) + "19" + year;
							}
						} else if (text.length() == 12) {
							if (year != 19 && year != 20) {
								if (isAvailableYearForMonthDay(day, month, 1900 + year))
									text = text.substring(0, 10) + "19" + year;
								else
									text = text.substring(0, 11);
							}
						} else if (text.length() == 13) {
							if (year > Calendar.getInstance().get(Calendar.YEAR) / 10) {
								text = text.substring(0, 12);
							}
						} else if (text.length() == 14) {
							if (!isAvailableYearForMonthDay(day, month, year)) {
								text = text.substring(0, 13);
							} else if (year < MIN_BIRTHYEAR_LIMIT || year > Calendar.getInstance().get(Calendar.YEAR)) {
								text = text.substring(0, 13);
							} else {
								Calendar cal = Calendar.getInstance();
								cal.set(Calendar.DAY_OF_MONTH, day);
								cal.set(Calendar.MONTH, month - 1);
								cal.set(Calendar.YEAR, year);
								if (cal.after(Calendar.getInstance())) {
									text = text.substring(0, 13);
								}
							}
						}


//						String dayPartStr = text.substring(0, separatorIndex1).trim();
//						String monthPartStr = text.substring(separatorIndex1 + 1, separatorIndex2).trim();
//						String yearPartStr = text.substring(separatorIndex2 + 1).trim();
//
//						int day = Integer.parseInt(dayPartStr);
//						int month = Integer.parseInt(monthPartStr);
//						int year = Integer.parseInt(yearPartStr);
//
//						if (text.length() == 11) {
//							if (year < 1) {
//								text = text.substring(0, 11);
//							} else if (year > 2) {
//								text = text.substring(0, 10) + "19" + year;
//							}
//						} else if (text.length() == 12) {
//							if (year != 19 && year != 20) {
//								text = text.substring(0, 10) + "19" + year;
//							}
//						} else if (text.length() == 13) {
//							if (year > Calendar.getInstance().get(Calendar.YEAR) / 10) {
//								text = text.substring(0, 12);
//							}
//						} else if (text.length() == 14) {
//							if (year < MIN_BIRTHYEAR_LIMIT || year > Calendar.getInstance().get(Calendar.YEAR)) {
//								text = text.substring(0, 13);
//							} else {
//								Calendar cal = Calendar.getInstance();
//								cal.set(Calendar.DAY_OF_MONTH, day);
//								cal.set(Calendar.MONTH, month - 1);
//								cal.set(Calendar.YEAR, year);
//								if (cal.after(Calendar.getInstance())) {
//									text = text.substring(0, 13);
//								}
//							}
//						}
					} else {
						text = text.substring(0, text.length() - 1);
					}
				} catch (Exception ex) {
					// Unexpected error occurred. Clear the edit field
					ex.printStackTrace();
					text = "";
				}

				current = text;
				KPHDateEditText.this.setText(current);
				KPHDateEditText.this.setSelection(current.length());
			}
		}

		/**
		 * Method to check if the day is out of the available range of the month
		 *
		 * @param day		Day value
		 * @param month		Month value(start from 1)
		 * @return
		 */
		private boolean isAvailableMonth4Day(int day, int month) {
			boolean result = false;

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, 1984);				// Should be lunar year
			cal.set(Calendar.MONTH, month - 1);
			if (day < 1 || day > cal.getActualMaximum(Calendar.DAY_OF_MONTH))
				result = false;
			else
				result = true;

			return result;
		}

		private boolean isAvailableYearForMonthDay(int day, int month, int year) {
			boolean result = false;

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, month - 1);
			if (cal.getActualMaximum(Calendar.DAY_OF_MONTH) < day) {
				result = false;
			} else {
				result = true;
			}

			return result;
		}
	}
}
