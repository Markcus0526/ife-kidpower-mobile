package org.unicefkidpower.schools;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.unicefkidpower.schools.helper.DebouncedOnClickListener;

public class ConfirmDialog extends Dialog {
	private String _title;
	private String _positive;
	private String _negative;

	protected Activity _parentActivity;
	protected SaveConfirmDialogListener _listener;

	public ConfirmDialog(Activity parentActivity, SaveConfirmDialogListener listener) {
		super(parentActivity);

		_parentActivity = parentActivity;
		_listener = listener;
	}

	public ConfirmDialog setCaption(String title) {
		_title = title;
		return this;
	}

	public ConfirmDialog setButtonTitle(String positive, String negative) {
		_positive = positive;
		_negative = negative;
		return this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_confirm);

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		getWindow().setAttributes(lp);

		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		if (!TextUtils.isEmpty(_title)) {
			((TextView) findViewById(R.id.tvDescription)).setText(_title);
		}
		if (!TextUtils.isEmpty(_positive)) {
			((TextView) findViewById(R.id.btnOK)).setText(_positive);
		}
		if (!TextUtils.isEmpty(_negative)) {
			((TextView) findViewById(R.id.btnCancel)).setText(_negative);
		}

		findViewById(R.id.btnCancel).setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				dismiss();
				_listener.onNegative();
			}
		});

		findViewById(R.id.btnOK).setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				dismiss();
				_listener.onPositive();
			}
		});
	}

	public interface SaveConfirmDialogListener {
		void onPositive();

		void onNegative();
	}
}
