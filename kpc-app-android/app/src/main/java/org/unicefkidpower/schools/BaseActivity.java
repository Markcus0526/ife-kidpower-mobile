package org.unicefkidpower.schools;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.flurry.android.FlurryAgent;
import com.swrve.sdk.SwrveSDK;

import org.unicefkidpower.schools.context.UserContext;
import org.unicefkidpower.schools.define.CommonUtils;
import org.unicefkidpower.schools.define.KPConstants;
import org.unicefkidpower.schools.define.config;
import org.unicefkidpower.schools.event.EventManager;

/**
 * Created by donal_000 on 1/6/2015.
 */

public abstract class BaseActivity extends FragmentActivity {
	protected View mainLayout = null;
	protected boolean bInitialized = false;
	protected int curOrientation;
	protected String _fromActivity = null;

	public int indexOfCurrentFragment = -1;
	protected SuperFragment currentFragment = null;

	public boolean isInitialized() {
		return bInitialized;
	}
	private static BaseActivity topInstance = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (UserContext.sharedInstance() == null)
			UserContext.initialize(getApplicationContext());

		super.onCreate(savedInstanceState);

		_fromActivity = getIntent().getStringExtra("from");
		if (isUseEvent()) {
			EventManager.sharedInstance().register(this);
		}

		SwrveSDK.onCreate(this);
	}

	@Override
	protected void onStart() {
		super.onStart();

		FlurryAgent.init(this, KPConstants.FLURRY_API_KEY);
		FlurryAgent.onStartSession(this, "Base Activity ");

		if (shouldCheckForUpdate()) {
			UpdateVersionHelper.sharedInstance(this).doCheck();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		KidpowerApplication.setCurrentActivity(BaseActivity.this);

		SwrveSDK.onResume(this);

		topInstance = BaseActivity.this;
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (KidpowerApplication.getAppContext() == BaseActivity.this) {
			KidpowerApplication.setCurrentActivity(null);
		}

		SwrveSDK.onPause();
	}

	@Override
	protected void onStop() {
		FlurryAgent.onEndSession(this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (isUseEvent()) {
			EventManager.sharedInstance().unregister(this);
		}

		SwrveSDK.onDestroy(this);
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		SwrveSDK.onLowMemory();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		SwrveSDK.onNewIntent(intent);
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);

		// initialize resolution set
		mainLayout = findViewById(R.id.layout_parent);

		if (mainLayout != null)
			mainLayout.setBackgroundColor(CommonUtils.getColorFromRes(getResources(), R.color.kidpower_background));

		if (config.USE_RESOLUTIONSET) {
			if (config.USE_GLOBALLISTENER) {
				mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(
						new ViewTreeObserver.OnGlobalLayoutListener() {
							public void onGlobalLayout() {
								if (!bInitialized) {
									int curOrientation = getResources().getConfiguration().orientation;

									Rect r = new Rect();
									mainLayout.getLocalVisibleRect(r);
									ResolutionSet._instance.setResolution(r.width(), r.height(), true);
									ResolutionSet._instance.iterateChild(mainLayout);
									bInitialized = true;

									changedLayoutOrientation(curOrientation);
								}
							}
						}
				);
			} else {
				ResolutionSet._instance.iterateChild(mainLayout);
				changedLayoutOrientation(curOrientation);
			}
		}
	}

	@Override
	public void startActivity(Intent intent) {
		intent.putExtra("from", this.getClass().getName());
		super.startActivity(intent);
	}

	protected int getContainerViewId() {
		return R.id.layout_content;
	}

	protected boolean shouldCheckForUpdate() {
		return true;
	}

	public void changedLayoutOrientation(int orientation) {
		curOrientation = orientation;
	}

	/**
	 * Push new fragment to fragment container
	 *
	 * @param newFragment New fragment to be pushed
	 */
	public void pushNewFragment(SuperFragment newFragment) {
		pushNewFragment(newFragment, indexOfCurrentFragment + 1);
	}

	/**
	 * Present new fragment to fragment container
	 *
	 * @param newFragment New fragment to be pushed
	 */
	public void presentNewFragment(SuperFragment newFragment) {
		presentNewFragment(newFragment, 0);
	}

	public void showDialogFragment(BaseDialogFragment fragment) {
		try {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment prev = getFragmentManager().findFragmentByTag("dialog");
			if (prev != null) {
				ft.addToBackStack(null);
			}
			fragment.show(ft, "dialog");
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public void presentNewFragment(
			SuperFragment newFragment,
			int fragmentIndex,
			int animEnter,
			int animExit,
			int animPopEnter,
			int animPopExit
	) {
		try {
			getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.setCustomAnimations(animEnter, animExit, animPopEnter, animPopExit);
			transaction.replace(getContainerViewId(), newFragment);
			transaction.addToBackStack(String.valueOf(fragmentIndex));
			transaction.commit();

			currentFragment = newFragment;
			indexOfCurrentFragment = fragmentIndex;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Present new fragment to fragment container
	 *
	 * @param newFragment New fragment to be pushed
	 */
	public void presentNewFragment(SuperFragment newFragment, int fragmentIndex) {
		presentNewFragment(
				newFragment,
				fragmentIndex,
				R.anim.right_in,
				R.anim.left_out,
				R.anim.left_in,
				R.anim.left_out
		);
	}

	/**
	 * Push new fragment to fragment container
	 *
	 * @param newFragment New fragment to be pushed
	 */
	public void pushNewFragment(SuperFragment newFragment, int fragmentIndex) {
		hideKeyboard();

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.right_in, R.anim.left_out, R.anim.left_in, R.anim.right_out);
		transaction.replace(getContainerViewId(), newFragment, String.valueOf(fragmentIndex));
		transaction.addToBackStack(String.valueOf(fragmentIndex));

		try {
			transaction.commit();
			currentFragment = newFragment;
			indexOfCurrentFragment = fragmentIndex;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public void popupNewFragment(SuperFragment newFragment) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.setCustomAnimations(R.anim.fade, R.anim.abc_fade_out, R.anim.abc_fade_in, R.anim.abc_fade_out);
		transaction.replace(R.id.layout_parent, newFragment);
		transaction.addToBackStack(String.valueOf(indexOfCurrentFragment + 1));
		transaction.commit();

		currentFragment = newFragment;
		indexOfCurrentFragment = indexOfCurrentFragment + 1;
	}

	///////////////////////// Activity transition methods begin /////////////////////////
	public void pushNewActivityAnimated(Class dstClass) {
		pushNewActivityAnimated(dstClass, AnimConst.ANIMDIR_FROM_RIGHT, 0, null, -1);
	}

	public void pushNewActivityAnimated(Class dstClass, Bundle bundle) {
		pushNewActivityAnimated(dstClass, AnimConst.ANIMDIR_FROM_RIGHT, 0, bundle, -1);
	}

	public void pushNewActivityAnimated(Class dstClass, Bundle bundle, int req_code) {
		pushNewActivityAnimated(dstClass, AnimConst.ANIMDIR_FROM_RIGHT, 0, bundle, req_code);
	}

	public void pushNewActivityAnimated(Class dstClass, int animation, Bundle bundle) {
		pushNewActivityAnimated(dstClass, animation, 0, bundle, -1);
	}

	public void pushNewActivityAnimated(Class dstClass, int animation) {
		pushNewActivityAnimated(dstClass, animation, 0, null, -1);
	}

	public void pushNewActivityAnimated(Class dstClass, int animation, int req_code) {
		pushNewActivityAnimated(dstClass, animation, 0, null, req_code);
	}

	public void pushNewActivityAnimated(Class dstClass,
										int animation,
										int activity_flags,
										int req_code) {
		pushNewActivityAnimated(dstClass, animation, activity_flags, null, req_code);
	}

	/**
	 * Method to show new activity with animation.
	 * Now animation only supports two types - cover from right and from left.
	 *
	 * @param dstClass       Destination activity class.
	 * @param animation      Push activity animation. See AnimConst class.
	 * @param activity_flags Used for the startActivityForResult(...) method
	 * @param extras         Used to pass extra parameters to activity
	 * @see BaseActivity.AnimConst
	 * @see android.content.Intent
	 * @see android.os.Bundle
	 */
	public void pushNewActivityAnimated(final Class dstClass,
										final int animation,
										final int activity_flags,
										final Bundle extras,
										final int req_code) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(BaseActivity.this, dstClass);
				intent.putExtra(AnimConst.EXTRA_ANIMDIR, animation);

				if (activity_flags != 0)
					intent.addFlags(activity_flags);

				if (extras != null)
					intent.putExtras(extras);

				BaseActivity.this.startActivityForResult(intent, req_code);
			}
		});
	}

	/**
	 * Method to dismiss current activity without animation
	 */
	public void popOverCurActivityNonAnimated() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				BaseActivity.this.finish();
				overridePendingTransition(0, 0);
			}
		});
	}

	/**
	 * Method to dismiss current activity with animation
	 * Animation is the opposite of the animation which is used to show activity
	 */
	public void popOverCurActivityAnimated() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				BaseActivity.this.finish();

				int nDir = getIntent().getIntExtra(AnimConst.EXTRA_ANIMDIR, -1);
				if (nDir == AnimConst.ANIMDIR_FROM_LEFT)
					overridePendingTransition(R.anim.right_in, R.anim.left_out);
				else if (nDir == AnimConst.ANIMDIR_FROM_RIGHT)
					overridePendingTransition(R.anim.left_in, R.anim.right_out);
				else
					overridePendingTransition(0, 0);
			}
		});
	}

	public void hideKeyboard() {
		View v = getCurrentFocus();
		if (v instanceof EditText) {
			v.clearFocus();
			InputMethodManager imm = (InputMethodManager) getSystemService(
					Context.INPUT_METHOD_SERVICE
			);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
	}

	protected abstract boolean isUseEvent();

	public class AnimConst {
		public static final int ANIMDIR_NONE = -1;
		public static final int ANIMDIR_FROM_LEFT = 0;
		public static final int ANIMDIR_FROM_RIGHT = 1;

		public static final String EXTRA_ANIMDIR = "anim_direction";
	}


	public static BaseActivity topInstance() {
		return topInstance;
	}
}