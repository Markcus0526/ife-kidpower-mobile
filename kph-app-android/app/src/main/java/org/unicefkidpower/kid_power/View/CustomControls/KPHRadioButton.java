package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.AttributeSet;
import android.util.Log;

import org.unicefkidpower.kid_power.R;

/**
 * Created by Ruifeng Shi on 9/1/2015.
 */
public class KPHRadioButton extends AppCompatRadioButton {
	private static final String TAG = "RadioButton";

	public KPHRadioButton(Context context) {
		super(context);
	}

	public KPHRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setCustomFont(context, attrs);
	}

	public KPHRadioButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setCustomFont(context, attrs);
	}

	private void setCustomFont(Context context, AttributeSet attributeSet) {
		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPHRadioButton);
		String customFont = typedArray.getString(R.styleable.KPHRadioButton_customRadioButtonFont);
		setCustomFont(context, customFont);
		typedArray.recycle();
	}

	public boolean setCustomFont(Context context, String asset) {
		Typeface typeface = null;
		try {
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + asset);
		} catch (Exception e) {
			Log.e(TAG, "Could not getV1 typeface: " + e.getMessage());
			return false;
		}

		setTypeface(typeface);
		return true;
	}
}
