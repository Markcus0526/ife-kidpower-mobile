package org.unicefkidpower.kid_power.View.Activities.Main.Friends;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.unicefkidpower.kid_power.Misc.AlertDialogHelper;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.KPHNotificationUtil;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.KPHAnalyticsService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHFollowService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.Model.Structure.KPHBlock;
import org.unicefkidpower.kid_power.Model.Structure.KPHCheer;
import org.unicefkidpower.kid_power.Model.Structure.KPHMessage;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMissionStats;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserSummary;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.Adapters.CheerIconListAdapter;
import org.unicefkidpower.kid_power.View.Adapters.FollowDetailListAdapter;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHCheerSelectionView;
import org.unicefkidpower.kid_power.View.CustomControls.KPHImageTextButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextImageButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

import java.util.List;

import retrofit.client.Response;

/**
 * Created by Ruifeng Shi on 10/27/2015.
 */
public class FriendDetailFragment extends SuperFragment {
	// UI Elements
	private View						contentView					= null;
	private KPHButton					btnBlock					= null;

	private ProgressBar					progressBar					= null;
	private RecyclerView				followDetailListView		= null;
	private FollowDetailListAdapter		followDetailListAdapter		= null;

	private View						headerView					= null;

	private ImageView					avatarImageView				= null;
	private KPHTextView					txtUsername					= null;
	private KPHTextView					txtMissionName				= null;

	private HorizontalScrollView		mainInfoScrollView			= null;
	private LinearLayout				mainInfoContentLayout		= null;
	private boolean						initializedWidth			= false;

	private KPHImageTextButton			txtPackets					= null;
	private KPHImageTextButton			txtMissionsCompleted		= null;
	private KPHImageTextButton			txtPowerPoints				= null;

	private KPHImageTextButton			btnFollow					= null;
	private KPHTextImageButton			btnCheer					= null;


	// Member Variables
	private KPHUserSummary				followerData				= null;
	private boolean						shouldShowBlockButton		= false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		followDetailListAdapter = new FollowDetailListAdapter(getSafeContext());

		followDetailListView = (RecyclerView) contentView.findViewById(R.id.lv_follow_detail);
		followDetailListView.setAdapter(followDetailListAdapter);
		followDetailListView.setLayoutManager(new LinearLayoutManager(getSafeContext(), LinearLayoutManager.VERTICAL, false));

		progressBar = (ProgressBar) contentView.findViewById(R.id.progressBar);

