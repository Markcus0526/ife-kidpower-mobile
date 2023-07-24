package org.unicefkidpower.schools.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;

import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.ui.KPTextView;

import java.util.ArrayList;

/**
 * Created by Ruifeng Shi on 9/26/2016.
 */
public class KidsListViewAdapter extends BaseAdapter {
	private Context							context;
	private ArrayList<String>				studentNameArray;
	private OnStudentItemEventListener		onStudentItemEventListener;


	public KidsListViewAdapter(Context context, ArrayList<String> studentNameArray) {
		this.context = context;
		this.studentNameArray = studentNameArray;
	}

	@Override
	public int getCount() {
		return studentNameArray.size();
	}

	@Override
	public String getItem(int position) {
		return studentNameArray.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_student, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.setStudentName(getItem(position));

		return convertView;
	}

	public void setOnStudentItemEventListener(OnStudentItemEventListener onStudentItemEventListener) {
		this.onStudentItemEventListener = onStudentItemEventListener;
	}

	public class ViewHolder {
		public KPTextView txtStudentName;
		public ImageButton btnRemove;

		public ViewHolder(View view) {
			txtStudentName = (KPTextView) view.findViewById(R.id.txt_student_name);
			btnRemove = (ImageButton) view.findViewById(R.id.btn_remove);
		}

		public void setStudentName(final String studentName) {
			txtStudentName.setText(studentName);
			btnRemove.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (onStudentItemEventListener != null)
						onStudentItemEventListener.onStudentRemoveButtonClicked(studentName);
				}
			});
		}
	}

	public interface OnStudentItemEventListener {
		void onStudentRemoveButtonClicked(String student);
	}
}
