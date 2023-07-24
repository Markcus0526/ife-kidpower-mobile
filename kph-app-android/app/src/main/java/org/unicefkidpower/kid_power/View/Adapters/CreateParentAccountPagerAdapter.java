package org.unicefkidpower.kid_power.View.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.unicefkidpower.kid_power.Misc.Config;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.CreateParentAccountEmailGenderBirthFragment;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.CreateParentAccountUsernamePasswordFragment;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.CreateParentAccountFinishFragment;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.CreateParentAccountTypeFragment;
import org.unicefkidpower.kid_power.View.Activities.Onboarding.CreateParentAccountStartFragment;

import static org.unicefkidpower.kid_power.View.Activities.Onboarding.CreateParentAccountDialogFragment.PARENT_ACCOUNT_PAGE_ACCOUNT_TYPE;
import static org.unicefkidpower.kid_power.View.Activities.Onboarding.CreateParentAccountDialogFragment.PARENT_ACCOUNT_PAGE_EMAIL_GENDER_BIRTH;
import static org.unicefkidpower.kid_power.View.Activities.Onboarding.CreateParentAccountDialogFragment.PARENT_ACCOUNT_PAGE_FINISH;
import static org.unicefkidpower.kid_power.View.Activities.Onboarding.CreateParentAccountDialogFragment.PARENT_ACCOUNT_PAGE_START;
import static org.unicefkidpower.kid_power.View.Activities.Onboarding.CreateParentAccountDialogFragment.PARENT_ACCOUNT_PAGE_USERNAME_PWD;


/**
 * Created by Ruifeng Shi on 12/16/2016.
 */
public class CreateParentAccountPagerAdapter extends FragmentStatePagerAdapter {
	public CreateParentAccountStartFragment					createParentAccountStartFragment;
	public CreateParentAccountTypeFragment					createParentAccountTypeFragment;
	public CreateParentAccountUsernamePasswordFragment		createParentAccountUsernamePasswordFragment;
	public CreateParentAccountEmailGenderBirthFragment		createParentAccountEmailGenderBirthFragment;
	public CreateParentAccountFinishFragment				createParentAccountFinishFragment;


	public CreateParentAccountPagerAdapter(FragmentManager fm) {
		super(fm);

		createParentAccountStartFragment				= new CreateParentAccountStartFragment();
		createParentAccountTypeFragment					= new CreateParentAccountTypeFragment();
		createParentAccountUsernamePasswordFragment		= new CreateParentAccountUsernamePasswordFragment();
		createParentAccountEmailGenderBirthFragment		= new CreateParentAccountEmailGenderBirthFragment();
		createParentAccountFinishFragment				= new CreateParentAccountFinishFragment();
	}


	@Override
	public Fragment getItem(int position) {
		if (position == PARENT_ACCOUNT_PAGE_START) {
			return createParentAccountStartFragment;
		} else if (position == PARENT_ACCOUNT_PAGE_ACCOUNT_TYPE) {
			return createParentAccountTypeFragment;
		} else if (position == PARENT_ACCOUNT_PAGE_USERNAME_PWD) {
			return createParentAccountUsernamePasswordFragment;
		} else if (position == PARENT_ACCOUNT_PAGE_EMAIL_GENDER_BIRTH) {
			return createParentAccountEmailGenderBirthFragment;
		} else if (position == PARENT_ACCOUNT_PAGE_FINISH) {
			return createParentAccountFinishFragment;
		}

		return null;
	}


	@Override
	public int getCount() {
		if (Config.USE_FACEBOOK_FEATURE)
			return 5;
		else
			return 4;
	}


}
