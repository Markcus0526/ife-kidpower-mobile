package org.unicefkidpower.schools.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.adapter.BandCandidatesGridViewAdapter;
import org.unicefkidpower.schools.ble.BlePeripheral;
import org.unicefkidpower.schools.helper.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ruifeng Shi on 1/8/2016.
 */
public class BandCandidatesView extends RelativeLayout {
	public interface OnItemSelectedListener {
		void OnItemSelected(int index, Object object);
	}

	private Context mContext;
	private List<BlePeripheral> bandCandidates = new ArrayList<>();
	private List<BlePeripheral> bandCandidatesToShow = new ArrayList<>();
	private String keyword = "";

	BandCandidatesGridViewAdapter bandCandidatesGridViewAdapter;
	protected OnItemSelectedListener listener;

	//UI Elements
	TextView txtNumberOfBandsFound;
	GridView gvBandCandidates;

	public BandCandidatesView(Context context) {
		super(context);

		this.mContext = context;
		init();
	}

	public BandCandidatesView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.mContext = context;
		init();
	}

	public BandCandidatesView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		this.mContext = context;
		init();
	}

	private void init() {
		LayoutInflater.from(mContext).inflate(R.layout.layout_band_candidates, this);

		txtNumberOfBandsFound = (TextView) findViewById(R.id.txt_number_of_bands_found);
		gvBandCandidates = (GridView) findViewById(R.id.gv_band_candidates);
		if (bandCandidatesToShow != null) {
			bandCandidatesGridViewAdapter = new BandCandidatesGridViewAdapter(mContext, bandCandidatesToShow);
			gvBandCandidates.setAdapter(bandCandidatesGridViewAdapter);
		}
		gvBandCandidates.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Logger.log("CandidatesView", "% position item clicked", position);
				if (BandCandidatesView.this.listener != null) {
					Object object = bandCandidatesGridViewAdapter.getItem(position);
					BandCandidatesView.this.listener.OnItemSelected(position, object);
				}
			}
		});
		gvBandCandidates.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				v.getParent().requestDisallowInterceptTouchEvent(true);
				return false;
			}
		});
	}

	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		this.listener = listener;
	}


	public void setFilterKeyword(String keyword) {
		this.keyword = keyword;

		if (bandCandidates == null || bandCandidatesToShow == null || bandCandidatesGridViewAdapter == null)
			return;

		bandCandidatesToShow.clear();

		if (bandCandidates == null)
			return;

		if (keyword.equals("")) {
			if (bandCandidatesToShow.size() == bandCandidates.size())
				return;

			bandCandidatesToShow.addAll(bandCandidates);
			bandCandidatesGridViewAdapter.notifyDataSetChanged();
		} else {
			for (int i = 0; i < bandCandidates.size(); i++) {
				BlePeripheral bandItem = bandCandidates.get(i);
				if (bandItem.getCode().toLowerCase().contains(keyword.toLowerCase())) {
					bandCandidatesToShow.add(bandItem);
				}
			}

			bandCandidatesGridViewAdapter.notifyDataSetChanged();
		}

		updateBandCountsText();
	}


	private void updateBandCountsText() {
		String sNumberOfBandFound;

		if (bandCandidatesToShow.size() == 1)
			sNumberOfBandFound = mContext.getString(R.string.one_band_found);
		else
			sNumberOfBandFound = String.format(
					mContext.getString(R.string.number_of_band_found),
					bandCandidatesToShow.size()
			);

		txtNumberOfBandsFound.setText(sNumberOfBandFound);
	}


	public void setBandCandidates(List<BlePeripheral> _bandCandidates) {
		bandCandidates = _bandCandidates;

		if (bandCandidates == null || bandCandidatesGridViewAdapter == null) {
			return;
		}

		setFilterKeyword(this.keyword);

		bandCandidatesGridViewAdapter.setBandCandidates(bandCandidatesToShow);
		bandCandidatesGridViewAdapter.notifyDataSetChanged();

		updateBandCountsText();
	}


	public int getEstimatedHeight(int numberOfBands) {
		if (numberOfBands == 0) {
			return 0;
		}

		ImageView ivTriangle = (ImageView) findViewById(R.id.iv_triangle);

		RelativeLayout.LayoutParams lpNumberOfBandsFound = (LayoutParams) txtNumberOfBandsFound.getLayoutParams();
		int nEstimatedHeight =
				ivTriangle.getLayoutParams().height * 2 +
						bandCandidatesGridViewAdapter.getCellHeight() * (numberOfBands / 4 + 1);

		return nEstimatedHeight;
	}
}
