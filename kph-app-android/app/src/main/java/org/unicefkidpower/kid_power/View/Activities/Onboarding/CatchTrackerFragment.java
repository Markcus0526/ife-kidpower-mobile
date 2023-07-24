package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.ImageButton;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperNormalSizeDialogFragment;

import java.util.Date;

/**
 * Created by Ruifeng Shi on 2/9/2017.
 */

public class CatchTrackerFragment extends SuperNormalSizeDialogFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		KPHUserService.sharedInstance().saveCatchTrackerDialogDate(new Date());
		initControls(rootView);
		return rootView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_catch_tracker;
	}


	private void initControls(View rootView) {
		KPHButton googleFitButton = (KPHButton)rootView.findViewById(R.id.btn_connect_googlefit);
		googleFitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedConnectToGoogleFit();
			}
		});

		ImageButton closeButton = (ImageButton)rootView.findViewById(R.id.btnClose);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedClose();
			}
		});

		final KPHTextView linkTextView = (KPHTextView)rootView.findViewById(R.id.text_link);

		String bandSpanPart = getSafeContext().getString(R.string.link_a_kid_power_band);
		ClickableSpan bandSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onClickedConnectToKidPowerBand();
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setColor(linkTextView.getCurrentTextColor());
			}
		};

		String buySpanPart = getSafeContext().getString(R.string.buy_one);
		ClickableSpan buySpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onClickedBuyKidPowerBand();
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setColor(linkTextView.getCurrentTextColor());
			}
		};

		SpannableString ss = new SpannableString(getSafeContext().getString(R.string.or_link_a_kid_power_band_or_buy_one));
		ss.setSpan(bandSpan, ss.toString().indexOf(bandSpanPart), ss.toString().indexOf(bandSpanPart) + bandSpanPart.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		ss.setSpan(buySpan, ss.toString().indexOf(buySpanPart), ss.toString().indexOf(buySpanPart) + buySpanPart.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		linkTextView.setText(ss);
		linkTextView.setMovementMethod(LinkMovementMethod.getInstance());
		linkTextView.setHighlightColor(Color.TRANSPARENT);
	}

	@Override
	public void startAction() {
		// No action. Do nothing.
	}

	private void onClickedConnectToGoogleFit() {
		CatchTrackerFragment.this.dismiss();

		Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_CATCH_GOOGLE_FIT);
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
	}

	private void onClickedConnectToKidPowerBand() {
		CatchTrackerFragment.this.dismiss();

		Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_CATCH_KID_POWER_BAND);
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
	}

	private void onClickedBuyKidPowerBand() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(KPHConstants.GET_KIDPOWER_BAND_URL));
		getParentActivity().startActivity(intent);
	}


	@Override
	public void onStart() {
		super.onStart();
	}

	private void onClickedClose() {
		CatchTrackerFragment.this.dismiss();
	}

}
