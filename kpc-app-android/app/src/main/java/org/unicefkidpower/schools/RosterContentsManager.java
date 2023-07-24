package org.unicefkidpower.schools;

import android.app.Activity;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.unicefkidpower.schools.ble.BlePeripheral;
import org.unicefkidpower.schools.define.CommonUtils;
import org.unicefkidpower.schools.define.config;
import org.unicefkidpower.schools.helper.DebouncedOnClickListener;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.helper.OSDate;
import org.unicefkidpower.schools.model.Student;
import org.unicefkidpower.schools.model.Team;
import org.unicefkidpower.schools.powerband.PowerBandDevice;
import org.unicefkidpower.schools.ui.KPDialog;
import org.unicefkidpower.schools.ui.UiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by donal_000 on 1/15/2015.
 */
public class RosterContentsManager {
	public static final String TAG = "RosterContentManager";
	public ArrayList<Student> _students;
	protected LinearLayout _container;
	protected Activity _parentActivity;
	protected Team _team;
	protected BlePeripheral _selectedPeripheral;
	protected PowerBandDevice _registeringDevice;
	protected ArrayList<BlePeripheral> _registeredPeripherals;
	protected ViewHolderForStudent _addingViewHolder;

	protected Handler _handlerForRestartScanning;
	private RelativeLayout layout_empty;

	public RosterContentsManager(Activity parentActivity, LinearLayout container, Team team) {
		_container = container;
		_parentActivity = parentActivity;

		_team = team;
		_students = new ArrayList<>();

		layout_empty = (RelativeLayout) _container.findViewById(R.id.layout_empty);
		layout_empty.setVisibility(View.VISIBLE);

		_registeredPeripherals = new ArrayList<>();
		_registeringDevice = null;
		_selectedPeripheral = null;

		_addingViewHolder = null;

		_handlerForRestartScanning = new Handler(parentActivity.getMainLooper());
	}

	public void onCreate() {
		_container.removeAllViews();

		// add views
		if (config.USE_LOGIC) {
			_students = new ArrayList<>();
			showStudents(_students);
		} else {
			Student student1 = new Student();
			student1._name = "Ms.Nacy";
			student1._isCoach = true;

			_students.add(student1);

			showStudents(_students);
		}
	}

	public void onGetStudents(ArrayList<Student> students) {
		_students = students;
		showStudents(_students);
	}

	public void onGroupSynced() {
		showStudents(_students);
	}

	public void onHideKeyboard() {
//        if (viewForScrolling != null)
//            viewForScrolling.setVisibility(View.GONE);
		if (layout_empty != null)
			layout_empty.setVisibility(View.GONE);
	}

	public void onShowKeyboard() {
//        if (viewForScrolling != null)
//            viewForScrolling.setVisibility(View.INVISIBLE);
		if (layout_empty != null)
			layout_empty.setVisibility(View.VISIBLE);
	}

	public void refreshStudents() {
		if (_students == null)
			return;

		showStudents(_students);
	}

	public void showStudents(ArrayList<Student> students) {

		if (students != null) {
			Collections.sort(students, new Comparator<Student>() {
				@Override
				public int compare(Student lhs, Student rhs) {
					return lhs._name.compareToIgnoreCase(rhs._name);
				}
			});
		}

		_container.removeAllViews();

		assert students != null;
		for (int index = 0; index < students.size() + 1; index += 3) {
			// create one horz layout
			LinearLayout layout = new LinearLayout(_parentActivity, null);
			layout.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			layout.setLayoutParams(params);
			layout.setWeightSum(6f);
			_container.addView(layout);

			RelativeLayout[] rl = new RelativeLayout[3];

			for (int i = 0; i < 3; i++) {
				rl[i] = new RelativeLayout(_parentActivity);
				LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
				params1.weight = 2f;
				rl[i].setLayoutParams(params1);
				layout.addView(rl[i]);
				rl[i].setGravity(Gravity.CENTER_HORIZONTAL);

				LayoutInflater inflater = _parentActivity.getLayoutInflater();
				View studentView = inflater.inflate(R.layout.layout_student, null);

				if (index + i == 0) {
					bind(null, studentView);
					rl[i].addView(studentView);
				} else if (index + i - 1 < students.size()) {
					Student student = students.get(index + i - 1);
					bind(student, studentView);
					rl[i].addView(studentView);
				}
			}
		}

		LinearLayout layout = new LinearLayout(_parentActivity, null);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.setLayoutParams(params);
		layout.setWeightSum(6f);
		_container.addView(layout);

		_container.addView(layout_empty);
	}

