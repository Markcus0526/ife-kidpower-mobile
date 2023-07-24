package org.unicefkidpower.schools.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ToggleButton;

import org.unicefkidpower.schools.R;

public class KPToggleButton extends ToggleButton {
	private static final String TAG = "Button";

	protected Context context;
	protected String customFont;

	public KPToggleButton(Context context) {
		super(context);
	}

	public KPToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.context = context;
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.KPButton);
		customFont = typedArray.getString(R.styleable.KPButton_customButtonFont);

		layoutView();
		typedArray.recycle();
	}

	public KPToggleButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		this.context = context;
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.KPButton);
		customFont = typedArray.getString(R.styleable.KPButton_customButtonFont);

		layoutView();
		typedArray.recycle();
	}

	private void layoutView() {
		setCustomFont(context);
	}

	private void setCustomFont(Context context) {
		if (!TextUtils.isEmpty(customFont)) {
			setCustomFont(context, customFont);
		}
	}

	public boolean setCustomFont(Context context, String asset) {
		Typeface typeface;
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
