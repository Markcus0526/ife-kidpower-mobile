package org.unicefkidpower.schools;

import android.content.Context;
import android.os.Bundle;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.swrve.sdk.SwrveSDK;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.schools.adapter.AvatarGridViewAdapter;
import org.unicefkidpower.schools.ble.BlePeripheral;
import org.unicefkidpower.schools.define.CommonUtils;
import org.unicefkidpower.schools.define.EventNames;
import org.unicefkidpower.schools.define.KPConstants;
import org.unicefkidpower.schools.event.EventManager;
import org.unicefkidpower.schools.event.SEvent;
import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.helper.DebouncedOnClickListener;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.model.Student;
import org.unicefkidpower.schools.model.Team;
import org.unicefkidpower.schools.model.Utils;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.StudentService;
import org.unicefkidpower.schools.sync.BandFinder;
import org.unicefkidpower.schools.sync.LinkBandDialog;
import org.unicefkidpower.schools.ui.BandCandidatesView;
import org.unicefkidpower.schools.ui.KPEditText;
import org.unicefkidpower.schools.ui.SegmentedGroup;
import org.unicefkidpower.schools.ui.UiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;

import static org.unicefkidpower.schools.sync.BandFinder.TIMEOUT_FOR_SEARCHING;

/**
 * Created by Ruifeng Shi on 1/3/2016.
 */
public class StudentCreateFragment extends BaseDialogFragment {
	static final String					TAG = "StudentEditFragment";

	protected Team						mTeam;
	private BlePeripheral				_selectedPeripheral;
	private boolean						isDetached;
	private int							scan_times = 0;

	// UI Elements
	private View						rootView;
	private HorizontalGridView			avatarGridView;
	private RelativeLayout				rlEdit;
	private EditText					editUsername;
	private KPEditText					fvBandCode;
	private SegmentedGroup				sgGender;

	private View						btnUnlink;
	private LinearLayout				llRescan;
	private TextView					lblScan;

	private ScrollView					svContent;
	private View						llTopPartial;
	private View						vwUsernameGender;
	private BandCandidatesView			vwBandCandidates;

	private AvatarGridViewAdapter		avatarGridViewAdapter;

	private String						gender;

	private boolean						isProcessing = false;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = super.onCreateView(inflater, container, savedInstanceState);

		FlurryAgent.onStartSession(parentActivity, "StudentCreateFragment");

