package org.unicefkidpower.schools.sync;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.swrve.sdk.SwrveSDK;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.schools.BuildConfig;
import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.ble.BlePeripheral;
import org.unicefkidpower.schools.define.CommonUtils;
import org.unicefkidpower.schools.define.KPConstants;
import org.unicefkidpower.schools.event.EventManager;
import org.unicefkidpower.schools.event.SEvent;
import org.unicefkidpower.schools.helper.DebouncedOnClickListener;
import org.unicefkidpower.schools.helper.FontView;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.helper.OSDate;
import org.unicefkidpower.schools.model.Student;
import org.unicefkidpower.schools.model.Team;
import org.unicefkidpower.schools.powerband.PowerBandDevice;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.CommandService;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.UserService;
import org.unicefkidpower.schools.sync.helper.SyncHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;

import static java.lang.Thread.sleep;

/**
 * Created by Dayong on 8/15/2016.
 */

public class SyncTeamDialog extends Dialog {
	private static final String TAG = "TeamSyncDialog";

	private static final String TEAM_SCANNING = "team_scanning";
	private static final String TEAM_UPLOADING_DRIFT = "team_uploadingDrift";
	private static final String TEAM_UPLOADING_ACTIVITIES = "team_uploadingActivity";
	private static final String TEAM_UPLOADING_DONE = "team_uploadingDone";

	private static final int STAGE_INITIAL = 0;
	private static final int STAGE_SEARCHING = 1;
	private static final int STAGE_SYNCING = 5;
	private static final int STAGE_STOPPED = 9;
	private static final int STAGE_FINISHED = 10;
	private static final int STAGE_FAILED = 11;

	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAND = 1;
	private static final int ERROR_NET = 2;

	private static boolean showDate = false;

	private static final int SLEEP_SCANNING = 2 * 1000; // 2 seconds
	private static final int SLEEP_SYNCING = 2 * 1000; // 2 seconds
	private static final int TIMEOUT_SEARCHING_DONE = 3 * 60 * 1000; // 3 minutes for search

	private LinearLayout _llFirstPage;
	private LinearLayout _llSyncing;
	private TextView _tvTitle;
	private TextView _tvStatus;
	private FontView _fvSyncMark;
	private ListView _lvTeam;
	private View _btnCancelSync;
	private View _btnDone;

	private RotateAnimation _rotateAnimation;
	private SyncingStudentAdapter adapter;

	/////////////////////////////////////////////////
	private Activity _activity;
	private Handler _handler;

	private Runnable _runnableScanning;
	private Runnable _runnablePersonalSyncing;
	private Runnable _runnableTeamSyncingTimeout;

	private SyncTeamDialogListener _listener;
	private boolean isRegistered = false;
	private boolean isSyncStarted = false;

	private int _stage;

	private SyncHelper syncHelper;
	private SyncHelper.SyncResult syncResult;

	private Team _team;
	private List<SyncingStudent> _syncingStudents;
	private int _countBands;
	private SyncingStudent _current_student;
	private Student _student;

	private Date _base_time; // current time(base time for calculating syncNeedDay

	public SyncTeamDialog(Team team, List<Student> allStudents, Activity parentActivity, SyncTeamDialogListener listener) {
		super(parentActivity);

		_team = team;
		_activity = parentActivity;
		_listener = listener;

		_syncingStudents = new ArrayList<>();
		for (Student student : allStudents) {
			_syncingStudents.add(new SyncingStudent(student));
			if (!student.isUnlinkedBand())
				_countBands++;
		}

		showDate = BuildConfig.DEBUG;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FlurryAgent.onStartSession(getContext(), "Team Sync Dialog");

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_band_sync_team);

