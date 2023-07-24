package org.caloriecloud.android.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import org.caloriecloud.android.R;

public class CCTextView extends TextView {

    public CCTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.source_sans_pro);
        String fontFamily = null;
        final int n = a.getIndexCount();

        for (int i = 0; i < n; ++i) {
            int attr = a.getIndex(i);

            if (attr == R.styleable.source_sans_pro_android_fontFamily) {
                fontFamily = a.getString(attr);
            }

        }

        if (!isInEditMode()) {

            try {
                Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + fontFamily);
                setTypeface(tf);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }

        a.recycle();
    }
}
