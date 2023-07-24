package org.caloriecloud.android.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.caloriecloud.android.R;
import org.caloriecloud.android.activity.RegisterActivity;
import org.caloriecloud.android.model.Challenge;
import org.caloriecloud.android.ui.CCViewPager;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class OnboardingAuthorizationFragment extends Fragment {

    CCViewPager mViewPager;
    public final static String TAG = ChallengeInfoFragment.class.getSimpleName();
    private Challenge mCurrentChallenge;
    private RegisterActivity mActivity;

    @OnClick(R.id.authorizeButton)
    void authorizeButtonPressed() {
        mActivity.authorizeButtonPressed();
    }

    public static OnboardingAuthorizationFragment newInstance(Challenge currentChallenge) {
        OnboardingAuthorizationFragment fragment = new OnboardingAuthorizationFragment();
        Bundle bundle = fragment.getArguments();

        if (bundle == null) {
            bundle = new Bundle();
        }

        bundle.putSerializable("challenge", currentChallenge);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentChallenge = (Challenge) getArguments().getSerializable("challenge");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_onboarding_authorization, container, false);
        ButterKnife.bind(this, rootView);

        mActivity = (RegisterActivity) getActivity();
        mViewPager = (CCViewPager) mActivity.findViewById(R.id.registerPager);

        return rootView;
    }
}
