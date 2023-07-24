package org.unicefkidpower.schools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flurry.android.FlurryAgent;

import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.ui.KPEditText;
import org.unicefkidpower.schools.ui.KPViewPager;
import org.unicefkidpower.schools.ui.UiUtils;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class FindApplicationFragment extends SuperFragment implements View.OnClickListener {
	public final static String TAG = FindApplicationFragment.class.getSimpleName();
	private KPEditText mEmailAddressField;
	private KPViewPager mViewPager;
	public static String findEmail;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		FlurryAgent.onStartSession(getContext(), TAG);
		rootView.findViewById(R.id.btn_back).setOnClickListener(this);
		rootView.findViewById(R.id.btn_continue).setOnClickListener(this);
		mEmailAddressField = (KPEditText) rootView.findViewById(R.id.edit_email);

		mViewPager = (KPViewPager) getActivity().findViewById(R.id.onboarding_pager);
		((OnboardingActivity) _parentActivity).setCurrentStep(0);

		return rootView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_find_application;
	}

	@Override
	protected boolean isUseEvent() {
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				getActivity().finish();
				break;

			case R.id.btn_continue:
				onContinueButtonPressed();
				break;
		}
	}

	private void onContinueButtonPressed() {
		findEmail = mEmailAddressField.getText().toString();

		if (findEmail.length() > 0) {
			UiUtils.hideKeyboard(getActivity());

			UIManager.sharedInstance().showProgressDialog(_parentActivity, null, getString(R.string.app_onemoment), true);

			ServerManager.sharedInstance().findApplication(findEmail, new RestCallback<String>() {
				@Override
				public void success(String resFindApplication, Response response) {
					UIManager.sharedInstance().dismissProgressDialog();
					mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
				}

				@Override
				public void failure(RetrofitError retrofitError, String message) {
					UIManager.sharedInstance().dismissProgressDialog();
					AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
							message,
							_parentActivity);
				}
			});
		}
	}
}
