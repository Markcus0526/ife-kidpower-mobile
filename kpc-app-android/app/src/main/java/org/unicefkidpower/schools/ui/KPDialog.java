package org.unicefkidpower.schools.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;

import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.helper.DebouncedOnClickListener;

/**
 * Created by Ruifeng Shi on 9/1/2016.
 */
public class KPDialog extends Dialog {
	public static int DIALOG_MODE_OK_CANCEL = 0;
	public static int DIALOG_MODE_OK = 1;

	Context context;
	KPTextView txtTitle, txtDescription;
	KPButton btnCancel, btnOK;

	int mode = DIALOG_MODE_OK_CANCEL;
	String title, description, okText, cancelText;

	OnDialogItemClickListener onDialogItemClickListener;

	public KPDialog(Context context, String title, String description) {
		super(context);
		this.context = context;
		this.title = title;
		this.description = description;
	}

	public KPDialog(Context context, int titleRes, int descriptionRes) {
		super(context);
		this.context = context;
		this.title = context.getString(titleRes);
		this.description = context.getString(descriptionRes);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_dialog);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setCanceledOnTouchOutside(false);

		initControl();
	}

	private void initControl() {
		txtTitle = (KPTextView) findViewById(R.id.txt_title);
		txtDescription = (KPTextView) findViewById(R.id.txt_description);
		btnOK = (KPButton) findViewById(R.id.btn_ok);
		btnCancel = (KPButton) findViewById(R.id.btn_cancel);

		txtTitle.setText(title);
		txtDescription.setText(description);

		if (!TextUtils.isEmpty(okText)) {
			btnOK.setText(okText);
		}

		if (!TextUtils.isEmpty(cancelText)) {
			btnCancel.setText(cancelText);
		}

		btnOK.setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				onOKButtonClicked();
			}
		});
		btnCancel.setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				onCancelButtonClicked();
			}
		});
	}

	public void setOnDialogItemClickListener(OnDialogItemClickListener onDialogItemClickListener) {
		this.onDialogItemClickListener = onDialogItemClickListener;
	}

	public void setOKButtonText(int resId) {
		this.okText = context.getString(resId);
	}

	public void setCancelButtonText(int resId) {
		this.cancelText = context.getString(resId);
	}

	private void onOKButtonClicked() {
		if (onDialogItemClickListener != null) {
			onDialogItemClickListener.onOKButtonClicked();
		}

		dismiss();
	}

	private void onCancelButtonClicked() {
		cancel();

		if (onDialogItemClickListener != null) {
			onDialogItemClickListener.onCancelButtonClicked();
		}
	}

	public interface OnDialogItemClickListener {
		void onOKButtonClicked();

		void onCancelButtonClicked();
	}

}
