package org.unicefkidpower.kid_power.View.Activities.Main.Passport;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.unicefkidpower.kid_power.View.CustomControls.KPHPageControl;
import org.unicefkidpower.kid_power.Model.Structure.KPHMission;
import org.unicefkidpower.kid_power.Model.Structure.KPHMissionInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMissionStats;
import org.unicefkidpower.kid_power.View.Super.SuperNormalSizeDialogFragment;
import org.unicefkidpower.kid_power.Model.MicroService.KPHMissionService;
import org.unicefkidpower.kid_power.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Dayong Li on 5/5/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class ProfileMissionAutoStartFragment extends SuperNormalSizeDialogFragment {
	private List<KPHMission>			userMissions			= new ArrayList<>();
	private boolean						autoStarted				= false;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(getDialogFragmentStyle(), R.style.KidPowerDialogStyle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View contentView = super.onCreateView(inflater, container, savedInstanceState);

		userMissions = new ArrayList<>();

		List<KPHMission> missions = KPHMissionService.sharedInstance().getSortedNewMissionsList();
		if (missions != null) {
			userMissions.addAll(missions);
			if (userMissions.size() > 0) {
				contentView.findViewById(R.id.btnSkip).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ProfileMissionAutoStartFragment.this.dismiss();
					}
				});
			}
		}

		ViewPager vpMissionInfo = (ViewPager) contentView.findViewById(R.id.vpMissionInfo);
		MissionInfoPagerAdapter pagerAdapter = new MissionInfoPagerAdapter(getChildFragmentManager());
		vpMissionInfo.setAdapter(pagerAdapter);

		final KPHPageControl pcMIssion = (KPHPageControl) contentView.findViewById(R.id.pcMIssion);
		pcMIssion.setPageCount(userMissions.size());
		pcMIssion.setCurrentPage(0);

		vpMissionInfo.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
			@Override
			public void onPageSelected(int position) {
				pcMIssion.setCurrentPage(position);
			}
			@Override
			public void onPageScrollStateChanged(int state) {}
		});

		return contentView;
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_profile_mission_autostart;
	}


	public void setAutoStartFlag(boolean flag) {
		autoStarted = flag;
	}


	class MissionInfoPagerAdapter extends FragmentStatePagerAdapter {
		public MissionInfoPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			KPHMission missionItem = userMissions.get(position);
			KPHUserMissionStats userMissionStats = KPHMissionService.sharedInstance().userMissionStateById(missionItem.getId());

			long missionId = missionItem.getId();
			KPHMissionInformation missionInfo = KPHMissionService.sharedInstance().getMissionInformationById(missionId);

			ProfileMissionInfoFragment fragment = new ProfileMissionInfoFragment();
			if (KPHMissionService.sharedInstance().isAlexMorganMission(missionId)) {
				fragment.setNeedCustomTitle(autoStarted);
			}
			fragment.setMissionInformation(missionInfo, userMissionStats, ProfileMissionAutoStartFragment.this);

			return fragment;
		}

		@Override
		public int getCount() {
			return userMissions.size();
		}
	}

	@Override
	public void startAction() {
		// No action. Do nothing
	}

}
