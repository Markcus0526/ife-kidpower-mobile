package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.UIManager;

public class KPHSegmentedGroup extends RadioGroup {
	private int					marginDp;
	private Resources			resources;
	private int					tintColor;
	private int					checkedTextColor = Color.WHITE;
	private int					uncheckedBackground;
	private Float				cornerRadius;
	private LayoutSelector		layoutSelector;


	public KPHSegmentedGroup(Context context) {
		super(context);
		resources = getResources();

		if (Build.VERSION.SDK_INT < 23)
			tintColor = resources.getColor(R.color.radio_button_selected_color);
		else
			tintColor = resources.getColor(R.color.radio_button_selected_color, null);

		marginDp = (int) getResources().getDimension(R.dimen.dimen_radio_button_stroke_border);
		cornerRadius = getResources().getDimension(R.dimen.dimen_radio_button_conner_radius);

		layoutSelector = new LayoutSelector(cornerRadius);
	}

	public KPHSegmentedGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		resources = getResources();

		if (Build.VERSION.SDK_INT < 23)
			tintColor = resources.getColor(R.color.radio_button_selected_color);
		else
			tintColor = resources.getColor(R.color.radio_button_selected_color, null);

		marginDp = (int) getResources().getDimension(R.dimen.dimen_radio_button_stroke_border);
		cornerRadius = getResources().getDimension(R.dimen.dimen_radio_button_conner_radius);

