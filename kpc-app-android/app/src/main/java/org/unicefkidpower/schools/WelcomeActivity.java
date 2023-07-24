package org.unicefkidpower.schools;

import android.os.Bundle;
import android.view.View;

import com.flurry.android.FlurryAgent;

import org.unicefkidpower.schools.helper.Logger;

public class WelcomeActivity extends BaseActivity implements View.OnClickListener {
	public static final String TAG = "Welcome Activity";

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);

		FlurryAgent.onStartSession(this, TAG);

		findViewById(R.id.btn_get_started).setOnClickListener(this);
		findViewById(R.id.btn_login).setOnClickListener(this);
	}

	@Override
	protected boolean isUseEvent() {
		return false;
	}

	// ---- onclick ---------------
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_get_started:
				onSignupClicked();
				break;

			case R.id.btn_login:
				onLoginClicked();
				break;
		}
	}

	// action handlers
	protected void onLoginClicked() {
		Logger.log(TAG, "Log In button clicked");
		pushNewActivityAnimated(LoginActivity.class);
		finish();
	}

	protected void onSignupClicked() {
		Logger.log(TAG, "Sign Up button clicked");
		pushNewActivityAnimated(OnboardingActivity.class);
	}
}
