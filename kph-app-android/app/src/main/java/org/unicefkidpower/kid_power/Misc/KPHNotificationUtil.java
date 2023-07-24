package org.unicefkidpower.kid_power.Misc;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import org.unicefkidpower.kid_power.Application.KPHApplication;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;

/**
 * Created by Ruifeng Shi on 11/15/2015.
 */
public class KPHNotificationUtil {
	// Constants
	public static final int NOTIFICATION_DELAY_MILLIS = 1500;

	private static KPHNotificationUtil		instance;
	protected Context						contextInstance;

	private View							notificationView = null;


	private KPHNotificationUtil(Context context) {
		contextInstance = context;
	}

	public static KPHNotificationUtil initialize(Context context) {
		if (instance == null)
			instance = new KPHNotificationUtil(context);
		return instance;
	}

	public static KPHNotificationUtil sharedInstance() {
		if (instance == null)
			instance = new KPHNotificationUtil(KPHApplication.sharedInstance().getApplicationContext());
		return instance;
	}

	public void showSuccessNotification(Activity activity, int nStringResID) {
		String sMessage = activity.getString(nStringResID);
		if (sMessage != null)
			showSuccessNotification(activity, sMessage);
	}

	public void showSuccessNotification(Activity activity, String sMessage) {
		if (activity == null)
			return;

		if (notificationView == null) {
			notificationView = activity.getLayoutInflater().inflate(R.layout.layout_notification, null);
			notificationView.setBackgroundColor(
					UIManager.sharedInstance().getColor(R.color.kph_color_success_green)
			);
		} else {
			if (notificationView.getParent() != null)
				((ViewGroup) notificationView.getParent()).removeView(notificationView);
		}

		notificationView.clearAnimation();
		notificationView.setVisibility(View.GONE);
		((KPHTextView) notificationView.findViewById(R.id.txt_notification)).setText(sMessage);

		activity.getWindow().addContentView(
				notificationView,
				new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						(int) contextInstance.getResources().getDimension(R.dimen.dimen_navigation_bar_height)
				)
		);

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				notificationView.setVisibility(View.VISIBLE);
				showToastAnimation(notificationView);
			}
		}, NOTIFICATION_DELAY_MILLIS);
	}

	/**
	 * Show and hide with fade in and out animation
	 *
	 * @param view View to be animated
	 */
	public void showToastAnimation(final View view) {
		Animation fadeInAnimation = AnimationUtils.loadAnimation(contextInstance, R.anim.fade_in);

		fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				return;
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						Animation fadeOutAnimation = AnimationUtils.loadAnimation(contextInstance, R.anim.fade_out);
						fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
							@Override
							public void onAnimationStart(Animation animation) {}

							@Override
							public void onAnimationEnd(Animation animation) {
								view.setVisibility(View.GONE);
								if (view.getParent() != null)
									((ViewGroup) view.getParent()).removeView(notificationView);
							}

							@Override
							public void onAnimationRepeat(Animation animation) {}
						});
						view.startAnimation(fadeOutAnimation);
					}
				}, NOTIFICATION_DELAY_MILLIS);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				return;
			}
		});

		view.startAnimation(fadeInAnimation);
	}

}
