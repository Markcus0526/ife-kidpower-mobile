package org.caloriecloud.android.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;

import org.caloriecloud.android.util.CCApp;
import org.caloriecloud.android.util.CCStatics;

public class DashboardFragment extends Fragment {

    private ReactInstanceManager mSharedReactInstanceManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ReactRootView rootView = new ReactRootView(getActivity());
        Bundle props = new Bundle();

        mSharedReactInstanceManager = ((CCApp) getActivity().getApplication()).getSharedInstance();

        props.putString("accessToken", CCStatics.getSavedUser(getActivity()).getAccessToken());
        props.putInt("userId", CCStatics.getSavedUser(getActivity()).getUserId());
        props.putString("screenName", CCStatics.getSavedUser(getActivity()).getScreenName());
        props.putString("email", CCStatics.getSavedUser(getActivity()).getEmail());
        props.putString("baseURL", CCStatics.BASE_URL);

        rootView.startReactApplication(mSharedReactInstanceManager, "Dashboard", props);

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
