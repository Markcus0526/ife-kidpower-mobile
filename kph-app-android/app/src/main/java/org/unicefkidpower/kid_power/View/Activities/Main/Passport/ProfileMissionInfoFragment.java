package org.unicefkidpower.kid_power.View.Activities.Main.Passport;

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

import org.unicefkidpower.kid_power.View.CustomControls.KPHImageTextButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.CustomControls.KPHVideoLayout;
import org.unicefkidpower.kid_power.View.CustomControls.KPHVideoView;
import org.unicefkidpower.kid_power.Model.Structure.KPHMissionInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHTracker;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMission;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMissionStats;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;
import org.unicefkidpower.kid_power.View.Super.SuperNormalSizeDialogFragment;
import org.unicefkidpower.kid_power.Model.MicroService.KPHMissionService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.AlertDialogHelper;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.Misc.UIManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit.client.Response;


/**
 * Created by Ruifeng Shi on 9/28/2015.
 */
public class ProfileMissionInfoFragment extends SuperFragment {
	public static int 							MODE_UNLOCK			= 0;
	public static int 							MODE_SYNC			= 1;
	public static int 							MODE_STARTED		= 2;

	private KPHUserService						userService				= null;
	private KPHMissionService					missionService			= null;

	private KPHMissionInformation				mMissionInformation		= null;
	private KPHUserData							mUserData				= null;
	private KPHUserMissionStats					mStatus					= null;

	private View								contentView				= null;
	private RelativeLayout						rlVideoView				= null;
	private KPHVideoLayout						videoMission			= null;
	private ImageView							ivVideoPlaceholder		= null;
	private ImageView							ivCountry				= null;
	private ImageButton							btnPlay					= null;
	private KPHImageTextButton					btnSyncToStartNow		= null;
	private KPHTextView							lblMissionName			= null;
	private KPHTextView							lblMissionDescription	= null;

	private ProfileMissionAutoStartFragment		parentFragment			= null;
	private IntentFilter						intentFilter			= null;

	private boolean								needCustomTitle			= false;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		missionService = KPHMissionService.sharedInstance();
		userService = KPHUserService.sharedInstance();

		mUserData = userService.getUserData();

		contentView = super.onCreateView(inflater, container, savedInstanceState);
		if (mMissionInformation == null)
			return contentView;

		rlVideoView = (RelativeLayout) contentView.findViewById(R.id.rlVideoView);
		ivVideoPlaceholder = (ImageView) contentView.findViewById(R.id.ivVideoPlaceholder);

		Drawable drawable = mMissionInformation.getVideoDrawable();
		if (drawable != null) {
			ivVideoPlaceholder.setImageDrawable(drawable);

			// Calculates the height of placeholder image
			float fPlaceHolderHeight = ((float) ((BitmapDrawable) drawable).getBitmap().getHeight() / ((BitmapDrawable) drawable).getBitmap().getWidth()) *
					(ResolutionSet.getScreenSize(getSafeContext(), false).x - getSafeContext().getResources().getDimensionPixelSize(R.dimen.dimen_24dp) * 2);
			ViewGroup.LayoutParams lpVideoView = rlVideoView.getLayoutParams();
			lpVideoView.height = (int) fPlaceHolderHeight;
			rlVideoView.setLayoutParams(lpVideoView);
		}

		ivCountry = (ImageView) contentView.findViewById(R.id.ivCountry);

		drawable = mMissionInformation.getCountryDrawable();
		if (drawable != null)
			ivCountry.setImageDrawable(drawable);


