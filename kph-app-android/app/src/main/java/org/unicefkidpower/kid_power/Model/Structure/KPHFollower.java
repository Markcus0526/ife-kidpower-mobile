package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by Ruifeng Shi on 10/26/2015.
 */
public class KPHFollower {
	@SerializedName("_id")
	int		_id;

	@SerializedName("followerId")
	int		followerId;

	@SerializedName("followingId")
	int		followingId;

	@SerializedName("startDate")
	Date	startDate;

	@SerializedName("endDate")
	Date	endDate;

	public KPHFollower() {}

	public KPHFollower(
			int id,
			int followerId,
			int followingId,
			Date startDate,
			Date endDate
	) {
		this._id = id;
		this.followerId = followerId;
		this.followingId = followingId;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public int getId() {
		return _id;
	}

	public void setId(int id) {
		this._id = id;
	}

	public int getFollowerId() {
		return followerId;
	}

	public void setFollowerId(int followerId) {
		this.followerId = followerId;
	}

	public int getFollowingId() {
		return followingId;
	}

	public void setFollowingId(int followingId) {
		this.followingId = followingId;
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
}
