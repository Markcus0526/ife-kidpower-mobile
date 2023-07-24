package org.unicefkidpower.schools;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.helper.DebouncedOnClickListener;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.UserService;
import org.unicefkidpower.schools.ui.KPEditText;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by donal_000 on 1/19/2015.
 */
public class CoachPasswordDialog extends Dialog {
	protected Activity parentActivity;
	protected CoachPasswordDialogListener listener;

	private KPEditText editPassword;

	public CoachPasswordDialog(Activity parentActivity, CoachPasswordDialogListener listener) {
		super(parentActivity);

		this.parentActivity = parentActivity;
		this.listener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_checkcoachpassword);

		ResolutionSet._instance.iterateChild(findViewById(R.id.layout_parent));

		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		setCanceledOnTouchOutside(false);

		initControl();
	}

	private void initControl() {
		editPassword = (KPEditText) findViewById(R.id.edit_password);

		findViewById(R.id.btn_unlock).setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				final String password = editPassword.getText().toString();
				if (listener != null) {
					// call validatePassword
					ServerManager.sharedInstance().validatePassword(password, new RestCallback<UserService.ResValidatePassword>() {
						@Override
						public void success(UserService.ResValidatePassword resValidatePassword, Response response) {
							if (resValidatePassword.validate) {
								dismiss();
								listener.onEnter(password);
							} else {
								AlertDialogWrapper.showErrorAlert(parentActivity.getString(R.string.dialog_caution),
										parentActivity.getString(R.string.coachpassworddlg_check_password),
										parentActivity);
							}
						}

						@Override
						public void failure(RetrofitError retrofitError, String message) {
							String errorMsg = ServerManager.getErrorMessageForRetrofitError(retrofitError, parentActivity);
							AlertDialogWrapper.showErrorAlert(parentActivity.getString(R.string.dialog_error),
									errorMsg == null ? parentActivity.getString(R.string.coachpassworddlg_check_password) : errorMsg,
									parentActivity);
						}
					});
				}
			}
		});

		findViewById(R.id.btn_cancel).setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				dismiss();
				if (listener != null) {
					listener.onCancel();
				}
			}
		});
	}

	public interface CoachPasswordDialogListener {

		void onEnter(String coachPassword);

		void onCancel();
	}
}
