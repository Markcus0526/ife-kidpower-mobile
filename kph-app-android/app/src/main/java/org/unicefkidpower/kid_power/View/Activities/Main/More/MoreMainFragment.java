package org.unicefkidpower.kid_power.View.Activities.Main.More;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Model.MicroService.KPHAnalyticsService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.Model.Structure.KPHLogoutResult;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.AboutActivity;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.EditProfileFragment;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.WelcomeActivity;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit.client.Response;

/**
 * Created by Ruifeng Shi on 9/4/2015.
 */
public class MoreMainFragment extends SuperFragment {
	private View					contentView			= null;

	private RecyclerView			moreListView		= null;
	private MoreItemAdapter			moreListAdapter		= new MoreItemAdapter();

	private ArrayList<String>		itemsList			= null;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_UI_MORE);

		// initialize list view item
		itemsList = new ArrayList<>(Arrays.asList(
				getString(R.string.profile),
				getString(R.string.family_accounts),
				getString(R.string.help),
				getString(R.string.privacy_policy),
				getString(R.string.log_out))
		);

		// find list view
		moreListView = (RecyclerView) contentView.findViewById(R.id.list_more);
		moreListView.setAdapter(moreListAdapter);
		moreListView.setHasFixedSize(true);
		moreListView.setLayoutManager(new LinearLayoutManager(getSafeContext(), LinearLayoutManager.VERTICAL, false));

		dismissProgressDialog();

		return contentView;
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_more;
	}


	public void onClickedProfile() {
		EditProfileFragment frag = new EditProfileFragment();
		getParentActivity().showNewFragment(frag);
	}


	public void onClickedChildAccount() {
		FamilyAccountsFragment fragFamilyAccounts = new FamilyAccountsFragment();
		getParentActivity().showNewFragment(fragFamilyAccounts);
	}


	public void onClickedAbout() {
		if (getParentActivity() != null)
			getParentActivity().pushNewActivityAnimated(AboutActivity.class);
	}


	public void onClickedPrivacyPolicy() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(KPHConstants.URL_PRIVACY_POLICY));
		getParentActivity().startActivity(intent);
	}


	public void onClickedHelp() {
		HelpMainFragment frag = new HelpMainFragment();
		getParentActivity().showNewFragment(frag);
	}


	public void onClickedLogOut() {
		//Show progress dialog
		showProgressDialog(getSafeContext().getString(R.string.signing_out));

		KPHUserData userData = KPHUserService.sharedInstance().getUserData();
		RestService.get().logout(
				userData.getId(),
				new RestCallback<KPHLogoutResult>() {
			@Override
			public void failure(RestError restError) {
				showErrorDialog(getString(R.string.logout_failed));
			}

			@Override
			public void success(KPHLogoutResult result, Response response) {
				dismissProgressDialog();

				if (result != null &&
						result.getStatus() != null &&
						result.getStatus().equals(KPHConstants.LOGOUT_SUCCESS)) {
					KPHUserService.sharedInstance().clearLoginData();
					KPHUserService.sharedInstance().clearUserData();
					KPHUserService.sharedInstance().clearCatchTrackerDialogDate();

					getParentActivity().pushNewActivityAnimated(
							WelcomeActivity.class,
							SuperActivity.AnimConst.ANIMDIR_FROM_LEFT
					);
					getParentActivity().popOverCurActivityAnimated();
				} else {
					showErrorDialog(getString(R.string.logout_failed));
				}
			}
		});
	}

	/*
	Class for More Items Adapter
	 */
	public class MoreItemAdapter extends RecyclerView.Adapter<MoreItemAdapter.ViewHolder> {
		@Override
		public MoreItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View itemView = getParentActivity().getLayoutInflater().inflate(R.layout.item_morecell, null);

			MoreItemAdapter.ViewHolder holder = new MoreItemAdapter.ViewHolder(itemView);
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					String sItemName = (String)view.getTag();

					if (sItemName.equalsIgnoreCase(getSafeContext().getString(R.string.profile))) {
						onClickedProfile();
					} else if (sItemName.equalsIgnoreCase(getSafeContext().getString(R.string.family_accounts))) {
						onClickedChildAccount();
					} else if (sItemName.equalsIgnoreCase(getSafeContext().getString(R.string.about))) {
						onClickedAbout();
					} else if (sItemName.equalsIgnoreCase(getSafeContext().getString(R.string.help))) {
						onClickedHelp();
					} else if (sItemName.equalsIgnoreCase(getSafeContext().getString(R.string.privacy_policy))) {
						onClickedPrivacyPolicy();
					} else if (sItemName.equalsIgnoreCase(getSafeContext().getString(R.string.log_out))) {
						onClickedLogOut();
					}
				}
			});

			return holder;
		}


		@Override
		public void onBindViewHolder(MoreItemAdapter.ViewHolder holder, int position) {
			holder.textView.setText(itemsList.get(position));
			holder.contentView.setTag(itemsList.get(position));
		}


		@Override
		public int getItemCount() {
			if (itemsList == null)
				return 0;

			return itemsList.size();
		}


		public class ViewHolder extends RecyclerView.ViewHolder {
			public View contentView;
			public KPHTextView textView;

			public ViewHolder(View itemView) {
				super(itemView);

				contentView = itemView;
				textView = (KPHTextView)itemView.findViewById(R.id.txtItemName);
			}
		}

	}
}
