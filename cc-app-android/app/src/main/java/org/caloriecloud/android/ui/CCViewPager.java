package org.caloriecloud.android.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CCViewPager extends ViewPager {

    public CCViewPager(Context context) {
        super(context);
    }

    public CCViewPager(Context context, AttributeSet attrs) {
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
}
