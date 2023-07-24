package org.unicefkidpower.kid_power.Misc;

/**
 * Created by Dayong Li on 9/21/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public interface onBandActionListener {
	void completed(Object object);

	void failed(int code, String message);

	void reportStatus(Object param);
}
