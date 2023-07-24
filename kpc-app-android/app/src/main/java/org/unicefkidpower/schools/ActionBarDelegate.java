package org.unicefkidpower.schools;

/**
 * Created by donal_000 on 1/5/2015.
 */
public interface ActionBarDelegate {
	String getActionBarTitle();

	boolean shouldShowMenu();

	boolean shouldShowBack();

	void onMenuClicked();

	void onBackClicked();
}
