package org.unicefkidpower.schools;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.flurry.android.FlurryAgent;

import org.unicefkidpower.schools.context.UserContext;
import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.model.UserManager;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.UserService;
import org.unicefkidpower.schools.ui.KPViewPager;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class CreatePasswordFragment extends SuperFragment implements View.OnClickListener {
	public final static String TAG = CreatePasswordFragment.class.getSimpleName();
	private KPViewPager mViewPager;

	private EditText editPassword, editPasswordConfirm;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		FlurryAgent.onStartSession(getContext(), TAG);

		editPassword = (EditText) rootView.findViewById(R.id.edit_create_password);
		editPasswordConfirm = (EditText) rootView.findViewById(R.id.edit_create_password_confirm);

		rootView.findViewById(R.id.btn_back).setOnClickListener(this);
		rootView.findViewById(R.id.btn_continue).setOnClickListener(this);

		mViewPager = (KPViewPager) getActivity().findViewById(R.id.onboarding_pager);

		return rootView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_create_password;
	}

	@Override
	protected boolean isUseEvent() {
		return false;
	}

	private void onContinueButtonClicked() {
		final String password = editPassword.getText().toString();
		String passwordConfirm = editPasswordConfirm.getText().toString();

		if (!isDataValid(password, passwordConfirm)) {
			return;
		}

		UIManager.sharedInstance().showProgressDialog(getActivity(), null, getString(R.string.please_wait), true);
		ServerManager.sharedInstance().updateUserPassword(
				Integer.toString(((OnboardingActivity) _parentActivity).getUserInfo()._id),
				password,
				new RestCallback<UserService.ResUserUpdate>() {
					@Override
					public void success(UserService.ResUserUpdate resUserUpdate, Response response) {
						UIManager.sharedInstance().dismissProgressDialog();

						// set current user
						UserContext.sharedInstance().setLastUserPassword(password);
						UserManager.sharedInstance().updateUserInfo(resUserUpdate);
						mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
						((OnboardingActivity) _parentActivity).setCurrentStep(1);
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						UIManager.sharedInstance().dismissProgressDialog();
						String errorMsg = ServerManager.getErrorMessageForRetrofitError(retrofitError, getActivity());
						AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
								getString(R.string.coachsettings_userpassword_create_failed) + " : " +
										(errorMsg == null ?
												getString(R.string.error_unknown) : errorMsg), getActivity());
					}
				}
		);
	}

	private boolean isDataValid(String password, String passwordConfirm) {
		if (TextUtils.isEmpty(password)) {
			editPassword.setFocusableInTouchMode(true);
			editPassword.requestFocus();
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.password_require),
					getActivity());
			return false;
		}

		if (TextUtils.isEmpty(passwordConfirm)) {
			editPasswordConfirm.setFocusableInTouchMode(true);
			editPasswordConfirm.requestFocus();
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.password_confirmation),
					getActivity());
			return false;
		}

		if (!password.equals(passwordConfirm)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.password_does_not_match),
					getActivity());
			return false;
		}

		if (password.length() < 6 || passwordConfirm.length() < 6) {
			editPassword.setFocusableInTouchMode(true);
			editPassword.requestFocus();
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.password_length),
					getActivity());
			return false;
		}

		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
				break;

			case R.id.btn_continue:
				onContinueButtonClicked();
				break;

			default:
				break;
		}
	}
}