	protected void bind(final Student student, View parentView) {
		if (student != null) {
			final ViewHolderForStudent viewHolderForStudent = new ViewHolderForStudent(parentView);
			viewHolderForStudent.student = student;
			viewHolderForStudent.rlNormal.setVisibility(View.VISIBLE);
			viewHolderForStudent.rlNormal.setOnClickListener(new DebouncedOnClickListener() {
				@Override
				public void onDebouncedClick(View v) {
					if (viewHolderForStudent.student == null) {
						return;
					}
					Logger.log(TAG, "Student selected - Name: %s", viewHolderForStudent.student._name);

					//if (viewHolderForStudent.student.isUnlinkedBand()) {
					//    StudentEditFragment studentEditFragment = new StudentEditFragment();
					//    studentEditFragment.setStudentTeam(viewHolderForStudent.student, _team);
					//    ((MainActivity) parentActivity).showDialogFragment(studentEditFragment);
					//    return;
					//} else
					{
						StudentFragment studentFragment = new StudentFragment();
						studentFragment.setData(viewHolderForStudent.student);
						((MainActivity) _parentActivity).showDialogFragment(
								studentFragment
						);
					}
				}
			});
			viewHolderForStudent.rlAddStudent.setVisibility(View.GONE);
			String name = student._name;
			viewHolderForStudent.textName.setText(name);

			viewHolderForStudent.ivAvatar.setImageDrawable(
					UiUtils.getInstance().getAvatarDrawable(student._imageSrc)
			);

			// syncstatus
			int nSyncIcon;
			int nSyncStatusColor;
			String sSyncStatus;

			if (student.isUnlinkedBand()) {
				nSyncStatusColor = R.color.kidpower_light_grey;
				nSyncIcon = R.drawable.icon_link;
				sSyncStatus = _parentActivity.getString(R.string.need_to_link);
			} else {
				if (student._lastSyncDateDetail == null) {
					sSyncStatus = _parentActivity.getString(R.string.has_not_synced);
					nSyncStatusColor = R.color.kidpower_light_red;
					nSyncIcon = R.drawable.red_warning_icon;
				} else {
					int days = OSDate.daysBetweenDates(student._lastSyncDateDetail, new OSDate());

					if (days == 1) {
						sSyncStatus = _parentActivity.getString(R.string.last_synced) + " " + _parentActivity.getString(R.string.app_yesterday);
						nSyncStatusColor = R.color.kidpower_tangerine;
						nSyncIcon = R.drawable.tangerine_information_icon;
					} else if (days > 1) {
						sSyncStatus = _parentActivity.getString(R.string.last_synced) + " " +
								String.valueOf(days) + " " + _parentActivity.getString(R.string.days_ago);
						if (days <= 7) {
							nSyncStatusColor = R.color.kidpower_tangerine;
							nSyncIcon = R.drawable.tangerine_information_icon;
						} else {
							nSyncStatusColor = R.color.kidpower_light_red;
							nSyncIcon = R.drawable.red_warning_icon;
						}
					} else {
						sSyncStatus = _parentActivity.getString(R.string.last_synced) + " " +
								_parentActivity.getString(R.string.today);
						nSyncStatusColor = R.color.kidpower_light_grey;
						nSyncIcon = R.drawable.grey_checkmark_icon;
					}
				}
			}
			viewHolderForStudent.textSyncStatus.setText(sSyncStatus);
			viewHolderForStudent.textSyncStatus.setTextColor(CommonUtils.getColorFromRes(_parentActivity.getResources(), nSyncStatusColor));
			viewHolderForStudent.ivSyncStatus.setImageResource(nSyncIcon);

		} else {
			// add a student
			final ViewHolderForStudent viewHolderForStudent = new ViewHolderForStudent(parentView);
			viewHolderForStudent.rlNormal.setVisibility(View.GONE);
			viewHolderForStudent.rlAddStudent.setVisibility(View.VISIBLE);

			viewHolderForStudent.rlAddStudent.setOnClickListener(new DebouncedOnClickListener() {
				@Override
				public void onDebouncedClick(View v) {
					onAddStudentClicked();
				}
			});

			_addingViewHolder = viewHolderForStudent;
		}
	}

	protected void onAddStudentClicked() {
		Logger.log(TAG, "Add Student button clicked");

		KPDialog newKidConfirmationDialog = new KPDialog(_parentActivity, R.string.new_kid, R.string.new_kid_note);
		newKidConfirmationDialog.setOnDialogItemClickListener(
				new KPDialog.OnDialogItemClickListener() {
					@Override
					public void onOKButtonClicked() {
						onAddNewStudent();
					}

					@Override
					public void onCancelButtonClicked() {

					}
				}
		);
		newKidConfirmationDialog.show();
	}

	public interface UpdatedStudentListener {
		void onUpdateStudent(Student student);
	}

	protected void onAddNewStudent() {
		StudentCreateFragment studentCreateFragment = new StudentCreateFragment();
		studentCreateFragment.setTeam(_team);
		((MainActivity) _parentActivity).showDialogFragment(studentCreateFragment);
	}

	private static class ViewHolderForStudent {
		public View parentView;

		// normal
		public View rlNormal;
		public ImageView ivAvatar;
		public TextView textName;
		public ImageView ivSyncStatus;
		public TextView textSyncStatus;

		// add a student
		public View rlAddStudent;

		// adding a student
		public Student student;

		public ViewHolderForStudent(View parentView) {
			this.parentView = parentView;

			rlNormal = parentView.findViewById(R.id.rlNormal);
			ivAvatar = (ImageView) parentView.findViewById(R.id.ivAvatar);
			textName = (TextView) parentView.findViewById(R.id.textName);
			ivSyncStatus = (ImageView) parentView.findViewById(R.id.iv_sync_status);
			textSyncStatus = (TextView) parentView.findViewById(R.id.txt_sync_status);

			rlAddStudent = parentView.findViewById(R.id.rlAddStudent);
		}
	}

	static class ViewHolder {
		final TextView codeTextView;
		final View parentView;

		ViewHolder(View view) {
			codeTextView = (TextView) view.findViewWithTag("code");
			parentView = view.findViewWithTag("parentView");
		}
	}
}
