package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble.BleManager;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.More.HelpMainActivity;
import org.unicefkidpower.kid_power.View.CustomControls.KPHBrandedDialog;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

/**
 * Created by Ruifeng Shi on 2/13/2017.
 */

public abstract class SelectTrackerSuperFragment extends SuperFragment {
	protected static int			SERVICE_ERROR_BASE				= 0x80;
	protected static int			SERVICE_ERROR					= SERVICE_ERROR_BASE + 1;

	protected static final int		BACKEND_RETRY_COUNT				= 5;
	protected static final int		BAND_RETRY_COUNT				= 3;

	protected int					nRetry							= BACKEND_RETRY_COUNT;
	protected int					showErrorDialogCount			= 0;


	protected void showErrorDialog(int errorType, String... errorMessage) {
		if (getParentActivity() == null) {
			return;
		}

		String msg = "";

		if (errorType == BleManager.BT_ERROR_NO_SERVICE) {
			msg = getSafeContext().getString(R.string.error_msg_bluetooth_off);
		} else if (errorType == BleManager.BT_ERROR_NO_ANY_DEVICES) {
			msg = getSafeContext().getString(R.string.error_msg_no_bands_found);
		} else if (errorType == BleManager.BT_ERROR_LINK_FAILED) {
			msg = getSafeContext().getString(R.string.error_msg_band_link_failed);
		} else if (errorType >= SERVICE_ERROR_BASE) {
			msg = errorMessage[0];
		} else {
			showErrorDialogCount += 1;

			if (showErrorDialogCount < 3)
				msg = getSafeContext().getString(R.string.error_msg_band_link_failed);
			else
				msg = getSafeContext().getString(R.string.error_msg_band_link_failed_need_help);
		}


		if (showErrorDialogCount < 3) {
			showBrandedDialog(msg, getSafeContext().getString(R.string.ok), null, null);
		} else {
			showBrandedDialog(msg, getSafeContext().getString(R.string.get_help), getSafeContext().getString(R.string.ok), new KPHBrandedDialog.KPHBrandedDialogCallback() {
				@Override
				public void onDefaultButtonClicked() {
					// go to support
					if (getParentActivity() != null)
						getParentActivity().pushNewActivityAnimated(HelpMainActivity.class);
				}

				@Override
				public void onOtherButtonClicked() {
				}
			});
		}
	}
}
