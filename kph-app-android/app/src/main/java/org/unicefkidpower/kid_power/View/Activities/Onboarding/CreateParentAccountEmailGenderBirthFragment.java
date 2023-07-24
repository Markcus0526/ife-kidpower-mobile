package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.Intent;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.unicefkidpower.kid_power.View.CustomControls.KPHDateEditText;
import org.unicefkidpower.kid_power.View.CustomControls.KPHEditText;
import org.unicefkidpower.kid_power.View.CustomControls.KPHRadioButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHSegmentedGroup;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserEmailAvailability;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.UIManager;

import java.util.Calendar;
import java.util.Date;

import retrofit.client.Response;

import static org.unicefkidpower.kid_power.Misc.KPHConstants.GENDER_FEMALE;
import static org.unicefkidpower.kid_power.Misc.KPHConstants.GENDER_MALE;
import static org.unicefkidpower.kid_power.Misc.KPHConstants.GENDER_SKIP;

/**
 * Created by Ruifeng Shi on 1/9/2017.
 */

public class CreateParentAccountEmailGenderBirthFragment extends CreateParentAccountSuperFragment {
	// Email portion
	public KPHEditText			editEmail				= null;
	private LinearLayout		llEmailError			= null;
	private KPHTextView			txtEmailErrorMark		= null;
	private KPHTextView			txtEmailError			= null;
	private ImageView			imgClearCircle			= null;

	// Gender portion
	private KPHSegmentedGroup	genderSegGroup			= null;
	private KPHRadioButton		rdoGenderMale			= null;
	private KPHRadioButton		rdoGenderFemale			= null;
	private KPHRadioButton		rdoGenderSkip			= null;

	// Birthday portion
	public  KPHDateEditText		editBirthday			= null;
	private LinearLayout		llBirthError			= null;
	private KPHTextView			txtBirthErrorMark		= null;
	private KPHTextView			txtBirthError			= null;

	public String				sGender					= GENDER_SKIP;
	private String				sLastCheckedEmail		= "";
	private boolean				bIsEmailValid			= false;
	private String				fbEmail					= "";


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View vwRoot = super.onCreateView(inflater, container, savedInstanceState);

