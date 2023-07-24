package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Ruifeng Shi on 9/23/2015.
 */
public class KPHUserStats {
	@SerializedName("totalPowerPoints")
	private int totalPowerPoints;

	@SerializedName("totalPackets")
	private int totalPackets;

	@SerializedName("sevenDaysPowerPoints")
	private int sevenDaysPowerPoints;

	@SerializedName("missionsCompleted")
	private int missionsCompleted = 0;

	@SerializedName("missionId")
	private long missionId;

	@SerializedName("missionPackets")
	private int missionPackets;

	@SerializedName("missionPoints")
	private int missionPoints;

	@SerializedName("progressPercent")
	private int progressPercent;

	@SerializedName("currentMission")
	private String currentMission = "";

	@SerializedName("completedMissions")
	private List<KPHUserMission> completedMissions;

	public KPHUserStats(int totalPowerPoints, int totalPackets, int missionsCompleted) {
		this.totalPowerPoints = totalPowerPoints;
		this.totalPackets = totalPackets;
		this.missionsCompleted = missionsCompleted;
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

	public int getSevenDaysPowerPoints() {
		return sevenDaysPowerPoints;
	}

	public void setSevenDaysPowerPoints(int sevenDaysPowerPoints) {
		this.sevenDaysPowerPoints = sevenDaysPowerPoints;
	}

	public int getMissionsCompleted() {
		return missionsCompleted;
	}

	public void setMissionsCompleted(int missionsCompleted) {
		this.missionsCompleted = missionsCompleted;
	}

	public long getMissionId() {
		return missionId;
	}

	public void setMissionId(long missionId) {
		this.missionId = missionId;
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

	public List<KPHUserMission> getCompletedMissions() {
		return completedMissions;
	}

	public void setCompletedMissions(List<KPHUserMission> completedMissions) {
		this.completedMissions = completedMissions;
	}

	public String getCurrentMission() {
		return currentMission;
	}

	public void setCurrentMission(String currentMission) {
		this.currentMission = currentMission;
	}

	public void incMissionsCompleted() {
		missionsCompleted++;
	}

	public void updateFromSnapshot(KPHSyncSnapshots snapshots) {
		if (snapshots == null)
			return;

		totalPowerPoints += snapshots.newBuzzPoints;
		totalPackets += snapshots.packetsUnlocked;
	}
}