		setCancelable(false);

		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		_handler = new Handler(_activity.getMainLooper());
		_runnableScanning = new Runnable() {
			@Override
			public void run() {
				Logger.log(TAG, "searching...");
				_handler.removeCallbacks(_runnableScanning);
				scanBand();
			}
		};
		_runnablePersonalSyncing = new Runnable() {
			@Override
			public void run() {
				Logger.log(TAG, "Student syncing...");
				_handler.removeCallbacks(_runnablePersonalSyncing);
				if (_current_student != null &&
						_current_student._peripheral != null)
					startSyncing(_current_student._peripheral);
			}
		};
		_runnableTeamSyncingTimeout = new Runnable() {
			@Override
			public void run() {
				Logger.log(TAG, "!!!Team syncing timeout!!!, will be stop");

				_handler.removeCallbacks(_runnableTeamSyncingTimeout);

				BandFinder.sharedFinder().stop();
				didTeamSynced(true, "TimeOut");
			}
		};


		initControl();

		if (!isRegistered) {
			isRegistered = true;
			EventManager.sharedInstance().register(this);
		}
	}

	@Override
	protected void onStop() {
		if (isRegistered) {
			EventManager.sharedInstance().unregister(this);
			isRegistered = false;
		}

		super.onStop();
		FlurryAgent.onEndSession(getContext());
	}

	private void initControl() {
		_llFirstPage = (LinearLayout) findViewById(R.id.llInitPage);
		_llSyncing = (LinearLayout) findViewById(R.id.llSyncing);
		findViewById(R.id.btnCancel).setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				dismiss();
			}
		});

		findViewById(R.id.btnStart).setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				if (isSyncStarted)
					return;

				isSyncStarted = true;
				startTeamSync();
			}
		});

		_tvTitle = (TextView) findViewById(R.id.tvSyncStatus);
		_tvStatus = (TextView) findViewById(R.id.tvSyncDescription);
		_lvTeam = (ListView) findViewById(R.id.lvTeam);
		_fvSyncMark = (FontView) findViewById(R.id.fvSyncMark);
		_btnCancelSync = findViewById(R.id.btnCancelSync);
		_btnDone = findViewById(R.id.btnSyncDone);

		DebouncedOnClickListener cancelListener = new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				if (v == _btnDone) {
					Logger.log(TAG, "Done clicked");
					stopSync(false);
					dismiss();
				} else {
					stopSync(true);
					Logger.log(TAG, "Cancel clicked");
				}
			}
		};

		_btnCancelSync.setOnClickListener(cancelListener);
		_btnDone.setOnClickListener(cancelListener);

		_rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		_rotateAnimation.setInterpolator(new LinearInterpolator());
		_rotateAnimation.setDuration(2000);
		_rotateAnimation.setRepeatCount(Animation.INFINITE);
		_rotateAnimation.setFillEnabled(true);
		_rotateAnimation.setFillAfter(true);

		setStage(STAGE_INITIAL, null);

		if (!showDate) {
			findViewById(R.id.tvStart).setVisibility(View.GONE);
			findViewById(R.id.tvEnd).setVisibility(View.GONE);
		}
	}

	private void stopSync(boolean needUpdateUI) {
		if (isSyncingDone()) {
			return;
		}

		Logger.log(TAG, "stopping TeamSync...");
		_handler.removeCallbacks(_runnableTeamSyncingTimeout);
		_handler.removeCallbacks(_runnableScanning);
		_handler.removeCallbacks(_runnablePersonalSyncing);

		BandFinder.sharedFinder().stop();

		try {
			if (needUpdateUI) {
				synchronized (_syncingStudents) {
					if (_current_student != null) {
						_current_student.setSyncState(SyncingStudent.SYNC_STATE_FAILED, "stopped");
						_current_student = null;
						if (syncHelper != null) {
							syncHelper.stop();
						}
					}
					adapter.notifyDataSetChanged();
				}

				_listener.onSuccess(getSyncedStudentCount(), getFailedStudentCount());
			}
		} catch (Exception e) {
			Logger.error(TAG, "exception" + e);
		} finally {
			setStage(STAGE_STOPPED, null);
		}
	}

	private void startTeamSync() {
		Logger.log(TAG, "start TeamSync, searching bands now");

		if (showDate) {
			OSDate now = new OSDate();
			((TextView) findViewById(R.id.tvStart)).setText(
					now.toStringWithFormat("HH:mm:ss")
			);
		}

		setCanceledOnTouchOutside(false);
		if (_listener == null) {
			Logger.error(TAG, "listener wasn't set, can't continue to work");
			throw new UnknownError();
		}

		_llSyncing.setKeepScreenOn(true);
		setStage(STAGE_SYNCING, null);
		adapter = new SyncingStudentAdapter(_activity, R.layout.sync_item);
		adapter.setStudents(_syncingStudents);
		_lvTeam.setAdapter(adapter);
		_lvTeam.setDivider(null);

		triggerScanning();
	}

	private void scanBand() {
		if (isSyncingDone())
			return;

		if (!hasStudentNeedSync()) {
			Logger.log(TAG, "There are no student sync need");
			didTeamSynced(true, "NoBands");
			return;
		}

		BandFinder.sharedFinder().search(_activity,
				null,
				0,
				new BandFinder.OnDiscoveredBandListener() {
					@Override
					public void onDiscovered(BlePeripheral newPeripheral, List<BlePeripheral> scannedItems) {
						didDiscovered(newPeripheral);
					}

					@Override
					public void onError(int error, String message) {
						didTeamSynced(false, message);
					}

					@Override
					public void onEnd(List<BlePeripheral> scannedItems, boolean byUserRequest) {
						// search ended
						//Logger.log(TAG, "Searching band done %s", byUserRequest?"programmatically":"timeout");
					}
				});

		_handler.postDelayed(_runnableTeamSyncingTimeout, TIMEOUT_SEARCHING_DONE);
	}

	private void didDiscovered(final BlePeripheral peripheral) {
		if (_current_student != null) {
			return;
		}

		if (isSyncingDone()) {
			Logger.log(TAG, "Syncing have done.");
			return;
		}

		synchronized (_syncingStudents) {
			SyncingStudent student = findStudentWithPeripheral(peripheral);

			if (student == null) {
				//Logger.log(TAG, "there are no sync needed student right now");
				return;
			}

			_handler.removeCallbacks(_runnableTeamSyncingTimeout);
			BandFinder.sharedFinder().stop();

			triggerSync(student, peripheral);
		}
	}

	private SyncingStudent findStudentWithPeripheral(final BlePeripheral peripheral) {
		for (SyncingStudent student : _syncingStudents) {
			if (!student.shouldBeSynced())
				continue;

			if (student.isThisPeripheral(peripheral))
				return student;
		}
		return null;
	}

	private boolean hasStudentNeedSync() {
		for (SyncingStudent student : _syncingStudents) {
			if (student.shouldBeSynced())
				return true;
		}
		return false;
	}

	private int getSyncedStudentCount() {
		int count = 0;
		for (SyncingStudent student : _syncingStudents) {
			if (student.isSyncedWithSuccess())
				count++;
		}
		return count;
	}

	private int getFailedStudentCount() {
		int count = 0;
		for (SyncingStudent student : _syncingStudents) {
			if (student.isSyncedWithFailed())
				count++;
		}
		return count;
	}


	private void triggerSync(final SyncingStudent student, final BlePeripheral peripheral) {
		_current_student = student;
		_current_student._peripheral = peripheral;
		_current_student.setSyncState(SyncingStudent.SYNC_STATE_SYNCING, null);
		_student = _current_student._student;
		updateStatusString();
		adapter.notifyDataSetChanged();

		_handler.postDelayed(_runnablePersonalSyncing, SLEEP_SYNCING);
	}


	private void startSyncing(final BlePeripheral peripheral) {
		_base_time = new Date();

		int dayNeedSync;
		if (_student._lastSyncDateDetail == null) {
			dayNeedSync = Student.POWERBAND_RECORD_DAYS_COUNT - 1;
		} else {
			dayNeedSync = OSDate.daysBetweenDates(_student._lastSyncDateDetail, _base_time);

			if (dayNeedSync >= Student.POWERBAND_RECORD_DAYS_COUNT) {
				Logger.error(TAG, "Activity Data lost for %d days", dayNeedSync - Student.POWERBAND_RECORD_DAYS_COUNT);
				dayNeedSync = Student.POWERBAND_RECORD_DAYS_COUNT - 1;
			}
		}

		Logger.log(TAG, "Start Personal Syncing(\"%s\" : %d days ago, LSD=%s, CUR=%s)", _student._name, dayNeedSync,
				_student._lastSyncDateDetail == null ? "null" : _student._lastSyncDateDetail, _base_time);

		syncHelper = new SyncHelper(_activity, new OnBandActionListener() {
			@Override
			public void success(Object object) {
				SyncHelper.SyncResult result = (SyncHelper.SyncResult) object;
				Map<String, String> payload = new HashMap<>();
				payload.put("student_id", "" + _student._id);
				payload.put("team_name", "" + _team._name);
				payload.put("points", "" + result.points);
				payload.put("rutf", "" + result.rutf);
				payload.put("steps", "" + result.steps);
				SwrveSDK.event(KPConstants.SWRVE_BAND_SYNC_SUCCESS, payload);

				didGetActivityData(peripheral.isCalorieCloudBand(), (SyncHelper.SyncResult) object);
			}

			@Override
			public void failed(int code, String message) {
				Map<String, String> payload = new HashMap<>();
				payload.put("error_code", "" + code);
				SwrveSDK.event(KPConstants.SWRVE_BAND_SYNC_ERROR, payload);

				didPersonalSynced(false, ERROR_BAND, message);
			}

			@Override
			public void connected() {
			}

			@Override
			public void reportForDaily(boolean didFinish, int agoDay, int wholeDay) {
			}

			@Override
			public void updateStatus(String status) {
			}
		});

		syncHelper.setBand(new PowerBandDevice(_current_student._peripheral));
		syncHelper.setParameter(_base_time, dayNeedSync, _student._name, _student._powerPoints);
		syncHelper.run();
	}

	private void didGetActivityData(boolean isCalorieBand, final SyncHelper.SyncResult result) {
		try {
			sleep(100);
		} catch (InterruptedException ex) {
		}

		syncResult = result;

		Logger.log(TAG, "Uploading activity data name:%s, band:%s, version:%s",
				_student._name, _student.getDeviceId(), syncResult.version);

		// if band is calorie cloud band : go to report drift at first, else go to uploading details
		if (isCalorieBand)
			EventManager.sharedInstance().post(TEAM_UPLOADING_DRIFT, this);
		else
			EventManager.sharedInstance().post(TEAM_UPLOADING_ACTIVITIES, this);
	}

	private void triggerScanning() {
		EventManager.sharedInstance().post(TEAM_SCANNING, this);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent e) {
		if (e.object != this)
			return;

		if (EventManager.isEvent(e, TEAM_SCANNING)) {
			_handler.postDelayed(_runnableScanning, SLEEP_SCANNING);
		} else if (EventManager.isEvent(e, TEAM_UPLOADING_DRIFT)) {
			ServerManager.sharedInstance().sendDriftLogs(_student.getDeviceId(), String.format("%d", syncResult.drift),
					new RestCallback<UserService.ResUserDevices>() {
						@Override
						public void failure(RetrofitError retrofitError, String message) {
							Logger.error(TAG, "SendDrift Failed");
							didPersonalSynced(false, ERROR_NET, message);
						}

						@Override
						public void success(UserService.ResUserDevices resUserDevices, Response response) {
							Logger.log(TAG, "Sending Drift Success");
							EventManager.sharedInstance().post(TEAM_UPLOADING_ACTIVITIES, SyncTeamDialog.this);
						}
					});
		} else if (EventManager.isEvent(e, TEAM_UPLOADING_ACTIVITIES)) {
			// start uploading
			ServerManager.sharedInstance().uploadDailyData(
					_student.getDeviceId(),
					syncResult.datas,
					new RestCallback<CommandService.ResUploadDailyData>() {
						@Override
						public void failure(RetrofitError retrofitError, String message) {
							Logger.error(TAG, "uploading DetailData failed : %s", message);
							didPersonalSynced(false, ERROR_NET, message);
						}

						@Override
						public void success(CommandService.ResUploadDailyData resUploadDailyData, Response response) {
							EventManager.sharedInstance().post(TEAM_UPLOADING_DONE, SyncTeamDialog.this);
						}
					});
		} else if (EventManager.isEvent(e, TEAM_UPLOADING_DONE)) {
			didPersonalSynced(true, ERROR_NONE, null);
		}
	}

	private void didPersonalSynced(boolean isSuccess, int kindError, String errorMessage) {
		if (isSuccess) {
			Logger.log(TAG, "student(%s) synced success", _student._name);
			if (_current_student != null) {
				_current_student.setSyncState(SyncingStudent.SYNC_STATE_SUCCESS, errorMessage);
			}
		} else {
			Logger.error(TAG, "student(%s) synced failed (error=%s)", _student._name, errorMessage);
			if (_current_student != null) {
				_current_student.setSyncState(SyncingStudent.SYNC_STATE_FAILED, errorMessage);
			}
		}

		updateStatusString();
		adapter.notifyDataSetChanged();

		if (!isSuccess && kindError == ERROR_NET) {
			didTeamSynced(false, errorMessage);
		} else {
			synchronized (_syncingStudents) {
				_current_student = null;
				if (!isSyncingDone())
					triggerScanning();
			}
		}
	}

	private void didTeamSynced(boolean success, String message) {
		Logger.log(TAG, "team synced with %s(%s)",
				success ? "success" : "failed", success ? "" : message);

		if (showDate) {
			OSDate now = new OSDate();
			((TextView) findViewById(R.id.tvEnd)).setText(
					now.toStringWithFormat("HH:mm:ss")
			);
		}
		if (isRegistered) {
			EventManager.sharedInstance().unregister(this);
			isRegistered = false;
		}

		_llSyncing.setKeepScreenOn(false);

		if (success) {
			setStage(STAGE_FINISHED, null);
			_listener.onSuccess(getSyncedStudentCount(), getFailedStudentCount());
		} else {
			setStage(STAGE_FAILED, message);
			_listener.onFailed(message);
		}
	}

	private boolean isSyncingDone() {
		return _stage == STAGE_FINISHED ||
				_stage == STAGE_FAILED ||
				_stage == STAGE_STOPPED;
	}

	private void setStage(int stage, String message) {
		_stage = stage;

		switch (_stage) {
			case STAGE_INITIAL:
				Logger.log(TAG, "STAGE INITIAL");
				_llFirstPage.setVisibility(View.VISIBLE);
				_llSyncing.setVisibility(View.GONE);
				break;
			case STAGE_SEARCHING:
				Logger.log(TAG, "STAGE_SEARCHING");
				break;

			case STAGE_SYNCING:
				Logger.log(TAG, "STAGE SYNCING");
				_llFirstPage.setVisibility(View.GONE);
				_llSyncing.setVisibility(View.VISIBLE);
				_btnCancelSync.setVisibility(View.VISIBLE);
				_btnDone.setVisibility(View.GONE);

				_fvSyncMark.setText(_activity.getString(R.string.icon_string_sync));
				_fvSyncMark.setAnimation(_rotateAnimation);

				_tvTitle.setText(_activity.getString(R.string.sync_all_bands));

				updateStatusString();
				break;

			// completed
			case STAGE_STOPPED:
				Logger.log(TAG, "STAGE STOPPED");
				_llFirstPage.setVisibility(View.GONE);
				_llSyncing.setVisibility(View.VISIBLE);

				_btnCancelSync.setVisibility(View.GONE);
				_btnDone.setVisibility(View.VISIBLE);

				_fvSyncMark.clearAnimation();
				_fvSyncMark.setText(_activity.getString(R.string.icon_string_check));
				_tvTitle.setText(_activity.getString(R.string.sync_stopped));
				updateStatusString();

				break;
			// completed
			case STAGE_FINISHED:
				Logger.log(TAG, "STAGE FINISHED");
				_llFirstPage.setVisibility(View.GONE);
				_llSyncing.setVisibility(View.VISIBLE);

				_btnCancelSync.setVisibility(View.GONE);
				_btnDone.setVisibility(View.VISIBLE);

				_fvSyncMark.clearAnimation();

				int nSynced = getSyncedStudentCount();
				if (nSynced == _countBands) {
					_tvTitle.setText(_activity.getString(R.string.sync_all_synced));
					_fvSyncMark.setTextColor(CommonUtils.getColorFromRes(_activity.getResources(), R.color.kidpower_light_blue));
					_fvSyncMark.setText(_activity.getString(R.string.icon_string_check));
				} else if (nSynced == 0) {
					_tvTitle.setText(_activity.getString(R.string.sync_not_synced));
					_fvSyncMark.setTextColor(CommonUtils.getColorFromRes(_activity.getResources(), R.color.kidpower_light_red));
					_fvSyncMark.setText(_activity.getString(R.string.icon_string_exclamation));
				} else {
					_tvTitle.setText(_activity.getString(R.string.sync_synced));
					_fvSyncMark.setTextColor(CommonUtils.getColorFromRes(_activity.getResources(), R.color.kidpower_orange));
					_fvSyncMark.setText(_activity.getString(R.string.icon_string_information));
				}
				updateStatusString();

				break;

			// failed
			case STAGE_FAILED:
				Logger.log(TAG, "STAGE FAILED");
				_llFirstPage.setVisibility(View.GONE);
				_llSyncing.setVisibility(View.VISIBLE);

				_btnCancelSync.setVisibility(View.GONE);
				_btnDone.setVisibility(View.VISIBLE);

				_fvSyncMark.clearAnimation();
				_fvSyncMark.setTextColor(CommonUtils.getColorFromRes(_activity.getResources(), R.color.kidpower_light_red));
				_fvSyncMark.setText(_activity.getString(R.string.icon_string_exclamation));
				_tvTitle.setText(_activity.getString(R.string.sync_failed));
				_tvStatus.setText(message);
				break;
		}
	}

	private void updateStatusString() {
		String status = String.format(_activity.getString(R.string.sync_state_format),
				getSyncedStudentCount(), _countBands, _countBands, _syncingStudents.size());
		_tvStatus.setText(status);
	}

	public interface SyncTeamDialogListener {
		void onSuccess(int success, int failed);

		void onFailed(String message);
	}

	static class ViewHolder {
		final FontView syncedMark;
		final TextView tvName;
		final TextView tvDeviceId;
		final TextView tvStatus;

		ViewHolder(View view) {
			syncedMark = (FontView) view.findViewById(R.id.syncedMark);
			tvName = (TextView) view.findViewById(R.id.tvName);
			tvDeviceId = (TextView) view.findViewById(R.id.tvDeviceId);
			tvStatus = (TextView) view.findViewById(R.id.tvStatus);
		}
	}

	private class SyncingStudentAdapter extends ArrayAdapter {
		List<SyncingStudent> students;

		private SyncingStudentAdapter(Context context, int resource) {
			super(context, resource);
		}

		public void setStudents(List<SyncingStudent> students) {
			this.students = students;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (students == null)
				return 0;
			return students.size();
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			view = getLayoutInflater().inflate(R.layout.sync_item, null);
			ViewHolder holder = new ViewHolder(view);
			SyncingStudent student = _syncingStudents.get(position);

			holder.tvName.setText(student._student._name);

			String deviceCode = "";
			try {
				deviceCode = student._student.getDeviceCode();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			holder.syncedMark.setVisibility(View.INVISIBLE);

			String str;
			switch (student._state) {
				case SyncingStudent.SYNC_STATE_SYNCING:
					holder.tvDeviceId.setTextColor(CommonUtils.getColorFromRes(_activity.getResources(), R.color.black));
					holder.tvName.setTextColor(CommonUtils.getColorFromRes(_activity.getResources(), R.color.black));
					holder.tvStatus.setTextColor(CommonUtils.getColorFromRes(_activity.getResources(), R.color.black));
					str = _activity.getString(R.string.teamsync_ret_syncing);
					break;
				case SyncingStudent.SYNC_STATE_SUCCESS:
					holder.tvDeviceId.setTextColor(CommonUtils.getColorFromRes(_activity.getResources(), R.color.black));
					holder.tvName.setTextColor(CommonUtils.getColorFromRes(_activity.getResources(), R.color.black));
					holder.tvStatus.setTextColor(CommonUtils.getColorFromRes(_activity.getResources(), R.color.black));

					holder.tvDeviceId.setTypeface(holder.tvDeviceId.getTypeface(), Typeface.BOLD);
					holder.tvName.setTypeface(holder.tvName.getTypeface(), Typeface.BOLD);
					holder.tvStatus.setTypeface(holder.tvStatus.getTypeface(), Typeface.BOLD);
					holder.syncedMark.setVisibility(View.VISIBLE);
					str = _activity.getString(R.string.teamsync_ret_synced);
					break;
				case SyncingStudent.SYNC_STATE_FAILED:
					str = _activity.getString(R.string.teamsync_ret_notsynced);
					break;
				case SyncingStudent.SYNC_STATE_NONE:
					str = _activity.getString(R.string.teamsync_ret_notsynced);
					break;
				default:
					str = _activity.getString(R.string.teamsync_ret_notsynced);
					break;
			}

			if (TextUtils.isEmpty(deviceCode)) {
				holder.tvDeviceId.setText("-");
				holder.tvStatus.setText(_activity.getString(R.string.sync_no_band));
			} else {
				holder.tvDeviceId.setText(deviceCode);
				holder.tvStatus.setText(str);
			}

			return view;
		}
	}

	private class SyncingStudent {
		static final int RETRY_DEFINE = 5;

		private static final int SYNC_STATE_NONE = -1;
		private static final int SYNC_STATE_SYNCING = 0;
		private static final int SYNC_STATE_SUCCESS = 1;
		private static final int SYNC_STATE_FAILED = 2;

		private SyncingStudent(Student student) {
			_student = student;
			_state = SYNC_STATE_NONE;
			_nRetry = RETRY_DEFINE;
			_errorMsg = null;
		}

		private boolean shouldBeSynced() {
			if (_student.isUnlinkedBand())
				return false;

			if (_state == SYNC_STATE_SUCCESS)
				return false;

			if (_state == SYNC_STATE_FAILED && _nRetry <= 0)
				return false;
			else
				return true;
		}

		private boolean isSyncedWithSuccess() {
			return _state == SYNC_STATE_SUCCESS;
		}

		private boolean isSyncedWithFailed() {
			return _state == SYNC_STATE_FAILED;
		}

		private void setSyncState(int nState, String errorMsg) {
			_state = nState;
			if (nState == SYNC_STATE_SYNCING) {
			} else if (nState == SYNC_STATE_SUCCESS) {
				_student._lastSyncDateDetail = _base_time;
			} else {
				_state = SYNC_STATE_FAILED;
				_errorMsg = errorMsg;
				_nRetry--;
			}
		}

		private boolean isThisPeripheral(BlePeripheral peripheral) {
			return _student.getDeviceId().equals(peripheral.getMACAddress());
		}

		Student _student;
		int _state;
		int _nRetry;
		String _errorMsg;
		BlePeripheral _peripheral;
	}
}
