package org.unicefkidpower.schools;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.unicefkidpower.schools.adapter.LeaderBoardTabAdapter;
import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.model.Team;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.TeamService;

import java.util.ArrayList;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Dayong on 12/26/2016.
 */
public class LeaderBoardManager {
	private ViewGroup							container;
	private RelativeLayout 						containerLayout;
	private TabLayout 							slidingTabLayout;
	private ViewPager							contentPager;
	protected Activity							parentActivity;
	private FragmentManager						childFragmentManager;
	private LeaderBoardTabAdapter 				leaderboardPagerAdapter;

	// protected LeTeamProgressListAdapter adapter;
	private int									myTeamOrderInProgress = -1;
	private Team								team;
	private ArrayList<TeamProgress>				favoriteList = new ArrayList<>();
	private ArrayList<TeamProgress>				nearbyList = new ArrayList<>();


	public LeaderBoardManager(Activity parentActivity, FragmentManager fragmentManager, ViewGroup container, Team team) {
		this.parentActivity = parentActivity;
		this.childFragmentManager = fragmentManager;
		this.container = container;
		this.team = team;
	}

	public void refreshLeaderBoard() {
		if (containerLayout == null) {
			container.removeAllViews();

			LayoutInflater inflater = parentActivity.getLayoutInflater();
			containerLayout = (RelativeLayout) inflater.inflate(R.layout.layout_leaderboard, null);

			slidingTabLayout = (TabLayout) containerLayout.findViewById(R.id.sliding_tabbar);
			slidingTabLayout.setSelectedTabIndicatorColor(UIManager.sharedInstance().getColor(R.color.kidpower_light_blue));
			slidingTabLayout.setSelectedTabIndicatorHeight(parentActivity.getResources().getDimensionPixelSize(R.dimen.kidpower_tabindicatorheight));
			slidingTabLayout.setAlpha(0.0f);

			contentPager = (ViewPager) containerLayout.findViewById(R.id.content_pager);

			setupTabAdapter();

			container.addView(containerLayout);
		}
	}

	private void setupTabAdapter() {
		if (parentActivity == null)
			return;

		ServerManager.sharedInstance().getLeaderboardFovaritesTeams(new RestCallback<TeamService.ResLeaderboardList>() {
			@Override
			public void failure(RetrofitError retrofitError, String message) {
				if (parentActivity == null || parentActivity.getBaseContext() == null)
					return;

				AlertDialogWrapper.showErrorAlert(parentActivity.getString(R.string.teamstats_leaderboard_get_failed),
						message == null ? parentActivity.getString(R.string.error_unknown) : message, parentActivity);
			}

			@Override
			public void success(TeamService.ResLeaderboardList leaderboardList, Response response) {
				if (parentActivity == null || parentActivity.getBaseContext() == null)
					return;

				for (int i = 0; i < leaderboardList.leaderboard.size(); i++) {
					TeamService.ResLeaderboard oldItem = leaderboardList.leaderboard.get(i);

					TeamProgress newTeam = new TeamProgress();
					newTeam.rank = oldItem.rank;
					newTeam.teamName = oldItem.name;
					newTeam.schoolName = oldItem.groupName;
					newTeam.totalPackets = oldItem.totalPackets;
					newTeam.averageStepsPerStudentDay = oldItem.averageStepsPerStudentDay;
					if (team._id == oldItem._id)
						newTeam.isMyTeam = true;
					else
						newTeam.isMyTeam = false;

					favoriteList.add(newTeam);
				}

				ServerManager.sharedInstance().getLeaderboardNearbyByTeamId(team._id, new RestCallback<TeamService.ResLeaderboardList>() {
					@Override
					public void failure(RetrofitError retrofitError, String message) {
						if (parentActivity == null || parentActivity.getBaseContext() == null)
							return;

						AlertDialogWrapper.showErrorAlert(parentActivity.getString(R.string.teamstats_leaderboard_get_failed),
								message == null ? parentActivity.getString(R.string.error_unknown) : message, parentActivity);
					}

					@Override
					public void success(TeamService.ResLeaderboardList leaderboardList, Response response) {
						if (parentActivity == null || parentActivity.getBaseContext() == null)
							return;

						if (leaderboardList.leaderboard.size() > 0) {
							for (int i = 0; i < leaderboardList.leaderboard.size(); i++) {
								TeamService.ResLeaderboard oldItem = leaderboardList.leaderboard.get(i);
								TeamProgress newTeam = new TeamProgress();
								newTeam.rank = oldItem.rank;
								newTeam.teamName = oldItem.name;
								newTeam.schoolName = oldItem.groupName;
								newTeam.totalPackets = oldItem.totalPackets;
								newTeam.averageStepsPerStudentDay = oldItem.averageStepsPerStudentDay;
								if (team._id == oldItem._id)
									newTeam.isMyTeam = true;
								else
									newTeam.isMyTeam = false;

								nearbyList.add(newTeam);
							}


							leaderboardPagerAdapter = new LeaderBoardTabAdapter(childFragmentManager, favoriteList, nearbyList);

							contentPager.setAdapter(leaderboardPagerAdapter);
							contentPager.setCurrentItem(0);
							slidingTabLayout.setVisibility(View.VISIBLE);

							contentPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
								@Override
								public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
								@Override
								public void onPageSelected(int position) {}
								@Override
								public void onPageScrollStateChanged(int state) {}
							});

							setupTablayoutWithViewPager();
						}
					}
				});
			}
		});
	}

	private void setupTablayoutWithViewPager() {
		slidingTabLayout.setupWithViewPager(contentPager);
		slidingTabLayout.setAlpha(1.0f);

		Typeface typeface = Typeface.createFromAsset(parentActivity.getAssets(), "fonts/PFDinDisplayPro_regular.otf");
		ViewGroup tabLayoutContainer = (ViewGroup)slidingTabLayout.getChildAt(0);
		for (int i = 0; i < slidingTabLayout.getTabCount(); i++) {
			TextView view = (TextView) ((ViewGroup)tabLayoutContainer.getChildAt(i)).getChildAt(1);
			view.setTypeface(typeface, Typeface.BOLD);
		}
	}


	public static class TeamProgress {
		public int rank;
		public String teamName;
		public String schoolName;
		public boolean isMyTeam;
		public int totalPackets;
		public int averageStepsPerStudentDay;
	}

}
