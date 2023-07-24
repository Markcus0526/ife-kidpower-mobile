package org.caloriecloud.android.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.caloriecloud.android.R;
import org.caloriecloud.android.activity.RegisterActivity;
import org.caloriecloud.android.model.Challenge;
import org.caloriecloud.android.ui.CCViewPager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChallengeInfoFragment extends Fragment {

    CCViewPager mViewPager;

    @BindView(R.id.challengeName)
    TextView challengeName;

    @BindView(R.id.challengeDates)
    TextView challengeDates;

    @BindView(R.id.challengeDonationRateValues)
    TextView challengeDonationRate;

    @BindView(R.id.challengeSponsor)
    TextView challengeSponsorInfo;

    @BindView(R.id.challengeLogo)
    ImageView challengeLogo;

    @BindView(R.id.challengeDisclaimer)
    TextView challengeDisclaimer;

    @OnClick(R.id.joinNowButton)
    void joinChallengeButtonPressed() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
    }

    public final static String TAG = ChallengeInfoFragment.class.getSimpleName();
    private Challenge mCurrentChallenge;
    private RegisterActivity mActivity;

    public static ChallengeInfoFragment newInstance(Challenge currentChallenge) {
        ChallengeInfoFragment fragment = new ChallengeInfoFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_challenge_info, container, false);
        ButterKnife.bind(this, rootView);

        mActivity = (RegisterActivity) getActivity();
        mViewPager = (CCViewPager) mActivity.findViewById(R.id.registerPager);


        /* Modify views to display challenge information */
        challengeName.setText(mCurrentChallenge.getName());

        SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        SimpleDateFormat clientDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

        try {
            Date startDate = serverDateFormat.parse(mCurrentChallenge.getStartDate());
            Date endDate = serverDateFormat.parse(mCurrentChallenge.getEndDate());

            String challengeDatesString = clientDateFormat.format(startDate) + " - " + clientDateFormat.format(endDate);
            challengeDates.setText(challengeDatesString);

        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        if (mCurrentChallenge.getBrand().equals("unicef")) {
            challengeSponsorInfo.setText(String.format(getString(R.string.challenge_sponsor_unicef), mCurrentChallenge.getOrganization().getName(), mCurrentChallenge.getImpactMultiplier()));
            challengeDonationRate.setText(String.format(getString(R.string.donation_rate_unicef), mCurrentChallenge.getImpactMultiplier()));
            challengeDisclaimer.setText(R.string.challenge_disclaimer_unicef);
        }
        else {
            challengeSponsorInfo.setText(String.format(getString(R.string.challenge_sponsor), mCurrentChallenge.getOrganization().getName(), mCurrentChallenge.getImpactMultiplier()));
            challengeDonationRate.setText(String.format(getString(R.string.donation_rate), mCurrentChallenge.getImpactMultiplier()));
        }


        String challengeLogoSrc = mCurrentChallenge.getOrganization().getImageSrc();

        if (!challengeLogoSrc.equals("")) {
            Picasso.with(mActivity).load(challengeLogoSrc).into(challengeLogo);
        }

        return rootView;
    }
}