		initAttrs(attrs);
		layoutSelector = new LayoutSelector(cornerRadius);
	}


	/* Reads the attributes from the layout */
	private void initAttrs(AttributeSet attrs) {
		TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.KPHSegmentedGroup,
				0,
				0
		);

		try {
			marginDp = (int) typedArray.getDimension(
					R.styleable.KPHSegmentedGroup_sc_border_width,
					getResources().getDimension(R.dimen.dimen_radio_button_stroke_border)
			);

			cornerRadius = typedArray.getDimension(
					R.styleable.KPHSegmentedGroup_sc_corner_radius,
					getResources().getDimension(R.dimen.dimen_radio_button_conner_radius)
			);

			tintColor = typedArray.getColor(
					R.styleable.KPHSegmentedGroup_sc_tint_color,
					UIManager.sharedInstance().getColor(R.color.radio_button_selected_color)
			);

			checkedTextColor = typedArray.getColor(
					R.styleable.KPHSegmentedGroup_sc_checked_text_color,
					UIManager.sharedInstance().getColor(R.color.kph_color_light_blue)
			);

			uncheckedBackground = typedArray.getResourceId(
					R.styleable.KPHSegmentedGroup_sc_unchecked_background,
					R.drawable.radio_unchecked
			);
		} finally {
			typedArray.recycle();
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// Use holo light for default
		updateBackground();
	}

	public void setTintColor(int tintColor) {
		this.tintColor = tintColor;
		updateBackground();
	}

	public void setTintColor(int tintColor, int checkedTextColor) {
		this.tintColor = tintColor;
		this.checkedTextColor = checkedTextColor;
		updateBackground();
	}

	public void updateBackground() {
		int count = super.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			updateBackground(i);

			// If this is the last view, don't set LayoutParams
			if (i == count - 1)
				break;

			LayoutParams initParams = (LayoutParams) child.getLayoutParams();
			LayoutParams params = new LayoutParams(initParams.width, initParams.height, initParams.weight);

			// Check orientation for proper margins
			if (getOrientation() == LinearLayout.HORIZONTAL) {
				params.setMargins(0, 0, -marginDp, 0);
			} else {
				params.setMargins(0, 0, 0, -marginDp);
			}
			child.setLayoutParams(params);
		}
	}

	private void updateBackground(int index) {
		View childView = getChildAt(index);

		int checked = layoutSelector.getSelected();
		int unchecked = layoutSelector.getUnselected();

		// Set text color
		ColorStateList colorStateList = new ColorStateList(new int[][] {
				{android.R.attr.state_pressed},
				{-android.R.attr.state_pressed, -android.R.attr.state_checked},
				{-android.R.attr.state_pressed, android.R.attr.state_checked}},
				new int[]{Color.GRAY, tintColor, checkedTextColor});
		((Button) childView).setTextColor(colorStateList);

		// Redraw with tint color
		Drawable checkedDrawable;
		Drawable uncheckedDrawable;

		if (Build.VERSION.SDK_INT < 23) {
			checkedDrawable = resources.getDrawable(checked).mutate();
			uncheckedDrawable = resources.getDrawable(unchecked).mutate();
		} else {
			checkedDrawable = resources.getDrawable(checked, null).mutate();
			uncheckedDrawable = resources.getDrawable(unchecked, null).mutate();
		}

		((GradientDrawable) checkedDrawable).setColor(tintColor);
		((GradientDrawable) checkedDrawable).setStroke(marginDp, tintColor);
		((GradientDrawable) uncheckedDrawable).setStroke(marginDp, tintColor);
		// Set proper radius
		((GradientDrawable) checkedDrawable).setCornerRadii(layoutSelector.getChildRadii(getChildCount(), index));
		((GradientDrawable) uncheckedDrawable).setCornerRadii(layoutSelector.getChildRadii(getChildCount(), index));

		// Create drawable
		StateListDrawable stateListDrawable = new StateListDrawable();
		stateListDrawable.addState(new int[]{-android.R.attr.state_checked}, uncheckedDrawable);
		stateListDrawable.addState(new int[]{android.R.attr.state_checked}, checkedDrawable);

		// Set button background
		if (Build.VERSION.SDK_INT >= 16) {
			childView.setBackground(stateListDrawable);
		} else {
			childView.setBackgroundDrawable(stateListDrawable);
		}
	}

	/*
	 * This class is used to provide the proper layout based on the view.
	 * Also provides the proper radius for corners.
	 * The layout is the same for each selected left/top middle or right/bottom button.
	 * float tables for setting the radius via Gradient.setCornerRadii are used instead
	 * of multiple xml drawables.
	 */
	private class LayoutSelector {
		private final int		SELECTED_LAYOUT = R.drawable.radio_checked;
		private final int		UNSELECTED_LAYOUT = R.drawable.radio_unchecked;

		private float			r1 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.1f, getResources().getDisplayMetrics());    // 0.1 dp to px
		private float[]			rLeft;			// left radio button
		private float[]			rRight;			// right radio button
		private float[]			rMiddle;		// middle radio button
		private float[]			rDefault;		// default radio button
		private float[]			rTop;			// top radio button
		private float[]			rBot;			// bot radio button

		private float			r;				// this is the radios read by attributes or xml dimens


		public LayoutSelector(float cornerRadius) {
			r = cornerRadius;

			rLeft		= new float[]		{r,		r,		r1,		r1,		r1,		r1,		r,		r};
			rRight		= new float[]		{r1,	r1,		r,		r,		r,		r,		r1,		r1};
			rMiddle		= new float[]		{r1,	r1,		r1,		r1,		r1,		r1,		r1,		r1};
			rDefault	= new float[]		{r,		r,		r,		r,		r,		r,		r,		r};
			rTop		= new float[]		{r,		r,		r,		r,		r1,		r1,		r1,		r1};
			rBot		= new float[]		{r1,	r1,		r1,		r1,		r,		r,		r,		r};
		}


		public float[] getChildRadii(int childCount, int childIndex) {
			float[] radiusArray;

			// If there is only one child provide the default radio button
			if (childCount == 1) {
				radiusArray = rDefault;
			} else if (childIndex == 0) { // Left or top
				radiusArray = (getOrientation() == LinearLayout.HORIZONTAL) ? rLeft : rTop;
			} else if (childIndex == childCount - 1) {  // Right or bottom
				radiusArray = (getOrientation() == LinearLayout.HORIZONTAL) ? rRight : rBot;
			} else {  // Middle
				radiusArray = rMiddle;
			}

			return radiusArray;
		}

		/* Returns the selected layout id based on view */
		public int getSelected() {
			return SELECTED_LAYOUT;
		}

		/* Returns the unselected layout id based on view */
		public int getUnselected() {
			return uncheckedBackground;
		}
	}
}