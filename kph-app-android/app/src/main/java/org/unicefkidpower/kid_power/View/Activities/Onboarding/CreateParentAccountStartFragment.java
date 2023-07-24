package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.R;

/**
 * Created by Ruifeng Shi on 12/16/2016.
 */

public class CreateParentAccountStartFragment extends CreateParentAccountSuperFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View vwRoot = super.onCreateView(inflater, container, savedInstanceState);

		vwRoot.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onYesButtonClicked();
			}
		});
		vwRoot.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onNoButtonClicked();
			}
		});

		return vwRoot;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_under_minimum_age_error;
	}

	private void onYesButtonClicked() {
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(
				new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_AGE_RESTRICTION_ERROR_YES));
	}

	private void onNoButtonClicked() {
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(
				new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_AGE_RESTRICTION_ERROR_NO));
	}
}
