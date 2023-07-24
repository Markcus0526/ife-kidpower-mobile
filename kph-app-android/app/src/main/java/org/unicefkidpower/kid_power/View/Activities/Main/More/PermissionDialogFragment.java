package org.unicefkidpower.kid_power.View.Activities.Main.More;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperNormalSizeDialogFragment;


/**
 * Created by Dayong Li on 9/16/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class PermissionDialogFragment extends SuperNormalSizeDialogFragment {
	private static final String			TAG					= "Permission";

	private boolean						type				= false;		// false : location, true : storage
	private View						contentView			= null;
	private RequestPermissionHandler	requestHandler		= null;
	private boolean						needCloseCallback	= true;


	public void setRequestHandler(RequestPermissionHandler requestHandler) {
		this.requestHandler = requestHandler;
	}

	public void setType(boolean isForLocation) {
		type = isForLocation;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (contentView != null)
			return contentView;

		contentView = super.onCreateView(inflater, container, savedInstanceState);

		ImageView ivIcon = (ImageView) contentView.findViewById(R.id.ivIcon);
		KPHTextView tvPermTitle = (KPHTextView) contentView.findViewById(R.id.tvPermTitle);
		KPHTextView tvPermDescription = (KPHTextView) contentView.findViewById(R.id.tvPermDescription);
		KPHButton btnRequest = (KPHButton) contentView.findViewById(R.id.btnRequest);

		if (type) {
			ivIcon.setImageDrawable(UIManager.sharedInstance().getImageDrawable(R.drawable.permission_storage));
			tvPermTitle.setText(R.string.permission_storage_title);
			tvPermDescription.setText(R.string.permission_storage_description);
			btnRequest.setText(R.string.permission_storage_button);
		}

		btnRequest.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				needCloseCallback = false;
				dismiss();

				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (requestHandler != null) {
							requestHandler.onRequest();
						}
					}
				}, 10);
			}
		});
		contentView.findViewById(R.id.ivClose).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		setDismissListener(new SuperDialogDismissListener() {
			@Override
			public void onDismiss() {
				if (needCloseCallback) {
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (requestHandler != null) {
								requestHandler.onClose();
							}
						}
					}, 10);
				}
			}
		});

		return contentView;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_permission;
	}


	@Override
	public void startAction() {
		// No action
	}

	public interface RequestPermissionHandler {
		void onRequest();
		void onClose();
	}
}
