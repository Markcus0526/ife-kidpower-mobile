package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Ruifeng Shi on 9/29/2015.
 */
public class KPHMission {
	@SerializedName("_id")
	private int _id;

	@SerializedName("name")
	private String name;

	@SerializedName("goal")
	private int goal;

	@SerializedName("timeToComplete")
	private int timeToComplete;

	@SerializedName("pointsPerPacket")
	private int pointsPerPacket;

	@SerializedName("isActive")
	private boolean isActive;

	@SerializedName("autoUnlock")
	private boolean autoUnlock;

	@SerializedName("sortOrder")
	private int sortOrder;

	@SerializedName("delights")
	private List<KPHDelight> delights;

	@SerializedName("calloutMessage")
	private String calloutMessage;

	public String		description;
	public String		completeImgURL;
	public String		countryImgURL;
	public String		introVideoBgImgURL;
	public String		introVideoURL;
	public String		completeVideoURL;
	public String		missionCompleteText;
	public String		missionCost;


	public KPHMission() {
		this.pointsPerPacket = 10;
	}

	public KPHMission(int id, String name, int goal, int timeToComplete, int pointsPerPacket,
					  boolean isActive, boolean autoUnlock, int sortOrder, List<KPHDelight> delights,
					  String calloutMessage) {
		this._id = id;
		this.name = name;
		this.goal = goal;
		this.timeToComplete = timeToComplete;
		this.pointsPerPacket = pointsPerPacket;
		this.isActive = isActive;
		this.autoUnlock = autoUnlock;
		this.sortOrder = sortOrder;
		this.delights = delights;
		this.calloutMessage = calloutMessage;
	}

	public int getId() {
		return _id;
	}

	public void setId(int id) {
		this._id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getGoal() {
		return goal;
	}

	public void setGoal(int goal) {
		this.goal = goal;
	}

	public int getTimeToComplete() {
		return timeToComplete;
	}

	public void setTimeToComplete(int timeToComplete) {
		this.timeToComplete = timeToComplete;
	}

	public int getPointsPerPacket() {
		return pointsPerPacket;
	}

	public void setPointsPerPacket(int pointsPerPacket) {
		this.pointsPerPacket = pointsPerPacket;
	}

	public List<KPHDelight> getDelights() {
		return delights;
	}

	public void setDelights(List<KPHDelight> delights) {
		this.delights = delights;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

	public boolean isAutoUnlock() {
		return autoUnlock;
	}

	public void setAutoUnlock(boolean autoUnlock) {
		this.autoUnlock = autoUnlock;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getCalloutMessage() {
		return calloutMessage == null ? "" : calloutMessage;
	}

}
