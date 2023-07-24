package org.unicefkidpower.kid_power.View.Activities.Main.More;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

import static java.lang.Thread.sleep;

/**
 * Created by Ruifeng Shi on 11/12/2015.
 */
public class HelpMainFragment extends SuperFragment {
	// UI Elements
	private View			contentView				= null;
	private KPHButton		btnFAQ					= null;
	private KPHButton		btnSendUsMessage		= null;
	private KPHTextView		txtAppVersion			= null;

	private boolean			isCompositingLog		= false;
	private IntentFilter	intentFilter			= null;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		btnFAQ = (KPHButton) contentView.findViewById(R.id.btn_faq);
		btnFAQ.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedFAQ();
			}
		});

		btnSendUsMessage = (KPHButton) contentView.findViewById(R.id.btn_send_us_a_message);
		btnSendUsMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedSendMessage();
			}
		});

		txtAppVersion = (KPHTextView) contentView.findViewById(R.id.txt_app_version);
		txtAppVersion.setText(getSafeContext().getString(
				R.string.app_version_number,
				KPHUtils.sharedInstance().getVersionName(getSafeContext()),
				KPHUtils.sharedInstance().getVersionCode(getSafeContext())
		));

		if (intentFilter == null) {
			intentFilter = new IntentFilter();
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_LOG_WRITTEN_REPORT);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_LOG_FAILED_REPORT);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_STORAGE_PERMISSION_SUCCESS);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_STORAGE_PERMISSION_FAILED);

			LocalBroadcastManager.getInstance(getSafeContext()).registerReceiver(receiver, intentFilter);
		}

		return contentView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		LocalBroadcastManager.getInstance(getSafeContext()).unregisterReceiver(receiver);
		intentFilter = null;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_help_main;
	}


	private void onClickedFAQ() {
		if (getParentActivity() == null)
			return;

		FAQFragment fragFAQ = new FAQFragment();
		getParentActivity().showNewFragment(fragFAQ);
	}


	private void onClickedSendMessage() {
		if (isCompositingLog)
			return;

		isCompositingLog = true;
		checkStoragePermission();
	}


	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (getActivity() == null) {
				// Already detached
				return;
			}

			String action = intent.getAction();
			if (action.equals(KPHBroadcastSignals.BROADCAST_SIGNAL_LOG_WRITTEN_REPORT)) {
				isCompositingLog = false;
				dismissProgressDialog();
			} else if (action.equals(KPHBroadcastSignals.BROADCAST_SIGNAL_LOG_FAILED_REPORT)) {
				int flag = intent.getFlags();

				if (flag == KPHConstants.PERM_STORAGE_NEED_GRANT) {
					isCompositingLog = false;
					showErrorDialog(context.getString(R.string.permission_storage_need_message));
				} else {
					showErrorDialog(context.getString(R.string.permission_storage_did_not_grant));
				}
			} else if (action.equals(KPHBroadcastSignals.BROADCAST_SIGNAL_STORAGE_PERMISSION_SUCCESS)) {
				sendEMailToSupportTeam(true);
			} else if (action.equals(KPHBroadcastSignals.BROADCAST_SIGNAL_STORAGE_PERMISSION_FAILED)) {
				isCompositingLog = false;
				storagePermissionDenied();
			}
		}
	};

	void checkStoragePermission() {
		if (getParentActivity() != null &&
				ContextCompat.checkSelfPermission(getParentActivity(),
						Manifest.permission.WRITE_EXTERNAL_STORAGE)
						!= PackageManager.PERMISSION_GRANTED) {
			PermissionDialogFragment permissionDialog = new PermissionDialogFragment();
			permissionDialog.setType(true);
			permissionDialog.setRequestHandler(new PermissionDialogFragment.RequestPermissionHandler() {
				@Override
				public void onRequest() {
					Logger.log("Help", "send request storage permission.");
					ActivityCompat.requestPermissions(getParentActivity(),
							new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
							KPHConstants.PERMISSION_REQUEST_STORAGE);
				}

				@Override
				public void onClose() {
					isCompositingLog = false;
					storagePermissionDenied();
				}
			});
			getParentActivity().showDialogFragment(permissionDialog);
		} else {
			storagePermissionGranted();
			Logger.log("Help", "Have already storage permission");
		}
	}


	private void storagePermissionGranted() {
		Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_STORAGE_PERMISSION_SUCCESS);
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
	}

	private void storagePermissionDenied() {
	}

	private void sendEMailToSupportTeam(final boolean addLogPermission) {
		if (getParentActivity() == null)
			return;

		showProgressDialog();
		(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					sleep(500);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}

				if (getParentActivity() != null) {
					KPHUserService.sharedInstance().sendMessage(getParentActivity(), addLogPermission);
					getParentActivity().dismissProgressDialog();
				}
			}
		})).start();
	}
}
