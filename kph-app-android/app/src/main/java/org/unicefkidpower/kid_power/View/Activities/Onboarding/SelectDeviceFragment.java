package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.KPHNotificationUtil;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.Model.MicroService.GoogleFit.GoogleFitService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;

/**
 * Created by Ruifeng Shi on 2/6/2017.
 */

public class SelectDeviceFragment extends SelectTrackerSuperFragment {
	private final String		TAG								= "SelectDeviceFragment";

	private ImageView			avatarImageView					= null;
	private KPHTextView			usernameTextView				= null;
	private KPHButton			kidPowerBandButton				= null;
	private KPHButton			googleFitButton					= null;
	private KPHTextView			helpTextView					= null;

	private ImageView			googleFitImageView				= null;
	private ImageView			googleFitDisclosureImageView	= null;
	private KPHTextView			googleFitTitle					= null;
	private KPHTextView			googleFitContents				= null;

	private int					userId							= 0;
	private String				avatarId						= "";
	private String				username						= "";

	private final int			REQCODE_SELECT_GOOGLEFIT		= 1;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		initIntentFilter();
		initControls(rootView);

		return rootView;
	}

	private void initControls(View rootView) {
		// Set minimum height for the scrollview content
		{
			Point screenSize = ResolutionSet.sharedInstance().getScreenSize(getSafeContext(), true);
			int minimumHeight, statusBarHeight, tabBarHeight;

			statusBarHeight = ResolutionSet.sharedInstance().getStatusBarHeight(getSafeContext());

			if (getParentActivity() != null && getParentActivity() instanceof MainActivity && isShowTabBar()) {
				tabBarHeight = getSafeContext().getResources().getDimensionPixelSize(R.dimen.activity_tab_bar_height);
			} else {
				tabBarHeight = 0;
			}

			minimumHeight = screenSize.y - statusBarHeight - tabBarHeight;

			// Decrease 2 pixels for the calculation offset
			minimumHeight -= 2;

			View scrollContentLayout = rootView.findViewById(R.id.scroll_content_layout);
			scrollContentLayout.setMinimumHeight(minimumHeight);
		}

		usernameTextView = (KPHTextView)rootView.findViewById(R.id.txt_username);
		avatarImageView = (ImageView)rootView.findViewById(R.id.ivAvatar);
		helpTextView = (KPHTextView)rootView.findViewById(R.id.txt_help);

		usernameTextView.setText(username);

		Drawable avatar = KPHUserService.sharedInstance().getAvatarDrawable(avatarId);
		if (avatar != null) {
			avatarImageView.setImageDrawable(avatar);
		}

		kidPowerBandButton = (KPHButton)rootView.findViewById(R.id.btn_kid_power);
		kidPowerBandButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedKidPowerBand();
			}
		});
		googleFitButton = (KPHButton)rootView.findViewById(R.id.btn_google_fit);
		googleFitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedGoogleFit();
			}
		});

		// Initialize text spans
		String learnMore = getSafeContext().getString(R.string.learn_more);
		String getAKidPowerBand = getSafeContext().getString(R.string.get_a_kid_power_band);

		SpannableString ss = new SpannableString(getSafeContext().getString(R.string.learn_more_or_get_a_kid_power_band));
		ss.setSpan(
				learnMoreSpan,
				ss.toString().indexOf(learnMore),
				ss.toString().indexOf(learnMore) + learnMore.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
		);
		ss.setSpan(
				getAKidPowerBandSpan,
				ss.toString().indexOf(getAKidPowerBand),
				ss.toString().indexOf(getAKidPowerBand) + getAKidPowerBand.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
		);
		helpTextView.setText(ss);
		helpTextView.setMovementMethod(LinkMovementMethod.getInstance());
		helpTextView.setHighlightColor(Color.TRANSPARENT);


		googleFitImageView = (ImageView)rootView.findViewById(R.id.img_fit);
		googleFitDisclosureImageView = (ImageView)rootView.findViewById(R.id.img_disclosure_fit);
		googleFitTitle = (KPHTextView)rootView.findViewById(R.id.fit_hint_title);
		googleFitContents = (KPHTextView)rootView.findViewById(R.id.fit_hint_subtitle);

		setGoogleFitEnabled(true);


		showProgressDialog();
		GoogleFitService.checkIfAttached(new GoogleFitService.OnAttachedListener() {
			@Override
			public void onSuccess(int userid) {
				dismissProgressDialog();
				setGoogleFitEnabled(userid <= 0);
			}

			@Override
			public void onFailure(String message) {
				dismissProgressDialog();
				setGoogleFitEnabled(true);
			}
		});
	}


	@Override
	public void onPause() {
		super.onPause();
	}


	@Override
	public void onDestroyView() {
		if (intentFilter != null) {
			LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
			intentFilter = null;
		}

		super.onDestroyView();
	}


	private void initIntentFilter() {
		if (intentFilter == null) {
			intentFilter = new IntentFilter();
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_TRACKER_SELECTED);

			LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);
		}
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_profile_select_device;
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQCODE_SELECT_GOOGLEFIT && resultCode != Activity.RESULT_CANCELED) {
			if (data.getIntExtra(GoogleFitWrapperActivity.OUT_EXTRA_RETCODE, -1) == GoogleFitService.GOOGLE_FIT_ERROR_NONE) {
				Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_TRACKER_SELECTED);
				intent.putExtra(KPHConstants.PROFILE_DEVICE_TYPE, KPHUserService.TRACKER_TYPE_GOOGLEFIT);
				intent.putExtra(KPHConstants.PROFILE_DEVICE_SELECTED, true);

				// Google fit tracker selected
				LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
			} else {
				String errMessage = data.getStringExtra(GoogleFitWrapperActivity.OUT_EXTRA_ERROR_MESSAGE);
				showErrorDialog(errMessage);
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private ClickableSpan learnMoreSpan = new ClickableSpan() {
		@Override
		public void onClick(View widget) {
			onClickedLearnMore();
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			super.updateDrawState(ds);
			ds.setColor(helpTextView.getCurrentTextColor());
			ds.setStyle(Paint.Style.STROKE);
		}
	};

	private ClickableSpan getAKidPowerBandSpan = new ClickableSpan() {
		@Override
		public void onClick(View widget) {
			onClickedGetAKidPowerBand();
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			super.updateDrawState(ds);
			ds.setColor(helpTextView.getCurrentTextColor());
		}
	};



	private void onClickedKidPowerBand() {
		if (getParentActivity() == null)
			return;

		SuperActivity parentActivity = getParentActivity();

		if (KPHUtils.sharedInstance().checkGPSProviderEnabled(parentActivity, true)) {
			// BYOD Post onboarding
			RegisterBandFragment fragment = new RegisterBandFragment();
			fragment.setData(userId, avatarId, username);
			parentActivity.showNewFragment(fragment);
		}
	}

	private void onClickedGoogleFit() {
		if (getParentActivity() == null)
			return;

		SuperActivity parentActivity = getParentActivity();

		Bundle extra = new Bundle();
		extra.putInt(GoogleFitWrapperActivity.EXTRA_USERID, userId);
		extra.putInt(GoogleFitWrapperActivity.EXTRA_ACTION, GoogleFitService.ACTION_CONNECT);
		extra.putString(GoogleFitWrapperActivity.EXTRA_SYNCDATE, "");

		GoogleFitWrapperActivity.showGoogleFitWrapperActivity(parentActivity, extra, REQCODE_SELECT_GOOGLEFIT);
	}

	private void onClickedLearnMore() {
		// Go to learn more fragment.
		ActivityTrackerInfoFragment trackerInfoFragment = new ActivityTrackerInfoFragment();
		trackerInfoFragment.setData(getParentActivity() instanceof OnboardingActivity);
		getParentActivity().showDialogFragment(trackerInfoFragment);
	}

	private void onClickedGetAKidPowerBand() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(KPHConstants.GET_KIDPOWER_BAND_URL));
		getParentActivity().startActivity(intent);
	}

	public void setData(int userId, String username, String avatarId) {
		this.username = username;
		this.avatarId = avatarId;
		this.userId = userId;
	}

	private IntentFilter intentFilter = null;
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (getActivity() == null) {
				// Already detached
				return;
			}

			String action = intent.getAction();
			switch (action) {
				case KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_TRACKER_SELECTED: {
					boolean isLinked = intent.getBooleanExtra(KPHConstants.PROFILE_DEVICE_SELECTED, false);
					if (isLinked &&
							getParentActivity() != null &&
							!(getParentActivity() instanceof OnboardingActivity)) {
						KPHNotificationUtil.sharedInstance().showSuccessNotification(getParentActivity(), R.string.sync_band_linked_caption);
						onClickedBackButton();
					}
					break;
				}
			}
		}
	};


	private void setGoogleFitEnabled(boolean enabled) {
		googleFitButton.setEnabled(enabled);

		if (enabled) {
			googleFitImageView.clearColorFilter();
			googleFitDisclosureImageView.clearColorFilter();
			googleFitTitle.setTextColor(UIManager.sharedInstance().getColor(R.color.kph_color_googlefittext_enabled));
			googleFitContents.setTextColor(UIManager.sharedInstance().getColor(R.color.kph_color_googlefittext_enabled));
			googleFitContents.setText(R.string.google_fit_subtitle);
		} else {
			googleFitImageView.setColorFilter(UIManager.sharedInstance().getColor(R.color.kph_color_googlefitmask_disabled), PorterDuff.Mode.SRC_ATOP);
			googleFitDisclosureImageView.setColorFilter(UIManager.sharedInstance().getColor(R.color.kph_color_googlefitmask_disabled), PorterDuff.Mode.SRC_ATOP);
			googleFitTitle.setTextColor(UIManager.sharedInstance().getColor(R.color.kph_color_googlefittext_disabled));
			googleFitContents.setTextColor(UIManager.sharedInstance().getColor(R.color.kph_color_googlefittext_disabled));
			googleFitContents.setText(R.string.google_fit_subtitle_disabled);
		}
	}
}
