package org.caloriecloud.android.util;

import android.app.Application;

import com.airbnb.android.react.lottie.LottiePackage;
import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.shell.MainReactPackage;
import com.microsoft.codepush.react.CodePush;
import com.zendesk.sdk.model.access.AnonymousIdentity;
import com.zendesk.sdk.network.impl.ZendeskConfig;

import org.caloriecloud.android.BuildConfig;
import org.caloriecloud.android.R;
import org.caloriecloud.android.rn.CCReactPackage;
import org.caloriecloud.android.sync.ActivityDataSyncJobCreator;

import java.util.Arrays;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class CCApp extends Application {

    private ReactInstanceManager sharedReactInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        JobManager.create(this).addJobCreator(new ActivityDataSyncJobCreator());

        sharedReactInstance = ReactInstanceManager.builder()
                .setApplication(CCApp.this)
                .setBundleAssetName("index.android.bundle")
                .setJSMainModulePath("index")
                .addPackages(getPackages())
                .setJSBundleFile(CodePush.getJSBundleFile())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setUseSeparateUIBackgroundThread(true)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();

        // Initialize Zendesk
        ZendeskConfig.INSTANCE.init(this, getString(R.string.ZENDESK_URL), getString(R.string.ZENDESK_APP_ID), getString(R.string.ZENDESK_CLIENT_ID));
        ZendeskConfig.INSTANCE.setIdentity(new AnonymousIdentity.Builder().build());

        // Initialize Crashlytics on Production Builds
        if (!BuildConfig.FLAVOR.contains("staging")) {
            Fabric.with(this, new Crashlytics());
        }
    }

    public ReactInstanceManager getSharedInstance() {
        return this.sharedReactInstance;
    }

    protected List<ReactPackage> getPackages() {
        return Arrays.asList(
                new MainReactPackage(),
                new CodePush((!BuildConfig.FLAVOR.contains("staging")) ? "SYOpyHYYWx0LzDCzZ1rDwuX2_K0Jac0fea22-7301-48f3-bab4-2677fb7e6567" : "_jYWuIc6ytQVSBPVuxaj-KcvvKdIac0fea22-7301-48f3-bab4-2677fb7e6567", getApplicationContext(), BuildConfig.DEBUG),
                new LottiePackage(),
                new CCReactPackage()
        );
    }

}
