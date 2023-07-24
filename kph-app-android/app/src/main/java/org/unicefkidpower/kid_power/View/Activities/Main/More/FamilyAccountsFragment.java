package org.unicefkidpower.kid_power.View.Activities.Main.More;

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
import android.widget.RelativeLayout;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHNotificationUtil;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserSummary;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.OnboardingActivity;
import org.unicefkidpower.kid_power.View.Adapters.FamilyAccountListAdapter;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

/**
 * Created by Ruifeng Shi on 11/10/2015.
 */
public class FamilyAccountsFragment extends SuperFragment {
	// UI Elements
	private View						contentView					= null;

	private RelativeLayout				layoutNoFamilyAccount		= null;
	private RelativeLayout				layoutFamilyAccounts		= null;

	private KPHButton					btnNewFamilyAccount			= null;
	private KPHButton					btnNew						= null;

	private RecyclerView				familyAccountsListView		= null;
	private FamilyAccountListAdapter	familyAccountListAdapter	= null;

	// Member Variables
	private IntentFilter				intentFilter				= null;
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
				case KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_USER_DATA_UPDATED: {
					if (familyAccountListAdapter.getItemCount() == 0) {
						showNoFamilyAccountView();
					} else {
						showFamilyAccountsListView();
					}

					familyAccountListAdapter.setUserData(KPHUserService.sharedInstance().getUserData());
					familyAccountListAdapter.notifyDataSetChanged();
					break;
				}
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


		// Initialize family accounts list view
		{
			familyAccountListAdapter = new FamilyAccountListAdapter(getSafeContext(), KPHUserService.sharedInstance().getUserData());
			familyAccountListAdapter.setShowDisclosure(false);
			familyAccountListAdapter.setHeaderItemCount(0);
			familyAccountListAdapter.setOnItemClickListener(new OnFamilyAccountListItemClickListener());

			familyAccountsListView = (RecyclerView) contentView.findViewById(R.id.lv_family_accounts);
			familyAccountsListView.setAdapter(familyAccountListAdapter);
			familyAccountsListView.setLayoutManager(new LinearLayoutManager(getSafeContext(), LinearLayoutManager.VERTICAL, false));

			if (familyAccountListAdapter.getItemCount() == 0) {
				showNoFamilyAccountView();
			} else {
				showFamilyAccountsListView();
			}
		}


		btnNewFamilyAccount = (KPHButton) contentView.findViewById(R.id.btn_new_family_account);
		btnNewFamilyAccount.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onNewFamilyAccountButtonClicked();
			}
		});

		btnNew = (KPHButton)contentView.findViewById(R.id.btn_new);
		btnNew.setCustomFont(getSafeContext(), getSafeContext().getString(R.string.font_pfdindisplaypro_regular));
		btnNew.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onNewFamilyAccountButtonClicked();
			}
		});

		if (KPHUserService.sharedInstance().getUserData().getUserType() != KPHUserData.USER_TYPE_CHILD)
			btnNew.setVisibility(View.VISIBLE);
		else
			btnNew.setVisibility(View.GONE);

		return contentView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_switch_family_accounts;
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();

		LocalBroadcastManager.getInstance(getSafeContext()).unregisterReceiver(receiver);
		intentFilter = null;
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


	private class OnFamilyAccountListItemClickListener implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			int position = (Integer)view.getTag();
			if (position < familyAccountListAdapter.getHeaderItemCount())
				return;

			if (getParentActivity() == null)
				return;

			KPHUserData currentUser = KPHUserService.sharedInstance().getUserData();
			KPHUserSummary selectedUser = familyAccountListAdapter.getItemAtPosition(position - familyAccountListAdapter.getHeaderItemCount());

			if (selectedUser.getId() == currentUser.getId()) {
				return;
			}

			showProgressDialog();
			KPHUserService.sharedInstance().signinOtherUser(
					selectedUser.getId(),
					new OnFamilyAccountLoginActionListener()
			);
		}

		class OnFamilyAccountLoginActionListener implements onActionListener {
			@Override
			public void completed(Object object) {
				if (getParentActivity() == null)
					return;

				dismissProgressDialog();

				if (getParentActivity() instanceof MainActivity) {
					MainActivity mainActivity = (MainActivity)getParentActivity();
					KPHNotificationUtil.sharedInstance().showSuccessNotification(mainActivity,
							getSafeContext().getString(
									R.string.logged_in_as,
									KPHUserService.sharedInstance().getUserData().getHandle()
							));
//					mainActivity.showSuccessNotification(
//							getSafeContext().getString(
//									R.string.logged_in_as,
//									KPHUserService.sharedInstance().getUserData().getHandle()
//							)
//					);
					mainActivity.restartActivity();
				}
			}

			@Override
			public void failed(int code, final String message) {
				if (getParentActivity() == null)
					return;

				getParentActivity().showAlertDialog(getSafeContext().getString(R.string.switch_account_error_title), message, null);
			}
		}
	}

}
