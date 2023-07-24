package org.caloriecloud.android.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.caloriecloud.android.R;
import org.caloriecloud.android.util.CCStatics;

public class SplashActivity extends AppCompatActivity{

    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mContext = this;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = null;

                if (CCStatics.isFirstLaunch(mContext)) {
                    mainIntent = new Intent(mContext, FirstTimeLaunchActivity.class);
                }
                else {

                    if (CCStatics.getSavedUser(mContext) != null) {
                        mainIntent = new Intent(mContext, MainActivity.class);
                    }
                    else {
                        mainIntent = new Intent(mContext, LoginActivity.class);
                    }

                }

                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, 500);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }
}
