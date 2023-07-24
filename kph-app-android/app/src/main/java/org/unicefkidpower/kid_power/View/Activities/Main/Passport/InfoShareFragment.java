package org.unicefkidpower.kid_power.View.Activities.Main.Passport;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.Misc.SharingHelper;
import org.unicefkidpower.kid_power.Model.MicroService.KPHMissionService;
import org.unicefkidpower.kid_power.Model.Structure.KPHDelightInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHMissionInformation;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperInfoFragment;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Dayong Li on 11/11/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class InfoShareFragment extends SuperInfoFragment {
	private final int				request_code			= 1256;
	private final String			TAG						= "SharingFragment";

	private KPHDelightInformation	mDelightInformation		= null;

	private View					contentView				= null;
	private ImageView				ivBackground			= null;
	private ImageView				ivShare					= null;
	private KPHButton				btnShare				= null;
	private KPHTextView				tvName					= null;
	private KPHTextView				tvDescription			= null;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		if (mDelightInformation == null)
			return contentView;

		ivShare = (ImageView) contentView.findViewById(R.id.ivShareMission);

		btnShare = (KPHButton) contentView.findViewById(R.id.btnShare);
		btnShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedShare();
			}
		});

		if (!mDelightInformation.getType().equals(KPHConstants.DELIGHT_POSTCARD)) {
			btnShare.setVisibility(View.GONE);
		} else {
			ivBackground = (ImageView) contentView.findViewById(R.id.ivBackground);
		}

		if (ivBackground != null) {
			ViewTreeObserver vto = ivBackground.getViewTreeObserver();
			vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				public boolean onPreDraw() {
					ivBackground.getViewTreeObserver().removeOnPreDrawListener(this);

					Point screenSize = ResolutionSet.getScreenSize(getSafeContext(), true);
					screenSize.y -= ResolutionSet.getStatusBarHeight(getSafeContext());

					int width = screenSize.x;
					int height = screenSize.y;

					Bitmap bitmap = makeGradientRect(width, height, mDelightInformation.getBgTopColor(), mDelightInformation.getBgBottomColor());
					if (bitmap != null)
						ivBackground.setImageDrawable(new BitmapDrawable(bitmap));

					return true;
				}
			});
		}

		tvName = (KPHTextView) contentView.findViewById(R.id.tvMissionName);
		tvDescription = (KPHTextView) contentView.findViewById(R.id.tvDescription);

		Drawable drawable = mDelightInformation.getDetailImageDrawable();
		if (drawable != null)
			ivShare.setImageDrawable(drawable);

		if (mDelightInformation.getName() != null)
			tvName.setText(mDelightInformation.getName());

		if (mDelightInformation.getDescription() != null)
			setLinkIfAvailable(mDelightInformation.getDescription());

		initComponents();

		return contentView;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == request_code) {
			if (resultCode == RESULT_OK) {
				Logger.log(TAG, "onActivityResult : sharing OK");
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void setLinkIfAvailable(String description) {
		SpannableString sContent = new SpannableString(description);
		String link;
		ClickableSpan clickableSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				if (getParentActivity() == null)
					return;

				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(KPHConstants.URL_UNICEFKIDPOWER_ORG));
				getParentActivity().startActivity(intent);
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
			}
		};

		link = "www.unicefkidpower.org";
		if (!description.contains(link)) {
			link = "unicefkidpower.org";
		}

		int pos = description.indexOf(link);
		if (pos > 0) {
			sContent.setSpan(
					clickableSpan,
					pos,
					pos + link.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
			);
		}

		tvDescription.setText(sContent);
		tvDescription.setMovementMethod(LinkMovementMethod.getInstance());
		tvDescription.setHighlightColor(Color.TRANSPARENT);
	}

	public void initComponents() {
		//Initialize facebook sharing stuff
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_info_share;
	}

	public void setDelightInformation(KPHDelightInformation delightInformation) {
		this.mDelightInformation = delightInformation;
	}

	private Bitmap makeGradientRect(int width, int height, int beginColor, int endColor) {
		if (width == 0 || height == 0)
			return null;

		if (beginColor == 0 && endColor == 0)
			return null;

		Shader shader = new LinearGradient(0, 0, 1, height, beginColor, endColor, Shader.TileMode.CLAMP);
		Paint p = new Paint();
		p.setDither(true);
		p.setShader(shader);

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bitmap);
		c.drawRect(new RectF(0, 0, width, height), p);

		return bitmap;
	}

	private void onClickedShare() {
		if (!mDelightInformation.getType().equals(KPHConstants.DELIGHT_POSTCARD))
			return;

		final SharingHelper.ShareBundle shareBundle = new SharingHelper.ShareBundle();

		shareBundle.requestCode = request_code;
		shareBundle.content = mDelightInformation.getDescription();
		shareBundle.twitter_content = mDelightInformation.getTwitter_description();
		shareBundle.subject = mDelightInformation.getName();
		shareBundle.assetImage = mDelightInformation.imageForSharing();
		shareBundle.url = KPHConstants.SHARING_URL;
		shareBundle.title = getSafeContext().getString(R.string.sharing_postcard);

		Map<String, String> params = new HashMap<>();
		params.put("delightType", mDelightInformation.getType());
		params.put("delightName", mDelightInformation.getName());

		KPHMissionInformation missionInformation = KPHMissionService.sharedInstance().getMissionInformationById(mDelightInformation.getMissionId());
		if (missionInformation != null) {
			params.put("delightMission", missionInformation.name());
		}

		SharingHelper.share(getParentActivity(), shareBundle);
	}

}
