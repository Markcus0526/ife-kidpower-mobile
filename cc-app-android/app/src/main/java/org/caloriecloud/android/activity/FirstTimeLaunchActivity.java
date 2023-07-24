package org.caloriecloud.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.caloriecloud.android.R;
import org.caloriecloud.android.util.CCStatics;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class FirstTimeLaunchActivity extends AppCompatActivity{

    private final static String TAG = FirstTimeLaunchActivity.class.getSimpleName();

    @OnClick(R.id.joinWithEventCodeButton)
    void joinWithEventCodeButtonPressed() {
        CCStatics.setFirstLaunch(this);
        Intent eventCodeIntent = new Intent(this, LoginActivity.class);
        eventCodeIntent.putExtra("firstTimeJoin", true);
        startActivity(eventCodeIntent);
        finish();

    }

    @OnClick(R.id.firstLaunchButton)
    void firstLaunchButtonPressed() {
        CCStatics.setFirstLaunch(this);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_launch);
        ButterKnife.bind(this);

    }

}
