package org.unicefkidpower.kid_power.Model.Structure;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Dayong Li on 8/22/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class KPHUserData {
	public static final int USER_TYPE_ADULT = 0;
	public static final int USER_TYPE_CHILD = 1;
	public static final int USER_TYPE_TEENAGER = 2;
	public static final int USER_TYPE_AGENT = 3;
	public static final int USER_TYPE_ADMIN = 4;

	@SerializedName("_id")
	private int			_id;

	@SerializedName("handle")
	private String		handle;

	@SerializedName("friendlyName")
	private String		friendly_name;

	@SerializedName("email")
	private String		email;

	@SerializedName("userType")
	private String		userType;

	@SerializedName("gender")
	private String		gender;

	@SerializedName("dob")
	private Date		birthday;

	@SerializedName("avatarId")
	private String		avatarId;

	@SerializedName("parentId")
	private int			parentId;

	@SerializedName("verifiedAdult")
	private boolean		verifiedAdult;

	@SerializedName("onMission")
	private boolean		onMission;

	@SerializedName("creditBalance")
	private int			creditBalance;

	@SerializedName("deviceId")
	private String					deviceId;

	@SerializedName("parent")
	private KPHUserSummary			parent;

	@SerializedName("siblings")
	private List<KPHUserSummary>	siblings;

	@SerializedName("children")
	private List<KPHUserSummary>	children;

	@SerializedName("followings")
	private List<KPHUserSummary>	followings;

	@SerializedName("followers")
	private List<KPHUserSummary>	followers;

	@SerializedName("trackers")
	private List<KPHTracker>		bands;

	@SerializedName("stats")
	private KPHUserStats			userStats;

	@SerializedName("access_token")
	private String					access_token;

	@SerializedName("userIP")
	private String					userIP;

	@SerializedName("isActive")
	private boolean					isActive;

	@SerializedName("postCode")
	private String					postCode;

	@SerializedName("lastSyncDate")
	private Date					lastSyncDate;

	@SerializedName("cheers")
	private List<KPHCheer>			cheers;


	public KPHUserData(
			int _id,
			String handle,
			String friendly_name,
			String email,
			String userType,
			String gender,
			Date birthday,
			String avatarId,
			int parentId,
			boolean verifiedAdult,
			boolean onMission,
			int creditBalance,
			String deviceId,
			KPHUserSummary parent,
			List<KPHUserSummary> siblings,
			List<KPHUserSummary> children,
			List<KPHUserSummary> followings,
			List<KPHUserSummary> followers,
			List<KPHTracker> bands,
			KPHUserStats userStats,
			String access_token,
			String userIP,
			boolean isActive,
			String postCode,
			Date lastSyncDate,
			List<KPHCheer> cheers
	) {
		this._id = _id;
		this.handle = handle;
		this.friendly_name = friendly_name;
		this.gender = gender;
		this.email = email;
		this.birthday = birthday;
		this.avatarId = avatarId;
		this.verifiedAdult = verifiedAdult;
		this.onMission = onMission;
		this.creditBalance = creditBalance;
		this.parentId = parentId;
		this.userType = userType;
		this.deviceId = deviceId;
		this.parent = parent;
		this.siblings = siblings;
		this.children = children;
		this.followings = followings;
		this.followers = followers;
		this.bands = bands;
		this.userStats = userStats;
		this.access_token = access_token;
		this.userIP = userIP;
		this.isActive = isActive;
		this.postCode = postCode;
		this.lastSyncDate = lastSyncDate;
		this.cheers = cheers;
	}

	public int getId() {
		return _id;
	}

	public void setId(int id) {
		this._id = id;
	}

	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public String getFriendlyName() {
		if (friendly_name == null)
			return "";
		return friendly_name;
	}

	public void setFriendlyName(String friendly_name) {
		this.friendly_name = friendly_name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public String getAvatarId() {
		if (avatarId == null)
			return "";
		return avatarId;
	}

	public void setAvatarId(String avatarId) {
		this.avatarId = avatarId;
	}

	public boolean getVerifiedAdult() {
		return verifiedAdult;
	}

	public void setVerifiedAdult(boolean verifiedAdult) {
		this.verifiedAdult = verifiedAdult;
	}

	public boolean getOnMission() {
		return onMission;
	}

	public void setOnMission(boolean onMission) {
		this.onMission = onMission;
	}

	public int getCreditBalance() {
		return creditBalance;
	}

	public void setCreditBalance(int creditBalance) {
		this.creditBalance = creditBalance;
	}

	public KPHUserSummary getParent() {
		return parent;
	}

	public void setParent(KPHUserSummary parent) {
		this.parent = parent;
	}

	public List<KPHUserSummary> getSiblings() {
		return siblings;
	}

	public void setSiblings(List<KPHUserSummary> siblings) {
		this.siblings = siblings;
	}

	public List<KPHUserSummary> getChildren() {
		if (children == null)
			children = new ArrayList<>();
		return children;
	}

	public void setChildren(List<KPHUserSummary> children) {
		this.children = children;
	}

	public KPHUserSummary getChildByName(String childName) {
		for (KPHUserSummary child : children) {
			if (child.getHandle().toUpperCase().equals(childName.toUpperCase())) {
				return child;
			}
		}
		return null;
	}

	public List<KPHUserSummary> getFollowings() {
		if (followings == null)
			followings = new ArrayList<>();
		return followings;
	}

	public void setFollowings(List<KPHUserSummary> followings) {
		this.followings = followings;
	}

	public void unfollowUser(KPHUserSummary following) {
		if (this.followings == null || this.followings.size() == 0)
			return;

		for (KPHUserSummary followingItem : followings) {
			if (followingItem.getId() == following.getId()) {
				followings.remove(followingItem);
				break;
			}
		}
	}

	public List<KPHUserSummary> getFollowers() {
		if (followers == null)
			followers = new ArrayList<>();
		return followers;
	}

	public void setFollowers(List<KPHUserSummary> followers) {
		this.followers = followers;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public int getUserType() {
		if (userType.equalsIgnoreCase("child")) {
			return USER_TYPE_CHILD;
		} else if (userType.equalsIgnoreCase("teenager")) {
			return USER_TYPE_TEENAGER;
		} else if (userType.equalsIgnoreCase("agent")) {
			return USER_TYPE_AGENT;
		} else if (userType.equalsIgnoreCase("admin")) {
			return USER_TYPE_ADMIN;
		} else if (userType.equalsIgnoreCase("adult")) {
			return USER_TYPE_ADULT;
		} else {
			return -1;
		}
	}

	public void setUserType(int userType) {
		switch (userType) {
			case USER_TYPE_CHILD:
				this.userType = "child";
				break;
			case USER_TYPE_TEENAGER:
				this.userType = "teenager";
				break;
			case USER_TYPE_AGENT:
				this.userType = "agent";
				break;
			case USER_TYPE_ADMIN:
				this.userType = "admin";
				break;
			case USER_TYPE_ADULT:
				this.userType = "adult";
				break;
			default:
				this.userType = "";
		}
	}

	public boolean isParent() {
		// TODO must be check it out
		return getVerifiedAdult();
	}

	public String getAccessToken() {
		return access_token;
	}

	public void setAccessToken(String access_token) {
		this.access_token = access_token;
	}

	public List<KPHTracker> getTrackers() {
		return bands;
	}

	public void setBands(List<KPHTracker> bands) {
		this.bands = bands;
	}

	public KPHUserStats getUserStats() {
		return userStats;
	}

	public void setUserStats(KPHUserStats userStats) {
		this.userStats = userStats;
	}

	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	public Date getLastSyncDate() {
		return lastSyncDate;
	}

	public void setLastSyncDate(Date lastSyncDate) {
		this.lastSyncDate = lastSyncDate;
	}

	public String getBirthdayString() {
		if (this.birthday == null)
			return "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
		return dateFormat.format(this.birthday);
	}

	public List<KPHCheer> getCheers() {
		ArrayList<KPHCheer> cheerList = new ArrayList<>();
		if (!TextUtils.isEmpty(avatarId)) {
			cheerList.add(new KPHCheer(0, "", KPHCheer.CHEER_TYPE_DEFAULT, "0", "", true, new Date(), new Date(), 0));
		}
		if (cheers != null) {
			cheerList.addAll(cheers);
		}

		return cheerList;
	}

	public void setCheers(List<KPHCheer> cheers) {
		this.cheers = cheers;
	}
}
