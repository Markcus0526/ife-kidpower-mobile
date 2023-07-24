package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Ruifeng Shi on 6/21/2016.
 */
public class KPHUserTravelLogResponse {
	@SerializedName("unreadCount")
	private int unreadCount;

	@SerializedName("tlData")
	private List<KPHUserTravelLog> travelLogList;

	public KPHUserTravelLogResponse(int unreadCount, List<KPHUserTravelLog> travelLogList) {
		this.unreadCount = unreadCount;
		this.travelLogList = travelLogList;
	}

	public int getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}

	public List<KPHUserTravelLog> getTravelLogList() {
		return travelLogList;
	}

	public void setTravelLogList(List<KPHUserTravelLog> travelLogList) {
		this.travelLogList = travelLogList;
	}
}
