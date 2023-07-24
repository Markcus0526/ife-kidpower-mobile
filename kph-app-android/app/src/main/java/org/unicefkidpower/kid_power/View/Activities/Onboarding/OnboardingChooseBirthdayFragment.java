package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.OSDate;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHDateEditText;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperDialogFragment;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Ruifeng Shi on 8/27/2015.
 */
public class OnboardingChooseBirthdayFragment extends SuperFragment {
	// Intent filters
	private IntentFilter							intentFilter		= null;
	private BroadcastReceiver						receiver			= null;
	// End of 'Intent filters'

	// UI Elements
	private ImageView								ivAvatar			= null;
	private KPHTextView								txtUsername			= null;
	private KPHDateEditText							editBirthday		= null;
	private KPHButton								btnNext				= null;

	private CreateParentAccountDialogFragment		createParentAccountDialogFragment		= null;
	// End of 'UI Elements'

	// Member Variables
	private String									username								= "";
	private String									avatarId								= "";
	private Date									mBirthday								= null;
	private boolean									isCreatingFamilyAccount					= false;
	// End of 'Member Variables'


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View contentView = super.onCreateView(inflater, container, savedInstanceState);

		ivAvatar = (ImageView) contentView.findViewById(R.id.ivAvatar);
		txtUsername = (KPHTextView) contentView.findViewById(R.id.txt_username);
		editBirthday = (KPHDateEditText) contentView.findViewById(R.id.edit_birthday);
		editBirthday.setDateFormat(KPHDateEditText.DATE_FORMAT_MM_DD_YYYY);

		btnNext = (KPHButton) contentView.findViewById(R.id.btn_next);
		btnNext.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickedNext();
					}
				}
		);

		// Create broadcast receiver
		if (receiver == null) {
			receiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (getActivity() == null) {
						// Already detached
						return;
					}

					String action = intent.getAction();
					switch (action) {
						case KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_LOGIN_CLICKED:
							onLoginClicked();
							break;
						case KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_CREATED_CLICKED: {
							onFinishParentAccount();
							break;
						}
					}
				}
			};
		}


		if (intentFilter == null) {
			intentFilter = new IntentFilter();
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_LOGIN_CLICKED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_CREATED_CLICKED);

			LocalBroadcastManager.getInstance(getSafeContext()).registerReceiver(receiver, intentFilter);
		}


		// Initializing contents
		txtUsername.setText(TextUtils.isEmpty(username) ? "" : username);

		if (!TextUtils.isEmpty(avatarId)) {
			ivAvatar.setImageDrawable(KPHUserService.sharedInstance().getAvatarDrawable(avatarId));
		}

		if (mBirthday != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(mBirthday);
			editBirthday.updateDate(
					calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH) + 1,
					calendar.get(Calendar.DAY_OF_MONTH)
			);
		}
		// End of 'Initializing contents'

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
		return R.layout.fragment_onboarding_birthday;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setAvatarId(String avatarId) {
		this.avatarId = avatarId;
	}

	public Date getBirthday() {
		Calendar calendar = Calendar.getInstance();

		int year = 1992;
		int month = 1;
		int day = 1;
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.YEAR, editBirthday.getYear());
		calendar.set(Calendar.MONTH, editBirthday.getMonth());
		calendar.set(Calendar.DAY_OF_MONTH, editBirthday.getDayOfMonth());

		return calendar.getTime();
	}

	public void setBirthday(Date birthday) {
		this.mBirthday = birthday;
	}

	public void setIsCreatingFamilyAccount(boolean isFamilyAccount) {
		this.isCreatingFamilyAccount = isFamilyAccount;
	}

	private void onClickedNext() {
		if (!editBirthday.isDateValid()) {
			showBrandedDialog(getSafeContext().getString(R.string.invalid_birthday),
					getSafeContext().getString(R.string.ok), null, null);
			return;
		}


		// If the user is child, then restrict the app usage
		OSDate birthday = new OSDate(getBirthday());
		if (birthday.getYearsPassed() < KPHUserService.USER_MINIMUM_AGE &&
				!isCreatingFamilyAccount) {
			createParentAccountDialogFragment = new CreateParentAccountDialogFragment();
			createParentAccountDialogFragment.setData(username);
			if (getParentActivity() instanceof OnboardingActivity)
				createParentAccountDialogFragment.initKeyListener();
			getParentActivity().showDialogFragment(createParentAccountDialogFragment);

			return;
		}

		if (getParentActivity() instanceof OnboardingActivity) {
			((OnboardingActivity) getParentActivity()).newBirthday = getBirthday();
		}

		Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_CHOOSE_BIRTHDAY_CONFIRMED);
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
	}

	private void onLoginClicked() {
		if (getParentActivity() == null)
			return;

		getParentActivity().showDialogFragment(new LoginFragment());
	}


	private void onFinishParentAccount() {
		if (getParentActivity() == null)
			return;

		createParentAccountDialogFragment.dismiss(new SuperDialogFragment.SuperDialogDismissListener() {
			@Override
			public void onDismiss() {
				isCreatingFamilyAccount = true;

				if (getParentActivity() instanceof OnboardingActivity) {
					OnboardingActivity parentActivity = (OnboardingActivity)getParentActivity();

					parentActivity.isShownParentAccountCreationDialog = true;
					parentActivity.newBirthday = getBirthday();
				}

				Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_CHOOSE_BIRTHDAY_CONFIRMED);
				LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
			}
		});
	}


}
