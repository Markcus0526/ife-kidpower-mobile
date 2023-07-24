package org.unicefkidpower.schools;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.schools.context.UserContext;
import org.unicefkidpower.schools.event.EventManager;
import org.unicefkidpower.schools.event.SEvent;
import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.model.Team;
import org.unicefkidpower.schools.model.TeamManager;
import org.unicefkidpower.schools.model.UserManager;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.TeamService;
import org.unicefkidpower.schools.ui.KPEditText;
import org.unicefkidpower.schools.ui.KPImageTextButton;
import org.unicefkidpower.schools.ui.SegmentedGroup;
import org.unicefkidpower.schools.ui.UiUtils;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Ruifeng Shi on 9/5/2016.
 */
public class TeamAddFragment extends SuperFragment implements View.OnClickListener {
	private static final String EVENT_POSTTEAM_SUCCESS = "EVENT_COACHSETTINGS_POSTTEAM_SUCCESS";
	private static final String EVENT_POSTTEAM_FAILURE = "EVENT_COACHSETTINGS_POSTTEAM_FAILURE";

	private KPEditText editTeamName, editTeamGrade;
	private SegmentedGroup segmentedTeamGrade;
	private KPImageTextButton btnCreateTeam;

	private String szTeamGrade = CreateTeamFragment.getTeamGradeString(3);

	public static TeamAddFragment newInstance() {
		return new TeamAddFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		editTeamName = (KPEditText) rootView.findViewById(R.id.edit_team_name);
		editTeamGrade = (KPEditText) rootView.findViewById(R.id.edit_team_grade);

		segmentedTeamGrade = (SegmentedGroup) rootView.findViewById(R.id.segmented_grades);

		btnCreateTeam = (KPImageTextButton) rootView.findViewById(R.id.btn_create_team);
		btnCreateTeam.setGravity(Gravity.CENTER);
		btnCreateTeam.setOnClickListener(this);

		View sixthGradeView = rootView.findViewById(R.id.radio_sixth);
		View seventhGradeView = rootView.findViewById(R.id.radio_seventh);
		View eighthGradeView = rootView.findViewById(R.id.radio_eighth);
		if (UserContext.sharedInstance().getAppLocale().getLanguage().equalsIgnoreCase("nl")) {
			sixthGradeView.setVisibility(View.VISIBLE);
			seventhGradeView.setVisibility(View.GONE);
			eighthGradeView.setVisibility(View.GONE);
		} else if (UserContext.sharedInstance().getAppLocale().getCountry().equalsIgnoreCase("gb") ||
				UserContext.sharedInstance().getAppLocale().getCountry().equalsIgnoreCase("uk")) {
			sixthGradeView.setVisibility(View.GONE);
			seventhGradeView.setVisibility(View.GONE);
			eighthGradeView.setVisibility(View.GONE);
		} else {
			sixthGradeView.setVisibility(View.VISIBLE);
			seventhGradeView.setVisibility(View.VISIBLE);
			eighthGradeView.setVisibility(View.VISIBLE);
		}

		segmentedTeamGrade.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				editTeamGrade.setVisibility(View.GONE);

				switch (checkedId) {
					case R.id.radio_third:
						szTeamGrade = CreateTeamFragment.getTeamGradeString(3);
						break;

					case R.id.radio_forth:
						szTeamGrade = CreateTeamFragment.getTeamGradeString(4);
						break;

					case R.id.radio_fifth:
						szTeamGrade = CreateTeamFragment.getTeamGradeString(5);
						break;

					case R.id.radio_sixth:
						szTeamGrade = CreateTeamFragment.getTeamGradeString(6);
						break;

					case R.id.radio_seventh:
						szTeamGrade = CreateTeamFragment.getTeamGradeString(7);
						break;

					case R.id.radio_eighth:
						szTeamGrade = CreateTeamFragment.getTeamGradeString(8);
						break;

					case R.id.radio_other:
						szTeamGrade = "o";
						if (UserContext.sharedInstance().getAppLocale().getLanguage().equalsIgnoreCase("nl") ||
								UserContext.sharedInstance().getAppLocale().getCountry().equalsIgnoreCase("gb") ||
								UserContext.sharedInstance().getAppLocale().getCountry().equalsIgnoreCase("uk")) {
							editTeamGrade.setVisibility(View.GONE);
						} else {
							editTeamGrade.setVisibility(View.VISIBLE);
						}
						break;
				}
			}
		});

		return rootView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_team_add;
	}

	@Override
	protected String getFragmentTitle() {
		return _parentActivity.getString(R.string.new_team);
	}

	@Override
	protected boolean isUseEvent() {
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_create_team:
				onCreateTeamButtonClicked();
				break;
		}
	}

	private void onCreateTeamButtonClicked() {
		UiUtils.hideKeyboard(getActivity());

		// try to login
		String szName = editTeamName.getText().toString();

		if (TextUtils.isEmpty(szTeamGrade))
			szTeamGrade = editTeamGrade.getText().toString();

		UIManager.sharedInstance().showProgressDialog(getActivity(), null, getString(R.string.app_onemoment), true);
		ServerManager.sharedInstance().createTeam(
				szName,
				UserManager.sharedInstance()._currentUser._id,
				szTeamGrade,
				new RestCallback<TeamService.ResCreateTeam>() {
					@Override
					public void success(TeamService.ResCreateTeam resPostTeam, Response response) {
						UIManager.sharedInstance().dismissProgressDialog();
						((MainActivity) _parentActivity).onBackClicked();

						// set current user
						Team team = TeamManager.sharedInstance().parseTeamFromResCreateTeam(resPostTeam);
						UserManager.sharedInstance()._currentUser._teams.add(team);
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						UIManager.sharedInstance().dismissProgressDialog();
						//String errorMsg = ServerManager.getErrorMessageForRetrofitError(retrofitError, getActivity());
						EventManager.sharedInstance().post(EVENT_POSTTEAM_FAILURE, message);
					}
				}
		);
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent e) {
		if (EVENT_POSTTEAM_SUCCESS.equals(e.name)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_success),
					getString(R.string.coachsettings_newteam_added), getActivity());
		} else if (EVENT_POSTTEAM_FAILURE.equals(e.name)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.coachsettings_newteam_added_failed) + " : " +
							(e.object == null ? getString(R.string.error_unknown) : e.object),
					getActivity());
		}
	}
}
