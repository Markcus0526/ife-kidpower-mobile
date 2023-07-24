package org.unicefkidpower.schools.sync;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.swrve.sdk.SwrveSDK;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.ble.BlePeripheral;
import org.unicefkidpower.schools.context.UserContext;
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
import org.unicefkidpower.schools.sync.helper.SyncHelper.SyncResult;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;

import static java.lang.Thread.sleep;
import static org.unicefkidpower.schools.sync.BandFinder.TIMEOUT_FOR_SEARCHING;

/**
 * Created by Dayong on 10/08/2016.
 */

public class SyncPersonalDialog extends Dialog {
	static final String TAG = "PersonalSyncDialog";

	static final int STAGE_SEARCHING = 0;
	static final int STAGE_CONNECTING = 1;
	static final int STAGE_SYNCING = 2;
	static final int STAGE_UPLOADING = 3;
	static final int STAGE_COMPLETE = 4;

	private Activity _activity;
	private int _nRetry = 5;

	private TextView btnCancelSync;
	private TextView btnSeeProgress;
	private TextView tvTitle;
	private TextView tvStatue;
	private FontView fvSyncMark;

	private RotateAnimation _rotateAnimation;

	/////////////////////////////////////
	private boolean isRegistered = false;

	private SyncHelper syncHelper;
	private SyncResult syncResult;

	private Student _student;
	private Team _team;

	private Date _current;				// current time(base time for calculating syncNeedDay

	private SyncDialogListener _listener;
	private int _stage = STAGE_SEARCHING;


	public SyncPersonalDialog(Activity activity, Team team, Student student, SyncDialogListener listener) {
		super(activity);

		_activity = activity;
		_team = team;
		_student = student;

		_listener = listener;

		setCancelable(false);
		setCanceledOnTouchOutside(false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FlurryAgent.onStartSession(getContext(), "Personal Sync Dialog");
		Logger.log(TAG, "Personal Sync Dialog for " + _student._name);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_band_sync_personal);

		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		initControl();

		startSearch();
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(getContext());
	}

