package org.caloriecloud.android.rn;

import android.content.Intent;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import org.caloriecloud.android.activity.ChallengeInfoActivity;
import org.caloriecloud.android.activity.MainActivity;
import org.caloriecloud.android.activity.OnboardingActivity;
import org.caloriecloud.android.activity.RegisterActivity;

public class ReactNativeEventEmitter extends ReactContextBaseJavaModule {

   private  ReactApplicationContext mApplicationContext;

    @Override
    public String getName() {
        return "ReactNativeEventEmitter";
    }

    public ReactNativeEventEmitter(ReactApplicationContext reactContext) {
        super(reactContext);
        this.mApplicationContext = reactContext;
    }

    @ReactMethod
    public void loadChallengeInfo(int reactTag, ReadableMap challenge) {
        Intent challengeInfoIntent = new Intent(mApplicationContext, ChallengeInfoActivity.class);
        challengeInfoIntent.putExtra("challenge", challenge.toHashMap());
        challengeInfoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mApplicationContext.startActivity(challengeInfoIntent);

        //MainActivity activity = (MainActivity) getReactApplicationContext().getCurrentActivity();
        //activity.loadChallengeInfo(challenge);

    }

    @ReactMethod
    public void updateUI(int reactTag, int index, int total) {
        OnboardingActivity activity = (OnboardingActivity) mApplicationContext.getCurrentActivity();
        activity.updateUI(index, total);

    }

    @ReactMethod void dismissPresentedViewController(int reactTag) {

        OnboardingActivity activity = (OnboardingActivity) mApplicationContext.getCurrentActivity();
        activity.skipButtonPressed();
    }

    @ReactMethod void loadHealthAuthScreen(int reactTag, boolean fromOnboarding) {

        if (!fromOnboarding) {
            MainActivity activity = (MainActivity) mApplicationContext.getCurrentActivity();
            activity.authorizeButtonPressed();
        }
        else {
            RegisterActivity activity = (RegisterActivity) mApplicationContext.getCurrentActivity();
            activity.authorizeButtonPressed();
        }

    }

}
