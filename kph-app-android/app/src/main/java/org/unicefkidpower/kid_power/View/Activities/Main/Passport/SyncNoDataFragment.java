package org.unicefkidpower.kid_power.View.Activities.Main.Passport;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.R;

/**
 * Created by Ruifeng Shi on 2/9/2017.
 */

public class SyncNoDataFragment extends SuperFragment {
	private String				username			= "";
	private String				avatarId			= "";

	private KPHTextView			usernameTextView	= null;
	private ImageView			avatarImageView		= null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		initControls(rootView);

		return rootView;
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_sync_no_data;
	}


	private void initControls(View rootView) {
		// Set username
		usernameTextView = (KPHTextView)rootView.findViewById(R.id.username_text);
		if (username.length() == 0)
			usernameTextView.setText(R.string.synced);
		else
			usernameTextView.setText(String.format(getSafeContext().getString(R.string.percent_s_synced), username));


		// Set avatar
		avatarImageView = (ImageView)rootView.findViewById(R.id.avatar_imageview);
		Drawable avatarDrawable = KPHUserService.sharedInstance().getAvatarDrawable(avatarId);
		if (avatarDrawable != null)
			avatarImageView.setImageDrawable(avatarDrawable);
		else
			avatarImageView.setImageResource(R.drawable.avatar_placeholder);


		// Initialize text links
		final KPHTextView readMoreTextView = (KPHTextView)rootView.findViewById(R.id.read_more_text);

		String readMorePart = getSafeContext().getString(R.string.read_more);
		ClickableSpan readMoreSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onClickedReadMore();
			}
			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setColor(readMoreTextView.getCurrentTextColor());
			}
		};

		SpannableString ss = new SpannableString(getSafeContext().getString(R.string.sync_googlefit_not_seem_right_read_more));
		ss.setSpan(
				readMoreSpan,
				ss.toString().toLowerCase().indexOf(readMorePart.toLowerCase()),
				ss.toString().toLowerCase().indexOf(readMorePart.toLowerCase()) + readMorePart.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
		);
		readMoreTextView.setText(ss);
		readMoreTextView.setMovementMethod(LinkMovementMethod.getInstance());
		readMoreTextView.setHighlightColor(Color.TRANSPARENT);


		final KPHTextView passportTextView = (KPHTextView)rootView.findViewById(R.id.text_link);
		ClickableSpan passportSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onClickedPassport();
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setColor(passportTextView.getCurrentTextColor());
			}
		};

		SpannableString passportSS = new SpannableString(getSafeContext().getString(R.string.my_passport));
		passportSS.setSpan(passportSpan, 0, passportSS.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		passportTextView.setText(passportSS);
		passportTextView.setMovementMethod(LinkMovementMethod.getInstance());
		passportTextView.setHighlightColor(Color.TRANSPARENT);


		KPHButton checkButton = (KPHButton)rootView.findViewById(R.id.btn_check_step_tracking);
		checkButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedCheckStepTracking();
			}
		});
	}


	private void onClickedReadMore() {
		// Go to read more
		getParentActivity().showDialogFragment(new SyncNoDataGuideFragment());
	}

	private void onClickedPassport() {
		getParentActivity().onClickedBackSystemButton();
		if (getParentActivity() instanceof MainActivity)
			((MainActivity)getParentActivity()).onSelectedPassportTab();
	}

	private void onClickedCheckStepTracking() {
		// Go to settings page
		getParentActivity().onClickedBackSystemButton();
	}


	public void setData(String username, String avatarId) {
		this.username = username;
		this.avatarId = avatarId;
	}

}