	private void initControl() {
		tvTitle = (TextView) findViewById(R.id.textTitle);
		tvStatue = (TextView) findViewById(R.id.textDescription);
		fvSyncMark = (FontView) findViewById(R.id.fvRefresh);

		btnCancelSync = (TextView) findViewById(R.id.btnCancelSync);
		btnCancelSync.setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				stopSync();
			}
		});

		btnSeeProgress = (TextView) findViewById(R.id.btnSyncDone);
		btnSeeProgress.setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				dismiss();
				_listener.onSeeMyStats();
			}
		});

		_rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		_rotateAnimation.setInterpolator(new LinearInterpolator());
		_rotateAnimation.setDuration(2000);
		_rotateAnimation.setRepeatCount(Animation.INFINITE);
		_rotateAnimation.setFillEnabled(true);
		_rotateAnimation.setFillAfter(true);
	}

	void stopSync() {
		if (_stage == STAGE_SEARCHING) {
			BandFinder.sharedFinder().stop();
		} else if (_stage == STAGE_CONNECTING ||
				_stage == STAGE_SYNCING) {
			if (syncHelper != null) {
				syncHelper.stop();
			}
		} else if (_stage == STAGE_UPLOADING) {

		} else {
			dismiss();
			return;
		}

		dismiss();

		tvTitle.setKeepScreenOn(false);
		_listener.onCompleted(false, null);
	}

	void startSearch() {
		Logger.log(TAG, "find the device : %s", _student.getDeviceId());

		tvTitle.setKeepScreenOn(true);
		BandFinder.sharedFinder().search(
				_activity,
				null,
				TIMEOUT_FOR_SEARCHING,
				new BandFinder.OnDiscoveredBandListener() {
					@Override
					public void onDiscovered(BlePeripheral newPeripheral, List<BlePeripheral> scannedItems) {
						if (newPeripheral.getMACAddress().equals(_student.getDeviceId())) {
							BandFinder.sharedFinder().stop();
							didDiscovered(newPeripheral);
						}
					}

					@Override
					public void onError(int error, String message) {
						didFailed(true, message);
					}

					@Override
					public void onEnd(List<BlePeripheral> scannedItems, boolean byUserRequest) {
						if (!byUserRequest) {
							didFailed(false, "Couldn't find the band");
						}
					}
				});

		setStage(STAGE_SEARCHING);
	}

	void didDiscovered(final BlePeripheral peripheral) {
		setStage(STAGE_CONNECTING);

		_current = new Date();

		int dayNeedSync;
		if (_student._lastSyncDateDetail == null) {
			dayNeedSync = Student.POWERBAND_RECORD_DAYS_COUNT - 1;
		} else {
			dayNeedSync = OSDate.daysBetweenDates(_student._lastSyncDateDetail, _current);

			if (dayNeedSync >= Student.POWERBAND_RECORD_DAYS_COUNT) {
				Logger.error(TAG, "Activity Data lost for %d days", dayNeedSync - Student.POWERBAND_RECORD_DAYS_COUNT);
				dayNeedSync = Student.POWERBAND_RECORD_DAYS_COUNT - 1;
			}
		}

		Logger.log(TAG, "Start Syncing \"%s\", %d days ago, LSD=%s, CUR=%s", _student._name, dayNeedSync,
				_student._lastSyncDateDetail == null ? "null" : _student._lastSyncDateDetail, _current);

		syncHelper = new SyncHelper(_activity, new OnBandActionListener() {
			@Override
			public void success(Object object) {
				SyncResult result = (SyncResult) object;
				Map<String, String> payload = new HashMap<>();
				payload.put("student_id", "" + _student._id);
				payload.put("team_name", "" + _team._name);
				payload.put("points", "" + result.points);
				payload.put("rutf", "" + result.rutf);
				payload.put("steps", "" + result.steps);
				SwrveSDK.event(KPConstants.SWRVE_BAND_SYNC_SUCCESS, payload);

				didSyncedWithBand(peripheral, result);
			}

			@Override
			public void failed(int code, String message) {

				Map<String, String> payload = new HashMap<>();
				payload.put("error_code", "" + code);
				SwrveSDK.event(KPConstants.SWRVE_BAND_SYNC_ERROR, payload);

				didFailed(true, message);
			}

			@Override
			public void connected() {
			}

			@Override
			public void reportForDaily(boolean didFinish, int agoDay, int wholeDay) {
				if (!didFinish) {
					syncingDay(agoDay + 1);
				}
			}

			@Override
			public void updateStatus(String status) {
			}
		});

		syncHelper.setBand(new PowerBandDevice(peripheral));
		syncHelper.setParameter(_current, dayNeedSync, _student._name, _student._powerPoints);
		syncHelper.run();
	}

	static final String PERSON_UPLOADING_DRIFT = "person_uploadingDrift";
	static final String PERSON_UPLOADING_ACTIVITIES = "person_uploadingActivity";
	static final String PERSON_UPLOADING_DONE = "person_uploadingDone";

	void didSyncedWithBand(final BlePeripheral peripheral, final SyncResult result) {
		setStage(STAGE_UPLOADING);

		if (!isRegistered) {
			isRegistered = true;
			EventManager.sharedInstance().register(this);
		}

		try {
			sleep(500);
		} catch (InterruptedException ex) {
		}

		syncResult = result;

		Logger.log(TAG, "Uploading activity data name:%s, band:%s, version:%s",
				_student._name, _student.getDeviceId(), syncResult.version);

		// if band is calorie cloud band : go to report drift at first, else go to uploading details
		if (peripheral.isCalorieCloudBand())
			EventManager.sharedInstance().post(PERSON_UPLOADING_DRIFT, this);
		else
			EventManager.sharedInstance().post(PERSON_UPLOADING_ACTIVITIES, this);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent e) {
		if (e.object != this)
			return;

		if (EventManager.isEvent(e, PERSON_UPLOADING_DRIFT)) {
			ServerManager.sharedInstance().sendDriftLogs(_student.getDeviceId(), String.format("%d", syncResult.drift),
					new RestCallback<UserService.ResUserDevices>() {
						@Override
						public void failure(RetrofitError retrofitError, String message) {
							Logger.error(TAG, "SendDrift Failed");
							didFailed(false, message);
						}

						@Override
						public void success(UserService.ResUserDevices resUserDevices, Response response) {
							Logger.log(TAG, "Sending Drift Success");
							EventManager.sharedInstance().post(PERSON_UPLOADING_ACTIVITIES, SyncPersonalDialog.this);
						}
					});
		} else if (EventManager.isEvent(e, PERSON_UPLOADING_ACTIVITIES)) {
			// start uploading
			ServerManager.sharedInstance().uploadDailyData(
					_student.getDeviceId(),
					syncResult.datas,
					new RestCallback<CommandService.ResUploadDailyData>() {
						@Override
						public void failure(RetrofitError retrofitError, String message) {
							Logger.error(TAG, "uploading DetailData failed : %s", message);
							didFailed(false, message);
						}

						@Override
						public void success(CommandService.ResUploadDailyData resUploadDailyData, Response response) {
							EventManager.sharedInstance().post(PERSON_UPLOADING_DONE, SyncPersonalDialog.this);
						}
					});
		} else if (EventManager.isEvent(e, PERSON_UPLOADING_DONE)) {
			didSynced(true, null);
		}
	}

	void didSynced(boolean isSuccess, String message) {
		if (isRegistered) {
			EventManager.sharedInstance().unregister(this);
			isRegistered = false;
		}

		setStage(STAGE_COMPLETE);
		tvTitle.setKeepScreenOn(false);

		if (isSuccess) {
			_student._lastSyncDateDetail = _current;

			_listener.onCompleted(true, null);
		} else {
			tvTitle.setText(_activity.getString(R.string.sync_stopped));
			tvStatue.setText(message);

			btnCancelSync.setText(_activity.getString(R.string.button_icon_ok));
			btnCancelSync.setVisibility(View.VISIBLE);
			btnSeeProgress.setVisibility(View.GONE);

			fvSyncMark.setTextColor(CommonUtils.getColorFromRes(_activity.getResources(), R.color.kidpower_light_red));
			fvSyncMark.setText(_activity.getString(R.string.icon_string_exclamation));
			fvSyncMark.clearAnimation();

			_listener.onCompleted(false, message);
		}
	}

	void didFailed(boolean shouldRetry, String message) {
		Logger.error(TAG, "sync failed(%s), retry %d times", message, _nRetry);

		_nRetry--;
		if (shouldRetry && _nRetry > 0) {
			try {
				sleep(500);
			} catch (InterruptedException ex) {
			}

			startSearch();
			return;
		}

		didSynced(false, message);
	}

	void setStage(int stage) {
		_stage = stage;
		switch (_stage) {
			case STAGE_SEARCHING:
				Logger.log(TAG, "Searching band " + _student.getDeviceId());
				tvTitle.setText(_activity.getString(R.string.studentsync_title_progress));
				tvStatue.setText(_activity.getString(R.string.sync_searching_band));

				btnCancelSync.setVisibility(View.VISIBLE);
				btnSeeProgress.setVisibility(View.GONE);

				fvSyncMark.setText(_activity.getString(R.string.icon_string_sync));
				fvSyncMark.startAnimation(_rotateAnimation);
				break;
			case STAGE_CONNECTING:
				Logger.log(TAG, "Connecting to band " + _student.getDeviceId());
				tvStatue.setText(_activity.getString(R.string.sync_connecting));
				break;
			case STAGE_UPLOADING:
				tvStatue.setText(_activity.getString(R.string.sync_uploading));
				break;
			case STAGE_COMPLETE:
				Logger.log(TAG, "Completed syncing band" + _student.getDeviceId());

				tvTitle.setText(_activity.getString(R.string.sync_complete));
				tvStatue.setText(_activity.getString(R.string.sync_complete));

				btnCancelSync.setVisibility(View.GONE);
				btnSeeProgress.setVisibility(View.VISIBLE);

				fvSyncMark.setText(_activity.getString(R.string.icon_string_check));
				fvSyncMark.clearAnimation();
				break;
		}
	}

	protected void syncingDay(int agoDay) {
		OSDate dt = new OSDate(_current).offsetDay(-1 * agoDay);
		String date = dt.MonthString() + " " + dt.DayString();
		String stage = "";

		if (!UserContext.sharedInstance().getAppLocale().getLanguage().equalsIgnoreCase("nl")) {
			stage = String.format("Syncing data from %s", date);
		} else {
			stage = String.format("Het synchroniseren van data vanaf %s", date);
		}

		tvStatue.setText(stage);

		Logger.log(TAG, "syncing from %s, %d day ago", date, agoDay);
	}

	public interface SyncDialogListener {
		void onCompleted(boolean success, String errMsg);

		void onSeeMyStats();
	}
}
