package org.unicefkidpower.schools;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;

import org.unicefkidpower.schools.server.apimanage.UserService;
import org.unicefkidpower.schools.ui.KPStepperIndicator;

public class OnboardingActivity extends BaseActivityWithNavBar {
	public static final String TAG = "Onboarding Activity";
	public static final int NUM_PAGES = 6;
	public static final String EXTRA_CURRENT_PAGE = "EXTRA_CURRENT_PAGE";
	public static final String EXTRA_USER_INFO = "EXTRA_USER_INFO";

	private ViewPager mPager;
	private ScreenSlidePagerAdapter mPagerAdapter;

	private KPStepperIndicator stepperIndicator;

	private UserService.ResLoginWithCode userInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_onboarding);

		stepperIndicator = (KPStepperIndicator) findViewById(R.id.stepper_indicator);
		stepperIndicator.setData(getResources().getStringArray(R.array.onboarding_steps));

		FlurryAgent.onStartSession(this, TAG);

		ImageView ivDummy = (ImageView) findViewById(R.id.iv_dummy);
		ViewGroup.LayoutParams lpDummy = ivDummy.getLayoutParams();
		lpDummy.width = ResolutionSet.getScreenSize(this, false, true).x;
		lpDummy.height = ResolutionSet.getScreenSize(this, false, true).y -
				ResolutionSet.getStatusBarHeight(this);
		ivDummy.setLayoutParams(lpDummy);

		mPager = (ViewPager) findViewById(R.id.onboarding_pager);
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);

		int currentPage = getIntent().getIntExtra(EXTRA_CURRENT_PAGE, 0);
		String userInfo = getIntent().getStringExtra(EXTRA_USER_INFO);

		mPager.setCurrentItem(currentPage, false);

		if (!TextUtils.isEmpty(userInfo)) {
			setUserInfo(new Gson().fromJson(userInfo, UserService.ResLoginWithCode.class));
		}

		if (currentPage < 3) {
			setCurrentStep(0);
		} else if (currentPage == 3) {
			setCurrentStep(1);
		} else if (currentPage == 4) {
			setCurrentStep(2);
		}
	}

	@Override
	public String getActionBarTitle() {
		return getString(R.string.setup_progress);
	}

	@Override
	public boolean shouldShowMenu() {
		return false;
	}

	@Override
	public boolean shouldShowBack() {
		return true;
	}

	@Override
	protected boolean isUseEvent() {
		return false;
	}

	public UserService.ResLoginWithCode getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserService.ResLoginWithCode userInfo) {
		this.userInfo = userInfo;

		if (mPagerAdapter.getAccountFoundFragment() != null) {
			mPagerAdapter.getAccountFoundFragment().setUserInfo(userInfo);
		}

		if (mPagerAdapter.getConfirmAccountDetailsFragment() != null) {
			mPagerAdapter.getConfirmAccountDetailsFragment().setUserInfo(userInfo);
		}
	}

	public void setCurrentStep(int index) {
		stepperIndicator.setCurrentStep(index);
	}

	public void restartActivity(int currentPage) {
		Intent intent = getIntent();
		intent.putExtra(EXTRA_CURRENT_PAGE, currentPage);
		intent.putExtra(EXTRA_USER_INFO, new Gson().toJson(userInfo));
		finish();
		startActivity(intent);
	}


	public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		FragmentManager fm;

		FindApplicationFragment fragFindApplication;
		VerifyIdentityFragment fragVerifyIdentity;
		AccountFoundFragment fragAccountFound;
		CreatePasswordFragment fragCreatePassword;
		ConfirmAccountDetailsFragment fragConfirmAccountDetails;
		CreateTeamFragment fragCreateTeam;
//        AddKidAccountsFragment fragAddKidAccounts;

		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
			this.fm = fm;
			fragFindApplication = new FindApplicationFragment();
			fragVerifyIdentity = new VerifyIdentityFragment();
			fragAccountFound = new AccountFoundFragment();
			fragCreatePassword = new CreatePasswordFragment();
			fragConfirmAccountDetails = new ConfirmAccountDetailsFragment();
			fragCreateTeam = new CreateTeamFragment();
//            fragAddKidAccounts = new AddKidAccountsFragment();
		}

		@Override
		public Fragment getItem(int position) {

			switch (position) {
				case 0:
					return fragFindApplication;

				case 1:
					return fragVerifyIdentity;

				case 2:
					return fragAccountFound;

				case 3:
					return fragCreatePassword;

				case 4:
					return fragConfirmAccountDetails;

				case 5:
					return fragCreateTeam;

//                case 6:
//                    return fragAddKidAccounts;

				default:
					return null;
			}
		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}

		public AccountFoundFragment getAccountFoundFragment() {
			return fragAccountFound;
		}

		public ConfirmAccountDetailsFragment getConfirmAccountDetailsFragment() {
			return fragConfirmAccountDetails;
		}
	}
}
