package org.unicefkidpower.schools;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by donal_000 on 1/8/2015.
 */
public class UIManager {
	public static		Toast					gToast				= null;
	private static		UIManager				_instance			= null;
	protected			Context					mContext			= null;

	protected			Dialog					progressDialog		= null;


	private UIManager(Context context) {
		mContext = context;
	}

	public static UIManager initialize(Context context) {
		if (_instance == null)
			_instance = new UIManager(context);
		return _instance;
	}

	public static UIManager sharedInstance() {
		return _instance;
	}

	public void showProgressDialog(Context context, String title, String message, boolean indeterminate) {
		dismissProgressDialog();

		progressDialog = new Dialog(context == null ? mContext : context, R.style.kidpower_student_progressdialog);

		progressDialog.setContentView(R.layout.dialog_progress);
		progressDialog.setCancelable(false);
		{
			TextView titleText = (TextView)progressDialog.findViewById(R.id.title_text);
			if (title == null || title.length() == 0)
				titleText.setVisibility(View.GONE);
			else
				titleText.setText(title);

			TextView messageText = (TextView)progressDialog.findViewById(R.id.content_text);
			messageText.setText(message);
		}

		progressDialog.show();
	}

	public void dismissProgressDialog() {
		if (progressDialog != null) {
			if (progressDialog.isShowing())
				progressDialog.dismiss();
			progressDialog = null;
		}
	}

	public void showToastMessage(Context context, String message) {
		if (gToast == null || gToast.getView().getWindowVisibility() != View.VISIBLE) {
			gToast = Toast.makeText((context == null) ? mContext : context, message, Toast.LENGTH_LONG);
			gToast.setGravity(Gravity.CENTER, 0, 0);
			gToast.show();
		}
	}

	public int getColor(int resId) {
		if (Build.VERSION.SDK_INT < 23) {
			return mContext.getResources().getColor(resId);
		} else {
			return mContext.getColor(resId);
		}
	}
}
