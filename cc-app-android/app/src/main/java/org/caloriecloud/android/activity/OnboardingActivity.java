package org.caloriecloud.android.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.caloriecloud.android.R;
import org.caloriecloud.android.fragment.OnboardingFragment;
import org.caloriecloud.android.model.Challenge;
import org.caloriecloud.android.ui.CCButton;
import org.caloriecloud.android.util.CCApp;
import org.caloriecloud.android.util.CCStatics;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OnboardingActivity extends AppCompatActivity {

    private Challenge mChallenge;
    private ReactInstanceManager mSharedReactInstanceManager;

    @BindView(R.id.skipButton)
    CCButton skipButton;

    @BindView(R.id.nextButton)
    CCButton nextButton;

    @OnClick(R.id.skipButton)
    public void skipButtonPressed() {

        Intent regIntent = new Intent(OnboardingActivity.this, RegisterActivity.class);

        regIntent.putExtra("challenge", mChallenge);
        startActivity(regIntent);
        finish();

    }

    public void updateUI(final int position, final int total) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (position == (total - 1)) {
                    skipButton.setVisibility(View.INVISIBLE);
                    nextButton.setText(R.string.get_started);
                }
                else {
                    if (skipButton.getVisibility() == View.INVISIBLE) {
                        skipButton.setVisibility(View.VISIBLE);
                    }

                    if (!nextButton.getText().equals("Next")) {
                        nextButton.setText(R.string.next);
                    }

                }
            }
        });

    }

    @OnClick(R.id.nextButton)
    void nextButtonPressed() {
        mSharedReactInstanceManager.getCurrentReactContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("OnboardingEvent", null);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        ButterKnife.bind(this);

        mChallenge = (Challenge) getIntent().getSerializableExtra("challenge");
        mSharedReactInstanceManager = ((CCApp) getApplication()).getSharedInstance();

        android.app.FragmentManager fragmentManager = getFragmentManager();
        android.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment fragment =  fragmentManager.findFragmentByTag(CCStatics.FRAGMENT_ONBOARDING);

        if (fragment == null) {
            fragment = new OnboardingFragment();
        }

        Bundle props = new Bundle();
        props.putString("startDate", mChallenge.getStartDate());
        props.putString("endDate", mChallenge.getEndDate());
        props.putString("imageSrc", (!mChallenge.getImageSrc().equals("")) ? mChallenge.getImageSrc() : mChallenge.getOrganization().getImageSrc());
        props.putString("name", mChallenge.getName());
        props.putString("impactMultiplier", mChallenge.getImpactMultiplier());
        props.putString("brand", mChallenge.getBrand());
        props.putString("organizationName", mChallenge.getOrganization().getName());

        if (!fragment.isAdded()) {
            fragment.setArguments(props);
        }

        fragmentTransaction.replace(R.id.frameContainer, fragment, CCStatics.FRAGMENT_ONBOARDING).commit();
    }
}
