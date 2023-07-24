package org.unicefkidpower.schools.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.unicefkidpower.schools.R;

public class KPSideMenu extends LinearLayout {
	protected Context parentContext;

	// UI Elements
	protected boolean isActive;
	private View contentView;
	private ImageView ivItem;
	private TextView txtLabel;

	// Attributes
	protected String labelFont = "";
	protected String labelCaption = "";
	private int normalImage;
	private int activeImage;

	public KPSideMenu(Context context) {
		super(context);

		parentContext = context;
		layoutViews();
	}

	public KPSideMenu(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPSideMenu);

		labelCaption = typedArray.getString(R.styleable.KPSideMenu_smText);
		labelFont = typedArray.getString(R.styleable.KPSideMenu_smFont);
		if (labelFont == null) {
			labelFont = context.getString(R.string.font_pfdindisplaypro_regular);
		}
		normalImage = typedArray.getResourceId(R.styleable.KPSideMenu_smImage, R.drawable.icon_team);
		activeImage = typedArray.getResourceId(R.styleable.KPSideMenu_smActiveImage, 0);
		isActive = typedArray.getBoolean(R.styleable.KPSideMenu_smIsActive, false);
		typedArray.recycle();

		parentContext = context;
		layoutViews();
	}

	public KPSideMenu(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPSideMenu);
		labelCaption = typedArray.getString(R.styleable.KPSideMenu_smText);
		labelFont = typedArray.getString(R.styleable.KPSideMenu_smFont);
		if (labelFont == null) {
			labelFont = context.getString(R.string.font_pfdindisplaypro_regular);
		}
		normalImage = typedArray.getResourceId(R.styleable.KPSideMenu_smImage, R.drawable.icon_team);
		activeImage = typedArray.getResourceId(R.styleable.KPSideMenu_smActiveImage, 0);
		isActive = typedArray.getBoolean(R.styleable.KPSideMenu_smIsActive, false);
		typedArray.recycle();

		parentContext = context;
		layoutViews();
	}

	public void layoutViews() {
		contentView = LayoutInflater.from(parentContext).inflate(R.layout.layout_sidebar_item, this);

		ivItem = (ImageView) contentView.findViewById(R.id.ivItem);
		txtLabel = (TextView) contentView.findViewById(R.id.txtLabel);

		txtLabel.setText(labelCaption);
		setCustomFont(parentContext, labelFont);

		if (isActive) {
			contentView.setBackgroundColor(ContextCompat.getColor(parentContext, R.color.sidebar_background_active));
			txtLabel.setTextColor(ContextCompat.getColor(parentContext, R.color.sidebar_text_active));
			ivItem.setImageResource(activeImage != 0 ? activeImage : normalImage);
		} else {
			contentView.setBackgroundColor(ContextCompat.getColor(parentContext, R.color.sidebar_background_inactive));
			txtLabel.setTextColor(ContextCompat.getColor(parentContext, R.color.sidebar_text_inactive));
			ivItem.setImageResource(normalImage);
		}
	}

	public boolean setCustomFont(Context context, String asset) {
		if (asset == null || asset.isEmpty())
			return false;

		Typeface typeface = null;
		try {
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + asset);
		} catch (Exception e) {
			Log.e("KPSideMenu", "Could not get typeface: " + e.getMessage());
			return false;
		}
		txtLabel.setTypeface(typeface);
		return true;
	}

	public void setText(String text) {
		if (txtLabel != null) {
			txtLabel.setText(text);
		}
	}

	public void setText(CharSequence text) {
		if (txtLabel != null) {
			txtLabel.setText(text);
		}
	}

	public void setTextColor(int color) {
		if (txtLabel != null) {
			txtLabel.setTextColor(color);
		}
	}

	public void setImage(int nImageResource) {
		normalImage = nImageResource;
		if (txtLabel != null && !isActive) {
			ivItem.setImageResource(normalImage);
		}
	}

	public void setActiveImage(int nImageResource) {
		activeImage = nImageResource;
		if (txtLabel != null && isActive) {
			ivItem.setImageResource(activeImage);
		}
	}

	public void setActive(boolean isActive) {
		if (contentView == null)
			return;

		this.isActive = isActive;
		if (isActive) {
			contentView.setBackgroundColor(ContextCompat.getColor(parentContext, R.color.sidebar_background_active));
			txtLabel.setTextColor(ContextCompat.getColor(parentContext, R.color.sidebar_text_active));
			ivItem.setImageResource(activeImage != 0 ? activeImage : normalImage);
		} else {
			contentView.setBackgroundColor(ContextCompat.getColor(parentContext, R.color.sidebar_background_inactive));
			txtLabel.setTextColor(ContextCompat.getColor(parentContext, R.color.sidebar_text_inactive));
			ivItem.setImageResource(normalImage);
		}
	}

	public void setGravity(int gravity) {
		((LinearLayout) contentView.findViewById(R.id.layout_content)).setGravity(gravity);
	}
}
