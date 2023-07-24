package org.unicefkidpower.kid_power.View.Super;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.swrve.sdk.SwrveSDK;

import org.unicefkidpower.kid_power.Misc.Config;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.CustomControls.KPHBrandedDialog;
import org.unicefkidpower.kid_power.View.CustomControls.KPHProgressDialog;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.AlertDialogHelper;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.Misc.onActivityResultListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by donal_000 on 1/6/2015.
 */
public abstract class SuperActivity extends FragmentActivity {
	private final String						TAG = "SuperActivity";

	/***********************************************************************************************
	 * Constants
	 */
	protected final int							ANIM_DIRECTION_FROM_NONE = -1;
	protected final int							ANIM_DIRECTION_FROM_LEFT = 0;
	protected final int							ANIM_DIRECTION_FROM_RIGHT = 1;
	protected final int							ANIM_DIRECTION_FROM_BOTTOM = 2;
	protected final int							ANIM_DIRECTION_FROM_TOP = 3;
	/*
	 * End of 'Constants'
	 **********************************************************************************************/

	/***********************************************************************************************
	 * Private fields
 	 */
	private static SuperActivity			topInstance				= null;

	private int								curOrientation;
	private KPHProgressDialog				progressDialog;

	private onActivityResultListener		iapResponseListener;

	private boolean							isInitializedResolution		= false;
	/**
	 * End of 'Private fields'
 	 **********************************************************************************************/

	// To Finish App, need to tap on back button twice in 3 seconds
	private final int						TAP_INTERVAL			= 3 * 1000;		// 3 seconds
	private long							lastTapTimeStamp		= 0;
	////////////////////////////////////////////////////////////////////////////////////////////////


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SwrveSDK.onCreate(SuperActivity.this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Logger.log(TAG, "onResume : " + getClass().getName());

		SwrveSDK.onResume(this);
		hideKeyboard();

		topInstance = SuperActivity.this;
	}


	@Override
	protected void onPause() {
		super.onPause();
		SwrveSDK.onPause();
		hideKeyboard();
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		SwrveSDK.onNewIntent(intent);
	}

	@Override
	protected void onDestroy() {
		SwrveSDK.onDestroy(this);
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		SwrveSDK.onLowMemory();
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);

		// Initialize resolution set
		if (Config.USE_RESOLUTIONSET) {
			View mainLayout = getWindow().getDecorView().findViewById(android.R.id.content);

			if (Config.USE_GLOBAL_LISTENER) {
				mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(
						new ViewTreeObserver.OnGlobalLayoutListener() {
							public void onGlobalLayout() {
								if (!isInitializedResolution) {
									int curOrientation = getResources().getConfiguration().orientation;

									View mainLayout = getWindow().getDecorView().findViewById(android.R.id.content);

									Rect r = new Rect();
									mainLayout.getLocalVisibleRect(r);
									ResolutionSet.sharedInstance().setResolution(r.width(), r.height(), true);
									ResolutionSet.sharedInstance().iterateChild(mainLayout);
									isInitializedResolution = true;

									changedLayoutOrientation(curOrientation);
								}
							}
						}
				);
			} else {
				ResolutionSet.sharedInstance().iterateChild(mainLayout);
				changedLayoutOrientation(curOrientation);
			}
		}

		initControls();

