package org.unicefkidpower.schools;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.swrve.sdk.SwrveSDK;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.schools.define.CommonUtils;
import org.unicefkidpower.schools.define.EventNames;
import org.unicefkidpower.schools.define.KPConstants;
import org.unicefkidpower.schools.event.EventManager;
import org.unicefkidpower.schools.event.SEvent;
import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.helper.DebouncedOnClickListener;
import org.unicefkidpower.schools.helper.FontView;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.model.Student;
import org.unicefkidpower.schools.model.StudentManager;
import org.unicefkidpower.schools.model.Team;
import org.unicefkidpower.schools.model.TeamManager;
import org.unicefkidpower.schools.model.Utils;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.CommandService;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.StudentService;
import org.unicefkidpower.schools.server.apimanage.TeamService;
import org.unicefkidpower.schools.sync.SyncTeamDialog;
import org.unicefkidpower.schools.ui.KPRadioButton;
import org.unicefkidpower.schools.ui.KPTextView;
import org.unicefkidpower.schools.ui.SegmentedGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Ruifeng Shi on 8/27/2016.
 */
public class TeamStatsFragment extends SuperFragment {
	public static final String				TAG = "TeamStatsFragment";

	private final int						RETRY_COUNT = 3;
	private int								retryCount = RETRY_COUNT;

	private static final String				EVENT_TEAMUPDATE_SUCCESS = "EVENT_TEAMUPDATE_SUCCESS";
	private static final String				EVENT_TEAMUPDATE_FAILURE = "EVENT_TEAMUPDATE_FAILURE";

	private RosterContentsManager			rosterContentManager;
	private LeaderBoardManager				teamLeaderBoarderManager;

	private KPTextView						txtPackets, txtPowerPoints, txtSteps, txtStatement;

	private SegmentedGroup					segmentedTeamStats;
	private KPRadioButton					radioKids;

	private FontView						btnSyncAll;
	private FontView						btnSettings;

	private Team							teamInfo;
	private ArrayList<Student>				studentsArray;
	private LinearLayout					layoutKids, layoutLeaderboard;


