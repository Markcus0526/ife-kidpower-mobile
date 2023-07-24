package org.unicefkidpower.schools;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.helper.DebouncedOnClickListener;
import org.unicefkidpower.schools.model.Team;
import org.unicefkidpower.schools.model.TeamManager;
import org.unicefkidpower.schools.model.UserManager;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.TeamService;

import java.util.ArrayList;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Dayong on 8/30/2016.
 */
public class TeamsFragment extends SuperFragment {
	protected RelativeLayout rlCreateTeamHint;
	protected RelativeLayout rlCreateTeamButton;

	protected ScrollView mainScrollView;
	protected ArrayList<Team> _teams;
	protected LinearLayout llTeamsContainer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		FlurryAgent.onStartSession(getContext(), "Team Fragment");

		mainScrollView = (ScrollView) rootView.findViewById(R.id.scrollTeams);
		mainScrollView.setVisibility(View.GONE);
		llTeamsContainer = (LinearLayout) rootView.findViewById(R.id.llTeamsContainer);

		rlCreateTeamHint = (RelativeLayout) rootView.findViewById(R.id.rlCreateTeamHint);
		rlCreateTeamHint.setVisibility(View.GONE);
		rlCreateTeamButton = (RelativeLayout) rootView.findViewById(R.id.rlCreateButton);
		rlCreateTeamButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedCreateTeamButton();
			}
		});


		refreshTeams();

		return rootView;
	}

	@Override
	protected String getFragmentTitle() {
		return getString(R.string.teams_title);
	}

	@Override
	protected boolean isUseEvent() {
		return false;
	}

	public void getTeams() {
		if (UserManager.sharedInstance()._currentUser == null) {
			return;
		}

		// try to get teams
		UIManager.sharedInstance().showProgressDialog(getActivity(), null, getString(R.string.teams_loading_team), true);
		ServerManager.sharedInstance().getAllTeamsByUserId(UserManager.sharedInstance()._currentUser._id,
				new RestCallback<TeamService.ResGetAllTeamByUserId>() {
					@Override
					public void success(TeamService.ResGetAllTeamByUserId resTeams, Response response) {
						UIManager.sharedInstance().dismissProgressDialog();
						_teams = TeamManager.sharedInstance().parseTeamArrayForResGetAllTeamsByUserId(resTeams);
						showTeams(_teams);

						if (_parentActivity != null) {
							MainActivity mainActivity = (MainActivity) _parentActivity;
							mainActivity.loadedTeams(_teams);
						}
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						UIManager.sharedInstance().dismissProgressDialog();
						AlertDialogWrapper.showErrorAlert(getString(R.string.teams_loadingteam_failed),
								message == null ? getActivity().getString(R.string.error_unknown) : message, getActivity());
					}
				});
	}

	@Override
	public int contentLayout() {
		return R.layout.layout_teams;
	}

	protected void showTeams(ArrayList<Team> teams) {
		if (teams.size() == 0) {
			rlCreateTeamHint.setVisibility(View.VISIBLE);
			mainScrollView.setVisibility(View.GONE);
		} else {
			rlCreateTeamHint.setVisibility(View.GONE);
			mainScrollView.setVisibility(View.VISIBLE);

			llTeamsContainer.removeAllViews();
			for (int i = 0; i < teams.size(); i += 3) {
				// add linear layout
				// create one horz layout
				LinearLayout layout = new LinearLayout(getActivity(), null);
				layout.setOrientation(LinearLayout.HORIZONTAL);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				layout.setLayoutParams(params);
				layout.setWeightSum(3f);


				RelativeLayout[] rl = new RelativeLayout[3];

				for (int j = 0; j < 3; j++) {
					rl[j] = new RelativeLayout(getActivity());
					LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
					params1.weight = 1f;
					rl[j].setLayoutParams(params1);
					layout.addView(rl[j]);
					rl[j].setGravity(Gravity.CENTER_HORIZONTAL);

					LayoutInflater inflater = _inflater;

					if (i + j < teams.size()) {
						View parentView = inflater.inflate(R.layout.layout_team_item, null);
						rl[j].addView(parentView);
						Team team = teams.get(i + j);
						bind(team, parentView);
					}
				}

				llTeamsContainer.addView(layout);
			}
		}
	}

	protected void refreshTeams() {
		_teams = new ArrayList<>();

		showTeams(_teams);
		getTeams();
	}

	protected void bind(Team team, View parentView) {
		ViewHolderForTeam viewHolderForTeam = new ViewHolderForTeam(parentView);
		viewHolderForTeam.rlCoachFlag.setVisibility(View.VISIBLE);
		viewHolderForTeam.textName.setText(team._name);

		parentView.setTag(team);
		parentView.setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				Object tag = v.getTag();
				if (tag instanceof Team) {
					Team team = (Team) tag;
					MainActivity activity = (MainActivity) getActivity();
					activity.goTeamStatsFragment(team);
				}
			}
		});
	}

	private static class ViewHolderForTeam {
		public final RelativeLayout rlCoachFlag;
		public final ImageView ivAvatar;
		public final TextView textName;

		public ViewHolderForTeam(View parentView) {
			rlCoachFlag = (RelativeLayout) parentView.findViewById(R.id.rlCoachFlag);
			ivAvatar = (ImageView) parentView.findViewById(R.id.ivAvatar);
			textName = (TextView) parentView.findViewById(R.id.textName);
		}
	}

	private void onClickedCreateTeamButton() {
		((MainActivity) _parentActivity).showNewTeam();
	}

}