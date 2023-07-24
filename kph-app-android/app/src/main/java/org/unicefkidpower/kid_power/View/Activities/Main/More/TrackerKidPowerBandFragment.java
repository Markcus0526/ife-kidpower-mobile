package org.unicefkidpower.kid_power.View.Activities.Main.More;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.unicefkidpower.kid_power.Misc.AlertDialogHelper;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHNotificationUtil;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.Structure.KPHTracker;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHBrandedDialog;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

/**
 * Created by Dayong Li on 2/8/2017.
 */

public class TrackerKidPowerBandFragment extends SuperFragment {
	private static final String 	TAG					= "TrackerKidPowerBandFragment";
	private int						unlinkRetryCount	= 3;

	private String					bandname			= "";
	private String					username			= "";
	private int						userid				= 0;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		initControls(rootView);

		return rootView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_tracker_kidpowerband;
	}

	private void initControls(View rootView) {
		KPHTextView bandNameText = (KPHTextView)rootView.findViewById(R.id.band_name_text);
		bandNameText.setText(getSafeContext().getString(R.string.kid_power_band) + " " + bandname);

		KPHTextView descriptionText = (KPHTextView)rootView.findViewById(R.id.description_textview);
		descriptionText.setText(String.format(getSafeContext().getString(R.string.get_steps_from_this_kid_power_band), username));

		KPHButton unlinkButton = (KPHButton)rootView.findViewById(R.id.btn_unlink);
		unlinkButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedUnlink();
			}
		});
	}


	public void setData(int userid, String username, String bandname) {
		this.userid = userid;
		this.bandname = bandname;
		this.username = username;
	}


	private void onClickedUnlink() {
		final KPHTracker curTracker = KPHUserService.sharedInstance().currentTracker();
		if (curTracker == null) {
			showErrorDialog(getSafeContext().getString(R.string.not_linked_any_band));
			return;
		}

		showBrandedDialog(getSafeContext().getString(R.string.unlink_band_question_message),
				getSafeContext().getString(R.string.cancel),
				getSafeContext().getString(R.string.un_link),
				new KPHBrandedDialog.KPHBrandedDialogCallback() {
					@Override
					public void onDefaultButtonClicked() {}
					@Override
					public void onOtherButtonClicked() {
						if (getParentActivity() == null)
							return;

						unlinkRetryCount = 3;

						showProgressDialog();
						unlinkBand(curTracker);
					}
				});
	}


	private void unlinkBand(final KPHTracker curTracker) {
		if (unlinkRetryCount < 0) {
			Logger.error(TAG, "unlinkBand : failed, will show error message");

			dismissProgressDialog();
			AlertDialogHelper.showErrorAlert(getSafeContext().getString(R.string.unlink_failed), getParentActivity());

			return;
		}

		Logger.log(TAG, "unlinkBand : try to attempt to unlink, band:%s, retry:%d", curTracker.getDeviceId(), unlinkRetryCount);

		KPHUserService.sharedInstance().unLinkTracker(
				userid,
				curTracker.getDeviceId(),
				new onActionListener() {
					@Override
					public void completed(Object object) {
						Logger.log(TAG, "unLinkTracker : success");
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
						Logger.error(TAG, "unLinkTracker : failed, retry : %d", unlinkRetryCount);

						unlinkRetryCount--;
						unlinkBand(curTracker);
					}
				}
		);
	}
}
