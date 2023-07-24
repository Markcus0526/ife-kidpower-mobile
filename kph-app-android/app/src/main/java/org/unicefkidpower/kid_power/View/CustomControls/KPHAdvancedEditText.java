package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.unicefkidpower.kid_power.R;

/**
 * Created by Ruifeng Shi on 1/16/2017.
 */

public class KPHAdvancedEditText extends RelativeLayout {
	public static final int					STATE_VALID				= 0;
	public static final int					STATE_INVALID			= 1;
	public static final int					STATE_PROCESSING		= 2;

	private Context							context;
	private AppCompatEditText				editMain;
	private ImageView						ivProgressIndicator;
	private ImageButton						btnClear;

	private RotateAnimation					rotateAnimation;


	public KPHAdvancedEditText(Context context) {
		super(context);
		this.context = context;
		initialize();
	}

	public KPHAdvancedEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initialize();
	}

	public KPHAdvancedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;
		initialize();
	}

	private void initialize() {
		View contentView = LayoutInflater.from(context).inflate(R.layout.layout_advanced_edittext, this);

		editMain = (AppCompatEditText) contentView.findViewById(R.id.edit_main);
		ivProgressIndicator = (ImageView) contentView.findViewById(R.id.iv_progress_indicator);
		btnClear = (ImageButton) contentView.findViewById(R.id.btn_clear);
		btnClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClearButtonClicked();
			}
		});

		rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotateAnimation.setInterpolator(new LinearInterpolator());
		rotateAnimation.setDuration(2000);
		rotateAnimation.setRepeatCount(Animation.INFINITE);
		rotateAnimation.setFillEnabled(true);
		rotateAnimation.setFillAfter(true);

		ivProgressIndicator.startAnimation(rotateAnimation);
	}


	private void onClearButtonClicked() {
		editMain.setText("");
	}

	public Editable getText() {
		return editMain.getText();
	}

	public void setText(String text) {
		editMain.setText(text);
	}

	public void setState(int state) {
		switch (state) {
			case STATE_VALID:
				editMain.setBackgroundColor(Color.GREEN);
				break;

			case STATE_INVALID:
				editMain.setBackgroundColor(Color.RED);
				break;

			case STATE_PROCESSING:
				editMain.setBackgroundColor(Color.TRANSPARENT);
				ivProgressIndicator.setVisibility(VISIBLE);
				btnClear.setVisibility(INVISIBLE);
				break;
		}
	}


	@Override
	public void setOnFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
		editMain.setOnFocusChangeListener(onFocusChangeListener);
	}


	public void setOnEditorActionListener(TextView.OnEditorActionListener onEditorActionListener) {
		editMain.setOnEditorActionListener(onEditorActionListener);
	}

}
