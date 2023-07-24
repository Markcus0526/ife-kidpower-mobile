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

public class KPHCheerView extends RelativeLayout {
	private Context			parentContext = null;
	private ImageView		ivAvatar = null;
	private KPHTextView		txtStatus = null;
	private KPHTextView		txtName = null;

	private String			_delightStatus;
	private String			_delightName;
	private int				_avatarImage;

	public KPHCheerView(Context context) {
		super(context);

		parentContext = context;
		layoutViews();
	}

	public KPHCheerView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPHCheerView);
		_delightStatus = typedArray.getString(R.styleable.KPHCheerView_avatarStatus);
		_delightName = typedArray.getString(R.styleable.KPHCheerView_avatarName);
		_avatarImage = typedArray.getResourceId(R.styleable.KPHCheerView_avatarImage, R.drawable.souvenir_placeholder);
		typedArray.recycle();

		parentContext = context;
		layoutViews();
	}

	public KPHCheerView(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPHCheerView);
		_delightStatus = typedArray.getString(R.styleable.KPHCheerView_avatarStatus);
		_delightName = typedArray.getString(R.styleable.KPHCheerView_avatarName);
		_avatarImage = typedArray.getResourceId(R.styleable.KPHCheerView_avatarImage, R.drawable.souvenir_placeholder);
		typedArray.recycle();

		parentContext = context;
		layoutViews();
	}

	private void layoutViews() {
		LayoutInflater.from(parentContext).inflate(R.layout.layout_cheer, this);

		ivAvatar = (ImageView) findViewById(R.id.ivAvatar);
		txtStatus = (KPHTextView) findViewById(R.id.tvState);
		txtName = (KPHTextView) findViewById(R.id.tvName);

		ivAvatar.setImageResource(_avatarImage);
		txtStatus.setText(_delightStatus);
		txtName.setText(_delightName);
	}

	public void setStatus(String status) {
		txtStatus.setText(status);
	}

	public void setName(String name) {
		txtName.setText(name);
	}

	public void setAvatarImage(int resourceId) {
		ivAvatar.setImageResource(resourceId);
	}

	public void setAvatarImage(Drawable drawable) {
		if (drawable == null)
			return;

		ivAvatar.setImageDrawable(drawable);
	}

}
