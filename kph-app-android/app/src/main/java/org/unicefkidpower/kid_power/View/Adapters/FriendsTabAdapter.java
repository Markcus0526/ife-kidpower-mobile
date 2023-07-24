package org.unicefkidpower.kid_power.View.Adapters;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import org.unicefkidpower.kid_power.Model.Structure.KPHBlock;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserSummary;
import org.unicefkidpower.kid_power.View.Activities.Main.Friends.FriendsFamilyFragment;
import org.unicefkidpower.kid_power.View.Activities.Main.Friends.FriendsFollowerFragment;
import org.unicefkidpower.kid_power.View.Activities.Main.Friends.FriendsFollowingFragment;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;

import java.util.List;

/**
 * Created by Ruifeng Shi on 9/12/2015.
 */
public class FriendsTabAdapter extends FragmentStatePagerAdapter {
	private FriendsFollowingFragment		fragFollowing;
	private FriendsFollowerFragment			fragFollower;
	private FriendsFamilyFragment			fragFamily;

	public FriendsTabAdapter(
			FragmentManager fm,
			@NonNull List<KPHUserSummary>	followingsArray,
			@NonNull List<KPHUserSummary>	followersArray,
			@NonNull List<KPHBlock>			blockedArray
	) {
		super(fm);

		fragFollowing = new FriendsFollowingFragment();
		fragFollowing.setData(followingsArray);

		fragFollower = new FriendsFollowerFragment();
		fragFollower.setData(followersArray, blockedArray);

		fragFamily = new FriendsFamilyFragment();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
			case 0:
				return fragFollowing.getPageTitle();
			case 1:
				return fragFollower.getPageTitle();
			case 2:
				return fragFamily.getPageTitle();
			default:
				return "";
		}
	}


	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				return fragFollowing;
			case 1:
				return fragFollower;
			case 2:
				return fragFamily;
			default:
				return null;
		}
	}


	@Override
	public int getCount() {
		if (KPHUserService.sharedInstance().getFamilyAccountsCount() > 0)
			return 3;
		else
			return 2;
	}


	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
		//DO NOT REMOVE THIS METHOD (Will crash)
	}


	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		if (position >= getCount()) {
			FragmentManager manager = ((Fragment) object).getChildFragmentManager();
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.remove((Fragment) object);

			try {
				transaction.commit();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}


	public void removeAllFragments(FragmentManager fm) throws IllegalStateException {
		if (fragFollowing != null) {
			fm.beginTransaction().remove(fragFollowing).commitAllowingStateLoss();
		}

		if (fragFollower != null) {
			fm.beginTransaction().remove(fragFollower).commitAllowingStateLoss();
		}

		if (fragFamily != null) {
			fm.beginTransaction().remove(fragFamily).commitAllowingStateLoss();
		}

		notifyDataSetChanged();
	}


	public List<KPHUserSummary> getFollowingsArray() {
		return fragFollowing.getFollowingsArray();
	}
	public List<KPHUserSummary> getFollowersArray() {
		return fragFollower.getFollowersArray();
	}
	public List<KPHBlock> getBlockedArray() {
		return fragFollower.getBlockedArray();
	}


	public void notifyDataSetChanged() {
		fragFollower.refreshList();
		fragFollowing.refreshList();
		fragFamily.refreshList();
	}




}
