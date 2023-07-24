package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.os.Bundle;
import android.view.View;

import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;

/**
 * Created by Ruifeng Shi on 11/15/2015.
 */
public class ChildRestrictionActivity extends SuperActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_child_restriction);

		findViewById(R.id.btn_create_account).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onCreateAccountButtonClicked();
			}
		});
		findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onLoginButtonClicked();
			}
		});
	}

	@Override
	public void onClickedBackSystemButton() {
		return;
	}

	private void onCreateAccountButtonClicked() {
		Bundle extra = new Bundle();
		extra.putInt(OnboardingActivity.EXTRA_FROM_ACTIVITY, OnboardingActivity.FROM_CHILD_RESTRICTION_ACTIVITY);
		pushNewActivityAnimated(OnboardingActivity.class, extra);
		popOverCurActivityAnimated();
	}

	private void onLoginButtonClicked() {
		Bundle extra = new Bundle();
		extra.putInt(OnboardingActivity.EXTRA_FROM_ACTIVITY, OnboardingActivity.FROM_CHILD_RESTRICTION_ACTIVITY);
		pushNewActivityAnimated(LoginActivity.class, extra);
		popOverCurActivityAnimated();
	}
}
