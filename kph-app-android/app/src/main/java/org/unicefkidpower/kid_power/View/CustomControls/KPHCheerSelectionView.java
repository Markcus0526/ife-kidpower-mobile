package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Adapters.CheerIconListAdapter;

/**
 * Created by Ruifeng Shi on 1/28/2016.
 */
public class KPHCheerSelectionView extends LinearLayout {
	private Context								contextInstance;
	private CheerIconListAdapter				cheerIconListAdapter;

	private PopupWindow							cheerSelectionWindow;

	// UI Controls
	private KPHTextView							chooseCheerTextView;
	private GridView							cheerIconsGridView;


	public KPHCheerSelectionView(Context context) {
		super(context);

		this.contextInstance = context;
		initialize();
	}

	public KPHCheerSelectionView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.contextInstance = context;
		initialize();
	}

	public KPHCheerSelectionView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		this.contextInstance = context;
		initialize();
	}

	protected void initialize() {
		View vwContent = LayoutInflater.from(contextInstance).inflate(
				R.layout.layout_cheer_selection, this
		);
		cheerIconsGridView = (GridView) vwContent.findViewById(R.id.gv_cheer_icon_list);
		chooseCheerTextView = (KPHTextView) vwContent.findViewById(
				R.id.txt_choose_cheer
		);
	}

	public void setCheerIconListAdapter(CheerIconListAdapter adapter) {
		cheerIconsGridView.setAdapter(adapter);
		this.cheerIconListAdapter = adapter;
	}

	public void setCheerIconListItemClickListener(final AdapterView.OnItemClickListener onItemClickListener) {
		cheerIconsGridView.setOnItemClickListener(
				new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						onItemClickListener.onItemClick(parent, view, position, id);
						dismiss();
					}
				}
		);
	}

	public int getEstimatedHeight() {
		int estimatedHeight = 0;

		if (chooseCheerTextView != null) {
			estimatedHeight += contextInstance.getResources().getDimensionPixelSize(
					R.dimen.dimen_navigation_bar_height
			);
			estimatedHeight += contextInstance.getResources().getDimensionPixelSize(
					R.dimen.dimen_margin_10
			);
			estimatedHeight += contextInstance.getResources().getDimensionPixelSize(
					R.dimen.dimen_margin_1
			);
		}

		if (cheerIconListAdapter != null) {
			estimatedHeight += cheerIconListAdapter.getEstimatedHeight();
		}

		return estimatedHeight;
	}

	public void show(View aboveView) {
		int[] nCoordinates = new int[2];
		aboveView.getLocationOnScreen(nCoordinates);

		int nWidth = ResolutionSet.getScreenSize(contextInstance, false).x -
				getResources().getDimensionPixelSize(R.dimen.dimen_margin_10) * 2;
		int nHeight = getEstimatedHeight();
		int nMaximumAvailableHeight = ResolutionSet.getScreenSize(contextInstance, false).y -
				getResources().getDimensionPixelSize(R.dimen.dimen_button_general_height) -
				nCoordinates[1] -
				getResources().getDimensionPixelSize(R.dimen.activity_tab_bar_height) -
				getResources().getDimensionPixelSize(R.dimen.dimen_margin_10);

		if (nHeight >= nMaximumAvailableHeight) {
			nHeight = nMaximumAvailableHeight;
		}

		cheerSelectionWindow = new PopupWindow(contextInstance);
		cheerSelectionWindow.setContentView(this);
		cheerSelectionWindow.setWidth(nWidth);
		cheerSelectionWindow.setHeight(nHeight);
		cheerSelectionWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		cheerSelectionWindow.setFocusable(true);
		cheerSelectionWindow.setAnimationStyle(R.style.kph_style_popup_for_cheer_popup);
		cheerSelectionWindow.showAtLocation(
				this,
				Gravity.NO_GRAVITY,
				getResources().getDimensionPixelSize(R.dimen.dimen_margin_10),
				nCoordinates[1] + getResources().getDimensionPixelSize(R.dimen.dimen_button_general_height)
		);
	}

	public void dismiss() {
		if (cheerSelectionWindow != null && cheerSelectionWindow.isShowing())
			cheerSelectionWindow.dismiss();
	}

}
