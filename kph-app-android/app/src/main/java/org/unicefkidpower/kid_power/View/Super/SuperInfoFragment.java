package org.unicefkidpower.kid_power.View.Super;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.unicefkidpower.kid_power.R;

/**
 * Created by Ruifeng Shi on 3/7/2017.
 */

public abstract class SuperInfoFragment extends SuperFragment {
	private String		backTitle = "";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View contentView = super.onCreateView(inflater, container, savedInstanceState);

		TextView backTitleTextView = (TextView) contentView.findViewById(R.id.back_title_textview);
		backTitleTextView.setText(backTitle);

		return contentView;
	}

	public void setBackTitle(String backTitle) {
		this.backTitle = backTitle;
	}
}
