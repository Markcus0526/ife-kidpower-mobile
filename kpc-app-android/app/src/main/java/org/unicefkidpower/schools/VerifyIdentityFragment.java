package org.unicefkidpower.schools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import org.unicefkidpower.schools.context.UserContext;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.UserService;
import org.unicefkidpower.schools.ui.KPEditText;
import org.unicefkidpower.schools.ui.KPViewPager;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class VerifyIdentityFragment extends SuperFragment implements View.OnClickListener {
	public final static String TAG = VerifyIdentityFragment.class.getSimpleName();
	private KPViewPager mViewPager;
	private KPEditText mVerificationCodeField;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		FlurryAgent.onStartSession(getContext(), TAG);

		mViewPager = (KPViewPager) getActivity().findViewById(R.id.onboarding_pager);

		mVerificationCodeField = (KPEditText) rootView.findViewById(R.id.edit_verify_code);

		rootView.findViewById(R.id.btn_back).setOnClickListener(this);
		rootView.findViewById(R.id.btn_continue).setOnClickListener(this);

		return rootView;
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_verify_identity;
	}


	@Override
	protected boolean isUseEvent() {
		return false;
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
				break;

			case R.id.btn_continue:
				onContinueButtonPressed();
				break;
		}
	}


	private void onContinueButtonPressed() {
		String verificationCode = mVerificationCodeField.getText().toString();

		if (verificationCode.length() > 0) {
			UIManager.sharedInstance().showProgressDialog(getContext(), null, getString(R.string.app_onemoment), true);

			ServerManager.sharedInstance().verifyIdentity(FindApplicationFragment.findEmail, verificationCode, new RestCallback<UserService.ResLoginWithCode>() {
				@Override
				public void success(UserService.ResLoginWithCode resLoginForSuccess, Response response) {
					UIManager.sharedInstance().dismissProgressDialog();

					ServerManager.sharedInstance().setAccessToken(resLoginForSuccess.access_token);
					UserContext.sharedInstance().setLastUserName(resLoginForSuccess.email);

					((OnboardingActivity) _parentActivity).setUserInfo(resLoginForSuccess);

					if (UserContext.sharedInstance().setUserLanguage(resLoginForSuccess.language)) {
						((OnboardingActivity) _parentActivity).restartActivity(mViewPager.getCurrentItem() + 1);
					} else {
						mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
					}
				}

				@Override
				public void failure(RetrofitError retrofitError, String message) {
					UIManager.sharedInstance().dismissProgressDialog();
					Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
}
