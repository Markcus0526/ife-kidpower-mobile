package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONObject;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.OSDate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Dayong Li on 11/11/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class KPHDailyDetailData {
	private static final String 	TAG = "KPHDailyDetailData";

	static final String			DateFormatString		= "yyyy-MM-dd";
	static final String			UTCDateFormatString		= "yyyy-MM-dd'T'HH:mm:ss'Z'";
	static SimpleDateFormat		formatter				= new SimpleDateFormat(DateFormatString);

	@SerializedName("date")
	public String date;

	@SerializedName("data")
	public List<DetailItem>		data;

	public transient double		totalCalories;
	public transient double		totalDuration;
	public transient int		totalSteps;
	public transient int		totalPowerPoints;



	public KPHDailyDetailData() {
		Date now = new Date();

		date = formatter.format(now);
		data = new ArrayList<>();
		totalCalories = 0;
		totalDuration = 0;
		totalSteps = 0;
		totalPowerPoints = 0;
	}

	public void addDetailItem(DetailItem item) {
		if (data == null) {
			data = new ArrayList<>();
		}
		data.add(item);
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setDate(Date when) {
		date = formatter.format(when);
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public void calculateTotals() {
		totalCalories = 0;
		totalDuration = 0;
		totalSteps = 0;

		if (data == null)
			return;

		for (DetailItem item : data) {
			totalCalories += item.calories;
			totalSteps += item.steps;
			totalDuration += item.duration;
		}
	}

	public void calcPPbyDate(Date filter) {
		totalCalories = 0;
		totalDuration = 0;
		totalSteps = 0;

		if (data == null)
			return;

		for (DetailItem item : data) {
			Date dateItem = OSDate.fromStringWithFormat(item.getDate(), UTCDateFormatString, true);

			if (filter == null || dateItem.after(filter)) {
				totalCalories += item.calories;
				totalDuration += item.duration;
				totalSteps += item.steps;
			}
		}

		totalPowerPoints = (int) (totalCalories / 50.0);
	}

	// important function to calculate new calories and new power points
	public static KPHDailyDetailData calculateCalories(List<KPHDailyDetailData> activities) {
		KPHDailyDetailData filterData = new KPHDailyDetailData();

		if (activities == null) {
			return filterData;
		}

		for (KPHDailyDetailData dailyDetailData : activities) {
			dailyDetailData.calculateTotals();
			filterData.totalSteps += dailyDetailData.totalSteps;
			filterData.totalDuration += dailyDetailData.totalDuration;
			filterData.totalCalories += dailyDetailData.totalCalories;
		}

		return filterData;
	}

	// important function to calculate new calories and new power points
	public static KPHDailyDetailData filterCalories(List<KPHDailyDetailData> activities, Date baseDate) {
		KPHDailyDetailData filterData = new KPHDailyDetailData();

		if (activities == null) {
			return filterData;
		}

		Logger.log(TAG, "filterCalories : last sync date=%s", baseDate != null ? baseDate.toString() : "null, Not filter");
		for (KPHDailyDetailData dailyData : activities) {
			dailyData.calcPPbyDate(baseDate);
			filterData.totalSteps += dailyData.totalSteps;
			filterData.totalDuration += dailyData.totalDuration;
			filterData.totalCalories += dailyData.totalCalories;
			filterData.totalPowerPoints += dailyData.totalPowerPoints;

			Logger.log(TAG, "filterCalories : %s filtered data : calories=%.2f, distance=%.2f, steps=%d, powerpoints=%d",
					dailyData.date, dailyData.totalCalories, dailyData.totalDuration,
					dailyData.totalSteps, dailyData.totalPowerPoints);
		}

		Logger.log(TAG, "filterCalories : Total Calories=%.2f, Total Distance=%.2f, Total Steps=%d, new earned powerpoints=%d",
				filterData.totalCalories, filterData.totalDuration, filterData.totalSteps, filterData.totalPowerPoints);
		return filterData;
	}


	public JSONObject encodeToJSON() {
		JSONObject result = new JSONObject();

		try { result.put("date", date); } catch (Exception ex) { ex.printStackTrace(); }

		try { result.put("totalCalories", totalCalories); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("totalDuration", totalDuration); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("totalSteps", totalSteps); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("totalPowerPoints", totalPowerPoints); } catch (Exception ex) { ex.printStackTrace(); }

		try {
			JSONArray jsonArray = new JSONArray();

			if (data != null) {
				for (int i = 0; i < data.size(); i++) {
					jsonArray.put(data.get(i).encodeToJSON());
				}
			}

			result.put("data", jsonArray);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return result;
	}


	public static KPHDailyDetailData decodeFromJSON(JSONObject jsonObject) {
		KPHDailyDetailData data = new KPHDailyDetailData();

		try { data.date = jsonObject.getString("date"); } catch (Exception ex) { ex.printStackTrace(); }

		try { data.totalCalories = jsonObject.getDouble("totalCalories"); } catch (Exception ex) { ex.printStackTrace(); }
		try { data.totalDuration = jsonObject.getDouble("totalDuration"); } catch (Exception ex) { ex.printStackTrace(); }
		try { data.totalSteps = jsonObject.getInt("totalSteps"); } catch (Exception ex) { ex.printStackTrace(); }
		try { data.totalPowerPoints = jsonObject.getInt("totalPowerPoints"); } catch (Exception ex) { ex.printStackTrace(); }

		try {
			data.data = new ArrayList<>();

			JSONArray dataArray = jsonObject.getJSONArray("data");
			for (int i = 0; i < dataArray.length(); i++) {
				data.data.add(DetailItem.decodeFromJSON(dataArray.getJSONObject(i)));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return data;
	}


	/**
	 * activity data for 15 mis, minimum unit
	 */
	public static class DetailItem {
		@SerializedName("date")
		public String date;

		@SerializedName("steps")
		public int steps;

		@SerializedName("duration")
		public int duration;

		@SerializedName("calories")
		public double calories;



		public DetailItem(String date, double calories, int steps, int duration) {
			this.steps = steps;
			this.duration = duration;
			this.calories = calories;
			this.date = date;
		}

		public int getStep() {
			return steps;
		}

		public int getDuration() {
			return duration;
		}

		public double getCalories() {
			return calories;
		}

		public String getDate() {
			return date;
		}


		public JSONObject encodeToJSON() {
			JSONObject result = new JSONObject();

			try { result.put("date", date); } catch (Exception ex) { ex.printStackTrace(); }
			try { result.put("steps", steps); } catch (Exception ex) { ex.printStackTrace(); }
			try { result.put("duration", duration); } catch (Exception ex) { ex.printStackTrace(); }
			try { result.put("calories", calories); } catch (Exception ex) { ex.printStackTrace(); }

			return result;
		}

		public static DetailItem decodeFromJSON(JSONObject jsonObject) {
			String date = "";
			int steps = 0;
			int duration = 0;
			double calories = 0;

			try { steps = jsonObject.getInt("steps"); } catch (Exception ex) { ex.printStackTrace(); }
			try { date = jsonObject.getString("date"); } catch (Exception ex) { ex.printStackTrace(); }
			try { duration = jsonObject.getInt("duration"); } catch (Exception ex) { ex.printStackTrace(); }
			try { calories = jsonObject.getDouble("calories"); } catch (Exception ex) { ex.printStackTrace(); }

			return new DetailItem(date, calories, steps, duration);
		}
	}
}
