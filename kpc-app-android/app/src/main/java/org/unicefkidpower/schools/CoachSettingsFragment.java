package org.unicefkidpower.schools;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.schools.define.CommonUtils;
import org.unicefkidpower.schools.define.config;
import org.unicefkidpower.schools.event.EventManager;
import org.unicefkidpower.schools.event.SEvent;
import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.model.Student;
import org.unicefkidpower.schools.model.StudentManager;
import org.unicefkidpower.schools.model.Team;
import org.unicefkidpower.schools.model.TeamManager;
import org.unicefkidpower.schools.model.User;
import org.unicefkidpower.schools.model.UserManager;
import org.unicefkidpower.schools.model.Utils;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.CommandService;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.StudentService;
import org.unicefkidpower.schools.server.apimanage.TeamService;
import org.unicefkidpower.schools.server.apimanage.UserService;
import org.unicefkidpower.schools.ui.KPTextView;
import org.unicefkidpower.schools.ui.SelectorDialog;
import org.unicefkidpower.schools.ui.UiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;


public class CoachSettingsFragment extends SuperFragment implements View.OnClickListener {
	protected static final String TAG = "CoachSettingsFragment";
	private static final String EVENT_USERNAMEUPDATE_SUCCESS = "EVENT_COACHSETTINGS_USERNAMEUPDATE_SUCCESS";
	private static final String EVENT_USERNAMEUPDATE_FAILURE = "EVENT_COACHSETTINGS_USERNAMEUPDATE_FAILURE";
	private static final String EVENT_USEREMAILUPDATE_SUCCESS = "EVENT_COACHSETTINGS_USEREMAILUPDATE_SUCCESS";
	private static final String EVENT_USEREMAILUPDATE_FAILURE = "EVENT_COACHSETTINGS_USEREMAILUPDATE_FAILURE";
	private static final String EVENT_GETTEAMS_SUCCESS = "EVENT_COACHSETTINGS_GETTEAMS_SUCCESS";
	private static final String EVENT_GETTEAMS_FAILURE = "EVENT_COACHSETTINGS_GETTEAMS_FAILURE";
	private static final String EVENT_POSTTEAM_SUCCESS = "EVENT_COACHSETTINGS_POSTTEAM_SUCCESS";
	private static final String EVENT_POSTTEAM_FAILURE = "EVENT_COACHSETTINGS_POSTTEAM_FAILURE";

	private static final String EVENT_GETALLSTUDENTSBYTEAMID_FAILED = "EVENT_COACHSETTINGS_GETALLSTUDENTSBYTEAMID_FAILED";
	private static final String EVENT_GETALLSTUDENTSBYTEAMID_SUCCESS = "EVENT_COACHSETTINGS_GETALLSTUDENTSBYTEAMID_SUCCESS";

	private static final String EVENT_EXTRAPOINT_FAILED = "EVENT_COACHSETTINGS_EXTRAPOINT_FAILED";
	private static final String EVENT_EXTRAPOINT_SUCCESS = "EVENT_COACHSETTINGS_EXTRAPOINT_SUCCESS";
	private final int MODE_AVATARNAME = 0;
	private final int MODE_NAME = 1;
	private final int MODE_EMAIL = 2;
	private final int MODE_ADDTEAM = 3;
	private final int MODE_ADDPOWERPOINT = 4;
	protected ImageView ivAvatar;
	protected View rlChangeAvatar;
	protected TextView textName;
	protected TextView textLastSyncDate;
	protected View rlEdit;
	protected LinearLayout llName;
	protected LinearLayout llEditingName;
	protected LinearLayout llEmail;
	protected LinearLayout llEditingEmail;
	protected LinearLayout llPassword;
	protected LinearLayout llEditingPassword;
	protected LinearLayout llAddTeam;
	protected LinearLayout llEditingAddTeam;
	protected LinearLayout llAddPowerPoints;
	protected LinearLayout llEditingAddPowerPoints;
	protected TextView textEditNames;
	protected RelativeLayout rlEditNames;
	protected EditText editFirstName;
	protected EditText editLastName;
	protected EditText editAlias;
	protected Button btnSaveName;
	protected Button btnCancelName;
	protected TextView textRealName;
	protected TextView textAlias;
	protected TextView textEditEmail;
	protected TextView textEmail;
	protected RelativeLayout rlEditEmail;
	protected EditText editEmail;
	protected EditText editConfirmEmail;
	protected Button btnConfirmEmail;
	protected Button btnSaveEmail;
	protected Button btnCancelEmail;
	protected TextView textEditPassword;
	protected RelativeLayout rlEditPassword;
	protected EditText editNewPassword;
	protected EditText editConfirmPassword;
	protected Button btnSavePassword;
	protected Button btnCancelPassword;
	protected TextView textEditAddTeam;
	protected RelativeLayout rlEditAddTeam;
	protected EditText editTeamName;
	protected RelativeLayout rlChangeLevel;
	protected Button btnSaveAddTeam;
	protected Button btnCancelAddTeam;
	protected View rlCenter;
	protected LinearLayout llChooseGrade;
	protected TextView textGrade;
	protected TextView textTeamCount;
	protected LinearLayout llTeamList;
	protected TextView textEditAddPowerPoints;
	protected RelativeLayout rlEditAddPowerPoints;
	protected Button btnSaveAddPoints;
	protected Button btnCancelAddPoints;
	protected View llAddPowerPointsTeamSelect;
	protected View llAddPowerPointsKindSelect;
	protected View llAddPowerPointsStudentSelect;
	protected View llAddPowerPointsPointSelect;
	protected TextView textAddPowerPointsTeam;
	protected TextView textAddPowerPointsKind;
	protected TextView textAddPowerPointsStudent;
	protected TextView textAddPowerPointsPoint;
	protected TextView textAppVersionNumber;

