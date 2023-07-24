package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.unicefkidpower.kid_power.Misc.AlertDialogHelper;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHNotificationUtil;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.Structure.KPHTracker;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHBrandedDialog;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

/**
 * Created by Ruifeng Shi on 2/7/2017.
 */

public class OwnDeviceTrackerFragment extends SuperFragment {
	private static final String 		TAG				= "OwnDeviceTrackerFragment";

	private int							retryCount		= 3;
	private String						username		= "";


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		initControls(rootView);

		return rootView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_tracker_google_fit;
	}

	private void initControls(View rootView) {
		KPHButton btnUnlink = (KPHButton)rootView.findViewById(R.id.btn_unlink);
		btnUnlink.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedUnlink();
			}
		});

		ImageView trackerIconView = (ImageView)rootView.findViewById(R.id.tracker_icon);
		KPHTextView trackerTextView = (KPHTextView)rootView.findViewById(R.id.tracker_connected);
		KPHTextView descriptionTextView = (KPHTextView)rootView.findViewById(R.id.description_textview);

		KPHTracker tracker = KPHUserService.sharedInstance().currentTracker();
		if (tracker.getDeviceType().equalsIgnoreCase(KPHTracker.TRACKER_TYPE_NAME_GOOGLEFIT)) {
			trackerIconView.setImageResource(R.drawable.google_fit);
			trackerTextView.setText(getSafeContext().getString(R.string.google_fit_is_connected));
			descriptionTextView.setText(String.format(getSafeContext().getString(R.string.get_steps_from_google_fit), username));
			btnUnlink.setText(getSafeContext().getString(R.string.un_link_google_fit));
		} else if (tracker.getDeviceType().equalsIgnoreCase(KPHTracker.TRACKER_TYPE_NAME_HEALTHKIT)) {
			trackerIconView.setImageResource(R.drawable.health_kit);
			trackerTextView.setText(getSafeContext().getString(R.string.health_kit_is_connected));
			descriptionTextView.setText(String.format(getSafeContext().getString(R.string.get_steps_from_healthkit), username));
			btnUnlink.setText(getSafeContext().getString(R.string.un_link_healthkit));
		} else {
			trackerIconView.setImageDrawable(null);
			trackerTextView.setText("");
			descriptionTextView.setText("");
			btnUnlink.setVisibility(View.INVISIBLE);
		}

	}


	private void onClickedUnlink() {
		String message;

		KPHTracker tracker = KPHUserService.sharedInstance().currentTracker();
		if (tracker.getDeviceType().equalsIgnoreCase(KPHTracker.TRACKER_TYPE_NAME_GOOGLEFIT)) {
			message = getSafeContext().getString(R.string.unlink_googlefit_question_message);
		} else if (tracker.getDeviceType().equalsIgnoreCase(KPHTracker.TRACKER_TYPE_NAME_HEALTHKIT)) {
			message = getSafeContext().getString(R.string.unlink_healthkit_question_message);
		} else {
			message = "";
		}

		showBrandedDialog(message,
				getSafeContext().getString(R.string.cancel),
				getSafeContext().getString(R.string.un_link),
				new KPHBrandedDialog.KPHBrandedDialogCallback() {
					@Override
					public void onDefaultButtonClicked() {}
					@Override
					public void onOtherButtonClicked() {
						unlinkOwnDevice();
					}
				});
	}

	public void setUsername(String username) {
		this.username = username;
	}

	private void unlinkOwnDevice() {
		if (retryCount < 0) {
			Logger.error(TAG, "unlinkOwnDevice : failed, will show error message");

			if (getParentActivity() != null) {
				dismissProgressDialog();
				AlertDialogHelper.showErrorAlert(getSafeContext().getString(R.string.unlink_failed), getParentActivity());
			}

			return;
		}

		KPHUserData userData = KPHUserService.sharedInstance().getUserData();
		KPHTracker tracker = KPHUserService.sharedInstance().currentTracker();

		Logger.log(TAG, "unlinkOwnDevice : try to attempt to unlink, band:%s, retry:%d", tracker.getDeviceId(), retryCount);

		showProgressDialog();
		KPHUserService.sharedInstance().unLinkTracker(
				userData.getId(),
				tracker.getDeviceId(),
				new onActionListener() {
					@Override
					public void completed(Object object) {
						if (getParentActivity() == null)
							return;

						dismissProgressDialog();
						Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_DEVICE_CHANGED);
						LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
						KPHNotificationUtil.sharedInstance().showSuccessNotification(getParentActivity(), R.string.device_unlinked);
						getParentActivity().onClickedBackSystemButton();
					}

					@Override
					public void failed(int code, String message) {
						Logger.error(TAG, "unlinkOwnDevice : unLinkTracker failed, retry : %d", retryCount);
						retryCount--;
						unlinkOwnDevice();
					}
				}
		);
	}

}
