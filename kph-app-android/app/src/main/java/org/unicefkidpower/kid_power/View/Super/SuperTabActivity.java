package org.unicefkidpower.kid_power.View.Super;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ruifeng Shi on 2/4/2017.
 */

public class SuperTabActivity extends SuperActivity {
	private final boolean								FRAGMENT_LOG_MANAGEMENT			= false;		// Required by Diana. Has logical issue. Do not set as true
	public static final String							EXTRA_INITIAL_FRAGMENT_INDEX = "extra_initial_fragment_index";

	protected OnTabChangedListener						tabChangedListener				= null;
	protected int										currentTabIndex					= 0;
	protected ArrayList<ArrayList<SuperFragment>>		fragmentsListArray				= new ArrayList<>();
	protected ArrayList<STFragmentLog>					fragmentsLogArray				= new ArrayList<>();

	protected View										tabBar			= null;
	protected View										tabBarShadow	= null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
	}

	/**
	 * new fragment to fragment container
	 *
	 * @param newFragment New fragment to be pushed
	 */
	public void pushNewFragment(SuperFragment newFragment) {
		pushNewFragment(newFragment, true);
	}

	public void pushNewFragment(SuperFragment newFragment, boolean needAnimation) {
		hideKeyboard();
		if (needAnimation)
			showFragment(newFragment, ANIM_DIRECTION_FROM_RIGHT);
		else
			showFragment(newFragment, ANIM_DIRECTION_FROM_NONE);

		ArrayList<SuperFragment> fragmentsForCurrentTab = fragmentsListArray.get(currentTabIndex);
		fragmentsForCurrentTab.add(newFragment);

		if (FRAGMENT_LOG_MANAGEMENT) {
			fragmentsLogArray.add(new STFragmentLog(currentTabIndex, fragmentsForCurrentTab.size() - 1));
		}
	}

	public void popFragment() {
		hideKeyboard();

		ArrayList<SuperFragment> fragmentsForCurrentTab = fragmentsListArray.get(currentTabIndex);
		if (fragmentsForCurrentTab.size() < 2)
			return;

		SuperFragment topFragment = fragmentsForCurrentTab.get(fragmentsForCurrentTab.size() - 1);
		SuperFragment newTopFragment = fragmentsForCurrentTab.get(fragmentsForCurrentTab.size() - 2);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.left_in, R.anim.right_out);

		if (topFragment.isAdded())
			transaction.remove(topFragment);

		if (newTopFragment.isAdded()) {
			if (tabBar != null) {
				setTabbarVisibility(newTopFragment.isShowTabBar());
			}

			transaction.show(newTopFragment);
			try {
				transaction.commit();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			showFragment(newTopFragment, ANIM_DIRECTION_FROM_LEFT);
		}

		fragmentsForCurrentTab.remove(fragmentsForCurrentTab.size() - 1);
	}


	@Override
	public void onClickedBackSystemButton() {
		if (MainActivity.currentPlayingVideoLayout != null && MainActivity.currentPlayingVideoLayout.isFullscreen()) {
			MainActivity.currentPlayingVideoLayout.exitFullScreen();
			return;
		}

		Fragment topFragment = getTopFragment();
		if (topFragment != null && topFragment.getTag() != null) {
			// This means at least there is one fragment except fragments which are managed manually by tab.
			super.onClickedBackSystemButton();
			return;
		}

		if (currentTabIndex < 0 || currentTabIndex >= fragmentsListArray.size()) {
			super.onClickedBackSystemButton();
			return;
		}

		if (FRAGMENT_LOG_MANAGEMENT) {
			if (fragmentsLogArray.size() == 1) {
				super.onClickedBackSystemButton();
			} else {
				STFragmentLog lastLog = fragmentsLogArray.get(fragmentsLogArray.size() - 1);
				STFragmentLog prevLog = fragmentsLogArray.get(fragmentsLogArray.size() - 2);

				if (prevLog.tabIndex == lastLog.tabIndex) {
					popFragment();
				} else {
					selectTab(prevLog.tabIndex, false);
				}

				fragmentsLogArray.remove(fragmentsLogArray.size() - 1);
			}
		} else {
			ArrayList<SuperFragment> fragmentsForCurrentTab = fragmentsListArray.get(currentTabIndex);
			if (fragmentsForCurrentTab.size() < 2) {
				super.onClickedBackSystemButton();
			} else {
				popFragment();
			}
		}
	}


	/**
	 * Called when a tab has been selected on activity
	 */
	protected void selectTab(int index, boolean addToFragmentLog) {
		if (currentTabIndex == index)
			return;

		dismissProgressDialog();
		currentTabIndex = index;

		ArrayList<SuperFragment> fragmentsForCurrentTab = fragmentsListArray.get(currentTabIndex);
		if (fragmentsForCurrentTab.size() == 0)
			return;

		SuperFragment topFragment = fragmentsForCurrentTab.get(fragmentsForCurrentTab.size() - 1);
		showFragment(topFragment, ANIM_DIRECTION_FROM_NONE);

		if (addToFragmentLog && FRAGMENT_LOG_MANAGEMENT) {
			fragmentsLogArray.add(new STFragmentLog(currentTabIndex, fragmentsForCurrentTab.size() - 1));
		}

		if (tabChangedListener != null)
			tabChangedListener.onTabChanged(currentTabIndex);
	}


	private void showFragment(SuperFragment newFragment, int anim_dir) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		if (anim_dir == ANIM_DIRECTION_FROM_RIGHT)
			transaction.setCustomAnimations(R.anim.right_in, R.anim.left_out);
		else if (anim_dir == ANIM_DIRECTION_FROM_LEFT)
			transaction.setCustomAnimations(R.anim.left_in, R.anim.right_out);
		else if (anim_dir == ANIM_DIRECTION_FROM_TOP)
			transaction.setCustomAnimations(R.anim.top_in, R.anim.bottom_out);
		else if (anim_dir == ANIM_DIRECTION_FROM_BOTTOM)
			transaction.setCustomAnimations(R.anim.bottom_in, R.anim.top_out);

		for (int i = 0; i < fragmentsListArray.size(); i++) {
			ArrayList<SuperFragment> fragments = fragmentsListArray.get(i);
			for (int j = 0; j < fragments.size(); j++) {
				SuperFragment fragmentItem = fragments.get(j);
				if (fragmentItem != newFragment && fragmentItem.isAdded()) {
					transaction.hide(fragmentItem);
				}
			}
		}

		if (!newFragment.isAdded()) {
			Fragment topFragment = getTopFragment();
			if (topFragment != null && topFragment instanceof SuperFragment && ((SuperFragment) topFragment).isApplyToChilds()) {
				newFragment.showTabBar(((SuperFragment) topFragment).isShowTabBar());
			}

			transaction.add(getContainerViewId(), newFragment);
		}

		transaction.show(newFragment);
		if (tabBar != null) {
			setTabbarVisibility(newFragment.isShowTabBar());
		}

		try {
			transaction.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	private void setTabbarVisibility(final boolean visibility) {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (visibility) {
							tabBar.setVisibility(View.VISIBLE);
							if (tabBarShadow != null)
								tabBarShadow.setVisibility(View.VISIBLE);
						} else {
							tabBar.setVisibility(View.GONE);
							if (tabBarShadow != null)
								tabBarShadow.setVisibility(View.GONE);
						}
					}
				});
			}
		};
		Timer timer = new Timer();
		timer.schedule(task, 150);
	}


	protected void initializeTabContents(ArrayList<ArrayList<SuperFragment>> fragmentsListArray, int initialTabIndex) {
		this.fragmentsListArray = fragmentsListArray;

		currentTabIndex = initialTabIndex;

		ArrayList<SuperFragment> fragmentsForCurrentTab = fragmentsListArray.get(currentTabIndex);
		if (fragmentsForCurrentTab.size() == 0)
			return;

		SuperFragment topFragment = fragmentsForCurrentTab.get(fragmentsForCurrentTab.size() - 1);
		showFragment(topFragment, ANIM_DIRECTION_FROM_NONE);

		if (FRAGMENT_LOG_MANAGEMENT) {
			fragmentsLogArray.add(new STFragmentLog(currentTabIndex, 0));
		}
	}


	public class STFragmentLog {
		public int tabIndex = 0;
		public int fragmentIndex = 0;

		public STFragmentLog(int tabIndex, int fragmentIndex) {
			this.tabIndex = tabIndex;
			this.fragmentIndex = fragmentIndex;
		}
	}

	public interface OnTabChangedListener {
		void onTabChanged(int index);
	}
}
