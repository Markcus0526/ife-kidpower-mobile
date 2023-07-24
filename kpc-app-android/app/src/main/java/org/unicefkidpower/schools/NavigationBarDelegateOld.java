package org.unicefkidpower.schools;

/**
 * Created by donal_000 on 1/5/2015.
 */
public interface NavigationBarDelegateOld {
	String getNavbarTitle();

	boolean shouldShowMenu();

	boolean shouldShowHome();

	boolean shouldShowBack();

	boolean shouldShowLogo();

	boolean shouldShowLanguageMenu();

	void onHomeClicked();

	void onMenuClicked();

	void onBackClicked();

	void onLanguageMenuClicked();
}