	public static TeamStatsFragment newInstance(Team team) {
		TeamStatsFragment f = new TeamStatsFragment();

		// supply team id
		Bundle args = new Bundle();
		args.putParcelable("teamInfo", team);
		f.setArguments(args);

		return f;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState == null)
			teamInfo = getArguments().getParcelable("teamInfo");
		else
			teamInfo = savedInstanceState.getParcelable("teamInfo");

		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					public void onGlobalLayout() {
						if (!bInitialized) {
							Rect r = new Rect();
							mainLayout.getLocalVisibleRect(r);
							bInitialized = true;
						}

						Rect rect = new Rect();
						mainLayout.getWindowVisibleDisplayFrame(rect);
						final int heightDiff = mainLayout.getRootView().getHeight() - (rect.bottom - rect.top);
						if (heightDiff > 100) {
							rosterContentManager.onShowKeyboard();
						} else if (heightDiff < 100) {
							rosterContentManager.onHideKeyboard();
						}
					}
				}
		);

		txtPackets = (KPTextView) rootView.findViewById(R.id.txt_packets_value);
		txtPowerPoints = (KPTextView) rootView.findViewById(R.id.txt_power_points_value);
		txtSteps = (KPTextView) rootView.findViewById(R.id.txt_average_steps_per_day_value);
		txtStatement = (KPTextView) rootView.findViewById(R.id.txt_statement);
		layoutKids = (LinearLayout) rootView.findViewById(R.id.layout_kids);
		layoutLeaderboard = (LinearLayout) rootView.findViewById(R.id.layout_leaderboard);
		segmentedTeamStats = (SegmentedGroup) rootView.findViewById(R.id.segmentedTeamStats);
		radioKids = (KPRadioButton) rootView.findViewById(R.id.radioKids);
		btnSyncAll = (FontView) rootView.findViewById(R.id.btn_sync_all);

		rosterContentManager = new RosterContentsManager(getActivity(), layoutKids, teamInfo);
		rosterContentManager.onCreate();

		teamLeaderBoarderManager = new LeaderBoardManager(getActivity(), getChildFragmentManager(), layoutLeaderboard, teamInfo);

		segmentedTeamStats.setOnCheckedChangeListener(
				new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						switch (checkedId) {
							case R.id.radioKids:
								onKidsRadioSelected();
								break;

							case R.id.radioLeaderboard:
								onLeaderRadioSelected();
								break;
						}
					}
				}
		);

		btnSyncAll.setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				startTeamSync();
			}
		});

		teamLeaderBoarderManager.refreshLeaderBoard();
		onKidsRadioSelected();
		getStudents();

		return rootView;
	}


	@Override
	public void onResume() {
		super.onResume();

		if (rosterContentManager != null) {
			rosterContentManager.refreshStudents();
		}
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_team_stats;
	}


	@Override
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		state.putParcelable("teamInfo", getArguments().getParcelable("teamInfo"));
	}

	@Override
	protected boolean isUseEvent() {
		return true;
	}

	@Override
	protected boolean shouldShowRightActionItem() {
		return true;
	}

	@Override
	public View rightBarItemView() {
		if (btnSettings == null) {
			btnSettings = new FontView(_parentActivity);
			btnSettings.setText(_parentActivity.getString(R.string.settings_with_icon));
			btnSettings.setTextSize(16);
			btnSettings.setAllCaps(true);
			btnSettings.setGravity(Gravity.CENTER);
			btnSettings.setTextColor(Color.WHITE);
			btnSettings.setBackgroundColor(CommonUtils.getColorFromRes(getResources(), R.color.kidpower_dark_blue));

			RelativeLayout.LayoutParams lpSettings = new RelativeLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT
			);
			btnSettings.setLayoutParams(lpSettings);
			btnSettings.setPadding(
					_parentActivity.getResources().getDimensionPixelSize(R.dimen.margin_4),
					0,
					_parentActivity.getResources().getDimensionPixelSize(R.dimen.margin_4),
					0
			);

			btnSettings.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((MainActivity) _parentActivity).showAccountSettings();
				}
			});
		}

		return btnSettings;
	}

	public String getFragmentTitle() {
		if (teamInfo == null)
			return null;

		return teamInfo._name;
	}

	protected void onKidsRadioSelected() {
		Logger.log(TAG, "Kids tab selected");

		layoutKids.setVisibility(View.VISIBLE);
		layoutLeaderboard.setVisibility(View.GONE);
		btnSyncAll.setVisibility(View.VISIBLE);
	}

	private void onLeaderRadioSelected() {
		Logger.log(TAG, "LeaderBoard tab selected");

		layoutKids.setVisibility(View.GONE);
		layoutLeaderboard.setVisibility(View.VISIBLE);
		btnSyncAll.setVisibility(View.GONE);
	}

	protected void updateTeamStats() {
		ServerManager.sharedInstance().updateStatsForTeam(teamInfo._id, new RestCallback<CommandService.ResUpdateStats>() {
			@Override
			public void success(CommandService.ResUpdateStats resUpdateStats, Response response) {
				retryCount = RETRY_COUNT;
				getTeamSummary();
			}

			@Override
			public void failure(RetrofitError retrofitError, String message) {
				retryCount = RETRY_COUNT;
				getTeamSummary();
			}
		});
	}

	protected void getTeamSummary() {
		//dlg = UIManager.sharedInstance().showProgressDialog(getActivity(), null, "", true);
		ServerManager.sharedInstance().getTeamSummaryByTeamId(teamInfo._id, new RestCallback<TeamService.ResTeamSummaryByTeamId>() {
			@Override
			public void success(TeamService.ResTeamSummaryByTeamId resTeamSummaryByTeamId, Response response) {
				UIManager.sharedInstance().dismissProgressDialog();

				txtPackets.setText(Utils.parsePacketWithCurrencyFormat(resTeamSummaryByTeamId.resTeamSummary.totalPackets));
				txtPowerPoints.setText(Utils.parsePowerpointWithCurrencyFormat(resTeamSummaryByTeamId.resTeamSummary.totalPoints));
				txtSteps.setText("" + resTeamSummaryByTeamId.resTeamSummary.averageStepsPerStudentDay);
				txtStatement.setText(resTeamSummaryByTeamId.resTeamSummary._displayMessage.message);
			}

			@Override
			public void failure(RetrofitError retrofitError, String message) {
				if (retryCount < 0) {
					UIManager.sharedInstance().dismissProgressDialog();
					AlertDialogWrapper.showErrorAlert(getActivity().getString(R.string.teamstats_load_teaminfo_failed),
							message == null ? getActivity().getString(R.string.error_unknown) :
									message, getActivity());
				} else {
					retryCount--;
					getTeamSummary();
				}
			}
		});
	}

	protected void getStudents() {
		UIManager.sharedInstance().showProgressDialog(getActivity(), null, getString(R.string.teamstats_loading_team), true);

		Logger.log(TAG, "Getting all students with teamInfo ID - %s", teamInfo._id);
		ServerManager.sharedInstance().getAllStudentsByTeamId((teamInfo == null) ? 0 : teamInfo._id, new RestCallback<StudentService.ResGetStudentsByTeamId>() {
			@Override
			public void success(StudentService.ResGetStudentsByTeamId resGetStudentsByTeamId, Response response) {
				Logger.log(TAG, "All students have been fetched successfully");

				// parse studentsArray
				ArrayList<Student> students = StudentManager.sharedInstance().parseStudentsForResGetAllStudentsByTeamId(resGetStudentsByTeamId);
				TeamManager.sharedInstance().updateStudentListForTeam(teamInfo._id, students);
				TeamStatsFragment.this.studentsArray = students;

				radioKids.setText(getActivity().getString(R.string.kids_number, TeamStatsFragment.this.studentsArray.size()));
				rosterContentManager.onGetStudents(TeamStatsFragment.this.studentsArray);

				updateTeamStats();
			}

			@Override
			public void failure(RetrofitError retrofitError, String message) {
				Logger.error(TAG, "Getting all students has been failed");

				studentsArray = new ArrayList<>();
				AlertDialogWrapper.showErrorAlert(getString(R.string.teamstats_student_get_failed),
						message == null ? getString(R.string.error_unknown) : message, getActivity());

				UIManager.sharedInstance().dismissProgressDialog();
			}
		});
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent e) {
		if (EVENT_TEAMUPDATE_SUCCESS.equals(e.name)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_success),
					getString(R.string.teamstats_teaminfo_updated),
					getActivity());

			Team team = (Team) e.object;
			if (team != null) {
				this.teamInfo._name = team._name;
				this.teamInfo._grade = team._grade;
			}
			Logger.log(TAG, "Team State Updated");
		} else if (EVENT_TEAMUPDATE_FAILURE.equals(e.name)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.teamstats_teaminfo_update_failed) + ": " +
							(e.object == null ? getString(R.string.error_unknown) : e.object),
					getActivity());
			Logger.error(TAG, "Team State Update failed" + ": " + (e.object == null ? getString(R.string.error_unknown) : e.object));
		} else if (EventManager.isEvent(e, EventNames.EVENT_STUDENT_CREATED)) {
			Logger.error(TAG, "Student created");
			getStudents();
		} else if (EventManager.isEvent(e, EventNames.EVENT_STUDENT_UPDATED)) {
			Logger.error(TAG, "Student updated");
			getStudents();
		}
	}

	void startTeamSync() {
		if (teamInfo == null || studentsArray.size() == 0) {
			Logger.log(TAG, "Team sync : Starting ");
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getActivity().getString(R.string.teamstats_has_no_students),
					getActivity());
			return;
		}

		SyncTeamDialog groupSyncDialog = new SyncTeamDialog(teamInfo, studentsArray, getActivity(), new SyncTeamDialog.SyncTeamDialogListener() {
			@Override
			public void onSuccess(int success, int failed) {
				Map<String, String> payload = new HashMap<>();
				payload.put("successful", "" + success);
				payload.put("failed", "" + failed);
				SwrveSDK.event(KPConstants.SWRVE_GROUP_SYNC_COMPLETE, payload);

				// update teamInfo sync information #133677121
				// TODO : Don't need to show progressing dialog right now
				updateTeamStats();

				rosterContentManager.onGroupSynced();
			}

			@Override
			public void onFailed(String message) {
			}
		});

		groupSyncDialog.show();
	}


}
