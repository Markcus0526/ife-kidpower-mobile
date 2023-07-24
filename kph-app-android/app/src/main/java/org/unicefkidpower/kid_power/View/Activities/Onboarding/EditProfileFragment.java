package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.KPHNotificationUtil;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.StringHelper;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.ErrorMessageDict;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.Model.Structure.KPHTracker;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserNameAvailability;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.Activities.Main.More.TrackerKidPowerBandFragment;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHEditText;
import org.unicefkidpower.kid_power.View.CustomControls.KPHSegmentedGroup;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

import java.util.Date;

import retrofit.client.Response;

/**
 * Created by Ruifeng Shi on 10/19/2015.
 */
public class EditProfileFragment extends SuperFragment {
	// UI Controls
	private View					contentView			= null;
	private KPHSegmentedGroup		sgGender			= null;
	private ImageView				avatarImageView		= null;
	private ImageView				deviceImageView		= null;

	private KPHTextView				usernameAvailabilityTextView		= null;
	private KPHTextView				emailErrorTextView					= null;
	private KPHTextView				deviceNameTextView					= null;
	private KPHTextView				connectionStateTextView				= null;
	private KPHTextView				resetPasswordTextView				= null;

	private KPHEditText				usernameEdit						= null;
	private KPHEditText				emailEdit							= null;
	private KPHEditText				birthdayEdit						= null;
	private KPHButton				btnSaveChanges						= null;
	private RelativeLayout			layoutAvatar						= null;
	private LinearLayout			layoutUsernameAvailability			= null;
	private ProgressBar				pbUsernameChecking					= null;
	private RelativeLayout			layoutTracker						= null;

	private String					sLastCheckedUsername		= "";
	private boolean					bIsUsernameValid			= true;
	private boolean					bIsEmailValid				= true;

	private KPHUserData				userData					= null;

	private IntentFilter			intentFilter				= null;
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (getActivity() == null) {
				// Already detached
				return;
			}

			String action = intent.getAction();
			switch (action) {
				case KPHBroadcastSignals.BROADCAST_SIGNAL_RESET_PASSWORD_LINK_SENT: {
					String sMessage = intent.getStringExtra("message");
					onResetPasswordLinkSent(sMessage);
					break;
				}
				case KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_CHOOSE_BIRTHDAY_CONFIRMED: {
					if (userData != null) {
						userData.setBirthday((Date) intent.getExtras().get("birthday"));
						if (birthdayEdit != null)
							birthdayEdit.setHint(userData.getBirthdayString());
					}
					break;
				}
				case KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_AVATAR_UPDATED: {
					setAvatarIcon();
					break;
				}
				case KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_TRACKER_SELECTED: {
					// Initialize current tracker
					KPHTracker registeredTracker = KPHUserService.sharedInstance().getTempTracker();
					if (registeredTracker == null) {
						// Error. Registered tracker must be set once device is connected and successfully attached to user
						return;
					}

					KPHUserService.sharedInstance().setCurrentTracker(registeredTracker);
				}
				case KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_DEVICE_CHANGED: {
					updateTrackerConnectionStatus();
					break;
				}
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		if (intentFilter == null) {
			intentFilter = new IntentFilter();
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_RESET_PASSWORD_LINK_SENT);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_CHOOSE_BIRTHDAY_CONFIRMED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_AVATAR_UPDATED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_DEVICE_CHANGED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_TRACKER_SELECTED);

