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

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperNormalSizeDialogFragment;


/**
 * Created by Ruifeng Shi on 2/9/2017.
 */
public class SyncWithOwnDeviceFragment extends SuperNormalSizeDialogFragment {
	private String		deviceModel		= "";
	private int			deviceType		= KPHUserService.TRACKER_TYPE_GOOGLEFIT;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		initControls(rootView);

		return rootView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_syncing_with_googlefit;
	}

	public void setData(String deviceModel, int trackerType) {
		this.deviceModel = deviceModel;
		this.deviceType = trackerType;
	}

	private void initControls(View rootView) {
		KPHTextView syncOnTextView = (KPHTextView)rootView.findViewById(R.id.sync_on_textview);
		syncOnTextView.setText(String.format(getSafeContext().getString(R.string.sync_on_s), deviceModel));

		KPHTextView syncOnContentsTextView = (KPHTextView)rootView.findViewById(R.id.sync_on_contents_textview);
		KPHTextView restoreContentsTextView = (KPHTextView)rootView.findViewById(R.id.restore_device_content1);

		if (deviceType == KPHUserService.TRACKER_TYPE_GOOGLEFIT) {
			syncOnContentsTextView.setText(String.format(getSafeContext().getString(R.string.sync_on_that_device_google_fit), deviceModel));
			restoreContentsTextView.setText(String.format(getSafeContext().getString(R.string.relink_to_google_fit), deviceModel));
		} else {
			syncOnContentsTextView.setText(String.format(getSafeContext().getString(R.string.sync_on_that_device_health_kit), deviceModel));
			restoreContentsTextView.setText(String.format(getSafeContext().getString(R.string.relink_to_healthkit), deviceModel));
		}

		final KPHTextView restoreContentsTextView2 = (KPHTextView)rootView.findViewById(R.id.restore_device_content2);
		String activityTrackerSpanPart = getSafeContext().getString(R.string.activity_tracker_link);
		ClickableSpan activityTrackerSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onClickedGotoActivityTracker();
			}
			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setColor(restoreContentsTextView2.getCurrentTextColor());
			}
		};
		SpannableString activityTrackerSS;

		if (deviceType == KPHUserService.TRACKER_TYPE_GOOGLEFIT) {
			activityTrackerSS = new SpannableString(getSafeContext().getString(R.string.relink_guide_google_fit));
		} else {
			activityTrackerSS = new SpannableString(getSafeContext().getString(R.string.relink_guide_health_kit));
		}
		activityTrackerSS.setSpan(
				activityTrackerSpan,
				activityTrackerSS.toString().indexOf(activityTrackerSpanPart),
				activityTrackerSS.toString().indexOf(activityTrackerSpanPart) + activityTrackerSpanPart.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
		);
		restoreContentsTextView2.setText(activityTrackerSS);
		restoreContentsTextView2.setMovementMethod(LinkMovementMethod.getInstance());
		restoreContentsTextView2.setHighlightColor(Color.TRANSPARENT);

		final KPHTextView switchContentsTextView = (KPHTextView)rootView.findViewById(R.id.switch_kpb_contents);
		SpannableString kidPowerBandSS = new SpannableString(getSafeContext().getString(R.string.new_kid_power_band));
		kidPowerBandSS.setSpan(
				activityTrackerSpan,
				kidPowerBandSS.toString().indexOf(activityTrackerSpanPart),
				kidPowerBandSS.toString().indexOf(activityTrackerSpanPart) + activityTrackerSpanPart.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
		);
		switchContentsTextView.setText(kidPowerBandSS);
		switchContentsTextView.setMovementMethod(LinkMovementMethod.getInstance());
		switchContentsTextView.setHighlightColor(Color.TRANSPARENT);

		KPHButton closeButton = (KPHButton)rootView.findViewById(R.id.btn_close);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedClose();
			}
		});


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
	}


	@Override
	public void startAction() {
		// No action. do nothing
	}


	private void onClickedGotoActivityTracker() {
		SyncWithOwnDeviceFragment.this.dismiss();

		Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_GOTO_ACTIVITY_TRACKER);
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
	}

	private void onClickedClose() {
		SyncWithOwnDeviceFragment.this.dismiss();
	}

	private void onClickedGetInTouch() {
		SyncWithOwnDeviceFragment.this.dismiss();

		Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_GOTO_GET_IN_TOUCH);
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
	}
}
