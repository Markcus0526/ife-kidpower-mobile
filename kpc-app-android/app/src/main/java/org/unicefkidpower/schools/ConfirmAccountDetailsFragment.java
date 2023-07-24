package org.unicefkidpower.schools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import org.unicefkidpower.schools.context.UserContext;
import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.model.UserManager;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.UserService;
import org.unicefkidpower.schools.ui.KPRadioButton;
import org.unicefkidpower.schools.ui.KPViewPager;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class ConfirmAccountDetailsFragment extends SuperFragment implements View.OnClickListener {
	public final static String TAG = ConfirmAccountDetailsFragment.class.getSimpleName();

	private UserService.ResLoginWithCode userInfo;

	private TextView textTitle;
	private Button btnSelectTitle;
	private RelativeLayout rlTitleListContainer;
	private LinearLayout llTitleList;
	private TextView textMr, textMs, textMrs, textDr, textProf;

	private EditText editFirstName, editLastName, editNickname, editAddress1, editAddress2;
	private EditText editCity, editState, editZipCode, editEmail, editPhoneNumber;
	private RadioGroup rgContactPreference;

	private KPViewPager mViewPager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		FlurryAgent.onStartSession(getContext(), TAG);

		textTitle = (TextView) rootView.findViewById(R.id.textTitle);

		btnSelectTitle = (Button) rootView.findViewById(R.id.btnSelectTitle);
		btnSelectTitle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedChooseTitle();
			}
		});

		rlTitleListContainer = (RelativeLayout) rootView.findViewById(R.id.rlSalutation);
		rlTitleListContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				rlTitleListContainer.setVisibility(View.GONE);
			}
		});
		llTitleList = (LinearLayout) rootView.findViewById(R.id.llSalutation);

		textMr = (TextView) rootView.findViewById(R.id.txtMr);
		textMr.setTag(1);
		textMr.setOnClickListener(onSelectedTitle);

		textMs = (TextView) rootView.findViewById(R.id.txtMs);
		textMs.setTag(2);
		textMs.setOnClickListener(onSelectedTitle);

		textMrs = (TextView) rootView.findViewById(R.id.txtMrs);
		textMrs.setTag(3);
		textMrs.setOnClickListener(onSelectedTitle);

		textDr = (TextView) rootView.findViewById(R.id.txtDr);
		textDr.setTag(4);
		textDr.setOnClickListener(onSelectedTitle);

		textProf = (TextView) rootView.findViewById(R.id.txtProf);
		textProf.setTag(5);
		textProf.setOnClickListener(onSelectedTitle);


