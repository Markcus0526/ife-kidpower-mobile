package org.unicefkidpower.schools.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.ble.BlePeripheral;

import java.util.List;

/**
 * Created by Ruifeng Shi on 1/8/2016.
 */
public class BandCandidatesGridViewAdapter extends BaseAdapter {
	private List<BlePeripheral>			bandCandidates;
	static LayoutInflater				inflater = null;

	public BandCandidatesGridViewAdapter(
			Context context,
			List<BlePeripheral> bandCandidates
	) {
		super();

		this.bandCandidates = bandCandidates;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return bandCandidates.size();
	}

	@Override
	public Object getItem(int position) {
		return bandCandidates.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_band_code, parent, false);
			holder = new ViewHolder(convertView);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.setBandCode(bandCandidates.get(position).getCode());

		return convertView;
	}

	public void setBandCandidates(List<BlePeripheral> bandCandidates) {
		this.bandCandidates = bandCandidates;
	}

	public int getCellHeight() {
		View convertView = inflater.inflate(R.layout.item_band_code, null, false);
		TextView txtCaption = (TextView) convertView.findViewById(R.id.btn_band);
		return txtCaption.getMinimumHeight() + 12;
	}

	private class ViewHolder {
		public TextView txtCaption;

		public ViewHolder(View vwItem) {
			txtCaption = (TextView) vwItem.findViewById(R.id.btn_band);
		}
		public void setBandCode(String bandCode) {
			txtCaption.setText(bandCode);
		}
	}
}
