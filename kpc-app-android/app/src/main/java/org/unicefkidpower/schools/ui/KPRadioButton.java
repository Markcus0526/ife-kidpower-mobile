package org.unicefkidpower.schools.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RadioButton;

import org.unicefkidpower.schools.R;

/**
 * Created by Ruifeng Shi on 9/1/2015.
 */
public class KPRadioButton extends RadioButton {
	private static final String TAG = "RadioButton";

	public KPRadioButton(Context context) {
		super(context);
	}

	public KPRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setCustomFont(context, attrs);
	}

	public KPRadioButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setCustomFont(context, attrs);
	}

	private void setCustomFont(Context context, AttributeSet attributeSet) {
		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPRadioButton);
		String customFont = typedArray.getString(R.styleable.KPRadioButton_customRadioButtonFont);
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
