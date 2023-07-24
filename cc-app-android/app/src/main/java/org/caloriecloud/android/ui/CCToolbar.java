package org.caloriecloud.android.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.TextView;

public class CCToolbar extends Toolbar {

    private TextView titleView;

    public CCToolbar(Context context) {
        super(context);
        init(context);
    }

    public CCToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CCToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        titleView.setX((getWidth() - titleView.getWidth())/2);
    }

    @Override
    public void setTitle(CharSequence title) {
        titleView.setText(title);
    }

    private void init(Context c) {

        titleView = new TextView(c);

        if (!isInEditMode()) {

            try {
                Typeface tf = Typeface.createFromAsset(c.getAssets(), "fonts/SourceSansPro_Regular.otf");
                titleView.setTypeface(tf);
                titleView.setTextSize(20);
                titleView.setAllCaps(true);
                titleView.setTextColor(Color.WHITE);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }

        addView(titleView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

    }
}
