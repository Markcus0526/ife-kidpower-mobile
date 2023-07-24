package org.unicefkidpower.schools.sync;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.ResolutionSet;
import org.unicefkidpower.schools.helper.DebouncedOnClickListener;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.StudentService;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by donal_000 on 1/19/2015.
 */
public class UnlinkBandDialog extends Dialog {
	private final int RETRY_COUNT_LIMIT = 3;
	static String TAG = "DelinkBand";
	protected Activity parentActivity;
	protected DialogListener listener;

	RotateAnimation rotateAnimation;

	int studentId;
	int retryCount = RETRY_COUNT_LIMIT;

	public UnlinkBandDialog(int studentId, Activity parentActivity, DialogListener listener) {
		super(parentActivity);

		this.parentActivity = parentActivity;
		this.listener = listener;
		this.studentId = studentId;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_unlink_band);

		ResolutionSet._instance.iterateChild(findViewById(R.id.layout_parent));
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setCanceledOnTouchOutside(false);
		initControl();
	}

	private void initControl() {
		findViewById(R.id.btnCancel).setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				dismiss();
			}
		});

		findViewById(R.id.btnOK).setOnClickListener(new DebouncedOnClickListener() {
			@Override
			public void onDebouncedClick(View v) {
				onClickedUnlinkBand();
			}
		});

		rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotateAnimation.setInterpolator(new LinearInterpolator());
		rotateAnimation.setDuration(2000);
		rotateAnimation.setRepeatCount(Animation.INFINITE);
		rotateAnimation.setFillEnabled(true);
		rotateAnimation.setFillAfter(true);
	}

	private void onClickedUnlinkBand() {
		Logger.log(TAG, "Unlink Button clicked");

		((TextView) findViewById(R.id.tvTitle)).setText(parentActivity.getString(R.string.sync_unlinking_band));
		findViewById(R.id.fvSyncMark).setAnimation(rotateAnimation);
		findViewById(R.id.llButtons).setVisibility(View.GONE);

		retryCount = RETRY_COUNT_LIMIT;

		unlinkBand();
	}

	void unlinkBand() {
		ServerManager.sharedInstance().replacePowerBand(studentId, "", new RestCallback<StudentService.ResUpdateStudent>() {
			@Override
			public void success(StudentService.ResUpdateStudent resUpdateStudent, Response response) {
				Logger.log(TAG, "Unlink success!");

				findViewById(R.id.fvSyncMark).clearAnimation();
				dismiss();

				listener.onSuccess();
			}

			@Override
			public void failure(RetrofitError retrofitError, String message) {
				if (retryCount < 0) {
					findViewById(R.id.fvSyncMark).clearAnimation();
					dismiss();
					listener.onFailed(message);
					Logger.error(TAG, "Unlink failed!");
				} else {
					retryCount--;
					unlinkBand();
				}
			}
		});
	}

	public interface DialogListener {
		void onSuccess();

		void onFailed(String message);
	}
}
