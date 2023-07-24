package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.ErrorMessageDict;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.Model.Structure.BaseResponse;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHEditText;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperNormalSizeDialogFragment;

import retrofit.client.Response;

/**
 * Created by Ruifeng Shi on 10/29/2015.
 */
public class ResetPasswordFragment extends SuperNormalSizeDialogFragment {
	// Constants
	public static final String			EXTRA_SHOULD_SHOW_EMAIL_BOX = "EXTRA_SHOULD_SHOW_EMAIL_BOX";
	public static final String			EXTRA_USERNAME = "username";

	// UI Elements
	private KPHEditText					editUsernameOrEmail		= null;
	private KPHTextView					txtDescription			= null;
	private KPHButton					btnResetPassword		= null;
	private ImageButton					btnBack					= null;

	// Member Variables
	private boolean						bShouldShowEmailBox		= false;
	private String						sInitialUsername		= "";
	private boolean						bResetEmailSent			= false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// create root view
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		editUsernameOrEmail = (KPHEditText) rootView.findViewById(R.id.edit_username_or_email_address);
		txtDescription = (KPHTextView) rootView.findViewById(R.id.txt_reset_password_description);
		btnResetPassword = (KPHButton) rootView.findViewById(R.id.btn_reset_password);
		btnBack = (ImageButton) rootView.findViewById(R.id.btn_back);

		if (bShouldShowEmailBox) {
			editUsernameOrEmail.setVisibility(View.VISIBLE);
			editUsernameOrEmail.setText(sInitialUsername);
			txtDescription.setVisibility(View.VISIBLE);
		} else {
			editUsernameOrEmail.setVisibility(View.GONE);
			txtDescription.setVisibility(View.GONE);
		}

		btnResetPassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onResetPasswordButtonClicked();
			}
		});
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideKeyboard();
				dismiss();
			}
		});

		return rootView;
	}

	@Override
	public void startAction() {
		// No action. Do nothing.
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_reset_password;
	}

	public void setShouldShowEmailBox(boolean shouldShowEmailBox) {
		this.bShouldShowEmailBox = shouldShowEmailBox;
	}

	public void setUsername(String username) {
		this.sInitialUsername = username;
	}

	private void onResetPasswordButtonClicked() {
		if (bResetEmailSent) {
			dismiss();
			return;
		}

		String sUsernameOrEmail = editUsernameOrEmail.getText().toString();
		if (!sUsernameOrEmail.equals(sUsernameOrEmail.trim())) {
			sUsernameOrEmail = sUsernameOrEmail.trim();
			editUsernameOrEmail.setText(sUsernameOrEmail);
			editUsernameOrEmail.setSelection(editUsernameOrEmail.getText().length());
		}

		if (!bShouldShowEmailBox) {
			sUsernameOrEmail = KPHUserService.sharedInstance().getUserData().getHandle();
		}

		showProgressDialog();

		RestService.get().sendResetLink(sUsernameOrEmail, new RestCallback<BaseResponse>() {
			@Override
			public void failure(RestError restError) {
				String message = KPHUtils.sharedInstance().getNonNullMessage(restError);
				showErrorDialog(ErrorMessageDict.serverResponse2UserFriendlyMessage(message));
			}

			@Override
			public void success(BaseResponse response, Response rawResponse) {
				if (getParentActivity() == null)
					return;

				dismissProgressDialog();

				if (response.getStatus() == RestCallback.REST_ERROR_SUCCESS) {
					bResetEmailSent = true;

					if (!bShouldShowEmailBox) {
						dismiss();
						Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_RESET_PASSWORD_LINK_SENT);
						intent.putExtra("message", response.getMessage());
						LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
					} else {    //When user is not logged in
						editUsernameOrEmail.setVisibility(View.GONE);
						txtDescription.setText(R.string.password_reset_email_sent);
						txtDescription.setGravity(Gravity.CENTER);
						btnResetPassword.setText(R.string.return_to_login);
					}
				}
			}
		});
	}
}
