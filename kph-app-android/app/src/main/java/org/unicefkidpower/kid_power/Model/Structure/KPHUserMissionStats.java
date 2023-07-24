package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

import org.unicefkidpower.kid_power.Model.MicroService.KPHMissionService;

import java.util.List;

/**
 * Created by Ruifeng Shi on 9/29/2015.
 */
public class KPHUserMissionStats {
	@SerializedName("userMissionId")
	private long userMissionId;

	@SerializedName("missionId")
	private long missionId;

	@SerializedName("missionName")
	private String missionName;

	@SerializedName("missionGoal")
	private int missionGoal;

	@SerializedName("unlockedAt")
	private String unlockedAt;

	@SerializedName("endDate")
	private String endDate;

	@SerializedName("status")
	private String status;

	@SerializedName("completed")
	private boolean completed;

	@SerializedName("startedAt")
	private String startedAt;

	@SerializedName("completedAt")
	private String completedAt;

	@SerializedName("missionCalories")
	private float missionCalories;

	@SerializedName("missionPackets")
	private int missionPackets;

	@SerializedName("delightsUnlocked")
	private List<KPHDelightUnlocked> delightsUnlocked;


	public KPHUserMissionStats() {}

	public KPHUserMissionStats(
			long userMissionId,
			long missionId,
			String missionName,
			int missionGoal,
			String unlockedAt,
			String endDate,
			String status,
			boolean completed,
			String startedAt,
			String completedAt,
			float missionCalories,
			int missionPackets,
			List<KPHDelightUnlocked> delightsUnlocked,
			String calloutMessage
	) {
		this.userMissionId = userMissionId;
		this.missionId = missionId;
		this.missionName = missionName;
		this.missionGoal = missionGoal;
		this.unlockedAt = unlockedAt;
		this.endDate = endDate;
		this.status = status;
		this.completed = completed;
		this.startedAt = startedAt;
		this.completedAt = completedAt;
		this.missionCalories = missionCalories;
		this.missionPackets = missionPackets;
		this.delightsUnlocked = delightsUnlocked;
	}

	public long getUserMissionId() {
		return userMissionId;
	}

	public void setUserMissionId(long userMissionId) {
		this.userMissionId = userMissionId;
	}

	public long getMissionId() {
		return missionId;
	}

	public void setMissionId(long missionId) {
		this.missionId = missionId;
	}

	public String getMissionName() {
		return missionName;
	}

	public void setMissionName(String missionName) {
		this.missionName = missionName;
	}

	public int getMissionGoal() {
		return missionGoal;
	}

	public void setMissionGoal(int missionGoal) {
		this.missionGoal = missionGoal;
	}

	public String getUnlockedAt() {
		return unlockedAt;
	}

	public void setUnlockedAt(String unlockedAt) {
		if (unlockedAt == null)
			unlockedAt = "";
		this.unlockedAt = unlockedAt;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		if (endDate == null)
			endDate = "";
		this.endDate = endDate;
	}

	public String getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(String startedAt) {
		if (startedAt == null)
			startedAt = "";
		this.startedAt = startedAt;
	}

	public String getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(String completedAt) {
		if (completedAt == null)
			completedAt = "";
		this.completedAt = completedAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean getCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public float getMissionCalories() {
		return missionCalories;
	}

	public void setMissionCalories(float missionCalories) {
		this.missionCalories = missionCalories;
	}

	public int getMissionPackets() {
		return missionPackets;
	}

	public int getMissionPowerPoint() {
		return (int) missionCalories / 50;
	}

	public void setMissionPackets(int missionPackets) {
		this.missionPackets = missionPackets;
	}

	public List<KPHDelightUnlocked> getDelightsUnlocked() {
		return delightsUnlocked;
	}

	public void setDelightsUnlocked(List<KPHDelightUnlocked> delightsUnlocked) {
		this.delightsUnlocked = delightsUnlocked;
	}

	public boolean isCompletedMission() {
		return getCompleted() || status.toLowerCase().equals(KPHMissionService.MISSION_COMPLETED);
	}

	public boolean isStartedMission() {
		if (startedAt == null || startedAt.isEmpty())
			return false;

		return true;
	}

	public boolean isUnlockedMission() {
		if (unlockedAt == null)
			return false;

		return true;
	}

	public int getProgress() {
		if (missionGoal == 0)
			return 0;

		int percent = (int) missionCalories * 100 / missionGoal;

		return percent;
	}

	public void updateFromSnapshot(KPHSyncSnapshots snapshots) {
		// compare updated data

		missionCalories = snapshots.lsdMissionCalories;
		missionPackets += snapshots.packetsUnlocked;

		for (KPHDelight delight : snapshots.delightsUnlocked) {
			if (delight.getType().equalsIgnoreCase("customCheer"))
				continue;

			KPHDelightInformation information = KPHMissionService.sharedInstance().getDelightInformationById(delight.getId());
			if (information == null || information.isMissionIdEquals(0))
				continue;

			if (!information.isMissionIdEquals(getMissionId()))
				continue;

			KPHDelightUnlocked unlocked = new KPHDelightUnlocked(delight.getId(), delight.getName(), snapshots.lsd);
			delightsUnlocked.add(unlocked);
		}
	}
}
