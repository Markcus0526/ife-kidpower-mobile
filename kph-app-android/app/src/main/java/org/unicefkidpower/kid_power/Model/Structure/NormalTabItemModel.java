package org.unicefkidpower.kid_power.Model.Structure;

/**
 * Created by Ruifeng Shi on 9/12/2015.
 */
public class NormalTabItemModel {
	private int position;                       // Position of the item
	private String title;                       // Title of the tab item

	public NormalTabItemModel(int position, String title) {
		this.position = position;
		this.title = title;
	}

	public int getPosition() {
		return this.position;
	}

	public String getTitle() {
		return this.title;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
