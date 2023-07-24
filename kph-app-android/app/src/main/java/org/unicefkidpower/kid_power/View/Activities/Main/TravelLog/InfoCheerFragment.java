package org.unicefkidpower.kid_power.View.Activities.Main.TravelLog;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperInfoFragment;


/**
 * Created by Dayong Li on 11/11/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class InfoCheerFragment extends SuperInfoFragment {
	private View				contentView			= null;
	private ImageView			ivImage				= null;
	private KPHTextView			tvName				= null;
	private KPHTextView			tvDescription		= null;

	////////////////////////////////////////////////////////////////////////////////////////////////
	private Drawable			image				= null;
	private String				name				= "";
	private String				desctiption			= "";
	private boolean				isUnlocked			= false;
	private boolean				isStamp				= false;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		ivImage = (ImageView) contentView.findViewById(R.id.ivAvatar);
		tvName = (KPHTextView) contentView.findViewById(R.id.tvName);
		tvDescription = (KPHTextView) contentView.findViewById(R.id.tvDescription);

		if (image != null) {
			ivImage.setImageDrawable(image);
		}

		if (isStamp) {
			tvName.setText(name);
			tvDescription.setText(desctiption);
		} else {
			if (isUnlocked) {
				tvName.setText(R.string.you_unlocked_a_new_cheer);
				tvDescription.setText(R.string.unlocked_cheers_description_1);
			} else {
				String text = name != null ? name : "";
				if (text.length() > 0)
					text += " ";
				text += getString(R.string.cheered_you);

				tvName.setText(text);
				tvDescription.setText("");
			}
		}

		return contentView;
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_info_cheer;
	}


	public void setInformation(Drawable image, String name, String description, boolean isStamp, boolean isUnlocked) {
		this.name = name;
		this.desctiption = description;
		this.image = image;
		this.isStamp = isStamp;
		this.isUnlocked = isUnlocked;
	}

}
