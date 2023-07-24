package org.caloriecloud.android.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;

import org.caloriecloud.android.R;
import org.caloriecloud.android.activity.RegisterActivity;
import org.caloriecloud.android.ui.CCViewPager;
import org.caloriecloud.android.util.CCApp;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ConnectTrackerFragment extends Fragment {

    @BindView(R.id.stepper)
    ImageView stepper;

    private CCViewPager mViewPager;
    private RegisterActivity mActivity;
    private ReactInstanceManager mSharedReactInstanceManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_connect_tracker, container, false);
        ButterKnife.bind(this, rootView);

        mActivity = (RegisterActivity) getActivity();
        mViewPager = (CCViewPager) mActivity.findViewById(R.id.registerPager);

        mSharedReactInstanceManager = ((CCApp) mActivity.getApplication()).getSharedInstance();

        // STEPPER STEPS HERE
        if (mViewPager.getAdapter().getCount() <= 3 && ((RegisterActivity.ScreenSlidePagerAdapter) mViewPager.getAdapter()).getItem(mViewPager.getAdapter().getCount() - 1).getClass() == OnboardingAuthorizationFragment.class) {
            stepper.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.step12s));
        }
        else if (mViewPager.getAdapter().getCount() < 3) {
            stepper.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.step12s));
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        android.support.v4.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.trackerFragmentContainer, new TrackersSupportFragment()).commit();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mSharedReactInstanceManager != null) {
            mSharedReactInstanceManager.onHostPause(getActivity());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSharedReactInstanceManager != null) {
            mSharedReactInstanceManager.onHostResume(getActivity(), new DefaultHardwareBackBtnHandler() {
                @Override
                public void invokeDefaultOnBackPressed() {
                    getActivity().onBackPressed();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mSharedReactInstanceManager != null) {
            mSharedReactInstanceManager.onHostDestroy(getActivity());
        }
    }
}