		vwRoot.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedBack();
			}
		});
		vwRoot.findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedNext();
			}
		});

		editEmail = (KPHEditText) vwRoot.findViewById(R.id.edit_email);
		editEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					String email = ((KPHEditText) v).getText().toString();
					checkEmailAvailability(email);
				}
			}
		});
		editEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
					if (event == null || !event.isShiftPressed()) {
						String email = v.getText().toString();
						checkEmailAvailability(email);
					}
				}

				return false;
			}
		});
		editEmail.addTextChangedListener(emailTextWatcher);
		if (fbEmail.length() > 0)
			editEmail.setText(fbEmail);

		llEmailError = (LinearLayout) vwRoot.findViewById(R.id.layout_email_error);
		llEmailError.setVisibility(View.GONE);

		txtEmailErrorMark = (KPHTextView) vwRoot.findViewById(R.id.email_error_mark);
		txtEmailError = (KPHTextView) vwRoot.findViewById(R.id.email_error_text);

		imgClearCircle = (ImageView) vwRoot.findViewById(R.id.ivClearCircle);
		imgClearCircle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedClearEmail();
			}
		});

		// Gender portion
		genderSegGroup = (KPHSegmentedGroup) vwRoot.findViewById(R.id.segmented_gender);
		genderSegGroup.setOnCheckedChangeListener(
				new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						switch (checkedId) {
							case R.id.radio_skip:
								sGender = GENDER_SKIP;
								break;
							case R.id.radio_male:
								sGender = KPHConstants.GENDER_MALE;
								break;
							case R.id.radio_female:
								sGender = GENDER_FEMALE;
								break;
						}
					}
				}
		);

		rdoGenderMale = (KPHRadioButton) vwRoot.findViewById(R.id.radio_male);
		rdoGenderFemale = (KPHRadioButton) vwRoot.findViewById(R.id.radio_female);
		rdoGenderSkip = (KPHRadioButton) vwRoot.findViewById(R.id.radio_skip);

		// Birthday portion
		editBirthday = (KPHDateEditText) vwRoot.findViewById(R.id.edit_birthday);
		editBirthday.setDateFormat(KPHDateEditText.DATE_FORMAT_MM_DD_YYYY);
		editBirthday.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					String dob = ((KPHDateEditText) v).getText().toString();
					checkDOBAvailability(dob);
				} else {
					editBirthday.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_edittext_lightgray));
					llBirthError.setVisibility(View.GONE);
				}
			}
		});

		llBirthError = (LinearLayout) vwRoot.findViewById(R.id.layout_dob_error);
		llBirthError.setVisibility(View.GONE);

		txtBirthErrorMark = (KPHTextView) vwRoot.findViewById(R.id.dob_error_mark);
		txtBirthError = (KPHTextView) vwRoot.findViewById(R.id.dob_error_text);

		KPHUserData parentData = KPHUserService.sharedInstance().getUserData();
		if (parentData != null) {
			editEmail.setText(parentData.getEmail());

			Date birthday = parentData.getBirthday();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(birthday);

			editBirthday.setText(String.format("%04d / %02d / %02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));

			if (parentData.getGender().equalsIgnoreCase(GENDER_MALE)) {
				sGender = parentData.getGender();
				rdoGenderMale.setSelected(true);
			} else if (parentData.getGender().equalsIgnoreCase(GENDER_FEMALE)) {
				sGender = parentData.getGender();
				rdoGenderFemale.setSelected(true);
			} else {
				sGender = KPHConstants.GENDER_SKIP;
				rdoGenderSkip.setSelected(true);
			}
		} else {
			if (sGender.equalsIgnoreCase(GENDER_MALE)) {
				rdoGenderMale.setSelected(true);
			} else if (sGender.equalsIgnoreCase(GENDER_FEMALE)) {
				rdoGenderFemale.setSelected(true);
			} else {
				rdoGenderSkip.setSelected(true);
			}
		}

		KPHUtils.sharedInstance().hideKeyboardInView(vwRoot);

		return vwRoot;
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_create_parent_account_email_gender_birth;
	}


	public void setData(String email, String gender) {
		this.fbEmail = email;
		this.sGender = gender;

		if (editEmail != null) {
			editEmail.setText(this.fbEmail);
		}

		if (genderSegGroup != null) {
			if (sGender.equalsIgnoreCase(GENDER_MALE)) {
				rdoGenderMale.setSelected(true);
			} else if (sGender.equalsIgnoreCase(GENDER_FEMALE)) {
				rdoGenderFemale.setSelected(true);
			} else {
				rdoGenderSkip.setSelected(true);
			}
		}
	}


	private void onClickedBack() {
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(
				new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_BACK_CLICKED)
		);
	}


	private void onClickedNext() {
		KPHUserData parentData = KPHUserService.sharedInstance().getUserData();
		if (parentData != null && parentData.getEmail().equalsIgnoreCase(editEmail.getText().toString())) {
			bIsEmailValid = true;
		}

		String message = "";
		if (editEmail.length() == 0 && editBirthday.length() == 0) {
			message = getSafeContext().getString(R.string.dialog_email_dob_empty);
		} else if (editEmail.length() == 0) {
			message = getSafeContext().getString(R.string.email_required);
		} else if (!bIsEmailValid && editBirthday.length() == 0) {
			message = getSafeContext().getString(R.string.dialog_email_dob_invalid);
		} else if (!KPHUtils.sharedInstance().isEmailValid(editEmail.getText().toString())) {
			message = getSafeContext().getString(R.string.email_syntax_invalid);
		} else if (editBirthday.length() == 0) {
			message = getSafeContext().getString(R.string.no_dob_inputted);
		} else if (!editBirthday.isDateValid()) {
			message = getSafeContext().getString(R.string.invalid_birthday);
		} else if (!bIsEmailValid) {
			message = getSafeContext().getString(R.string.account_email_exist);
		} else {
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			if (year - editBirthday.getYear() < KPHUserService.USER_MINIMUM_AGE) {
				message = getString(R.string.be_13_to_parent_account);
			}
		}

		if (message.length() > 0) {
			showBrandedDialog(message, getSafeContext().getString(R.string.ok), null, null);
			return;
		}

		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(
				new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_EMAIL_GENDER_DOB_NEXT_CLICKED)
		);
	}


	private void onClickedClearEmail() {
		imgClearCircle.setVisibility(View.GONE);

		editEmail.setText("");
		editEmail.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_edittext_lightgray));
		editEmail.requestFocus();

		llEmailError.setVisibility(View.GONE);
	}


	private TextWatcher emailTextWatcher = new TextWatcher() {
		String sTextBeforeBeingChanged = "";

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			sTextBeforeBeingChanged = String.valueOf(s);
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			moveToEditingEmail(s.toString());
			checkEmailAvailability(editEmail.getText().toString());
		}

		@Override
		public void afterTextChanged(Editable s) {}
	};


	private void moveToEditingEmail(String email) {
		if (!email.equals(sLastCheckedEmail)) {
			editEmail.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_edittext_lightgray));
			llEmailError.setVisibility(View.GONE);
		}

		if (TextUtils.isEmpty(email)) {
			imgClearCircle.setVisibility(View.GONE);
		} else {
			imgClearCircle.setVisibility(View.VISIBLE);
		}
	}


	private void checkEmailAvailability(String email) {
		KPHUserData parentData = KPHUserService.sharedInstance().getUserData();
		if (parentData != null && email.equalsIgnoreCase(parentData.getEmail())) {
			llEmailError.setVisibility(View.GONE);
			return;
		}

		if (email.length() == 0) {
			showEmailAvailabilityText(getSafeContext().getString(R.string.email_required), false);
			return;
		}

		// Remove spaces at the prefix and suffix
		if (!email.equals(email.trim())) {
			email = email.trim();
			editEmail.setText(email);
		}

		if (!KPHUtils.sharedInstance().isEmailValid(email)) {
			showEmailAvailabilityText(getSafeContext().getString(R.string.email_syntax_invalid), false);
			return;
		}

		// Check if the email was already checked
		if (!TextUtils.isEmpty(sLastCheckedEmail) && email.equals(sLastCheckedEmail))
			return;
		else
			sLastCheckedEmail = email;

		bIsEmailValid = false;

		// Move to validating state
		RestService.get().isEmailAvailable(email, new RestCallback<KPHUserEmailAvailability>() {
			@Override
			public void success(KPHUserEmailAvailability availability, Response response) {
				Boolean available = availability.available;

				if (available) {
					showEmailAvailabilityText("", true);
					bIsEmailValid = true;
				} else {
					showEmailAvailabilityText("", false);
				}
			}

			@Override
			public void failure(RestError restError) {
				showEmailAvailabilityText(getSafeContext().getString(R.string.default_error), false);
			}
		});
	}


	private void showEmailAvailabilityText(String text, boolean isAvailable) {
		imgClearCircle.setVisibility(View.VISIBLE);
		if (isAvailable) {
			llEmailError.setVisibility(View.GONE);
		} else {
			llEmailError.setVisibility(View.VISIBLE);

			txtEmailErrorMark.setText(R.string.icon_string_close);
			if (text.length() == 0) {
				txtEmailError.setText(createSpannableString());
				txtEmailError.setMovementMethod(LinkMovementMethod.getInstance());
				txtEmailError.setHighlightColor(Color.TRANSPARENT);
			} else {
				txtEmailError.setText(text);
			}

			editEmail.setBackground(UIManager.sharedInstance().getDrawable(R.drawable.kph_drawable_check_failed));
		}
	}


	private SpannableString createSpannableString() {
		SpannableString result = new SpannableString(getSafeContext().getResources().getString(R.string.account_email_exist));
		ClickableSpan loginSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onClickedLogin();
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setColor(txtEmailError.getCurrentTextColor());
			}
		};

		result.setSpan(
				loginSpan,
				result.length() - 8,
				result.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
		);


		return result;
	}


	private void checkDOBAvailability(String dob) {
		if (dob.length() == 0) {
			showBirthdayAvailabilityText("", false);
		} else {
			showBirthdayAvailabilityText("", true);
		}
	}

	private void showBirthdayAvailabilityText(String text, boolean availability) {
		if (availability) {
			editBirthday.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_edittext_lightgray));
			llBirthError.setVisibility(View.GONE);
		} else {
			editBirthday.setBackground(UIManager.sharedInstance().getDrawable(R.drawable.kph_drawable_check_failed));
			llBirthError.setVisibility(View.VISIBLE);
		}
	}


	private void onClickedLogin() {
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(
				new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_LOGIN_CLICKED)
		);
	}
}
