package org.unicefkidpower.schools;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v17.leanback.widget.OnChildSelectedListener;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.swrve.sdk.SwrveSDK;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.schools.adapter.AvatarGridViewAdapter;
import org.unicefkidpower.schools.ble.BlePeripheral;
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
import org.unicefkidpower.schools.model.UserManager;
import org.unicefkidpower.schools.model.Utils;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.StudentService;
import org.unicefkidpower.schools.sync.BandFinder;
import org.unicefkidpower.schools.sync.LinkBandDialog;
import org.unicefkidpower.schools.sync.SyncPersonalDialog;
import org.unicefkidpower.schools.sync.UnlinkBandDialog;
import org.unicefkidpower.schools.ui.BandCandidatesView;
import org.unicefkidpower.schools.ui.KPEditText;
import org.unicefkidpower.schools.ui.KPImageTextButton;
import org.unicefkidpower.schools.ui.SegmentedGroup;
import org.unicefkidpower.schools.ui.UiUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit.RetrofitError;
import retrofit.client.Response;

import static org.unicefkidpower.schools.sync.BandFinder.TIMEOUT_FOR_SEARCHING;

/**
 * Created by Ruifeng Shi on 8/29/2016.
 */
public class StudentFragment extends BaseDialogFragment {
	static final String TAG = "StudentFragment";

	static final String EVENT_GETDATA_SUCCESS = "PROFILE_EVENT_GETSTUDENTDATA_SUCCESS";
	static final String EVENT_GETDATA_FAILED = "PROFILE_EVENT_GETSTUDENTDATA_FAILED";

	static final String EVENT_UPDATEINFOR_SUCCESS = "PROFILE_EVENT_UPDATE_INFORMATION_SUCCESS";
	static final String EVENT_UPDATEINFOR_FAILED = "PROFILE_EVENT_UPDATE_INFORMATION_FAILED";

	static final String EVENT_PROFILE_UPDATED = "PROFILE_UPDATED";

	private Student mStudent;
	private Team mTeam;
	private BlePeripheral _selectedPeripheral;

	private int scan_times = 0;

	private TextView txtTitle, txtStatement;
	private TextView txtPackets, txtPowerPoints, txtSteps;

	private ImageView ivAvatar;
	private HorizontalGridView avatarGridView;
	private AvatarGridViewAdapter avatarGridViewAdapter;

	private EditText editUsername;
	private KPEditText txtBandId;

	private SegmentedGroup sgGender;
	private View vwScan;
	private TextView lblScan;

	private View vwEdit, vwUnlock;

	private KPImageTextButton btnLinkOrSync;
	private BandCandidatesView vwBandCandidates;

	private String gender;

	private View btnCancel;
	private View btnDelete;
	private View btnUnlink;
	private View btnSave;

	private boolean isProcessing = false;
	private boolean isModified = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = super.onCreateView(inflater, container, savedInstanceState);

		FlurryAgent.onStartSession(parentActivity, "Student Profile Fragment");

		for (Team team : UserManager.sharedInstance()._currentUser._teams) {
			if (mStudent._teamId == team._id) {
				mTeam = team;
				break;
			}
		}