//		editTitle = (EditText) rootView.findViewById(R.id.edit_title);
		editFirstName = (EditText) rootView.findViewById(R.id.edit_first_name);
		editLastName = (EditText) rootView.findViewById(R.id.edit_last_name);
		editNickname = (EditText) rootView.findViewById(R.id.edit_nickname);
		editAddress1 = (EditText) rootView.findViewById(R.id.edit_address_1);
		editAddress2 = (EditText) rootView.findViewById(R.id.edit_address_2);
		editCity = (EditText) rootView.findViewById(R.id.edit_city);
		editState = (EditText) rootView.findViewById(R.id.edit_state);
		editZipCode = (EditText) rootView.findViewById(R.id.edit_zipcode);
		editEmail = (EditText) rootView.findViewById(R.id.edit_email_address);
		editPhoneNumber = (EditText) rootView.findViewById(R.id.edit_phone_number);
		rgContactPreference = (RadioGroup) rootView.findViewById(R.id.rg_contact_preference);

		mViewPager = (KPViewPager) getActivity().findViewById(R.id.onboarding_pager);

		rootView.findViewById(R.id.btn_continue).setOnClickListener(this);

		if (userInfo != null) {
			textTitle.setText(userInfo.title);
//			editTitle.setText(userInfo.title);
			editFirstName.setText(userInfo.firstName);
			editLastName.setText(userInfo.lastName);
			editNickname.setText(userInfo.nickName);
			editAddress1.setText(userInfo.address1);
			editAddress2.setText(userInfo.address2);
			editCity.setText(userInfo.city);
			editState.setText(userInfo.state);
			editZipCode.setText(userInfo.postCode);
			editEmail.setText(userInfo.email);
			editPhoneNumber.setTag(userInfo.cellPhone);
		}

		KPRadioButton smsRadio = (KPRadioButton) rootView.findViewById(R.id.radioSMSEmail);
		if (UserContext.sharedInstance().getAppLocale().getLanguage().equalsIgnoreCase("nl") ||
				UserContext.sharedInstance().getAppLocale().getCountry().equalsIgnoreCase("gb") ||
				UserContext.sharedInstance().getAppLocale().getCountry().equalsIgnoreCase("uk")) {
			editPhoneNumber.setVisibility(View.GONE);
			smsRadio.setVisibility(View.GONE);
			rgContactPreference.check(R.id.radioEmail);
		} else {
			editPhoneNumber.setVisibility(View.VISIBLE);
			smsRadio.setVisibility(View.VISIBLE);
			rgContactPreference.check(R.id.radioSMSEmail);
		}

		return rootView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_confirm_account_details;
	}

	@Override
	protected boolean isUseEvent() {
		return false;
	}

	public void setUserInfo(UserService.ResLoginWithCode userInfo) {
		this.userInfo = userInfo;
	}

	private void onContinueButtonClicked() {
		String title = textTitle.getText().toString();
//		String title = editTitle.getText().toString();
		String firstName = editFirstName.getText().toString();
		String lastName = editLastName.getText().toString();
		String nickName = editNickname.getText().toString();
		String address1 = editAddress1.getText().toString();
		String address2 = editAddress2.getText().toString();
		String city = editCity.getText().toString();
		String state = editState.getText().toString();
		String zipCode = editZipCode.getText().toString();
		String email = editEmail.getText().toString();
		String phone = editPhoneNumber.getText().toString();
		int contactPreference = rgContactPreference.getCheckedRadioButtonId() == R.id.radioSMSEmail ? 1 : 2;

		UIManager.sharedInstance().showProgressDialog(getActivity(), null, getString(R.string.please_wait), true);

		ServerManager.sharedInstance().updateUserInfo(
				title, firstName, lastName, nickName, address1, address2, city, state, zipCode,
				email, phone, contactPreference, Integer.toString(userInfo._id),
				new RestCallback<UserService.ResUserUpdate>() {
					@Override
					public void success(UserService.ResUserUpdate resUserUpdate, Response response) {
						UIManager.sharedInstance().dismissProgressDialog();
						UserManager.sharedInstance().updateUserInfo(resUserUpdate);
						mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
						((OnboardingActivity) _parentActivity).setCurrentStep(2);
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						UIManager.sharedInstance().dismissProgressDialog();
						String errorMsg = ServerManager.getErrorMessageForRetrofitError(retrofitError, getActivity());
						AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
								getString(R.string.coachsettings_user_profile_update_failed) + " : " +
										(errorMsg == null ? getString(R.string.error_unknown) : errorMsg),
								getActivity());
					}
				}
		);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_continue:
				onContinueButtonClicked();
				break;
		}
	}


	private void onClickedChooseTitle() {
		rlTitleListContainer.setVisibility(View.VISIBLE);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		View view = (View) textTitle.getParent().getParent();
		int left = view.getLeft();
		int top = view.getTop() + textTitle.getHeight();

		params.leftMargin = left;
		params.topMargin = top;

		llTitleList.setLayoutParams(params);
	}


	private int getRelativeLeft(View myView) {
		if (myView.getParent() == myView.getRootView())
			return myView.getLeft();
		else
			return myView.getLeft() + getRelativeLeft((View) myView.getParent());
	}

	private int getRelativeTop(View myView) {
		if (myView.getParent() == myView.getRootView())
			return myView.getTop();
		else
			return myView.getTop() + getRelativeTop((View) myView.getParent());
	}

	private View.OnClickListener onSelectedTitle = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			TextView textView = (TextView) v;
			String strTitle = textView.getText().toString();
			textTitle.setText(strTitle);

			rlTitleListContainer.setVisibility(View.GONE);
		}
	};

}
