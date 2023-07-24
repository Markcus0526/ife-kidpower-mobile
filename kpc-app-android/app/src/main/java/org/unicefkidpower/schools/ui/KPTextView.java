package org.unicefkidpower.schools.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;

import org.unicefkidpower.schools.R;

/**
 * Created by Ruifeng Shi on 9/1/2015.
 */
public class KPTextView extends AppCompatTextView {
	private static final String TAG		= "TextView";
	private Context context				= null;

	public KPTextView(Context context) {
		super(context);
	}

	public KPTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.context = context;
		setCustomFont(context, attrs);
	}

	public KPTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		this.context = context;
		setCustomFont(context, attrs);
	}

	private void setCustomFont(Context context, AttributeSet attributeSet) {
		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPTextView);
		String customFont = typedArray.getString(R.styleable.KPTextView_customTextViewFont);
		if (customFont == null) {
			customFont = context.getString(R.string.font_pfdindisplaypro_regular);
		}

		int style = attributeSet.getAttributeIntValue(
				"http://schemas.android.com/apk/res/android",
				"textStyle",
				Typeface.NORMAL
		);

		setCustomFont(context, customFont, style);
		typedArray.recycle();
	}

	public boolean setCustomFont(Context context, String asset, int style) {
		Typeface typeface;
		try {
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + asset);
		} catch (Exception e) {
			Log.e(TAG, "Could not get typeface: " + e.getMessage());
			return false;
		}

		//setTypeface(typeface, style);

		return true;
	}
}
