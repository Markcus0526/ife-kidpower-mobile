package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ruifeng Shi on 6/22/2016.
 */
public class KPHTravelLogUnreadItemCount {
	@SerializedName("unreadCount")
	private int unreadCount;

	public KPHTravelLogUnreadItemCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}

	public int getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}
}
