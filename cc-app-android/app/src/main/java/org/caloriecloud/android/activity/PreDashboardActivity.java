package org.caloriecloud.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.caloriecloud.android.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class PreDashboardActivity extends AppCompatActivity {

    private static final String TAG = PreDashboardActivity.class.getSimpleName();

    @OnClick(R.id.visitDashboardButton)
    void visitDashboardButtonPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_dashboard);
        ButterKnife.bind(this);
    }
}
