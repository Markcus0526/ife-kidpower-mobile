package org.unicefkidpower.schools.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

import org.unicefkidpower.schools.R;

/**
 * Created by Ruifeng Shi on 9/1/2015.
 */
public class KPButton extends Button {
	private static final String TAG = "Button";

	public KPButton(Context context) {
		super(context);
	}

	public KPButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setCustomFont(context, attrs);
	}

	public KPButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setCustomFont(context, attrs);
	}

	private void setCustomFont(Context context, AttributeSet attributeSet) {
		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPButton);
		String customFont = typedArray.getString(R.styleable.KPButton_customButtonFont);
		setCustomFont(context, customFont);
		typedArray.recycle();
	}

	public boolean setCustomFont(Context context, String asset) {
		Typeface typeface = null;
		try {
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + asset);
		} catch (Exception e) {
			Log.e(TAG, "Could not get typeface: " + e.getMessage());
			return false;
		}

		setTypeface(typeface);
		return true;
	}
}
