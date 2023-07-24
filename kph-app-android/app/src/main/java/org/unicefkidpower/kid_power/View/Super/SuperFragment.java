package org.unicefkidpower.kid_power.View.Super;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.unicefkidpower.kid_power.Application.KPHApplication;
import org.unicefkidpower.kid_power.Misc.Config;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.View.CustomControls.KPHBrandedDialog;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.AlertDialogHelper;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;

import java.lang.reflect.Field;

/**
 * Created by Ruifeng Shi on 1/15/2015.
 */
public abstract class SuperFragment extends Fragment {
	private final String				TAG = "SuperFragment";

	private static final Field			sChildFragmentManagerField;
	private boolean						showTabBar = true;
	private boolean						applyToChilds = false;


	static {
		Field f = null;
		try {
			f = Fragment.class.getDeclaredField("mChildFragmentManager");
			f.setAccessible(true);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}

		sChildFragmentManagerField = f;
	}


	public SuperActivity getParentActivity() {
		FragmentActivity parentActivity = getActivity();
		if (parentActivity != null && parentActivity instanceof SuperActivity)
			return (SuperActivity)parentActivity;

		return null;
	}


	public Context getSafeContext() {
		if (getContext() != null)
			return getContext();

		if (getActivity() != null)
			return getActivity();

		if (SuperActivity.topInstance() != null)
			return SuperActivity.topInstance();

		if (KPHApplication.sharedInstance() != null)
			return KPHApplication.sharedInstance().getApplicationContext();

		return null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// create root view
		View rootView = setContentLayout(inflater, container, contentLayout());

		if (Config.USE_RESOLUTIONSET) {
			// initialize resolution set
			ResolutionSet.sharedInstance().iterateChild(rootView);
		}

		// Define back button action
		View backButton = rootView.findViewById(R.id.btnBack);
		if (backButton != null) {
			backButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickedBackButton();
				}
			});
		}

		KPHUtils.sharedInstance().hideKeyboardInView(rootView);

		return rootView;
	}


	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);

		Logger.log(TAG, "onHidden Changed" + getClass().getName());

		if (!hidden)
			hideKeyboard();
	}


	@Override
	public void onResume() {
		super.onResume();
		hideKeyboard();
	}


	@Override
	public void onStart() {
		super.onStart();
		hideKeyboard();
	}


	public void hideKeyboard() {
		SuperActivity parentActivity = getParentActivity();
		if (parentActivity != null) {
			parentActivity.hideKeyboard();
		}
	}


	/**
	 * Detect if this fragment is visible to user or not.
	 * when the fragment seems visible, no matter parent fragment is hidden or not
	 * So it should be decided on the basis of hierachy tree.
	 * if one of the parent fragment is hidden this fragment should be considered as hidden
	 */
	public boolean isVisibleOnHierachy() {
		if (!SuperFragment.this.isVisible())
			return false;

		Fragment parentFragment = getParentFragment();
		while (parentFragment != null) {
			if (!parentFragment.isVisible())
				return false;

			parentFragment = parentFragment.getParentFragment();
		}

		return true;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		if (sChildFragmentManagerField != null) {
			try {
				sChildFragmentManagerField.set(this, null);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public View setContentLayout(LayoutInflater inflater, ViewGroup container, int layout) {
		return inflater.inflate(layout, container, false);
	}


	public void onClickedBackButton() {
		onClickedBackSystemButton();
	}


	public void onClickedBackSystemButton() {
		if (getParentActivity() != null)
			getParentActivity().onClickedBackSystemButton();
	}

	// layout resource : R.layout.fragment_profile ...
	public abstract int contentLayout();

	public void showProgressDialog() {
		if (getParentActivity() != null)
			getParentActivity().showProgressDialog();
	}

	public void showProgressDialog(String text) {
		if (getParentActivity() != null)
			getParentActivity().showProgressDialog(text);
	}

	public void dismissProgressDialog() {
		if (getParentActivity() != null)
			getParentActivity().dismissProgressDialog();
	}

	public void dismissProgressDialogWithDelay(int delayMillis) {
		if (getParentActivity() != null)
			getParentActivity().dismissProgressDialogWithDelay(delayMillis);
	}

	public void showErrorDialog(String text) {
		showErrorDialog(text, null);
	}

	public void showErrorDialog(String text, AlertDialogHelper.AlertListener dismissListener) {
		if (getParentActivity() != null)
			getParentActivity().showErrorDialog(text, dismissListener);
	}


	public void showBrandedDialog(String text, String defButton, String otherButton, KPHBrandedDialog.KPHBrandedDialogCallback callback) {
		if (getParentActivity() != null)
			getParentActivity().showBrandedDialog(text, defButton, otherButton, callback);
	}

	public void showBrandedDialog(String text) {
		showBrandedDialog(text, getString(R.string.ok), null, null);
	}


	public void showTabBar(boolean isShow) {
		showTabBar = isShow;
	}

	public boolean isShowTabBar() {
		return showTabBar;
	}

	public void setApplyToChilds(boolean apply) {
		applyToChilds = apply;
	}

	public boolean isApplyToChilds() {
		return applyToChilds;
	}
}

