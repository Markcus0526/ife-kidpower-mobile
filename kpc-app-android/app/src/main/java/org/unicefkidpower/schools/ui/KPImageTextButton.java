package org.unicefkidpower.schools.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.unicefkidpower.schools.R;

/**
 * Created by Ruifeng Shi on 9/8/2015.
 */
public class KPImageTextButton extends RelativeLayout {
	protected Context parentContext;

	// UI Elements
	private View contentView;
	private ImageView ivItem;
	private TextView txtLabel;

	// Attributes
	protected String customFont = "";
	protected String customLabel = "";
	protected int customImageSize = 30;
	protected int customTextSize = 16;
	private int customImageResource = R.drawable.sync_white;
	private int customTextColor = Color.WHITE;

	public KPImageTextButton(Context context) {
		super(context);

		parentContext = context;
		layoutViews();
	}

	public KPImageTextButton(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPImageTextButton);
		customImageSize = (int) typedArray.getDimension(R.styleable.KPImageTextButton_customImageSize, 30.0f);
		customLabel = typedArray.getString(R.styleable.KPImageTextButton_customText);
		customFont = typedArray.getString(R.styleable.KPImageTextButton_customImageTextButtonFont);
		customImageResource = typedArray.getResourceId(R.styleable.KPImageTextButton_customImageResourceId, R.drawable.sync_white);
		customTextColor = typedArray.getColor(R.styleable.KPImageTextButton_customTextColor, Color.WHITE);
		customTextSize = (int) (typedArray.getDimension(
				R.styleable.KPImageTextButton_customTextSize,
				context.getResources().getDimension(R.dimen.fontsize_normal)) /
				context.getResources().getDisplayMetrics().density
		);
		typedArray.recycle();

		parentContext = context;
		layoutViews();
	}

	public KPImageTextButton(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPImageTextButton);
		customImageSize = (int) typedArray.getDimension(R.styleable.KPImageTextButton_customImageSize, 30.0f);
		customLabel = typedArray.getString(R.styleable.KPImageTextButton_customText);
		customFont = typedArray.getString(R.styleable.KPImageTextButton_customImageTextButtonFont);
		customImageResource = typedArray.getResourceId(R.styleable.KPImageTextButton_customImageResourceId, R.drawable.sync_white);
		customTextColor = typedArray.getColor(R.styleable.KPImageTextButton_customTextColor, Color.WHITE);
		customTextSize = (int) (typedArray.getDimension(
				R.styleable.KPImageTextButton_customTextSize,
				context.getResources().getDimension(R.dimen.fontsize_normal)) /
				context.getResources().getDisplayMetrics().density
		);
		typedArray.recycle();

		parentContext = context;
		layoutViews();
	}

	public void layoutViews() {
		contentView = LayoutInflater.from(parentContext).inflate(R.layout.layout_image_text_button, this);

		ivItem = (ImageView) contentView.findViewById(R.id.iv_item);
		txtLabel = (TextView) contentView.findViewById(R.id.txt_label);

		ViewGroup.LayoutParams params = ivItem.getLayoutParams();
		params.width = customImageSize;
		params.height = customImageSize;
		ivItem.setLayoutParams(params);
		ivItem.setImageResource(customImageResource);
		txtLabel.setText(customLabel);
		txtLabel.setTextColor(customTextColor);
		txtLabel.setTextSize(customTextSize);
		setCustomFont(parentContext, customFont);
	}

	public boolean setCustomFont(Context context, String asset) {
		if (TextUtils.isEmpty(asset))
			return false;

		Typeface typeface;
		try {
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + asset);
		} catch (Exception e) {
			Log.e(txtLabel.getTag().toString(), "Could not get typeface: " + e.getMessage());
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

	public void setTextSize(int customTextSize) {
		this.customTextSize = customTextSize;

		if (txtLabel != null) {
			txtLabel.setTextSize(customTextSize);
		}
	}

	public void setTextColor(int color) {
		if (txtLabel != null) {
			txtLabel.setTextColor(color);
		}
	}

	public void setCustomImage(int resourceId) {
		ivItem.setImageResource(resourceId);
	}

	public void setCustomImage(Drawable drawable) {
		if (drawable == null) {
			ivItem.setVisibility(GONE);
		} else {
			ivItem.setVisibility(VISIBLE);
		}

		ivItem.setImageDrawable(drawable);
	}

	public void setGravity(int gravity) {
		((RelativeLayout) contentView.findViewById(R.id.layout_content)).setGravity(gravity);
	}

	public String getText() {
		return txtLabel.getText().toString();
	}
}
