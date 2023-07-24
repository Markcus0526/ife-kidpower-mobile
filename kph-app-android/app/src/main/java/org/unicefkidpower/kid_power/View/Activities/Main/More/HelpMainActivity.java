package org.unicefkidpower.kid_power.View.Activities.Main.More;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;

import static java.lang.Thread.sleep;

/**
 * Created by Ruifeng Shi on 11/13/2015.
 */
public class HelpMainActivity extends SuperActivity {
	// UI Elements
	private KPHButton			btnFAQ				= null;
	private KPHButton			btnSendMessage		= null;
	private KPHTextView			txtAppVersion		= null;

	private boolean				isCompositingLog	= false;
	private IntentFilter		intentFilter		= null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);

		btnFAQ = (KPHButton) findViewById(R.id.btn_faq);
		btnFAQ.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedFAQ();
			}
		});

		btnSendMessage = (KPHButton) findViewById(R.id.btn_send_us_a_message);
		btnSendMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedSendMessage();
			}
		});

		txtAppVersion = (KPHTextView) findViewById(R.id.txt_app_version);
		txtAppVersion.setText(
				getString(
						R.string.app_version_number,
						KPHUtils.sharedInstance().getVersionName(HelpMainActivity.this),
						KPHUtils.sharedInstance().getVersionCode(HelpMainActivity.this)
				)
		);

		if (intentFilter == null) {
			intentFilter = new IntentFilter();
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_LOG_WRITTEN_REPORT);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_LOG_FAILED_REPORT);

			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_STORAGE_PERMISSION_SUCCESS);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_STORAGE_PERMISSION_FAILED);

			LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
		}
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		intentFilter = null;
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case KPHConstants.PERMISSION_REQUEST_STORAGE: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					storagePermissionGranted();
				} else {
					storagePermissionDenied();
				}
			}

			// other 'case' lines to check for other
			// permissions this app might request
		}
	}

	private void storagePermissionGranted() {
		Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_STORAGE_PERMISSION_SUCCESS);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void storagePermissionDenied() {
	}

	private void onClickedFAQ() {
		pushNewActivityAnimated(FAQActivity.class);
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
			String action = intent.getAction();

			switch (action) {
				case KPHBroadcastSignals.BROADCAST_SIGNAL_LOG_WRITTEN_REPORT: {
					isCompositingLog = false;
					dismissProgressDialog();
					break;
				}
				case KPHBroadcastSignals.BROADCAST_SIGNAL_LOG_FAILED_REPORT: {
					int flag = intent.getFlags();

					if (flag == KPHConstants.PERM_STORAGE_NEED_GRANT) {
						isCompositingLog = false;
						showErrorDialog(context.getString(R.string.permission_storage_need_message));
					} else {
						showErrorDialog(context.getString(R.string.permission_storage_did_not_grant));
					}
					break;
				}
				case KPHBroadcastSignals.BROADCAST_SIGNAL_STORAGE_PERMISSION_SUCCESS: {
					sendLogToSupportTeam(true);
					break;
				}
				case KPHBroadcastSignals.BROADCAST_SIGNAL_STORAGE_PERMISSION_FAILED: {
					isCompositingLog = false;
					sendLogToSupportTeam(false);
					break;
				}
			}
		}
	};

	private void checkStoragePermission() {
		int permissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (permissionResult != PackageManager.PERMISSION_GRANTED) {
			PermissionDialogFragment permissionDialog = new PermissionDialogFragment();
			permissionDialog.setType(true);
			permissionDialog.setRequestHandler(new PermissionDialogFragment.RequestPermissionHandler() {
				@Override
				public void onRequest() {
					Logger.log("Help", "send request storage permission.");

					ActivityCompat.requestPermissions(HelpMainActivity.this,
							new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
							KPHConstants.PERMISSION_REQUEST_STORAGE);
				}

				@Override
				public void onClose() {
					// The same as denied result
					storagePermissionDenied();
				}
			});
			this.showDialogFragment(permissionDialog);
		} else {
			sendLogToSupportTeam(true);
			Logger.log("Help", "have already storage permission");
		}
	}

	private void sendLogToSupportTeam(final boolean addLog) {
		this.showProgressDialog();
		(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					sleep(500);
				} catch (InterruptedException ex) {
				}

				KPHUserService.sharedInstance().sendMessage(HelpMainActivity.this, addLog);
			}
		})).start();
	}

}
