package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.UIManager;

/**
 * Created by Dayong Li on 11/11/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

/*
	Simple progress bar class
 */
public class ProgressBarView extends LinearLayout {
	private ImageView		ivForeground;
	private ImageView		ivPadding;

	private int				progress;

	public ProgressBarView(Context context) {
		super(context);
		init(context, null);
	}

	public ProgressBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public ProgressBarView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attributeSet) {
		int initPercentage = 0;
		int foregroundResId = 0;

		this.setOrientation(HORIZONTAL);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ProgressBarView);

		if (typedArray != null) {
			initPercentage = typedArray.getInteger(R.styleable.ProgressBarView_initPercent, 10);
			foregroundResId = typedArray.getResourceId(R.styleable.ProgressBarView_foregroundResId, 0);
		}

		// default background and foreground views
		ivForeground = new ImageView(context);
		ivForeground.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.PROGRESS_FOREGROUND)); // temporary fix to stupid problem
		LinearLayout.LayoutParams lp = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, initPercentage);
		ivForeground.setLayoutParams(lp);

		ivPadding = new ImageView(context);
		ivPadding.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.PROGRESS_BACKGROUND)); // temporary fix to stupid problem
		lp = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 100 - initPercentage);
		ivPadding.setLayoutParams(lp);

		this.addView(ivForeground);
		this.addView(ivPadding);
	}

	public void setPercentageFilled(int percentage) {
		if (percentage < 0)
			percentage = 0;
		if (percentage > 100)
			percentage = 100;

		progress = percentage;
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, progress);
		ivForeground.setLayoutParams(lp);
		ivForeground.invalidate();

		lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 100 - progress);
		ivPadding.setLayoutParams(lp);
		ivPadding.invalidate();
	}

	// these two funcs are for future classes that may need to programatically getV1 progress
	// or set the foreground resource
	public int getProgress() {
		return progress;
	}

	public void setForegroundRes(int resId) {
		ivForeground.setImageResource(resId);
	}
}
