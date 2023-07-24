package org.unicefkidpower.schools.helper;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by Dayong Li on 1/3/2016.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud Org
 * Dayong@CalorieCloud.Org
 */
public class FontView extends AppCompatTextView {
	private static final String TAG = FontView.class.getSimpleName();
	// Cache the font load status to improve performance
	private static Typeface font;

	public FontView(Context context) {
		super(context);
		setFont(context);
	}

	public FontView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFont(context);
	}

	public FontView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setFont(context);
	}

	private void setFont(Context context) {
		// prevent exception in Android Studio / ADT interface builder
		if (this.isInEditMode()) {
			return;
		}

		//Check for font is already loaded
		if (font == null) {
			try {
				font = Typeface.createFromAsset(context.getAssets(), "fonts/fontawesome-webfont.ttf");
				Logger.log(TAG, "Font awesome loaded");
			} catch (RuntimeException e) {
				Logger.log(TAG, "Font awesome not loaded");
			}
		}

		//Finally set the font
		setTypeface(font);
	}
}