package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.util.AttributeSet;

import org.unicefkidpower.kid_power.R;

/**
 * Created by Ruifeng Shi on 3/8/2017.
 */

public class KPHTextImageButton extends KPHIconButton {
	public KPHTextImageButton(Context context) {
		super(context);
	}

	public KPHTextImageButton(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	public KPHTextImageButton(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);
	}

	@Override
	public int itemLayout() {
		return R.layout.item_text_image_button;
	}
}
