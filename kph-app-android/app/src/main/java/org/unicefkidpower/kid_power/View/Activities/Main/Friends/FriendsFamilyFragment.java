package org.unicefkidpower.kid_power.View.Activities.Main.Friends;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserSummary;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.OnboardingActivity;
import org.unicefkidpower.kid_power.View.Adapters.FamilyAccountListAdapter;
import org.unicefkidpower.kid_power.View.Adapters.FollowListAdapter;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHSegmentedGroup;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

/**
 * Created by Ruifeng Shi on 11/10/2015.
 */
public class FriendsFamilyFragment extends SuperFragment {
	// UI Elements
	private View					contentView						= null;
	private RelativeLayout			layoutNoFamilyAccount			= null;
	private RelativeLayout			layoutFamilyAccounts			= null;
	private KPHButton				btnNewFamilyAccount				= null;
	private KPHButton				btnNewFamilyAccountBottom		= null;
	private KPHSegmentedGroup		sgSortingMethod					= null;
	private KPHTextView				txtRankDescription				= null;


	// Member Variables
	private FamilyAccountListAdapter		familyAccountListAdapter	= null;
	private RecyclerView					familyAccountsListView		= null;

	private IntentFilter intentFilter = null;
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (getActivity() == null) {
				// Already detached
				return;
			}

			String action = intent.getAction();
			switch (action) {
				case KPHBroadcastSignals.BROADCAST_SIGNAL_CHILDREN_SUMMARY_UPDATED:
				case KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_USER_DATA_UPDATED:
					if (familyAccountListAdapter != null) {
						if (familyAccountListAdapter.getItemCount() != 0 &&
								familyAccountListAdapter.getItemCount() != KPHUserService.sharedInstance().getFamilyAccountsCount()) {
							Intent newIntent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_SHOULD_UPDATE_FRIENDS_TABS);
							LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(newIntent);
						}

						familyAccountListAdapter.setUserData(KPHUserService.sharedInstance().getUserData());
						familyAccountListAdapter.notifyDataSetChanged();
					}

					break;
			}
		}
	};


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		if (intentFilter == null) {
			intentFilter = new IntentFilter();
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_USER_DATA_UPDATED);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_CHILDREN_SUMMARY_UPDATED);

			LocalBroadcastManager.getInstance(getSafeContext()).registerReceiver(receiver, intentFilter);
		}


		layoutNoFamilyAccount = (RelativeLayout) contentView.findViewById(R.id.layout_no_family_account);
		layoutFamilyAccounts = (RelativeLayout) contentView.findViewById(R.id.layout_family_accounts);


		familyAccountListAdapter = new FamilyAccountListAdapter(getSafeContext(), KPHUserService.sharedInstance().getUserData());
		familyAccountListAdapter.setShowDisclosure(true);
		familyAccountListAdapter.setOnItemClickListener(new OnFamilyAccountListItemClickListener());

		familyAccountsListView = (RecyclerView) contentView.findViewById(R.id.lv_family_accounts);
		familyAccountsListView.setAdapter(familyAccountListAdapter);
		familyAccountsListView.setLayoutManager(new LinearLayoutManager(getSafeContext(), LinearLayoutManager.VERTICAL, false));

		btnNewFamilyAccount = (KPHButton) contentView.findViewById(R.id.btn_new_family_account);
		btnNewFamilyAccount.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onNewFamilyAccountButtonClicked();
			}
		});

		btnNewFamilyAccountBottom = (KPHButton) contentView.findViewById(R.id.btn_new_family_account_bottom);
		btnNewFamilyAccountBottom.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onNewFamilyAccountButtonClicked();
			}
		});

		txtRankDescription = (KPHTextView) contentView.findViewById(R.id.txt_rank_description);
		sgSortingMethod = (KPHSegmentedGroup) contentView.findViewById(R.id.segmentedSort);
		sgSortingMethod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.radioAZ:
						onRadioAZChecked();
						break;
					case R.id.radioRank:
						onRadioRankChecked();
						break;
				}
			}
		});

		configureFamilyAccountsList();

		return contentView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_friends_familyaccounts;
	}

	public String getPageTitle() {
		return getSafeContext().getString(R.string.family) + " (" + KPHUserService.sharedInstance().getFamilyAccountsCount() + ")";
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		LocalBroadcastManager.getInstance(getSafeContext()).unregisterReceiver(receiver);
		intentFilter = null;
	}

	protected void onRadioAZChecked() {
		txtRankDescription.setVisibility(View.GONE);
		refreshFamilyAccountsList(FollowListAdapter.DISPLAY_MODE_SUMMARY);
	}

	protected void onRadioRankChecked() {
		txtRankDescription.setVisibility(View.VISIBLE);
		refreshFamilyAccountsList(FollowListAdapter.DISPLAY_MODE_SEVEN_DAYS_POINTS);
	}


	public void refreshList() {
		if (familyAccountListAdapter == null)
			return;

		familyAccountListAdapter.setUserData(KPHUserService.sharedInstance().getUserData());
		familyAccountListAdapter.notifyDataSetChanged();
	}


	public void refreshFamilyAccountsList(final int displayMode) {
		Animation fadeOutAnimation = AnimationUtils.loadAnimation(getSafeContext(), R.anim.fade_out);
		fadeOutAnimation.setAnimationListener(
				new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						familyAccountListAdapter.setDisplayMode(displayMode);
						familyAccountListAdapter.notifyDataSetChanged();

						Animation fadeInAnimation = AnimationUtils.loadAnimation(getSafeContext(), R.anim.fade_in);
						familyAccountsListView.startAnimation(fadeInAnimation);
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}
				}
		);

		familyAccountsListView.startAnimation(fadeOutAnimation);
	}

	private void showNoFamilyAccountView() {
		layoutNoFamilyAccount.setVisibility(View.VISIBLE);
		layoutFamilyAccounts.setVisibility(View.GONE);
	}

	private void showFamilyAccountsListView() {
		layoutNoFamilyAccount.setVisibility(View.GONE);
		layoutFamilyAccounts.setVisibility(View.VISIBLE);
	}

	private void onNewFamilyAccountButtonClicked() {
		if (getParentActivity() == null)
			return;

		getParentActivity().pushNewActivityAnimated(OnboardingActivity.class);
	}

	private void configureFamilyAccountsList() {
		familyAccountListAdapter = new FamilyAccountListAdapter(
				getSafeContext(), KPHUserService.sharedInstance().getUserData()
		);

		if (familyAccountListAdapter.getItemCount() > 0) {
			Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_CHILDREN_SUMMARY_UPDATED);
			LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
		} else {
			showNoFamilyAccountView();
		}
	}


	private class OnFamilyAccountListItemClickListener implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			int position = (Integer) view.getTag();
			KPHUserSummary userSummary = familyAccountListAdapter.getItemAtPosition(position);
			if (userSummary == null || getParentActivity() == null)
				return;

			FriendDetailFragment frag = new FriendDetailFragment();
			frag.setData(userSummary, false);
			getParentActivity().showNewFragment(frag);
		}
	}
}
