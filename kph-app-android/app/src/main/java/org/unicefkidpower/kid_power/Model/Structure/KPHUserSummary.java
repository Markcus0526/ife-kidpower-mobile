package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ruifeng Shi on 10/26/2015.
 */
public class KPHUserSummary {
	@SerializedName("_id")
	private int _id;

	@SerializedName("handle")
	private String handle;

	@SerializedName("friendlyName")
	private String friendlyName;

	@SerializedName("avatarId")
	private String avatarId;

	@SerializedName("deviceId")
	private String deviceId;

	@SerializedName("totalPoints")
	private int totalPoints;

	@SerializedName("sevenDaysPowerPoints")
	private int sevenDaysPowerPoints;

	@SerializedName("totalPowerPoints")
	private int totalPowerPoints;

	@SerializedName("totalPackets")
	private int totalPackets;

	@SerializedName("missionsCompleted")
	private int missionsCompleted;

	@SerializedName("missionId")
	private int missionId;

	@SerializedName("currentMission")
	private String currentMission;

	@SerializedName("missionPackets")
	private int missionPackets;

	@SerializedName("missionPoints")
	private int missionPoints;

	@SerializedName("progressPercent")
	private int progressPercent;

	public KPHUserSummary(
			int id,
			String handle,
			String friendlyName,
			String avatarId,
			String deviceId,
			int totalPoints,
			int sevenDaysPowerPoints,
			int totalPowerPoints,
			int totalPackets,
			int missionsCompleted,
			int missionId,
			String currentMission,
			int missionPackets,
			int missionPoints,
			int progressPercent) {
		this._id = id;
		this.handle = handle;
		this.friendlyName = friendlyName;
		this.avatarId = avatarId;
		this.deviceId = deviceId;
		this.totalPoints = totalPoints;
		this.sevenDaysPowerPoints = sevenDaysPowerPoints;
		this.totalPowerPoints = totalPowerPoints;
		this.totalPackets = totalPackets;
		this.missionsCompleted = missionsCompleted;
		this.missionId = missionId;
		this.currentMission = currentMission;
		this.missionPackets = missionPackets;
		this.missionPoints = missionPoints;
		this.progressPercent = progressPercent;
	}

	public KPHUserSummary(KPHUserData userData) {
		this._id = userData.getId();
		this.handle = userData.getHandle();
		this.friendlyName = userData.getFriendlyName();
		this.avatarId = userData.getAvatarId();
	}

	public int getId() {
		return _id;
	}

	public void setId(int id) {
		this._id = id;
	}

	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public String getAvatarId() {
		if (avatarId == null)
			return "";
		return avatarId;
	}

	public void setAvatarId(String avatarId) {
		this.avatarId = avatarId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public int getTotalPoints() {
		return totalPoints;
	}

	public void setTotalPoints(int totalPoints) {
		this.totalPoints = totalPoints;
	}

	public int getSevenDaysPowerPoints() {
		return sevenDaysPowerPoints;
	}

	public void setSevenDaysPowerPoints(int sevenDaysPowerPoints) {
		this.sevenDaysPowerPoints = sevenDaysPowerPoints;
	}

	public int getTotalPowerPoints() {
		return totalPowerPoints;
	}

	public void setTotalPowerPoints(int totalPowerPoints) {
		this.totalPowerPoints = totalPowerPoints;
	}

	public int getTotalPackets() {
		return totalPackets;
	}

	public void setTotalPackets(int totalPackets) {
		this.totalPackets = totalPackets;
	}

	public int getMissionsCompleted() {
		return missionsCompleted;
	}

	public void setMissionsCompleted(int missionsCompleted) {
		this.missionsCompleted = missionsCompleted;
	}

	public int getMissionId() {
		return missionId;
	}

	public void setMissionId(int missionId) {
		this.missionId = missionId;
	}

	public String getCurrentMission() {
		return currentMission;
	}

	public void setCurrentMission(String currentMission) {
		this.currentMission = currentMission;
	}

	public int getMissionPackets() {
		return missionPackets;
	}

	public void setMissionPackets(int missionPackets) {
		this.missionPackets = missionPackets;
	}

	public int getMissionPoints() {
		return missionPoints;
	}

	public void setMissionPoints(int missionPoints) {
		this.missionPoints = missionPoints;
	}

	public int getProgressPercent() {
		return progressPercent;
	}

	public void setProgressPercent(int progressPercent) {
		this.progressPercent = progressPercent;
	}
}
