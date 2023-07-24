package org.unicefkidpower.kid_power.View.Activities.Main.Friends;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.KPHAnalyticsService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHFollowService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.Structure.KPHBlock;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserSummary;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Adapters.FriendsTabAdapter;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ruifeng Shi on 9/4/2015.
 */
public class FriendsMainFragment extends SuperFragment {
	// UI Controls
	private View								contentView			= null;
	private TabLayout							slidingTabLayout	= null;
	private ViewPager							contentPager		= null;
	private FriendsTabAdapter					mTabAdapter			= null;

	// Member Variables
	private IntentFilter						intentFilter		= null;

	private int									nCurrentTabItem		= 0;

	public static List<KPHUserSummary>			followingsArray		= null;
	public static List<KPHUserSummary>			followersArray		= null;
	public static List<KPHBlock>				blockedArray		= null;

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (getActivity() == null) {
				// Already detached
				return;
			}

			String action = intent.getAction();
			switch (action) {
				case KPHBroadcastSignals.BROADCAST_SIGNAL_SHOULD_UPDATE_FRIENDS_TABS:
					updateTabAdapter();
					break;
			}
		}
	};


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_UI_FRIENDS);

		if (intentFilter == null) {
			intentFilter = new IntentFilter();
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_SHOULD_UPDATE_FRIENDS_TABS);
			LocalBroadcastManager.getInstance(getSafeContext()).registerReceiver(receiver, intentFilter);
		}


		slidingTabLayout = (TabLayout) contentView.findViewById(R.id.sliding_tab_bar);
		slidingTabLayout.setSelectedTabIndicatorColor(UIManager.sharedInstance().getColor(R.color.kph_color_green));
		slidingTabLayout.setSelectedTabIndicatorHeight(getSafeContext().getResources().getDimensionPixelSize(R.dimen.tab_indicator_height));
		slidingTabLayout.setAlpha(0.0f);

		contentPager = (ViewPager) contentView.findViewById(R.id.content_pager);

		KPHUserData userData = KPHUserService.sharedInstance().getUserData();
		followingsArray = userData.getFollowings();
		followersArray = userData.getFollowers();

		setupTabAdapter();

		return contentView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_friends;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		LocalBroadcastManager.getInstance(getSafeContext()).unregisterReceiver(receiver);
		intentFilter = null;
	}

	private void updateTabAdapter() {
		if (mTabAdapter == null) {
			setupTabAdapter();
			return;
		}

		if (getParentActivity() == null)
			return;

		mTabAdapter = new FriendsTabAdapter(
				getChildFragmentManager(),
				followingsArray,
				followersArray,
				blockedArray
		);

		contentPager.setAdapter(mTabAdapter);
		contentPager.setCurrentItem(nCurrentTabItem);

		setupTablayoutWithViewPager();
	}


	private void setupTablayoutWithViewPager() {
		slidingTabLayout.setupWithViewPager(contentPager);
		slidingTabLayout.setAlpha(1.0f);

		Typeface typeface = Typeface.createFromAsset(getParentActivity().getAssets(), "fonts/PFDinDisplayPro_regular.otf");
		ViewGroup tabLayoutContainer = (ViewGroup)slidingTabLayout.getChildAt(0);
		for (int i = 0; i < slidingTabLayout.getTabCount(); i++) {
			TextView view = (TextView) ((ViewGroup)tabLayoutContainer.getChildAt(i)).getChildAt(1);
			view.setTypeface(typeface, Typeface.BOLD);
		}
	}


	private void setupTabAdapter() {
		if (getParentActivity() == null)
			return;

		showProgressDialog();
		KPHFollowService.sharedInstance().fetchBlockedList(new onActionListener() {
			@Override
			public void completed(Object object) {
				dismissProgressDialog();
				if (object == null)
					return;

				if (object != null)
					blockedArray = (ArrayList<KPHBlock>) object;
				else
					blockedArray = new ArrayList<>();

				mTabAdapter = new FriendsTabAdapter(
						getChildFragmentManager(),
						followingsArray,
						followersArray,
						blockedArray
				);

				contentPager.setAdapter(mTabAdapter);
				contentPager.setCurrentItem(nCurrentTabItem);
				contentPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
					@Override
					public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
					@Override
					public void onPageSelected(int position) {
						nCurrentTabItem = position;
					}
					@Override
					public void onPageScrollStateChanged(int state) {}
				});

				setupTablayoutWithViewPager();
			}
			@Override
			public void failed(int code, String message) {
				dismissProgressDialog();
			}
		});
	}

}
