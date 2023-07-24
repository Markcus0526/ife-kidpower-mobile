package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dayong Li on 8/22/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class KPHTracker {
	public static final int BAND_CODE_LENGTH = 5;
	public static final String DELIMITER = ":";

	public static final String TRACKER_TYPE_NAME_KPBAND				= "Power Band";
	public static final String TRACKER_TYPE_NAME_GOOGLEFIT			= "Google Fit";
	public static final String TRACKER_TYPE_NAME_HEALTHKIT			= "HealthKit";
	public static final String TRACKER_VERSION_NAME_GOOGLEFIT		= "2.0";


	private		int _id;

	@SerializedName("name")
	private		String name;

	@SerializedName("type")
	private		String type;

	@SerializedName("version")
	private		String version;

	// mac address in case of kid power band
	// device identifier in case of google fit
	@SerializedName("deviceId")
	private		String deviceId;

	@SerializedName("userId")
	private		int userId;

	@SerializedName("startDate")
	private		String startDate;

	@SerializedName("endDate")
	private		String endDate;

	@SerializedName("isCurrent")
	private		boolean isCurrent;

	@SerializedName("isNew")
	private		boolean isNew;

	// must use this deviceCode for communicate with band, not deviceID
	private		String deviceCode;


	public KPHTracker(int _id, String type, String version, String deviceId, int userId,
					  String startDate, String endDate, boolean isCurrent) {
		this._id = _id;
		this.type = type;
		this.version = version;
		this.deviceId = deviceId;
		this.userId = userId;
		this.startDate = startDate;
		this.endDate = endDate;
		this.isCurrent = isCurrent;
	}

	public int getId() {
		return _id;
	}

	public String getName() {
		// Temporary code
		if (deviceId.contains(":"))
			return getDeviceModel();

		if (name == null)
			name = "";

		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public int getUserId() {
		return userId;
	}

	public void setId(int id) {
		this._id = id;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public boolean getCurrentFlag() { return isCurrent; }

	public String getDeviceType() {
		return type;
	}

	public String getDeviceCode() {
		if (deviceId == null || deviceId.length() < BAND_CODE_LENGTH) {
			throw new IllegalArgumentException();
		}

		if (deviceCode == null || deviceCode.isEmpty()) {
			deviceCode = deviceId.substring(deviceId.length() - BAND_CODE_LENGTH);
		}

		return deviceCode;
	}

	public boolean isNew() {
		return isNew;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}


	/**
	 * Added two methods for the google fit. Temporary code.
	 */

	/**
	 * Method to get only the device identifier part from the field 'deviceId'
	 * @return Device Identifier
	 */
	private String getDeviceIdentifier() {
		String[] parts = deviceId.split(DELIMITER);
		if (parts.length != 2)
			return "";

		return parts[0];
	}

	/**
	 * Method to get only the device build model part from the field 'deviceId'
	 * @return Build model(ex : Android HV2, Google Nexus 6P etc)
	 */
	private String getDeviceModel() {
		String[] parts = deviceId.split(":");
		if (parts.length != 2)
			return "";

		return parts[1];
	}


}
