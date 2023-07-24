package org.unicefkidpower.schools.support;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.zendesk.sdk.model.request.CreateRequest;
import com.zendesk.sdk.model.request.UploadResponse;
import com.zendesk.sdk.network.RequestProvider;
import com.zendesk.sdk.network.UploadProvider;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;

import org.unicefkidpower.schools.BaseActivity;
import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.UIManager;
import org.unicefkidpower.schools.context.UserContext;
import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.model.Utils;
import org.unicefkidpower.schools.ui.KPEditText;
import org.unicefkidpower.schools.ui.KPTextView;

import java.io.File;
import java.util.Arrays;


/**
 * Created by Dayong Li on 3/21/2017.
 */

public class ContactUsActivity extends BaseActivity {
	private final String			TAG = "ContactUsActivity";

	public static final String		EXTRA_ADD_LOG_FILE = "AddLogFile";

	private String					sendDescript;
	private KPTextView				txtFileName;

	private boolean					shouldAddLogFile = false;
	private File 					logFile = null;
	private Boolean					logFileCreated = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contactus);

		shouldAddLogFile = getIntent().getBooleanExtra(EXTRA_ADD_LOG_FILE, false);

		txtFileName = (KPTextView) findViewById(R.id.text_filename);

		ImageButton btnBack = (ImageButton) findViewById(R.id.image_back);
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		final KPEditText edtDescript = (KPEditText) findViewById(R.id.edit_desc);
		ImageButton btnSend = (ImageButton) findViewById(R.id.image_send);
		btnSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String szDescript = edtDescript.getText().toString();
				if (TextUtils.isEmpty(szDescript)) {
					edtDescript.requestFocus();
					return;
				}

				sendDescript = szDescript;
				createTicket();
			}
		});


		if (shouldAddLogFile) {
			UIManager.sharedInstance().showProgressDialog(ContactUsActivity.this, null, getString(R.string.app_onemoment), true);

			CreateLogfileRunnable runnable = new CreateLogfileRunnable();
			Thread thread = new Thread(runnable);
			thread.start();
		}
	}


	@Override
	protected boolean isUseEvent() {
		return false;
	}


	private void createTicket() {
		UIManager.sharedInstance().showProgressDialog(this, null, getString(R.string.app_onemoment), true);

		if (!logFileCreated) {
			requestTicket(null);
			return;
		}

		UploadProvider uploadProvider = ZendeskConfig.INSTANCE.provider().uploadProvider();
		uploadProvider.uploadAttachment(logFile.getName(), logFile, "application/zip",  new ZendeskCallback<UploadResponse>() {
			@Override
			public void onSuccess(UploadResponse uploadResponse) {
				String token = uploadResponse.getToken();
				requestTicket(token);
			}

			@Override
			public void onError(ErrorResponse errorResponse) {
				UIManager.sharedInstance().dismissProgressDialog();
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error), getString(R.string.error_network), ContactUsActivity.this);
			}
		});
	}

	private void requestTicket(String attachToken) {
		RequestProvider requestProvider = ZendeskConfig.INSTANCE.provider().requestProvider();
		CreateRequest createRequest = new CreateRequest();

		if (attachToken != null) {
			createRequest.setAttachments(Arrays.asList(attachToken));
		}

		String subject = "";
		{
			// Set subject of e-mail
			String lastUserEmail = UserContext.sharedInstance().lastUserName();
			if (lastUserEmail != null && lastUserEmail.length() > 0) {
				try {
					subject = getString(R.string.kid_power_feedback_from) + " " + lastUserEmail +
							String.format(" (%s for Android)",
									getPackageManager().getPackageInfo(getPackageName(), 0).versionName
							);
				} catch (PackageManager.NameNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				try {
					subject = String.format(
							getString(R.string.feedback_email_subject),
							getPackageManager().getPackageInfo(getPackageName(), 0).versionName
					);
				} catch (PackageManager.NameNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		createRequest.setSubject(subject);
		createRequest.setDescription(sendDescript);
		createRequest.setEmail(UserContext.sharedInstance().lastUserName());

		requestProvider.createRequest(createRequest, new ZendeskCallback<CreateRequest>() {
			@Override
			public void onSuccess(CreateRequest result) {
				// Handle success
				UIManager.sharedInstance().dismissProgressDialog();
				Toast.makeText(ContactUsActivity.this, getResources().getString(R.string.dialog_success), Toast.LENGTH_LONG).show();
				finish();
			}

			@Override
			public void onError(ErrorResponse error) {
				// Handle error
				UIManager.sharedInstance().dismissProgressDialog();
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error), getString(R.string.error_network), ContactUsActivity.this);
			}
		});
	}

	public class CreateLogfileRunnable implements Runnable {
		@Override
		public void run() {
			synchronized (logFileCreated) {
				logFileCreated = false;
			}

			logFile = Utils.getLogFile();

			synchronized (logFileCreated) {
				logFileCreated = true;
			}

			runOnUiThread(new Runnable(){
				@Override
				public void run(){
					UIManager.sharedInstance().dismissProgressDialog();
					if (logFile != null)
						txtFileName.setText(logFile.getName());
					else
						Logger.error(TAG, "Log file creation failed");
				}
			});
		}
	}
}
