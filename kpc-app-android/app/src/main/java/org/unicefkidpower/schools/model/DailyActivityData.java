package org.unicefkidpower.schools.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Dayong on 7/20/2016.
 */
public class DailyActivityData {
	@SerializedName("date")
	public String date;
	@SerializedName("data")
	public List<DetailData> data;
	@SerializedName("dateSummary")
	public SummaryData summary;

	static public class DetailData {
		public String time;
		public double calories;
		public int steps;
		public double distance;
	}

	static public class SummaryData {
		public int steps;
		public double calories;
		public double distance;

		public int duration; //minute
		public int mvpa; //minute
		public int vpa; //minute
	}
}
