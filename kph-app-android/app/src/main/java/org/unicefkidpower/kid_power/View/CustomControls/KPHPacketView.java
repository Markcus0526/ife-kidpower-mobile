package org.unicefkidpower.kid_power.View.CustomControls;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;

/**
 * Created by Dayong Li on 10/05/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class KPHPacketView extends RelativeLayout {
	private Context				parentContext = null;
	private ImageView			ivPacket = null;
	private View				aniContent = null;
	private KPHTextView			txtPercent = null;
	private KPHTextView			txtBadge = null;
	private RelativeLayout		layoutBadge = null;
	private LinearLayout		llAniBackground = null;

	private String				_packetStatus;
	private int					_packetBadge;
	private int					_packetImage;

	private boolean				_isAnimating;
	private ValueAnimator		_animation;


	public KPHPacketView(Context context) {
		super(context);

		parentContext = context;
		layoutViews();
	}

	public KPHPacketView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPHPacketView);
		_isAnimating = typedArray.getBoolean(R.styleable.KPHPacketView_convertAnimating, false);
		_packetStatus = typedArray.getString(R.styleable.KPHPacketView_packetStatus);
		_packetBadge = typedArray.getInteger(R.styleable.KPHPacketView_packetBadge, 0);
		_packetImage = typedArray.getResourceId(R.styleable.KPHPacketView_packetImage, R.drawable.anim_packet_sync);
		typedArray.recycle();

		parentContext = context;
		layoutViews();
	}

	public KPHPacketView(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPHPacketView);
		_isAnimating = typedArray.getBoolean(R.styleable.KPHPacketView_convertAnimating, false);
		_packetStatus = typedArray.getString(R.styleable.KPHPacketView_packetStatus);
		_packetBadge = typedArray.getInteger(R.styleable.KPHPacketView_packetBadge, 0);
		_packetImage = typedArray.getResourceId(R.styleable.KPHPacketView_packetImage, R.drawable.anim_packet_sync);
		typedArray.recycle();

		parentContext = context;
		layoutViews();
	}

	private void layoutViews() {
		LayoutInflater.from(parentContext).inflate(R.layout.layout_packet, this);

		ivPacket = (ImageView) findViewById(R.id.ivPacket);
		aniContent = findViewById(R.id.aniContent);
		txtPercent = (KPHTextView) findViewById(R.id.tvPercent);
		txtBadge = (KPHTextView) findViewById(R.id.tvBadge);
		layoutBadge = (RelativeLayout) findViewById(R.id.layout_badge);
		llAniBackground = (LinearLayout) findViewById(R.id.aniBackground);

		llAniBackground.setVisibility(GONE);
		ivPacket.setImageResource(_packetImage);
		txtPercent.setText(_packetStatus);
		txtBadge.setText("" + _packetBadge);
		if (_packetBadge == 0) {
			layoutBadge.setVisibility(GONE);
		} else {
			layoutBadge.setVisibility(VISIBLE);
		}

		_animation = ValueAnimator.ofInt(0, 100);
		_animation.setDuration(1000);
		_animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) aniContent.getLayoutParams();

				lp.height = ResolutionSet.getPixelFromDP(parentContext, ((Integer) animation.getAnimatedValue()).intValue());
				aniContent.setLayoutParams(lp);
			}
		});
	}

	public void setString(String hints) {
		txtPercent.setText(hints);
	}

	public void setBadgeCount(int count) {
		if (count == 0) {
			layoutBadge.setVisibility(GONE);
		} else {
			layoutBadge.setVisibility(VISIBLE);
			txtBadge.setText("" + count);
		}
	}

	public void setPacketImage(int resourceId) {
		ivPacket.setImageResource(resourceId);
	}

	public void setPacketImage(Drawable drawable) {
		if (drawable == null)
			return;

		ivPacket.setImageDrawable(drawable);
	}

	public void startAnimation() {
		if (_isAnimating)
			return;

		llAniBackground.setVisibility(VISIBLE);
		_isAnimating = true;
		_animation.setRepeatCount(Animation.INFINITE);
		_animation.start();
	}

	public void stopAnimation() {
		llAniBackground.setVisibility(GONE);
		_isAnimating = false;
		_animation.setRepeatMode(Animation.ABSOLUTE);
		_animation.setRepeatCount(0);
		_animation.cancel();
	}
}