			LocalBroadcastManager.getInstance(getSafeContext()).registerReceiver(receiver, intentFilter);
		}

		deviceImageView = (ImageView)contentView.findViewById(R.id.device_imageview);
		deviceNameTextView = (KPHTextView)contentView.findViewById(R.id.txt_tracker_name);
		connectionStateTextView = (KPHTextView)contentView.findViewById(R.id.txt_connection_state);

		layoutTracker = (RelativeLayout)contentView.findViewById(R.id.layout_tracker);
		layoutTracker.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedLayoutTracker();
			}
		});

		layoutAvatar = (RelativeLayout) contentView.findViewById(R.id.layout_avatar);
		layoutAvatar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onAvatarClicked();
			}
		});

		avatarImageView = (ImageView) contentView.findViewById(R.id.ivAvatar);
		Drawable avatar = KPHUserService.sharedInstance().getAvatarDrawable(
				KPHUserService.sharedInstance().getUserData().getAvatarId()
		);
		if (avatar != null) {
			avatarImageView.setImageDrawable(avatar);
		}

		usernameEdit = (KPHEditText) contentView.findViewById(R.id.edit_username);
		usernameEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					String username = ((EditText) v).getText().toString();
					checkUserNameAvailability(username);
				}
			}
		});
		usernameEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
					if (event == null || !event.isShiftPressed()) {
						String username = v.getText().toString();
						checkUserNameAvailability(username);
					}
				}

				return false;
			}
		});
		usernameEdit.addTextChangedListener(new TextWatcher() {
			String sTextBeforeBeingChanged = "";

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				sTextBeforeBeingChanged = String.valueOf(s);
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > KPHConstants.USERNAME_MAX_LENGTH ||
						StringHelper.isContainingSpecialCharacters(s.toString())) {
					usernameEdit.setText(sTextBeforeBeingChanged);
					usernameEdit.setSelection(usernameEdit.getText().length());
				}
			}
		});

		emailEdit = (KPHEditText) contentView.findViewById(R.id.edit_email);
		emailEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					String email = ((EditText) v).getText().toString();
					validateEmail(email);
				}
			}
		});
		emailEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
					if (event == null || !event.isShiftPressed()) {
						String email = ((EditText) v).getText().toString();
						validateEmail(email);
					}
				}

				return false;
			}
		});

		sgGender = (KPHSegmentedGroup) contentView.findViewById(R.id.segmentedGender);
		sgGender.setOnCheckedChangeListener(new OnGenderRadioGroupCheckChangedListener());

		birthdayEdit = (KPHEditText) contentView.findViewById(R.id.edit_birthday);
		birthdayEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//            onBirthdayClicked();
			}
		});

		btnSaveChanges = (KPHButton) contentView.findViewById(R.id.btn_save_changes);
		btnSaveChanges.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedSave();
			}
		});

		layoutUsernameAvailability = (LinearLayout) contentView.findViewById(R.id.layout_username_availability);
		usernameAvailabilityTextView = (KPHTextView) contentView.findViewById(R.id.txt_username_availability);
		pbUsernameChecking = (ProgressBar) contentView.findViewById(R.id.pb_username_checking);
		layoutUsernameAvailability.setVisibility(View.GONE);

		emailErrorTextView = (KPHTextView) contentView.findViewById(R.id.txt_email_availability);
		emailErrorTextView.setVisibility(View.GONE);


		resetPasswordTextView = (KPHTextView)contentView.findViewById(R.id.txt_reset_password);
		SpannableString ss = new SpannableString(getSafeContext().getString(R.string.reset_password));
		ss.setSpan(new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onClickedResetPassword();
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setColor(resetPasswordTextView.getCurrentTextColor());
			}
		}, 0, ss.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		resetPasswordTextView.setText(ss);
		resetPasswordTextView.setMovementMethod(LinkMovementMethod.getInstance());
		resetPasswordTextView.setHighlightColor(Color.TRANSPARENT);

		return contentView;
	}

	@Override
	public void onResume() {
		super.onResume();
		initUI();
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_edit_profile;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		LocalBroadcastManager.getInstance(getSafeContext()).unregisterReceiver(receiver);
		intentFilter = null;
	}


	private void initUI() {
		userData = KPHUserService.sharedInstance().getUserData();

		usernameEdit.setText(userData.getHandle());
		emailEdit.setText(userData.getEmail());
		if (userData.getGender().equalsIgnoreCase(KPHConstants.GENDER_MALE)) {
			sgGender.check(R.id.radioMale);
		} else if (userData.getGender().equalsIgnoreCase(KPHConstants.GENDER_FEMALE)) {
			sgGender.check(R.id.radioFemale);
		} else {
			sgGender.check(R.id.radioSkip);
		}

		birthdayEdit.setHint(userData.getBirthdayString());

		// Show/Hide email box according to whether user is adult or child
		if (userData.getUserType() != KPHUserData.USER_TYPE_CHILD)
			emailEdit.setVisibility(View.VISIBLE);
		else
			emailEdit.setVisibility(View.GONE);

		updateTrackerConnectionStatus();
	}


	private void onResetPasswordLinkSent(String sMessage) {
		if (getParentActivity() != null && getParentActivity() instanceof MainActivity) {
			KPHNotificationUtil.sharedInstance().showSuccessNotification(getParentActivity(), sMessage);
		}
	}


	public void checkUserNameAvailability(String username) {
		// Remove spaces at the prefix and suffix
		if (!username.equals(username.trim())) {
			username = username.trim();
			usernameEdit.setText(username);
			usernameEdit.setSelection(usernameEdit.getText().length());
		}

		if (username.equals(userData.getHandle())) {
			layoutUsernameAvailability.setVisibility(View.GONE);
			bIsUsernameValid = true;
			return;
		}

		// Check if the username was already checked
		if (!TextUtils.isEmpty(sLastCheckedUsername) && username.equals(sLastCheckedUsername))
			return;
		else
			sLastCheckedUsername = username;

		bIsUsernameValid = false;

		//Move to validating state
		moveToInCheckingUsernameProcess();

		if (TextUtils.isEmpty(username)) {
			showUsernameAvailabilityText(
					getSafeContext().getString(R.string.username_required),
					false
			);
			return;
		}

		if (StringHelper.isContainingSpecialCharacters(username)) {
			showUsernameAvailabilityText(getSafeContext().getString(R.string.username_invalid), false);
			return;
		}

		RestService.get().isHandleAvailable(username, new RestCallback<KPHUserNameAvailability>() {
			@Override
			public void success(KPHUserNameAvailability availability, Response response) {
				Boolean available = availability.available;

				if (available) {
					showUsernameAvailabilityText(
							getSafeContext().getString(R.string.available),
							true
					);
					bIsUsernameValid = true;
				} else {
					showUsernameAvailabilityText(
							getSafeContext().getString(R.string.username_taken),
							false
					);
				}
			}

			@Override
			public void failure(RestError restError) {
				showUsernameAvailabilityText(
						getSafeContext().getString(R.string.default_error),
						false
				);
			}
		});
	}

	private void moveToInCheckingUsernameProcess() {
		usernameAvailabilityTextView.setText(R.string.validating_username);
		usernameAvailabilityTextView.setTextColor(Color.WHITE);
		pbUsernameChecking.setVisibility(View.VISIBLE);
		layoutUsernameAvailability.setVisibility(View.VISIBLE);
	}

	private void showUsernameAvailabilityText(String sAvailability, boolean isAvailable) {
		pbUsernameChecking.setVisibility(View.GONE);

		if (isAvailable) {
			usernameAvailabilityTextView.setTextColor(
					UIManager.sharedInstance().getColor(R.color.kph_color_success_green)
			);
			layoutUsernameAvailability.setVisibility(View.GONE);
		} else {
			usernameAvailabilityTextView.setTextColor(Color.WHITE);
		}

		sAvailability = sAvailability.replace('\n', ' ');
		usernameAvailabilityTextView.setText(sAvailability);
	}


	/**
	 * Called when user click avatar
	 */
	public void onAvatarClicked() {
		SetAvatarFragment fragSetAvatar = new SetAvatarFragment();
		fragSetAvatar.setData(userData.getHandle(), userData.getAvatarId());
		getParentActivity().showNewFragment(fragSetAvatar);
	}


	/**
	 * Called when user click Save Changes button
	 */
	public void onClickedSave() {
		if (!checkChangedDataValid()) {
			return;
		}

		showProgressDialog();

		int userId = KPHUserService.sharedInstance().getUserData().getId();
		String username = usernameEdit.getText().toString();
		String email = emailEdit.getText().toString();
		Date dob = userData.getBirthday();
		String avatarId = KPHUserService.sharedInstance().getUserData().getAvatarId();
		String gender = userData.getGender();

		KPHUserService.sharedInstance().updateUserData(
				userId,
				username,
				email,
				userData.getFriendlyName(),
				gender,
				dob,
				avatarId,
				new onActionListener() {
					@Override
					public void completed(Object object) {
						if (object != null)
							KPHUserService.sharedInstance().saveUserData((KPHUserData) object);

						if (getParentActivity() == null)
							return;

						dismissProgressDialog();
						KPHNotificationUtil.sharedInstance().showSuccessNotification(getParentActivity(), getSafeContext().getString(R.string.profile_updated));
					}

					@Override
					public void failed(int code, String message) {
						showErrorDialog(ErrorMessageDict.serverResponse2UserFriendlyMessage(message));
					}
				}
		);
	}

	private boolean checkChangedDataValid() {
		String username = usernameEdit.getText().toString();
		String email = emailEdit.getText().toString();

		KPHUserData orgUserData = KPHUserService.sharedInstance().getUserData();
		if (orgUserData != null && orgUserData.getHandle() != null && orgUserData.getEmail() != null &&
				orgUserData.getGender() != null && userData.getGender() != null) {
			if (orgUserData.getHandle().equals(username) && orgUserData.getEmail().equals(email) &&
					orgUserData.getGender().equals(userData.getGender())) {
				return false;
			}
		}

		// Check if username is valid
		if (!bIsUsernameValid) {
			usernameEdit.requestFocus();
			return false;
		}

		if (!bIsEmailValid) {
			emailEdit.requestFocus();
			return false;
		}

		return true;
	}

	private void validateEmail(String email) {
		if (!KPHUtils.sharedInstance().isEmailValid(email)) {
			emailErrorTextView.setText(R.string.email_syntax_invalid);
			emailErrorTextView.setVisibility(View.VISIBLE);
			bIsEmailValid = false;
		} else {    //Hides invalid email error message if the email is valid
			emailErrorTextView.setText("");
			emailErrorTextView.setVisibility(View.GONE);
			bIsEmailValid = true;
		}
	}

	/**
	 * Called when user click birthday button
	 */
	public void onBirthdayClicked() {
		OnboardingChooseBirthdayFragment fragChooseBirthday = new OnboardingChooseBirthdayFragment();
		fragChooseBirthday.setBirthday(userData.getBirthday());
		getParentActivity().showNewFragment(fragChooseBirthday);
	}

	private class OnGenderRadioGroupCheckChangedListener implements RadioGroup.OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
				case R.id.radioSkip:
					userData.setGender(KPHConstants.GENDER_SKIP);
					break;
				case R.id.radioMale:
					userData.setGender(KPHConstants.GENDER_MALE);
					break;
				case R.id.radioFemale:
					userData.setGender(KPHConstants.GENDER_FEMALE);
					break;
			}
		}
	}

	public void setAvatarIcon() {
		userData = KPHUserService.sharedInstance().getUserData();
		Drawable avatar = KPHUserService.sharedInstance().getAvatarDrawable(
				userData.getAvatarId()
		);

		if (avatar != null) {
			avatarImageView.setImageDrawable(avatar);
		}
	}


	private void onClickedResetPassword() {
		ResetPasswordFragment resetPasswordFragment = new ResetPasswordFragment();
		resetPasswordFragment.setShouldShowEmailBox(false);
		getParentActivity().showDialogFragment(resetPasswordFragment);
	}


	private void onClickedLayoutTracker() {
		if (userData == null)
			return;

		int trackerType = KPHUserService.sharedInstance().loadCurrentTrackerType();
		if (trackerType == KPHUserService.TRACKER_TYPE_NONE) {
			// Go to the connection page
			SelectDeviceFragment fragment = new SelectDeviceFragment();
			fragment.showTabBar(false);
			fragment.setApplyToChilds(true);
			fragment.setData(userData.getId(), userData.getHandle(), userData.getAvatarId());
			getParentActivity().showNewFragment(fragment);
		} else {
			// Go to kid power band tracker page
			KPHTracker curTracker = KPHUserService.sharedInstance().currentTracker();
			if (trackerType == KPHUserService.TRACKER_TYPE_KIDPOWERBAND) {
				TrackerKidPowerBandFragment fragment = new TrackerKidPowerBandFragment();
				fragment.setData(userData.getId(), userData.getHandle(), curTracker.getDeviceCode());
				getParentActivity().showNewFragment(fragment);
			} else {
				OwnDeviceTrackerFragment fragment = new OwnDeviceTrackerFragment();
				fragment.setUsername(userData.getHandle());
				getParentActivity().showNewFragment(fragment);
			}
		}
	}


	private void updateTrackerConnectionStatus() {
		int trackerType = KPHUserService.sharedInstance().loadCurrentTrackerType();
		if (trackerType == KPHUserService.TRACKER_TYPE_GOOGLEFIT) {
			deviceImageView.setImageResource(R.drawable.google_fit);
			deviceImageView.setVisibility(View.VISIBLE);

			deviceNameTextView.setText(R.string.google_fit);

			connectionStateTextView.setVisibility(View.GONE);
		} else if (trackerType == KPHUserService.TRACKER_TYPE_HEALTHKIT) {
			deviceImageView.setImageResource(R.drawable.health_kit);
			deviceImageView.setVisibility(View.VISIBLE);

			deviceNameTextView.setText(getSafeContext().getString(R.string.health_app));

			connectionStateTextView.setVisibility(View.GONE);
		} else if (trackerType == KPHUserService.TRACKER_TYPE_KIDPOWERBAND) {
			deviceImageView.setImageResource(R.drawable.orange_band);
			deviceImageView.setVisibility(View.VISIBLE);

			KPHTracker curTracker = KPHUserService.sharedInstance().currentTracker();
			if (curTracker == null)
				deviceNameTextView.setText(getSafeContext().getString(R.string.kid_power_band));
			else
				deviceNameTextView.setText(getSafeContext().getString(R.string.kid_power_band) + " " + curTracker.getDeviceCode());

			connectionStateTextView.setVisibility(View.GONE);
		} else {
			deviceImageView.setVisibility(View.GONE);
			deviceNameTextView.setText(R.string.link_an_activity_tacker);
			connectionStateTextView.setVisibility(View.GONE);
		}
	}


}
