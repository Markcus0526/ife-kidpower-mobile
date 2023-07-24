package org.unicefkidpower.schools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.unicefkidpower.schools.context.UserContext;
import org.unicefkidpower.schools.define.KPConstants;
import org.unicefkidpower.schools.define.config;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.helper.OSDate;

import java.util.Date;

/**
 * Created by donal_000 on 1/5/2015.
 */
public class NavigationBarOld implements View.OnClickListener {
	public View rlBtnLanguageMenu, rlLanguageMenuBackground;

	// controls
	private TextView mTextTitle;
	private View rlBtnMenu;
	private View rlBtnHome;
	private View rlBtnBack;
	private ImageView ivIconBack;
	private TextView textBack;
	private View ivLogo;

	private View mView;
	private LayoutInflater mInflator;
	private NavigationBarDelegateOld mDelegate;

	private Context context;
	protected final static long minSendDuration = 10 * 1000;
	protected final static long minTappDuration = 30;
	public static final String TAG = "Navigation Bar";

	private int tapped_count = 0;
	private Date last_tapped = new Date();
	private Date last_sent = null;

	// constructor
	public NavigationBarOld(final Context context, NavigationBarDelegateOld delegate) {
		this.context = context;
		mDelegate = delegate;

		mInflator = LayoutInflater.from(context);
		mView = mInflator.inflate(R.layout.navigationbar_old, null);

		mTextTitle = (TextView) mView.findViewById(R.id.textTitle);
		mTextTitle.setText(delegate.getNavbarTitle());

		rlBtnMenu = mView.findViewById(R.id.rlBtnMenu);
		rlBtnMenu.setOnClickListener(this);

		rlBtnHome = mView.findViewById(R.id.rlBtnHome);
		rlBtnHome.setOnClickListener(this);

		rlBtnLanguageMenu = mView.findViewById(R.id.rlBtnLanguageMenu);
		rlBtnLanguageMenu.setOnClickListener(this);

		rlLanguageMenuBackground = mView.findViewById(R.id.rlLanguageMenuBackground);

		setLanguageFlag();

		rlBtnBack = mView.findViewById(R.id.rlBtnBack);
		rlBtnBack.setOnClickListener(this);

		ivIconBack = (ImageView) mView.findViewById(R.id.ivIconBack);
		textBack = (TextView) mView.findViewById(R.id.textBack);

		ivLogo = (ImageView) mView.findViewById(R.id.ivLogo);

		// show hide buttons
		if (mDelegate.shouldShowHome())
			rlBtnHome.setVisibility(View.VISIBLE);
		else
			rlBtnHome.setVisibility(View.GONE);

		if (mDelegate.shouldShowMenu())
			rlBtnMenu.setVisibility(View.VISIBLE);
		else
			rlBtnMenu.setVisibility(View.GONE);

		if (mDelegate.shouldShowBack()) {
			rlBtnBack.setVisibility(View.VISIBLE);
		} else {
			rlBtnBack.setVisibility(View.GONE);
		}

		if (mDelegate.shouldShowLogo()) {
			ivLogo.setVisibility(View.VISIBLE);
		} else {
			ivLogo.setVisibility(View.INVISIBLE);
		}

		ivLogo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Date now = new Date();

				if (last_sent != null) {
					long diffSec = OSDate.betweenDates(last_sent, now);
					//if (diffSec < minSendDuration) {return;}
				}

				long diffSec = OSDate.betweenDates(now, last_tapped);
				if (diffSec > minTappDuration) {
					tapped_count = 0;
				}
				last_tapped = now;
				tapped_count++;

				if (tapped_count < 5) {
					return;
				}
				tapped_count = 0;
				last_sent = last_tapped;

				// tap processing
				Logger.sendLogWithEMail(context);
			}
		});

		if (mDelegate.shouldShowLanguageMenu()) {
			rlBtnLanguageMenu.setVisibility(View.VISIBLE);
		} else {
			rlBtnLanguageMenu.setVisibility(View.INVISIBLE);
		}

		if (!config.USE_GLOBALLISTENER)
			ResolutionSet._instance.iterateChild(mView);
	}

	public View getView() {
		return mView;
	}

	@Override
	public void onClick(View v) {
		if (rlBtnMenu == v) {
			Logger.log(TAG, "Menu button clicked");
			mDelegate.onMenuClicked();
		} else if (rlBtnHome == v) {
			Logger.log(TAG, "Home button clicked");
			mDelegate.onHomeClicked();
		} else if (rlBtnBack == v) {
			Logger.log(TAG, "Back button clicked");
			mDelegate.onBackClicked();
		} else if (rlBtnLanguageMenu == v) {
			Logger.log(TAG, "Language Selection button clicked");
			mDelegate.onLanguageMenuClicked();
		}
	}

	public void showMenu() {
		rlBtnMenu.setVisibility(View.VISIBLE);
	}

	public void showHome() {
		rlBtnHome.setVisibility(View.VISIBLE);
	}

	public void showBack() {
		rlBtnBack.setVisibility(View.VISIBLE);
	}

	public void showLanguageMenu() {
		rlBtnBack.setVisibility(View.VISIBLE);
	}

	public void disableBack() {
		rlBtnBack.setEnabled(false);
	}

	public void enableBack() {
		rlBtnBack.setEnabled(true);
	}

	private void setLanguageFlag() {
		ImageView ivFlag = (ImageView) rlBtnLanguageMenu.findViewById(R.id.ivFlag);
		for (int i = 0; i < KPConstants.SUPPORTED_LANGUAGES.size(); i++) {

			if (UserContext.sharedInstance() == null)
				UserContext.initialize(getView().getContext().getApplicationContext());

			if (KPConstants.SUPPORTED_LANGUAGES.get(i)._locale.equals(UserContext.sharedInstance().getAppLocale())) {
				ivFlag.setImageResource(KPConstants.SUPPORTED_LANGUAGES.get(i)._resId);
				break;
			}
		}
	}


}
