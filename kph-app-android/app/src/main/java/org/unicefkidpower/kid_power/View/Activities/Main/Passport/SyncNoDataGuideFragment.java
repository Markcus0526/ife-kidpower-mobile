package org.unicefkidpower.kid_power.View.Activities.Main.Passport;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jaredrummler.android.device.DeviceName;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperNormalSizeDialogFragment;

/**
 * Created by Ruifeng Shi on 2/9/2017.
 */

public class SyncNoDataGuideFragment extends SuperNormalSizeDialogFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		initControls(rootView);
		return rootView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_sync_no_data_guide;
	}


	private void initControls(View rootView) {
		String deviceName = DeviceName.getDeviceName();

		KPHTextView basicReasonText = (KPHTextView)rootView.findViewById(R.id.basic_reason_text);
		basicReasonText.setText(String.format(getSafeContext().getString(R.string.basic_reason), deviceName));

		KPHTextView trackerPermText1 = (KPHTextView)rootView.findViewById(R.id.tracker_permission_text);
		trackerPermText1.setText(String.format(getSafeContext().getString(R.string.activity_tracker_needs_permission), deviceName));

		KPHTextView trackerPermText2_title = (KPHTextView)rootView.findViewById(R.id.tracker_permission_text2_heading);
		trackerPermText2_title.setText(String.format(getSafeContext().getString(R.string.using_this_s), deviceName));

		KPHTextView trackerPermText2_sub = (KPHTextView)rootView.findViewById(R.id.tracker_permission_text2_sub);
		trackerPermText2_sub.setText(String.format(getSafeContext().getString(R.string.check_that_this_dev_still_has_permission), deviceName));

		final KPHTextView getInTouchText = (KPHTextView)rootView.findViewById(R.id.text_link);
		SpannableString getInTouchSS = new SpannableString(getSafeContext().getString(R.string.didn_t_solve_it_get_in_touch));

		String getInTouchPart = getSafeContext().getString(R.string.get_in_touch);
		ClickableSpan getInTouchSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onClickedGetInTouch();
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setColor(getInTouchText.getCurrentTextColor());
			}
		};
		getInTouchSS.setSpan(getInTouchSpan, getInTouchSS.toString().indexOf(getInTouchPart), getInTouchSS.toString().indexOf(getInTouchPart) + getInTouchPart.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		getInTouchText.setText(getInTouchSS);
		getInTouchText.setMovementMethod(LinkMovementMethod.getInstance());
		getInTouchText.setHighlightColor(Color.TRANSPARENT);


		KPHButton closeButton = (KPHButton)rootView.findViewById(R.id.btn_close);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedClose();
			}
		});
	}

	@Override
	public void startAction() {
		// No action. do nothing
	}

	private void onClickedGotoConnectedApps() {
		Toast.makeText(getSafeContext(), "Connected APPS", Toast.LENGTH_LONG).show();
	}


	private void onClickedGotoGoogleFitSettings() {
		Toast.makeText(getSafeContext(), "Google Fit Settings", Toast.LENGTH_LONG).show();
	}


	private void onClickedGetInTouch() {
		dismiss();

		Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_GOTO_GET_IN_TOUCH);
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
	}


	private void onClickedClose() {
		SyncNoDataGuideFragment.this.dismiss();
	}
}