		rootView.findViewById(R.id.btn_close).setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				onCancelButtonClicked();
			}
		});

		txtTitle = (TextView) rootView.findViewById(R.id.txt_title);

		ivAvatar = (ImageView) rootView.findViewById(R.id.iv_avatar);
		avatarGridView = (HorizontalGridView) rootView.findViewById(R.id.gv_avatar);
		avatarGridViewAdapter = new AvatarGridViewAdapter(parentActivity);
		avatarGridView.setAdapter(avatarGridViewAdapter);
		avatarGridView.setOnChildSelectedListener(new OnChildSelectedListener() {
			@Override
			public void onChildSelected(ViewGroup parent, View view, int position, long id) {
				avatarGridView.setVisibility(View.GONE);
				ivAvatar.setVisibility(View.VISIBLE);
				ivAvatar.setImageDrawable(UiUtils.getInstance().getAvatarDrawable(
						avatarGridViewAdapter.getSelectedAvatarId()
				));
			}
		});
		ivAvatar.setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				ivAvatar.setVisibility(View.INVISIBLE);
				avatarGridView.setVisibility(View.VISIBLE);
			}
		});

		//Added to prevent horizontal gridview to scroll to the most left when an avatar is selected
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(parentActivity);
		linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		avatarGridView.setLayoutManager(linearLayoutManager);

		txtPackets = (TextView) rootView.findViewById(R.id.txt_packets_value);
		txtPowerPoints = (TextView) rootView.findViewById(R.id.txt_power_points_value);
		txtSteps = (TextView) rootView.findViewById(R.id.txt_steps_value);

		txtStatement = (TextView) rootView.findViewById(R.id.txt_statement);
		txtBandId = (KPEditText) rootView.findViewById(R.id.select_band);
		txtBandId.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				vwBandCandidates.setFilterKeyword(txtBandId.getText().toString());
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
//		txtBandId.setOnClickListener(new DebouncedOnClickListener() {
//			@Override
//			public void onDebouncedClick(View v) {
//				hideBandCandidates();
//				BandFinder.sharedFinder().stop();
//			}
//		});

		btnLinkOrSync = (KPImageTextButton) rootView.findViewById(R.id.btn_sync);
		// btnLinkOrSync.setGravity(Gravity.CENTER);
		btnLinkOrSync.setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				if (mStudent.isUnlinkedBand()) {
					onRescanButtonClicked();
				} else {
					onSyncButtonClicked();
				}
			}
		});

		editUsername = (EditText) rootView.findViewById(R.id.edit_username);
		editUsername.setOnFocusChangeListener(new OnEditTextFocusChangedListener());
		editUsername.setText(mStudent._name);

		vwEdit = rootView.findViewById(R.id.rlContent);
		vwUnlock = rootView.findViewById(R.id.llUnlock);
		vwUnlock.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// none processing
			}
		});
		rootView.findViewById(R.id.ivUnlock).setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				onUnlockButtonClicked(true);
			}
		});

		vwScan = rootView.findViewById(R.id.llRescan);
		vwScan.setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				onRescanButtonClicked();
			}
		});
		lblScan = (TextView) rootView.findViewById(R.id.lblRescan);

		sgGender = (SegmentedGroup) rootView.findViewById(R.id.seggroup_gender);
		sgGender.setOnCheckedChangeListener(new OnGenderRadioGroupCheckChangedListener());

		getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK &&
						event.getAction() == KeyEvent.ACTION_UP) {
					onCancelButtonClicked();
					return true;
				}
				return false;
			}
		});

		btnCancel = rootView.findViewById(R.id.btn_cancel);
		btnDelete = rootView.findViewById(R.id.btn_delete);
		btnUnlink = rootView.findViewById(R.id.btn_unlink);
		btnSave = rootView.findViewById(R.id.btn_save);

		btnCancel.setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				onCancelButtonClicked();
			}
		});
		btnDelete.setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				onDeleteButtonClicked();
			}
		});
		btnUnlink.setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				onUnlinkButtonClicked();
			}
		});
		btnSave.setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				onSaveButtonClicked();
			}
		});

		enableButton(btnCancel, true);
		enableButton(btnDelete, true);
		enableButton(btnUnlink, false);
		enableButton(btnSave, true);

		vwBandCandidates = (BandCandidatesView) rootView.findViewById(R.id.view_band_candidates);
		vwBandCandidates.setVisibility(View.GONE);
		vwBandCandidates.setFilterKeyword("");
		vwBandCandidates.setOnItemSelectedListener(new BandCandidatesView.OnItemSelectedListener() {
			@Override
			public void OnItemSelected(int index, Object object) {
				if (!(object instanceof BlePeripheral))
					return;

				BlePeripheral peripheral = (BlePeripheral) object;
				onBandSelected(index, peripheral);

//				if (!isUnlockedEdition()) {
//					unlockAndLinkBand(index, (BlePeripheral) object);
//				} else {
//					BlePeripheral peripheral = (BlePeripheral) object;
//					onBandSelected(index, peripheral);
//				}
			}
		});

		showData();
		unlockEdition(false);

		getStudentData();

		// start scanning when student hasn't linked yet.
		onRescanButtonClicked();

		setCancelable(false);

		return rootView;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
	}

	void showData() {
		txtTitle.setText(mStudent._name);
		editUsername.setText(mStudent._name);

		ivAvatar.setImageDrawable(UiUtils.getInstance().getAvatarDrawable(mStudent._imageSrc));
		avatarGridViewAdapter.setAvatarSelection(mStudent._imageSrc);

		setBandId(mStudent.getDeviceCode());

		txtPackets.setText(Utils.parsePacketWithCurrencyFormat(mStudent._packets));
		txtPowerPoints.setText(Utils.parsePowerpointWithCurrencyFormat(mStudent._powerPoints));
		txtSteps.setText(Utils.parseStepWithCurrencyFormat(mStudent._steps));

		gender = mStudent._gender;
		if (gender.equalsIgnoreCase(Student.GENDER_GIRL)) {
			sgGender.check(R.id.radio_female);
		} else if (gender.equalsIgnoreCase(Student.GENDER_BOY)) {
			sgGender.check(R.id.radio_male);
		} else {
			sgGender.check(R.id.radio_skip);
		}

		if (mStudent._displayMessage != null &&
				TextUtils.isEmpty(mStudent._displayMessage.message))
			txtStatement.setText(mStudent._displayMessage.message);
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_student;
	}

	public void setData(Student student) {
		this.mStudent = student;
	}

	@Override
	protected boolean isUseEvent() {
		return true;
	}

	boolean isStudentChanged() {
		return isNameChanged() ||
				!gender.equalsIgnoreCase(mStudent._gender) ||
				!avatarGridViewAdapter.getSelectedAvatarId().equals(mStudent._imageSrc);
	}

	boolean isNameChanged() {
		String sUsername = editUsername.getText().toString();
		return !sUsername.equalsIgnoreCase(mStudent._name);
	}

	boolean isGenderChanged() {
		return !gender.equalsIgnoreCase(mStudent._gender);
	}

	boolean isAvatarChanged() {
		return !avatarGridViewAdapter.getSelectedAvatarId().equals(mStudent._imageSrc);
	}

	void hideBandCandidates() {
		vwBandCandidates.setVisibility(View.GONE);
	}

	void showBandCandidates(List<BlePeripheral> scannedItems) {
		if (scannedItems == null || scannedItems.isEmpty()) {
			Toast.makeText(parentActivity, getString(R.string.no_band_nearby), Toast.LENGTH_SHORT).show();
			return;
		}

		vwBandCandidates.setBandCandidates(scannedItems);
		vwBandCandidates.setVisibility(View.VISIBLE);
	}

	void closeDialog() {
		if (isModified) {
			EventManager.sharedInstance().post(EventNames.EVENT_STUDENT_UPDATED);
		}
		BandFinder.sharedFinder().stop();
		dismissAllowingStateLoss();
	}

	void onSyncButtonClicked() {
		if (isProcessing)
			return;

		isProcessing = true;

		if (mStudent.isUnlinkedBand()) {
			isProcessing = false;
			return;
		}

		new SyncPersonalDialog(parentActivity, mTeam, mStudent,
				new SyncPersonalDialog.SyncDialogListener() {
					@Override
					public void onCompleted(boolean success, String errMsg) {
						isProcessing = false;
						isModified = true;
					}

					@Override
					public void onSeeMyStats() {
						isProcessing = false;
						getStudentData();
					}
				}
		).show();
	}

	void onDeleteButtonClicked() {
		Logger.log(TAG, "be going to delete the STUDENT(%s)", mStudent._name);

		final String bandId = mStudent.getDeviceId();

		new ConfirmDialog(parentActivity, new ConfirmDialog.SaveConfirmDialogListener() {
			@Override
			public void onNegative() {
				Logger.log(TAG, "deleting STUDENT(%s) canceled.", mStudent._name);
			}

			@Override
			public void onPositive() {
				Logger.log(TAG, "deleting STUDENT(%s)...", mStudent._name);

				UIManager.sharedInstance().showProgressDialog(parentActivity, null, getString(R.string.app_onemoment), true);

				ServerManager.sharedInstance().deleteStudent(mStudent._id, new RestCallback<String>() {
					@Override
					public void failure(RetrofitError retrofitError, String message) {
						UIManager.sharedInstance().dismissProgressDialog();

						AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_failed),
								parentActivity.getString(R.string.profile_delete_failed),
								parentActivity);
					}

					@Override
					public void success(String s, Response response) {
						UIManager.sharedInstance().dismissProgressDialog();

						ServerManager.sharedInstance().didLinkedBand(bandId);
						EventManager.sharedInstance().post(EventNames.EVENT_LOAD_ALL_LINKED_BAND);

						isModified = true;
						closeDialog();
					}
				});
			}
		}).setCaption(getString(R.string.delete_student_confirm_caption))
				.setButtonTitle(getString(R.string.button_icon_ok), getString(R.string.button_icon_cancel))
				.show();
	}

	void onCancelButtonClicked() {
		if (isStudentChanged()) {
			new ConfirmDialog(parentActivity, new ConfirmDialog.SaveConfirmDialogListener() {
				@Override
				public void onPositive() {
					onSaveButtonClicked();
				}

				@Override
				public void onNegative() {
					closeDialog();
				}
			}).setCaption(getString(R.string.save_confirm_caption))
					.setButtonTitle(getString(R.string.button_icon_save), getString(R.string.button_icon_discard))
					.show();
		} else {
			closeDialog();
		}
	}

	void onUnlockButtonClicked(Boolean checkCoach) {
		if (checkCoach) {
			//Display Teacher Authentication Dialog
			new CoachPasswordDialog(
					parentActivity,
					new CoachPasswordDialog.CoachPasswordDialogListener() {
						@Override
						public void onEnter(String coachPassword) {
							unlockEdition(true);
							onRescanButtonClicked();
						}

						@Override
						public void onCancel() {
						}
					}
			).show();
		} else {
			unlockEdition(true);
			onRescanButtonClicked();
		}
	}

	void onUnlinkButtonClicked() {
		if (mStudent.isUnlinkedBand()) {
			return;
		}

		Logger.log(TAG, "User are going to unlink band(user:%s, band:%s",
				mStudent._name, mStudent.getDeviceCode());

		UnlinkBandDialog dlg = new UnlinkBandDialog(mStudent._id,
				parentActivity,
				new UnlinkBandDialog.DialogListener() {
					@Override
					public void onSuccess() {
						Logger.log(TAG, "unlink success");
						ServerManager.sharedInstance().didDeLinkedBand(mStudent.getDeviceId());

						txtBandId.setHint(getString(R.string.not_linked));
						txtBandId.setText("");

						mStudent.setDeviceId("");
						setBandId(mStudent.getDeviceCode());
						EventManager.sharedInstance().post(EVENT_PROFILE_UPDATED);


						isModified = true;

						SwrveSDK.event(KPConstants.SWRVE_BAND_UNLINKED);
					}

					@Override
					public void onFailed(String message) {
						AlertDialogWrapper.showErrorAlert(parentActivity.getString(R.string.dialog_error),
								message == null ? parentActivity.getString(R.string.error_unknown) : message, parentActivity);
//						Toast.makeText(parentActivity, message/*"un-link failed"*/, Toast.LENGTH_SHORT).show();
					}
				}
		);
		dlg.show();
	}

	protected void unlockAndLinkBand(final int index, final BlePeripheral object) {
		new CoachPasswordDialog(
				parentActivity,
				new CoachPasswordDialog.CoachPasswordDialogListener() {
					@Override
					public void onEnter(String coachPassword) {
						BlePeripheral peripheral = object;
						onBandSelected(index, peripheral);
					}

					@Override
					public void onCancel() {
					}
				}
		).show();
	}

	protected void onRescanButtonClicked() {
		if (mStudent.isUnlinkedBand()) {
			txtBandId.setText("");
			scanBand();
		}
	}

	protected void onSaveButtonClicked() {
		if (isProcessing)
			return;

		isProcessing = true;
		UiUtils.hideKeyboard(parentActivity);

		String inputtedName = editUsername.getText().toString();
		if (inputtedName.length() > 12) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.profile_name_max_length), parentActivity);
			isProcessing = false;
			return;
		}
		// checking name is empty
		final String szName = Utils.getStudentName(inputtedName);
		editUsername.setText(szName);
		if (szName == null || szName.length() == 0) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.profile_name_required), parentActivity);
			isProcessing = false;
			return;
		}

		// no changed. don't do any action
		if (!isStudentChanged()) {
			closeDialog();
			isProcessing = false;
			return;
		}

		EventManager.sharedInstance().post(EVENT_UPDATEINFOR_SUCCESS, this);

		/*mStudent._imageSrc = avatarGridViewAdapter.getSelectedAvatarId();
		if (isNameChanged()) {
			changingName(szName);
		} else {
			EventManager.sharedInstance().post(EVENT_UPDATEINFOR_SUCCESS, this);
		}*/
	}

	void unlockEdition(boolean unlock) {
		if (unlock) {
			vwUnlock.setVisibility(View.GONE);
			editUsername.setEnabled(true);
		} else {
			vwUnlock.setVisibility(View.VISIBLE);
			editUsername.setEnabled(false);
		}
	}

	boolean isUnlockedEdition() {
		if (vwUnlock.getVisibility() == View.VISIBLE)
			return false;
		else
			return true;
	}

	protected void onBandSelected(int index, BlePeripheral peripheral) {
		BandFinder.sharedFinder().stop();

		linkBand(peripheral);
	}

	protected void linkBand(BlePeripheral peripheral) {
		if (peripheral == null) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.did_not_found_band),
					parentActivity);
			return;
		}
		_selectedPeripheral = peripheral;

		Logger.log(TAG, "try to link band(user:%s, device:%s(%s)",
				mStudent._name, _selectedPeripheral.getCode(), _selectedPeripheral.getMACAddress());
		SwrveSDK.event(KPConstants.SWRVE_BAND_LINK_STARTED);

		LinkBandDialog dialog = new LinkBandDialog(parentActivity, new LinkBandDialog.LinkBandDialogListener() {
			@Override
			public void onCompleted() {
				Logger.log(TAG, "success registering band");
				SwrveSDK.event(KPConstants.SWRVE_BAND_LINK_SUCCESS);

				hideBandCandidates();
				//changingBand(_selectedPeripheral.getMACAddress());

				UIManager.sharedInstance().dismissProgressDialog();

				mStudent.setDeviceId(_selectedPeripheral.getMACAddress());
				setBandId(mStudent.getDeviceCode());

				ServerManager.sharedInstance().didLinkedBand(_selectedPeripheral.getMACAddress());

				isModified = true;
			}

			@Override
			public void onFailed(int error_code, String message) {
				Logger.error(TAG, "failed registering band(code=%d, message=\"%s\")", error_code, message);

				Map<String, String> payload = new HashMap<>();
				payload.put("error_code", "" + error_code);
				payload.put("error_msg", message);
				SwrveSDK.event(KPConstants.SWRVE_BAND_LINK_ERROR, payload);

				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_failed), message, getActivity());
			}
		});
		dialog.setParameters(mTeam, mStudent._id, mStudent._name, _selectedPeripheral.getMACAddress(), mStudent._powerPoints);
		dialog.show();
	}

	/**
	 * changing band id
	 *
	 * @param deviceId : band id, maybe deviceId is null or empty
	 */
	protected void changingBand(final String deviceId) {
		UIManager.sharedInstance().showProgressDialog(parentActivity, null, getString(R.string.app_onemoment), true);

		ServerManager.sharedInstance().replacePowerBand(mStudent._id, deviceId == null ? "" : deviceId, new RestCallback<StudentService.ResUpdateStudent>() {
			@Override
			public void success(StudentService.ResUpdateStudent resUpdateStudent, Response response) {
				mStudent.setDeviceId(resUpdateStudent.deviceId);

				UIManager.sharedInstance().dismissProgressDialog();

				setBandId(mStudent.getDeviceCode());
				EventManager.sharedInstance().post(EVENT_PROFILE_UPDATED, resUpdateStudent);

				ServerManager.sharedInstance().didLinkedBand(deviceId);

				isModified = true;
			}

			@Override
			public void failure(RetrofitError retrofitError, String message) {
				UIManager.sharedInstance().dismissProgressDialog();
				Toast.makeText(parentActivity, message/*"Cannot link this band."*/, Toast.LENGTH_SHORT).show();
			}
		});
	}

	protected void scanBand() {
		//start Scan
		hideBandCandidates();

		vwScan.setVisibility(View.GONE);
		txtBandId.setHint(getString(R.string.scanning));
		scan_times++;

		BandFinder.sharedFinder().search(
				getActivity(),
				ServerManager.sharedInstance().getAllBands(),
				TIMEOUT_FOR_SEARCHING + scan_times * 10 * 1000,
				new BandFinder.OnDiscoveredBandListener() {
					@Override
					public void onDiscovered(BlePeripheral newPeripheral, List<BlePeripheral> scannedItems) {
						if (isDetached()) return;
						showBandCandidates(scannedItems);
					}

					@Override
					public void onError(int error, String message) {
						AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error), message, parentActivity);
					}

					@Override
					public void onEnd(List<BlePeripheral> scannedItems, boolean byUserRequest) {
						if (isDetached()) return;

						lblScan.setText(getString(R.string.button_rescan));
						vwScan.setVisibility(View.VISIBLE);
						txtBandId.setHint(getString(R.string.not_linked));

						if (!byUserRequest) {
							showBandCandidates(scannedItems);
						}
					}
				});
	}

	/*protected void changingName(String szName) {
		isProcessing = true;
		if (!isNameChanged()) {
			isProcessing = false;
			return;
		}

		if (mStudent.isUnlinkedBand()) {
			mStudent._name = szName;
			EventManager.sharedInstance().post(EVENT_UPDATEINFOR_SUCCESS, this);
			return;
		}

		Logger.log(TAG, "Update name, name:%s, device:%s", szName, mStudent.getDeviceId());

		ServerManager.sharedInstance().updateStudentName(mStudent._id, szName, new RestCallback<StudentService.ResUpdateStudent>() {
			@Override
			public void failure(RetrofitError retrofitError, String message) {
				Logger.error(TAG, "failed setting name(message:\"%s\")", message);

				AlertDialogWrapper.showErrorAlert(getString(R.string.profile_update_failed), message, parentActivity);
				EventManager.sharedInstance().post(EVENT_UPDATEINFOR_FAILED, StudentFragment.this);
			}

			@Override
			public void success(StudentService.ResUpdateStudent resUpdateStudent, Response response) {
				Logger.log(TAG, "success setting name");

				mStudent._name = editUsername.getText().toString();
				EventManager.sharedInstance().post(EVENT_UPDATEINFOR_SUCCESS, StudentFragment.this);
			}
		});

		LinkBandDialog dialog = new LinkBandDialog(parentActivity, new LinkBandDialog.LinkBandDialogListener() {
			@Override
			public void onCompleted() {
				Logger.log(TAG, "success setting name");

				mStudent._name = editUsername.getText().toString();
				EventManager.sharedInstance().post(EVENT_UPDATEINFOR_SUCCESS, StudentFragment.this);
			}

			@Override
			public void onFailed(int error_code, String message) {
				Logger.error(TAG, "failed setting name(code=%d, message:\"%s\")", error_code, message);

				AlertDialogWrapper.showErrorAlert(getString(R.string.profile_update_failed), message, parentActivity);
				EventManager.sharedInstance().post(EVENT_UPDATEINFOR_FAILED, StudentFragment.this);
			}
		});
		dialog.setParameters(mTeam, mStudent._id, szName, mStudent.getDeviceId(), mStudent._powerPoints);
		dialog.setUpdatingName();
		dialog.show();
	}*/

	void setBandId(String bandId) {
		if (!TextUtils.isEmpty(bandId)) {
			txtBandId.setText(bandId);
			vwScan.setVisibility(View.GONE);
			lblScan.setText(R.string.button_scan);

			btnLinkOrSync.setCustomImage(R.drawable.sync_white);
			btnLinkOrSync.setText(parentActivity.getString(R.string.sync));
			enableButton(btnUnlink, true);
		} else {
			if (!txtBandId.getText().toString().equalsIgnoreCase(getString(R.string.scanning))) {
				txtBandId.setHint(R.string.not_linked);
				vwScan.setVisibility(View.VISIBLE);
			} else {
				vwScan.setVisibility(View.GONE);
			}
			btnLinkOrSync.setCustomImage(R.drawable.icon_link);
			btnLinkOrSync.setText(parentActivity.getString(R.string.link_band));


			enableButton(btnUnlink, false);
		}
	}

	private void getStudentData() {
		if (mStudent == null) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					parentActivity.getString(R.string.profile_student_notfound),
					parentActivity);
			return;
		}

		// final Object dlg = UIManager.sharedInstance().showProgressDialog(parentActivity, null,
		//        parentActivity.getString(R.string.profile_loading_student), true);
		ServerManager.sharedInstance().getStudentData(mStudent._id, new RestCallback<StudentService.ResGetStudentData>() {
			@Override
			public void success(StudentService.ResGetStudentData resGetStudentData, Response response) {
				// UIManager.sharedInstance().dismissProgressDialog(dlg);
				EventManager.sharedInstance().post(EVENT_GETDATA_SUCCESS, resGetStudentData);
			}

			@Override
			public void failure(RetrofitError retrofitError, String message) {
				// UIManager.sharedInstance().dismissProgressDialog(dlg);
				EventManager.sharedInstance().post(EVENT_GETDATA_FAILED, message);
			}
		});
	}

	private void updateStudentInformation() {
		UIManager.sharedInstance().showProgressDialog(parentActivity, null, getString(R.string.app_onemoment), true);

		Map<String, String> params = new HashMap<String, String>();
		if (isAvatarChanged())
			params.put("imageSrc", avatarGridViewAdapter.getSelectedAvatarId());
		if (isNameChanged())
			params.put("name", editUsername.getText().toString());
		if (isGenderChanged())
			params.put("gender", gender);

		//Student updatedStudent = mStudent;
		//updatedStudent._gender = gender;
		//updatedStudent._name = editUsername.getText().toString();

		ServerManager.sharedInstance().updateStudentWithParam(mStudent._id, params, new RestCallback<StudentService.ResCreateStudent>() {
			@Override
			public void failure(RetrofitError retrofitError, String message) {
				UIManager.sharedInstance().dismissProgressDialog();
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
						parentActivity.getString(R.string.student_update_failed),
						parentActivity);
				isProcessing = false;
			}

			@Override
			public void success(StudentService.ResCreateStudent resCreateStudent, Response response) {
				UIManager.sharedInstance().dismissProgressDialog();
				isProcessing = false;
				isModified = true;
				closeDialog();
			}
		});
		// call api
		/*ServerManager.sharedInstance().updateStudent(
				mStudent,
				new RestCallback<StudentService.ResCreateStudent>() {
					@Override
					public void success(StudentService.ResCreateStudent resCreateStudent, Response response) {
						UIManager.sharedInstance().dismissProgressDialog();
						isProcessing = false;
						isModified = true;
						closeDialog();
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						UIManager.sharedInstance().dismissProgressDialog();
						AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
								parentActivity.getString(R.string.student_update_failed),
								parentActivity);
						isProcessing = false;
					}
				}
		);*/
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent e) {
		if (EventManager.isEvent(e, EVENT_GETDATA_SUCCESS)) {
			StudentService.ResGetStudentData resGetStudentData = (StudentService.ResGetStudentData) e.object;
			mStudent = StudentManager.sharedInstance().updateStudentFromResGetData(resGetStudentData);

			// get team
			try {
				for (Team team : UserManager.sharedInstance()._currentUser._teams) {
					if (mStudent._teamId == team._id) {
						mTeam = team;
						break;
					}
				}
			} catch (Exception ex) {
				//
			}

			showData();
		} else if (EventManager.isEvent(e, EVENT_GETDATA_FAILED)) {
			AlertDialogWrapper.showErrorAlert(parentActivity.getString(R.string.profile_getstudent_failed),
					e.object == null ? parentActivity.getString(R.string.error_unknown) : (String) e.object, parentActivity);
			isProcessing = false;
		} else if (EventManager.isEvent(e, EVENT_UPDATEINFOR_SUCCESS)) {
			Logger.log(TAG, "update student success");
			if (e.object == this)
				updateStudentInformation();
			else {
				Logger.error(TAG, "this event is not for this band (object:%s)", e.object == null ? "null" : e.object);
			}
			isProcessing = false;
		} else if (EventManager.isEvent(e, EVENT_UPDATEINFOR_FAILED)) {
			Logger.error(TAG, "update student failed");
			if (e.object == this) {
				Logger.error(TAG, "onUpdateFailed");
			} else {
				Logger.error(TAG, "this event is not for this (object:%s)", e.object == null ? "null" : e.object);
			}
			isProcessing = false;
		} else if (EventManager.isEvent(e, EVENT_PROFILE_UPDATED)) {
			if (e.object == null)
				return;

			if (e.object instanceof StudentService.ResCreateStudent) {
				StudentService.ResCreateStudent resUpdateStudent = (StudentService.ResCreateStudent) e.object;
				if (resUpdateStudent != null) {
					mStudent._name = resUpdateStudent.name;
					mStudent._gender = resUpdateStudent.gender;
					mStudent.setDeviceId(resUpdateStudent.deviceId);
					mStudent._imageSrc = resUpdateStudent.imageSrc;
				}
			}
			if (e.object instanceof StudentService.ResUpdateStudent) {
				StudentService.ResUpdateStudent resUpdateStudent = (StudentService.ResUpdateStudent) e.object;
				if (resUpdateStudent != null) {
					mStudent._name = resUpdateStudent.name;
					mStudent._gender = resUpdateStudent.gender;
					mStudent.setDeviceId(resUpdateStudent.deviceId);
					mStudent._imageSrc = resUpdateStudent.imageSrc;
				}
			}

			showData();
			isProcessing = false;
		}
	}

	void enableButton(View view, boolean enabled) {
		if (view == null)
			return;

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
