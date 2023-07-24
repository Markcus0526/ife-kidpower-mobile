package org.unicefkidpower.kid_power.Misc;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;

import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHAlertDialog;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;

/**
 * Implementation of AlertDialogHelper
 * <p>
 * Created by Li DaYong on 10/05/15.
 */
public class AlertDialogHelper {
	private static KPHAlertDialog alertDialog = null;

	private static final String BTN_OK		= "OK";
	private static final String BTN_CANCEL	= "CANCEL";

	public static void showConfirmDialog(String title,
										 String message,
										 SuperActivity activity,
										 AlertListener listener) {
		showConfirmDialog(title, message, BTN_OK, BTN_CANCEL, activity, listener);
	}

	/**
	 * Show Dialog had "OK" and "Cancel" buttons
	 *
	 * @param title
	 * @param message
	 * @param positive
	 * @param negative
	 * @param activity
	 * @param listener
	 */
	public static void showConfirmDialog(String title,
										 String message,
										 String positive,
										 String negative,
										 SuperActivity activity,
										 final AlertListener listener)
	{
		KPHAlertDialog dialog = createDialogOnActivity(activity);

		View.OnClickListener defaultlistener = null, otherlistener = null, cancelListener = null;
		if (listener != null) {
			defaultlistener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					listener.onPositive();
				}
			};
			otherlistener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					listener.onNegative();
				}
			};
			cancelListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					listener.onCancelled();
				}
			};
		}

		dialog.setData(KPHAlertDialog.DIALOG_MODE_CONFIRM,
				title,
				message,
				positive,
				negative,
				defaultlistener,
				otherlistener,
				cancelListener);
		dialog.show();
	}


	/**
	 * Show alert dialog which has "OK" button
	 *
	 * @param title
	 * @param message
	 * @param activity
	 * @param listener
	 */
	public static void showAlertDialog(String title,
									   String message,
									   SuperActivity activity,
									   final AlertListener listener) {
		KPHAlertDialog dialog = createDialogOnActivity(activity);

		View.OnClickListener defaultlistener = null, otherlistener = null, cancelListener = null;
		if (listener != null) {
			defaultlistener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					listener.onPositive();
				}
			};
			otherlistener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					listener.onNegative();
				}
			};
			cancelListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					listener.onCancelled();
				}
			};
		}

		dialog.setData(KPHAlertDialog.DIALOG_MODE_NORMAL,
				title,
				message,
				BTN_OK,
				null,
				defaultlistener,
				otherlistener,
				cancelListener);
		dialog.show();
	}


	/**
	 * Show error alert
	 *
	 * @param errorMsg
	 * @param activity
	 */
	public static void showErrorAlert(String errorMsg, SuperActivity activity) {
		showErrorAlert(errorMsg, activity, null);
	}


	/**
	 * Show error alert
	 *
	 * @param errorMsg
	 * @param dismissListener
	 * @param activity
	 */
	public static void showErrorAlert(String errorMsg,
									  SuperActivity activity,
									  final AlertListener dismissListener) {
		KPHAlertDialog dialog = createDialogOnActivity(activity);

		View.OnClickListener okListener = null, otherListener = null, cancelListener = null;
		if (dismissListener != null) {
			okListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismissListener.onPositive();
				}
			};
			otherListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismissListener.onNegative();
				}
			};
			cancelListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismissListener.onCancelled();
				}
			};
		}

		dialog.setData(KPHAlertDialog.DIALOG_MODE_ERROR, activity.getString(R.string.error), errorMsg, BTN_OK, null, okListener, otherListener, cancelListener);
		dialog.show();
	}


	/**
	 * Show Notification alert
	 *
	 * @param title
	 * @param content
	 * @param activity
	 */
	public static void showNotificationAlert(String title, String content, final SuperActivity activity) {
		KPHAlertDialog dialog = createDialogOnActivity(activity);
		dialog.setData(KPHAlertDialog.DIALOG_MODE_NOTIFICATION, title, content, BTN_OK, null, null, null, null);
		dialog.show();
	}


	public interface AlertListener {
		void onPositive();
		void onNegative();
		void onCancelled();
	}


	private static KPHAlertDialog createDialogOnActivity(SuperActivity activity) {
		if (alertDialog != null && alertDialog.isShowing())
			alertDialog.dismiss();

		Context parentContext;

		Fragment topFragment = activity.getTopFragment();
		if (topFragment != null && topFragment instanceof DialogFragment) {
			parentContext = ((DialogFragment) topFragment).getDialog().getContext();
		} else {
			parentContext = activity;
		}

		alertDialog = new KPHAlertDialog(parentContext);

		return alertDialog;
	}

}
