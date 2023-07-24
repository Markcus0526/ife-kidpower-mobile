package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.Log;

import org.unicefkidpower.kid_power.R;

/**
 * Created by Ruifeng Shi on 9/1/2015.
 */
public class KPHButton extends AppCompatButton {
	private static final String TAG = "Button";

	public KPHButton(Context context) {
		super(context);
	}

	public KPHButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setCustomFont(context, attrs);
	}

	public KPHButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setCustomFont(context, attrs);
	}

	private void setCustomFont(Context context, AttributeSet attributeSet) {
		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPHButton);
		String customFont = typedArray.getString(R.styleable.KPHButton_customButtonFont);
		setCustomFont(context, customFont);
		typedArray.recycle();
	}

	public boolean setCustomFont(Context context, String asset) {
		Typeface typeface;
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
