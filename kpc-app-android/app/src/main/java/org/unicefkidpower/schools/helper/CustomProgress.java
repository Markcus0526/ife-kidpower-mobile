package org.unicefkidpower.schools.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.unicefkidpower.schools.R;

/**
 * Created by donal_000 on 2/3/2015.
 */
public class CustomProgress {
	protected Context			context;
	protected ViewGroup			parentView;
	protected View				rootView;
	protected ImageView			ivProgressBack;
	protected ImageView			ivProgress;

	protected int				min;
	protected int				max;
	protected int				current;

	protected int				width;


	public CustomProgress(Context context, LayoutInflater layoutInflater, ViewGroup parentView, int min, int max) {
		this.context = context;
		this.parentView = parentView;
		this.min = min;
		this.max = max;
		this.current = min;

		// get width
		ViewGroup.LayoutParams lp = parentView.getLayoutParams();
		this.width = lp.width;

		this.rootView = layoutInflater.inflate(R.layout.layout_progress, null);
		parentView.addView(this.rootView);

		this.ivProgressBack = (ImageView) this.rootView.findViewById(R.id.ivProgressBack);
		this.ivProgress = (ImageView) this.rootView.findViewById(R.id.ivProgress);

		_setProgress(this.min);
	}

	protected void _setProgress(int current) {
		this.current = current;
		ViewGroup.LayoutParams lp = this.ivProgress.getLayoutParams();
		if (this.current == this.min)
			lp.width = 0;
		else if (this.current == this.max)
			lp.width = this.width - 2;
		else {
			if ((this.max - this.min) != 0)
				lp.width = (int) (this.width * (float) (this.current - this.min) / (float) (this.max - this.min));
			else
				lp.width = 0;
		}
		if (lp.width < 0)
			lp.width = 0;

		this.ivProgress.setLayoutParams(lp);
	}

	public int getProgress() {
		return this.current;
	}

	public void setProgress(int value) {
		int _value = value;
		if (value < this.min)
			_value = this.min;
		else if (value > this.max)
			_value = this.max;

		_setProgress(_value);
	}

	public void setRange(int min, int max) {
		this.max = max;
		this.min = min;
		if (this.current < this.min)
			this.current = min;
		else if (this.current > this.max)
			this.current = max;
		this.setProgress(this.current);
	}

	public int getMin() {
		return this.min;
	}

	public int getMax() {
		return this.max;
	}
}
