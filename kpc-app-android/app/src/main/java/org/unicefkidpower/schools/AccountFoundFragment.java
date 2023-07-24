package org.unicefkidpower.schools;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import org.unicefkidpower.schools.server.apimanage.UserService;
import org.unicefkidpower.schools.ui.KPViewPager;

public class AccountFoundFragment extends SuperFragment implements View.OnClickListener {
	public final static String TAG = AccountFoundFragment.class.getSimpleName();
	private KPViewPager mViewPager;

	private TextView txtUserName;
	private TextView txtSchool;
	private TextView txtEmail;
	private TextView txtPhone;

	private UserService.ResLoginWithCode userInfo;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		FlurryAgent.onStartSession(getContext(), TAG);

		txtUserName = (TextView) rootView.findViewById(R.id.txt_user_name);
		txtSchool = (TextView) rootView.findViewById(R.id.txt_user_school);
		txtEmail = (TextView) rootView.findViewById(R.id.txt_user_email);
		txtPhone = (TextView) rootView.findViewById(R.id.txt_user_phone);

		rootView.findViewById(R.id.btn_back).setOnClickListener(this);
		rootView.findViewById(R.id.btn_thats_me).setOnClickListener(this);

		mViewPager = (KPViewPager) getActivity().findViewById(R.id.onboarding_pager);
		refreshUserInfo();

		return rootView;
	}

	public void setUserInfo(UserService.ResLoginWithCode userInfo) {
		this.userInfo = userInfo;
		refreshUserInfo();
	}

	private void refreshUserInfo() {
		if (userInfo == null) {
			return;
		}

		String username = "";

		if (!TextUtils.isEmpty(userInfo.title)) {
			username += (userInfo.title + " ");
		}

		if (!TextUtils.isEmpty(userInfo.firstName)) {
			username += (userInfo.firstName + " ");
		}

		if (!TextUtils.isEmpty(userInfo.lastName)) {
			username += (userInfo.lastName + " ");
		}

		if (txtUserName != null) {
			txtUserName.setText(username);
		}

		if (txtSchool != null) {
			txtSchool.setText(userInfo.group.name);
		}

		if (txtEmail != null) {
			txtEmail.setText(userInfo.email);
		}

		if (txtPhone != null) {
			txtPhone.setText(userInfo.cellPhone);
		}
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_account_found;
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

			case R.id.btn_thats_me:
				mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
				break;
		}
	}
}
