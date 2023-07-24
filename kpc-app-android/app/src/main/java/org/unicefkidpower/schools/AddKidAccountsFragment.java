package org.unicefkidpower.schools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.flurry.android.FlurryAgent;

import org.unicefkidpower.schools.adapter.KidsListViewAdapter;
import org.unicefkidpower.schools.ui.KPViewPager;

import java.util.ArrayList;

public class AddKidAccountsFragment extends SuperFragment implements
		View.OnClickListener,
		KidsListViewAdapter.OnStudentItemEventListener
{
	public final static String TAG = AddKidAccountsFragment.class.getSimpleName();
	private KPViewPager mViewPager;
	private EditText editStudentName;
	private ListView lvStudent;
	private KidsListViewAdapter kidsListViewAdapter;

	private ArrayList<String> studentNameArray;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		FlurryAgent.onStartSession(getContext(), TAG);

		mViewPager = (KPViewPager) getActivity().findViewById(R.id.onboarding_pager);

		rootView.findViewById(R.id.btn_back).setOnClickListener(this);
		rootView.findViewById(R.id.btn_done_adding_kids).setOnClickListener(this);
		rootView.findViewById(R.id.btn_add_student).setOnClickListener(this);
		lvStudent = (ListView) rootView.findViewById(R.id.lv_students);
		editStudentName = (EditText) rootView.findViewById(R.id.edit_student_name);

		studentNameArray = new ArrayList<>();
		kidsListViewAdapter = new KidsListViewAdapter(_parentActivity, studentNameArray);
		kidsListViewAdapter.setOnStudentItemEventListener(this);
		lvStudent.setAdapter(kidsListViewAdapter);

		return rootView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_add_kid_accounts;
	}

	@Override
	protected boolean isUseEvent() {
		return false;
	}

	private void onDoneAddingKidsButtonClicked() {
		((BaseActivity) _parentActivity).pushNewActivityAnimated(MainActivity.class);
		_parentActivity.finish();
	}

	private void onAddStudentButtonClicked() {
		String studentName = editStudentName.getText().toString();

		studentNameArray.add(studentName);
		kidsListViewAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_skip:
				mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
				break;

			case R.id.btn_done_adding_kids:
				onDoneAddingKidsButtonClicked();
				break;

			case R.id.btn_add_student:
				onAddStudentButtonClicked();
				break;
		}
	}

	@Override
	public void onStudentRemoveButtonClicked(String student) {
		studentNameArray.remove(student);
		kidsListViewAdapter.notifyDataSetChanged();
	}
}
