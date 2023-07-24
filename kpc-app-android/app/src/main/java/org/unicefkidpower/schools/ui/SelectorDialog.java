package org.unicefkidpower.schools.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.annotation.StyleRes;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import org.unicefkidpower.schools.CoachSettingsFragment;
import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.ResolutionSet;
import org.unicefkidpower.schools.model.Student;
import org.unicefkidpower.schools.model.Team;
import org.unicefkidpower.schools.model.TeamManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ruifeng Shi on 3/23/2017.
 */

public class SelectorDialog extends Dialog {
	public static final int CHOOSING_NONE		= -1;
	public static final int CHOOSING_GRADE		= 0;
	public static final int CHOOSING_TEAM		= 10;
	public static final int CHOOSING_KIND		= 20;
	public static final int CHOOSING_STUDENT	= 30;
	public static final int CHOOSING_POINT		= 40;

	private ArrayList itemsArray = new ArrayList();
	private int chooseType = CHOOSING_NONE;
	private OnItemSelectedListener listener = null;

	private RecyclerView recyclerView = null;
	private DataAdapter dataAdapter = new DataAdapter();

	public SelectorDialog(@NonNull Context context) {
		super(context);
	}

	public SelectorDialog(@NonNull Context context, @StyleRes int themeResId) {
		super(context, themeResId);
	}

	public void setData(ArrayList itemsArray, int chooseType, OnItemSelectedListener listener) {
		this.itemsArray = itemsArray;
		this.chooseType = chooseType;
		this.listener = listener;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_coachsetting_selector);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		initControls();
	}

	private void initControls() {
		recyclerView = (RecyclerView)findViewById(R.id.selector_items_view);
		recyclerView.hasFixedSize();
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		recyclerView.setAdapter(dataAdapter);
	}


	private class DataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		@Override
		public int getItemCount() {
			if (itemsArray == null)
				return 0;

			return itemsArray.size();
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View itemView = getLayoutInflater().inflate(R.layout.item_selector_layout, null);

			ItemViewHolder itemViewHolder = new ItemViewHolder(itemView);
			return itemViewHolder;
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
			Object item = itemsArray.get(position);
			if (item == null)
				return;

			holder.itemView.setTag(position);
			TextView destView = ((ItemViewHolder)holder).contentsTextView;

			if (chooseType == CHOOSING_GRADE)
				destView.setText(((TeamManager.TeamGrade) item).desc);
			else if (chooseType == CHOOSING_TEAM)
				destView.setText(((Team) item)._name);
			else if (chooseType == CHOOSING_STUDENT)
				destView.setText(((Student) item)._name);
			else if (chooseType == CHOOSING_KIND)
				destView.setText((String) item);
			else if (chooseType == CHOOSING_POINT)
				destView.setText(CoachSettingsFragment.getStrPowerPoint((Integer) item));
		}


		public class ItemViewHolder extends RecyclerView.ViewHolder {
			private TextView contentsTextView = null;

			public ItemViewHolder(View itemView) {
				super(itemView);

				contentsTextView = (TextView)itemView.findViewWithTag("grade");
				itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (listener != null) {
							listener.onItemSelected((Integer)v.getTag());
						}

						dismiss();
					}
				});
			}
		}

	}


	public interface OnItemSelectedListener {
		void onItemSelected(int index);
	}

}
