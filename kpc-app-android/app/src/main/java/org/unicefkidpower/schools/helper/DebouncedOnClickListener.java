package org.unicefkidpower.schools.helper;

/**
 * Created by Dayong Li on 5/2/2016.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

import android.os.SystemClock;
import android.view.View;

/**
 * A Debounced OnClickListener
 * Rejects clicks that are too close together in time.
 * This class is safe to use as an OnClickListener for multiple views, and will debounce each one separately.
 */
public abstract class DebouncedOnClickListener implements View.OnClickListener {
	private static final int DEFAULT_INTERVAL = 1000;

	private final long minimumInterval;
	private static long previousClickTimestamp;
	// private Map<View, Long> lastClickMap;

	/**
	 * Implement this in your subclass instead of onClick
	 *
	 * @param v The view that was clicked
	 */
	public abstract void onDebouncedClick(View v);

	public DebouncedOnClickListener() {
		this.minimumInterval = DEFAULT_INTERVAL;
		//this.lastClickMap = new WeakHashMap<View, Long>();
	}

	/**
	 * The one and only constructor
	 *
	 * @param minimumIntervalMsec The minimum allowed time between clicks - any click sooner than this after a previous click will be rejected
	 */
	public DebouncedOnClickListener(long minimumIntervalMsec) {
		this.minimumInterval = minimumIntervalMsec;
		//this.lastClickMap = new WeakHashMap<View, Long>();
	}

	@Override
	public void onClick(View clickedView) {
		long currentTimestamp = SystemClock.uptimeMillis();

		if (previousClickTimestamp == 0 || (currentTimestamp - previousClickTimestamp > minimumInterval)) {
			previousClickTimestamp = currentTimestamp;
			onDebouncedClick(clickedView);
		}
	}
}