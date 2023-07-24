package org.caloriecloud.android.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.caloriecloud.android.R;
import org.caloriecloud.android.ui.CCTextView;
import org.caloriecloud.android.ui.CCToolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChallengeInfoActivity extends AppCompatActivity {

    @BindView(R.id.challengeName)
    CCTextView challengeName;

    @BindView(R.id.challengeDates)
    CCTextView challengeDates;

    @BindView(R.id.challengeDonationRateValues)
    CCTextView challengeDonationRate;

    @BindView(R.id.challengeSponsor)
    CCTextView challengeSponsorInfo;

    @BindView(R.id.challengeLogo)
    ImageView challengeLogo;

    @BindView(R.id.challengeDisclaimer)
    CCTextView challengeDisclaimer;

    CCToolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_challenge_info);

        toolbar = findViewById(R.id.toolbar);

        ButterKnife.bind(this);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        HashMap<String, Object> mCurrentChallenge = (HashMap<String, Object>) getIntent().getSerializableExtra("challenge");

        /* Modify views to display challenge information */
        challengeName.setText(mCurrentChallenge.get("challengeName").toString());

        SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        SimpleDateFormat clientDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

        try {
            Date startDate = serverDateFormat.parse(mCurrentChallenge.get("startDate").toString());
            Date endDate = serverDateFormat.parse(mCurrentChallenge.get("endDate").toString());

            String challengeDatesString = clientDateFormat.format(startDate) + " - " + clientDateFormat.format(endDate);
            challengeDates.setText(challengeDatesString);

        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        if (mCurrentChallenge.get("brand").toString().equals("unicef")) {
            challengeSponsorInfo.setText(String.format(getString(R.string.challenge_sponsor_unicef), mCurrentChallenge.get("orgName").toString(), mCurrentChallenge.get("impactMultiplier").toString()));
            challengeDonationRate.setText(String.format(getString(R.string.donation_rate_unicef), mCurrentChallenge.get("impactMultiplier").toString()));
            challengeDisclaimer.setText(R.string.challenge_disclaimer_unicef);
        }
        else {
            challengeSponsorInfo.setText(String.format(getString(R.string.challenge_sponsor), mCurrentChallenge.get("orgName").toString(), mCurrentChallenge.get("impactMultiplier").toString()));
            challengeDonationRate.setText(String.format(getString(R.string.donation_rate), mCurrentChallenge.get("impactMultiplier").toString()));
        }


        String challengeLogoSrc = (!mCurrentChallenge.get("challengePic").toString().equals("")) ? mCurrentChallenge.get("challengePic").toString() : mCurrentChallenge.get("orgLogo").toString();

        if (!challengeLogoSrc.equals("")) {
            Picasso.with(this).load(challengeLogoSrc).into(challengeLogo);
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }
}
