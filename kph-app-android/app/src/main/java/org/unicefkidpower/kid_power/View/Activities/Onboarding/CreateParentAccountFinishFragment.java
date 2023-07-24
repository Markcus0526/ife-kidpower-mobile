package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;

/**
 * Created by Ruifeng Shi on 1/31/2017.
 */

public class CreateParentAccountFinishFragment extends CreateParentAccountSuperFragment {
	private KPHTextView		txtDescription		= null;
	private KPHButton		btnFinish			= null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View vwRoot = super.onCreateView(inflater, container, savedInstanceState);

		txtDescription = (KPHTextView) vwRoot.findViewById(R.id.txt_description);
		btnFinish = (KPHButton) vwRoot.findViewById(R.id.btnFinish);

		if (childUsername.length() > 0) {
			txtDescription.setText(getSafeContext().getString(R.string.finish_setting_up_account) + " " + childUsername + ".");
			btnFinish.setText(getSafeContext().getString(R.string.finish) + " " + childUsername);
		}

		btnFinish.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedFinish();
			}
		});

		return vwRoot;
	}


	public int contentLayout() {
		return R.layout.fragment_parent_account_created;
	}


	private void onClickedFinish() {
		((OnboardingActivity)getParentActivity()).setShowBackButtonFlag(false);

		Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_CREATED_CLICKED);
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
	}


	@Override
	public void setChildUsername(String username) {
		super.setChildUsername(username);

		if (txtDescription != null) {
			txtDescription.setText(getSafeContext().getString(R.string.finish_setting_up_account) + " " + username + ".");
		}

		if (btnFinish != null) {
			btnFinish.setText(getSafeContext().getString(R.string.finish) + " " + username);
		}
	}
}
