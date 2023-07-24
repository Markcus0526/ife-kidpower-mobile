package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Implemntation of Rest Service
 * <p>
 * Created by Dayong Li on 08/10/15.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class KPHSyncSnapshots {
	@SerializedName("_id")
	public int _id;

	@SerializedName("deviceId")
	public String deviceId;

	@SerializedName("date")
	public String date;

	@SerializedName("lsd")
	public String lsd;

	@SerializedName("lsdCalories")
	public float lsdCalories;

	@SerializedName("lsdMissionCalories")
	public float lsdMissionCalories;

	@SerializedName("newBuzzPoints")
	public int newBuzzPoints;

	@SerializedName("newCalories")
	public float newCalories;

	@SerializedName("packetsUnlocked")
	public int packetsUnlocked;

	@SerializedName("delightsUnlocked")
	public List<KPHDelight> delightsUnlocked;

	public ArrayList<Long> unlockedCheerIDs;

	@SerializedName("enablingMission")
	public boolean enablingMission;

	@SerializedName("disablingMission")
	public boolean disablingMission;

	@SerializedName("missionId")
	public int missionId;

	@SerializedName("createdAt")
	public String createdAt;

	@SerializedName("updatedAt")
	public String updatedAt;

	@SerializedName("userId")
	public int userId;

	@SerializedName("syncCompleteScreens")
	public ArrayList<SyncScreen> syncCompleteScreens;

	public String syncstatus;

	public KPHSyncSnapshots() {}


	public KPHSyncSnapshots(int id, String deviceId, String date, String lsd, float lsdCalories, float lsdMissionCalories,
							int newBuzzPoints, float newCalories, int packetsUnlocked, List<KPHDelight> delightsUnlocked, boolean enablingMission,
							boolean disablingMission, int missionId, String createdAt, String updatedAt, int userId) {
		this._id = id;
		this.deviceId = deviceId;
		this.date = date;
		this.lsd = lsd;
		this.lsdCalories = lsdCalories;
		this.lsdMissionCalories = lsdMissionCalories;
		this.newCalories = newCalories;
		this.newBuzzPoints = newBuzzPoints;
		this.packetsUnlocked = packetsUnlocked;
		this.delightsUnlocked = delightsUnlocked;
		this.enablingMission = enablingMission;
		this.disablingMission = disablingMission;
		this.missionId = missionId;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.userId = userId;
	}

	// This constructor is for TEST
	// Don't use other case
	public KPHSyncSnapshots(int packetsUnlocked, List<KPHDelight> delightsUnlocked) {
		this.packetsUnlocked = packetsUnlocked;
		this.delightsUnlocked = delightsUnlocked;
	}

	static public class SyncScreen {
		@SerializedName("screenId")
		public int screenId;
		@SerializedName("content")
		public ScreenContent content;

		public SyncScreen(int id, String h1, String text, String button) {
			screenId = id;
			content = new ScreenContent();
			content.h1 = h1;
			content.text = text;
			content.buttonText = button;
		}
	}

	static public class ScreenContent {
		@SerializedName("h1")
		public String h1;
		@SerializedName("text")
		public String text;
		@SerializedName("buttonText")
		public String buttonText;
	}
}
