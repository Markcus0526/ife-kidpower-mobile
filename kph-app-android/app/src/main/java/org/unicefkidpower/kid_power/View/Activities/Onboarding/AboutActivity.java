package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;

import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;

/**
 * Created by Ruifeng Shi on 10/26/2015.
 */
public class AboutActivity extends SuperActivity {
	private KPHTextView				txtAboutContent			= null;
	private KPHTextView				txtPrivacyPolicy		= null;
	private KPHTextView				txtAcknowledgements		= null;
	private KPHTextView				txtAppVersion			= null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		txtAboutContent = (KPHTextView) findViewById(R.id.txt_about_content);
		txtPrivacyPolicy = (KPHTextView) findViewById(R.id.txt_privacy_policy);
		txtAcknowledgements = (KPHTextView) findViewById(R.id.txt_acknowledgements);
		txtAppVersion = (KPHTextView) findViewById(R.id.txt_app_version);
		txtAppVersion.setText(
				getString(
						R.string.app_version_number,
						KPHUtils.sharedInstance().getVersionName(AboutActivity.this),
						KPHUtils.sharedInstance().getVersionCode(AboutActivity.this)
				)
		);

		configureAboutContent();
		configurePrivacyPolicy();
		configureAcknowledgements();
	}

	private void configureAboutContent() {
		SpannableString sContent = new SpannableString(
				getString(R.string.about_unicef_kid_power_content)
		);
		ClickableSpan clickableSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(KPHConstants.URL_UNICEFKIDPOWER_ORG));
				startActivity(intent);
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				//ds.setUnderlineText(false);
			}
		};
		sContent.setSpan(
				clickableSpan,
				sContent.length() - 18,
				sContent.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
		);
		txtAboutContent.setText(sContent);
		txtAboutContent.setMovementMethod(LinkMovementMethod.getInstance());
		txtAboutContent.setHighlightColor(Color.TRANSPARENT);
	}


	private void configurePrivacyPolicy() {
		SpannableString sContent = new SpannableString(
				getString(R.string.privacy_policy)
		);
		ClickableSpan clickableSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(KPHConstants.URL_PRIVACY_POLICY));
				startActivity(intent);
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				//ds.setUnderlineText(false);
			}
		};
		sContent.setSpan(
				clickableSpan,
				0,
				sContent.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
		);
		txtPrivacyPolicy.setText(sContent);
		txtPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());
		txtPrivacyPolicy.setHighlightColor(Color.TRANSPARENT);
	}


	private void configureAcknowledgements() {
		final SpannableString sContent = new SpannableString(
				getString(R.string.acknowledgements)
		);
		ClickableSpan clickableSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onClickedAcknowledgements();
			}
		};
		sContent.setSpan(
				clickableSpan,
				0,
				sContent.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
		);
		txtAcknowledgements.setText(sContent);
		txtAcknowledgements.setMovementMethod(LinkMovementMethod.getInstance());
		txtAcknowledgements.setHighlightColor(Color.TRANSPARENT);
	}


	private void onClickedAcknowledgements() {
		pushNewActivityAnimated(AcknowledgementsActivity.class);
	}


}
