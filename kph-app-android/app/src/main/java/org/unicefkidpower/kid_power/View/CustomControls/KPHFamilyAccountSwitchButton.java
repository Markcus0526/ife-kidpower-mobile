package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.unicefkidpower.kid_power.R;

/**
 * Created by Ruifeng Shi on 1/28/2016.
 */
public class KPHFamilyAccountSwitchButton extends RelativeLayout {
	private Context			contextInstance;
	private ImageView		ivSmallAvatar;


	public KPHFamilyAccountSwitchButton(Context context) {
		super(context);

		this.contextInstance = context;
		initialize();
	}

	public KPHFamilyAccountSwitchButton(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.contextInstance = context;
		initialize();
	}

	public KPHFamilyAccountSwitchButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		this.contextInstance = context;
		initialize();
	}

	protected void initialize() {
		View contentView = LayoutInflater.from(contextInstance).inflate(R.layout.layout_family_account_switch_button, this);
		ivSmallAvatar = (ImageView) contentView.findViewById(R.id.ivSmallAvatar);
	}

	public void setAvatarDrawable(Drawable drawable) {
		ivSmallAvatar.setImageDrawable(drawable);
	}

}
