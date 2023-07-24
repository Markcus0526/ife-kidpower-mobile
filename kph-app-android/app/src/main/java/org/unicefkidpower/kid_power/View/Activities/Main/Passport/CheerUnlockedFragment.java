package org.unicefkidpower.kid_power.View.Activities.Main.Passport;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import org.unicefkidpower.kid_power.View.Adapters.UnlockedCheerListAdapter;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.Model.Structure.KPHCheerInformation;
import org.unicefkidpower.kid_power.View.Super.SuperNormalSizeDialogFragment;
import org.unicefkidpower.kid_power.Model.MicroService.KPHMissionService;
import org.unicefkidpower.kid_power.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ruifeng Shi on 4/11/2016.
 */
public class CheerUnlockedFragment extends SuperNormalSizeDialogFragment {
	private GridView							gvCheerIconList				= null;
	private KPHButton							btnGotIt					= null;
	private UnlockedCheerListAdapter			cheerIconListAdapter		= null;
	private ArrayList<KPHCheerInformation>		cheerList					= new ArrayList<>();


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// create root view
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		btnGotIt = (KPHButton) rootView.findViewById(R.id.btn_got_it);
		btnGotIt.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickedGotItButton();
					}
				}
		);

		cheerIconListAdapter = new UnlockedCheerListAdapter(getSafeContext(), cheerList);

		gvCheerIconList = (GridView) rootView.findViewById(R.id.gv_cheer_icon_list);
		gvCheerIconList.setAdapter(cheerIconListAdapter);

		ViewGroup.LayoutParams lpCheerIconList = gvCheerIconList.getLayoutParams();
		lpCheerIconList.height = cheerIconListAdapter.getEstimatedHeight();

		gvCheerIconList.setLayoutParams(lpCheerIconList);

		return rootView;
	}

	@Override
	public int contentLayout() {
		return R.layout.layout_cheers_unlocked;
	}

	public void setData(List<Long> cheerIDList) {
		cheerList = new ArrayList<>();

		for (Long cheerID : cheerIDList) {
			KPHCheerInformation cheerInformation = KPHMissionService.sharedInstance().getCheerInformation(cheerID);

			if (cheerInformation != null)
				cheerList.add(cheerInformation);
		}
	}

	private void onClickedGotItButton() {
		dismiss();
	}

	@Override
	public void startAction() {
		// No action. Do nothing.
	}

}
