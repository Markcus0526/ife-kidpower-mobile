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
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.StringHelper;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserNameAvailability;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHEditText;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;

import retrofit.client.Response;

/**
 * Created by Ruifeng Shi on 12/22/2016.
 */
public class CreateParentAccountUsernamePasswordFragment extends CreateParentAccountSuperFragment {
	public KPHEditText				editUsername			= null;
	public KPHEditText				editPassword			= null;

	private LinearLayout			llAvailability			= null;
	private KPHTextView				txtAvailability			= null;
	private KPHTextView				txtAvailabilitySign		= null;
	private ImageView				ivClearCircle			= null;
	private RotateAnimation			rotateAnimation			= null;

	private String					sLastCheckedUsername	= "";
	private boolean					isUsernameValid			= false;
	private boolean					isFacebook				= false;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View vwRoot = super.onCreateView(inflater, container, savedInstanceState);

		editUsername = (KPHEditText) vwRoot.findViewById(R.id.edit_username);
		KPHUserData parentData = KPHUserService.sharedInstance().getUserData();
		if (parentData != null) {
			editUsername.setText(parentData.getHandle());
		}

		editPassword = (KPHEditText) vwRoot.findViewById(R.id.edit_password);
		editPassword.setText(KPHUserService.sharedInstance().loadUserPassword());

		llAvailability = (LinearLayout) vwRoot.findViewById(R.id.layout_dob_error);
		llAvailability.setVisibility(View.GONE);

		txtAvailability = (KPHTextView) vwRoot.findViewById(R.id.txt_availability);
		txtAvailabilitySign = (KPHTextView) vwRoot.findViewById(R.id.txt_availability_sign);

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

		editUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					String username = ((EditText) v).getText().toString();
					checkUserNameAvailability(username);
				}
			}
		});
		editUsername.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
		editUsername.addTextChangedListener(usernameTextWatcher);

		ivClearCircle = (ImageView) vwRoot.findViewById(R.id.ivClearCircle);
		ivClearCircle.setVisibility(View.GONE);
		ivClearCircle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ivClearCircle.setVisibility(View.GONE);
				editUsername.setText("");
				editUsername.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_edittext_lightgray));
				editUsername.requestFocus();

				llAvailability.setVisibility(View.GONE);
			}
		});

		rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotateAnimation.setInterpolator(new LinearInterpolator());
		rotateAnimation.setDuration(2000);
		rotateAnimation.setRepeatCount(Animation.INFINITE);
		rotateAnimation.setFillEnabled(true);
		rotateAnimation.setFillAfter(true);

		KPHUtils.sharedInstance().hideKeyboardInView(vwRoot);

		return vwRoot;
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_create_parent_account_username_password;
	}

	private void checkUserNameAvailability(String username) {
		KPHUserData parentData = KPHUserService.sharedInstance().getUserData();
		if (parentData != null && username.equalsIgnoreCase(parentData.getHandle())) {
			llAvailability.setVisibility(View.GONE);
			editUsername.setBackground(UIManager.sharedInstance().getDrawable(R.drawable.kph_drawable_check_success));
			return;
		}

		if (username.length() < KPHConstants.USERNAME_MIN_VALID_LENGTH) {
			llAvailability.setVisibility(View.GONE);
			editUsername.setBackground(UIManager.sharedInstance().getDrawable(R.drawable.kph_drawable_check_success));
			return;
		}

		// Remove spaces at the prefix and suffix
		if (!username.equals(username.trim())) {
			username = username.trim();
			editUsername.setText(username);
		}

		// Check if the username was already checked
		if (!TextUtils.isEmpty(sLastCheckedUsername) && username.equals(sLastCheckedUsername))
			return;

		if (StringHelper.isContainingSpecialCharacters(username)) {
			showUsernameAvailabilityText(getSafeContext().getString(R.string.username_invalid), false);
			return;
		}

		isUsernameValid = false;

		final String lastCheckUserName = username;
		// Move to validating state
		moveToInCheckingUsernameProcess();
		RestService.get().isHandleAvailable(username, new RestCallback<KPHUserNameAvailability>() {
			@Override
			public void success(KPHUserNameAvailability availability, Response response) {
				Boolean available = availability.available;

				if (available) {
					showUsernameAvailabilityText(
							getSafeContext().getString(R.string.cool_name),
							true
					);
					isUsernameValid = true;
				} else {
					showUsernameAvailabilityText("", false);
				}

				sLastCheckedUsername = lastCheckUserName;
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


	private void moveToInCheckingUsernameProcess() {}


	private void showUsernameAvailabilityText(CharSequence text, boolean isAvailable) {
		ivClearCircle.setVisibility(View.VISIBLE);
		llAvailability.setVisibility(View.VISIBLE);
		if (isAvailable) {
			txtAvailabilitySign.setText(R.string.icon_string_check);
			if (text.length() == 0)
				txtAvailability.setText(R.string.cool_name);
			else
				txtAvailability.setText(text);

			editUsername.setBackground(UIManager.sharedInstance().getDrawable(R.drawable.kph_drawable_check_success));
		} else {
			txtAvailabilitySign.setText(R.string.icon_string_close);
			if (text.length() == 0) {
				txtAvailability.setText(createSpannableString());
				txtAvailability.setMovementMethod(LinkMovementMethod.getInstance());
				txtAvailability.setHighlightColor(Color.TRANSPARENT);
			} else {
				txtAvailability.setText(text);
			}

			editUsername.setBackground(UIManager.sharedInstance().getDrawable(R.drawable.kph_drawable_check_failed));
		}
	}


	private SpannableString createSpannableString() {
		SpannableString result = new SpannableString(getSafeContext().getString(R.string.account_name_exist));
		String loginPart = getSafeContext().getString(R.string.log_in);
		ClickableSpan loginSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onClickedLogin();
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setColor(txtAvailability.getCurrentTextColor());
			}
		};

		result.setSpan(
				loginSpan,
				result.toString().indexOf(loginPart),
				result.toString().indexOf(loginPart) + loginPart.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
		);


		return result;
	}


	private void onClickedLogin() {
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(
				new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_LOGIN_CLICKED)
		);
	}


	private void onClickedBack() {
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(
				new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_BACK_CLICKED)
		);
	}

	private void onClickedNext() {
		String message = "";
		KPHUserData parentData = KPHUserService.sharedInstance().getUserData();
		if (parentData != null && editUsername.getText().toString().equalsIgnoreCase(parentData.getHandle())) {
			isUsernameValid = true;
		}

		if (editUsername.length() == 0 && editPassword.length() == 0 && !isFacebook) {
			message = getSafeContext().getString(R.string.dialog_username_password_empty);
		} else if (editUsername.length() == 0) {
			message = getSafeContext().getString(R.string.username_required);
		} else if (editUsername.length() < KPHConstants.USERNAME_MIN_VALID_LENGTH) {
			message = getSafeContext().getString(R.string.username_at_least_3_chars);
		} else if (!isUsernameValid && editPassword.length() == 0 && !isFacebook) {
			message = getSafeContext().getString(R.string.dialog_username_password_invalid);
		} else if (isUsernameValid && editPassword.length() == 0 && !isFacebook) {
			message = getSafeContext().getString(R.string.dialog_password_blank);
		} else if (StringHelper.isContainingSpecialCharacters(editUsername.getText().toString())) {
			message = getSafeContext().getString(R.string.username_invalid);
		} else if (!isUsernameValid) {
			if (sLastCheckedUsername.equalsIgnoreCase(editUsername.getText().toString())) {
				message = getSafeContext().getString(R.string.account_name_exist);
			} else {
				showProgressDialog();
				RestService.get().isHandleAvailable(editUsername.getText().toString(), new RestCallback<KPHUserNameAvailability>() {
					@Override
					public void failure(RestError restError) {
						dismissProgressDialog();
						showBrandedDialog(getSafeContext().getString(R.string.account_name_exist), getString(R.string.ok), null, null);
					}

					@Override
					public void success(KPHUserNameAvailability kphUserNameAvailability, Response response) {
						dismissProgressDialog();
						sLastCheckedUsername = editUsername.getText().toString();

						if (kphUserNameAvailability.available) {
							confirmUserName();
						} else {
							showBrandedDialog(getSafeContext().getString(R.string.account_name_exist), getString(R.string.ok), null, null);
						}
					}
				});
				return;
			}
		} else if (!isFacebook && editPassword.length() < KPHConstants.PASSWORD_MIN_LENGTH) {
			message = getSafeContext().getString(R.string.password_invalid);
		}

		if (message.length() > 0) {
			showBrandedDialog(message, getString(R.string.ok), null, null);
			return;
		}

		confirmUserName();
	}

	private void confirmUserName() {
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(
				new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PARENT_ACCOUNT_USERNAME_PASSWORD_NEXT_CLICKED)
		);
	}


	TextWatcher usernameTextWatcher = new TextWatcher() {
		String sTextBeforeBeingChanged = "";

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			sTextBeforeBeingChanged = String.valueOf(s);
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s.length() > KPHConstants.USERNAME_MAX_LENGTH ||
					StringHelper.isContainingSpecialCharacters(s.toString())) {
				editUsername.setText(sTextBeforeBeingChanged);
				editUsername.setSelection(editUsername.getText().length());
			}

			moveToEditing(s.toString());

			checkUserNameAvailability(editUsername.getText().toString());
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	private void moveToEditing(String str) {
		if (!str.equals(sLastCheckedUsername)) {
			editUsername.setBackgroundColor(UIManager.sharedInstance().getColor(R.color.kph_color_edittext_lightgray));
			txtAvailabilitySign.setText("");
			txtAvailability.setText("");
		}

		if (TextUtils.isEmpty(str)) {
			ivClearCircle.setVisibility(View.GONE);
		} else {
			ivClearCircle.setVisibility(View.VISIBLE);
		}
	}

	public void setData(boolean isFacebook) {
		this.isFacebook = isFacebook;

		if (isFacebook) {
			editPassword.setEnabled(false);
		} else {
			editPassword.setEnabled(true);
		}
	}


}
