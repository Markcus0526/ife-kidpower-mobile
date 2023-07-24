
package org.unicefkidpower.kid_power.View.Activities.Main.Friends;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.unicefkidpower.kid_power.Misc.AlertDialogHelper;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.KPHFollowService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.Structure.KPHBlock;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Ruifeng Shi on 10/30/2015.
 */
public class FollowBlockedFragment extends SuperFragment {
	// UI Elements
	private View					contentView				= null;
	private RecyclerView			blockListView			= null;

	// Member Variables
	private BlockedListAdapter		blockedListAdapter		= null;
	private List<KPHBlock>			blockedArray			= new ArrayList<>();


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		blockListView = (RecyclerView) contentView.findViewById(R.id.lv_blocked);

		if (blockedArray != null) {
			blockedListAdapter = new BlockedListAdapter(
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							onUnblockAtIndex((int) v.getTag());
						}
					}
			);
			blockListView.setAdapter(blockedListAdapter);
			blockListView.setLayoutManager(new LinearLayoutManager(getSafeContext(), LinearLayoutManager.VERTICAL, false));
		}

		return contentView;
	}

	public void setBlockedArray(List<KPHBlock> blockedArray) {
		this.blockedArray = blockedArray;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_blocked;
	}


	private void onUnblockAtIndex(int nIndex) {
		final KPHBlock block = blockedArray.get(nIndex);

		String sUnblockAlertTitle = getSafeContext().getString(R.string.unblock) + " " + block.getBlocked().getHandle();
		String sUnblockAlertDescription = getSafeContext().getString(R.string.unblock_alert_description) + " " + block.getBlocked().getHandle() + "?";

		if (getParentActivity() != null) {
			AlertDialogHelper.showConfirmDialog(
					sUnblockAlertTitle,
					sUnblockAlertDescription,
					getParentActivity(),
					new AlertDialogHelper.AlertListener() {
						@Override
						public void onPositive() {
							showProgressDialog();
							KPHFollowService.sharedInstance().unblockUser(block, new onActionListener() {
								@Override
								public void completed(Object object) {
									dismissProgressDialog();

									SuperActivity parentActivity = getParentActivity();
									if (parentActivity == null)
										return;

									for (int i = 0; i < FriendsMainFragment.blockedArray.size(); i++) {
										KPHBlock blockedUserItem = FriendsMainFragment.blockedArray.get(i);
										if (blockedUserItem.getId() == block.getId()) {
											FriendsMainFragment.blockedArray.remove(i);
											break;
										}
									}

									Intent newIntent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_SHOULD_UPDATE_FRIENDS_TABS);
									LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(newIntent);

									parentActivity.onClickedBackSystemButton();
								}

								@Override
								public void failed(int code, String message) {
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


	private class BlockedListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		View.OnClickListener onUnblockButtonClickListener = null;

		public BlockedListAdapter(
				View.OnClickListener onUnblockButtonClickListener
		) {
			// Sort array of missions by their IDs
			Collections.sort(blockedArray, new Comparator<KPHBlock>() {
				@Override
				public int compare(KPHBlock lhs, KPHBlock rhs) {
					return lhs.getBlocked().getHandle().compareToIgnoreCase(rhs.getBlocked().getHandle()) +
							lhs.getBlocker().getHandle().compareToIgnoreCase(rhs.getBlocker().getHandle());
				}
			});

			this.onUnblockButtonClickListener = onUnblockButtonClickListener;
		}

		@Override
		public int getItemCount() {
			return blockedArray.size();
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater layoutInflater = (LayoutInflater) getSafeContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View contentView = layoutInflater.inflate(R.layout.item_block, parent, false);
			BlockedListItemHolder holder = new BlockedListItemHolder(contentView);

			if (onUnblockButtonClickListener != null)
				holder.btnUnblock.setOnClickListener(onUnblockButtonClickListener);

			return holder;
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
			((BlockedListItemHolder)holder).setBlock(blockedArray.get(position));
			((BlockedListItemHolder)holder).btnUnblock.setTag(position);
		}


		private class BlockedListItemHolder extends RecyclerView.ViewHolder {
			private ImageView ivAvatar;
			private KPHTextView txtUsername;
			private KPHButton btnUnblock;

			public BlockedListItemHolder(View itemView) {
				super(itemView);

				ivAvatar = (ImageView) itemView.findViewById(R.id.ivAvatar);
				txtUsername = (KPHTextView) itemView.findViewById(R.id.txt_username);
				btnUnblock = (KPHButton) itemView.findViewById(R.id.btn_unblock);
			}

			public void setBlock(KPHBlock block) {
				int userId = KPHUserService.sharedInstance().getUserData().getId();
				KPHUserData blockedUser;

				if (block.getBlockerId() == userId) {
					blockedUser = block.getBlocked();
				} else {
					blockedUser = block.getBlocker();
				}

				Drawable drawable = KPHUserService.sharedInstance().getAvatarDrawable(blockedUser.getAvatarId());

				if (drawable != null)
					ivAvatar.setImageDrawable(drawable);
				else {
					ivAvatar.setImageDrawable(UIManager.sharedInstance().getImageDrawable(R.drawable.avatar_placeholder));
				}

				txtUsername.setText(blockedUser.getHandle());
			}
		}
	}
}