		btnPlay = (ImageButton) contentView.findViewById(R.id.btnPlay);
		btnPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onVideoPlayButtonClicked();
			}
		});

		btnSyncToStartNow = (KPHImageTextButton) contentView.findViewById(R.id.btnSyncToStartNow);
		btnSyncToStartNow.setGravity(Gravity.CENTER);
		btnSyncToStartNow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onSyncButtonClicked();
			}
		});


		lblMissionName = (KPHTextView) contentView.findViewById(R.id.lblCountryName);
		lblMissionDescription = (KPHTextView) contentView.findViewById(R.id.lblMissionDescription);

		// Make the font of "Star Wars: Force For Change" copy italic.
		if (needCustomTitle) {
			lblMissionName.setText(R.string.alex_morgan_custom_mission_title);
			lblMissionDescription.setText(R.string.alex_morgan_custom_mission_description);
		} else {
			String missionName = mMissionInformation.name();
			missionName = missionName.replace("Star Wars: Force For Change ", "<i>Star Wars: Force For Change</i> ");
			lblMissionName.setText(Html.fromHtml(missionName));

			String missionDescription = mMissionInformation.description();
			// Make the font of "Star Wars Rebels" copy italic.
			missionDescription = missionDescription.replace("Star Wars Rebels", "<i>Star Wars Rebels</i>");
			lblMissionDescription.setText(Html.fromHtml(missionDescription));
		}


		if (intentFilter == null) {
			intentFilter = new IntentFilter();
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_CREDIT_BALANCE_UPDATED);
			LocalBroadcastManager.getInstance(getSafeContext()).registerReceiver(receiver, intentFilter);
		}


		updateState();

		return contentView;
	}


	@Override
	public void onStart() {
		super.onStart();
		if (parentFragment != null)
			SuperNormalSizeDialogFragment.resizeDialog(parentFragment.getDialog(), parentFragment.getParentLayout());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (parentFragment != null)
			SuperNormalSizeDialogFragment.resizeDialog(parentFragment.getDialog(), parentFragment.getParentLayout());
	}


	@Override
	public void onResume() {
		super.onResume();
		parentFragment.getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(
					android.content.DialogInterface dialog,
					int keyCode,
					android.view.KeyEvent event
			) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == android.view.KeyEvent.KEYCODE_BACK) {
					if (videoMission != null && videoMission.isFullscreen()) {
						if (event.getAction() == KeyEvent.ACTION_DOWN) {
							videoMission.exitFullScreen();
						}
					} else {
						getParentActivity().onClickedBackSystemButton();
					}

					return true;
				} else {
					// pass on to be processed as normal
					return false;
				}
			}
		});
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
				mUserData = userService.getUserData();
				updateState();
			}
		}
	};


	@Override
	public int contentLayout() {
		return R.layout.item_profile_info_mission;
	}


	public void setNeedCustomTitle(boolean flag) {
		needCustomTitle = flag;
	}


	private void onSyncButtonClicked() {
		if (mStatus != null && mStatus.isUnlockedMission()) {
			//  for start mission
			KPHTracker currentTracker = KPHUserService.sharedInstance().currentTracker();
			if (currentTracker == null) {
				AlertDialogHelper.showAlertDialog(getSafeContext().getString(R.string.no_current_tracker_title),
						getSafeContext().getString(R.string.no_current_tracker_message), getParentActivity(), null);
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

			if (missionService.getActiveUserMission() != null) {
				// has already active user Mission
				AlertDialogHelper.showAlertDialog(getSafeContext().getString(R.string.unlock_mission_has_one_title),
						getSafeContext().getString(R.string.unlock_mission_has_one_description), getParentActivity(), null);
				return;
			}

			Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_STARTING_USERMISSION);
			intent.putExtra("MissionId", mStatus.getMissionId());
			LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);

			parentFragment.dismiss();
		} else {
			// for unlock mission
			if (mUserData == null)
				return;

			if (mUserData.getCreditBalance() < 1) {
				gotoCreditDialogs();
				return;
			}

			KPHTracker currentTracker = KPHUserService.sharedInstance().currentTracker();
			if (currentTracker == null) {
				AlertDialogHelper.showAlertDialog(getSafeContext().getString(R.string.no_current_tracker_title),
						getSafeContext().getString(R.string.no_current_tracker_message), getParentActivity(), null);
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
			RestService.get().createUserMission((int) mMissionInformation.missionId(), mUserData.getId(), new RestCallback<KPHUserMission>() {
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
					mStatus = missionService.userMissionStatsFromUserMission(kphUserMission);

					// update Credit card
					if (mUserData != null) {
						int balance = mUserData.getCreditBalance() - 1;
						if (balance < 0)
							balance = 0;
						mUserData.setCreditBalance(balance);
						KPHUserService.sharedInstance().saveUserData(mUserData);
					}

					// add new user mission status
					missionService.updateUserMissionStatus(mStatus);

					// update UI
					updateState();

					// notify to Passport List
					Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_USER_MISSION_LIST_CHANGED);
					LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
				}
			});
		}
	}


	private void onVideoPlayButtonClicked() {
		if (videoMission != null) {
			rlVideoView.removeView(videoMission);
			videoMission = null;
		}

		videoMission = new KPHVideoLayout(getActivity());
		videoMission.setActivity(getParentActivity());

		try {
			videoMission.setVideoURI(Uri.parse(mMissionInformation.introVideoURL()));

			// Flurry - Track Video Play Event
			Map<String, String> params = new HashMap<>();
			params.put("videoType", "Mission Info");
			if (mMissionInformation.name() != null)
				params.put("videoName", mMissionInformation.name());
			params.put("videoURL", mMissionInformation.introVideoURL());
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
			}
		});

		videoMission.setLayoutParams(
				new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT
				)
		);

		rlVideoView.addView(videoMission);
	}


	public void setMissionInformation(KPHMissionInformation missionInfo, KPHUserMissionStats stats, ProfileMissionAutoStartFragment parent) {
		this.mMissionInformation = missionInfo;
		this.mStatus = stats;
		this.parentFragment = parent;
	}


	protected void updateState() {
		if (mStatus == null) {
			changeUISync(MODE_UNLOCK);
		} else if (mStatus.isStartedMission()) {
			changeUISync(MODE_STARTED);
		} else if (mStatus.isUnlockedMission()) {
			changeUISync(MODE_SYNC);
		} else {
			changeUISync(MODE_UNLOCK);
		}
	}


	protected void changeUISync(int mode) {
		if (mUserData == null) {
			throw new IllegalArgumentException();
		}

		if (mode == MODE_UNLOCK) {
			btnSyncToStartNow.setVisibility(View.VISIBLE);
			btnSyncToStartNow.setCustomImage(null);

			if (mUserData.getCreditBalance() <= 0) {
				btnSyncToStartNow.setText(getSafeContext().getString(R.string.unlock_buy_credit));
			} else {
				btnSyncToStartNow.setText(getSafeContext().getString(R.string.unlock_mission_message));
			}
		} else if (mode == MODE_SYNC) {
			btnSyncToStartNow.setVisibility(View.VISIBLE);
			btnSyncToStartNow.setText(getSafeContext().getString(R.string.sync_mission_start));
			btnSyncToStartNow.setCustomImage(UIManager.sharedInstance().getImageDrawable(R.drawable.sync));
		} else if (mode == MODE_STARTED) {
			btnSyncToStartNow.setVisibility(View.GONE);
		}
	}


	public void gotoCreditDialogs() {
		if (getParentActivity() == null)
			return;

		if (!userService.enabledPurchases()) {
			showErrorDialog(getSafeContext().getString(R.string.disabled_purchases));
		} else {
			BuyCreditFragment frag = new BuyCreditFragment();
			getParentActivity().showDialogFragment(frag);
		}
	}

}
