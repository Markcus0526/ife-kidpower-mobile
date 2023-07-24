package org.caloriecloud.android.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;

import org.caloriecloud.android.util.CCApp;

public class OnboardingFragment extends android.app.Fragment {

    private ReactRootView rootView;
    private ReactInstanceManager mSharedReactInstanceManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = new ReactRootView(getActivity());
        mSharedReactInstanceManager = ((CCApp) getActivity().getApplication()).getSharedInstance();
        rootView.startReactApplication(mSharedReactInstanceManager, "Onboarding", getArguments());

        return rootView;
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
