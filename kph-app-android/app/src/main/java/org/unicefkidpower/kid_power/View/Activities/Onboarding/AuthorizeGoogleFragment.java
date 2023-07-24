package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Ruifeng Shi on 2/7/2017.
 */

public class AuthorizeGoogleFragment extends SuperFragment {
	private String				username			= "";
	private String				avatarId			= "";

	private ImageView			avatarImageView		= null;
	private KPHTextView			usernameTextView	= null;
	private KPHButton			closeButton			= null;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		initControls(rootView);
		return rootView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_authorize_googlefit;
	}

	private void initControls(View rootView) {
		avatarImageView = (ImageView)rootView.findViewById(R.id.ivAvatar);
		usernameTextView = (KPHTextView)rootView.findViewById(R.id.txt_username);

		Drawable avatarDrawable = KPHUserService.sharedInstance().getAvatarDrawable(avatarId);
		if (avatarDrawable != null)
			avatarImageView.setImageDrawable(avatarDrawable);

		usernameTextView.setText(username);

		closeButton = (KPHButton)rootView.findViewById(R.id.btn_close);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedClose();
			}
		});
	}


	private void onClickedClose() {
		// Go to settings page.
		Intent viewIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
		viewIntent.setData(uri);
		startActivity(viewIntent);
	}


	public void setData(String username, String avatarId) {
		this.username = username;
		this.avatarId = avatarId;
	}

}
