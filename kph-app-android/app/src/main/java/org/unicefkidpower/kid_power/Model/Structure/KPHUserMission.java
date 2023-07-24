package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ruifeng Shi on 9/29/2015.
 */
public class KPHUserMission {
	@SerializedName("_id")
	private int			_id;

	@SerializedName("startedAt")
	private String		startedAt;

	@SerializedName("unlockedAt")
	private String		unlockedAt;

	@SerializedName("endDate")
	private String		endDate;

	@SerializedName("name")
	private String		name;

	@SerializedName("completedAt")
	private String		completedAt;

	@SerializedName("completed")
	private boolean		completed;

	@SerializedName("updatedAt")
	private String		updatedAt;

	@SerializedName("missionId")
	private int			missionId;

	@SerializedName("userId")
	private int			userId;

	@SerializedName("status")
	private String		status;

	@SerializedName("calloutMessage")
	private String		calloutMessage;

	public KPHUserMission() {
		this.completed = false;
		this.status = "pending";
	}

	public KPHUserMission(
			int id,
			String unlockedAt,
			String endDate,
			String name,
			String completedAt,
			boolean completed,
			String updatedAt,
			String status,
			int missionId,
			int userId,
			String calloutMessage) {
		this._id = id;
		this.unlockedAt = unlockedAt;
		this.endDate = endDate;
		this.name = name;
		this.completedAt = completedAt;
		this.completed = completed;
		this.updatedAt = updatedAt;
		this.status = status;
		this.missionId = missionId;
		this.userId = userId;
		this.calloutMessage = calloutMessage;
	}

	public int getId() {
		return _id;
	}

	public void setId(int id) {
		this._id = id;
	}

	public String getUnlockedAt() {
		return unlockedAt;
	}

	public String getEndDate() {
		return endDate;
	}

	public String getStartedAt() {
		return startedAt;
	}

	public String getCompletedAt() {
		return completedAt;
	}

	public boolean getCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getMissionId() {
		return missionId;
	}

	public void setMissionId(int missionId) {
		this.missionId = missionId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCalloutMessage() {
		return calloutMessage == null ? "" : calloutMessage;
	}
}
