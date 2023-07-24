package org.unicefkidpower.schools;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flurry.android.FlurryAgent;

import org.unicefkidpower.schools.define.CommonUtils;
import org.unicefkidpower.schools.event.EventManager;

import java.lang.reflect.Field;

/**
 * Created by donal_000 on 1/15/2015.
 */
public abstract class SuperFragment extends Fragment {
	private static final Field sChildFragmentManagerField;

	static {
		Field f = null;
		try {
			f = Fragment.class.getDeclaredField("mChildFragmentManager");
			f.setAccessible(true);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		sChildFragmentManagerField = f;
	}

	protected FragmentActivity _parentActivity;
	protected LayoutInflater _inflater;
	protected View mainLayout = null;
	protected boolean bInitialized = false;
	protected int curOrientation;

	public boolean isInitialized() {
		return bInitialized;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (isUseEvent())
			EventManager.sharedInstance().register(this);

		// create root view
		View rootView = setContentLayout(inflater, container, contentLayout());
		assert rootView != null;
		_inflater = inflater;
		mainLayout = rootView.findViewById(R.id.layout_parent);

		if (shouldUpdateNavigationBar() && getActivity() instanceof BaseActivityWithNavBar) {
			BaseActivityWithNavBar parentNavBarActivity = (BaseActivityWithNavBar) getActivity();

			if (getFragmentTitle() != null) {
				parentNavBarActivity.getCustomActionBar().setActionBarTitle(getFragmentTitle());
			} else {
				parentNavBarActivity.getCustomActionBar().setActionBarTitle(
						((BaseActivityWithNavBar) getActivity()).getActionBarTitle()
				);
			}

			parentNavBarActivity.getCustomActionBar().setBackVisibility(
					shouldShowBackButton() ? View.VISIBLE : View.GONE
			);

			parentNavBarActivity.getCustomActionBar().setMenuVisibility(
					shouldShowMenuIcon() ? View.VISIBLE : View.GONE
			);

			parentNavBarActivity.getCustomActionBar().setRightActionItemVisibility(
					shouldShowRightActionItem() ? View.VISIBLE : View.GONE
			);

			parentNavBarActivity.getCustomActionBar().setRightBarActionItemView(rightBarItemView());

			if (getActionBarColor() != -1) {
				parentNavBarActivity.getCustomActionBar().setActionBarColor(getActionBarColor());
			}
		}

		return rootView;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		_parentActivity = getActivity();
	}

	@Override
	public void onDetach() {
		super.onDetach();

		if (sChildFragmentManagerField != null) {
			try {
				sChildFragmentManagerField.set(this, null);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDestroyView() {
		if (isUseEvent())
			EventManager.sharedInstance().unregister(this);
		FlurryAgent.onEndSession(getContext());
		super.onDestroyView();
	}

	public View setContentLayout(LayoutInflater inflater, ViewGroup container, int layout) {
		return inflater.inflate(layout, container, false);
	}

	// layout resource : R.layout.fragment_profile ...
	public abstract int contentLayout();

	public boolean onBackPressed() {
		return false;
	}

	public Context getSafeContext() {
		if (getContext() != null)
			return getContext();

		if (getActivity() != null)
			return getActivity();

		if (BaseActivity.topInstance() != null)
			return BaseActivity.topInstance();

		return null;
	}

	///////////////////// Action Bar Customization ///////////////////////

	protected String getFragmentTitle() {
		return null;
	}

	protected int getActionBarColor() {
		return CommonUtils.getColorFromRes(getActivity().getResources(), R.color.kidpower_light_blue);
	}

	public boolean shouldUpdateNavigationBar() {
		return true;
	}

	protected boolean shouldShowBackButton() {
		return false;
	}

	protected boolean shouldShowMenuIcon() {
		return true;
	}

	protected boolean shouldShowRightActionItem() {
		return false;
	}

	public View rightBarItemView() {
		return null;
	}

	protected void changedLayoutOrientation(int orientation) {
		curOrientation = orientation;
	}

	protected abstract boolean isUseEvent();

}