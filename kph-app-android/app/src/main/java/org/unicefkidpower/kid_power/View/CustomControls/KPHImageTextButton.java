package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.util.AttributeSet;

import org.unicefkidpower.kid_power.R;


/**
 * Created by Ruifeng Shi on 9/8/2015.
 */
public class KPHImageTextButton extends KPHIconButton {
	public KPHImageTextButton(Context context) {
		super(context);
	}

	public KPHImageTextButton(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	public KPHImageTextButton(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);
	}

	@Override
	public int itemLayout() {
		return R.layout.item_image_text_button;
	}
}
