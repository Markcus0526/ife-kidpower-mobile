package org.unicefkidpower.kid_power.View.CustomControls;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;

import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;


/**
 * Created by Dayong on 12/4/2016.
 */
public class KPHBrandedDialog extends Dialog {
	// UI Elements
	private KPHBrandedDialogCallback _callback;
	private KPHTextView				tvMessage;
	private KPHTextView				btnDefault, btnOther;


	private String					_message;
	private String					_captionDefault;
	private String					_captionOther;


	public KPHBrandedDialog(@NonNull Context context) {
		super(context, R.style.KidPowerDialogStyle);
	}

	public KPHBrandedDialog(@NonNull Context context, @StyleRes int themeResId) {
		super(context, R.style.KidPowerDialogStyle);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_branded_alert);

		resizeDialog();

		tvMessage = (KPHTextView) findViewById(R.id.tvMessage);
		tvMessage.setText(_message);

		btnDefault = (KPHTextView) findViewById(R.id.tvDefault);
		btnDefault.setText(_captionDefault);
		btnDefault.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (_callback != null) {
					_callback.onDefaultButtonClicked();
				}
				dismiss();
			}
		});


		btnOther = (KPHTextView) findViewById(R.id.tvOther);
		if (_captionOther != null) {
			btnOther.setText(_captionOther);
			btnOther.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (_callback != null) {
						_callback.onOtherButtonClicked();
					}
					dismiss();
				}
			});
		}

		if (TextUtils.isEmpty(_captionOther)) {
			btnOther.setVisibility(View.GONE);
		}
	}

	protected void resizeDialog() {
		Point screenSize = ResolutionSet.getScreenSize(getContext(), false);
		int screenWidth = screenSize.x;
		int screenHeight = screenSize.y - ResolutionSet.getStatusBarHeight(getContext());

		int width = screenWidth - getContext().getResources().getDimensionPixelSize(R.dimen.dimen_16dp) * 2;
		int height = screenHeight - getContext().getResources().getDimensionPixelSize(R.dimen.dimen_16dp) * 2;

		getWindow().setLayout(width, height);
	}


	public void setCallback(String msg, String def, String other, KPHBrandedDialogCallback callback) {
		_message = msg;
		_captionDefault = def;
		_captionOther = other;

		_callback = callback;
	}


	@Override
	public void onStop() {
		super.onStop();

		if (_callback != null) {
			_callback.onDefaultButtonClicked();
		}

	}

	public interface KPHBrandedDialogCallback {
		void onDefaultButtonClicked();
		void onOtherButtonClicked();
	}

}
