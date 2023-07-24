package org.unicefkidpower.kid_power.Model.Structure;

import org.unicefkidpower.kid_power.Misc.KPHConstants;

/**
 * Created by Ruifeng Shi on 9/30/2015.
 */

public class KPHDelight {
	private long		_id;
	private String		name;
	private int			goal;
	private String		type;
	private String		description;

	public String		imgURL;
	public String		detailImgURL;
	public String		shareImgURL;
	public String		bgTopColor;
	public String		bgBottomColor;


	public KPHDelight() {
		this.type = KPHConstants.DELIGHT_STAMP;
	}

	public KPHDelight(long id, String name, String type, int goal) {
		this._id = id;
		this.name = name;
		this.type = type;
		this.goal = goal;
	}

	public long getId() {
		return _id;
	}

	public void setId(int id) {
		this._id = id;
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
