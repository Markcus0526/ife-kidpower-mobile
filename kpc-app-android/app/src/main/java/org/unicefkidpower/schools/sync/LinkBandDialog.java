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
import android.widget.Toast;

import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.ResolutionSet;
import org.unicefkidpower.schools.UIManager;
import org.unicefkidpower.schools.ble.BleError;
import org.unicefkidpower.schools.ble.BlePeripheral;
import org.unicefkidpower.schools.define.config;
import org.unicefkidpower.schools.event.EventManager;
import org.unicefkidpower.schools.helper.DebouncedOnClickListener;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.model.Team;
import org.unicefkidpower.schools.powerband.PowerBandDevice;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.StudentService;
import org.unicefkidpower.schools.sync.helper.LinkHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

import static org.unicefkidpower.schools.sync.BandFinder.TIMEOUT_FOR_SEARCHING;

/**
 * Created by ruifeng on 2015/1/25.
 */


public class LinkBandDialog extends Dialog implements OnBandActionListener {
	static final String TAG = "LinkBandDialog";

	static final int STAGE_CHECKING_DUPLICATE = 0;
	static final int STAGE_SEARCHING = 1;
	static final int STAGE_LINKING = 2;
	static final int STAGE_STOPPED = 3;

	protected Activity _parentActivity;
	LinkBandDialogListener _listener;
	LinkHelper linkHelper;

	TextView tvTitle;
	boolean _isLinkingBand = true;
	String _deviceId;
	Team _team;
	int _id;
	String _name;
	int _totalPowerPoints = 0;

	int _stage;

	int _nRetry = 5;

	public LinkBandDialog(Activity parentActivity, LinkBandDialogListener listener) {
		super(parentActivity);

		_parentActivity = parentActivity;
		_listener = listener;

		setCancelable(false);
		setCanceledOnTouchOutside(false);
	}

	public void setParameters(Team team, int id, String name, String deviceId, int totalPoints) {
		_deviceId = deviceId;
		_team = team;
		_id = id;
		_name = name;
		_totalPowerPoints = totalPoints;
	}

	public void setUpdatingName() {
		_isLinkingBand = false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_band_link);

		if (config.USE_RESOLUTIONSET)
			ResolutionSet._instance.iterateChild(findViewById(R.id.layout_parent));

		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		initControl();

