package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;

/**
 * Created by Ruifeng Shi on 12/16/2016.
 */

public class CreateParentAccountTypeFragment extends CreateParentAccountSuperFragment {
	private KPHTextView		descriptionTextView		= null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		initControls(rootView);

		return rootView;
	}

	private void initControls(View rootView) {
		descriptionTextView = (KPHTextView)rootView.findViewById(R.id.txt_description);

		rootView.findViewById(R.id.btn_email).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onEmailButtonClicked();
			}
		});
		rootView.findViewById(R.id.btn_facebook).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onFacebookButtonClicked();
			}
		});
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_parent_account_setup_method;
	}

	private void onEmailButtonClicked() {
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(
				new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_SETUP_METHOD_EMAIL)
		);
	}

	private void onFacebookButtonClicked() {
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(
				new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_SETUP_METHOD_FACEBOOK)
		);
	}

	@Override
	public void setChildUsername(String username) {
		super.setChildUsername(username);

		if (descriptionTextView != null)
			descriptionTextView.setText(String.format(getSafeContext().getString(R.string.parent_account_setup_method_description), username));
	}
}
