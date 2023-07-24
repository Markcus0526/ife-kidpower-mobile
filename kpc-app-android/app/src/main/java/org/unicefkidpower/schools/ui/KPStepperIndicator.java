package org.unicefkidpower.schools.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.UIManager;
import org.unicefkidpower.schools.define.CommonUtils;

import java.util.ArrayList;

/**
 * Created by Ruifeng Shi on 9/19/2016.
 */
public class KPStepperIndicator extends LinearLayout {

    Context context;
    String[] stepTitles;
    ArrayList<KPToggleButton> toggleButtons = new ArrayList<>();

    public KPStepperIndicator(Context context) {
        super(context);
        this.context = context;
        layoutView();
    }

    public KPStepperIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        layoutView();
    }

    public KPStepperIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        layoutView();
    }

    private void layoutView() {
        setOrientation(LinearLayout.HORIZONTAL);
        setBackgroundColor(Color.WHITE);

        if (stepTitles != null) {
            for (String stepTitle : stepTitles) {
                KPToggleButton toggleButton = new KPToggleButton(context);
                toggleButton.setText(stepTitle);
                toggleButton.setTextOn(stepTitle);
                toggleButton.setTextOff(stepTitle);
                toggleButton.setEnabled(false);
                toggleButton.setGravity(Gravity.CENTER);
                toggleButton.setBackgroundResource(R.drawable.step_progress_toggle);
                toggleButton.setCustomFont(context, context.getString(R.string.font_awesome_webfont));
                toggleButton.setTextColor(CommonUtils.getColorFromRes(getResources(), R.color.kidpower_brownish_grey));
                toggleButton.setPadding(
                        0, context.getResources().getDimensionPixelSize(R.dimen.margin_10),
                        0, context.getResources().getDimensionPixelSize(R.dimen.margin_10)
                );
                toggleButton.setTextSize(
                        context.getResources().getDimension(R.dimen.fontsize_extra_tiny)
                                / context.getResources().getDisplayMetrics().density
                );
                LinearLayout.LayoutParams lpToggleButton = new LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f
                );
                toggleButton.setLayoutParams(lpToggleButton);
                addView(toggleButton);
                toggleButtons.add(toggleButton);
            }
        }
    }

    public void setData(String[] stepTitles) {
        this.stepTitles = stepTitles;
        layoutView();
    }

    public void setCurrentStep(int index) {
        if (index > toggleButtons.size())
            return;

        for (int i = 0; i < index + 1; i++) {
            KPToggleButton toggleButton = toggleButtons.get(i);
            toggleButton.setChecked(true);
            toggleButton.setText("\uF00C " + stepTitles[i]);
            toggleButton.setTextColor(Color.WHITE);

            if (i == index) {
                toggleButton.setBackgroundResource(R.drawable.ic_current_step);
            } else {
                toggleButton.setBackgroundColor(CommonUtils.getColorFromRes(getResources(), R.color.progress_step_highlight));
            }
        }

        for (int i = index + 1; i < toggleButtons.size(); i++ ) {
            KPToggleButton toggleButton = toggleButtons.get(i);
            toggleButton.setChecked(false);
            toggleButton.setText(stepTitles[i]);
            toggleButton.setTextColor(CommonUtils.getColorFromRes(getResources(), R.color.kidpower_brownish_grey));
            toggleButton.setBackgroundColor(Color.TRANSPARENT);
        }
    }
}
