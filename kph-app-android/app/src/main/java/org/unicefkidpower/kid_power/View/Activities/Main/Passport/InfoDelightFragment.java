package org.unicefkidpower.kid_power.View.Activities.Main.Passport;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.CustomControls.KPHVideoLayout;
import org.unicefkidpower.kid_power.View.CustomControls.KPHVideoView;
import org.unicefkidpower.kid_power.Model.Structure.KPHDelightInformation;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.Super.SuperInfoFragment;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.Misc.UIManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dayong Li on 11/11/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class InfoDelightFragment extends SuperInfoFragment {
	private KPHDelightInformation		delightInformation		= null;
	private Drawable					presetDrawable			= null;

	private View						contentView				= null;
	private ImageView					ivDelight				= null;
	private KPHTextView					tvName					= null;
	private KPHTextView					tvDescription			= null;

	// for video view
	private RelativeLayout				rlVideoView				= null;
	private KPHVideoLayout				videoMission			= null;
	private ImageView					ivVideoPlaceholder		= null;
	private ImageButton					btnPlay					= null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		if (delightInformation == null)
			return contentView;

		tvName = (KPHTextView) contentView.findViewById(R.id.tvMissionName);
		tvDescription = (KPHTextView) contentView.findViewById(R.id.tvDescription);

		if (delightInformation.getName() != null)
			tvName.setText(delightInformation.getName());

		if (delightInformation.getDescription() != null) {
			// Make the font of "Star Wars: Force for Change" copy italic.
			String delightDescription = delightInformation.getDescription();
			delightDescription = delightDescription.replace("Star Wars: Force for Change ", "<i>Star Wars: Force for Change</i> ");
			tvDescription.setText(Html.fromHtml(delightDescription));
		}

		ivDelight = (ImageView) contentView.findViewById(R.id.ivDelight);
		rlVideoView = (RelativeLayout) contentView.findViewById(R.id.rlVideoView);

		if (delightInformation.getVideoURL() == null || delightInformation.getVideoURL().isEmpty()) {
			Drawable drawable;

			if (presetDrawable != null) {
				drawable = presetDrawable;
			} else {
				drawable = delightInformation.getImageDrawable();
			}

			if (drawable != null)
				ivDelight.setImageDrawable(drawable);

			ivDelight.setVisibility(View.VISIBLE);
			rlVideoView.setVisibility(View.GONE);
		} else {
			Drawable drawable;

			// for video view
			btnPlay = (ImageButton) contentView.findViewById(R.id.btnPlay);
			btnPlay.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onVideoPlayButtonClicked();
				}
			});

			ivVideoPlaceholder = (ImageView) contentView.findViewById(R.id.ivVideoPlaceholder);

			drawable = UIManager.sharedInstance().getImageDrawable(R.drawable.video_generic_black_overlay);
			if (drawable != null) {
				ivVideoPlaceholder.setImageDrawable(drawable);
				//Calculates the height of placeholder image
				float fPlaceHolderHeight = ((float) ((BitmapDrawable) drawable).getBitmap().getHeight() /
						(float) ((BitmapDrawable) drawable).getBitmap().getWidth()) *
						(ResolutionSet.getScreenSize(getSafeContext(), false).x);
				ViewGroup.LayoutParams lpVideoView = rlVideoView.getLayoutParams();
				lpVideoView.height = (int) fPlaceHolderHeight;
				rlVideoView.setLayoutParams(lpVideoView);
			}

			ivDelight.setVisibility(View.GONE);
			rlVideoView.setVisibility(View.VISIBLE);
		}

		return contentView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_info_delight;
	}

	@Override
	public void onClickedBackSystemButton() {
		if (videoMission != null && videoMission.isFullscreen()) {
			videoMission.exitFullScreen();
			MainActivity.currentPlayingVideoLayout = null;
			return;
		}

		super.onClickedBackSystemButton();
	}


	public void setDelightInformation(KPHDelightInformation delightInformation, Drawable drawable) {
		this.delightInformation = delightInformation;
		this.presetDrawable = drawable;
	}


	private void onVideoPlayButtonClicked() {
		if (videoMission != null) {
			rlVideoView.removeView(videoMission);
			MainActivity.currentPlayingVideoLayout = null;
			videoMission = null;
		}

		videoMission = new KPHVideoLayout(getActivity());
		videoMission.setActivity(getParentActivity());

		try {
			videoMission.setVideoURI(Uri.parse(delightInformation.getVideoURL()));

			//Flurry - Track Video Play Event
			Map<String, String> params = new HashMap<>();
			params.put("videoType", "Delight");
			if (delightInformation != null) {
				params.put("videoName", delightInformation.getName());
			}
			params.put("videoURL", delightInformation.getVideoURL());
		} catch (IOException e) {
			e.printStackTrace();
		}

		ivVideoPlaceholder.setVisibility(View.INVISIBLE);
		btnPlay.setVisibility(View.INVISIBLE);
		videoMission.hideControls();

		if (videoMission.getCurrentState() == KPHVideoView.State.PREPARED) {
			videoMission.start();
		} else {
			videoMission.setShouldAutoplay(true);
		}

		videoMission.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				videoMission.exitFullScreen();
				videoMission.hideControls();

				ivVideoPlaceholder.setVisibility(View.VISIBLE);
				btnPlay.setVisibility(View.VISIBLE);
				ivVideoPlaceholder.bringToFront();
				btnPlay.bringToFront();

				MainActivity.currentPlayingVideoLayout = null;
			}
		});

		videoMission.setLayoutParams(
				new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT
				)
		);

		rlVideoView.addView(videoMission);
		MainActivity.currentPlayingVideoLayout = videoMission;
	}

}
