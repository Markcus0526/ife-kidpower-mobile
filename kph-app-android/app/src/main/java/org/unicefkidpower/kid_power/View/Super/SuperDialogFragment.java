package org.unicefkidpower.kid_power.View.Super;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.unicefkidpower.kid_power.Misc.Config;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.View.CustomControls.KPHBrandedDialog;

/**
 * Created by Ruifeng Shi on 10/7/2015.
 */
public abstract class SuperDialogFragment extends DialogFragment {
	private final String						TAG = "SuperDialogFragment";
	protected View								mainLayout = null;
	private SuperDialogDismissListener			dismissListener = null;
	private boolean								dialogAlreadyStarted = false;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(getDialogFragmentStyle(), getDialogFragmentTheme());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// create root view
		View rootView = setContentLayout(inflater, container, contentLayout());

		setRetainInstance(true);

		mainLayout = rootView;

		if (Config.USE_RESOLUTIONSET) {
			// initialize resolution set
			ResolutionSet.sharedInstance().iterateChild(mainLayout);
		}

		return rootView;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Dialog dialog = getDialog();
		dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN &&
						keyCode == KeyEvent.KEYCODE_BACK) {
					getParentActivity().onClickedBackSystemButton();
					return true;
				}

				return false;
			}
		});
	}


	@Override
	public void onStart() {
		super.onStart();

		if (!dialogAlreadyStarted) {
			startAction();
			dialogAlreadyStarted = true;
		}
	}

	/**
	 * Method to start work such as
	 */
	public abstract void startAction();


	protected void hideKeyboard() {
		KPHUtils.sharedInstance().hideKeyboardInView(mainLayout);
	}


	@Override
	public void onDestroyView() {
		Dialog dialog = getDialog();

		// Work around bug: http://code.google.com/p/android/issues/detail?id=17423
		if ((dialog != null) && getRetainInstance())
			dialog.setDismissMessage(null);

		super.onDestroyView();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		dismissProgressDialog();

		super.onDismiss(dialog);

		Logger.log(TAG, "onDismiss");
		if (dismissListener != null) {
			dismissListener.onDismiss();
		}
	}


	public View setContentLayout(LayoutInflater inflater, ViewGroup container, int layout) {
		return inflater.inflate(layout, container, false);
	}

	public int getDialogFragmentStyle() {
		return STYLE_NO_TITLE;
	}

	public int getDialogFragmentTheme() {
		return android.R.style.Theme_Holo_Dialog;
	}

	/**
	 * Defines the layout identifier of the fragment
	 *
	 * @return layout identifier
	 * @note This method must to be overrode to show its content
	 */
	public abstract int contentLayout();


	public Context getSafeContext() {
		if (getContext() != null)
			return getContext();

		if (getActivity() != null)
			return getActivity();

		if (SuperActivity.topInstance() != null)
			return SuperActivity.topInstance();

		return null;
	}

	public SuperActivity getParentActivity() {
		FragmentActivity parentActivity = getActivity();
		if (parentActivity != null && parentActivity instanceof SuperActivity)
			return (SuperActivity)parentActivity;

		return null;
	}

	public void showProgressDialog() {
		if (getParentActivity() != null)
			getParentActivity().showProgressDialog();
	}

	public void showProgressDialog(String text) {
		if (getParentActivity() != null)
			getParentActivity().showProgressDialog(text);
	}

	public void showProgressDialog(String title, String text) {
		if (getParentActivity() != null)
			getParentActivity().showProgressDialog(title, text);
	}


	public void dismissProgressDialog() {
		if (getParentActivity() != null)
			getParentActivity().dismissProgressDialog();
	}

	public void showErrorDialog(String text) {
		if (getParentActivity() != null)
			getParentActivity().showErrorDialog(text);
	}

	public void showBrandedDialog(String text, String defButton, String otherButton, KPHBrandedDialog.KPHBrandedDialogCallback callback) {
		if (getParentActivity() != null)
			getParentActivity().showBrandedDialog(text, defButton, otherButton, callback);
	}

	public void dismiss() {
		super.dismiss();
	}

	public void setDismissListener(SuperDialogDismissListener listener) {
		dismissListener = listener;
	}

	public void dismiss(SuperDialogDismissListener listener) {
		dismissListener = listener;
		super.dismiss();
	}

	public interface SuperDialogDismissListener {
		void onDismiss();
	}

}
