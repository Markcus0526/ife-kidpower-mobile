package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.Super.SuperNormalSizeDialogFragment;
import org.unicefkidpower.kid_power.R;

/**
 * Created by Ruifeng Shi on 9/28/2015.
 */
public class AnotherBandFragment extends SuperNormalSizeDialogFragment {
	private View				contentView				= null;
	private ImageButton			btnClose				= null;
	private KPHButton			btnParent				= null;
	private KPHButton			btnChild				= null;

	public boolean				isShowMeButton			= false;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(getDialogFragmentStyle(), R.style.KidPowerDialogStyle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		btnClose = (ImageButton) contentView.findViewById(R.id.btnClose);
		btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		btnParent = (KPHButton) contentView.findViewById(R.id.btnParent);
		btnParent.setText(getSafeContext().getString(R.string.band_is_parent, "parent"));
		btnParent.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onParentBandClicked();
			}
		});

		if (isShowMeButton)
			btnParent.setVisibility(View.VISIBLE);
		else
			btnParent.setVisibility(View.GONE);

		btnChild = (KPHButton) contentView.findViewById(R.id.btnChild);
		btnChild.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onChildBandClicked();
			}
		});

		return contentView;
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_profile_another_band;
	}

	private void onParentBandClicked() {
		dismiss();

		// Go to OnboardingActivity with completing parent profile flag
		Bundle bundle = new Bundle();
		bundle.putBoolean(OnboardingActivity.EXTRA_COMPLETING_PARENT_PROFILE, true);
		bundle.putBoolean(OnboardingActivity.EXTRA_SHOW_BACK_BUTTON, false);

		getParentActivity().pushNewActivityAnimated(OnboardingActivity.class, bundle);
		getParentActivity().popOverCurActivityAnimated();
	}

	private void onChildBandClicked() {
		// Go to OnboardingActivity with parent user data
		dismiss();

		Bundle bundle = new Bundle();
		bundle.putInt(OnboardingActivity.EXTRA_FROM_ACTIVITY, OnboardingActivity.FROM_WELCOME_ACTIVITY);
		bundle.putBoolean(OnboardingActivity.EXTRA_SHOW_BACK_BUTTON, false);

		getParentActivity().pushNewActivityAnimated(OnboardingActivity.class, bundle);
		getParentActivity().popOverCurActivityAnimated();
	}

	@Override
	public void startAction() {
		// No action. do nothing
	}

}
