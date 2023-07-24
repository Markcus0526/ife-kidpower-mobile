package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.StringHelper;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserNameAvailability;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.FontView;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHEditText;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

import retrofit.client.Response;

/**
 * Created by Dayong on 12/4/2016.
 */
public class OnboardingUserNameFragment extends SuperFragment {
	// UI Elements
	private View				contentView				= null;
	private KPHEditText			editUsername			= null;
	private TextView			txtAvailability			= null;
	private FontView			fvMark					= null;

	private View				btnBack					= null;
	private KPHButton			btnNext					= null;
	private ImageView			ivClearCircle			= null;
	private ImageView			ivRotateCircle			= null;
	private RotateAnimation		rotateAnimation			= null;
	// End of 'UI Elements'

	// Variables
	private String				sUsername				= "";
	private String				sLastCheckedUsername	= "";

	private boolean				bIsUsernameValid		= false;
	// End of 'Variables'


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		ivClearCircle = (ImageView) contentView.findViewById(R.id.ivClearCircle);
		ivClearCircle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ivClearCircle.setVisibility(View.GONE);
				editUsername.setText("");

				fvMark.setText("");
				txtAvailability.setText("");
			}
		});

		ivRotateCircle = (ImageView) contentView.findViewById(R.id.ivRotateCircle);


		rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotateAnimation.setInterpolator(new LinearInterpolator());
		rotateAnimation.setDuration(2000);
		rotateAnimation.setRepeatCount(Animation.INFINITE);
		rotateAnimation.setFillEnabled(true);
		rotateAnimation.setFillAfter(true);


		ivRotateCircle.setVisibility(View.GONE);
		ivClearCircle.setVisibility(View.GONE);


		// Setting user name
		editUsername = (KPHEditText) contentView.findViewById(R.id.edit_username);
		editUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					String username = ((EditText) v).getText().toString();
					checkUserNameAvailability(username);
				}
			}
		});
		editUsername.setOnKeyListener(new KPHEditText.onKeyListener() {
			@Override
			public void onKeyDownEnter() {
				onClickedNext();
			}
		});
		editUsername.addTextChangedListener(new TextWatcher() {
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
		});
		// End of 'Setting user name'


		txtAvailability = (TextView) contentView.findViewById(R.id.tvUserNameAvailability);
		fvMark = (FontView) contentView.findViewById(R.id.fvAvailabilityMark);
		txtAvailability.setText("");
		fvMark.setText("");

		btnBack = contentView.findViewById(R.id.btnBack);

		btnNext = (KPHButton) contentView.findViewById(R.id.btn_next);
		btnNext.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickedNext();
					}
				}
		);

		final KPHTextView txtGetBand = (KPHTextView) contentView.findViewById(R.id.txtGetBand);

		// Get One band
		SpannableString sContent;
		ClickableSpan gotoTarget = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				// go to Target URL
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(KPHConstants.GET_KIDPOWER_BAND_URL));
				getParentActivity().startActivity(intent);
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setColor(txtGetBand.getCurrentTextColor());
			}
		};

		String content = getSafeContext().getString(R.string.do_not_have_a_band);
		String strUnderline = getSafeContext().getString(R.string.get_one_band);
		sContent = new SpannableString(content);

		// Learn More Underline
		sContent.setSpan(gotoTarget, content.indexOf(strUnderline),
				content.indexOf(strUnderline) + strUnderline.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		txtGetBand.setText(sContent);
		txtGetBand.setMovementMethod(LinkMovementMethod.getInstance());
		txtGetBand.setHighlightColor(Color.TRANSPARENT);

		editUsername.setText(sUsername);
		refreshBackButton();

		return contentView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_onboarding_username;
	}

	private void moveToEditing(String str) {
		if (!str.equals(sLastCheckedUsername)) {
			editUsername.setBackground(
					UIManager.sharedInstance().getDrawable(R.drawable.kph_drawable_check_default)
			);
			fvMark.setText("");
			txtAvailability.setText("");
		}

		if (TextUtils.isEmpty(str)) {
			ivClearCircle.setVisibility(View.GONE);
		} else {
			ivClearCircle.setVisibility(View.VISIBLE);
		}
	}

	private void moveToInCheckingUsernameProcess() {
		fvMark.setText("");
		txtAvailability.setText("");
		ivClearCircle.setVisibility(View.GONE);
		ivRotateCircle.startAnimation(rotateAnimation);
		ivRotateCircle.setVisibility(View.VISIBLE);
	}

	private void onChecked(int nAvailable) {
		String sAvailability;

		ivRotateCircle.setVisibility(View.GONE);
		ivRotateCircle.clearAnimation();

		ivClearCircle.setVisibility(View.VISIBLE);

		if (nAvailable == 1) {
			// available
			editUsername.setBackground(
					UIManager.sharedInstance().getDrawable(R.drawable.kph_drawable_check_success)
			);
			fvMark.setText(R.string.icon_string_check);

			sAvailability = getSafeContext().getString(R.string.cool_name);
			txtAvailability.setText(sAvailability);
		} else if (nAvailable == -1) {
			// already taken
			editUsername.setBackground(
					UIManager.sharedInstance().getDrawable(R.drawable.kph_drawable_check_failed)
			);
			fvMark.setText(R.string.icon_string_close);

			SpannableString sContent;
			ClickableSpan gotoLogin = new ClickableSpan() {
				@Override
				public void onClick(View widget) {
					// go to Target URL
					getParentActivity().showDialogFragment(new LoginFragment());
				}

				@Override
				public void updateDrawState(TextPaint ds) {
					super.updateDrawState(ds);
					//ds.setUnderlineText(false);
				}
			};

			sAvailability = getSafeContext().getString(R.string.thats_taken);
			String strUnderline = "Log in";
			sContent = new SpannableString(sAvailability);
			// Learn More Underline
			sContent.setSpan(gotoLogin, sAvailability.indexOf(strUnderline),
					sAvailability.indexOf(strUnderline) + strUnderline.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

			txtAvailability.setText(sContent);
			txtAvailability.setMovementMethod(LinkMovementMethod.getInstance());
			txtAvailability.setHighlightColor(Color.TRANSPARENT);
		} else {
			// other case, checking error
			editUsername.setTextColor(
					UIManager.sharedInstance().getColor(R.color.kph_color_light_red)
			);
			fvMark.setText(R.string.icon_string_close);

			sAvailability = getSafeContext().getString(R.string.network_error_lost_network_connection);
			txtAvailability.setText(sAvailability);
		}
	}

	public void checkUserNameAvailability(String username) {
		if (getParentActivity() == null)
			return;

		if (username.length() < KPHConstants.USERNAME_MIN_VALID_LENGTH) {
			txtAvailability.setText("");
			fvMark.setText("");
			editUsername.setBackground(
					UIManager.sharedInstance().getDrawable(R.drawable.kph_drawable_check_default)
			);
			return;
		}

		if (isOriginalUserName())
			bIsUsernameValid = true;

		// Remove spaces at the prefix and suffix
		if (!username.equals(username.trim())) {
			username = username.trim();
			editUsername.setText(username);
			editUsername.setSelection(editUsername.getText().length());
		}

		// Check if the username was already checked
		if (username.equals(sLastCheckedUsername))
			return;


		final String tempLastCheckedUserName = username;
		bIsUsernameValid = false;

		// Move to validating state
		moveToInCheckingUsernameProcess();
		RestService.get().isHandleAvailable(
				username,
				new RestCallback<KPHUserNameAvailability>() {
					@Override
					public void success(KPHUserNameAvailability availability, Response response) {
						Boolean available = availability.available;

						if (available) {
							onChecked(1);
							bIsUsernameValid = true;
						} else {
							onChecked(-1);
						}

						sLastCheckedUsername = tempLastCheckedUserName;
					}

					@Override
					public void failure(RestError restError) {
						onChecked(0);
					}
				}
		);
	}


	private boolean isOriginalUserName() {
		if (getParentActivity() != null &&
				((OnboardingActivity) getParentActivity()).isCompletingParentProfile) {
			String orgUserName = KPHUserService.sharedInstance().getUserData().getHandle();
			if (orgUserName.trim().equalsIgnoreCase(editUsername.getText().toString()))
				return true;
		}

		return false;
	}


	private void onClickedNext() {
		if (editUsername.isFocused()) {
			editUsername.clearFocus();
		}

		if (isOriginalUserName()) {
			bIsUsernameValid = true;
			confirmUserName();
		} else {
			String message = "";
			if (editUsername.length() == 0) {
				message = getSafeContext().getString(R.string.dialog_user_name_do_not_forget);
			} else if (editUsername.length() < KPHConstants.USERNAME_MIN_VALID_LENGTH) {
				message = getSafeContext().getString(R.string.username_at_least_3_chars);
			} else if (StringHelper.isContainingSpecialCharacters(editUsername.getText().toString())) {
				message = getSafeContext().getString(R.string.username_invalid);
			} else if (!bIsUsernameValid) {
				if (sLastCheckedUsername.equalsIgnoreCase(editUsername.getText().toString())) {
					message = getSafeContext().getString(R.string.account_name_exist);
				} else {
					showProgressDialog();
					RestService.get().isHandleAvailable(editUsername.getText().toString(), new RestCallback<KPHUserNameAvailability>() {
						@Override
						public void failure(RestError restError) {
							dismissProgressDialog();
							showUsernameErrorDialog(getSafeContext().getString(R.string.account_name_exist));
						}

						@Override
						public void success(KPHUserNameAvailability kphUserNameAvailability, Response response) {
							dismissProgressDialog();
							sLastCheckedUsername = editUsername.getText().toString();

							if (kphUserNameAvailability.available) {
								confirmUserName();
							} else {
								onChecked(-1);
								showUsernameErrorDialog(getSafeContext().getString(R.string.account_name_exist));
							}
						}
					});
					return;
				}
			}

			if (message.length() > 0) {
				showUsernameErrorDialog(message);
				return;
			}

			confirmUserName();
		}
	}

	private void confirmUserName() {
		((OnboardingActivity) getParentActivity()).newUsername = editUsername.getText().toString();

		Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_CREATED_USER_NAME);
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
	}

	private void showUsernameErrorDialog(String message) {
		showBrandedDialog(message, getSafeContext().getString(R.string.ok), null, null);
		editUsername.requestFocus();
	}


	public void setData(String username) {
		this.sUsername = username;
		this.sLastCheckedUsername = sUsername;

		if (editUsername != null) {
			editUsername.setText(sUsername);
		}

		refreshBackButton();
	}


	public void refreshBackButton() {
		if (btnBack != null) {
			if (((OnboardingActivity)getParentActivity()).getShowBackButtonFlag())
				btnBack.setVisibility(View.VISIBLE);
			else
				btnBack.setVisibility(View.INVISIBLE);
		}
	}


}
