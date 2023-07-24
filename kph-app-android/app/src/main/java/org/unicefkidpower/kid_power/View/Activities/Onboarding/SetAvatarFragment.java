package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHNotificationUtil;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.Adapters.AvatarListAdapter;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

import java.util.Date;
import java.util.Random;

/**
 * Created by Ruifeng Shi on 8/31/2015.
 */
public class SetAvatarFragment extends SuperFragment {
	private final int				START_SHOWING_DELAY				= 500;

	// UI Elements
	private ImageView				avatarImageView					= null;
	private KPHTextView				txtUsername						= null;
	private KPHTextView				txtRandomizedChoiceMessage		= null;
	private GridView				gvAvatars						= null;
	private KPHButton				btnNext							= null;

	private AvatarListAdapter		adapter							= null;
	// End of 'UI Elements'


	// Random strings for selecting avatars
	private Random					random							= null;
	private String[]				randomizedChoiceMessages		= null;
	private String					randomizedChoiceMessage			= "";
	// End of 'Random strings for selecting avatars'

	private int						nSelectedAvatarItemIndex		= -1;
	private String					sDefaultSelectedAvatarId		= null;

	private String					mUsername						= "";


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View contentView = super.onCreateView(inflater, container, savedInstanceState);

		avatarImageView = (ImageView) contentView.findViewById(R.id.ivAvatar);
		txtUsername = (KPHTextView) contentView.findViewById(R.id.txt_username);
		txtRandomizedChoiceMessage = (KPHTextView) contentView.findViewById(R.id.txt_randomized_choice_message);
		btnNext = (KPHButton) contentView.findViewById(R.id.btn_next);
		gvAvatars = (GridView) contentView.findViewById(R.id.gv_avatars);

		txtUsername.setText(TextUtils.isEmpty(mUsername) ?
				getSafeContext().getResources().getString(R.string.hi) :
				getSafeContext().getResources().getString(R.string.hi) + ", " + mUsername);


		adapter = new AvatarListAdapter(getSafeContext());
		if (getParentActivity() != null && (getParentActivity() instanceof OnboardingActivity)) {
			sDefaultSelectedAvatarId = ((OnboardingActivity)getParentActivity()).newAvatarId;
		}

		if (sDefaultSelectedAvatarId != null && sDefaultSelectedAvatarId.length() > 0) {
			nSelectedAvatarItemIndex = adapter.setAvatarSelection(sDefaultSelectedAvatarId);
		}

		if (getParentActivity() != null && !(getParentActivity() instanceof OnboardingActivity)) {
			btnNext.setText(R.string.update);
		}

		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onSetAvatarButtonClicked();
			}
		});

		if (nSelectedAvatarItemIndex != -1) {
			adapter.setSelectedItemIndex(nSelectedAvatarItemIndex);

			if (!TextUtils.isEmpty(randomizedChoiceMessage)) {
				txtRandomizedChoiceMessage.setText(randomizedChoiceMessage);
			}

			avatarImageView.setImageDrawable(adapter.getSelectedAvatarDrawable());
		}

		gvAvatars.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position != nSelectedAvatarItemIndex) {
					txtRandomizedChoiceMessage.setText(getRandomizedChoiceMessage());
				}

				adapter.setSelectedItemIndex(position);
				nSelectedAvatarItemIndex = position;
				adapter.notifyDataSetChanged();

				avatarImageView.setImageDrawable(adapter.getSelectedAvatarDrawable());

				if (getParentActivity() instanceof OnboardingActivity) {
					OnboardingActivity activity = (OnboardingActivity) getParentActivity();
					activity.newAvatarId = getAvatarId();
				}
			}
		});


		showProgressDialog();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				gvAvatars.setAdapter(adapter);
				dismissProgressDialog();
			}
		}, START_SHOWING_DELAY);


		return contentView;
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_set_avatar;
	}


	public void setData(String username, String avatarId) {
		this.mUsername = username;

		if (avatarId != null && !avatarId.trim().equals("")) {
			this.sDefaultSelectedAvatarId = avatarId;
		}

		// Initialize controls
		if (txtUsername == null) {
			// Not initialized yet
			return;
		}

		txtUsername.setText(TextUtils.isEmpty(mUsername) ?
				getSafeContext().getResources().getString(R.string.hi) :
				getSafeContext().getResources().getString(R.string.hi) + ", " + mUsername);

		if (sDefaultSelectedAvatarId != null && sDefaultSelectedAvatarId.length() > 0) {
			nSelectedAvatarItemIndex = adapter.setAvatarSelection(sDefaultSelectedAvatarId);
		}

		if (nSelectedAvatarItemIndex != -1) {
			adapter.setSelectedItemIndex(nSelectedAvatarItemIndex);
			avatarImageView.setImageDrawable(adapter.getSelectedAvatarDrawable());
		}
	}


	public String getAvatarId() {
		return adapter.getSelectedAvatarId();
	}


	public String getRandomizedChoiceMessage() {
		if (randomizedChoiceMessages == null || randomizedChoiceMessages.length == 0)
			randomizedChoiceMessages = getSafeContext().getResources().getStringArray(R.array.avatar_randomized_choice_messages);

		if (random == null)
			random = new Random(new Date().getTime() % 100);

		String randomizedChoiceMessage = randomizedChoiceMessages[random.nextInt(randomizedChoiceMessages.length)];
		while (randomizedChoiceMessage.equalsIgnoreCase(this.randomizedChoiceMessage)) {
			randomizedChoiceMessage = randomizedChoiceMessages[random.nextInt(randomizedChoiceMessages.length)];
		}

		this.randomizedChoiceMessage = randomizedChoiceMessage;

		return randomizedChoiceMessage;
	}


	private void onSetAvatarButtonClicked() {
		if (getAvatarId().length() == 0) {
			showBrandedDialog(getSafeContext().getString(R.string.no_avatar_selected), getSafeContext().getString(R.string.ok), null, null);
			return;
		}

		SuperActivity parentActivity = getParentActivity();
		if (parentActivity instanceof OnboardingActivity &&
				!((OnboardingActivity)parentActivity).isCompletingParentProfile) {
			Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_SET_ICON_CONFIRMED);
			LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
		} else {
			// When called from Edit Profile screen
			KPHUserData userData = KPHUserService.sharedInstance().getUserData();

			showProgressDialog();
			KPHUserService.sharedInstance().updateUserData(
					userData.getId(),
					userData.getHandle(),
					userData.getEmail(),
					userData.getFriendlyName(),
					userData.getGender(),
					userData.getBirthday(),
					getAvatarId(),
					updatedAvatarListener
			);
		}
	}


	private onActionListener updatedAvatarListener = new onActionListener() {
		@Override
		public void completed(Object object) {
			if (getParentActivity() == null)
				return;

			dismissProgressDialog();

			if (object != null)
				KPHUserService.sharedInstance().saveUserData((KPHUserData) object);

			SuperActivity parentActivity = getParentActivity();
			if (parentActivity instanceof MainActivity) {
				MainActivity mainActivity = (MainActivity) getParentActivity();

				Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_AVATAR_UPDATED);
				LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
				mainActivity.onClickedBackSystemButton();

				KPHNotificationUtil.sharedInstance().showSuccessNotification(mainActivity, getSafeContext().getString(R.string.icon_changed));
			} else if (parentActivity instanceof OnboardingActivity) {
				// In case of completing parent profile.
				Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_EMAIL_PASSWORD_SELECTED);
				LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
			}
		}

		@Override
		public void failed(int code, String message) {
			showErrorDialog(message);
		}
	};

}
