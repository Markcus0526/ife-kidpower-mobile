package org.unicefkidpower.schools.adapter;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

import org.unicefkidpower.schools.LeaderBoardListFragment;
import org.unicefkidpower.schools.LeaderBoardManager;

import java.util.List;

import static org.unicefkidpower.schools.LeaderBoardListFragment.LEADERBOARD_FAVORITE;
import static org.unicefkidpower.schools.LeaderBoardListFragment.LEADERBOARD_NEARBY;

/**
 * Created by Dayong Li on 3/15/2017.
 */

public class LeaderBoardTabAdapter extends FragmentStatePagerAdapter {
	private LeaderBoardListFragment				favoriteFragment;
	private LeaderBoardListFragment				nearbyFragment;

	public LeaderBoardTabAdapter(
			FragmentManager fragmentManager,
			@NonNull List<LeaderBoardManager.TeamProgress> favoritesArray,
			@NonNull List<LeaderBoardManager.TeamProgress> leaderboardArray
	) {
		super(fragmentManager);

		favoriteFragment = new LeaderBoardListFragment();
		favoriteFragment.setData(LEADERBOARD_FAVORITE, favoritesArray);

		nearbyFragment = new LeaderBoardListFragment();
		nearbyFragment.setData(LEADERBOARD_NEARBY, leaderboardArray);
	}


	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
			case 0:
				return favoriteFragment.getPageTitle();
			case 1:
				return nearbyFragment.getPageTitle();
			default:
				return "";
		}
	}


	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				return favoriteFragment;
			case 1:
				return nearbyFragment;
			default:
				return null;
		}
	}


	@Override
	public int getCount() {
		return 2;
	}


	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
		// DO NOT REMOVE THIS METHOD (Will crash)
	}


	@Override
	public boolean isViewFromObject(View view, Object object) {
		return ((Fragment)object).getView() == view;
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

	public List<LeaderBoardManager.TeamProgress> getFavoriteArray() {
		return favoriteFragment.getLeaderBoardArray();
	}
	public List<LeaderBoardManager.TeamProgress> getFollowersArray() {
		return nearbyFragment.getLeaderBoardArray();
	}

	public void notifyDataSetChanged() {
		favoriteFragment.refreshList();
		nearbyFragment.refreshList();
	}
}
