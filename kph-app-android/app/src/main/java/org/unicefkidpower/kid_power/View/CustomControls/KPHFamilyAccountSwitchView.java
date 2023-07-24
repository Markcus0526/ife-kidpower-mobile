package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.unicefkidpower.kid_power.View.Adapters.FamilyAccountSwitchListAdapter;
import org.unicefkidpower.kid_power.R;

/**
 * Created by Ruifeng Shi on 1/28/2016.
 */
public class KPHFamilyAccountSwitchView extends LinearLayout {
	private Context								context;

	// UI Controls
	private KPHTextView							switchToFamilyAccountTextView;
	private RecyclerView						familyAccountsListView;
	private FamilyAccountSwitchListAdapter		familyAccountSwitchListAdapter;


	public KPHFamilyAccountSwitchView(Context context) {
		super(context);

		this.context = context;
		initialize();
	}

	public KPHFamilyAccountSwitchView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.context = context;
		initialize();
	}

	public KPHFamilyAccountSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		this.context = context;
		initialize();
	}

	protected void initialize() {
		View contentView = LayoutInflater.from(context).inflate(R.layout.layout_switch_to_family_account, this);

		familyAccountsListView = (RecyclerView) contentView.findViewById(R.id.lv_family_accounts);
		familyAccountsListView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

		switchToFamilyAccountTextView = (KPHTextView) contentView.findViewById(
				R.id.txt_switch_to_family_account
		);
	}

	public void setFamilyAccountListAdapter(FamilyAccountSwitchListAdapter adapter) {
		this.familyAccountsListView.setAdapter(adapter);
		this.familyAccountSwitchListAdapter = adapter;
	}

	public int getEstimatedHeight() {
		int estimatedHeight = 0;

		if (switchToFamilyAccountTextView != null) {
			estimatedHeight += context.getResources().getDimensionPixelSize(
					R.dimen.dimen_navigation_bar_height
			);
			estimatedHeight += context.getResources().getDimensionPixelSize(
					R.dimen.dimen_margin_10
			);
			estimatedHeight += context.getResources().getDimensionPixelSize(
					R.dimen.dimen_margin_1
			);
		}

		if (familyAccountSwitchListAdapter != null) {
			estimatedHeight += familyAccountSwitchListAdapter.getEstimatedHeight();
		}

		return estimatedHeight;
	}
}
