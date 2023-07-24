package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by Ruifeng Shi on 11/30/2016.
 */

public class KPHResizableImageView extends AppCompatImageView {
	public KPHResizableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Drawable drawable = getDrawable();

		if (drawable != null) {
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = (int) Math.ceil(
					(float) width * (float) drawable.getIntrinsicHeight() / (float) drawable.getIntrinsicWidth()
			);
			setMeasuredDimension(width, height);
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}

/*
		Drawable drawable = getDrawable();
        if (drawable != null) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int maxWidth = getMaxWidth();
            width = width > maxWidth ? maxWidth : width;
            int height = (int)Math.ceil(
                    (float)width * (float)drawable.getIntrinsicHeight() / (float)drawable.getIntrinsicWidth()
            );
            int maxHeight = getMaxHeight();
            height = height > maxHeight ? maxHeight : height;

            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
*/
	}
}
