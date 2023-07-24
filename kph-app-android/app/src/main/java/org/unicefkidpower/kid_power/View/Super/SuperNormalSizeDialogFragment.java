package org.unicefkidpower.kid_power.View.Super;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.R;


/**
 * Created by Ruifeng Shi on 2/28/2017.
 * If you're gonna use this class, DO NOT FORGET to place the RelativeLayout at the root.
 * And place one layout inside it whose id is 'layout_parent'
 */
public abstract class SuperNormalSizeDialogFragment extends SuperDialogFragment {
	protected View				parentLayout = null;			// Layout whose id is 'parent_layout'

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		parentLayout = rootView.findViewById(R.id.layout_parent);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		resizeDialog(getDialog(), getParentLayout());
	}

	public View getParentLayout() {
		return parentLayout;
	}

	public static void resizeDialog(Dialog dialog, View parentLayout) {
		if (dialog != null) {
			Context context = dialog.getContext();

			Point screenSize = ResolutionSet.getScreenSize(context, false);
			int width = screenSize.x - context.getResources().getDimensionPixelSize(R.dimen.dimen_16dp) * 2;

			dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.MATCH_PARENT);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

			int topBottomMargin = context.getResources().getDimensionPixelSize(R.dimen.dimen_16dp);

			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			params.setMargins(0, topBottomMargin, 0, topBottomMargin);
			parentLayout.setLayoutParams(params);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		resizeDialog(getDialog(), getParentLayout());
	}

}
