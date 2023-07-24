package org.unicefkidpower.schools;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.swrve.sdk.SwrveSDK;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.schools.ble.BleManager;
import org.unicefkidpower.schools.ble.BlePeripheral;
import org.unicefkidpower.schools.context.UserContext;
import org.unicefkidpower.schools.define.CommonUtils;
import org.unicefkidpower.schools.define.KPConstants;
import org.unicefkidpower.schools.define.config;
import org.unicefkidpower.schools.event.EventManager;
import org.unicefkidpower.schools.event.SEvent;
import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.model.Group;
import org.unicefkidpower.schools.model.Student;
import org.unicefkidpower.schools.model.Team;
import org.unicefkidpower.schools.model.TeamManager;
import org.unicefkidpower.schools.model.User;
import org.unicefkidpower.schools.model.UserManager;
import org.unicefkidpower.schools.model.Utils;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.StudentService;
import org.unicefkidpower.schools.server.apimanage.TeamService;
import org.unicefkidpower.schools.server.apimanage.UserService;
import org.unicefkidpower.schools.sync.LinkBandDialog;
import org.unicefkidpower.schools.ui.UiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class SignupActivity extends BaseActivityWithNavBarOld implements View.OnClickListener {
	public static final String TAG = "SignupActivity";
	public static final String SIGNUP_USERSIGNEDUP_SUCCESS = "SIGNUP_USERSIGNEDUP_SUCCESS";
	public static final String SIGNUP_USERSIGNEDUP_FAILED = "SIGNUP_USERSIGNEDUP_FAILED";
	public static final String SIGNUP_BYDEVICEIDS_SUCCESS = "SIGNUP_BYDEVICEIDS_SUCCESS";
	public static final String SIGNUP_BYDEVICEIDS_FAILED = "SIGNUP_BYDEVICEIDS_FAILED";
	public static final String EVENT_CREATESTUDENT_SUCCESS = "SIGNUP_EVENT_CREATESTUDENT_SUCCESS";
	public static final String EVENT_CREATESTUDENT_FAILED = "SIGNUP_EVENT_CREATESTUDENT_FAILED";
	public static final String EVENT_CREATETEAM_SUCCESS = "SIGNUP_EVENT_CREAETEAM_SUCCESS";
	public static final String EVENT_CREATETEAM_FAILED = "SIGNUP_EVENT_CREATETEAM_FAILED";
	public static final int STAGE_CREATING_PROFILE = 0;
	public static final int STAGE_CREATING_TEAM = 1;
	public static final int STAGE_REGISTERING_BAND = 3;
	public static final int STAGE_TAKING_SELFIE = 4;
	public static final int STAGE_FINISHED = 20;
	public static final int STAGE_TEAMSETUP = 50;
	public static final int TIMEOUT_FOR_SCANNING_ANYDEVICE = 5 * 1000; //5 seconds, this means the app can find at least one device within 5 seconds
	public static boolean HIDE_AVATAR = true;
	// validate constants
	private final int VALIDATE_SUCCESS = 0;
	private final int VALIDATE_FIRSTNAME_EMPTY = 0x01;
	private final int VALIDATE_LASTNAME_EMPTY = 0x02;
	private final int VALIDATE_EMAIL_EMPTY = 0x04;
	private final int VALIDATE_EMAILCONFIRM_EMPTY = 0x08;
	private final int VALIDATE_EMAIL_NOTEQUAL = 0x10;
	private final int VALIDATE_EMAIL_INCORRECT_FORMAT = 0x20;
	private final int VALIDATE_PASSWORD_EMPTY = 0x40;
	private final int VALIDATE_PASSWORDCONFIRM_EMPTY = 0x80;
	private final int VALIDATE_PASSWORD_NOTEQUAL = 0x100;
	protected View viewSignupContent;
	protected View viewSetupTeamContent;
	// step 1
	protected View viewStep1Contents;
	protected TextView textStep1Title;
	protected TextView textStep1Description;
	protected Button btnCreateProfile;
	protected EditText editFirstName;
	protected EditText editLastName;
	protected EditText editAlias;
	protected EditText editEmail;
	protected EditText editEmailConfirm;
	protected EditText editPassword;
	protected EditText editPasswordConfirm;
	// step 2
	protected View viewStep2Contents;
	protected TextView textStep2Title;
	protected View viewStep2ContentsFirstPart;
	protected View viewStep2ContentsSecondPart;
	protected EditText editWristbandCode;
	protected Button btnNextWristband;
	protected TextView textStep2Description;
	protected RelativeLayout rlScannedItems;
	protected ListView listViewScannedItems;
	protected LeDeviceListAdapter adapter;
	protected TextView textGoName;
	protected TextView textSkipRegisterBand;
	// step 3
	protected View viewStep3Contents;
	protected View llStep3Title;
	protected TextView textStep3Title;
	protected ImageView imgAvatar;
	protected Button btnTakeAvatar;
	protected View rlSkipAvatar;
	// setup team
	protected Button btnSetupTeam;
	protected BlePeripheral _selectedPeripheral;
	protected ArrayList<BlePeripheral> _registeredPeripherals;
	protected int _stage = STAGE_CREATING_PROFILE;
	protected boolean _isConnected = false;
	protected boolean _isNamed = false;
	protected boolean _isPersonal = false;
	protected boolean _isTiming = false;
	protected Handler _handlerForRestartScanning;
	protected Runnable _runnableForRestartScanning;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);

		FlurryAgent.onStartSession(this, "Signup Activity");

		viewStep1Contents = findViewById(R.id.llStep1Contents);
		textStep1Title = (TextView) findViewById(R.id.textStep1Title);
		textStep1Description = (TextView) findViewById(R.id.textStep1Description);
		btnCreateProfile = (Button) findViewById(R.id.btnCreateProfile);
		editFirstName = (EditText) findViewById(R.id.editFirstName);
		editLastName = (EditText) findViewById(R.id.editLastName);
		editAlias = (EditText) findViewById(R.id.editAlias);
		editEmail = (EditText) findViewById(R.id.editEmail);
		editEmailConfirm = (EditText) findViewById(R.id.editEmailConfirm);
		editPassword = (EditText) findViewById(R.id.editPassword);
		editPasswordConfirm = (EditText) findViewById(R.id.editPasswordConfirm);

		// step 2
		viewStep2Contents = findViewById(R.id.llStep2Contents);
		textStep2Title = (TextView) findViewById(R.id.textStep2Title);
		viewStep2ContentsFirstPart = findViewById(R.id.llStep2ContentsFirstPart);
		viewStep2ContentsSecondPart = findViewById(R.id.llStep2ContentsSecondPart);
		editWristbandCode = (EditText) findViewById(R.id.editWristbandCode);
		btnNextWristband = (Button) findViewById(R.id.btnNextWristband);
		textStep2Description = (TextView) findViewById(R.id.textStep2Description);
		rlScannedItems = (RelativeLayout) findViewById(R.id.rlScannedItems);
		listViewScannedItems = (ListView) findViewById(R.id.listScannedItems);
		textGoName = (TextView) findViewById(R.id.textGoName);

		textSkipRegisterBand = (TextView) findViewById(R.id.textSkip);
		textSkipRegisterBand.setOnClickListener(this);

		adapter = new LeDeviceListAdapter(this);
		listViewScannedItems.setAdapter(adapter);
		listViewScannedItems.setOnItemClickListener(createOnItemClickListener());

		editWristbandCode.addTextChangedListener(new TextWatcher() {
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
				// text changed for wristband code
				_onWristbandCodeInputed();
			}
		});

		// step 3
		viewStep3Contents = findViewById(R.id.llStep3Contents);
		llStep3Title = findViewById(R.id.llStep3Title);
		textStep3Title = (TextView) findViewById(R.id.textStep3Title);
		imgAvatar = (ImageView) findViewById(R.id.imgAvatar);
		btnTakeAvatar = (Button) findViewById(R.id.btnTakeAvatar);
		rlSkipAvatar = findViewById(R.id.rlSkipAvatar);

		btnCreateProfile.setOnClickListener(this);
		btnNextWristband.setOnClickListener(this);

		btnTakeAvatar.setOnClickListener(this);
		rlSkipAvatar.setOnClickListener(this);

		_selectedPeripheral = null;
		_registeredPeripherals = new ArrayList<BlePeripheral>();

		viewSignupContent = findViewById(R.id.llSignup);
		viewSetupTeamContent = findViewById(R.id.llSetupTeam);

		btnSetupTeam = (Button) findViewById(R.id.btnSetupTeam);
		btnSetupTeam.setOnClickListener(this);

		_handlerForRestartScanning = new Handler(this.getMainLooper());

		_stage = STAGE_CREATING_PROFILE;

		// hide step 3
		if (HIDE_AVATAR)
			llStep3Title.setVisibility(View.GONE);// check stage


		if (UserContext.sharedInstance().isSignUp()) {
			_stage = UserContext.sharedInstance().signUpStage();
			if (_stage == STAGE_CREATING_PROFILE) {
				// step 1
				UserManager.sharedInstance()._currentUser = UserContext.sharedInstance().getUser();
				goStep(1);
			} else if (_stage == STAGE_CREATING_TEAM) {
				UserManager.sharedInstance()._currentUser = UserContext.sharedInstance().getUser();
				User user = UserManager.sharedInstance()._currentUser;
				ServerManager.sharedInstance().setAccessToken(user._access_token);
				goStep(1);
				editFirstName.setText(user._firstName);
				editLastName.setText(user._lastName);
				editAlias.setText(user._nickname);
				editEmail.setText(user._email);
				editEmailConfirm.setText(user._email);
			} else if (_stage == STAGE_REGISTERING_BAND) {
				UserManager.sharedInstance()._currentUser = UserContext.sharedInstance().getUser();
				ServerManager.sharedInstance().setAccessToken(UserManager.sharedInstance()._currentUser._access_token);
				goStep(2);
			} else if (_stage == STAGE_TAKING_SELFIE) {
				UserManager.sharedInstance()._currentUser = UserContext.sharedInstance().getUser();
				ServerManager.sharedInstance().setAccessToken(UserManager.sharedInstance()._currentUser._access_token);
				goStep(3);
			} else if (_stage == STAGE_FINISHED) {
				UserManager.sharedInstance()._currentUser = UserContext.sharedInstance().getUser();
				ServerManager.sharedInstance().setAccessToken(UserManager.sharedInstance()._currentUser._access_token);
				goStep(4);
			} else if (_stage == STAGE_TEAMSETUP) {
				UserManager.sharedInstance()._currentUser = UserContext.sharedInstance().getUser();
				ServerManager.sharedInstance().setAccessToken(UserManager.sharedInstance()._currentUser._access_token);
				onSetupTeamClicked();
			}
		} else {
			goStep(1);

			UserContext.sharedInstance().setSignUp(true);
			UserContext.sharedInstance().setSignUpStage(_stage);
			UserContext.sharedInstance().setUser(UserManager.sharedInstance()._currentUser);
		}
	}

	@Override
	public void onBackPressed() {
		if (_fromActivity.equalsIgnoreCase(SplashActivity.class.getName())) {
			UserContext.sharedInstance().setSignUp(false);
			Intent intent = new Intent(this, WelcomeActivity.class);
			startActivity(intent);
			finish();
		} else {
			if (_stage == STAGE_CREATING_PROFILE) {
				UserContext.sharedInstance().setSignUp(false);
				super.onBackPressed();
			} else {
				// disable back
			}
		}
	}

	@Override
	protected boolean isUseEvent() {
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	// ---- onclick
	@Override
	public void onClick(View v) {
		if (btnCreateProfile == v) {
			onCreateProfileClicked();
		} else if (btnNextWristband == v) {
			onNextWristbandClicked();
		} else if (btnTakeAvatar == v) {
			onTakeAvatarClicked();
		} else if (rlSkipAvatar == v) {
			onSkipAvatarClicked();
		} else if (btnSetupTeam == v) {
			onSetupTeamClicked();
		} else if (textSkipRegisterBand == v) {
			onSkipRegisterBandClicked();
		}
	}

	protected void onSkipRegisterBandClicked() {
		Logger.log(TAG, "Skip This Step For Now button has been clicked");

		// stop scanning
		_stopScan();

		_stage = STAGE_FINISHED;

		UserContext.sharedInstance().setSignUp(true);
		UserContext.sharedInstance().setSignUpStage(_stage);

		goStep(4);
	}

	protected void _stopScan() {
		// stop scanning
		BleManager.sharedInstance().stopScan(0);

		_handlerForRestartScanning.removeCallbacks(_runnableForRestartScanning);
	}

	// ----- action handlers
	protected void onCreateProfileClicked() {
		UiUtils.hideKeyboard(this);

		if (config.USE_LOGIC) {
			// check profiles
			if (UserManager.sharedInstance()._currentUser == null) {
				// not registered user by regCode
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
						getString(R.string.signup_user_not_registered),
						SignupActivity.this);
				return;
			}

			Logger.log(TAG, "Create My Profile button clicked");

			if (_stage == STAGE_CREATING_PROFILE) {
				// --------- sign up ---------------------------------
				int invalidate = isValidateProfiles();
				displayInvalidateFields(invalidate);
				if (invalidate != VALIDATE_SUCCESS) {
					AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
							getString(R.string.signup_correct_invalid_fields),
							SignupActivity.this);
					return;
				}
				String password = editPassword.getText().toString();
				String email = editEmail.getText().toString();
				String firstName = editFirstName.getText().toString();
				String lastName = editLastName.getText().toString();
				// nick name
				String nickname = editAlias.getText().toString().trim();
				if (nickname.equals(""))
					nickname = getString(R.string.coach_string);
				nickname = Utils.getStudentName(nickname);
				editAlias.setText(nickname);

				Group group = UserManager.sharedInstance()._currentUser._group;
				String userType = UserManager.sharedInstance()._currentUser._userType;

				UIManager.sharedInstance().showProgressDialog(this, null, getString(R.string.signup_creating_profile), true);
				// create profile
				ServerManager.sharedInstance().signup(group._id, email, password, firstName, lastName, nickname, userType,
						new RestCallback<UserService.ResSignup>() {
							@Override
							public void success(UserService.ResSignup resSignup, Response response) {
								Logger.log(TAG, "signup success! : email = %s", resSignup.email);
								UIManager.sharedInstance().dismissProgressDialog();

								FlurryAgent.setUserId(resSignup.email);
								FlurryAgent.onEvent("Sign-up");

								Crashlytics.setUserIdentifier(resSignup.id + "");
								Crashlytics.setUserEmail(resSignup.email);
								Crashlytics.setUserName(resSignup.firstName + " " + resSignup.lastName);

								// registered user
								EventManager.sharedInstance().post(SIGNUP_USERSIGNEDUP_SUCCESS, resSignup);
							}

							@Override
							public void failure(RetrofitError retrofitError, String message) {
								Logger.error(TAG, "signup failed!" + retrofitError.getMessage());
								UIManager.sharedInstance().dismissProgressDialog();
								String errorMsg = ServerManager.getErrorMessageForRetrofitError(retrofitError, SignupActivity.this);
								if (retrofitError.getResponse() != null && retrofitError.getResponse().getStatus() == 400)
									errorMsg = ServerManager.getResponseAsString(retrofitError.getResponse());
								EventManager.sharedInstance().post(SIGNUP_USERSIGNEDUP_FAILED, errorMsg);
							}
						});
			} else if (_stage == STAGE_CREATING_TEAM) {
				_createTeam();
			}
		} else {
			goStep(2);
		}
	}

	protected void onNextWristbandClicked() {
		UiUtils.hideKeyboard(this);

		// first step of step2
		if (viewStep2ContentsSecondPart.getVisibility() == View.GONE) {
			if (config.USE_LOGIC) {

				String strWristbandCode = editWristbandCode.getText().toString();
				if (CommonUtils.isEmpty(strWristbandCode)) {
					AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
							getString(R.string.signup_enter_bandid),
							SignupActivity.this);
					return;
				}

				for (BlePeripheral peripheral : BleManager.sharedInstance().getScannedPeripherals()) {
					if (!CommonUtils.isEmpty(peripheral.getCode()) &&
							peripheral.getCode().equals(strWristbandCode)) {
						if (_registeredPeripherals.contains(peripheral)) {
							AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
									getString(R.string.signup_band_already_claimed),
									SignupActivity.this);
							return;
						} else {
							_selectedPeripheral = peripheral;
							break;
						}
					}
				}

				// register
				if (_selectedPeripheral == null) {
					AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
							getString(R.string.signup_bandid_not_found),
							SignupActivity.this);
					return;
				}

				// stop scanning
				_stopScan();

				Team team = UserManager.sharedInstance()._currentUser._teams.get(0);
				int id = UserManager.sharedInstance()._currentUser._id;
				String name = UserManager.sharedInstance()._currentUser._nickname;

				SwrveSDK.event(KPConstants.SWRVE_BAND_LINK_STARTED);
				Logger.log(TAG, "Linking for coach name:%s, device:%s", name, _selectedPeripheral.getMACAddress());

				LinkBandDialog dialog = new LinkBandDialog(this, new LinkBandDialog.LinkBandDialogListener() {
					@Override
					public void onCompleted() {
						Logger.log(TAG, "success registering band for coach");
						SwrveSDK.event(KPConstants.SWRVE_BAND_LINK_SUCCESS);

						_createStudent();
					}

					@Override
					public void onFailed(int error_code, String message) {
						Logger.error(TAG, "failed registering band for coach(code=%d, msg=\"%s\")", error_code, message);
						Map<String, String> payload = new HashMap<>();
						payload.put("error_code", "" + error_code);
						payload.put("error_msg", message);
						SwrveSDK.event(KPConstants.SWRVE_BAND_LINK_ERROR, payload);

						AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
								getString(R.string.signup_registerband_failed),
								SignupActivity.this);
					}
				});
				dialog.setParameters(team, id, name, _selectedPeripheral.getMACAddress(), 0);
				dialog.show();
			} else {
				goNextStepForWristband();
			}
		} else {
			if (HIDE_AVATAR) {
				_stage = STAGE_FINISHED;

				UserContext.sharedInstance().setSignUp(true);
				UserContext.sharedInstance().setSignUpStage(_stage);

				goStep(4);
			} else {
				goStep(3);
			}
		}
	}

	public void displayInvalidateFields(int invalidate) {

		editFirstName.setBackgroundResource(R.drawable.kidpower_inputboxbg);
		editLastName.setBackgroundResource(R.drawable.kidpower_inputboxbg);
		editEmail.setBackgroundResource(R.drawable.kidpower_inputboxbg);
		editEmailConfirm.setBackgroundResource(R.drawable.kidpower_inputboxbg);
		editPassword.setBackgroundResource(R.drawable.kidpower_inputboxbg);
		editPasswordConfirm.setBackgroundResource(R.drawable.kidpower_inputboxbg);

		if ((invalidate & VALIDATE_FIRSTNAME_EMPTY) != 0) {
			editFirstName.setBackgroundResource(R.drawable.kidpower_inputbox_invalid_bg);
		}

		if ((invalidate & VALIDATE_LASTNAME_EMPTY) != 0) {
			editLastName.setBackgroundResource(R.drawable.kidpower_inputbox_invalid_bg);
		}

		if ((invalidate & VALIDATE_EMAIL_EMPTY) != 0 ||
				(invalidate & VALIDATE_EMAIL_NOTEQUAL) != 0 ||
				(invalidate & VALIDATE_EMAIL_INCORRECT_FORMAT) != 0) {
			editEmail.setBackgroundResource(R.drawable.kidpower_inputbox_invalid_bg);
		}

		if ((invalidate & VALIDATE_EMAILCONFIRM_EMPTY) != 0 ||
				(invalidate & VALIDATE_EMAIL_NOTEQUAL) != 0) {
			editEmailConfirm.setBackgroundResource(R.drawable.kidpower_inputbox_invalid_bg);
		}

		if ((invalidate & VALIDATE_PASSWORD_EMPTY) != 0 ||
				(invalidate & VALIDATE_PASSWORD_NOTEQUAL) != 0) {
			editPassword.setBackgroundResource(R.drawable.kidpower_inputbox_invalid_bg);
		}

		if ((invalidate & VALIDATE_PASSWORDCONFIRM_EMPTY) != 0 ||
				(invalidate & VALIDATE_PASSWORD_NOTEQUAL) != 0) {
			editPasswordConfirm.setBackgroundResource(R.drawable.kidpower_inputbox_invalid_bg);
		}
	}

	protected void goNextStepForWristband() {
		viewStep2ContentsFirstPart.setVisibility(View.GONE);
		viewStep2ContentsSecondPart.setVisibility(View.VISIBLE);

		textGoName.setText((getString(R.string.app_go) + " " +
				UserManager.sharedInstance()._currentUser._nickname).toUpperCase());
	}

	protected void onTakeAvatarClicked() {
		_stage = STAGE_FINISHED;

		UserContext.sharedInstance().setSignUp(true);
		UserContext.sharedInstance().setSignUpStage(_stage);

		goStep(4);
	}

	protected void onSkipAvatarClicked() {
		_stage = STAGE_FINISHED;

		UserContext.sharedInstance().setSignUp(true);
		UserContext.sharedInstance().setSignUpStage(_stage);

		goStep(4);
	}

	protected void onSetupTeamClicked() {

		Logger.log(TAG, "Set Up My Team button has been clicked");

		_stage = STAGE_TEAMSETUP;

		UserContext.sharedInstance().setSignUp(true);
		UserContext.sharedInstance().setSignUpStage(_stage);

		// goto setup team
		Intent intent = new Intent(this, TeamSetupActivity.class);
		startActivity(intent);
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent e) {
		if (EventManager.isEvent(e, SIGNUP_USERSIGNEDUP_FAILED)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.signup_createprofile_failed) + ": " +
							((e.object != null) ? e.object : getString(R.string.error_unknown)),
					SignupActivity.this);
		} else if (EventManager.isEvent(e, SIGNUP_USERSIGNEDUP_SUCCESS)) {

			// set current user
			User user = UserManager.sharedInstance().parseUserForSignupRes((UserService.ResSignup) e.object);
			UserManager.sharedInstance()._currentUser = user;
			//UserContext.sharedInstance().setCrashUserInfo(user._firstName + " " + user._lastName, user._email);

			_stage = STAGE_CREATING_TEAM;

			UserContext.sharedInstance().setSignUp(true);
			UserContext.sharedInstance().setSignUpStage(_stage);
			UserContext.sharedInstance().setUser(user);

			// create a team
			_createTeam();
		} else if (EventManager.isEvent(e, SIGNUP_BYDEVICEIDS_SUCCESS)) {
			ArrayList<BlePeripheral> registeredDevices = (ArrayList<BlePeripheral>) e.object;
			_registeredPeripherals = registeredDevices;
		} else if (EventManager.isEvent(e, EVENT_CREATESTUDENT_SUCCESS)) {
			_stage = STAGE_TAKING_SELFIE;

			// reigser band success
			UserContext.sharedInstance().setSignUp(true);
			UserContext.sharedInstance().setSignUpStage(_stage);

			goNextStepForWristband();
		} else if (EventManager.isEvent(e, EVENT_CREATESTUDENT_FAILED)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.signup_createstudent_failed) + ": " +
							(e.object == null ? getString(R.string.error_unknown) : e.object),
					SignupActivity.this);
		} else if (EventManager.isEvent(e, EVENT_CREATETEAM_SUCCESS)) {
			// add a team
			Team team = TeamManager.sharedInstance().parseTeamFromResCreateTeam((TeamService.ResCreateTeam) e.object);
			UserManager.sharedInstance()._currentUser._teams.add(team);

			_stage = STAGE_REGISTERING_BAND;

			UserContext.sharedInstance().setSignUp(true);
			UserContext.sharedInstance().setSignUpStage(_stage);
			UserContext.sharedInstance().setUser(UserManager.sharedInstance()._currentUser);

			// go next step
			goStep(2);

			// start scanning
			boolean scanning = BleManager.sharedInstance().scanForPeripheralsWithServices(null, true);
			if (scanning) {
				_runnableForRestartScanning = new Runnable() {
					@Override
					public void run() {
						if (_stage == STAGE_REGISTERING_BAND) {
							if (BleManager.sharedInstance().getScannedPeripherals().size() == 0) {
								// have to restart ble
								BleManager.sharedInstance().restartScanForPeripherals();
							}
						} else {
							//
						}
					}
				};
				_handlerForRestartScanning.postDelayed(_runnableForRestartScanning, TIMEOUT_FOR_SCANNING_ANYDEVICE);
			}

		} else if (EventManager.isEvent(e, EVENT_CREATETEAM_FAILED)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.signup_createteam_failed) + ": " +
							(e.object == null ? getString(R.string.error_unknown) : e.object),
					SignupActivity.this);
		}
	}

	// UI operations for steps
	protected void goStep(int step) {
		if (step == 1 || step == 2 || step == 3) {
			viewSignupContent.setVisibility(View.VISIBLE);
			viewSetupTeamContent.setVisibility(View.GONE);
			;
		} else {
			viewSignupContent.setVisibility(View.GONE);
			viewSetupTeamContent.setVisibility(View.VISIBLE);
		}

		if (step == 1) {
			textStep1Title.setTextColor(CommonUtils.getColorFromRes(getResources(), R.color.label_step_selected));
			viewStep1Contents.setVisibility(View.VISIBLE);
			textStep1Description.setVisibility(View.GONE);

			textStep2Title.setTextColor(CommonUtils.getColorFromRes(getResources(), R.color.label_step_nonselected));
			viewStep2Contents.setVisibility(View.GONE);
			textStep2Description.setVisibility(View.GONE);

			textStep3Title.setTextColor(CommonUtils.getColorFromRes(getResources(), R.color.label_step_nonselected));
			viewStep3Contents.setVisibility(View.GONE);
		} else if (step == 2) {
			textStep2Title.setTextColor(CommonUtils.getColorFromRes(getResources(), R.color.label_step_selected));
			viewStep2Contents.setVisibility(View.VISIBLE);
			textStep2Description.setVisibility(View.GONE);
			viewStep2ContentsFirstPart.setVisibility(View.VISIBLE);
			viewStep2ContentsSecondPart.setVisibility(View.GONE);


			textStep1Title.setTextColor(CommonUtils.getColorFromRes(getResources(), R.color.label_step_nonselected));

			//textStep1Description.setVisibility(View.VISIBLE);
			// remove "Welcome coach lopez!"
			textStep1Description.setVisibility(View.GONE);

			viewStep1Contents.setVisibility(View.GONE);

			textStep3Title.setTextColor(CommonUtils.getColorFromRes(getResources(), R.color.label_step_nonselected));
			viewStep3Contents.setVisibility(View.GONE);
		} else if (step == 3) {
			textStep3Title.setTextColor(CommonUtils.getColorFromRes(getResources(), R.color.label_step_selected));
			viewStep3Contents.setVisibility(View.VISIBLE);

			textStep1Title.setTextColor(CommonUtils.getColorFromRes(getResources(), R.color.label_step_nonselected));

			//textStep1Description.setVisibility(View.VISIBLE);
			// remove "Welcome coach lopez!"
			textStep1Description.setVisibility(View.GONE);

			viewStep1Contents.setVisibility(View.GONE);

			textStep2Title.setTextColor(CommonUtils.getColorFromRes(getResources(), R.color.label_step_nonselected));

			textStep2Description.setVisibility(View.VISIBLE);
			textStep2Description.setVisibility(View.GONE);

			viewStep2Contents.setVisibility(View.GONE);
		}
	}

	// validate functions
	protected int isValidateProfiles() {
		int validate = VALIDATE_SUCCESS;
		if (editFirstName.getText() == null ||
				CommonUtils.isEmpty(editFirstName.getText().toString())) {
			validate |= VALIDATE_FIRSTNAME_EMPTY;
		}
		if (editLastName.getText() == null ||
				CommonUtils.isEmpty(editLastName.getText().toString())) {
			validate |= VALIDATE_LASTNAME_EMPTY;
		}

		if (editEmail.getText() == null ||
				CommonUtils.isEmpty(editEmail.getText().toString())) {
			validate |= VALIDATE_EMAIL_EMPTY;
		} else {
			if (!CommonUtils.isValidEmail(editEmail.getText().toString())) {
				validate |= VALIDATE_EMAIL_INCORRECT_FORMAT;
			}
		}

		if (editEmailConfirm.getText() == null ||
				CommonUtils.isEmpty(editEmailConfirm.getText().toString())) {
			validate |= VALIDATE_EMAILCONFIRM_EMPTY;
		}

		if ((editEmail.getText() != null && !CommonUtils.isEmpty(editEmail.getText().toString())) &&
				(editEmailConfirm.getText() != null && !CommonUtils.isEmpty(editEmailConfirm.getText().toString())) &&
				!editEmail.getText().toString().equals(editEmailConfirm.getText().toString())) {
			validate |= VALIDATE_EMAIL_NOTEQUAL;
		}

		if (editPassword.getText() == null ||
				CommonUtils.isEmpty(editPassword.getText().toString())) {
			validate |= VALIDATE_PASSWORD_EMPTY;
		}
		if (editPasswordConfirm.getText() == null ||
				CommonUtils.isEmpty(editPasswordConfirm.getText().toString())) {
			validate |= VALIDATE_PASSWORDCONFIRM_EMPTY;
		}
		if ((editPassword.getText() != null && !CommonUtils.isEmpty(editPassword.getText().toString())) &&
				(editPasswordConfirm.getText() != null && !CommonUtils.isEmpty(editPasswordConfirm.getText().toString())) &&
				!editPassword.getText().toString().equals(editPasswordConfirm.getText().toString())) {
			validate |= VALIDATE_PASSWORD_NOTEQUAL;
		}
		return validate;
	}

	// called when user input wristbandcode
	protected void _onWristbandCodeInputed() {
		if (editWristbandCode.getText() == null ||
				CommonUtils.isEmpty(editWristbandCode.getText().toString())) {
			rlScannedItems.setVisibility(View.GONE);
			_selectedPeripheral = null;
		} else {
			String strWristbandCode = editWristbandCode.getText().toString();
			strWristbandCode = strWristbandCode.toUpperCase();

			ArrayList<BlePeripheral> scannedItems = BleManager.sharedInstance().getScannedPeripherals();
			ArrayList<BlePeripheral> matchedItems = new ArrayList<BlePeripheral>();
			for (int i = 0; i < scannedItems.size(); i++) {
				BlePeripheral peripheral = scannedItems.get(i);
				if (peripheral.getCode().startsWith(strWristbandCode)) {

					// check registered or not
					boolean registered = false;
					for (int j = 0; j < _registeredPeripherals.size(); j++) {
						BlePeripheral registeredPeripheral = _registeredPeripherals.get(i);
						if (registeredPeripheral.equals(peripheral)) {
							registered = true;
							break;
						}
					}

					// check already registered or not
					if (!registered) {
						matchedItems.add(peripheral);
						if (peripheral.getCode().equalsIgnoreCase(strWristbandCode))
							_selectedPeripheral = peripheral;
					}
				}
			}

			if (matchedItems.size() == 0) {
				rlScannedItems.setVisibility(View.GONE);
				return;
			} else {
				if (strWristbandCode.length() >= 5) {
					rlScannedItems.setVisibility(View.GONE);
				} else {
					int[] location = {0, 0};
					rlScannedItems.setVisibility(View.VISIBLE);
					adapter.replaceWith(matchedItems);

					editWristbandCode.getLocationOnScreen(location);
					int editX = location[0];
					int editY = location[1];

					ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);
					scrollView.getLocationOnScreen(location);
					int scrollX = location[0];
					int scrollY = location[1];

					int scrollOffset = scrollView.getScrollY();
					int left = editX - scrollX;
					RelativeLayout.LayoutParams editParams = (RelativeLayout.LayoutParams) editWristbandCode.getLayoutParams();
					int top = scrollOffset + editY - scrollY + editParams.height;

					RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rlScannedItems.getLayoutParams();
					layoutParams.leftMargin = left - (int) (KPConstants.SCANNEDITEM_LEFTMARGIN_DELTA * CommonUtils.getDeviceDensity(this));
					layoutParams.topMargin = top;
					rlScannedItems.setLayoutParams(layoutParams);
					rlScannedItems.bringToFront();
				}
			}
		}
	}

	protected void _createTeam() {
		User currentUser = UserManager.sharedInstance()._currentUser;
		if (currentUser == null) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.signup_create_userfirst),
					SignupActivity.this);
			return;
		}

		// call api
		UIManager.sharedInstance().showProgressDialog(this, null, getString(R.string.signup_creating_team), true);

		Logger.log(TAG, "Creating a team - Team name: %s, Team Grade: %d", TeamManager.DEFAULT_TEAMNAME, TeamManager.DEFAULT_TEAMGRADE);

		ServerManager.sharedInstance().createTeam(
				TeamManager.DEFAULT_TEAMNAME,
				currentUser._id,
				CreateTeamFragment.getTeamGradeString(TeamManager.DEFAULT_TEAMGRADE),
				new RestCallback<TeamService.ResCreateTeam>() {
					@Override
					public void success(TeamService.ResCreateTeam resCreateTeam, Response response) {
						UIManager.sharedInstance().dismissProgressDialog();

						Logger.log(TAG, "Team has successfully been created - Team name: %s, Team Grade: %d", TeamManager.DEFAULT_TEAMNAME, TeamManager.DEFAULT_TEAMGRADE);
						EventManager.sharedInstance().post(EVENT_CREATETEAM_SUCCESS, resCreateTeam);
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						UIManager.sharedInstance().dismissProgressDialog();

						Logger.error(TAG, "Creating team has been failed.");
						String errorMsg = ServerManager.getErrorMessageForRetrofitError(retrofitError, SignupActivity.this);
						EventManager.sharedInstance().post(EVENT_CREATETEAM_FAILED, errorMsg);
					}
				});
	}

	protected void _createStudent() {
		if (_selectedPeripheral == null) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.signup_enter_bandid),
					SignupActivity.this);
			return;
		}

		User currentUser = UserManager.sharedInstance()._currentUser;
		if (currentUser == null) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.signup_create_userfirst),
					SignupActivity.this);
			return;
		}

		if (currentUser._teams.size() == 0) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.signup_create_teamfirst),
					SignupActivity.this);
			return;
		}

		// use band mac address for identifier, not use device code (last 5 character of mac address)
		String deviceId = _selectedPeripheral.getMACAddress();//_registeringDevice.peripheral().getCode();

		// try to create student
		UIManager.sharedInstance().showProgressDialog(this, null, getString(R.string.signup_creating_student), true);

		Logger.log(TAG, "Creating student - Name: %s, Device ID: %s", currentUser._nickname, deviceId);

		// call api
		ServerManager.sharedInstance().createStudent(currentUser._nickname, // name
				Student.GENDER_UNDEFINED, deviceId, // deviceId
				currentUser._teams.get(0)._id,// teamId
				true, // isCoach
				"", // imageSrc
				new RestCallback<StudentService.ResCreateStudent>() {
					@Override
					public void success(StudentService.ResCreateStudent resCreateStudent, Response response) {
						UIManager.sharedInstance().dismissProgressDialog();
						EventManager.sharedInstance().post(EVENT_CREATESTUDENT_SUCCESS, resCreateStudent);

						Logger.log(TAG, "Student has successfully been created");
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						UIManager.sharedInstance().dismissProgressDialog();
						String errorMsg = ServerManager.getErrorMessageForRetrofitError(retrofitError, SignupActivity.this);
						EventManager.sharedInstance().post(EVENT_CREATESTUDENT_FAILED, errorMsg);

						Logger.error(TAG, "Creating student has been failed");
					}
				});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			rlScannedItems.setVisibility(View.GONE);
		}
		return false;
	}

	// ------------------- list for devices ----------------------------------

	private AdapterView.OnItemClickListener createOnItemClickListener() {
		return new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final BlePeripheral device = (BlePeripheral) adapter.getItem(position);
				if (device == null)
					return;

				_onPeripheralSelected(device);
			}
		};
	}

	protected void _onPeripheralSelected(BlePeripheral device) {
		editWristbandCode.setText(device.getCode());
		Selection.setSelection(editWristbandCode.getText(), editWristbandCode.getText().toString().length());

		rlScannedItems.setVisibility(View.GONE);
		_selectedPeripheral = device;

		UiUtils.hideKeyboard(SignupActivity.this);
	}

	@Override
	public String getNavbarTitle() {
		return getString(R.string.signup_title);
	}

	static class ViewHolder {
		final TextView codeTextView;
		final View parentView;

		ViewHolder(View view) {

			codeTextView = (TextView) view.findViewWithTag("code");
			parentView = (View) view.findViewWithTag("parentView");
		}
	}

	public class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BlePeripheral> mLeDevices;
		private LayoutInflater mInflator;

		public LeDeviceListAdapter(Context context) {
			super();
			mLeDevices = new ArrayList<BlePeripheral>();
			mInflator = LayoutInflater.from(context);
		}

		public void replaceWith(ArrayList<BlePeripheral> devices) {
			mLeDevices.clear();
			mLeDevices.addAll(devices);
			notifyDataSetChanged();
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			view = inflateIfRequired(view, position, parent);
			bind((BlePeripheral) getItem(position), view);
			return view;
		}

		public void refreshUI() {
			notifyDataSetChanged();
		}

		private void bind(final BlePeripheral device, View view) {
			ViewHolder holder = (ViewHolder) view.getTag();

			holder.codeTextView.setText(device.getCode());
			holder.codeTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					_onPeripheralSelected(device);
				}
			});
		}

		private View inflateIfRequired(View view, int position, ViewGroup parent) {
			if (view == null) {
				view = mInflator.inflate(R.layout.signup_scanneddevice_item, null);
				if (config.USE_RESOLUTIONSET) {
					if (SignupActivity.this.isInitialized())
						ResolutionSet._instance.iterateChild(view);
				}
				view.setTag(new ViewHolder(view));
			}
			return view;
		}
	}
}
