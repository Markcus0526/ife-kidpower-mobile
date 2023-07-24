package org.unicefkidpower.schools.support;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;

import com.flurry.android.FlurryAgent;
import com.zendesk.sdk.model.access.AnonymousIdentity;
import com.zendesk.sdk.model.access.Identity;
import com.zendesk.sdk.model.settings.ContactUsSettings;
import com.zendesk.sdk.network.impl.ZendeskConfig;

import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.SuperFragment;
import org.unicefkidpower.schools.define.config;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.model.Team;
import org.unicefkidpower.schools.model.User;
import org.unicefkidpower.schools.model.UserManager;
import org.unicefkidpower.schools.model.Utils;
import org.unicefkidpower.schools.ui.KPButton;
import org.unicefkidpower.schools.ui.KPTextView;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Ruifeng Shi on 1/16/2017.
 */

public class SupportFragment extends SuperFragment implements View.OnClickListener {
	public static final int PERMISSION_REQUEST_STORAGE = 12345;

	protected ArrayList<Team> 				_teams;
	protected LinearLayout 					llTeamsContainer;

	private KPButton 						btnHelpCenter;
	private KPButton 						btnContactUs;
	private KPTextView 						txtAppVersion;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		FlurryAgent.onStartSession(getContext(), "Support Fragment");

		btnHelpCenter = (KPButton) rootView.findViewById(R.id.btn_helpcenter);
		btnHelpCenter.setGravity(Gravity.CENTER);
		btnHelpCenter.setOnClickListener(this);

		btnContactUs = (KPButton) rootView.findViewById(R.id.btn_contactus);
		btnContactUs.setGravity(Gravity.CENTER);
		btnContactUs.setOnClickListener(this);

		txtAppVersion = (KPTextView) rootView.findViewById(R.id.txt_app_version);
		txtAppVersion.setText(getActivity().getString(
				R.string.app_version_number,
				Utils.getVersionName(getActivity()),
				Utils.getVersionCode(getActivity())
		));

		return rootView;
	}

	@Override
	protected String getFragmentTitle() {
		return getString(R.string.support_title);
	}

	@Override
	protected boolean isUseEvent() {
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_helpcenter:
				onHelpCenterButtonClicked();
				break;
			case R.id.btn_contactus:
				onContactUsButtonClicked();
				break;
		}
	}


	private void onHelpCenterButtonClicked() {
		// goto helper center

		if (true) {
			Intent intent = new Intent(getActivity(), HelperCenterActivity.class);
			getActivity().startActivity(intent);
		} else {
			//SupportActivity.Builder builder = new SupportActivity.Builder();
			//builder.withCategoriesCollapsed(true).show(getActivity());
		}
	}


	private void onContactUsButtonClicked() {
/*
		Intent contactUsIntent = new Intent(getActivity(), ContactZendeskActivity.class);
		startActivity(contactUsIntent);
*/
		checkStoragePermission();
	}

	private void checkStoragePermission() {
		if (getActivity() != null &&
				ContextCompat.checkSelfPermission(getActivity(),
						android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
						!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(getActivity(),
					new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
					PERMISSION_REQUEST_STORAGE);
		} else {
			sendEMailToSupportTeam(true);
			Logger.log("Help", "Have already storage permission");
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == PERMISSION_REQUEST_STORAGE) {
			if (getActivity() != null &&
					ContextCompat.checkSelfPermission(getActivity(),
							android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
							== PackageManager.PERMISSION_GRANTED) {
				sendEMailToSupportTeam(true);
			} else {
				sendEMailToSupportTeam(false);
			}
			return;
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	void sendEMailToSupportTeam(boolean addLog) {
		if (getActivity() == null)
			return;

		// goto cantact us
		Intent intent = new Intent(getActivity(), ContactUsActivity.class);
		intent.putExtra(ContactUsActivity.EXTRA_ADD_LOG_FILE, addLog);
		getActivity().startActivity(intent);
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_support;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		initZendesk(context);
	}

	protected void initZendesk(Context context) {
		ZendeskConfig.INSTANCE.setDeviceLocale(new Locale("nl"));
		ZendeskConfig.INSTANCE.init(context, config.ZENDESK_URL, config.ZENDESK_APPID, config.ZENDESK_CLIENTID);

		User user = UserManager.sharedInstance()._currentUser;
		Identity identify;
		identify = new AnonymousIdentity.Builder()
				.withEmailIdentifier(user._email)
				.withNameIdentifier(user._firstName + " " + user._lastName)
				.build();
		ZendeskConfig.INSTANCE.setIdentity(identify);
	}

}
