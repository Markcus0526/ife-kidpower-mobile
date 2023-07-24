package org.unicefkidpower.kid_power.View.Activities.Main.Passport;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.unicefkidpower.kid_power.Misc.AlertDialogHelper;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.Model.MicroService.KPHMissionService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.Model.Structure.KPHMissionInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHTracker;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMission;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMissionStats;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHImageTextButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.CustomControls.KPHVideoLayout;
import org.unicefkidpower.kid_power.View.CustomControls.KPHVideoView;
import org.unicefkidpower.kid_power.View.Super.SuperNormalSizeDialogFragment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit.client.Response;

/**
 * Created by Ruifeng Shi on 9/28/2015.
 */
public class MissionInfoFragment extends SuperNormalSizeDialogFragment {
	public static int					MODE_UNLOCK				= 0;
	public static int					MODE_SYNC				= 1;
	public static int					MODE_STARTED			= 2;

	private KPHUserService				userService				= null;
	private KPHMissionService			missionService			= null;

	private KPHMissionInformation		missionInformation		= null;
	private KPHUserData					userData				= null;
	private KPHUserMissionStats			status					= null;

	private View						contentView						= null;
	private RelativeLayout				videoLayout						= null;
	private KPHVideoLayout				missionVideoView				= null;
	private ImageView					videoPlaceholderImageView		= null;
	private ImageView					countryImageView				= null;
	private ImageButton					playButton, closeButton			= null;
	private KPHImageTextButton			syncToStartNowButton			= null;
	private KPHTextView					missionNameTextView				= null;
	private KPHTextView					missionDescTextView				= null;

