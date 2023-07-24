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
 * Created by Ruifeng Shi on 9/8/2015.
 */
public class KPHImageLabelTextButton extends RelativeLayout {
	private Context				parentContext;

	//UI Elements
	private View				contentView;
	private ImageView			ivItem;
	private KPHTextView			lblLabel;
	private KPHTextView			lblValue;

	//Attributes
	private int					customImageSize = 30;
	private int					customImageResource;

	private String				valueFont = "";
	private String				valueText = "";
	private int					valueSize = 0;
	private int					valueColor;

	private String				labelFont = "";
	private String				labelText = "";
	private int					labelSize = 0;
	private int					labelColor;


	public KPHImageLabelTextButton(Context context) {
		super(context);

		parentContext = context;
		layoutViews();
	}

	public KPHImageLabelTextButton(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPHImageLabelTextButton);

		customImageSize = (int) typedArray.getDimension(R.styleable.KPHImageLabelTextButton_ILTImageSize, 30.0f);
		customImageResource = typedArray.getResourceId(R.styleable.KPHImageLabelTextButton_ILTImageResourceId, R.drawable.packet);

		valueText = typedArray.getString(R.styleable.KPHImageLabelTextButton_ILTValue);
		valueFont = typedArray.getString(R.styleable.KPHImageLabelTextButton_ILTValueFont);
		valueColor = typedArray.getColor(R.styleable.KPHImageLabelTextButton_ILTValueColor, Color.WHITE);
		valueSize = (int) (typedArray.getDimension(
				R.styleable.KPHImageLabelTextButton_ILTValueSize,
				context.getResources().getDimension(R.dimen.dimen_font_size_heading_count)) /
				context.getResources().getDisplayMetrics().density
		);

		labelText = typedArray.getString(R.styleable.KPHImageLabelTextButton_ILTLabel);
		labelFont = typedArray.getString(R.styleable.KPHImageLabelTextButton_ILTLabelFont);
		labelColor = typedArray.getColor(R.styleable.KPHImageLabelTextButton_ILTLabelColor, Color.WHITE);
		labelSize = (int) (typedArray.getDimension(
				R.styleable.KPHImageLabelTextButton_ILTLabelSize,
				context.getResources().getDimension(R.dimen.dimen_font_size_base_small)) /
				context.getResources().getDisplayMetrics().density
		);

		typedArray.recycle();

		parentContext = context;
		layoutViews();
	}

	public KPHImageLabelTextButton(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);

		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPHImageLabelTextButton);

		customImageSize = (int) typedArray.getDimension(R.styleable.KPHImageLabelTextButton_ILTImageSize, 30.0f);
		customImageResource = typedArray.getResourceId(R.styleable.KPHImageLabelTextButton_ILTImageResourceId, R.drawable.packet);

		valueText = typedArray.getString(R.styleable.KPHImageLabelTextButton_ILTValue);
		valueFont = typedArray.getString(R.styleable.KPHImageLabelTextButton_ILTValueFont);
		valueColor = typedArray.getColor(R.styleable.KPHImageLabelTextButton_ILTValueColor, Color.WHITE);
		valueSize = (int) (typedArray.getDimension(
				R.styleable.KPHImageLabelTextButton_ILTValueSize,
				context.getResources().getDimension(R.dimen.dimen_font_size_heading_count)) /
				context.getResources().getDisplayMetrics().density
		);

		labelText = typedArray.getString(R.styleable.KPHImageLabelTextButton_ILTLabel);
		labelFont = typedArray.getString(R.styleable.KPHImageLabelTextButton_ILTLabelFont);
		labelColor = typedArray.getColor(R.styleable.KPHImageLabelTextButton_ILTLabelColor, Color.WHITE);
		labelSize = (int) (typedArray.getDimension(
				R.styleable.KPHImageLabelTextButton_ILTLabelSize,
				context.getResources().getDimension(R.dimen.dimen_font_size_base_small)) /
				context.getResources().getDisplayMetrics().density
		);

		typedArray.recycle();

		parentContext = context;
		layoutViews();
	}

	public void layoutViews() {
		contentView = LayoutInflater.from(parentContext).inflate(R.layout.item_image_label_text_button, this);

		ivItem = (ImageView) contentView.findViewById(R.id.ivItem);
		lblValue = (KPHTextView) contentView.findViewById(R.id.lblValue);
		lblLabel = (KPHTextView) contentView.findViewById(R.id.lblLabel);

		ViewGroup.LayoutParams params = ivItem.getLayoutParams();
		params.width = customImageSize;
		params.height = customImageSize;

		ivItem.setLayoutParams(params);
		ivItem.setImageResource(customImageResource);

		lblValue.setText(valueText);
		lblValue.setTextColor(valueColor);
		lblValue.setTextSize(valueSize);
		setValueFont(parentContext, valueFont);

		lblLabel.setText(labelText);
		lblLabel.setTextColor(labelColor);
		lblLabel.setTextSize(labelSize);
		//setLabelFont(parentContext, labelFont);
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
		String labelText = lblLabel.getText().toString();

		int textSize = (int)lblLabel.getTextSize();

		TextPaint tp = new TextPaint();
		tp.setTypeface(lblLabel.getTypeface());
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

		lblLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
	}

	public boolean setValueFont(Context context, String asset) {
		Typeface typeface = null;
		try {
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + asset);
		} catch (Exception e) {
			Log.e(lblValue.getTag().toString(), "Could not getV1 typeface: " + e.getMessage());
			return false;
		}
		lblValue.setTypeface(typeface);
		return true;
	}

	public void setValue(String text) {
		if (lblValue != null) {
			lblValue.setText(text);
		}
	}

	public void setValue(CharSequence text) {
		if (lblValue != null) {
			lblValue.setText(text);
		}
	}

	public void setNumericValue(int numericText) {
		if (lblValue != null) {
			lblValue.setText(String.valueOf(numericText));
		}
	}

	public void setValueColor(int color) {
		if (lblValue != null) {
			lblValue.setTextColor(color);
		}
	}

	public boolean setLabelFont(Context context, String asset) {
		Typeface typeface = null;
		try {
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + asset);
		} catch (Exception e) {
			Log.e(lblLabel.getTag().toString(), "Could not getV1 typeface: " + e.getMessage());
			return false;
		}
		lblLabel.setTypeface(typeface);
		return true;
	}

	public void setLabel(String text) {
		if (lblLabel != null) {
			lblLabel.setText(text);
		}
	}

	public void setLabel(CharSequence text) {
		if (lblLabel != null) {
			lblLabel.setText(text);
		}
	}

	public void setLabelColor(int color) {
		if (lblLabel != null) {
			lblLabel.setTextColor(color);
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
