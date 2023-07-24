package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ruifeng Shi on 9/29/2015.
 */
public class KPHDelightUnlocked {
	@SerializedName("delightId")
	private long		delightId;

	@SerializedName("delightName")
	private String		delightName;

	@SerializedName("delightUnlockedAt")
	private String		delightUnlockedAt;


	public KPHDelightUnlocked() {}

	public KPHDelightUnlocked(long delightId, String delightName, String delightUnlockedAt) {
		this.delightId = delightId;
		this.delightName = delightName;
		this.delightUnlockedAt = delightUnlockedAt;
	}

	public long getDelightId() {
		return delightId;
	}

	public void setDelightId(long delightId) {
		this.delightId = delightId;
	}

	public String getDelightName() {
		return delightName;
	}

	public void setDelightName(String delightName) {
		this.delightName = delightName;
	}

	public String getDelightUnlockedAt() {
		return delightUnlockedAt;
	}

	public void setDelightUnlockedAt(String delightUnlockedAt) {
		this.delightUnlockedAt = delightUnlockedAt;
	}
}
