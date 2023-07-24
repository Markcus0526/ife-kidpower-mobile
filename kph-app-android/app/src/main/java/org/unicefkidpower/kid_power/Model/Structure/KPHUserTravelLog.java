package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dayong Li on 10/4/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class KPHUserTravelLog {
	// Travel Log Types Constants
	public static final String TYPE_CHEER = "cheer";
	public static final String TYPE_SNAPSHOT = "snapshot";
	public static final String TYPE_MISSION_START = "missionStart";
	public static final String TYPE_MISSION_FINISH = "missionFinish";
	public static final String TYPE_CUSTOM_CHEER = "customCheer";

	static public int LogTypeFlag_Cheer = 0;
	static public int LogTypeFlag_Snapshot = 1;
	static public int LogTypeFlag_MissionStart = 2;
	static public int LogTypeFlag_MissionFinish = 3;
	static public int LogTypeFlag_CustomCheer = 5;

	@SerializedName("_id")
	private long id;

	@SerializedName("date")
	private String date;

	@SerializedName("type")
	private String type;

	@SerializedName("read")
	private boolean read;

	@SerializedName("content")
	private Content content;


	public KPHUserTravelLog() {
		this.type = "snapshot";
	}

	public KPHUserTravelLog(long id, String date, String type, boolean read, Content content) {
		this.id = id;
		this.date = date;
		this.type = type;
		this.read = read;
		this.content = content;
	}

	public KPHUserTravelLog(String date, String type, long missionId, KPHDelight delight) {
		this.date = date;
		this.type = type;
		this.content = new Content(
				delight.getId(), 0, missionId, 0, "", 0, "", "", delight.getName(), delight.getType(), delight.getGoal()
		);
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getMissionId() {
		return content.getMissionId();
	}

	public void setMissionId(long missionId) {
		this.content.setMissionId(missionId);
	}

	public long getSenderID() {
		return content.getSenderId();
	}

	public String getSenderName() {
		return content.getSenderName();
	}

	public String getCheerAvatar() {
		return content.getImageId();
	}

	public String getCheerMessage() {
		return content.getMessage();
	}

	public long getCheerId() {
		return content.getCheerId();
	}

	public void setCheerId(long cheerId) {
		this.content.setCheerId(cheerId);
	}

	public long getDelightId() {
		return content.getId();
	}

	public void setDelightId(long delightId) {
		content.setId(delightId);
	}

	public String getDelightType() {
		if (content.getType() != null)
			return content.getType();
		else
			return getType();
	}

	public void setDelightType(String delightType) {
		content.setType(delightType);
	}

	public String getDelightName() {
		return content.getName();
	}

	public void setDelightName(String delightName) {
		content.setName(delightName);
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isStartedLog() {
		if (type == null)
			return false;
		return type.equals(TYPE_MISSION_START);
	}

	public boolean isCompletedLog() {
		if (type == null)
			return false;
		return type.equals(TYPE_MISSION_FINISH);
	}

	public boolean isSnapshot() {
		if (type == null)
			return false;
		return type.equals(TYPE_SNAPSHOT);
	}

	public boolean isCheer() {
		if (type == null)
			return false;
		return type.equals(TYPE_CHEER);
	}

	public boolean isCustomCheer() {
		return type != null && type.equals(TYPE_CUSTOM_CHEER);
	}

	class Content {

		@SerializedName("_id")
		long id;

		@SerializedName("cheerId")
		long cheerId;

		@SerializedName("missionId")
		long missionId;

		@SerializedName("userMissionId")
		long userMissionId;

		@SerializedName("message")
		String message;

		@SerializedName("senderId")
		long senderId;

		@SerializedName("senderName")
		String senderName;

		@SerializedName("imageId")
		String imageId;

		@SerializedName("name")
		String name;

		@SerializedName("type")
		String type;

		@SerializedName("goal")
		int goal;

		public Content(long id, long cheerId, long missionId, long userMissionId, String message, long senderId, String senderName, String imageId, String name, String type, int goal) {
			this.id = id;
			this.cheerId = cheerId;
			this.missionId = missionId;
			this.userMissionId = userMissionId;
			this.message = message;
			this.senderId = senderId;
			this.senderName = senderName;
			this.imageId = imageId;
			this.name = name;
			this.type = type;
			this.goal = goal;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public long getCheerId() {
			return cheerId;
		}

		public void setCheerId(long cheerId) {
			this.cheerId = cheerId;
		}

		public long getMissionId() {
			return missionId;
		}

		public void setMissionId(long missionId) {
			this.missionId = missionId;
		}

		public long getUserMissionId() {
			return userMissionId;
		}

		public void setUserMissionId(long userMissionId) {
			this.userMissionId = userMissionId;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public long getSenderId() {
			return senderId;
		}

		public void setSenderId(long senderId) {
			this.senderId = senderId;
		}

		public String getSenderName() {
			return senderName;
		}

		public void setSenderName(String senderName) {
			this.senderName = senderName;
		}

		public String getImageId() {
			return imageId;
		}

		public void setImageId(String imageId) {
			this.imageId = imageId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public int getGoal() {
			return goal;
		}

		public void setGoal(int goal) {
			this.goal = goal;
		}
	}
}
