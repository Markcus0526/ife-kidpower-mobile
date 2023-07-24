package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.unicefkidpower.kid_power.R;

/**
 * Created by Ruifeng Shi on 9/1/2015.
 */
public abstract class KPHIconButton extends RelativeLayout {
	private Context				parentContext;

	// UI Elements
	private View				contentView;
	private ImageView			ivItem;
	private KPHTextView			txtLabel;

	// Attributes
	private String				customFont = "";
	private String				customLabel = "";
	private int					customImageSize = 30;
	private int					customTextSize = 0;
	private int					customImageResource;
	private int					customTextColor;


	public KPHIconButton(Context context) {
		super(context);

		parentContext = context;
		layoutViews();
	}

	public KPHIconButton(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPHImageTextButton);

		customImageSize = (int) typedArray.getDimension(R.styleable.KPHImageTextButton_customImageSize, 30.0f);
		customLabel = typedArray.getString(R.styleable.KPHImageTextButton_customText);
		customFont = typedArray.getString(R.styleable.KPHImageTextButton_customImageTextButtonFont);
		customImageResource = typedArray.getResourceId(R.styleable.KPHImageTextButton_customImageResourceId, R.drawable.packet);
		customTextColor = typedArray.getColor(R.styleable.KPHImageTextButton_customTextColor, Color.WHITE);
		customTextSize = (int) (
				typedArray.getDimension(R.styleable.KPHImageTextButton_customTextSize,
						context.getResources().getDimension(R.dimen.dimen_font_size_heading_count)) /
						context.getResources().getDisplayMetrics().density
		);
		typedArray.recycle();

		parentContext = context;
		layoutViews();
	}

	public KPHIconButton(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPHImageTextButton);

		customImageSize = (int) typedArray.getDimension(R.styleable.KPHImageTextButton_customImageSize, 30.0f);
		customLabel = typedArray.getString(R.styleable.KPHImageTextButton_customText);
		customFont = typedArray.getString(R.styleable.KPHImageTextButton_customImageTextButtonFont);
		customImageResource = typedArray.getResourceId(R.styleable.KPHImageTextButton_customImageResourceId, R.drawable.packet);
		customTextColor = typedArray.getColor(R.styleable.KPHImageTextButton_customTextColor, Color.WHITE);
		customTextSize = (int) (typedArray.getDimension(
				R.styleable.KPHImageTextButton_customTextSize,
				context.getResources().getDimension(R.dimen.dimen_font_size_heading_count)) /
				context.getResources().getDisplayMetrics().density
		);
		typedArray.recycle();

		parentContext = context;
		layoutViews();
	}

	public abstract int itemLayout();

	public void layoutViews() {
		contentView = LayoutInflater.from(parentContext).inflate(itemLayout(), this);

		ivItem = (ImageView) contentView.findViewById(R.id.ivItem);
		txtLabel = (KPHTextView) contentView.findViewById(R.id.txtLabel);

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
		if (asset == null || asset.isEmpty())
			return false;

		Typeface typeface = null;
		try {
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + asset);
		} catch (Exception e) {
			Log.e(txtLabel.getTag().toString(), "Could not getV1 typeface: " + e.getMessage());
			return false;
		}
		txtLabel.setTypeface(typeface);
		return true;
	}

	/**
	 * Do not call this method if you're not developer of this method
	 * This method assumes the image size is one third of the total size and resizes font and images
	 */
	public void autoFitSize(int totalWidth) {
		int imageWeight = 1;
		int totalWeight = 3;

		int imageWidth = totalWidth * imageWeight / totalWeight;
		int textMaxWidth = totalWidth - imageWidth;

		ViewGroup.LayoutParams image_layoutParams = ivItem.getLayoutParams();
		image_layoutParams.width = imageWidth;
		ivItem.setLayoutParams(image_layoutParams);

		Rect textBounds = new Rect();
		String labelText = txtLabel.getText().toString();

		int textSize = (int)txtLabel.getTextSize();

		TextPaint tp = new TextPaint();
		tp.setTypeface(txtLabel.getTypeface());
		tp.setTextSize(textSize);

		tp.getTextBounds(labelText, 0, labelText.length(), textBounds);

		while (textBounds.width() >= textMaxWidth) {
			textSize--;
			if (textSize <= 0) {
				textSize = 1;
				break;
			}

			tp.setTextSize(textSize);
			tp.getTextBounds(labelText, 0, labelText.length(), textBounds);
		}

		txtLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
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

	public void setNumericText(int numericText) {
		if (txtLabel != null) {
			txtLabel.setText(String.valueOf(numericText));
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
}