		rootView.findViewById(R.id.btn_close).setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				dismissAllowingStateLoss();
			}
		});

		avatarGridView = (HorizontalGridView) rootView.findViewById(R.id.gv_avatar);
		avatarGridViewAdapter = new AvatarGridViewAdapter(parentActivity);
		avatarGridView.setAdapter(avatarGridViewAdapter);

		//Added to prevent horizontal gridview to scroll to the most left when an avatar is selected
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(parentActivity);
		linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

		avatarGridView.setLayoutManager(linearLayoutManager);

		rlEdit = (RelativeLayout) rootView.findViewById(R.id.layout_edit);

		editUsername = (EditText) rootView.findViewById(R.id.edit_username);
		editUsername.setOnFocusChangeListener(new OnEditTextFocusChangedListener());

		fvBandCode = (KPEditText) rootView.findViewById(R.id.select_band);
		fvBandCode.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				vwBandCandidates.setFilterKeyword(fvBandCode.getText().toString());
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});


		enableButton(rootView.findViewById(R.id.btn_cancel), false);
		enableButton(rootView.findViewById(R.id.btn_delete), false);
		btnUnlink = rootView.findViewById(R.id.btn_unlink);
		btnUnlink.setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				onUnlinkButtonClicked();
			}
		});

		llRescan = (LinearLayout) rootView.findViewById(R.id.llRescan);
		llRescan.setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				onRescanButtonClicked();
			}
		});

		lblScan = (TextView) rootView.findViewById(R.id.lblRescan);

		sgGender = (SegmentedGroup) rootView.findViewById(R.id.seggroup_gender);

		rootView.findViewById(R.id.btn_save).setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				onSaveButtonClicked();
			}
		});

		llTopPartial = rootView.findViewById(R.id.llTopPartial);
		vwUsernameGender = rootView.findViewById(R.id.llEditContent);

		vwBandCandidates = (BandCandidatesView) rootView.findViewById(R.id.view_band_candidates);
		vwBandCandidates.setVisibility(View.GONE);
		vwBandCandidates.setFilterKeyword("");
		vwBandCandidates.setOnItemSelectedListener(new BandCandidatesView.OnItemSelectedListener() {
			@Override
			public void OnItemSelected(int index, Object object) {
				Logger.log("CandidatesView", "% position item clicked(%s)", index, object == null ? "null" : object.toString());

				if (!(object instanceof BlePeripheral))
					return;

				BlePeripheral peripheral = (BlePeripheral) object;
				onBandSelected(index, peripheral);
			}
		});

		initData();
		onRescanButtonClicked();

		isDetached = false;

		setCancelable(false);

		return rootView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_create_student;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		// stop scanning
		BandFinder.sharedFinder().stop();
		EventManager.sharedInstance().unregister(this);
		isDetached = true;
	}

	@Override
	protected boolean isUseEvent() {
		return true;
	}

	protected void initData() {
		editUsername.setText("");
		setBandCodeUI("");

		gender = Student.GENDER_UNDEFINED;
		sgGender.setOnCheckedChangeListener(new OnGenderRadioGroupCheckChangedListener());
		sgGender.check(R.id.radio_skip);
	}

	protected String getChooseAvatarString() {
		int nNumberOfAvatars = UiUtils.getInstance().getNumberOfAvatars();
		return String.format(
				parentActivity.getString(R.string.choose_avatar_status),
				nNumberOfAvatars
		);
	}

	public void setTeam(Team team) {
		this.mTeam = team;
	}

	protected void hideTopPartial() {
		if (llTopPartial == null || vwUsernameGender == null) {
			return;
		}

		llTopPartial.setVisibility(View.GONE);
		vwUsernameGender.setVisibility(View.GONE);
	}

	protected void showTopPartial() {
		if (llTopPartial == null || vwUsernameGender == null) {
			return;
		}

		llTopPartial.setVisibility(View.VISIBLE);
		vwUsernameGender.setVisibility(View.VISIBLE);
	}

	protected void hideBandCandidates() {
		if (vwBandCandidates.getVisibility() != View.VISIBLE) {
			return;
		}

		if (isTopPartialHidden())
			showTopPartial();
	}

	protected void showBandCandidates(List<BlePeripheral> scannedItems) {
		if (scannedItems == null || scannedItems.isEmpty()) {
			Toast.makeText(parentActivity, getString(R.string.no_band_nearby), Toast.LENGTH_SHORT).show();
			return;
		}

		vwBandCandidates.setBandCandidates(scannedItems);
		vwBandCandidates.setVisibility(View.VISIBLE);
	}

	protected boolean isTopPartialHidden() {
		boolean isTopPartialHidden = false;
		if (llTopPartial != null && vwUsernameGender != null) {
			if (llTopPartial.getVisibility() == View.GONE && vwUsernameGender.getVisibility() == View.GONE)
				isTopPartialHidden = true;
		}

		vwBandCandidates.setVisibility(View.GONE);

		return isTopPartialHidden;
	}

	protected void onUnlinkButtonClicked() {
		setBandCodeUI("");
	}

	protected void onRescanButtonClicked() {
		fvBandCode.setText("");
		scanBand();
	}

	protected void onSaveButtonClicked() {
		if (isProcessing) {
			Logger.error(TAG, "you tapped Save button, but processing now");
			return;
		}

		isProcessing = true;

		UiUtils.hideKeyboard(parentActivity);

		// checking name is empty
		String inputtedName = editUsername.getText().toString();
		if (inputtedName.length() > 12) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.profile_name_max_length),
					parentActivity);
			isProcessing = false;
			return;
		}

		String szName = Utils.getStudentName(inputtedName);
		editUsername.setText(szName);
		if (TextUtils.isEmpty(szName)) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.profile_name_required),
					parentActivity);
			isProcessing = false;
			return;
		}

		if (_selectedPeripheral != null) {
			// if already selected band, at first linking band
			linkBand();
		} else {
			// if don't selected band, register new student with empty band
			createNewStudent();
		}
	}

	protected void onBandSelected(int index, BlePeripheral peripheral) {
		Logger.log(TAG, "onBandSelected %d", index);
		BandFinder.sharedFinder().stop();

		checkingDuplicate(peripheral);
	}

	protected void linkBand() {
		String name = editUsername.getText().toString();
		if (name.isEmpty()) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.profile_name_required),
					parentActivity);
			return;
		}

		if (_selectedPeripheral == null) {
			return;
		}

		SwrveSDK.event(KPConstants.SWRVE_BAND_LINK_STARTED);
		Logger.log(TAG, "Linking for student, name:%s, device:%s", name, _selectedPeripheral.getMACAddress());

		LinkBandDialog dialog = new LinkBandDialog(parentActivity, new LinkBandDialog.LinkBandDialogListener() {
			@Override
			public void onCompleted() {
				Logger.log(TAG, "success relink band");
				SwrveSDK.event(KPConstants.SWRVE_BAND_LINK_SUCCESS);

				createNewStudent();
			}

			@Override
			public void onFailed(int error_code, String message) {
				Logger.error(TAG, "failed relink band(code=%d, message=\"%s\")", error_code, message);
				Map<String, String> payload = new HashMap<>();
				payload.put("error_code", "" + error_code);
				payload.put("error_msg", message);
				SwrveSDK.event(KPConstants.SWRVE_BAND_LINK_ERROR, payload);

				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_failed), message, getActivity());
				isProcessing = false;
			}
		});
		dialog.setParameters(mTeam, 0, name, _selectedPeripheral.getMACAddress(), 0);
		dialog.show();
	}


	protected void scanBand() {
		hideBandCandidates();
		llRescan.setVisibility(View.GONE);
		fvBandCode.setHint(getString(R.string.scanning));
		scan_times++;

		// start Scan
		BandFinder.sharedFinder().search(
				parentActivity,
				ServerManager.sharedInstance().getAllBands(),
				TIMEOUT_FOR_SEARCHING + scan_times * 10 * 1000,
				new BandFinder.OnDiscoveredBandListener() {
					@Override
					public void onDiscovered(BlePeripheral newPeripheral, List<BlePeripheral> scannedItems) {
						showBandCandidates(scannedItems);
					}

					@Override
					public void onError(int error, String message) {}

					@Override
					public void onEnd(List<BlePeripheral> scannedItems, boolean byUserRequest) {
						if (isDetached())
							return;

						llRescan.setVisibility(View.VISIBLE);
						lblScan.setText(getString(R.string.button_rescan));
						fvBandCode.setHint(getString(R.string.band_id_string));

						if (!byUserRequest) {
							showBandCandidates(scannedItems);
						}
					}
				}
		);
	}


	/**
	 * changing band id
	 *
	 * @param peripheral : ble peripheral
	 */
	protected void checkingDuplicate(BlePeripheral peripheral) {
		Logger.log(TAG, "Checking Duplicate : %s", peripheral.getMACAddress());

		UIManager.sharedInstance().showProgressDialog(parentActivity, null, "checking band now...", true);

		_selectedPeripheral = peripheral;

		ArrayList<String> deviceIds = new ArrayList<String>();
		deviceIds.add(_selectedPeripheral.getMACAddress());
		ServerManager.sharedInstance().byDeviceIds(deviceIds, new RestCallback<List<StudentService.ResByDeviceIds>>() {
			@Override
			public void success(List<StudentService.ResByDeviceIds> resByDeviceIdes, Response response) {
				UIManager.sharedInstance().dismissProgressDialog();
				// duplication checking
				if (resByDeviceIdes == null ||
						resByDeviceIdes.size() == 0) {
					didCheckedDuplicating(false);
				} else {
					if (resByDeviceIdes.get(0).deviceId != null &&
							resByDeviceIdes.get(0).deviceId.equalsIgnoreCase(_selectedPeripheral.getMACAddress())) {
						didCheckedDuplicating(true);
					} else {
						didCheckedDuplicating(false);
					}
				}
			}

			@Override
			public void failure(RetrofitError retrofitError, String message) {
				UIManager.sharedInstance().dismissProgressDialog();
				// duplication checked
				if (retrofitError.getKind() == RetrofitError.Kind.NETWORK) {
					AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
							getActivity().getString(R.string.registerband_duplication_check_failed),
							getActivity());
				} else {
					didCheckedDuplicating(false);
				}
			}
		});
	}

	protected void didCheckedDuplicating(boolean isDuplicated) {
		if (isDuplicated) {
			_selectedPeripheral = null;
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_alert),
					getActivity().getString(R.string.registerband_band_registered),
					getActivity());
			return;
		}
		hideBandCandidates();
		setBandCodeUI(_selectedPeripheral.getCode());
	}


	protected void setBandCodeUI(String deviceCode) {
		if (TextUtils.isEmpty(deviceCode)) {
			fvBandCode.setHint(parentActivity.getString(R.string.band_id_string));
			llRescan.setVisibility(View.VISIBLE);

			enableButton(btnUnlink, false);
		} else {
			fvBandCode.setText(deviceCode);
			llRescan.setVisibility(View.GONE);

			enableButton(btnUnlink, true);
		}
	}


	private void createNewStudent() {
		Logger.log(TAG, "=> Create New Student");

		UIManager.sharedInstance().showProgressDialog(parentActivity, null, getString(R.string.app_onemoment), true);

		final String deviceId = _selectedPeripheral == null ? "" : _selectedPeripheral.getMACAddress();
		// call api
		Logger.log(TAG, "create NewStudent => Calling api now...");
		ServerManager.sharedInstance().createStudent(
				editUsername.getText().toString(),
				gender,
				deviceId,
				mTeam._id,
				false, avatarGridViewAdapter.getSelectedAvatarId(),
				new RestCallback<StudentService.ResCreateStudent>() {
					@Override
					public void success(StudentService.ResCreateStudent resCreateStudent, Response response) {
						isProcessing = false;

						UIManager.sharedInstance().dismissProgressDialog();

						EventManager.sharedInstance().post(EventNames.EVENT_STUDENT_CREATED, resCreateStudent);
						dismissAllowingStateLoss();

						ServerManager.sharedInstance().didLinkedBand(deviceId);
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						isProcessing = false;

						UIManager.sharedInstance().dismissProgressDialog();
						ServerManager.getErrorMessageForRetrofitError(retrofitError, parentActivity);
						AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
								parentActivity.getString(R.string.signup_createstudent_failed),
								parentActivity);
					}
				}
		);
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent e) {
		if (EventManager.isEvent(e, EventNames.EVENT_KEYBOARD_SHOWN)) {
			if (editUsername.hasFocus() || fvBandCode.hasFocus()) {
				adjustScrollViewSize((int) e.object);
			}
		}
	}


	public void adjustScrollViewSize(int keyboardHeight) {
		if (svContent == null)
			return;

		RelativeLayout.LayoutParams lpContentScrollView = (RelativeLayout.LayoutParams) svContent.getLayoutParams();
		lpContentScrollView.bottomMargin = keyboardHeight;
		svContent.setLayoutParams(lpContentScrollView);
	}


	void enableButton(View view, boolean enabled) {
		if (view == null)
			return;

		view.setBackgroundColor(CommonUtils.getColorFromRes(getResources(), enabled ? R.color.kidpower_azure : R.color.kidpower_light_grey));
		view.setEnabled(enabled);
	}


	private class OnGenderRadioGroupCheckChangedListener implements RadioGroup.OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
				case R.id.radio_skip:
					gender = Student.GENDER_UNDEFINED;
					break;
				case R.id.radio_female:
					gender = Student.GENDER_GIRL;
					break;
				case R.id.radio_male:
					gender = Student.GENDER_BOY;
					break;
			}
		}
	}


	private class OnEditTextFocusChangedListener implements View.OnFocusChangeListener {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (v.getId() == R.id.edit_username && !hasFocus) {
				InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(
						Context.INPUT_METHOD_SERVICE
				);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}
		}
	}
}
