package org.unicefkidpower.schools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.unicefkidpower.schools.ui.KPTextView;

/**
 * Created by donal_000 on 1/5/2015.
 */
public class ActionBar implements View.OnClickListener {
	public static final String TAG = "Action Bar";

	private Context context;
	private View view;
	private LayoutInflater inflater;
	private ActionBarDelegate delegate;

	private KPTextView txtTitle;
	private LinearLayout layoutBack;
	private ImageButton btnMenu;
	private RelativeLayout layoutRightActionItem;

	/**
	 * Constructor method
	 *
	 * @param context  Context
	 * @param delegate Callback Object
	 */
	public ActionBar(Context context, ActionBarDelegate delegate) {
		this.context = context;
		this.delegate = delegate;

		inflater = LayoutInflater.from(context);
		view = inflater.inflate(R.layout.action_bar, null);

		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.getResources().getDimensionPixelSize(R.dimen.kidpower_navbarheight));
		view.setLayoutParams(params);

		txtTitle = (KPTextView) view.findViewById(R.id.txt_title);
		txtTitle.setText(delegate.getActionBarTitle());
		txtTitle.setMaxWidth(ResolutionSet.getScreenSize(context, false, true).x * 2 / 5);

		layoutBack = (LinearLayout) view.findViewById(R.id.layout_back);
		layoutBack.setOnClickListener(this);

		btnMenu = (ImageButton) view.findViewById(R.id.btn_menu);
		btnMenu.setOnClickListener(this);

		layoutRightActionItem = (RelativeLayout) view.findViewById(R.id.layout_right_action_item);

		btnMenu.setVisibility(delegate.shouldShowMenu() ? View.VISIBLE : View.GONE);
		layoutBack.setVisibility(delegate.shouldShowBack() ? View.VISIBLE : View.GONE);

		if (btnMenu.getVisibility() == View.GONE) {
			btnMenu.setAlpha(0f);
		}
	}

	public View getView() {
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.layout_back:
				delegate.onBackClicked();
				break;

			case R.id.btn_menu:
				delegate.onMenuClicked();
				break;
		}
	}

	public void setActionBarTitle(String title) {
		txtTitle.setText(title);
	}

	/**
	 * Set action bar color
	 *
	 * @param color Action bar color
	 */
	public void setActionBarColor(int color) {
		view.setBackgroundColor(color);
	}

	public void setBackVisibility(int visibility) {
		layoutBack.setVisibility(visibility);
	}

	public void setMenuVisibility(int visibility) {
		btnMenu.setVisibility(visibility);
	}

	public void setRightActionItemVisibility(int visibility) {
		layoutRightActionItem.setVisibility(visibility);
	}

	public void setRightBarActionItemView(View view) {
		if (view != null && view.getParent() == null) {
			layoutRightActionItem.removeAllViews();
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
			if (params == null) {
				params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
			}
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			view.setLayoutParams(params);

			layoutRightActionItem.addView(view);
		}
	}
}
