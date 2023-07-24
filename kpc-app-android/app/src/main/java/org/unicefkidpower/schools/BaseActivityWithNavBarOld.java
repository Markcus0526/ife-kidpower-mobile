package org.unicefkidpower.schools;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.unicefkidpower.schools.context.UserContext;
import org.unicefkidpower.schools.define.KPConstants;
import org.unicefkidpower.schools.helper.Logger;

/**
 * Created by donal_000 on 1/6/2015.
 */
public abstract class BaseActivityWithNavBarOld extends BaseActivity implements NavigationBarDelegateOld {
	protected ViewGroup mNavigationContainer;
	protected NavigationBarOld mNavigationBar;
	private PopupWindow popup = null;
	private int layoutResID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);

		this.layoutResID = layoutResID;
		// create status monitor
		mNavigationBar = new NavigationBarOld(this, this);
		mNavigationContainer = (ViewGroup) findViewById(R.id.rlNavigationBar);
		mNavigationContainer.addView(mNavigationBar.getView());
	}

	// --------- NavigationBarOld delegate -------------------------------------------------
	@Override
	public String getNavbarTitle() {
		return "";
	}

	@Override
	public boolean shouldShowMenu() {
		return false;
	}

	@Override
	public boolean shouldShowHome() {
		return false;
	}

	@Override
	public boolean shouldShowBack() {
		return false;
	}

	@Override
	public boolean shouldShowLogo() {
		return true;
	}

	@Override
	public boolean shouldShowLanguageMenu() {
		return false;
	}

	@Override
	public void onMenuClicked() {
	}

	@Override
	public void onHomeClicked() {
	}

	@Override
	public void onBackClicked() {
	}

	@Override
	public void onLanguageMenuClicked() {
		mNavigationBar.rlLanguageMenuBackground.setSelected(true);

		int heightForLanguageMenuCell = mNavigationBar.getView().getHeight() * 29 / 43;
		int widthForLanguageMenuCell = mNavigationBar.rlBtnLanguageMenu.getWidth() * 214 / 105;

		LinearLayout langMenu = new LinearLayout(this);
		langMenu.setOrientation(LinearLayout.VERTICAL);

		for (int i = 0; i < KPConstants.SUPPORTED_LANGUAGES.size(); i++) {
			LinearLayout item_layout = (LinearLayout) getLayoutInflater()
					.inflate(R.layout.language_menu_item, null);
			item_layout.setBackgroundResource(R.drawable.kidpower_languagemenuitembg);
			item_layout.setTag(i);
			LanguageMenuViewHolder holder = new LanguageMenuViewHolder(item_layout);
			holder.txtLanguage.setText(KPConstants.SUPPORTED_LANGUAGES.get(i)._name);
			holder.ivFlag.setImageResource(KPConstants.SUPPORTED_LANGUAGES.get(i)._resId);
			item_layout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (KPConstants.SUPPORTED_LANGUAGES.get((Integer) v.getTag())._locale
							.equals(UserContext.sharedInstance().getAppLocale())) {
						if (popup != null)
							popup.dismiss();
						return;
					}

					Logger.log("Navigation Bar", "Language Selected - %s", KPConstants.SUPPORTED_LANGUAGES.get((Integer) v.getTag())._name);

					UserContext.sharedInstance().setAppLocale(KPConstants.SUPPORTED_LANGUAGES.get((Integer) v.getTag())._locale);
					((ImageView) mNavigationBar.rlBtnLanguageMenu.findViewById(R.id.ivFlag))
							.setImageResource(KPConstants.SUPPORTED_LANGUAGES.get((Integer) v.getTag())._resId);
					restartActivity();
				}
			});

			if (KPConstants.SUPPORTED_LANGUAGES.get(i)._locale.equals(UserContext.sharedInstance().getAppLocale())) {
				item_layout.setSelected(true);
			} else {
				item_layout.setSelected(false);
			}

			langMenu.addView(item_layout, widthForLanguageMenuCell, heightForLanguageMenuCell);
		}

		int[] nCoordinates = new int[2];
		mNavigationBar.rlBtnLanguageMenu.getLocationOnScreen(nCoordinates);
		popup = new PopupWindow(this);
		popup.setContentView(langMenu);
		popup.setWidth(widthForLanguageMenuCell);
		popup.setHeight(heightForLanguageMenuCell * KPConstants.SUPPORTED_LANGUAGES.size());
		popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popup.setFocusable(true);
		popup.setAnimationStyle(R.style.DropDownAnimation);
		popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				mNavigationBar.rlLanguageMenuBackground.setSelected(false);
			}
		});

		popup.showAtLocation(langMenu, Gravity.NO_GRAVITY,
				nCoordinates[0] - widthForLanguageMenuCell + mNavigationBar.rlBtnLanguageMenu.getWidth(),
				nCoordinates[1] + heightForLanguageMenuCell
		);
	}

	public void showDialogFragment(BaseDialogFragment fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.addToBackStack(null);
		}
		fragment.show(ft, "dialog");
	}

	private void restartActivity() {
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}

	// ---------------------------------------------------------
	public class LanguageMenuViewHolder {
		TextView txtLanguage;
		ImageView ivFlag;

		LanguageMenuViewHolder(View view) {
			txtLanguage = (TextView) view.findViewWithTag("language");
			ivFlag = (ImageView) view.findViewWithTag("flag");
		}
	}
}