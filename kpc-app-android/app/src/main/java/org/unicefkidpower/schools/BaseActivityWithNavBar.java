package org.unicefkidpower.schools;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.ViewGroup;

/**
 * Created by donal_000 on 1/6/2015.
 */
public abstract class BaseActivityWithNavBar extends BaseActivity implements ActionBarDelegate {
	protected ViewGroup mNavigationContainer;
	protected ActionBar mActionBar;
	int layoutResID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);

		this.layoutResID = layoutResID;
		// create status monitor
		mActionBar = new ActionBar(this, this);
		mNavigationContainer = (ViewGroup) findViewById(R.id.rlNavigationBar);
		mNavigationContainer.addView(mActionBar.getView());
	}

	// --------- NavigationBarOld delegate -------------------------------------------------
	@Override
	public String getActionBarTitle() {
		return "";
	}

	@Override
	public boolean shouldShowMenu() {
		return true;
	}

	@Override
	public boolean shouldShowBack() {
		return false;
	}

	@Override
	public void onMenuClicked() {

	}

	@Override
	public void onBackClicked() {
		if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
			FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().getBackStackEntryAt(
					getSupportFragmentManager().getBackStackEntryCount() - 1
			);
			String str = backEntry.getName();

			android.support.v4.app.Fragment lastFragment = getSupportFragmentManager().findFragmentByTag(
					str
			);

			// @NOTE: Please do NOT delete (SuperFragment) cast. It's NECESSARY
			if (((SuperFragment) lastFragment) != null) {
				if (!((SuperFragment) lastFragment).onBackPressed()) {
					getSupportFragmentManager().popBackStack();
				}
			} else {
				getSupportFragmentManager().popBackStack();
			}
		} else {
			popOverCurActivityAnimated();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			onBackClicked();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public ActionBar getCustomActionBar() {
		return mActionBar;
	}

	private void restartActivity() {
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}
}