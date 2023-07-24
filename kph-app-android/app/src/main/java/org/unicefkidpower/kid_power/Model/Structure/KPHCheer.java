package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by Ruifeng Shi on 4/7/2016.
 */
public class KPHCheer {
	public static final String CHEER_TYPE_DEFAULT = "cheer";
	public static final String CHEER_TYPE_CUSTOM = "customCheer";
	public static final String CHEER_TYPE_UNKNOWN = "";

	@SerializedName("_id")
	private long id;

	@SerializedName("title")
	private String title;

	@SerializedName("type")
	private String type;

	@SerializedName("minimumApiVersion")
	private String minimumApiVersion;

	@SerializedName("note")
	private String note;

	@SerializedName("active")
	private boolean active;

	@SerializedName("createdAt")
	private Date createdAt;

	@SerializedName("updatedAt")
	private Date updatedAt;

	@SerializedName("missionId")
	private long missionId;


	public KPHCheer(long id, String title, String type, String minimumApiVersion, String note, boolean active, Date createdAt, Date updatedAt, long missionId) {
		this.id = id;
		this.title = title;
		this.type = type;
		this.minimumApiVersion = minimumApiVersion;
		this.note = note;
		this.active = active;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.missionId = missionId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMinimumApiVersion() {
		return minimumApiVersion;
	}

	public void setMinimumApiVersion(String minimumApiVersion) {
		this.minimumApiVersion = minimumApiVersion;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public long getMissionId() {
		return missionId;
	}

	public void setMissionId(long missionId) {
		this.missionId = missionId;
	}
}
