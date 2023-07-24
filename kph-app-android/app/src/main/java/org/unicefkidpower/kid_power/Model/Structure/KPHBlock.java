package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by Ruifeng Shi on 10/29/2015.
 */
public class KPHBlock {
	@SerializedName("_id")
	private int _id;

	@SerializedName("blockerId")
	private int blockerId;

	@SerializedName("blockedId")
	private int blockedId;

	@SerializedName("startDate")
	private Date startDate;

	@SerializedName("endDate")
	private Date endDate;

	@SerializedName("blocker")
	private KPHUserData blocker;

	@SerializedName("blocked")
	private KPHUserData blocked;

	public KPHBlock() {}

	public KPHBlock(
			int _id,
			int blockerId,
			int blockedId,
			Date startDate,
			Date endDate,
			KPHUserData blocker,
			KPHUserData blocked
	) {
		this._id = _id;
		this.blockerId = blockerId;
		this.blockedId = blockedId;
		this.startDate = startDate;
		this.endDate = endDate;
		this.blocker = blocker;
		this.blocked = blocked;
	}

	public int getId() {
		return _id;
	}

	public void setId(int id) {
		this._id = id;
	}

	public int getBlockerId() {
		return blockerId;
	}

	public void setBlockerId(int blockerId) {
		this.blockerId = blockerId;
	}

	public int getBlockedId() {
		return blockedId;
	}

	public void setBlockedId(int blockedId) {
		this.blockedId = blockedId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public KPHUserData getBlocker() {
		return blocker;
	}

	public void setBlocker(KPHUserData blocker) {
		this.blocker = blocker;
	}

	public KPHUserData getBlocked() {
		return blocked;
	}

	public void setBlocked(KPHUserData blocked) {
		this.blocked = blocked;
	}
}
