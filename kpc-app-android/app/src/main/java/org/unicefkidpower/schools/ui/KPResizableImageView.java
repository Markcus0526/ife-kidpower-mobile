package org.unicefkidpower.schools.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Ruifeng Shi on 5/9/2016.
 */
public class KPResizableImageView extends ImageView {
	public KPResizableImageView(Context context, AttributeSet attrs) {
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
