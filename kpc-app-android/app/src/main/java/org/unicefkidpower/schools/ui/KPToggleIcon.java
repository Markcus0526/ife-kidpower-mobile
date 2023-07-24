package org.unicefkidpower.schools.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import org.unicefkidpower.schools.R;

public class KPToggleIcon extends AppCompatImageView {
	private static final String TAG = "ImageView";

	protected Context context;


	public KPToggleIcon(Context context) {
		super(context);
		setCustomIcon(context, null);
	}


	public KPToggleIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		setCustomIcon(context, attrs);
	}


	public KPToggleIcon(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setCustomIcon(context, attrs);
	}


	private void setCustomIcon(Context context, AttributeSet attributeSet) {
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

		setCustomIcon(context, customFont, style);
		typedArray.recycle();
	}

	public boolean setCustomIcon(Context context, String asset, int style) {
		Typeface typeface;
		try {
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + asset);
		} catch (Exception e) {
			Log.e(TAG, "Could not get typeface: " + e.getMessage());
			return false;
		}

		// need eee
		//setTypeface(typeface, style);

		return true;
	}
}
