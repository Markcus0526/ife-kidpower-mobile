package org.unicefkidpower.kid_power.View.Activities.Main.Passport;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.Model.Structure.KPHMissionInformation;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.CustomControls.KPHVideoLayout;
import org.unicefkidpower.kid_power.View.CustomControls.KPHVideoView;
import org.unicefkidpower.kid_power.View.Super.SuperInfoFragment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Dayong Li on 11/11/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class InfoMissionFragment extends SuperInfoFragment {
	private KPHMissionInformation		mMissionInformation		= null;

	private View						contentView				= null;
	private ImageView					ivDelight				= null;
	private KPHTextView					tvName					= null;
	private KPHTextView					tvDescription			= null;

	// for video view
	private RelativeLayout				rlVideoView				= null;
	private KPHVideoLayout				videoMission			= null;
	private ImageView					ivVideoPlaceholder		= null;
	private ImageButton					btnPlay					= null;

	private boolean						isFinishedMission		= false;
	private String						videoURL				= "";


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		if (mMissionInformation == null) {
			return contentView;
		}

		tvName = (KPHTextView) contentView.findViewById(R.id.tvMissionName);
		tvDescription = (KPHTextView) contentView.findViewById(R.id.tvDescription);

		if (!TextUtils.isEmpty(mMissionInformation.name())) {
			tvName.setText(mMissionInformation.name());
		}

		if (isFinishedMission && !TextUtils.isEmpty(mMissionInformation.missionCompleteText())) {
			tvDescription.setText(mMissionInformation.missionCompleteText());
		} else if (!isFinishedMission && !TextUtils.isEmpty(mMissionInformation.description())) {
			String missionDescription = mMissionInformation.description();
			//Make the font of "Star Wars Rebels" copy italic.
			missionDescription = missionDescription.replace("Star Wars Rebels", "<i>Star Wars Rebels</i>");
			tvDescription.setText(Html.fromHtml(missionDescription));
		}

		ivDelight = (ImageView) contentView.findViewById(R.id.ivDelight);
		rlVideoView = (RelativeLayout) contentView.findViewById(R.id.rlVideoView);

		if (videoURL == null || videoURL.isEmpty()) {
			Drawable drawable = mMissionInformation.getCompleteDrawable();
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

			drawable = mMissionInformation.getVideoDrawable();
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


	public void setMissionInformation(KPHMissionInformation missionInformation, boolean isFinished) {
		this.mMissionInformation = missionInformation;
		this.isFinishedMission = isFinished;

		if (isFinished)
			videoURL = mMissionInformation.completeVideoURL();
		else
			videoURL = mMissionInformation.introVideoURL();
	}


	private void onVideoPlayButtonClicked() {
		if (videoMission != null) {
			rlVideoView.removeView(videoMission);
			videoMission = null;

			MainActivity.currentPlayingVideoLayout = null;
		}

		videoMission = new KPHVideoLayout(getActivity());
		videoMission.setActivity(getParentActivity());
		try {
			videoMission.setVideoURI(Uri.parse(videoURL));

			//Flurry - Track Video Play Event
			Map<String, String> params = new HashMap<>();
			params.put("videoType", "Mission Finish");
			if (mMissionInformation != null) {
				params.put("videoName", mMissionInformation.name());
			}
			params.put("videoURL", videoURL);
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