		// Initialize animation
		int nDir = getIntent().getIntExtra(AnimConst.EXTRA_ANIMDIR, -1);
		if (nDir == AnimConst.ANIMDIR_FROM_LEFT)
			overridePendingTransition(R.anim.left_in, R.anim.right_out);
		else if (nDir == AnimConst.ANIMDIR_FROM_RIGHT)
			overridePendingTransition(R.anim.right_in, R.anim.left_out);
		else
			overridePendingTransition(0, 0);
	}


	protected void clearExtras() {
		Bundle extras = getIntent().getExtras();

		Set<String> keySet = extras.keySet();
		for (String keyItem : keySet) {
			if (getIntent().hasExtra(keyItem))
				getIntent().removeExtra(keyItem);
		}
	}


	public static SuperActivity topInstance() {
		return topInstance;
	}

	/**
	 * Link control objects and views, and initializes controls
	 */
	public void initControls() {
		View backButton = findViewById(R.id.btnBack);
		if (backButton != null) {
			backButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickedBackSystemButton();
				}
			});
		}
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
	 * @see SuperActivity.AnimConst
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
				Intent intent = new Intent(SuperActivity.this, dstClass);
				intent.putExtra(AnimConst.EXTRA_ANIMDIR, animation);

				if (activity_flags != 0)
					intent.addFlags(activity_flags);

				if (extras != null)
					intent.putExtras(extras);

				SuperActivity.this.startActivityForResult(intent, req_code);
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
				SuperActivity.this.finish();
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
				SuperActivity.this.finish();

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
	///////////////////////// End of "Activity transition methods" /////////////////////////

	public void showProgressDialog() {
		showProgressDialog(true);
	}

	public void showProgressDialog(boolean backTrans) {
		showProgressDialog("", getString(R.string.please_wait), backTrans);
	}

	public void showProgressDialog(String text) {
		showProgressDialog("", text, true);
	}

	public void showProgressDialog(String text, boolean backTrans) {
		showProgressDialog("", text, backTrans);
	}

	public void showProgressDialog(String title, String text) {
		showProgressDialog(title, text, true);
	}


	public void showProgressDialog(String title, String text, boolean backTrans) {
		Logger.log(TAG, "*************************************start progress dialog");

		if (progressDialog != null && progressDialog.isShowing())
			return;

		Context parentContext;
		Fragment topFragment = getTopFragment();
		if (topFragment != null && topFragment instanceof DialogFragment && ((DialogFragment)topFragment).getDialog() != null) {
			parentContext = ((DialogFragment) topFragment).getDialog().getContext();
		} else {
			parentContext = SuperActivity.this;
		}

		progressDialog = new KPHProgressDialog(parentContext);
		progressDialog.setData(title, text, backTrans);
		progressDialog.show();
	}


	public boolean isShowingProgressDialog() {
		return progressDialog != null && progressDialog.isShowing();
	}

	/**
	 * Close Progress Dialog
	 */
	public void dismissProgressDialog() {
		Logger.log(TAG, "*************************************dismiss progress dialog");
		try {
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			progressDialog.setCancelable(true);
		}
	}

	public void dismissProgressDialogWithDelay(int delayMillis) {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				dismissProgressDialog();
			}
		};

		Timer timer = new Timer();
		timer.schedule(timerTask, delayMillis);
	}

	public void showBrandedDialog(final String text,
								  final String defButton,
								  final String otherButton,
								  final KPHBrandedDialog.KPHBrandedDialogCallback callback) {
		dismissProgressDialog();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Context parentContext;
				Fragment topFragment = getTopFragment();
				if (topFragment != null && topFragment instanceof DialogFragment) {
					parentContext = ((DialogFragment) topFragment).getDialog().getContext();
				} else {
					parentContext = SuperActivity.this;
				}

				KPHBrandedDialog dialog = new KPHBrandedDialog(parentContext);
				dialog.setCallback(text, defButton, otherButton, callback);
				dialog.show();
			}
		});
	}

	public void showErrorDialog(String text) {
		try {
			showErrorDialog(text, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void showErrorDialog(String text, AlertDialogHelper.AlertListener dismissListener) {
		try {
			dismissProgressDialog();
			AlertDialogHelper.showErrorAlert(text, SuperActivity.this, dismissListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showAlertDialog(String title, String message, AlertDialogHelper.AlertListener listener) {
		try {
			dismissProgressDialog();
			AlertDialogHelper.showAlertDialog(title, message, SuperActivity.this, listener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showQuestionDialog(String title, String message,
								   String positive, String negative,
								   AlertDialogHelper.AlertListener listener) {
		try {
			dismissProgressDialog();
			AlertDialogHelper.showConfirmDialog(title, message, positive, negative, SuperActivity.this, listener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		boolean processed = false;
		if (iapResponseListener != null) {
			processed = iapResponseListener.onSharedActivityResult(requestCode, resultCode, data);
		}

		if (processed)
			return;

		Fragment topFragment = getTopFragment();
		if (topFragment != null) {
			topFragment.onActivityResult(requestCode, resultCode, data);
			return;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void setSharedListener(onActivityResultListener listener) {
		iapResponseListener = listener;
	}

	public void changedLayoutOrientation(int orientation) {
		curOrientation = orientation;
	}

	public View getRootView() {
		return findViewById(android.R.id.content);
	}

	public void hideKeyboard() {
		View v = getCurrentFocus();
		if (v != null && v instanceof EditText) {
			v.clearFocus();
		}

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getRootView().getApplicationWindowToken(), 0);
	}


	/****************************************************************************************************************
	 * Fragments transaction methods
 	 */
	public void showDialogFragment(SuperDialogFragment fragment) {
		try {
			String tag = generateUniqueTag();

			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction ft = fragmentManager.beginTransaction();
			ft.addToBackStack(tag);
			fragment.show(ft, tag);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}


	// Push new fragment if activity is SuperTabActivity, present new fragment otherwise
	public void showNewFragment(SuperFragment fragment) {
		if (SuperActivity.this instanceof SuperTabActivity) {
			((SuperTabActivity)SuperActivity.this).pushNewFragment(fragment);
		} else {
			presentNewFragmentFromRight(fragment);
		}
	}


	public void presentNewFragmentFromRight(SuperFragment newFragment) {
		presentNewFragment(newFragment, ANIM_DIRECTION_FROM_RIGHT);
	}


	public void presentNewFragmentFromBottom(SuperFragment newFragment) {
		presentNewFragment(newFragment, ANIM_DIRECTION_FROM_BOTTOM);
	}


	public void presentNewFragment(SuperFragment newFragment, int anim_dir) {
		hideKeyboard();

		if (getContainerViewId() == 0)
			return;

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();

		if (anim_dir == ANIM_DIRECTION_FROM_RIGHT)
			transaction.setCustomAnimations(R.anim.right_in, R.anim.left_out, R.anim.left_in, R.anim.right_out);
		else if (anim_dir == ANIM_DIRECTION_FROM_LEFT)
			transaction.setCustomAnimations(R.anim.left_in, R.anim.right_out, R.anim.right_in, R.anim.left_out);
		else if (anim_dir == ANIM_DIRECTION_FROM_TOP)
			transaction.setCustomAnimations(R.anim.top_in, R.anim.bottom_out, R.anim.bottom_in, R.anim.top_out);
		else if (anim_dir == ANIM_DIRECTION_FROM_BOTTOM)
			transaction.setCustomAnimations(R.anim.bottom_in, R.anim.top_out, R.anim.top_in, R.anim.bottom_out);


		Fragment prevFragment = getTopFragment();
		if (prevFragment != null)
			transaction.hide(prevFragment);

		String tag = generateUniqueTag();
		transaction.add(getContainerViewId(), newFragment, tag);
		transaction.addToBackStack(tag);

		try {
			transaction.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	private String generateUniqueTag() {
		FragmentManager fragmentManager = getSupportFragmentManager();

		String tag = "";
		int index = fragmentManager.getBackStackEntryCount();
		while (true) {
			tag = "" + index;
			if (fragmentManager.findFragmentByTag(tag) == null) {
				break;
			}

			index++;
		}

		return tag;
	}

	/**
	 * End of 'Fragments transaction methods'
	 ****************************************************************************************************************/


	/**
	 * Get the identifier value of fragment container view
	 *
	 * @return Returns the identifier of the container view
	 */
	protected int getContainerViewId() { return 0; }

	/**
	 * Class for the activity transition animation constants
	 */
	public class AnimConst {
		public static final int ANIMDIR_NONE = -1;
		public static final int ANIMDIR_FROM_LEFT = 0;
		public static final int ANIMDIR_FROM_RIGHT = 1;

		public static final String EXTRA_ANIMDIR = "anim_direction";
	}


	public void onClickedBackSystemButton() {
		if (MainActivity.currentPlayingVideoLayout != null &&
				MainActivity.currentPlayingVideoLayout.isFullscreen()) {
			MainActivity.currentPlayingVideoLayout.exitFullScreen();
			return;
		}

		Fragment topFragment = getTopFragment();
		if (topFragment != null && topFragment.getTag() != null) {
			if (topFragment instanceof DialogFragment &&
					!((DialogFragment)topFragment).isCancelable()) {
				return;
			}

			if (topFragment instanceof DialogFragment) {
				((DialogFragment) topFragment).dismiss();
			} else {
				FragmentManager fragmentManager = getSupportFragmentManager();
				fragmentManager.popBackStack(topFragment.getTag(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
			}
		} else {
			if (Config.NEED_CONFIRM_EXIT && !Config.PREVENT_CLOSE_APP) {
				if (isTaskRoot()) {
					if (Calendar.getInstance().getTimeInMillis() - lastTapTimeStamp > TAP_INTERVAL) {
						Toast.makeText(SuperActivity.this, R.string.tap_again_to_exit, Toast.LENGTH_LONG).show();
						lastTapTimeStamp = Calendar.getInstance().getTimeInMillis();
						return;
					}
				}
			}

			if (isTaskRoot()) {
				if (!Config.PREVENT_CLOSE_APP) {
					popOverCurActivityNonAnimated();
					System.exit(0);
				}
			} else {
				popOverCurActivityAnimated();
			}
		}
	}


	public Fragment getTopFragment() {
		Fragment resultFragment = null;
		FragmentManager fragmentManager = getSupportFragmentManager();

		if (fragmentManager.getBackStackEntryCount() > 0) {
			int fragmentCount = fragmentManager.getBackStackEntryCount();

			for (int i = fragmentCount - 1; i >= 0; i--) {
				String tagName = fragmentManager.getBackStackEntryAt(i).getName();

				Fragment fragmentItem = fragmentManager.findFragmentByTag(tagName);
				if (fragmentItem == null)
					continue;

				resultFragment = fragmentItem;
				break;
			}
		}

		if (resultFragment == null && SuperActivity.this instanceof SuperTabActivity) {
			SuperTabActivity tabActivity = (SuperTabActivity)SuperActivity.this;
			if (tabActivity.fragmentsListArray != null &&
					tabActivity.currentTabIndex >= 0 &&
					tabActivity.fragmentsListArray.size() > tabActivity.currentTabIndex) {
				ArrayList<SuperFragment> fragments = tabActivity.fragmentsListArray.get(tabActivity.currentTabIndex);
				if (fragments.size() > 0)
					resultFragment = fragments.get(fragments.size() - 1);
			}
		}

		return resultFragment;
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN &&
				keyCode == KeyEvent.KEYCODE_BACK) {
			onClickedBackSystemButton();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

}
