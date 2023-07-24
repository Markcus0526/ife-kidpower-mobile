package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import org.unicefkidpower.kid_power.R;

/**
 * Created by Ruifeng Shi on 3/4/2017.
 */

public class KPHRoundRectLinearLayout extends LinearLayout {
	public KPHRoundRectLinearLayout(Context context) {
		super(context);
	}

	public KPHRoundRectLinearLayout(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public KPHRoundRectLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onDraw(Canvas c) {
		int tenDP_PixelSize = getContext().getResources().getDimensionPixelSize(R.dimen.dimen_margin_10);

		Path roundRectPath = new Path();
		if (Build.VERSION.SDK_INT < 21) {
			roundRectPath.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), tenDP_PixelSize, tenDP_PixelSize, Path.Direction.CW);
		} else {
			roundRectPath.addRoundRect(0, 0, getWidth(), getHeight(), tenDP_PixelSize, tenDP_PixelSize, Path.Direction.CW);
		}

		c.clipPath(roundRectPath, Region.Op.REPLACE);

		super.onDraw(c);
	}
}
