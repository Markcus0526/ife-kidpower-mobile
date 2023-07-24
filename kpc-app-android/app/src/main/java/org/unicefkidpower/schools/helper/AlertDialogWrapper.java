package org.unicefkidpower.schools.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import org.unicefkidpower.schools.KidpowerApplication;

/**
 * Created by Zhongyu Zhang on 20/2/2016
 * Copyright 2015 ~ 2019 Bluemastro
 * zhong.yu@outlook.com
 */
public class AlertDialogWrapper {
	private static AlertDialog.Builder getNormalStyleDialog(final Context activityContext) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
		return builder;
	}

	public static void showErrorAlert(String errorTitle, String errorMsg, final Context activityContext) {
		if (KidpowerApplication.currentDialog != null && KidpowerApplication.currentDialog.isShowing()) {
			return;
		}

		KidpowerApplication.currentDialog = getNormalStyleDialog(activityContext)
				.setTitle(errorTitle)
				.setMessage(errorMsg)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						AlertDialogWrapper.dismissErrorAlert();
						dialog.cancel();
					}
				}).show();
	}

	private static void dismissErrorAlert() {
		if (KidpowerApplication.currentDialog == null)
			return;
		if (KidpowerApplication.currentDialog.isShowing()) {
			KidpowerApplication.currentDialog.dismiss();
		}
		KidpowerApplication.currentDialog = null;
	}
}
