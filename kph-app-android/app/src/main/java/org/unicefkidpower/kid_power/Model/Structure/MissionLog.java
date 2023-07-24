package org.unicefkidpower.kid_power.Model.Structure;

/**
 * Created by Dayong Li on 10/4/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class MissionLog {
	static public final int Log_Cheer = 0;
	static public final int Log_Snapshot = 1;
	static public final int Log_MissionStart = 2;
	static public final int Log_MissionFinish = 3;
	static public final int Log_MissionSummary = 4;
	static public final int Log_CustomCheer = 5;

	private long id;
	private String date;
	private int type;
	private long missionId;
	private long cheerId;
	private long delightId;
	private String delightType;
	private String delightName;
	private boolean unread;

	// for Cheers
	private String sender;
	private String imageId;
	private String message;

	public MissionLog(String date, int type, long missionId) {
		this.date = date;
		this.type = type;
		this.missionId = missionId;
	}

	public MissionLog(KPHUserTravelLog userTravelLog) {
		this.date = userTravelLog.getLogDate();

		int type = Log_Snapshot;
		if (userTravelLog.isCompletedLog())
			type = Log_MissionFinish;
		else if (userTravelLog.isStartedLog())
			type = Log_MissionStart;
		else if (userTravelLog.isSnapshot())
			type = Log_Snapshot;
		else if (userTravelLog.isCheer())
			type = Log_Cheer;
		else if (userTravelLog.isCustomCheer())
			type = Log_CustomCheer;

		this.id = userTravelLog.getId();
		this.type = type;
		this.missionId = userTravelLog.getMissionId();

		this.sender = userTravelLog.getSenderName();
		this.imageId = userTravelLog.getCheerAvatar();
		this.message = userTravelLog.getCheerMessage();
		this.cheerId = userTravelLog.getCheerId();
		this.delightId = userTravelLog.getDelightId();
		this.delightType = userTravelLog.getDelightType();
		this.delightName = userTravelLog.getDelightName();
		this.unread = !userTravelLog.isRead();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLogDate() {
		return date;
	}

	public void setLogDate(String date) {
		this.date = date;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getDelightType() {
		return delightType != null ? delightType : "";
	}

	public void setDelightType(String delightType) {
		this.delightType = delightType;
	}

	public long getMissionId() {
		return missionId;
	}

	public void setMissionId(long missionId) {
		this.missionId = missionId;
	}

	public String getSender() {
		if (sender == null)
			return "";
		return sender;
	}

	public String getCheerAvatar() {
		return imageId;
	}

	public String getCheerMessage() {
		if (message == null)
			return "";
		return message;
	}

	public long getCheerId() {
		return cheerId;
	}

	public void setCheerId(long cheerId) {
		this.cheerId = cheerId;
	}

	public String getDelightName() {
		return delightName;
	}

	public void setDelightName(String delightName) {
		this.delightName = delightName;
	}

	public long getDelightId() {
		return delightId;
	}

	public void setDelightId(long delightId) {
		this.delightId = delightId;
	}

	public boolean isUnread() {
		return unread;
	}

	public void setUnread(boolean unread) {
		this.unread = unread;
	}

	public boolean isSummaryLog() {
		return type == Log_MissionSummary;
	}

	public boolean isStartedLog() {
		return type == Log_MissionStart;
	}

	public boolean isCompletedLog() {
		return type == Log_MissionFinish;
	}

	public boolean isSnapshot() {
		return type == Log_Snapshot;
	}

	public boolean isCheer() {
		return type == Log_Cheer;
	}

	public boolean isCustomCheer() {
		return type == Log_CustomCheer;
	}
}
