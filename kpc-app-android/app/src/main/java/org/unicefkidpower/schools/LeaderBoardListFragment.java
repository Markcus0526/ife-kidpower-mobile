package org.unicefkidpower.schools;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.unicefkidpower.schools.context.UserContext;
import org.unicefkidpower.schools.ui.KPTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dayong Li on 3/16/2017.
 */

public class LeaderBoardListFragment extends SuperFragment {
	public static final int							LEADERBOARD_FAVORITE = 1;
	public static final int							LEADERBOARD_NEARBY = 2;

	public List<LeaderBoardManager.TeamProgress>	leaderboardArray = new ArrayList();
	private int 									boardType =  LEADERBOARD_FAVORITE;

	private LeaderBoardListViewAdapter				listViewAdapter = null;
	private RecyclerView							progressListView = null;
	private LinearLayout							noFavoritesLayout = null;
	private LayoutInflater							layoutInflater = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		layoutInflater = inflater;

		progressListView = (RecyclerView) rootView.findViewById(R.id.progressListVIew);
		listViewAdapter = new LeaderBoardListViewAdapter();

		progressListView.setAdapter(listViewAdapter);
		progressListView.setLayoutManager(new LinearLayoutManager(getSafeContext(), LinearLayoutManager.VERTICAL, false));

		noFavoritesLayout = (LinearLayout)rootView.findViewById(R.id.no_favorites_layout);
		if (leaderboardArray.size() == 0 && boardType == LEADERBOARD_FAVORITE) {
			noFavoritesLayout.setVisibility(View.VISIBLE);
			progressListView.setVisibility(View.GONE);
		} else {
			noFavoritesLayout.setVisibility(View.GONE);
			progressListView.setVisibility(View.VISIBLE);
		}

		return rootView;
	}

	@Override
	public int contentLayout() {
		return R.layout.layout_leaderboard_list;
	}

	@Override
	protected boolean isUseEvent() {
		return false;
	}


	public void setData(int boardType, List<LeaderBoardManager.TeamProgress> leaderboardArray) {
		this.boardType = boardType;
		this.leaderboardArray = leaderboardArray;
	}


	public String getPageTitle() {
		int arraySize = leaderboardArray != null ? leaderboardArray.size() : 0;

		if (boardType == LEADERBOARD_FAVORITE) {
			return getSafeContext().getResources().getString(R.string.favorite_title) + "(" + arraySize + ")";
		} else {
			return getSafeContext().getResources().getString(R.string.nearby_title) + "(" + arraySize + ")";
		}
	}


	public List<LeaderBoardManager.TeamProgress> getLeaderBoardArray() {
		return this.leaderboardArray;
	}

	public void refreshList() {
		if (listViewAdapter != null)
			listViewAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean shouldUpdateNavigationBar() {
		return false;
	}

	public class LeaderBoardListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private static final int VIEW_TYPE_HEADER		= 1;
		private static final int VIEW_TYPE_ITEM			= 2;

		public LeaderBoardListViewAdapter() {
			super();
		}

		@Override
		public int getItemCount() {
			return leaderboardArray.size() + 1;
		}

		@Override
		public int getItemViewType(int position) {
			if (position == 0) {
				return VIEW_TYPE_HEADER;
			} else {
				return VIEW_TYPE_ITEM;
			}
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			RecyclerView.ViewHolder holder = null;

			if (viewType == VIEW_TYPE_HEADER) {
				View headerView = layoutInflater.inflate(R.layout.item_teamprogress_header_layout, null);
				holder = new HeaderViewHolder(headerView);
			} else {
				View itemView = layoutInflater.inflate(R.layout.item_teamprogress_layout, null);
				holder = new ItemViewHolder(itemView);
			}

			return holder;
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
			if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
				((ItemViewHolder)holder).setProgress(leaderboardArray.get(position - 1));
			}
		}


		public class HeaderViewHolder extends RecyclerView.ViewHolder {
			public HeaderViewHolder(View itemView) {
				super(itemView);
			}
		}

		public class ItemViewHolder extends RecyclerView.ViewHolder {
			public KPTextView		rankTextView;
			public KPTextView		teamNameTextView;
			public KPTextView		schoolNameTextView;
			public KPTextView		stepsTextView;
			public KPTextView		packetsTextView;
			public RelativeLayout	myteamPointer;

			public ItemViewHolder(View itemView) {
				super(itemView);

				rankTextView = (KPTextView)itemView.findViewById(R.id.txt_rank);
				teamNameTextView = (KPTextView)itemView.findViewById(R.id.txt_team);
				schoolNameTextView = (KPTextView)itemView.findViewById(R.id.txt_school);
				stepsTextView = (KPTextView)itemView.findViewById(R.id.txt_steps);
				packetsTextView = (KPTextView)itemView.findViewById(R.id.txt_packets);
				myteamPointer = (RelativeLayout)itemView.findViewById(R.id.team_pointer);
			}

			public void setProgress(LeaderBoardManager.TeamProgress progress) {
				if (progress.rank != -1) {
					rankTextView.setText(String.format("%s:", getRankAsString(progress.rank)));
					teamNameTextView.setText(progress.teamName);
					schoolNameTextView.setText(progress.schoolName);
					stepsTextView.setText("" + progress.averageStepsPerStudentDay);
					packetsTextView.setText("" + progress.totalPackets);

					if (progress.isMyTeam) {
						myteamPointer.setVisibility(View.VISIBLE);
					} else {
						myteamPointer.setVisibility(View.INVISIBLE);
					}

					teamNameTextView.setVisibility(View.VISIBLE);
					schoolNameTextView.setVisibility(View.VISIBLE);
					stepsTextView.setVisibility(View.VISIBLE);
					packetsTextView.setVisibility(View.VISIBLE);
				} else {
					rankTextView.setText("...");
					myteamPointer.setVisibility(View.INVISIBLE);

					teamNameTextView.setVisibility(View.INVISIBLE);
					schoolNameTextView.setVisibility(View.INVISIBLE);
					stepsTextView.setVisibility(View.INVISIBLE);
					packetsTextView.setVisibility(View.INVISIBLE);

					itemView.setBackgroundColor(Color.TRANSPARENT);
				}
			}

			private String getRankAsString(int rank) {
				String ret;
				if (UserContext.sharedInstance().getAppLocale().getLanguage().equalsIgnoreCase("nl")) {
					if (rank <= 0) {
						ret = "";
					} else if (rank == 1 ||
							rank == 8 ||
							rank == 11 ||
							rank == 12 ||
							rank >= 20) {
						ret = "" + rank + "ste";
					} else {
						ret = "" + rank + "de";
					}
				} else {
					// US, UK ranking
					if (rank <= 0) {
						ret = "";
					} else if (rank % 10 == 1) {
						ret = "" + rank + "st";
					} else if (rank % 10 == 2) {
						ret = "" + rank + "nd";
					} else if (rank % 10 == 3) {
						ret = "" + rank + "rd";
					} else {
						ret = String.format(_parentActivity.getString(R.string.teamstats_format_rank), rank);
					}
				}

				return ret;
			}
		}

	}

}
