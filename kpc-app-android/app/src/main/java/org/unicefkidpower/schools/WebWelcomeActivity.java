package org.unicefkidpower.schools;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import org.unicefkidpower.schools.helper.Logger;

public class WebWelcomeActivity extends BaseActivity implements View.OnClickListener {
	public static final String TAG = "Welcome Activity";
	private TextView txtWelcomeDescription;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome_web);

		FlurryAgent.onStartSession(this, "Welcome Activity");

		findViewById(R.id.btn_signin).setOnClickListener(this);
		txtWelcomeDescription = (TextView) findViewById(R.id.txt_welcome_description);

		SpannableString ss = new SpannableString(getString(R.string.welcome_description));
		ClickableSpan clickableSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onSignUpClicked();
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setUnderlineText(false);
			}
		};
		ss.setSpan(clickableSpan, 126, 154, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		txtWelcomeDescription.setText(ss);
		txtWelcomeDescription.setMovementMethod(LinkMovementMethod.getInstance());
		txtWelcomeDescription.setHighlightColor(Color.TRANSPARENT);
	}

	@Override
	protected boolean isUseEvent() {
		return false;
	}

	// ---- onclick ---------------
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_signin:
				onSignInClicked();
				break;
		}
	}

	// action handlers
	protected void onSignInClicked() {
		Logger.log(TAG, "Sign In button clicked");
		pushNewActivityAnimated(LoginActivity.class);
		finish();
	}

	protected void onSignUpClicked() {
		Logger.log(TAG, "Sign Up button clicked");

		Bundle extra = new Bundle();
		extra.putString(WebViewActivity.EXTRA_KEY_URL, "http://go.unicefkidpower.org");
		pushNewActivityAnimated(WebViewActivity.class, extra);
	}
}
