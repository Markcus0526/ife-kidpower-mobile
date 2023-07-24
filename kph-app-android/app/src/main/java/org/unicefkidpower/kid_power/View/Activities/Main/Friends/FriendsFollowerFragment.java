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

import org.unicefkidpower.kid_power.Model.Structure.KPHBlock;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserSummary;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Adapters.FollowListAdapter;
import org.unicefkidpower.kid_power.View.CustomControls.KPHSegmentedGroup;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ruifeng Shi on 9/12/2015.
 */
public class FriendsFollowerFragment extends SuperFragment implements RadioGroup.OnCheckedChangeListener {
	// UI Elements
	private View						contentView							= null;
	private RelativeLayout				layoutFollowersList					= null;
	private RecyclerView				followersListView					= null;
	private LinearLayout				layoutNoFollowers					= null;
	private KPHSegmentedGroup			sortingMethodSegmentedGroup			= null;
	private KPHTextView					rankDescriptionTextView				= null;

	// Member Variables
	private List<KPHUserSummary>		followersArray				= new ArrayList<>();
	private List<KPHBlock>				blockedArray				= new ArrayList<>();
	private FollowListAdapter			mFollowerListAdapter		= null;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		layoutFollowersList = (RelativeLayout) contentView.findViewById(R.id.layout_follower_list);
		followersListView = (RecyclerView) contentView.findViewById(R.id.listview_followers);
		layoutNoFollowers = (LinearLayout) contentView.findViewById(R.id.layout_no_followers);
		rankDescriptionTextView = (KPHTextView) contentView.findViewById(R.id.txt_rank_description);
		sortingMethodSegmentedGroup = (KPHSegmentedGroup) contentView.findViewById(R.id.segmentedSort);

		sortingMethodSegmentedGroup.setOnCheckedChangeListener(this);

		if (followersArray.size() > 0 || blockedArray.size() > 0) {
			mFollowerListAdapter = new FollowListAdapter(getSafeContext(), followersArray, blockedArray);
			mFollowerListAdapter.setOnItemClickedListener(new OnFollowerListItemClickListener());

			followersListView.setAdapter(mFollowerListAdapter);
			followersListView.setLayoutManager(new LinearLayoutManager(getSafeContext(), LinearLayoutManager.VERTICAL, false));

			layoutFollowersList.setVisibility(View.VISIBLE);
			layoutNoFollowers.setVisibility(View.GONE);
		} else {
			layoutFollowersList.setVisibility(View.GONE);
			layoutNoFollowers.setVisibility(View.VISIBLE);
		}

		return contentView;
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_friends_followers;
	}


	public String getPageTitle() {
		int followersArraySize = followersArray != null ? followersArray.size() : 0;
		int blockedArraySize = blockedArray != null ? blockedArray.size() : 0;

		return getSafeContext().getString(R.string.followers) + " (" + (followersArraySize + blockedArraySize) + ")";
	}


	public void setData(List<KPHUserSummary> followersArray, List<KPHBlock> blockedArray) {
		if (followersArray != null)
			this.followersArray = followersArray;

		if (blockedArray != null)
			this.blockedArray = blockedArray;
	}


	public List<KPHUserSummary> getFollowersArray() {
		return this.followersArray;
	}

	public List<KPHBlock> getBlockedArray() {
		return this.blockedArray;
	}


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

	protected void onRadioAZChecked() {
		rankDescriptionTextView.setVisibility(View.GONE);
		refreshFollowersList(FollowListAdapter.DISPLAY_MODE_SUMMARY);
	}

	protected void onRadioRankChecked() {
		rankDescriptionTextView.setVisibility(View.VISIBLE);
		refreshFollowersList(FollowListAdapter.DISPLAY_MODE_SEVEN_DAYS_POINTS);
	}

	public void refreshList() {
		if (mFollowerListAdapter != null)
			mFollowerListAdapter.notifyDataSetChanged();
	}

	public void refreshFollowersList(final int displayMode) {
		Animation fadeOutAnimation = AnimationUtils.loadAnimation(getSafeContext(), R.anim.fade_out);

		fadeOutAnimation.setAnimationListener(
				new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {}
					@Override
					public void onAnimationEnd(Animation animation) {
						mFollowerListAdapter.setDisplayMode(displayMode);
						mFollowerListAdapter.notifyDataSetChanged();

						Animation fadeInAnimation = AnimationUtils.loadAnimation(getSafeContext(), R.anim.fade_in);
						followersListView.startAnimation(fadeInAnimation);
					}
					@Override
					public void onAnimationRepeat(Animation animation) {}
				}
		);

		followersListView.startAnimation(fadeOutAnimation);
	}

	private class OnFollowerListItemClickListener implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			int position = (Integer)view.getTag();

			if (position < followersArray.size()) {
				KPHUserSummary userSummary = mFollowerListAdapter.getItem(position);
				if (userSummary == null || getParentActivity() == null)
					return;

				FriendDetailFragment frag = new FriendDetailFragment();
				frag.setData(userSummary, true);
				getParentActivity().showNewFragment(frag);
			} else {
				if (getParentActivity() == null)
					return;

				FollowBlockedFragment frag = new FollowBlockedFragment();
				frag.setBlockedArray(blockedArray);
				getParentActivity().showNewFragment(frag);
			}
		}
	}
}
