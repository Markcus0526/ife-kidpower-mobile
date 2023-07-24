package org.unicefkidpower.kid_power.View.Activities.Main.Friends;

import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserSummary;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHEditText;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperFragment;

/**
 * Created by Ruifeng Shi on 9/12/2015.
 */
public class FollowSomeoneFragment extends SuperFragment {
	private View				contentView				= null;
	private KPHEditText			editUsername			= null;
	private KPHTextView			txtNotification			= null;
	private KPHButton			btnGo					= null;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		editUsername = (KPHEditText) contentView.findViewById(R.id.edit_username);
		editUsername.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO) {
					performSearch();
				}
				return false;
			}
		});

		txtNotification = (KPHTextView) contentView.findViewById(R.id.txt_notification);
		txtNotification.setVisibility(View.INVISIBLE);

		btnGo = (KPHButton) contentView.findViewById(R.id.btn_go);
		btnGo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onGoButtonClicked();
			}
		});

		return contentView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_follow_someone;
	}


	/**
	 * Called when Go button has been clicked
	 */
	private void onGoButtonClicked() {
		editUsername.onEditorAction(EditorInfo.IME_ACTION_GO);
	}

	/**
	 * Search user with the search text, and after that perform corresponding actions
	 */
	public void performSearch() {
		//Search user with username
		final String sUsername = editUsername.getText().toString();
		if (sUsername.length() == 0) {
			return;
		}

		showOnSearchingNotification();
		KPHUserService.sharedInstance().searchUsers(sUsername, new onActionListener() {
			@Override
			public void completed(Object object) {
				enableControls(true);

				KPHUserSummary foundUser = (KPHUserSummary) object;

				FriendDetailFragment friendDetailFragment = new FriendDetailFragment();
				friendDetailFragment.setData(foundUser, false);

				if (getParentActivity() != null)
					getParentActivity().showNewFragment(friendDetailFragment);
			}

			@Override
			public void failed(int code, String message) {
				enableControls(true);
				showCannotFollowSomeoneNotification(message);
			}
		});
	}

	private void enableControls(boolean flag) {
		if (flag)
			txtNotification.setVisibility(View.INVISIBLE);

		btnGo.setEnabled(flag);
		editUsername.setEnabled(flag);
	}

	public void showOnSearchingNotification() {
		enableControls(false);

		txtNotification.setText(R.string.searching);
		txtNotification.setGravity(Gravity.CENTER_HORIZONTAL);
		txtNotification.setVisibility(View.VISIBLE);
	}

	public void showCannotFollowSomeoneNotification(String sNotification) {
		txtNotification.setText(sNotification);
		txtNotification.setGravity(Gravity.LEFT);
		txtNotification.setVisibility(View.VISIBLE);
	}
}
