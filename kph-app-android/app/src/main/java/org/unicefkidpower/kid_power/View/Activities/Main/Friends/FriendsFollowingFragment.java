package org.unicefkidpower.kid_power.View.Activities.Main.Friends;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import org.unicefkidpower.kid_power.Model.Structure.KPHUserSummary;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Adapters.FollowListAdapter;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHSegmentedGroup;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ruifeng Shi on 9/12/2015.
 */
public class FriendsFollowingFragment extends SuperFragment {
	// UI Controls
	private View						contentView					= null;
	private RecyclerView				followingList				= null;
	private RelativeLayout				layoutFollowingList			= null;
	private LinearLayout				layoutNoFollowing			= null;
	private KPHButton					btnFollowSomeone1			= null;
	private KPHButton					btnFollowSomeone2			= null;
	private KPHSegmentedGroup			sgSortingMethod				= null;
	private KPHTextView					txtRankDescription			= null;

	// Member Variables
	private List<KPHUserSummary>		followingsArray				= new ArrayList<>();
	private FollowListAdapter			mFollowingListAdapter		= null;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		layoutFollowingList = (RelativeLayout) contentView.findViewById(R.id.layout_following_list);
		followingList = (RecyclerView) contentView.findViewById(R.id.listview_following);

		layoutNoFollowing = (LinearLayout) contentView.findViewById(R.id.layout_no_following);

		btnFollowSomeone1 = (KPHButton) contentView.findViewById(R.id.btnFollowSomeone1);
		btnFollowSomeone1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onFollowSomeoneButtonClicked();
			}
		});

		btnFollowSomeone2 = (KPHButton) contentView.findViewById(R.id.btnFollowSomeone2);
		btnFollowSomeone2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onFollowSomeoneButtonClicked();
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

		if (followingsArray.size() > 0) {
			mFollowingListAdapter = new FollowListAdapter(getSafeContext(), followingsArray, null);
			mFollowingListAdapter.setOnItemClickedListener(new OnFollowingListItemClickListener());

			followingList.setAdapter(mFollowingListAdapter);
			followingList.setHasFixedSize(true);
			followingList.setLayoutManager(new LinearLayoutManager(getSafeContext(), LinearLayoutManager.VERTICAL, false));

			layoutFollowingList.setVisibility(View.VISIBLE);
			layoutNoFollowing.setVisibility(View.GONE);
		} else {
			layoutFollowingList.setVisibility(View.GONE);
			layoutNoFollowing.setVisibility(View.VISIBLE);
		}

		return contentView;
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_friends_following;
	}

	public String getPageTitle() {
		int followingsArraySize = followingsArray != null ? followingsArray.size() : 0;
		return getSafeContext().getResources().getString(R.string.following) + " (" + followingsArraySize + ")";
	}


	public void setData(List<KPHUserSummary> followingsArray) {
		this.followingsArray = followingsArray;
	}

	public List<KPHUserSummary> getFollowingsArray() {
		return this.followingsArray;
	}


	private void onFollowSomeoneButtonClicked() {
		if (getParentActivity() != null)
			getParentActivity().showNewFragment(new FollowSomeoneFragment());
	}

	protected void onRadioAZChecked() {
		txtRankDescription.setVisibility(View.GONE);
		refreshFollowingsList(FollowListAdapter.DISPLAY_MODE_SUMMARY);
	}


	protected void onRadioRankChecked() {
		txtRankDescription.setVisibility(View.VISIBLE);
		refreshFollowingsList(FollowListAdapter.DISPLAY_MODE_SEVEN_DAYS_POINTS);
	}

	public void refreshList() {
		if (mFollowingListAdapter != null)
			mFollowingListAdapter.notifyDataSetChanged();
	}

	protected void refreshFollowingsList(final int displayMode) {
		Animation fadeOutAnimation = AnimationUtils.loadAnimation(getSafeContext(), R.anim.fade_out);

		fadeOutAnimation.setAnimationListener(
				new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {}
					@Override
					public void onAnimationEnd(Animation animation) {
						mFollowingListAdapter.setDisplayMode(displayMode);
						mFollowingListAdapter.notifyDataSetChanged();

						Animation fadeInAnimation = AnimationUtils.loadAnimation(getSafeContext(), R.anim.fade_in);
						followingList.startAnimation(fadeInAnimation);
					}
					@Override
					public void onAnimationRepeat(Animation animation) {}
				}
		);

		followingList.startAnimation(fadeOutAnimation);
	}


	private class OnFollowingListItemClickListener implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			int position = (Integer)view.getTag();
			if (position < followingsArray.size() && getParentActivity() != null) {
				KPHUserSummary userSummary = mFollowingListAdapter.getItem(position);
				if (userSummary == null)
					return;

				FriendDetailFragment frag = new FriendDetailFragment();
				frag.setData(userSummary, false);
				getParentActivity().showNewFragment(frag);
			}
		}
	}
}