		btnBlock = (KPHButton)contentView.findViewById(R.id.btn_block);
		btnBlock.setCustomFont(getSafeContext(), getSafeContext().getString(R.string.font_pfdindisplaypro_regular));
		btnBlock.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBlockButtonClicked();
			}
		});

		if (!shouldShowBlockButton) {
			btnBlock.setVisibility(View.GONE);
		} else {
			btnBlock.setVisibility(View.VISIBLE);
		}


		RestService.get().getUserMissionProgressForUser(
				followerData.getId(),
				new RestCallback<List<KPHUserMissionStats>>() {
					@Override
					public void failure(RestError restError) {
						contentView.findViewById(R.id.progressBar).setVisibility(View.GONE);
						progressBar.setVisibility(View.GONE);
						followDetailListView.setVisibility(View.VISIBLE);
					}

					@Override
					public void success(List<KPHUserMissionStats> kphUserMissionStats, Response response) {
						contentView.findViewById(R.id.progressBar).setVisibility(View.GONE);
						progressBar.setVisibility(View.GONE);
						followDetailListView.setVisibility(View.VISIBLE);
						followDetailListAdapter.setUserMissionStats(kphUserMissionStats);
					}
				}
		);


		initListHeader();

		return contentView;
	}


	private void initListHeader() {
		if (getParentActivity() == null)
			return;

		headerView = getParentActivity().getLayoutInflater().inflate(R.layout.item_follow_detail_list_header, followDetailListView, false);

		avatarImageView = (ImageView) headerView.findViewById(R.id.ivAvatar);
		txtUsername = (KPHTextView) headerView.findViewById(R.id.txtUsername);
		txtMissionName = (KPHTextView) headerView.findViewById(R.id.txtMissionState);
		txtPackets = (KPHImageTextButton) headerView.findViewById(R.id.ivPacket);
		txtMissionsCompleted = (KPHImageTextButton) headerView.findViewById(R.id.ivMission);
		txtPowerPoints = (KPHImageTextButton) headerView.findViewById(R.id.ivPowerpoint);

		mainInfoScrollView = (HorizontalScrollView) headerView.findViewById(R.id.main_info_horscrollview);
		mainInfoContentLayout = (LinearLayout) headerView.findViewById(R.id.main_info_horscrollview_content_layout);
		mainInfoContentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (initializedWidth)
					return;

				if (mainInfoScrollView.getWidth() > 0 && mainInfoContentLayout.getWidth() > 0) {
					boolean needAutoResize = false;
					if (mainInfoScrollView.getWidth() <= mainInfoContentLayout.getWidth()) {
						needAutoResize = true;
					}

					int totalWidth = mainInfoScrollView.getWidth();
					ViewGroup.LayoutParams mainParams = mainInfoContentLayout.getLayoutParams();
					mainParams.width = totalWidth;

					LinearLayout.LayoutParams params1, params2, params3;

					params1 = (LinearLayout.LayoutParams) txtPackets.getLayoutParams();
					params2 = (LinearLayout.LayoutParams) txtMissionsCompleted.getLayoutParams();
					params3 = (LinearLayout.LayoutParams) txtPowerPoints.getLayoutParams();

					int weight1 = params1.width, weight2 = params2.width, weight3 = params3.width;
					int weightSum = weight1 + weight2 + weight3;

					params1.width = totalWidth * weight1 / weightSum;
					txtPackets.setLayoutParams(params1);

					params2.width = totalWidth * weight2 / weightSum;
					txtMissionsCompleted.setLayoutParams(params2);

					params3.width = totalWidth * weight3 / weightSum;
					txtPowerPoints.setLayoutParams(params3);

					if (needAutoResize) {
						txtPackets.autoFitSize(params1.width);
						txtMissionsCompleted.autoFitSize(params2.width);
						txtPowerPoints.autoFitSize(params3.width);
					}

					initializedWidth = true;
				}
			}
		});

		btnFollow = (KPHImageTextButton) headerView.findViewById(R.id.btnFollow);
		btnFollow.setGravity(Gravity.CENTER);
		btnFollow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onFollowButtonClicked();
			}
		});

		btnCheer = (KPHTextImageButton) headerView.findViewById(R.id.btnCheer);
		btnCheer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onCheerButtonClicked((KPHTextImageButton) v);
			}
		});

		// Initializing friend details......
		if (followerData.getAvatarId().length() != 0) {
			Drawable drawable = KPHUserService.sharedInstance().getAvatarDrawable(followerData.getAvatarId());
			if (drawable != null)
				avatarImageView.setImageDrawable(drawable);
			else {
				avatarImageView.setImageDrawable(
						UIManager.sharedInstance().getImageDrawable(R.drawable.avatar_placeholder)
				);
			}
		}

		txtUsername.setText(followerData.getHandle());

		if (followerData.getCurrentMission() != null &&
				followerData.getCurrentMission().length() > 0) {
			txtMissionName.setText("in " + followerData.getCurrentMission());
			txtMissionName.setVisibility(View.VISIBLE);
		} else {
			txtMissionName.setVisibility(View.GONE);
		}

		txtPackets.setText("" + followerData.getTotalPackets());
		txtMissionsCompleted.setText("" + followerData.getMissionsCompleted());
		txtPowerPoints.setText("" + followerData.getTotalPoints());

		followDetailListAdapter.setHeaderView(headerView);

		updateFollowButtonState();
		// End of 'Initializing friend details'

	}


	private void updateFollowButtonState() {
		if (KPHUserService.sharedInstance().isUserBeingFollowedByMe(followerData.getId())) {
			btnFollow.setCustomImage(UIManager.sharedInstance().getDrawable(R.drawable.ic_green_check));
			btnFollow.setText(getSafeContext().getString(R.string.following));
			btnFollow.setTextColor(UIManager.sharedInstance().getColor(R.color.kph_color_success_green));
		} else {
			btnFollow.setCustomImage(null);
			btnFollow.setText(getSafeContext().getString(R.string.follow));
			btnFollow.setTextColor(UIManager.sharedInstance().getColor(R.color.kph_color_orange));
		}
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_friend_detail;
	}

	public void setData(@NonNull KPHUserSummary follower, boolean shouldShowBlockButton) {
		this.followerData = follower;
		this.shouldShowBlockButton = shouldShowBlockButton;

		//Remove Block button if follower is a family account
		KPHUserData userData = KPHUserService.sharedInstance().getUserData();
		if (userData.getParent() != null && userData.getParent().getId() == follower.getId() && this.shouldShowBlockButton) {
			this.shouldShowBlockButton = false;
		} else {
			if (this.shouldShowBlockButton && userData.getSiblings() != null) {
				for (KPHUserSummary userSummary : userData.getSiblings()) {
					if (userSummary.getId() == followerData.getId()) {
						this.shouldShowBlockButton = false;
						break;
					}
				}
			}

			if (this.shouldShowBlockButton && userData.getChildren() != null) {
				for (KPHUserSummary userSummary : userData.getChildren()) {
					if (userSummary.getId() == followerData.getId()) {
						this.shouldShowBlockButton = false;
						break;
					}
				}
			}
		}
	}

	/**
	 * Called when user click the block button on the top right corner
	 */
	private void onBlockButtonClicked() {
		String sAlertTitle = getSafeContext().getString(R.string.block) + " " + followerData.getHandle();
		String sAlertDescription = getSafeContext().getString(R.string.block_alert_description) +
				" " + followerData.getHandle() + "?";

		if (getParentActivity() != null) {
			AlertDialogHelper.showConfirmDialog(
					sAlertTitle,
					sAlertDescription,
					getParentActivity(),
					new AlertDialogHelper.AlertListener() {
						@Override
						public void onPositive() {
							showProgressDialog();
							KPHFollowService.sharedInstance().blockUser(followerData, new onActionListener() {
								@Override
								public void completed(Object object) {
									dismissProgressDialog();

									KPHBlock blockedUser = (KPHBlock) object;
									KPHFollowService.sharedInstance().fetchBlockedList(null);

									KPHUserData userData = KPHUserService.sharedInstance().getUserData();
									for (int i = 0; i < userData.getFollowers().size(); i++) {
										if (userData.getFollowers().get(i).getId() == followerData.getId()) {
											userData.getFollowers().remove(i);
											break;
										}
									}

									KPHUserService.sharedInstance().saveUserData(userData);

									SuperActivity parentActivity = getParentActivity();
									if (parentActivity == null)
										return;

									parentActivity.onClickedBackSystemButton();

									if (parentActivity instanceof MainActivity) {
										// user is blocked, need to add to blocked array
										FriendsMainFragment.blockedArray.add(blockedUser);
										LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(
												new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_SHOULD_UPDATE_FRIENDS_TABS)
										);
									}

									KPHNotificationUtil.sharedInstance().showSuccessNotification(
											parentActivity,
											getSafeContext().getString(R.string.blocked) + " " + followerData.getHandle()
									);
								}

								@Override
								public void failed(int code, String message) {
									dismissProgressDialog();
									showErrorDialog(message);
								}
							});
						}

						@Override
						public void onNegative() {}

						@Override
						public void onCancelled() {}
					}
			);
		}
	}


	private void onFollowButtonClicked() {
		btnFollow.setEnabled(false);

		showProgressDialog();
		if (KPHUserService.sharedInstance().isUserBeingFollowedByMe(followerData.getId())) {
			// Unfollowing user
			KPHFollowService.sharedInstance().unfollowUser(followerData, new onActionListener() {
				@Override
				public void completed(Object object) {
					dismissProgressDialog();
					KPHUserService.sharedInstance().getUserData().unfollowUser(followerData);

					followDetailListAdapter.notifyDataSetChanged();
					btnFollow.setEnabled(true);

					SuperActivity parentActivity = getParentActivity();
					if (parentActivity == null)
						return;

					updateFollowButtonState();


					KPHNotificationUtil.sharedInstance().showSuccessNotification(parentActivity, getSafeContext().getString(R.string.unfollowed) + " " + followerData.getHandle());

					Intent newIntent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_SHOULD_UPDATE_FRIENDS_TABS);
					LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(newIntent);
				}


				@Override
				public void failed(int code, String message) {
					btnFollow.setEnabled(true);
					showErrorDialog(message);
				}
			});
		} else {
			KPHFollowService.sharedInstance().followUser(followerData, new onActionListener() {
				@Override
				public void completed(Object object) {
					dismissProgressDialog();
					KPHUserService.sharedInstance().getUserData().getFollowings().add(followerData);

					followDetailListAdapter.notifyDataSetChanged();
					btnFollow.setEnabled(true);

					SuperActivity parentActivity = getParentActivity();
					if (parentActivity == null)
						return;

					updateFollowButtonState();


					KPHNotificationUtil.sharedInstance().showSuccessNotification(parentActivity, getSafeContext().getString(R.string.follow) + " " + followerData.getHandle());

					Intent newIntent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_SHOULD_UPDATE_FRIENDS_TABS);
					LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(newIntent);
				}

				@Override
				public void failed(int code, String message) {
					btnFollow.setEnabled(true);
					showErrorDialog(message);
				}
			});
		}
	}

	private void onCheerButtonClicked(KPHTextImageButton btnCheer) {
		KPHCheerSelectionView vwCheerSelection = new KPHCheerSelectionView(getSafeContext());
		vwCheerSelection.setCheerIconListAdapter(
				new CheerIconListAdapter(
						getSafeContext(), KPHUserService.sharedInstance().getUserData().getCheers()
				)
		);
		vwCheerSelection.setCheerIconListItemClickListener(new OnCheerIconListItemClickListener(btnCheer));
		vwCheerSelection.show(btnCheer);
	}


	private class OnCheerIconListItemClickListener implements AdapterView.OnItemClickListener {
		KPHTextImageButton btnCheer;

		public OnCheerIconListItemClickListener(View v) {
			super();
			this.btnCheer = (KPHTextImageButton) v;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			KPHCheer selectedCheer = (KPHCheer) parent.getItemAtPosition(position);
			RestService.get().sendMessage(
					followerData.getId(),
					selectedCheer.getType(),
					selectedCheer.getId(),
					new RestCallback<KPHMessage>() {
						@Override
						public void failure(RestError restError) {
							showErrorDialog(KPHUtils.sharedInstance().getNonNullMessage(restError));
						}

						@Override
						public void success(KPHMessage kphMessage, Response response) {
							KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_CHEER_SENT);
							if (getParentActivity() != null && getParentActivity() instanceof MainActivity) {
								KPHNotificationUtil.sharedInstance().showSuccessNotification(getParentActivity(), getSafeContext().getString(R.string.cheer_sent));
							}
						}
					}
			);
		}
	}
}
