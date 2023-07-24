package org.unicefkidpower.schools;

import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flurry.android.FlurryAgent;

import org.unicefkidpower.schools.event.EventManager;

/**
 * Created by Ruifeng Shi on 10/7/2015.
 */
public abstract class BaseDialogFragment extends DialogFragment {
	protected View mainLayout = null;
	protected FragmentActivity parentActivity = null;
	protected LayoutInflater _inflater;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(getDialogFragmentStyle(), getDialogFragmentTheme());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// create root view
		View rootView = setContentLayout(inflater, container, contentLayout());
		_inflater = inflater;

		parentActivity = getActivity();
		setRetainInstance(true);

		if (isUseEvent()) {
			EventManager.sharedInstance().register(this);
		}

		return rootView;
	}

	@Override
	public void onDestroyView() {
		if (isUseEvent()) {
			EventManager.sharedInstance().unregister(this);
		}

		Dialog dialog = getDialog();

		// Work around bug: http://code.google.com/p/android/issues/detail?id=17423
		if ((dialog != null) && getRetainInstance())
			dialog.setDismissMessage(null);

		super.onDestroyView();
	}

	@Override
	public void onStart() {
		super.onStart();

		Dialog dialog = getDialog();
		if (dialog != null) {
			resizeDialog();
		}
	}

	@Override
	public void onStop() {
		FlurryAgent.onEndSession(getActivity());
		super.onStop();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		Dialog dialog = getDialog();
		if (dialog != null) {
			resizeDialog();
		}
	}

	public View setContentLayout(LayoutInflater inflater, ViewGroup container, int layout) {
		return inflater.inflate(layout, container, false);
	}

	public int getDialogFragmentStyle() {
		return STYLE_NO_TITLE;
	}

	public int getDialogFragmentTheme() {
		return R.style.KidPowerDialog;
	}

	public void resizeDialog() {
		//Can be override
	}

	protected abstract boolean isUseEvent();

	/**
	 * Defines the layout identifier of the fragment
	 *
	 * @return layout identifier
	 * @note This method must to be overrode to show its content
	 */
	public abstract int contentLayout();
}
