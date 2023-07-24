package org.unicefkidpower.schools;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.flurry.android.FlurryAgent;

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
import org.unicefkidpower.schools.ui.KPViewPager;
import org.unicefkidpower.schools.ui.SegmentedGroup;
import org.unicefkidpower.schools.ui.UiUtils;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class CreateTeamFragment extends SuperFragment implements View.OnClickListener {
	private static final String EVENT_POSTTEAM_SUCCESS = "EVENT_COACHSETTINGS_POSTTEAM_SUCCESS";
	private static final String EVENT_POSTTEAM_FAILURE = "EVENT_COACHSETTINGS_POSTTEAM_FAILURE";

	public final static String TAG = CreateTeamFragment.class.getSimpleName();
	private KPViewPager mViewPager;

	private KPEditText editTeamName, editTeamGrade;
	private SegmentedGroup segmentedTeamGrade;

	private String szTeamGrade = getTeamGradeString(3);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		FlurryAgent.onStartSession(getContext(), TAG);

		rootView.findViewById(R.id.btn_back).setOnClickListener(this);
		rootView.findViewById(R.id.btn_continue).setOnClickListener(this);

		mViewPager = (KPViewPager) getActivity().findViewById(R.id.onboarding_pager);

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

		editTeamName = (KPEditText) rootView.findViewById(R.id.edit_team_name);
		editTeamGrade = (KPEditText) rootView.findViewById(R.id.edit_team_grade);
		segmentedTeamGrade = (SegmentedGroup) rootView.findViewById(R.id.segmented_grades);
		segmentedTeamGrade.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				editTeamGrade.setVisibility(View.INVISIBLE);

				switch (checkedId) {
					case R.id.radio_third:
						szTeamGrade = getTeamGradeString(3);
						break;

					case R.id.radio_forth:
						szTeamGrade = getTeamGradeString(4);
						break;

					case R.id.radio_fifth:
						szTeamGrade = getTeamGradeString(5);
						break;

					case R.id.radio_sixth:
						szTeamGrade = getTeamGradeString(6);
						break;

					case R.id.radio_seventh:
						szTeamGrade = getTeamGradeString(7);
						break;

					case R.id.radio_eighth:
						szTeamGrade = getTeamGradeString(8);
						break;

					case R.id.radio_other:
						szTeamGrade = "o";
						if (UserContext.sharedInstance().getAppLocale().getLanguage().equalsIgnoreCase("nl") ||
								UserContext.sharedInstance().getAppLocale().getCountry().equalsIgnoreCase("gb") ||
								UserContext.sharedInstance().getAppLocale().getCountry().equalsIgnoreCase("uk")) {
							editTeamGrade.setVisibility(View.INVISIBLE);
						} else {
							editTeamGrade.setVisibility(View.VISIBLE);
						}

						break;
				}
			}
		});

		return rootView;
	}


	public static String getTeamGradeString(int teamGrade) {
		String result = "";

		if (UserContext.sharedInstance().getAppLocale().getLanguage().equalsIgnoreCase("nl")) {
			result = "" + (teamGrade + 2);
		} else if (UserContext.sharedInstance().getAppLocale().getCountry().equalsIgnoreCase("gb") ||
				UserContext.sharedInstance().getAppLocale().getCountry().equalsIgnoreCase("uk")) {
			result = "" + (teamGrade + 4);
		} else {
			result = "" + teamGrade;
		}

		return result;
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_create_team;
	}

	@Override
	protected boolean isUseEvent() {
		return true;
	}

	private void onContinueButtonClicked() {
		String szName = editTeamName.getText().toString();
		if (TextUtils.isEmpty(szName)) {
			editTeamName.requestFocus();
			return;
		}

		UiUtils.hideKeyboard(getActivity());

		if (TextUtils.isEmpty(szTeamGrade)) {
			szTeamGrade = editTeamGrade.getText().toString();
		}

		UIManager.sharedInstance().showProgressDialog(getActivity(), null, getString(R.string.app_onemoment), true);
		ServerManager.sharedInstance().createTeam(
				szName,
				UserManager.sharedInstance()._currentUser._id,
				szTeamGrade,
				new RestCallback<TeamService.ResCreateTeam>() {
					@Override
					public void success(TeamService.ResCreateTeam resPostTeam, Response response) {
						UIManager.sharedInstance().dismissProgressDialog();
						Team team = TeamManager.sharedInstance().parseTeamFromResCreateTeam(resPostTeam);
						UserManager.sharedInstance()._currentUser._teams.add(team);
						EventManager.sharedInstance().post(EVENT_POSTTEAM_SUCCESS);
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						UIManager.sharedInstance().dismissProgressDialog();
						String errorMsg = ServerManager.getErrorMessageForRetrofitError(retrofitError, getActivity());
						EventManager.sharedInstance().post(EVENT_POSTTEAM_FAILURE, errorMsg);
					}
				}
		);
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent e) {
		if (EVENT_POSTTEAM_SUCCESS.equals(e.name)) {
			((OnboardingActivity) getActivity()).pushNewActivityAnimated(MainActivity.class);
			getActivity().finish();
		} else if (EVENT_POSTTEAM_FAILURE.equals(e.name)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.coachsettings_newteam_added_failed) + " : " +
							(e.object == null ? getString(R.string.error_unknown) : e.object),
					getActivity());
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
				break;

			case R.id.btn_continue:
				onContinueButtonClicked();
				break;
		}
	}
}
