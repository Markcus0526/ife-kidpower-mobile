package org.unicefkidpower.schools.sync;

/**
 * Created by Dayong Li on 8/05/2016.
 * Copyright 2016 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public interface OnBandActionListener {
	void success(Object object);

	void failed(int code, String message);

	void connected();

	void reportForDaily(boolean didFinish, int agoDay, int wholeDay);

	void updateStatus(String status);
}