		if (_isLinkingBand)
			checkDuplicate();
		else
			startSearch();
	}

	void initControl() {
		tvTitle = ((TextView) findViewById(R.id.textTitle));
		tvTitle.setKeepScreenOn(true);
		if (_isLinkingBand)
			tvTitle.setText(_parentActivity.getString(R.string.band_linking));
		else
			tvTitle.setText(_parentActivity.getString(R.string.band_updating));

		findViewById(R.id.btnCancelLink).setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				stop();
			}
		});

		setStatusMessage(_parentActivity.getString(R.string.registerband_checking_duplication));

		RotateAnimation _rotateAnimation = new RotateAnimation(0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		_rotateAnimation.setInterpolator(new LinearInterpolator());
		_rotateAnimation.setDuration(2000);
		_rotateAnimation.setRepeatCount(Animation.INFINITE);
		_rotateAnimation.setFillEnabled(true);
		_rotateAnimation.setFillAfter(true);

		findViewById(R.id.fvRefresh).setAnimation(_rotateAnimation);
	}

	void checkDuplicate() {
		Logger.log(TAG, "checking duplicate");
		_stage = STAGE_CHECKING_DUPLICATE;

		setStatusMessage(_parentActivity.getString(R.string.registerband_checking_duplication));

		ArrayList<String> deviceIds = new ArrayList<String>();
		deviceIds.add(_deviceId);
		ServerManager.sharedInstance().byDeviceIds(deviceIds, new RestCallback<List<StudentService.ResByDeviceIds>>() {
			@Override
			public void success(List<StudentService.ResByDeviceIds> resByDeviceIds, Response response) {
				int isDuplicated;
				// duplication checking
				if (resByDeviceIds == null ||
						resByDeviceIds.size() == 0) {
					isDuplicated = 0;
				} else {
					if (resByDeviceIds.get(0).deviceId != null &&
							resByDeviceIds.get(0).deviceId.equalsIgnoreCase(_deviceId)) {
						isDuplicated = 1;
					} else {
						isDuplicated = 0;
					}
				}

				didCheckDuplicate(isDuplicated);
			}

			@Override
			public void failure(RetrofitError retrofitError, String message) {
				int isDuplicated;
				// duplication checked
				if (retrofitError.getKind() == RetrofitError.Kind.NETWORK) {
					isDuplicated = 2;// network
				} else {
					isDuplicated = 1;
				}
				didCheckDuplicate(isDuplicated);
			}
		});
	}

	void didCheckDuplicate(int isDuplicated) {
		Logger.log(TAG, "Checking done : %d", isDuplicated);
		if (_stage == STAGE_STOPPED)
			return;

		if (isDuplicated == 1) {
			error(BleError.BE_GENERAL, _parentActivity.getString(R.string.registerband_band_registered));
		} else if (isDuplicated == 2) {
			error(BleError.BE_GENERAL, _parentActivity.getString(R.string.registerband_duplication_check_failed));
		} else {
			startSearch();
		}
	}

	void startSearch() {
		Logger.log(TAG, "find the device : %s", _deviceId);

		_stage = STAGE_SEARCHING;

		BandFinder.sharedFinder().search(
				_parentActivity,
				null,
				TIMEOUT_FOR_SEARCHING,
				new BandFinder.OnDiscoveredBandListener() {
					@Override
					public void onDiscovered(BlePeripheral newPeripheral, List<BlePeripheral> scannedItems) {
						if (_stage == STAGE_STOPPED)
							return;

						if (newPeripheral.getMACAddress().equals(_deviceId)) {
							Logger.log(TAG, "found the device : %s(%s)",
									newPeripheral.getMACAddress(), newPeripheral.getCode());
							BandFinder.sharedFinder().stop();

							if (_id > 0)
								doLinkBandWithServer(newPeripheral);
							else
								doLinkBand(newPeripheral);
						}
					}

					@Override
					public void onError(int error, String message) {
						error(error, "Can't start searching band");
					}

					@Override
					public void onEnd(List<BlePeripheral> scannedItems, boolean byUserRequest) {
						if (!byUserRequest) {
							error(BleError.BE_NOT_FOUND, "didn't find the band");
						}
					}
				});
	}

	void doLinkBand(final BlePeripheral peripheral) {
		_stage = STAGE_LINKING;

		linkHelper = new LinkHelper(_parentActivity, LinkBandDialog.this);
		linkHelper.setBand(new PowerBandDevice(peripheral));
		linkHelper.setParameter(_name, (int) _team._height,
				(int) _team._weight, (int) _team._stride, _team._message, _totalPowerPoints);
		linkHelper.run();

		setStatusMessage(_parentActivity.getString(R.string.registerband_connecting_band));
	}

	void doLinkBandWithServer(final BlePeripheral peripheral) {
		ServerManager.sharedInstance().replacePowerBand(_id, _deviceId == null ? "" : _deviceId, new RestCallback<StudentService.ResUpdateStudent>() {
			@Override
			public void success(StudentService.ResUpdateStudent resUpdateStudent, Response response) {
				Logger.log(TAG, "try to link (%d)", _nRetry);

				_stage = STAGE_LINKING;

				linkHelper = new LinkHelper(_parentActivity, LinkBandDialog.this);
				linkHelper.setBand(new PowerBandDevice(peripheral));
				linkHelper.setParameter(_name, (int) _team._height,
						(int) _team._weight, (int) _team._stride, _team._message, _totalPowerPoints);
				linkHelper.run();

				setStatusMessage(_parentActivity.getString(R.string.registerband_connecting_band));
			}

			@Override
			public void failure(RetrofitError retrofitError, String message) {
				error(STAGE_LINKING, message);
			}
		});

	}

	void stop() {
		if (_stage == STAGE_SEARCHING) {
			Logger.log(TAG, "Stop searching...");
			BandFinder.sharedFinder().stop();
		} else if (_stage == STAGE_LINKING) {
			Logger.log(TAG, "Stop linking...");

			if (linkHelper == null) {
				return;
			}

			linkHelper.stop();
		} else if (_stage == STAGE_CHECKING_DUPLICATE) {

		}
		_stage = STAGE_STOPPED;
		dismiss();
	}

	void setStatusMessage(String message) {
		((TextView) findViewById(R.id.textDescription)).setText(message);
	}

	void error(int error_code, String message) {
		if (_stage == STAGE_STOPPED)
			return;

		_stage = STAGE_STOPPED;

		dismiss();
		tvTitle.setKeepScreenOn(false);
		_listener.onFailed(error_code, message);
	}

	/////// onBandActionListener members

	@Override
	public void success(Object object) {
		LinkHelper.LinkResult result = (LinkHelper.LinkResult) object;
		Logger.log(TAG, "success registering band, version : %s", result.version);

		if (_stage == STAGE_STOPPED)
			return;

		dismiss();
		_listener.onCompleted();
	}

	@Override
	public void failed(int code, String message) {
		if (_stage == STAGE_STOPPED)
			return;

		Logger.error(TAG, "failed linking band(code=%s, msg=\"%s\")", code, message);

		_nRetry--;
		if (_nRetry > 0) {
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {

			}
			startSearch();
		} else {
			error(code, message);
		}
	}

	@Override
	public void connected() {
	}

	@Override
	public void reportForDaily(boolean didFinish, int agoDay, int wholeDay) {
	}

	@Override
	public void updateStatus(String status) {
		setStatusMessage(status);
	}

	public interface LinkBandDialogListener {
		void onCompleted();

		void onFailed(int error_code, String message);
	}
}
