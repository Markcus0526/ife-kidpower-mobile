package org.unicefkidpower.schools.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class KPViewPager extends ViewPager {
	public KPViewPager(Context context) {
		super(context);
	}

	public KPViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// Don't allow swiping between pages
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// Don't allow swiping between pages
		return false;
	}

	@Override //override onMeasure so we can measure the heights of our children
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = 0;
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
			int h = child.getMeasuredHeight();
			if (h > height) height = h;
		}

		heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
