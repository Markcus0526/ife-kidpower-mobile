package org.unicefkidpower.schools;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.schools.context.UserContext;
import org.unicefkidpower.schools.define.config;
import org.unicefkidpower.schools.event.EventManager;
import org.unicefkidpower.schools.event.SEvent;
import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.model.Team;
import org.unicefkidpower.schools.model.TeamManager;
import org.unicefkidpower.schools.model.User;
import org.unicefkidpower.schools.model.UserManager;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.TeamService;
import org.unicefkidpower.schools.ui.UiUtils;

import java.util.ArrayList;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class TeamSetupActivity extends BaseActivityWithNavBarOld implements View.OnClickListener {
	private static final String TAG = "TeamSetupActivity";
	private static final String EVENT_UPDATETEAM_SUCCESS = "TEAMSETUP_EVENT_UPDATETEAM_SUCCESS";
	private static final String EVENT_UPDATETEAM_FAILED = "TEAMSETUP_EVENT_UPDATETEAM_FAILED";
	protected EditText editTeamName;
	protected LinearLayout llChooseGrade;
	protected TextView textGrade;
	protected Button btnNext;
	protected RelativeLayout rlGradeItems;
	protected ListView listGradeItems;
	protected LeDeviceListAdapter adapter;
	protected View rlCenter;
	protected View llSetupTeamNameAndGrade;
	protected TeamManager.TeamGrade _selectedGrade;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_teamsetup);

		FlurryAgent.onStartSession(this, "Team Setup Activity");

		editTeamName = (EditText) findViewById(R.id.editTeamName);
		editTeamName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				//
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				//
			}

			@Override
			public void afterTextChanged(Editable s) {
				//
				if (editTeamName.getText() != null &&
						editTeamName.getText().toString() != null &&
						editTeamName.getText().toString().length() > 0 &&
						_selectedGrade != null) {
					enableNextButton();
				} else {
					disableNextButton();
				}
			}
		});

		llChooseGrade = (LinearLayout) findViewById(R.id.llChooseGrade);
		textGrade = (TextView) findViewById(R.id.textGrade);
		btnNext = (Button) findViewById(R.id.btnNext);
		rlGradeItems = (RelativeLayout) findViewById(R.id.rlGradeItems);
		listGradeItems = (ListView) findViewById(R.id.listGradeItems);

		adapter = new LeDeviceListAdapter(this);
		listGradeItems.setAdapter(adapter);
		listGradeItems.setOnItemClickListener(createOnItemClickListener());

		rlCenter = findViewById(R.id.rlCenter);

		llSetupTeamNameAndGrade = findViewById(R.id.llSetupTeamNameAndGrade);

		btnNext.setOnClickListener(this);
		llChooseGrade.setOnClickListener(this);

		btnNext.setEnabled(false);

		// hide roster & stats
		llSetupTeamNameAndGrade.setVisibility(View.VISIBLE);

		if (_fromActivity.equals(SplashActivity.class.getName())) {
			UserManager.sharedInstance()._currentUser = UserContext.sharedInstance().getUser();
			ServerManager.sharedInstance().setAccessToken(UserManager.sharedInstance()._currentUser._access_token);
		}
	}

	@Override
	public void onBackPressed() {
		// disable back

		if (_fromActivity.equals(SplashActivity.class.getName())) {
			UserContext.sharedInstance().setSignUp(false);
			Intent intent = new Intent(this, WelcomeActivity.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	protected boolean isUseEvent() {
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();

		// check teams of the current user
		if (UserManager.sharedInstance()._currentUser == null) {
			finish();
			return;
		}

		if (UserManager.sharedInstance()._currentUser._teams == null) {
			finish();
			return;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void disableNextButton() {
		btnNext.setEnabled(false);
	}

	private void enableNextButton() {
		btnNext.setEnabled(true);
	}

	// ---- onclick ---------------
	@Override
	public void onClick(View v) {
		if (btnNext == v) {
			onNextClicked();
		} else if (llChooseGrade == v) {
			onChooseGrade();
		}
	}

	// action handlers
	protected void onNextClicked() {

		Logger.log(TAG, "Next button clicked", editTeamName.getText().toString(), _selectedGrade.value);

		UiUtils.hideKeyboard(this);

		if (config.USE_LOGIC) {

			User currentUser = UserManager.sharedInstance()._currentUser;
			if (currentUser == null) {
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_caution),
						getString(R.string.teamsetup_register_userfirst),
						TeamSetupActivity.this);
				return;
			}

			if (currentUser._teams.size() == 0) {
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_caution),
						getString(R.string.signup_create_teamfirst),
						TeamSetupActivity.this);
				return;
			}

			// call api
			UIManager.sharedInstance().showProgressDialog(this, null, getString(R.string.teamsetup_settingup_team), true);

			Logger.log(TAG, "Updating team - Team name: %s, Grade: %s", editTeamName.getText().toString(), _selectedGrade.value);

			ServerManager.sharedInstance().updateTeam(
					currentUser._teams.get(0)._id,
					editTeamName.getText().toString(),
					currentUser._id,
					_selectedGrade.value,
					new RestCallback<TeamService.ResCreateTeam>() {
						@Override
						public void success(TeamService.ResCreateTeam resCreateTeam, Response response) {
							UIManager.sharedInstance().dismissProgressDialog();
							EventManager.sharedInstance().post(EVENT_UPDATETEAM_SUCCESS, resCreateTeam);

							Logger.log(TAG, "Team successfully updated");
						}

						@Override
						public void failure(RetrofitError retrofitError, String message) {
							UIManager.sharedInstance().dismissProgressDialog();
							String errorMsg = ServerManager.getErrorMessageForRetrofitError(retrofitError, TeamSetupActivity.this);
							EventManager.sharedInstance().post(EVENT_UPDATETEAM_FAILED, errorMsg);

							Logger.error(TAG, "Team update has been failed");
						}
					});
		} else {
			_goNext();
		}
	}

	protected void onChooseGrade() {
		UiUtils.hideKeyboard(this);

		ArrayList<TeamManager.TeamGrade> grades = TeamManager.sharedInstance()._teamGrades;

		// show grade
		int[] location = {0, 0};
		rlGradeItems.setVisibility(View.VISIBLE);
		adapter.replaceWith(grades);

		llChooseGrade.getLocationOnScreen(location);
		int editX = location[0];
		int editY = location[1];

        /*
		ScrollView scrollView = (ScrollView)findViewById(R.id.scrollview);
        scrollView.getLocationOnScreen(location);
        int scrollX = location[0];
        int scrollY = location[1];

        int scrollOffset = scrollView.getScrollY();
        int left = editX - scrollX;
        RelativeLayout.LayoutParams editParams = (RelativeLayout.LayoutParams)editWristbandCode.getLayoutParams();
        int top = scrollOffset + editY - scrollY + editParams.height;
        */

		rlCenter.getLocationOnScreen(location);
		int topParent = 0;//location[0];
		int leftParent = 0;//location[1];

		int left = editX - topParent;
		int top = editY - leftParent;

		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rlGradeItems.getLayoutParams();
		layoutParams.leftMargin = left;
		layoutParams.topMargin = top;
		rlGradeItems.setLayoutParams(layoutParams);
		rlGradeItems.bringToFront();
	}

	protected void onSelectedGrade(TeamManager.TeamGrade grade) {
		textGrade.setText(grade.desc);
		_selectedGrade = grade;
		if (editTeamName.getText() != null && editTeamName.getText().toString().length() > 0) {
			enableNextButton();
		}
	}

	protected void _goNext() {
		User user = UserManager.sharedInstance()._currentUser;
		if (user == null) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_caution),
					getString(R.string.signup_create_userfirst),
					TeamSetupActivity.this);
			return;
		}
		if (user._teams.size() == 0) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_caution),
					getString(R.string.signup_create_teamfirst),
					TeamSetupActivity.this);
			return;
		}

		Logger.log(TAG, "Go to the main screen");

		//Intent intent = new Intent(this, MainActivityOld.class);
		//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//intent.putExtra(MainActivityOld.CURRENT_POSITION, MainActivityOld.INDEX_TEAMSTATS);
		//intent.putExtra(MainActivityOld.CURRENT_TEAM, user._teams.get(0));
		//startActivity(intent);

		//finish();
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent e) {
		if (EventManager.isEvent(e, EVENT_UPDATETEAM_SUCCESS)) {

			// update team infor success
			UserContext.sharedInstance().setSignUp(false);

			// set current user
			Team team = TeamManager.sharedInstance().parseTeamFromResCreateTeam((TeamService.ResCreateTeam) e.object);

			// update team
			UserManager.sharedInstance()._currentUser._teams.remove(0);
			UserManager.sharedInstance()._currentUser._teams.add(team);

			// go next
			_goNext();
		} else if (EventManager.isEvent(e, EVENT_UPDATETEAM_FAILED)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_caution),
					getString(R.string.teamsetup_createteam_failed) + ": " +
							(e.object == null ? getString(R.string.error_unknown) : e.object),
					TeamSetupActivity.this);
		}
	}

	// --------- NavigationBarOld delegate -------------------------------------------------
	@Override
	public String getNavbarTitle() {
		return getString(R.string.teamsetup_title);
	}

	@Override
	public boolean shouldShowMenu() {
		return false;
	}

	@Override
	public boolean shouldShowHome() {
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			rlGradeItems.setVisibility(View.GONE);
		}
		return false;
	}

	// ---------------- list for grade ------------------------------------------------
	private AdapterView.OnItemClickListener createOnItemClickListener() {
		return new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final TeamManager.TeamGrade grade = (TeamManager.TeamGrade) adapter.getItem(position);
				if (grade == null)
					return;

				rlGradeItems.setVisibility(View.GONE);

				onSelectedGrade(grade);
			}
		};
	}

	static class ViewHolder {
		final TextView gradeView;
		final View parentView;

		ViewHolder(View view) {

			gradeView = (TextView) view.findViewWithTag("grade");
			parentView = (View) view.findViewWithTag("parentView");
		}
	}

	public class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<TeamManager.TeamGrade> mItems;
		private LayoutInflater mInflator;

		public LeDeviceListAdapter(Context context) {
			super();
			mItems = new ArrayList<TeamManager.TeamGrade>();
			mInflator = LayoutInflater.from(context);
		}

		public void replaceWith(ArrayList<TeamManager.TeamGrade> grades) {
			mItems.clear();
			mItems.addAll(grades);
			notifyDataSetChanged();
		}

		public void clear() {
			mItems.clear();
		}

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public Object getItem(int i) {
			return mItems.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			view = inflateIfRequired(view, position, parent);
			bind((TeamManager.TeamGrade) getItem(position), view);
			return view;
		}

		public void refreshUI() {
			notifyDataSetChanged();
		}

		private void bind(TeamManager.TeamGrade grade, View view) {
			ViewHolder holder = (ViewHolder) view.getTag();

			holder.gradeView.setText(grade.desc);
		}

		private View inflateIfRequired(View view, int position, ViewGroup parent) {
			if (view == null) {
				view = mInflator.inflate(R.layout.item_teamsetup_grade_layout, null);
				if (config.USE_RESOLUTIONSET) {
					if (TeamSetupActivity.this.isInitialized())
						ResolutionSet._instance.iterateChild(view);
				}
				view.setTag(new ViewHolder(view));
			}
			return view;
		}
	}
}