	private IntentFilter				intentFilter					= null;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(getDialogFragmentStyle(), R.style.KidPowerDialogStyle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		missionService = KPHMissionService.sharedInstance();
		userService = KPHUserService.sharedInstance();
		userData = userService.getUserData();


		if (missionInformation == null)
			return contentView;

		videoLayout = (RelativeLayout) contentView.findViewById(R.id.rlVideoView);
		videoPlaceholderImageView = (ImageView) contentView.findViewById(R.id.ivVideoPlaceholder);

		Drawable drawable = missionInformation.getVideoDrawable();
		if (drawable != null) {
			videoPlaceholderImageView.setImageDrawable(drawable);
			// Calculates the height of placeholder image
			float fPlaceHolderHeight = ((float) ((BitmapDrawable) drawable).getBitmap().getHeight() / ((BitmapDrawable) drawable).getBitmap().getWidth()) *
					(ResolutionSet.getScreenSize(getSafeContext(), false).x);
			ViewGroup.LayoutParams lpVideoView = videoLayout.getLayoutParams();
			lpVideoView.height = (int) fPlaceHolderHeight;
			videoLayout.setLayoutParams(lpVideoView);
		}

		countryImageView = (ImageView) contentView.findViewById(R.id.ivCountry);

		drawable = missionInformation.getCountryDrawable();
		if (drawable != null)
			countryImageView.setImageDrawable(drawable);

		playButton = (ImageButton) contentView.findViewById(R.id.btnPlay);
		playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedPlayVideo();
			}
		});

		closeButton = (ImageButton) contentView.findViewById(R.id.btnClose);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		syncToStartNowButton = (KPHImageTextButton) contentView.findViewById(R.id.btnSyncToStartNow);
		syncToStartNowButton.setGravity(Gravity.CENTER);
		syncToStartNowButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedSyncToStartNow();
			}
		});

		missionNameTextView = (KPHTextView) contentView.findViewById(R.id.lblCountryName);

		// Make the font of "Star Wars: Force For Change" copy italic.
		String missionName = missionInformation.name();
		missionName = missionName.replace("Star Wars: Force For Change ", "<i>Star Wars: Force For Change</i> ");
		missionNameTextView.setText(Html.fromHtml(missionName));

		missionDescTextView = (KPHTextView) contentView.findViewById(R.id.lblMissionDescription);
		String missionDescription = missionInformation.description();
		// Make the font of "Star Wars Rebels" copy italic.
		missionDescription = missionDescription.replace("Star Wars Rebels", "<i>Star Wars Rebels</i>");
		missionDescTextView.setText(Html.fromHtml(missionDescription));

		if (intentFilter == null) {
			intentFilter = new IntentFilter();
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_CREDIT_BALANCE_UPDATED);
			LocalBroadcastManager.getInstance(getSafeContext()).registerReceiver(receiver, intentFilter);
		}

		return contentView;
	}


	@Override
	public void startAction() {
		updateState();
	}


	@Override
	public void onResume() {
		super.onResume();
		getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(android.content.DialogInterface dialog, int keyCode,
								 android.view.KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN &&
						keyCode == android.view.KeyEvent.KEYCODE_BACK) {
					if (missionVideoView != null && missionVideoView.isFullscreen()) {
						if (event.getAction() == KeyEvent.ACTION_DOWN) {
							missionVideoView.exitFullScreen();
						}
					} else {
						getParentActivity().onClickedBackSystemButton();
					}

					return true;
				} else
					return false; // pass on to be processed as normal
			}
		});
	}


	@Override
	public void onStart() {
		super.onStart();
		MissionInfoFragment.this.resizeDialog();
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();

		LocalBroadcastManager.getInstance(getSafeContext()).unregisterReceiver(receiver);
		intentFilter = null;
	}


	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (getActivity() == null) {
				// Already detached
				return;
			}

			String action = intent.getAction();
			if (action.equals(KPHBroadcastSignals.BROADCAST_SIGNAL_CREDIT_BALANCE_UPDATED)) {
				userData = userService.getUserData();
				updateState();
			}
		}
	};


	@Override
	public int contentLayout() {
		return R.layout.fragment_info_mission;
	}


	private void onClickedSyncToStartNow() {
		if (status != null && status.isUnlockedMission()) {
			//  for start mission
			KPHTracker currentTracker = KPHUserService.sharedInstance().currentTracker();
			if (currentTracker == null) {
				if (getParentActivity() != null) {
					AlertDialogHelper.showAlertDialog(getSafeContext().getString(R.string.no_current_tracker_title),
							getSafeContext().getString(R.string.no_current_tracker_message),
							getParentActivity(),
							null);
				}

				return;
			} else if (currentTracker.getDeviceType().equalsIgnoreCase(KPHTracker.TRACKER_TYPE_NAME_HEALTHKIT)) {
				if (getParentActivity() != null) {
					AlertDialogHelper.showAlertDialog(getSafeContext().getString(R.string.no_current_tracker_title),
							getSafeContext().getString(R.string.health_kit_attached),
							getParentActivity(),
							null);
				}

				return;
			} else if (currentTracker.getDeviceType().equalsIgnoreCase(KPHTracker.TRACKER_TYPE_NAME_GOOGLEFIT)) {
				// Check if user's traker is GF and current device is correct tracker.
				if (!currentTracker.getDeviceId().equalsIgnoreCase(KPHUtils.sharedInstance().getDeviceIdentifier())) {
					// Current tracker is not attached to current user
					String deviceNotMatchGuide = String.format(getString(R.string.google_fit_device_not_match_alert), currentTracker.getName());
					AlertDialogHelper.showAlertDialog(getSafeContext().getString(R.string.no_current_tracker_title),
							deviceNotMatchGuide,
							getParentActivity(),
							null);
					return;
				}
			}

			if (missionService.getActiveUserMission() != null) {
				// has already active user Mission
				if (getParentActivity() != null) {
					AlertDialogHelper.showAlertDialog(getSafeContext().getString(R.string.unlock_mission_has_one_title),
							getSafeContext().getString(R.string.unlock_mission_has_one_description),
							getParentActivity(),
							null);
				}

				return;
			}

			Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_STARTING_USERMISSION);
			intent.putExtra("MissionId", status.getMissionId());
			LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);

			this.dismiss();
		} else {
			// for unlock mission
			if (userData == null)
				return;

			if (userData.getCreditBalance() < 1) {
				gotoCreditDialogs();
				return;
			}

			KPHTracker currentTracker = KPHUserService.sharedInstance().currentTracker();
			if (currentTracker == null) {
				AlertDialogHelper.showAlertDialog(getSafeContext().getString(R.string.no_current_tracker_title),
						getSafeContext().getString(R.string.no_current_tracker_message),
						getParentActivity(),
						null);
				return;
			} else if (currentTracker.getDeviceType().equalsIgnoreCase(KPHTracker.TRACKER_TYPE_NAME_HEALTHKIT)) {
				if (getParentActivity() != null) {
					AlertDialogHelper.showAlertDialog(getSafeContext().getString(R.string.no_current_tracker_title),
							getSafeContext().getString(R.string.health_kit_attached),
							getParentActivity(),
							null);
				}

				return;
			}


			showProgressDialog(getString(R.string.unlocking_mission));
			RestService.get().createUserMission((int) missionInformation.missionId(), userData.getId(), new RestCallback<KPHUserMission>() {
				@Override
				public void failure(RestError restError) {
					// failure, check error
					String name, message = "";

					// show error message
					name = getSafeContext().getString(R.string.unlock_err_credit_title);
					if (restError.getName() != null)
						name = restError.getName();
					if (restError.getMessage() != null)
						message = restError.getMessage();

					AlertDialogHelper.showAlertDialog(name, message, getParentActivity(), null);
				}

				@Override
				public void success(KPHUserMission kphUserMission, Response response) {
					// success
					dismissProgressDialog();
					status = missionService.userMissionStatsFromUserMission(kphUserMission);

					// update Credit card
					if (userData != null) {
						int balance = userData.getCreditBalance() - 1;
						if (balance < 0)
							balance = 0;

						userData.setCreditBalance(balance);
						KPHUserService.sharedInstance().saveUserData(userData);
					}
					// add new user mission status
					missionService.updateUserMissionStatus(status);

					// update UI
					updateState();

					// notify to Passport List
					Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_USER_MISSION_LIST_CHANGED);
					LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
				}
			});
		}
	}


	private void onClickedPlayVideo() {
		if (missionVideoView != null) {
			videoLayout.removeView(missionVideoView);
			missionVideoView = null;
		}

		missionVideoView = new KPHVideoLayout(getActivity());
		missionVideoView.setActivity(getParentActivity());

		try {
			missionVideoView.setVideoURI(Uri.parse(missionInformation.introVideoURL()));

			//Flurry - Track Video Play Event
			Map<String, String> params = new HashMap<>();
			params.put("videoType", "Mission Info");
			if (missionInformation.name() != null)
				params.put("videoName", missionInformation.name());

			params.put("videoURL", missionInformation.introVideoURL());
		} catch (IOException e) {
			e.printStackTrace();
		}

		videoPlaceholderImageView.setVisibility(View.INVISIBLE);
		playButton.setVisibility(View.INVISIBLE);
		missionVideoView.hideControls();

		if (missionVideoView.getCurrentState() == KPHVideoView.State.PREPARED) {
			missionVideoView.start();
		} else {
			missionVideoView.setShouldAutoplay(true);
		}

		missionVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				missionVideoView.exitFullScreen();
				missionVideoView.hideControls();

				videoPlaceholderImageView.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.VISIBLE);
				videoPlaceholderImageView.bringToFront();
				playButton.bringToFront();
			}
		});

		missionVideoView.setLayoutParams(
				new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT
				)
		);
		videoLayout.addView(missionVideoView);
	}


	public void setMissionInformation(KPHMissionInformation missionInfo, KPHUserMissionStats stats) {
		this.missionInformation = missionInfo;
		this.status = stats;
	}


	protected void updateState() {
		if (status == null) {
			changeUISync(MODE_UNLOCK);
		} else if (status.isStartedMission()) {
			changeUISync(MODE_STARTED);
		} else if (status.isUnlockedMission()) {
			changeUISync(MODE_SYNC);
		} else {
			changeUISync(MODE_UNLOCK);
		}
	}


	protected void changeUISync(int mode) {
		if (userData == null) {
			throw new IllegalArgumentException();
		}

		if (mode == MODE_UNLOCK) {
			syncToStartNowButton.setVisibility(View.VISIBLE);
			syncToStartNowButton.setCustomImage(null);

			if (userData.getCreditBalance() <= 0) {
				syncToStartNowButton.setText(getSafeContext().getString(R.string.unlock_buy_credit));
			} else {
				syncToStartNowButton.setText(getSafeContext().getString(R.string.unlock_mission_message));
			}
		} else if (mode == MODE_SYNC) {
			syncToStartNowButton.setVisibility(View.VISIBLE);
			syncToStartNowButton.setText(getSafeContext().getString(R.string.sync_mission_start));
			syncToStartNowButton.setCustomImage(UIManager.sharedInstance().getImageDrawable(R.drawable.sync));
		} else if (mode == MODE_STARTED) {
			syncToStartNowButton.setVisibility(View.GONE);
		}
	}


	public void gotoCreditDialogs() {
		if (getParentActivity() == null)
			return;

		if (!userService.enabledPurchases()) {
			showErrorDialog(getSafeContext().getString(R.string.disabled_purchases));
			return;
		} else {
			BuyCreditFragment frag = new BuyCreditFragment();
			getParentActivity().showDialogFragment(frag);
		}
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Dialog dialog = getDialog();
		if (dialog != null) {
			MissionInfoFragment.this.resizeDialog();
		}
	}


	private void resizeDialog() {
		if (getParentActivity() == null)
			return;

		Dialog dialog = getDialog();
		if (dialog != null) {
			int width = ResolutionSet.getScreenSize(getParentActivity(), false).x;
			int height = ResolutionSet.getScreenSize(getParentActivity(), false).y - ResolutionSet.getStatusBarHeight(getParentActivity());

			dialog.getWindow().setLayout(width, height);

			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			parentLayout.setLayoutParams(params);
		}
	}


}
