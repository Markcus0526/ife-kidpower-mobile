package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.R;

/**
 * Created by Dayong Li on 10/05/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class KPHSyncMark extends RelativeLayout {
	private Context			parentContext = null;

	private ImageView		ivBackground = null;
	private ImageView		ivAvatar = null;
	private ImageView		ivGoogleFit = null;

	private KPHTextView		txtStatus = null;
	private KPHTextView		txtName = null;

	private boolean			isAnimating;
	private String			syncName;
	private String			syncStatus;
	private int				syncAvatar;

	private Animation		animation;

	private int 			trackerType;


	public KPHSyncMark(Context context) {
		super(context);

		parentContext = context;
		layoutViews();
	}

	public KPHSyncMark(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPHSyncMark);
		isAnimating = typedArray.getBoolean(R.styleable.KPHSyncMark_isAnimating, false);
		syncName = typedArray.getString(R.styleable.KPHSyncMark_syncName);
		syncStatus = typedArray.getString(R.styleable.KPHSyncMark_syncStatus);
		syncAvatar = typedArray.getResourceId(R.styleable.KPHSyncMark_syncAvatar, R.drawable.avatar_large_archery);
		typedArray.recycle();

		parentContext = context;
		layoutViews();
	}

	public KPHSyncMark(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPHSyncMark);
		isAnimating = typedArray.getBoolean(R.styleable.KPHSyncMark_isAnimating, false);
		syncName = typedArray.getString(R.styleable.KPHSyncMark_syncName);
		syncStatus = typedArray.getString(R.styleable.KPHSyncMark_syncStatus);
		syncAvatar = typedArray.getResourceId(R.styleable.KPHSyncMark_syncAvatar, R.drawable.avatar_placeholder);
		typedArray.recycle();

		parentContext = context;
		layoutViews();
	}

	private void layoutViews() {
		LayoutInflater.from(parentContext).inflate(R.layout.layout_sync_mark, this);

		ivBackground = (ImageView) findViewById(R.id.ivBackground);
		ivAvatar = (ImageView) findViewById(R.id.ivAvatar);
		ivGoogleFit = (ImageView) findViewById(R.id.ivGoogleFit);
		txtStatus = (KPHTextView) findViewById(R.id.txtStatus);
		txtName = (KPHTextView) findViewById(R.id.txtName);

		ivAvatar.setImageResource(syncAvatar);
		ivGoogleFit.setVisibility(GONE);

		if (syncStatus != null)
			txtStatus.setText(syncStatus);
		if (syncName != null)
			txtName.setText(syncName);

		animation = AnimationUtils.loadAnimation(parentContext, R.anim.rotate);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}
			@Override
			public void onAnimationEnd(Animation animation) {}
			@Override
			public void onAnimationRepeat(Animation animation) {
				animation.start();
			}
		});
	}

	public void setStatus(String status) {
		if (TextUtils.isEmpty(status)) {
			txtStatus.setVisibility(GONE);
		} else {
			txtStatus.setVisibility(VISIBLE);
			txtStatus.setText(status);
		}
	}

	public void setName(String name) {
		txtName.setText(name);
	}

	public void setAvatar(int resourceId) {
		ivAvatar.setImageResource(resourceId);
	}

	public void setAvatar(Drawable drawable) {
		if (drawable == null)
			return;

		ivAvatar.setImageDrawable(drawable);
	}

	public void startAnimation(int deviceType) {
		if (isAnimating)
			return;

		isAnimating = true;

		trackerType = deviceType;
		if (trackerType == KPHUserService.TRACKER_TYPE_GOOGLEFIT)
			ivBackground.setImageResource(R.drawable.anim_sync_googlefit);
		else
			ivBackground.setImageResource(R.drawable.anim_sync_band);

		animation.setRepeatCount(Animation.INFINITE);
		ivBackground.startAnimation(animation);
	}

	public void stopAnimation() {
		isAnimating = false;

		ivBackground.setAnimation(null);
		animation.setRepeatMode(Animation.ABSOLUTE);
		animation.setRepeatCount(0);
		animation.cancel();
		animation.reset();

		if (trackerType == KPHUserService.TRACKER_TYPE_GOOGLEFIT) {
			ivBackground.setImageResource(R.drawable.badge_background_googlefit);
			ivGoogleFit.setVisibility(VISIBLE);
		} else {
			ivBackground.setImageResource(R.drawable.badge_background);
		}
	}
}