	// edit mode
	protected View llEditControls;
	protected EditText editName;
	protected Button btnSave;
	protected Button btnCancel;
	protected int _statusBarHeight;
	protected TeamManager.TeamGrade _selectedTeamGrade = null;
	protected Team _selectedTeam = null;
	protected int _selectedKind = -1;
	protected Student _selectedStudent = null;
	protected int _selectedPoint = -1;
	TextWatcher textWatcherEmail = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			String szEmail = s.toString();
			String szConfirmEmail = editConfirmEmail.getText().toString();
			if (szEmail.equals(szConfirmEmail))
				btnSaveEmail.setEnabled(true);
			else
				btnSaveEmail.setEnabled(false);
		}
	};
	TextWatcher textWatcherConfirmEmail = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			String szConfirmEmail = s.toString();
			String szEmail = editEmail.getText().toString();
			if (szConfirmEmail.equals(szEmail))
				btnSaveEmail.setEnabled(true);
			else
				btnSaveEmail.setEnabled(false);
		}
	};
	TextWatcher textWatcherTeamName = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			String szGrade = textGrade.getText().toString();
			String szTeamName = s.toString();
			if (szGrade.length() > 0 && szTeamName.length() > 0)
				enableSaveAddTeam();
			else
				disableSaveAddTeam();
		}
	};

	private int MODE_EDIT = MODE_NAME;
	private User currentUser = null;
	private ArrayList<TeamManager.TeamGrade> _gradeArrayList;
	private ArrayList<Team> _teamArrayList;
	private ArrayList<String> _kindArrayList;
	private ArrayList<Integer> _pointArrayList;
	private boolean isEditingName = false;
	private boolean isEditingEmail = false;
	private boolean isEditingPassword = false;
	private boolean isEditingAddTeam = false;
	private boolean isEditingAddPowerPoint = false;


	/**
	 * Called when the activity is first created.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		FlurryAgent.onStartSession(getContext(), "Coach Settings Fragment");

		mainLayout = rootView.findViewById(R.id.layout_parent);

		ivAvatar = (ImageView) rootView.findViewById(R.id.ivAvatar);

		rlChangeAvatar = rootView.findViewById(R.id.rlChangeAvatar);
		rlChangeAvatar.setOnClickListener(this);

		textName = (TextView) rootView.findViewById(R.id.textName);
		textLastSyncDate = (TextView) rootView.findViewById(R.id.textLastSyncDate);

		rlEdit = rootView.findViewById(R.id.rlEdit);
		rlEdit.setOnClickListener(this);

		llName = (LinearLayout) rootView.findViewById(R.id.llNameBeforeEdit);
		llEditingName = (LinearLayout) rootView.findViewById(R.id.llNameEditing);

		textEditNames = (TextView) rootView.findViewById(R.id.labelEditName);
		textEditNames.setOnClickListener(this);
		rlEditNames = (RelativeLayout) rootView.findViewById(R.id.rlEditName);
		rlEditNames.setOnClickListener(this);
		editFirstName = (EditText) rootView.findViewById(R.id.editFirstName);
		editLastName = (EditText) rootView.findViewById(R.id.editLastName);
		editAlias = (EditText) rootView.findViewById(R.id.editAlias);
		btnSaveName = (Button) rootView.findViewById(R.id.btnSaveName);
		btnSaveName.setOnClickListener(this);
		btnCancelName = (Button) rootView.findViewById(R.id.btnCancelName);
		btnCancelName.setOnClickListener(this);
		textRealName = (TextView) rootView.findViewById(R.id.textRealName);
		textAlias = (TextView) rootView.findViewById(R.id.textAlias);

		llEmail = (LinearLayout) rootView.findViewById(R.id.llEmailBeforeEdit);
		llEditingEmail = (LinearLayout) rootView.findViewById(R.id.llEmailEditing);

		textEditEmail = (TextView) rootView.findViewById(R.id.labelEditEmail);
		textEditEmail.setOnClickListener(this);
		textEmail = (TextView) rootView.findViewById(R.id.textEmail);
		rlEditEmail = (RelativeLayout) rootView.findViewById(R.id.rlEditEmail);
		rlEditEmail.setOnClickListener(this);
		editEmail = (EditText) rootView.findViewById(R.id.editEmail);
		editEmail.addTextChangedListener(textWatcherEmail);
		editConfirmEmail = (EditText) rootView.findViewById(R.id.editConfirmEmail);
		editConfirmEmail.addTextChangedListener(textWatcherConfirmEmail);
		btnConfirmEmail = (Button) rootView.findViewById(R.id.buttonConfirmEmail);
		btnConfirmEmail.setOnClickListener(this);
		btnSaveEmail = (Button) rootView.findViewById(R.id.buttonSaveEmail);
		btnSaveEmail.setOnClickListener(this);
		btnCancelEmail = (Button) rootView.findViewById(R.id.buttonCancelEmail);
		btnCancelEmail.setOnClickListener(this);

		llPassword = (LinearLayout) rootView.findViewById(R.id.llPasswordBeforeEdit);
		llEditingPassword = (LinearLayout) rootView.findViewById(R.id.llPasswordEditing);

		textEditPassword = (TextView) rootView.findViewById(R.id.labelEditPassword);
		textEditPassword.setOnClickListener(this);
		rlEditPassword = (RelativeLayout) rootView.findViewById(R.id.rlEditPassword);
		rlEditPassword.setOnClickListener(this);
		editNewPassword = (EditText) rootView.findViewById(R.id.editPassword);
		editConfirmPassword = (EditText) rootView.findViewById(R.id.editConfirmPassword);
		btnSavePassword = (Button) rootView.findViewById(R.id.buttonSavePassword);
		btnSavePassword.setOnClickListener(this);
		btnCancelPassword = (Button) rootView.findViewById(R.id.buttonCancelPassword);
		btnCancelPassword.setOnClickListener(this);

		llAddTeam = (LinearLayout) rootView.findViewById(R.id.llAddTeamBeforeEdit);
		llEditingAddTeam = (LinearLayout) rootView.findViewById(R.id.llAddTeamEditing);

		textEditAddTeam = (TextView) rootView.findViewById(R.id.labelEditAddTeam);
		textEditAddTeam.setOnClickListener(this);
		rlEditAddTeam = (RelativeLayout) rootView.findViewById(R.id.rlEditAddTeam);
		rlEditAddTeam.setOnClickListener(this);
		editTeamName = (EditText) rootView.findViewById(R.id.editTeamName);
		editTeamName.addTextChangedListener(textWatcherTeamName);
		btnSaveAddTeam = (Button) rootView.findViewById(R.id.buttonSaveAddTeam);
		btnSaveAddTeam.setOnClickListener(this);
		btnCancelAddTeam = (Button) rootView.findViewById(R.id.buttonCancelAddTeam);
		btnCancelAddTeam.setOnClickListener(this);
		textGrade = (TextView) rootView.findViewById(R.id.textGrade);
		textGrade.setOnClickListener(this);
		textTeamCount = (TextView) rootView.findViewById(R.id.textTeamCount);
		llTeamList = (LinearLayout) rootView.findViewById(R.id.llTeamList);

		rlCenter = rootView.findViewById(R.id.rlCenter);
		llChooseGrade = (LinearLayout) rootView.findViewById(R.id.llChooseGrade);
		llChooseGrade.setOnClickListener(this);

		llAddPowerPoints = (LinearLayout) rootView.findViewById(R.id.llAddPowerPointsBeforeEdit);
		llEditingAddPowerPoints = (LinearLayout) rootView.findViewById(R.id.llAddPowerPointsEditing);

		textEditAddPowerPoints = (TextView) rootView.findViewById(R.id.labelEditAddPowerPoints);
		textEditAddPowerPoints.setOnClickListener(this);
		rlEditAddPowerPoints = (RelativeLayout) rootView.findViewById(R.id.rlEditAddPowerPoints);
		rlEditAddPowerPoints.setOnClickListener(this);
		btnSaveAddPoints = (Button) rootView.findViewById(R.id.buttonSaveAddPowerPoints);
		btnSaveAddPoints.setOnClickListener(this);
		btnCancelAddPoints = (Button) rootView.findViewById(R.id.buttonCancelAddPowerPoints);
		btnCancelAddPoints.setOnClickListener(this);

		llAddPowerPointsTeamSelect = rootView.findViewById(R.id.llAddPowerPointsTeamSelect);
		llAddPowerPointsTeamSelect.setOnClickListener(this);

		llAddPowerPointsKindSelect = rootView.findViewById(R.id.llAddPowerPointsKindSelect);
		llAddPowerPointsKindSelect.setOnClickListener(this);

		llAddPowerPointsStudentSelect = rootView.findViewById(R.id.llAddPowerPointsStudentSelect);
		llAddPowerPointsStudentSelect.setOnClickListener(this);

		llAddPowerPointsPointSelect = rootView.findViewById(R.id.llAddPowerPointsPointSelect);
		llAddPowerPointsPointSelect.setOnClickListener(this);

		textAddPowerPointsTeam = (TextView) rootView.findViewById(R.id.textAddPowerPointsTeam);
		textAddPowerPointsTeam.setOnClickListener(this);

		textAddPowerPointsKind = (TextView) rootView.findViewById(R.id.textAddPowerPointsKind);
		textAddPowerPointsKind.setOnClickListener(this);

		textAddPowerPointsStudent = (TextView) rootView.findViewById(R.id.textAddPowerPointsStudent);
		textAddPowerPointsStudent.setOnClickListener(this);

		textAddPowerPointsPoint = (TextView) rootView.findViewById(R.id.textAddPowerPointsPoint);
		textAddPowerPointsPoint.setOnClickListener(this);

		// edit mode
		llEditControls = rootView.findViewById(R.id.llEditControls);
		editName = (EditText) rootView.findViewById(R.id.editName);
		btnSave = (Button) rootView.findViewById(R.id.btnSave);
		btnCancel = (Button) rootView.findViewById(R.id.btnCancel);
		btnSave.setOnClickListener(this);
		btnCancel.setOnClickListener(this);

		llEditControls.setVisibility(View.INVISIBLE);
		textName.setVisibility(View.VISIBLE);

		isEditingName = false;
		isEditingEmail = false;
		isEditingPassword = false;
		isEditingAddTeam = false;
		isEditingAddPowerPoint = false;

		_initGradeArrayList();
		_initTeamArrayList();
		_initKindArrayList();
		_initPointArrayList();

		_statusBarHeight = UiUtils.getStatusBarHeight(getActivity());

		llEditingAddPowerPoints.setVisibility(View.GONE);

		textAppVersionNumber = (KPTextView) rootView.findViewById(R.id.txt_app_version);
		textAppVersionNumber.setText(getActivity().getString(
				R.string.app_version_number,
				Utils.getVersionName(getActivity()),
				Utils.getVersionCode(getActivity())
		));

		initShowData();

		return rootView;
	}

	protected void _initGradeArrayList() {
		_gradeArrayList = TeamManager.sharedInstance()._teamGrades;
	}

	protected void _initTeamArrayList() {
		_teamArrayList = new ArrayList<Team>();
		if (UserManager.sharedInstance()._currentUser == null ||
				UserManager.sharedInstance()._currentUser._teams == null)
			return;
		for (Team team : UserManager.sharedInstance()._currentUser._teams) {
			_teamArrayList.add(team);
		}
	}

	protected void _initKindArrayList() {
		ArrayList<String> kinds = new ArrayList<String>();
		kinds.add(getResources().getString(R.string.addpowerpoints_kind_toteam));
		kinds.add(getResources().getString(R.string.addpowerpoints_kind_toindividual));
		_kindArrayList = kinds;
	}

	protected void _initPointArrayList() {
		_pointArrayList = new ArrayList<>();
		for (int i = 1; i <= 5; i++) {
			_pointArrayList.add(i);
		}
	}

	@Override
	protected boolean isUseEvent() {
		return true;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_coachsetting;
	}

	private void initShowData() {
		currentUser = UserManager.sharedInstance()._currentUser;
		if (currentUser != null) {
			if (currentUser._userType.equals(User.USERTYPE_TEACHER)) {
				textRealName.setText(currentUser._firstName + " " + currentUser._lastName);
				textAlias.setText(currentUser._nickname);
				editFirstName.setText(currentUser._firstName);
				editLastName.setText(currentUser._lastName);
				editAlias.setText(currentUser._nickname);

				textEmail.setText(CommonUtils.getMaskedEmail(currentUser._email));
				editEmail.setText(currentUser._email);

				int nTeamCount = UserManager.sharedInstance()._currentUser._teams.size();
				String szTeamCount = String.format(getString(R.string.coachsettings_format_for_teamcount), nTeamCount);
				textTeamCount.setText(szTeamCount);

				llTeamList.removeAllViews();
				for (int i = 0; i < UserManager.sharedInstance()._currentUser._teams.size(); i++) {
					TextView textView = new TextView(getActivity());
					textView.setTextColor(CommonUtils.getColorFromRes(getResources(), R.color.kidpower_brownish_grey));
					float size = getResources().getDimension(R.dimen.kidpower_textlinkfontsize);
					textView.setTextSize(size);
					textView.setText(UserManager.sharedInstance()._currentUser._teams.get(i)._name);

					llTeamList.addView(textView);
				}
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();

		exitEditMode();
	}

	protected void onSelectedGrade(String grade) {
		textGrade.setText(grade);
		if (editTeamName.getText() != null && editTeamName.getText().toString() != null && editTeamName.getText().toString().length() > 0) {
			enableSaveAddTeam();
		}
	}

	private void enableSaveAddTeam() {
		btnSaveAddTeam.setEnabled(true);
	}

	private void disableSaveAddTeam() {
		btnSaveAddTeam.setEnabled(false);
	}

	protected void getStudents(final Team team) {
		UIManager.sharedInstance().showProgressDialog(getActivity(), null, getString(R.string.coachsettings_progress_loadingteam), true);
		ServerManager.sharedInstance().getAllStudentsByTeamId(team._id, new RestCallback<StudentService.ResGetStudentsByTeamId>() {
			@Override
			public void success(StudentService.ResGetStudentsByTeamId resGetStudentsByTeamId, Response response) {
				UIManager.sharedInstance().dismissProgressDialog();

				// parse students
				ArrayList<Student> students = StudentManager.sharedInstance().parseStudentsForResGetAllStudentsByTeamId(resGetStudentsByTeamId);
				TeamManager.sharedInstance().updateStudentListForTeam(team._id, students);
				team._students = students;

				EventManager.sharedInstance().post(EVENT_GETALLSTUDENTSBYTEAMID_SUCCESS, resGetStudentsByTeamId);
			}

			@Override
			public void failure(RetrofitError retrofitError, String message) {
				UIManager.sharedInstance().dismissProgressDialog();
				String errorMsg = ServerManager.getErrorMessageForRetrofitError(retrofitError, getActivity());
				EventManager.sharedInstance().post(EVENT_GETALLSTUDENTSBYTEAMID_FAILED, errorMsg);
			}
		});
	}



	protected void showList(final ArrayList itemsArray, final int chooseType) {
		SelectorDialog dialog = new SelectorDialog(getSafeContext());
		dialog.setData(itemsArray, chooseType, new SelectorDialog.OnItemSelectedListener() {
			@Override
			public void onItemSelected(int position) {
				if (chooseType == SelectorDialog.CHOOSING_GRADE) {
					final String grade = _gradeArrayList.get(position).desc;
					_selectedTeamGrade = _gradeArrayList.get(position);
					if (grade == null)
						return;

					onSelectedGrade(grade);
				} else if (chooseType == SelectorDialog.CHOOSING_TEAM) {
					_selectedTeam = _teamArrayList.get(position);
					if (_selectedTeam == null)
						return;

					onSelectedTeam();
				} else if (chooseType == SelectorDialog.CHOOSING_KIND) {
					_selectedKind = position;

					onSelectedKind();
				} else if (chooseType == SelectorDialog.CHOOSING_STUDENT) {
					if (_selectedTeam == null || _selectedTeam._students == null)
						return;

					_selectedStudent = _selectedTeam._students.get(position);

					onSelectedStudent();
				} else if (chooseType == SelectorDialog.CHOOSING_POINT) {
					_selectedPoint = _pointArrayList.get(position);

					onSelectedPoint();
				}
			}
		});
		dialog.show();
//		// show teams
//		int[] location = {0, 0};
//		rlGradeItems.setVisibility(View.VISIBLE);
//		rlGradeItems.setClickable(true);
//		rlGradeItems.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				_hideList();
//			}
//		});
//
//		viewForChoosing.getLocationOnScreen(location);
//		int editX = location[0];
//		int editY = location[1];
//
//		rlCenter.getLocationOnScreen(location);
//		int leftParent = 0;			// location[0];
//		int topParent = 0;			//location[1];
//
//		int left = editX - leftParent;
//		int top = editY - topParent + viewForChoosing.getHeight() - _statusBarHeight;
//
//		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) listGradeItems.getLayoutParams();
//		layoutParams.leftMargin = left;
//		layoutParams.topMargin = top;
//		layoutParams.width = viewForChoosing.getWidth();
//
//		listGradeItems.setLayoutParams(layoutParams);
//		rlGradeItems.bringToFront();
	}

	protected void onChooseGroup() {
		UiUtils.hideKeyboard(getActivity());

//		_chooseType = CHOOSING_GRADE;
//
//		// show grade
//		rlGradeItems.setVisibility(View.VISIBLE);
//		adapter.replaceWith(_gradeArrayList);
//
//		_showList(llChooseGrade);
		showList(_gradeArrayList, SelectorDialog.CHOOSING_GRADE);
	}

	protected void onChooseAddPowerPointsTeam() {
		UiUtils.hideKeyboard(getActivity());

		if (_teamArrayList == null || _teamArrayList.size() == 0) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.coachsettings_no_teams),
					getActivity());
			return;
		}

//		_chooseType = CHOOSING_TEAM;
//		adapter.replaceWith(_teamArrayList);
//
//		_showList(llAddPowerPointsTeamSelect);
		showList(_teamArrayList, SelectorDialog.CHOOSING_TEAM);
	}

	protected void onChooseAddPowerPointsKind() {
		UiUtils.hideKeyboard(getActivity());

//		_chooseType = CHOOSING_KIND;
//
//		adapter.replaceWith(_kindArrayList);
//
//		_showList(llAddPowerPointsKindSelect);
		showList(_kindArrayList, SelectorDialog.CHOOSING_KIND);
	}

	protected void onChooseAddPowerPointsStudent() {
		UiUtils.hideKeyboard(getActivity());
//		_chooseType = CHOOSING_STUDENT;

		if (_selectedTeam == null) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_caution),
					getString(R.string.coachsettings_select_team),
					getActivity());
			return;
		}

		if (_selectedTeam._students == null ||
				_selectedTeam._students.size() == 0) {
			// fetch students
			getStudents(_selectedTeam);
		} else {
//			adapter.replaceWith(_selectedTeam._students);
			showList(_selectedTeam._students, SelectorDialog.CHOOSING_STUDENT);
//			_showList(llAddPowerPointsStudentSelect);
		}
	}

	protected void onChooseAddPowerPointsPoint() {
		UiUtils.hideKeyboard(getActivity());

//		_chooseType = CHOOSING_POINT;
//		adapter.replaceWith(_pointArrayList);
//		_showList(llAddPowerPointsPointSelect);
		showList(_pointArrayList, SelectorDialog.CHOOSING_POINT);
	}

	// ---- onclick ---------------
	@Override
	public void onClick(View v) {
		if (rlChangeAvatar == v) {
			onChangeAvatarClicked();
		} else if (rlEdit == v) {
			MODE_EDIT = MODE_AVATARNAME;
			Logger.log(TAG, "Edit Avatar Name button clicked");
			onEditClicked();
		} else if (textEditNames == v) {
			MODE_EDIT = MODE_NAME;
			Logger.log(TAG, "Edit Name button clicked");
			onEditClicked();
		} else if (rlEditNames == v) {
			MODE_EDIT = MODE_NAME;
			Logger.log(TAG, "Edit Name button clicked");
			onEditClicked();
		} else if (btnSaveName == v) {
			Logger.log(TAG, "Save Name button clicked");
			onSaveName();
		} else if (btnCancelName == v) {
			Logger.log(TAG, "Cancel Editing Name button clicked");
			onCancelName();
		} else if (textEditEmail == v) {
			Logger.log(TAG, "Edit Email button clicked");
			MODE_EDIT = MODE_EMAIL;
			onEditClicked();
		} else if (rlEditEmail == v) {
			MODE_EDIT = MODE_EMAIL;
			Logger.log(TAG, "Edit Email button clicked");
			onEditClicked();
		} else if (btnConfirmEmail == v) {
			Logger.log(TAG, "Edit Email button clicked");
			onConfirmEmail();
		} else if (btnSaveEmail == v) {
			Logger.log(TAG, "Save Email button clicked");
			onSaveEmail();
		} else if (btnCancelEmail == v) {
			Logger.log(TAG, "Cancel Editing Email button clicked");
			onCancelEmail();
		} else if (textEditPassword == v) {
			Logger.log(TAG, "Edit Password button clicked");
			onEditPassword();
		} else if (rlEditPassword == v) {
			Logger.log(TAG, "Edit Password button clicked");
			onEditPassword();
		} else if (btnSavePassword == v) {
			Logger.log(TAG, "Save Password button clicked");
			onSavePassword();
		} else if (btnCancelPassword == v) {
			Logger.log(TAG, "Cancel Editing Password button clicked");
			onCancelPassword();
		} else if (textEditAddTeam == v) {
			Logger.log(TAG, "Edit / Add Team button clicked");
			MODE_EDIT = MODE_ADDTEAM;
			onEditClicked();
		} else if (rlEditAddTeam == v) {
			Logger.log(TAG, "Edit / Add Team button clicked");
			MODE_EDIT = MODE_ADDTEAM;
			onEditClicked();
		} else if (btnSaveAddTeam == v) {
			Logger.log(TAG, "Save Team button clicked");
			onSaveAddTeam();
		} else if (btnCancelAddTeam == v) {
			Logger.log(TAG, "Cancel Editing Team button clicked");
			onCancelAddTeam();
		} else if (textEditAddPowerPoints == v) {
			Logger.log(TAG, "Add Powerpoint button clicked");
			MODE_EDIT = MODE_ADDPOWERPOINT;
			onEditClicked();
		} else if (rlEditAddPowerPoints == v) {
			Logger.log(TAG, "Add Powerpoint button clicked");
			MODE_EDIT = MODE_ADDPOWERPOINT;
			onEditClicked();
		} else if (btnSaveAddPoints == v) {
			Logger.log(TAG, "Save Powerpoint button clicked");
			onSavePowerPoints();
		} else if (btnCancelAddPoints == v) {
			Logger.log(TAG, "Cancel Adding Powerpoint button clicked");
			onCancelPowerPoints();
		} else if (btnSave == v) {
			Logger.log(TAG, "Save button clicked");
			onSaveClicked();
		} else if (btnCancel == v) {
			Logger.log(TAG, "Cancel button clicked");
			onCancelClicked();
		} else if (llChooseGrade == v ||
				textGrade == v) {
			onChooseGroup();
		} else if (llAddPowerPointsTeamSelect == v ||
				textAddPowerPointsTeam == v) {
			onChooseAddPowerPointsTeam();
		} else if (llAddPowerPointsKindSelect == v ||
				textAddPowerPointsKind == v) {
			onChooseAddPowerPointsKind();
		} else if (llAddPowerPointsStudentSelect == v ||
				textAddPowerPointsStudent == v) {
			onChooseAddPowerPointsStudent();
		} else if (llAddPowerPointsPointSelect == v ||
				textAddPowerPointsPoint == v) {
			onChooseAddPowerPointsPoint();
		}
	}

	// ---- handler ----------
	protected void onChangeAvatarClicked() {
		//
	}

	protected void onSaveClicked() {
		exitEditMode();
	}

	protected void onCancelClicked() {
		exitEditMode();
	}

	protected void enterEditMode() {
		llEditControls.setVisibility(View.VISIBLE);
		textName.setVisibility(View.INVISIBLE);
		rlEdit.setVisibility(View.INVISIBLE);

		editName.setText(textName.getText().toString());

		editName.requestFocus();
		UiUtils.showKeyboard(getActivity(), editName);
		Selection.setSelection(editName.getText(), editName.getText().length());
	}

	protected void exitEditMode() {
		llEditControls.setVisibility(View.INVISIBLE);
		textName.setVisibility(View.VISIBLE);
		rlEdit.setVisibility(View.VISIBLE);

		textName.setText(editName.getText().toString());

		UiUtils.hideKeyboard(getActivity());
	}

	protected void onEditClicked() {
		switch (MODE_EDIT) {
			case MODE_AVATARNAME:
				enterEditMode();
				break;
			case MODE_NAME:
				onEditNames();
				break;
			case MODE_EMAIL:
				onEditEmail();
				break;
			case MODE_ADDTEAM:
				onEditAddTeam();
				break;
			case MODE_ADDPOWERPOINT:
				onEditAddPowerPoints();
				break;
		}
	}

	protected void onEditNames() {
		if (isEditingName) {
			onCancelName();
		}
		if (isEditingEmail) {
			onCancelEmail();
		}

		if (isEditingAddTeam) {
			onCancelAddTeam();
		}

		if (isEditingAddPowerPoint) {
			onCancelPowerPoints();
		}

		if (!isEditingName) {
			isEditingName = true;
			llName.setVisibility(View.GONE);
			llEditingName.setVisibility(View.VISIBLE);
			editFirstName.requestFocus();
			Selection.setSelection(editFirstName.getText(), editFirstName.getText().length());
			Selection.setSelection(editLastName.getText(), editLastName.getText().length());
			Selection.setSelection(editAlias.getText(), editAlias.getText().length());
		}
	}

	protected void onSaveName() {
		if (isEditingName) {
			String szFirstName = editFirstName.getText().toString();
			String szLastName = editLastName.getText().toString();
			String szNickname = editAlias.getText().toString();

			if (szFirstName.length() == 0) {
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
						getString(R.string.coachsettings_firstname_required),
						getActivity());
				return;
			}
			if (szLastName.length() == 0) {
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
						getString(R.string.coachsettings_lastname_required),
						getActivity());
				return;
			}
			if (currentUser._firstName.equals(szFirstName) && currentUser._lastName.equals(szLastName) && currentUser._nickname.equals(szNickname)) {
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
						getString(R.string.coachsettings_nothing_updated),
						getActivity());
				return;
			}

			UiUtils.hideKeyboard(getActivity());

			if (config.USE_LOGIC) {
				// try to save
				UIManager.sharedInstance().showProgressDialog(getActivity(), null, getString(R.string.app_connecting), true);
				ServerManager.sharedInstance().usernameupdate(szFirstName, szLastName, szNickname, new RestCallback<UserService.ResUserUpdate>() {
					@Override
					public void success(UserService.ResUserUpdate resUserUpdate, Response response) {
						UIManager.sharedInstance().dismissProgressDialog();

						// set current user
						UserManager.sharedInstance().updateUserInfo(resUserUpdate);
						EventManager.sharedInstance().post(EVENT_USERNAMEUPDATE_SUCCESS);
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						UIManager.sharedInstance().dismissProgressDialog();
						String errorMsg = ServerManager.getErrorMessageForRetrofitError(retrofitError, getActivity());
						EventManager.sharedInstance().post(EVENT_USERNAMEUPDATE_FAILURE, errorMsg);
					}
				});
			} else {
				EventManager.sharedInstance().post(EVENT_USERNAMEUPDATE_SUCCESS);
			}
		}
	}

	protected void onCancelName() {
		if (isEditingName == true) {
			isEditingName = false;
			llName.setVisibility(View.VISIBLE);
			llEditingName.setVisibility(View.GONE);

			UiUtils.hideKeyboard(getActivity());
		}
	}

	protected void onEditEmail() {

		if (isEditingName == true) {
			onCancelName();
		}
		if (isEditingEmail == true) {
			onCancelEmail();
		}

		if (isEditingAddTeam == true) {
			onCancelAddTeam();
		}

		if (isEditingAddPowerPoint == true) {
			onCancelPowerPoints();
		}

		if (isEditingEmail == false) {
			isEditingEmail = true;
			llEmail.setVisibility(View.GONE);
			llEditingEmail.setVisibility(View.VISIBLE);
			editEmail.requestFocus();
			Selection.setSelection(editEmail.getText(), editEmail.getText().length());
			Selection.setSelection(editConfirmEmail.getText(), editConfirmEmail.getText().length());
		}
	}

	protected void onSaveEmail() {
		if (isEditingEmail) {
			String szEmail = editEmail.getText().toString();
			String szConfirmEmail = editConfirmEmail.getText().toString();

			if (szEmail.length() == 0) {
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
						getString(R.string.coachsettings_email_required),
						getActivity());
				return;
			}
			if (!CommonUtils.isValidEmail(szEmail)) {
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
						getString(R.string.coachsettings_invalid_email),
						getActivity());
				return;
			}
			if (!szEmail.equals(szConfirmEmail)) {
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
						getString(R.string.coachsettings_email_not_match),
						getActivity());
				return;
			}
			if (szEmail.equals(currentUser._email)) {
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
						getString(R.string.coachsettings_nothing_updated),
						getActivity());
				return;
			}

			UiUtils.hideKeyboard(getActivity());

			if (config.USE_LOGIC) {
				// try to save
				UIManager.sharedInstance().showProgressDialog(getActivity(), null, getString(R.string.app_connecting), true);
				ServerManager.sharedInstance().useremailupdate(szEmail, new RestCallback<UserService.ResUserUpdate>() {
					@Override
					public void success(UserService.ResUserUpdate resUserUpdate, Response response) {
						UIManager.sharedInstance().dismissProgressDialog();

						// set current user
						UserManager.sharedInstance().updateUserInfo(resUserUpdate);
						EventManager.sharedInstance().post(EVENT_USEREMAILUPDATE_SUCCESS);
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						UIManager.sharedInstance().dismissProgressDialog();
						String errorMsg = ServerManager.getErrorMessageForRetrofitError(retrofitError, getActivity());
						EventManager.sharedInstance().post(EVENT_USEREMAILUPDATE_FAILURE, errorMsg);
					}
				});
			} else {
				EventManager.sharedInstance().post(EVENT_USEREMAILUPDATE_SUCCESS);
			}
		}
	}

	protected void onCancelEmail() {
		if (isEditingEmail == true) {
			isEditingEmail = false;
			llEmail.setVisibility(View.VISIBLE);
			llEditingEmail.setVisibility(View.GONE);

			UiUtils.hideKeyboard(getActivity());
		}
	}

	protected void onConfirmEmail() {
	}

	protected void onEditPassword() {
		if (isEditingPassword == false) {
			isEditingPassword = true;
			llPassword.setVisibility(View.GONE);
			llEditingPassword.setVisibility(View.VISIBLE);
		}
	}

	protected void onSavePassword() {
		if (isEditingPassword == true) {
			isEditingPassword = false;
			llPassword.setVisibility(View.VISIBLE);
			llEditingPassword.setVisibility(View.GONE);

			UiUtils.hideKeyboard(getActivity());
		}
	}

	protected void onCancelPassword() {
		if (isEditingPassword == true) {
			isEditingPassword = false;
			llPassword.setVisibility(View.VISIBLE);
			llEditingPassword.setVisibility(View.GONE);

			UiUtils.hideKeyboard(getActivity());
		}
	}

	protected void onEditAddTeam() {
		if (isEditingName == true) {
			onCancelName();
		}
		if (isEditingEmail == true) {
			onCancelEmail();
		}

		if (isEditingAddTeam == true) {
			onCancelAddTeam();
		}

		if (isEditingAddPowerPoint == true) {
			onCancelPowerPoints();
		}

		editTeamName.setText("");
		textGrade.setText("");

		_gradeArrayList = TeamManager.sharedInstance()._teamGrades;
		EventManager.sharedInstance().post(EVENT_GETTEAMS_SUCCESS);
	}

	protected void onSaveAddTeam() {
		if (isEditingAddTeam) {
			UiUtils.hideKeyboard(getActivity());

			if (config.USE_LOGIC) {
				// try to login
				String szName = editTeamName.getText().toString();
				String szGrade = "";
				if (_selectedTeamGrade != null)
					szGrade = _selectedTeamGrade.value;
				else {
					AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_caution),
							getString(R.string.coachsettings_select_grade),
							getActivity());
					return;
				}

				UIManager.sharedInstance().showProgressDialog(getActivity(), null, getString(R.string.app_onemoment), true);
				ServerManager.sharedInstance().createTeam(szName, UserManager.sharedInstance()._currentUser._id, szGrade, new RestCallback<TeamService.ResCreateTeam>() {
					@Override
					public void success(TeamService.ResCreateTeam resPostTeam, Response response) {
						UIManager.sharedInstance().dismissProgressDialog();

						List<Team> teams = UserManager.sharedInstance()._currentUser._teams;
						// set current user
						Team team = TeamManager.sharedInstance().parseTeamFromResCreateTeam(resPostTeam);
						teams.add(team);

						Collections.sort(teams, new Comparator<Team>() {
							@Override
							public int compare(Team lhs, Team rhs) {
								return lhs._name.compareToIgnoreCase(rhs._name);
							}
						});
						EventManager.sharedInstance().post(EVENT_POSTTEAM_SUCCESS);

						MainActivity mainActivity = (MainActivity) _parentActivity;
						mainActivity.loadedTeams(teams);
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						UIManager.sharedInstance().dismissProgressDialog();
						String errorMsg = ServerManager.getErrorMessageForRetrofitError(retrofitError, getActivity());
						EventManager.sharedInstance().post(EVENT_POSTTEAM_FAILURE, errorMsg);
					}
				});
			} else {
				EventManager.sharedInstance().post(EVENT_POSTTEAM_SUCCESS);
			}
		}
	}

	protected void onCancelAddTeam() {
		if (isEditingAddTeam == true) {
			isEditingAddTeam = false;
			llAddTeam.setVisibility(View.VISIBLE);
			llEditingAddTeam.setVisibility(View.GONE);

			UiUtils.hideKeyboard(getActivity());
		}
	}

	protected void onEditAddPowerPoints() {
		if (isEditingName == true) {
			onCancelName();
		}
		if (isEditingEmail == true) {
			onCancelEmail();
		}

		if (isEditingAddTeam == true) {
			onCancelAddTeam();
		}

		if (isEditingAddPowerPoint == true) {
			onCancelPowerPoints();
		}

		UiUtils.hideKeyboard(getActivity());

		if (isEditingAddPowerPoint == false) {
			isEditingAddPowerPoint = true;
			llAddPowerPoints.setVisibility(View.GONE);
			llEditingAddPowerPoints.setVisibility(View.VISIBLE);

			_selectedTeam = null;
			_selectedKind = -1;
			_selectedStudent = null;
			_selectedPoint = -1;

			llAddPowerPointsTeamSelect.setVisibility(View.VISIBLE);
			textAddPowerPointsTeam.setText(getResources().getString(R.string.addpowerpoints_hintfor_choose_team));

			llAddPowerPointsKindSelect.setVisibility(View.GONE);
			llAddPowerPointsStudentSelect.setVisibility(View.GONE);
			llAddPowerPointsPointSelect.setVisibility(View.GONE);

			btnSaveAddPoints.setEnabled(false);
		}
	}

	protected void onSavePowerPoints() {
		UiUtils.hideKeyboard(getActivity());

		if (isEditingAddPowerPoint) {
			// call service
			UIManager.sharedInstance().showProgressDialog(getActivity(), null, getString(R.string.coachsettings_adding_powerpoints), true);

			if (_selectedKind == 0) {
				if (_selectedTeam == null) {
					UIManager.sharedInstance().dismissProgressDialog();
					return;
				}

				ServerManager.sharedInstance().extraPointTeam(_selectedTeam._id, _selectedPoint, new RestCallback<List<CommandService.ResExtraPointTeam>>() {
					@Override
					public void success(List<CommandService.ResExtraPointTeam> resExtraPointTeams, Response response) {
						UIManager.sharedInstance().dismissProgressDialog();

						EventManager.sharedInstance().post(EVENT_EXTRAPOINT_SUCCESS);
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						UIManager.sharedInstance().dismissProgressDialog();
						String errorMsg = ServerManager.getErrorMessageForRetrofitError(retrofitError, getActivity());
						EventManager.sharedInstance().post(EVENT_EXTRAPOINT_FAILED, errorMsg);
					}
				});
			} else if (_selectedKind == 1) {
				if (_selectedStudent == null) {
					UIManager.sharedInstance().dismissProgressDialog();
					return;
				}

				ServerManager.sharedInstance().extraPointStudent(_selectedStudent._id, _selectedPoint, new RestCallback<CommandService.ResExtraPointStudent>() {
					@Override
					public void success(CommandService.ResExtraPointStudent resExtraPointStudent, Response response) {
						UIManager.sharedInstance().dismissProgressDialog();
						EventManager.sharedInstance().post(EVENT_EXTRAPOINT_SUCCESS);
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						UIManager.sharedInstance().dismissProgressDialog();

						String errorMsg = ServerManager.getErrorMessageForRetrofitError(retrofitError, getActivity());
						EventManager.sharedInstance().post(EVENT_EXTRAPOINT_FAILED, errorMsg);
					}
				});
			}
		}
	}

	protected void onCancelPowerPoints() {
		if (isEditingAddPowerPoint == true) {
			isEditingAddPowerPoint = false;
			llAddPowerPoints.setVisibility(View.VISIBLE);
			llEditingAddPowerPoints.setVisibility(View.GONE);

			UiUtils.hideKeyboard(getActivity());
		}
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent e) {
		if (EVENT_USERNAMEUPDATE_SUCCESS.equals(e.name)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_success),
					getString(R.string.coachsettings_username_updated),
					getActivity());

			isEditingName = false;
			llName.setVisibility(View.VISIBLE);
			llEditingName.setVisibility(View.GONE);

			initShowData();
		} else if (EVENT_USERNAMEUPDATE_FAILURE.equals(e.name)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.coachsettings_username_update_failed) + " : " +
							(e.object == null ? getString(R.string.error_unknown) : e.object),
					getActivity());
		} else if (EVENT_USEREMAILUPDATE_SUCCESS.equals(e.name)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_success),
					getString(R.string.coachsettings_useremail_updated),
					getActivity());

			isEditingEmail = false;
			llEmail.setVisibility(View.VISIBLE);
			llEditingEmail.setVisibility(View.GONE);

			initShowData();
		} else if (EVENT_USEREMAILUPDATE_FAILURE.equals(e.name)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.coachsettings_useremail_update_failed) + " : " +
							(e.object == null ? getString(R.string.error_unknown) : e.object),
					getActivity());
		} else if (EVENT_GETTEAMS_SUCCESS.equals(e.name)) {
			if (!isEditingAddTeam) {
				isEditingAddTeam = true;
				llAddTeam.setVisibility(View.GONE);
				llEditingAddTeam.setVisibility(View.VISIBLE);
				editTeamName.requestFocus();
				Selection.setSelection(editTeamName.getText(), editTeamName.getText().length());
			}
		} else if (EVENT_GETTEAMS_FAILURE.equals(e.name)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.coachsettings_getteams_failed),
					getActivity());
		} else if (EVENT_POSTTEAM_SUCCESS.equals(e.name)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_success),
					getString(R.string.coachsettings_newteam_added),
					getActivity());

			isEditingAddTeam = false;
			llAddTeam.setVisibility(View.VISIBLE);
			llEditingAddTeam.setVisibility(View.GONE);

			initShowData();
		} else if (EVENT_POSTTEAM_FAILURE.equals(e.name)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.coachsettings_newteam_added_failed) + " : " +
							(e.object == null ? getString(R.string.error_unknown) : e.object),
					getActivity());
		} else if (EventManager.isEvent(e, EVENT_GETALLSTUDENTSBYTEAMID_SUCCESS)) {
			if (_selectedTeam._students == null || _selectedTeam._students.size() == 0) {
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
						getString(R.string.coachsettings_no_students),
						getActivity());
			} else {
//				adapter.replaceWith(_selectedTeam._students);
//				_showList(llAddPowerPointsStudentSelect);
				showList(_selectedTeam._students, SelectorDialog.CHOOSING_STUDENT);
			}
		} else if (EventManager.isEvent(e, EVENT_GETALLSTUDENTSBYTEAMID_FAILED)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.coachsettings_fetch_students_failed) + " : " +
							(e.object == null ? getString(R.string.error_unknown) : e.object),
					getActivity());
		} else if (EventManager.isEvent(e, EVENT_EXTRAPOINT_SUCCESS)) {
			// false
			isEditingAddPowerPoint = false;
			llAddPowerPoints.setVisibility(View.VISIBLE);
			llEditingAddPowerPoints.setVisibility(View.GONE);

			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_success),
					getString(R.string.coachsettings_added_powerpoints),
					getActivity());
		} else if (EventManager.isEvent(e, EVENT_EXTRAPOINT_FAILED)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.coachsettings_add_powerpoints_failed) + " : " +
							(e.object == null ? getString(R.string.error_unknown) : e.object),
					getActivity());
		}
	}

	protected void onSelectedTeam() {

		if (_selectedTeam == null)
			return;

		textAddPowerPointsTeam.setText(_selectedTeam._name);

		_selectedKind = -1;
		_selectedStudent = null;
		_selectedPoint = -1;

		llAddPowerPointsKindSelect.setVisibility(View.VISIBLE);
		textAddPowerPointsKind.setText(getResources().getString(R.string.addpowerpoints_hintfor_choose_kind));

		llAddPowerPointsStudentSelect.setVisibility(View.GONE);
		llAddPowerPointsPointSelect.setVisibility(View.GONE);

		btnSaveAddPoints.setEnabled(false);
	}

	protected void onSelectedKind() {

		if (_selectedKind < 0 || _selectedKind >= _kindArrayList.size())
			return;

		textAddPowerPointsKind.setText(_kindArrayList.get(_selectedKind));

		// team selected
		if (_selectedKind == 0) {
			_selectedPoint = -1;

			// hide student list
			llAddPowerPointsStudentSelect.setVisibility(View.GONE);

			// show power point list
			llAddPowerPointsPointSelect.setVisibility(View.VISIBLE);
			textAddPowerPointsPoint.setText("3. " + getResources().getString(R.string.addpowerpoints_hintfor_choose_powerpoint));
		} else {
			_selectedStudent = null;
			_selectedPoint = -1;

			// show student selection
			llAddPowerPointsStudentSelect.setVisibility(View.VISIBLE);
			textAddPowerPointsStudent.setText(getResources().getString(R.string.addpowerpoints_hintfor_choose_student));

			llAddPowerPointsPointSelect.setVisibility(View.GONE);
		}

		btnSaveAddPoints.setEnabled(false);
	}

	protected void onSelectedStudent() {
		if (_selectedStudent == null)
			return;

		textAddPowerPointsStudent.setText(_selectedStudent._name);

		_selectedPoint = -1;

		llAddPowerPointsPointSelect.setVisibility(View.VISIBLE);
		textAddPowerPointsPoint.setText("4. " + getResources().getString(R.string.addpowerpoints_hintfor_choose_powerpoint));

		btnSaveAddPoints.setEnabled(false);
	}

	protected void onSelectedPoint() {
		if (_selectedPoint == -1)
			return;

		textAddPowerPointsPoint.setText(getStrPowerPoint(_selectedPoint));

		if (_selectedTeam == null)
			return;
		if (_selectedKind == -1)
			return;
		if (_selectedPoint <= 0)
			return;
		if (_selectedKind == 1 && _selectedStudent == null)
			return;

		btnSaveAddPoints.setEnabled(true);
	}

	public static String getStrPowerPoint(int point) {
		if (point == 1)
			return "1 " + BaseActivity.topInstance().getString(R.string.app_powerpoint);
		else if (point > 1)
			return point + " " + BaseActivity.topInstance().getString(R.string.app_powerpoints);
		else
			return "";
	}


	@Override
	protected String getFragmentTitle() {
		return getString(R.string.coachsettings_title);
	}

	static class ViewHolder {
		final TextView gradeView;
		final View parentView;

		ViewHolder(View view) {

			gradeView = (TextView) view.findViewWithTag("grade");
			parentView = view.findViewWithTag("parentView");
		}
	}

}
