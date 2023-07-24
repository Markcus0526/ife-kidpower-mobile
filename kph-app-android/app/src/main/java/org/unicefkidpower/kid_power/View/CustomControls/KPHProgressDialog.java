package org.unicefkidpower.kid_power.View.CustomControls;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;

/**
 * Created by Ruifeng Shi on 2/28/2017.
 */

public class KPHProgressDialog extends Dialog {
	private String		title				= "";
	private String		content				= "";
	private boolean		backTrans			= false;

	private TextView	titleTextView		= null;
	private TextView	contentTextView		= null;
	private View		backgroundView		= null;


	public KPHProgressDialog(Context context) {
		super(context);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_progress);

		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		initControls();
	}


	@Override
	public void onStart() {
		super.onStart();

		Point screenSize = ResolutionSet.getScreenSize(getContext(), false);
		int screenWidth = screenSize.x;
		int screenHeight = screenSize.y;

		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
		layoutParams.width = screenWidth;
		layoutParams.height = screenHeight;

		getWindow().setAttributes(layoutParams);
	}


	public void setData(String title, String content, boolean backgroundTransparent) {
		this.title = title;
		this.content = content;
		this.backTrans = backgroundTransparent;
	}


	private void initControls() {
		setCancelable(false);

		if (content.trim().length() == 0) {
			content = getContext().getString(R.string.please_wait);
		}

		titleTextView = (TextView) findViewById(R.id.title_text);
		if (title == null || title.trim().length() == 0)
			titleTextView.setVisibility(View.GONE);
		else
			titleTextView.setText(title);

		contentTextView = (TextView) findViewById(R.id.content_text);
		contentTextView.setText(content);

		backgroundView = findViewById(R.id.content_layout);
		if (backTrans) {
			backgroundView.setBackgroundColor(Color.TRANSPARENT);
		} else {
			backgroundView.setBackgroundColor(Color.argb(0x90, 0, 0, 0));
		}
	}

}

