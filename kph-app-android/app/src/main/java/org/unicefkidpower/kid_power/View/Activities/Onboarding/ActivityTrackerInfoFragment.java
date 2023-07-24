package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.ActivityNotFoundException;
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
import android.widget.Toast;

import com.jaredrummler.android.device.DeviceName;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperNormalSizeDialogFragment;

/**
 * Created by Ruifeng Shi on 2/7/2017.
 */

public class ActivityTrackerInfoFragment extends SuperNormalSizeDialogFragment {
	private			KPHTextView		linkLaterTextView		= null;
	private			boolean			showLinkTracker			= true;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		initControls(rootView);
		return rootView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_activity_tracker_info_dialog;
	}


	private void initControls(View rootView) {
		final KPHTextView buyKidpowerTextView = (KPHTextView)rootView.findViewById(R.id.buy_kidpower_text);

		SpannableString spannableString = new SpannableString(getSafeContext().getString(R.string.you_can_buy_a_kid_power_band_online));
		String spannablePart = getSafeContext().getString(R.string.buy_a_kid_power_band_online);
		spannableString.setSpan(new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onClickedBuyAKidPower();
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setColor(buyKidpowerTextView.getCurrentTextColor());
			}
		}, spannableString.toString().indexOf(spannablePart), spannableString.toString().indexOf(spannablePart) + spannablePart.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		buyKidpowerTextView.setText(spannableString);
		buyKidpowerTextView.setMovementMethod(LinkMovementMethod.getInstance());
		buyKidpowerTextView.setHighlightColor(Color.TRANSPARENT);


		final KPHTextView getGoogleFitTextView = (KPHTextView)rootView.findViewById(R.id.get_google_fit_on_store_text);

		spannableString = new SpannableString(String.format(getSafeContext().getString(R.string.google_fit_is_a_free_app), DeviceName.getDeviceName()));
		spannablePart = getSafeContext().getString(R.string.get_google_fit_on_the_play_store);
		spannableString.setSpan(new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onClickedGetGoogleFit();
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setColor(getGoogleFitTextView.getCurrentTextColor());
			}
		}, spannableString.toString().indexOf(spannablePart), spannableString.toString().indexOf(spannablePart) + spannablePart.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		getGoogleFitTextView.setText(spannableString);
		getGoogleFitTextView.setMovementMethod(LinkMovementMethod.getInstance());
		getGoogleFitTextView.setHighlightColor(Color.TRANSPARENT);


		KPHTextView permissionTextView = (KPHTextView)rootView.findViewById(R.id.permission_text);
		permissionTextView.setText(String.format(getSafeContext().getString(R.string.yes_at_any_time), DeviceName.getDeviceName()));


		KPHButton closeButton = (KPHButton)rootView.findViewById(R.id.btn_close);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedClose();
			}
		});


		linkLaterTextView = (KPHTextView)rootView.findViewById(R.id.later_text);
		if (!showLinkTracker) {
			linkLaterTextView.setVisibility(View.GONE);
		} else {
			linkLaterTextView.setVisibility(View.VISIBLE);
		}

		spannableString = new SpannableString(getSafeContext().getString(R.string.set_up_an_activity_tracker_later));
		spannableString.setSpan(new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onClickedSetupLater();
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setColor(linkLaterTextView.getCurrentTextColor());
			}
		}, 0, spannableString.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		linkLaterTextView.setText(spannableString);
		linkLaterTextView.setMovementMethod(LinkMovementMethod.getInstance());
		linkLaterTextView.setHighlightColor(Color.TRANSPARENT);
	}

	private void onClickedBuyAKidPower() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(KPHConstants.GET_KIDPOWER_BAND_URL));
		getParentActivity().startActivity(intent);
	}

	private void onClickedGetGoogleFit() {
		try {
			getParentActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(KPHConstants.GOOGLE_FIT_STORE_URL)));
		} catch (ActivityNotFoundException ex) {
			getParentActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(KPHConstants.GOOGLE_FIT_PAGE_URL)));
		} catch (Exception ex) {
			ex.printStackTrace();
			Toast.makeText(getSafeContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private void onClickedClose() {
		ActivityTrackerInfoFragment.this.dismiss();
	}

	private void onClickedSetupLater() {
		ActivityTrackerInfoFragment.this.dismiss();

		Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_TRACKER_SELECTED);
		intent.putExtra(KPHConstants.PROFILE_DEVICE_TYPE, KPHUserService.TRACKER_TYPE_NONE);
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
	}

	@Override
	public void startAction() {
		// No action. do nothing
	}

	public void setData(boolean showLinkTracker) {
		this.showLinkTracker = showLinkTracker;

		if (linkLaterTextView != null) {
			if (this.showLinkTracker) {
				linkLaterTextView.setVisibility(View.VISIBLE);
			} else {
				linkLaterTextView.setVisibility(View.GONE);
			}
		}
	}

}
