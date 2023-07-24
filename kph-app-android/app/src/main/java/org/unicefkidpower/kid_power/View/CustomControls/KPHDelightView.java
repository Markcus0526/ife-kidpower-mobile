package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.unicefkidpower.kid_power.R;

/**
 * Created by Dayong Li on 10/05/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class KPHDelightView extends RelativeLayout {
	private Context				parentContext = null;
	private ImageView			ivDelight = null;
	private KPHTextView			txtStatus = null;
	private KPHTextView			txtName = null;

	private String				_delightStatus;
	private String				_delightName;
	private int					_delightImage;


	public KPHDelightView(Context context) {
		super(context);

		parentContext = context;
		layoutViews();
	}

	public KPHDelightView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPHDelightView);

		_delightStatus = typedArray.getString(R.styleable.KPHDelightView_delightStatus);
		_delightName = typedArray.getString(R.styleable.KPHDelightView_delightName);
		_delightImage = typedArray.getResourceId(R.styleable.KPHDelightView_delightImage, R.drawable.souvenir_placeholder);
		typedArray.recycle();

		parentContext = context;

		layoutViews();
	}

	public KPHDelightView(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPHDelightView);

		_delightStatus = typedArray.getString(R.styleable.KPHDelightView_delightStatus);
		_delightName = typedArray.getString(R.styleable.KPHDelightView_delightName);
		_delightImage = typedArray.getResourceId(R.styleable.KPHDelightView_delightImage, R.drawable.souvenir_placeholder);

		typedArray.recycle();

		parentContext = context;
		layoutViews();
	}

	private void layoutViews() {
		LayoutInflater.from(parentContext).inflate(R.layout.layout_delight, this);

		ivDelight = (ImageView) findViewById(R.id.ivDelight);
		txtStatus = (KPHTextView) findViewById(R.id.tvState);
		txtName = (KPHTextView) findViewById(R.id.tvName);

		ivDelight.setImageResource(_delightImage);
		txtStatus.setText(_delightStatus);
		txtName.setText(_delightName);
	}

	public void setStatus(String status) {
		txtStatus.setText(status);
	}

	public void setName(String name) {
		txtName.setText(name);
	}

	public void setDelightImage(int resourceId) {
		ivDelight.setImageResource(resourceId);
	}

	public void setDelightImage(Drawable drawable) {
		if (drawable == null)
			return;

		ivDelight.setImageDrawable(drawable);
	}

}
