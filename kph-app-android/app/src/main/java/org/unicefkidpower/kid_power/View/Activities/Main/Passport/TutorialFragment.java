package org.unicefkidpower.kid_power.View.Activities.Main.Passport;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;

/**
 * Created by Dayong Li on 5/11/2016.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class TutorialFragment extends Fragment {
	private static final int[] id_indicator = {
			R.id.indicator1,
			R.id.indicator2,
			R.id.indicator3,
			R.id.indicator4,
			R.id.indicator5
	};

	private int					image			= 0;
	private int					description		= 0;
	private int					caption			= 0;

	private int					pageNumber		= 0;

	private View 				contentView		= null;
	private ImageView			ivTutorial		= null;
	private KPHTextView			tvDescription	= null;
	private KPHButton			button			= null;

	private OnNextListener		onNextListener	= null;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (contentView == null) {
			contentView = inflater.inflate(R.layout.fragment_mission_tutorial, null);

			ivTutorial = (ImageView) contentView.findViewById(R.id.ivTutorialImage);
			tvDescription = (KPHTextView) contentView.findViewById(R.id.tvTutorialText);
			button = (KPHButton) contentView.findViewById(R.id.btnTutorialButton);

			try {
				ivTutorial.setImageResource(image);
				tvDescription.setText(description);
				button.setText(caption);

				((ImageView) contentView.findViewById(id_indicator[pageNumber]))
						.setImageResource(R.drawable.ic_dot_on);
			} catch (Exception e) {
			}

			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (onNextListener != null) {
						onNextListener.onNext(pageNumber);
					}
				}
			});
		}

		return contentView;
	}

	public void setPageNumber(int index) {
		pageNumber = index;
	}

	public void setContents(int image_resource, int description, int caption) {
		this.image = image_resource;
		this.description = description;
		this.caption = caption;
	}

	public void setOnNextListener(OnNextListener listener) {
		onNextListener = listener;
	}

	public interface OnNextListener {
		public void onNext(int page);
	}
}
