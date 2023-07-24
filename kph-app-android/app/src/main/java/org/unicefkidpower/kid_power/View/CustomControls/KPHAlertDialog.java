package org.unicefkidpower.kid_power.View.CustomControls;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.R;

/**
 * Created by Ruifeng Shi on 2/28/2017.
 */

public class KPHAlertDialog extends Dialog {
	public static final int				DIALOG_MODE_NORMAL			= 0;
	public static final int				DIALOG_MODE_NOTIFICATION	= 1;
	public static final int				DIALOG_MODE_CONFIRM			= 2;
	public static final int				DIALOG_MODE_ERROR			= 3;

	private int							dialogMode					= DIALOG_MODE_NOTIFICATION;

	private TextView					titleTextView;
	private TextView					contentTextView;
	private Button						defaultButton, otherButton;

	private String						title, content, defaultTitle, otherTitle;
	private View.OnClickListener		defaultListener, otherListener, cancelListener;


	public KPHAlertDialog(Context context) {
		super(context);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_alert);

		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		initControls();
	}

	@Override
	public void onStart() {
		super.onStart();

		Point screenSize = ResolutionSet.getScreenSize(getContext(), false);
		int screenWidth = screenSize.x;
		int width = screenWidth - getContext().getResources().getDimensionPixelSize(R.dimen.dimen_16dp) * 2;

		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
		layoutParams.width = width;

		getWindow().setAttributes(layoutParams);
	}


	public void setData(
			int dialogMode,
			String title,
			String content,
			String defaultButton,
			String otherButton,
			View.OnClickListener defaultListener,
			View.OnClickListener otherListener,
			View.OnClickListener cancelListener
	) {
		this.dialogMode = dialogMode;

		this.title = title;
		this.content = content;
		this.defaultTitle = defaultButton;
		this.otherTitle = otherButton;
		this.defaultListener = defaultListener;
		this.otherListener = otherListener;
		this.cancelListener = cancelListener;
	}


	private void initControls() {
		titleTextView = (TextView) findViewById(R.id.title_textview);
		titleTextView.setText(title);

		contentTextView = (TextView) findViewById(R.id.contents_textview);
		contentTextView.setText(content);

		defaultButton = (Button) findViewById(R.id.btn_default);
		defaultButton.setText(defaultTitle);
		defaultButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedDefault();
			}
		});

		otherButton = (Button) findViewById(R.id.btn_other);
		otherButton.setText(otherTitle);
		otherButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedCancel();
			}
		});


		if (title == null || title.trim().length() == 0) {
			titleTextView.setVisibility(View.GONE);
		}

		if (defaultTitle == null || defaultTitle.trim().length() == 0) {
			defaultButton.setVisibility(View.GONE);
		}

		if (otherTitle == null || otherTitle.trim().length() == 0) {
			otherButton.setVisibility(View.GONE);
		}


		if (dialogMode == DIALOG_MODE_NORMAL) {
			titleTextView.setTextColor(UIManager.sharedInstance().getColor(R.color.MSG_NORMAL_TEXT));
			contentTextView.setTextColor(UIManager.sharedInstance().getColor(R.color.MSG_NORMAL_TEXT));
			defaultButton.setTextColor(UIManager.sharedInstance().getColor(R.color.MSG_DARK_BLUE));
			otherButton.setTextColor(UIManager.sharedInstance().getColor(R.color.MSG_TEXT_RED));
			otherButton.setVisibility(View.GONE);
		} else if (dialogMode == DIALOG_MODE_NOTIFICATION) {
			titleTextView.setTextColor(UIManager.sharedInstance().getColor(R.color.MSG_DARK_BLUE));
			contentTextView.setTextColor(UIManager.sharedInstance().getColor(R.color.MSG_NORMAL_TEXT));
			defaultButton.setTextColor(UIManager.sharedInstance().getColor(R.color.MSG_DARK_BLUE));
			otherButton.setTextColor(UIManager.sharedInstance().getColor(R.color.MSG_DARK_BLUE));
			otherButton.setVisibility(View.GONE);
		} else if (dialogMode == DIALOG_MODE_CONFIRM) {
			titleTextView.setTextColor(UIManager.sharedInstance().getColor(R.color.MSG_NORMAL_TEXT));
			contentTextView.setTextColor(UIManager.sharedInstance().getColor(R.color.MSG_NORMAL_TEXT));
			defaultButton.setTextColor(UIManager.sharedInstance().getColor(R.color.MSG_DARK_BLUE));
			otherButton.setTextColor(UIManager.sharedInstance().getColor(R.color.MSG_TEXT_RED));
		} else if (dialogMode == DIALOG_MODE_ERROR) {
			titleTextView.setTextColor(UIManager.sharedInstance().getColor(R.color.MSG_NORMAL_TEXT));
			contentTextView.setTextColor(UIManager.sharedInstance().getColor(R.color.MSG_NORMAL_TEXT));
			defaultButton.setTextColor(UIManager.sharedInstance().getColor(R.color.MSG_DARK_BLUE));
			otherButton.setTextColor(UIManager.sharedInstance().getColor(R.color.MSG_TEXT_RED));
			otherButton.setVisibility(View.GONE);
		}

		setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (cancelListener == null) {
					if (otherListener != null) {
						otherListener.onClick(null);
					} else if (defaultListener != null) {
						defaultListener.onClick(null);
					}
				} else {
					cancelListener.onClick(null);
				}
			}
		});
	}


	private void onClickedDefault() {
		KPHAlertDialog.this.dismiss();
		if (defaultListener != null)
			defaultListener.onClick(null);
	}

	private void onClickedCancel() {
		KPHAlertDialog.this.dismiss();
		if (otherListener != null)
			otherListener.onClick(null);
	}

}
