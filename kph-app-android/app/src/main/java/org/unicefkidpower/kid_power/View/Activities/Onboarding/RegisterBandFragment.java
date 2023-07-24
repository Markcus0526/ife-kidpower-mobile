package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.kid_power.Misc.AlertDialogHelper;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Misc.onBandActionListener;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.BandService;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble.BleManager;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble.BlePeripheral;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.CBBandDetails;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamBandDetails;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamLinkBand;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.EventManager;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.SEvent;
import org.unicefkidpower.kid_power.Model.MicroService.KPHAnalyticsService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHMissionService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.Structure.KPHTracker;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMissionStats;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.Activities.Main.More.PermissionDialogFragment;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;

/**
 * Created by Dayong Li on 10/16/2016.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class RegisterBandFragment extends SelectTrackerSuperFragment {
	private static final String 			TAG									= "RegisterBandFragment";
	private final int						REQUEST_FROM_SEARCH					= 1;
	private final int						REQUEST_FROM_LINK					= 2;

	private int								requestFrom							= REQUEST_FROM_SEARCH;
	private BandItem 						selectedBandItem					= null;

	protected String						eid_Step1ForBandGetDetails			= "stepForBandDetails";
	protected String						eid_Step2BandRegister				= "stepForBandRegister";
	protected String						eid_Step3ForBandSetInformation		= "stepForBandSetDateTime";


	private View							contentView					= null;

	private RelativeLayout					llSearching					= null;
	private KPHTextView						txtStatus					= null;
	private ImageView						ivRotateCircle				= null;

	private LinearLayout					llRegisterBand				= null;
	private KPHTextView						btnSkip						= null;
	private KPHTextView						txtRescan					= null;

	private BandListAdapter					bandAdapter					= null;
	private LinearLayout					bandsListLayout				= null;

	private View							llBandChain					= null;
	private View							rlBandLinked				= null;

	private boolean							isSearchingBleNow			= false;
	private boolean							isProcessing				= false;

	private BlePeripheral					peripheral					= null;

	CBBandDetails.CBBandDetailsResult		bandDetails					= null;
	RotateAnimation							rotateAnimation				= null;

	// Data variables
	private int								userId						= 0;
	private String							sAvatarId					= "";
	private String							sUsername					= "";
	// End of 'Data variables'


	private onBandActionListener nearbyListener = new onBandActionListener() {
		@Override
		public void completed(Object object) {
			isSearchingBleNow = false;

			if (bandAdapter == null) {
				showErrorDialog(BleManager.BT_ERROR_UNKNOWN);
				updateUIState();
				return;
			}

			if (object == null && !ArrayList.class.isInstance(object)) {
				showErrorDialog(BleManager.BT_ERROR_NO_ANY_DEVICES);
				updateUIState();
				return;
			}

			// show bands list layout
			llSearching.setVisibility(View.GONE);
			ivRotateCircle.clearAnimation();
			txtRescan.setVisibility(View.VISIBLE);

			ArrayList<BlePeripheral> _bands = (ArrayList<BlePeripheral>) object;
			if (_bands.isEmpty()) {
				showErrorDialog(BleManager.BT_ERROR_NO_ANY_DEVICES);
				updateUIState();
			} else {
				bandAdapter.setBands(_bands);
				bandAdapter.notifyDataSetChanged();
				llRegisterBand.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void failed(int code, String message) {
			isSearchingBleNow = false;
			showErrorDialog(BleManager.BT_ERROR_NO_SERVICE);
			updateUIState();
		}

		@Override
		public void reportStatus(Object param) {
		}
	};

	public void setData(
			int uId,             // user iD
			String sAvatarId,       // Avatar ID
			String sUsername       // Username
	) {
		userId = uId;

		if (sAvatarId != null) {
			this.sAvatarId = sAvatarId;
		}

		if (sUsername != null) {
			this.sUsername = sUsername;
		}
	}

	private void setSkipEnable(boolean enabled) {
		if (enabled) {
			btnSkip.setTextColor(UIManager.sharedInstance().getColor(R.color.kph_color_white_clickable));
			btnSkip.setEnabled(true);
		} else {
			btnSkip.setTextColor(UIManager.sharedInstance().getColor(R.color.kph_color_disabled_button));
			btnSkip.setEnabled(false);
		}
	}

	private void setRescanEnable(boolean enabled) {
		if (enabled) {
			txtRescan.setTextColor(UIManager.sharedInstance().getColor(R.color.kph_color_white_clickable));
			txtRescan.setEnabled(true);
		} else {
			txtRescan.setTextColor(UIManager.sharedInstance().getColor(R.color.kph_color_pinkish_grey));
			txtRescan.setEnabled(false);
		}
	}

	private IntentFilter intentFilter = null;
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (getActivity() == null) {
				// Already detached
				return;
			}

			String action = intent.getAction();
			switch (action) {
				case KPHBroadcastSignals.BROADCAST_SIGNAL_LOCATION_PERMISSION_SUCCESS:
					Logger.log(TAG, "BroadcastReceiver : Grant Location Permission succeed.");
					if (requestFrom == REQUEST_FROM_SEARCH) {
						searchBandNow();
					} else if (requestFrom == REQUEST_FROM_LINK) {
						onLinkBand(selectedBandItem);
					}
					break;

				case KPHBroadcastSignals.BROADCAST_SIGNAL_LOCATION_PERMISSION_FAILED:
					Logger.log(TAG, "BroadcastReceiver : Grant Location Permission failed.");
					onClickedBackSystemButton();
					showErrorDialog(getSafeContext().getString(R.string.permission_location_did_not_grant));
					break;
			}
		}
	};


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = super.onCreateView(inflater, container, savedInstanceState);

		llBandChain = contentView.findViewById(R.id.llAvatarBandChain);
		rlBandLinked = contentView.findViewById(R.id.rlAvatarBandLinked);

		llBandChain.setVisibility(View.VISIBLE);
		rlBandLinked.setVisibility(View.GONE);

		bandsListLayout = (LinearLayout) contentView.findViewById(R.id.layoutList);

		bandAdapter = new BandListAdapter();
		bandAdapter.setContainerView(bandsListLayout);

		if (!TextUtils.isEmpty(sAvatarId)) {
			Drawable avatar = KPHUserService.sharedInstance().getAvatarDrawable(sAvatarId);

			if (avatar != null) {
				ImageView ivAvatar = (ImageView) contentView.findViewById(R.id.ivAvatar);
				ImageView ivAvatar1 = (ImageView) contentView.findViewById(R.id.iv_avatar1);
				ivAvatar.setImageDrawable(avatar);
				ivAvatar1.setImageDrawable(avatar);
			}
		}

		if (!TextUtils.isEmpty(sUsername)) {
			KPHTextView username = (KPHTextView) contentView.findViewById(R.id.txtUsername);
			username.setText(sUsername);

			KPHTextView linkDescription = (KPHTextView) contentView.findViewById(R.id.txtLinkDescription);
			linkDescription.setText(getSafeContext().getString(R.string.which_band_do_you_want, sUsername));
		}

		btnSkip = (KPHTextView) contentView.findViewById(R.id.btnSkip);
		btnSkip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoNext(false);
			}
		});
		if (getParentActivity() instanceof OnboardingActivity) {
			btnSkip.setVisibility(View.VISIBLE);
		} else {
			btnSkip.setVisibility(View.INVISIBLE);
		}

		llSearching = (RelativeLayout) contentView.findViewById(R.id.searching_layout);
		txtStatus = (KPHTextView) contentView.findViewById(R.id.txtStatus);
		ivRotateCircle = (ImageView) contentView.findViewById(R.id.ivRotateCircle);
		llRegisterBand = (LinearLayout) contentView.findViewById(R.id.llRegisterBands);

		txtRescan = (KPHTextView) contentView.findViewById(R.id.txtRescan);
		ClickableSpan searchBand = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onClickedSearchBand();
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setColor(txtRescan.getCurrentTextColor());
			}
		};

		String content = getSafeContext().getString(R.string.dont_see_band_search_again);
		String strUnderline = getSafeContext().getString(R.string.search_again);
		SpannableString sContent = new SpannableString(content);
		// Learn More Underline
		sContent.setSpan(searchBand, content.indexOf(strUnderline),
				content.indexOf(strUnderline) + strUnderline.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		txtRescan.setText(sContent);
		txtRescan.setMovementMethod(LinkMovementMethod.getInstance());
		txtRescan.setHighlightColor(Color.TRANSPARENT);


		rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotateAnimation.setInterpolator(new LinearInterpolator());
		rotateAnimation.setDuration(2000);
		rotateAnimation.setRepeatCount(Animation.INFINITE);
		rotateAnimation.setFillEnabled(true);
		rotateAnimation.setFillAfter(true);


		llSearching.setVisibility(View.VISIBLE);
		llRegisterBand.setVisibility(View.GONE);
		txtRescan.setVisibility(View.GONE);

		if (intentFilter == null) {
			intentFilter = new IntentFilter();

			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_LOCATION_PERMISSION_SUCCESS);
			intentFilter.addAction(KPHBroadcastSignals.BROADCAST_SIGNAL_LOCATION_PERMISSION_FAILED);

			LocalBroadcastManager.getInstance(getSafeContext()).registerReceiver(receiver, intentFilter);
		}


		// Check bluetooth state
		onClickedSearchBand();

		return contentView;
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();

		LocalBroadcastManager.getInstance(getSafeContext()).unregisterReceiver(receiver);
		intentFilter = null;
	}

	@Override
	public int contentLayout() {
		return R.layout.fragment_register_band;
	}

	@Override
	public void onStart() {
		super.onStart();

		if (!EventManager.sharedInstance().isRegistered(this)) {
			EventManager.sharedInstance().register(this);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BleManager.REQUEST_ENABLE_BLE) {
			boolean isBleEnabled = BleManager.sharedInstance().isBleEnabled();
			if (requestFrom == REQUEST_FROM_SEARCH) {
				if (resultCode == Activity.RESULT_CANCELED || !isBleEnabled) {
					// failed searching ble
					llSearching.setVisibility(View.GONE);
					ivRotateCircle.clearAnimation();
					txtRescan.setVisibility(View.VISIBLE);

					isSearchingBleNow = false;
					updateUIState();
				} else if (resultCode == Activity.RESULT_OK) {
					checkLocationPermission();
				}
			} else if (requestFrom == REQUEST_FROM_LINK) {
				if (resultCode == Activity.RESULT_CANCELED || !isBleEnabled) {
				} else if (resultCode == Activity.RESULT_OK) {
					checkLocationPermission();
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		if (EventManager.sharedInstance().isRegistered(this)) {
			EventManager.sharedInstance().unregister(this);
		}

		if (isSearchingBleNow) {
			BandService.sharedInstance().stopFinding();
		}

		if (isProcessing) {
			KPHMissionService.sharedInstance().stopForBand(getSafeContext());
		}
	}

	private void updateUIState() {
		setSkipEnable(true);
		setRescanEnable(true);

		ArrayList<BandItem> bandsList = bandAdapter.getBands();
		for (int i = 0; i < bandsList.size(); i++) {
			bandsList.get(i)._state = BandItem.STATE_NORMAL;
		}

		bandAdapter.notifyDataSetChanged();
	}


	private void onClickedSearchBand() {
		requestFrom = REQUEST_FROM_SEARCH;

		int bluetoothCheckResult = BleManager.checkInitBluetooth(getParentActivity());
		if (bluetoothCheckResult == BleManager.CHECK_BLUETOOTH_RESULT_FAILURE) {
			showErrorDialog(getString(R.string.bt_not_supported), new AlertDialogHelper.AlertListener() {
				@Override
				public void onPositive() {
					onClickedBackButton();
				}
				@Override
				public void onCancelled() {}
				@Override
				public void onNegative() {}
			});
		} else if (bluetoothCheckResult == BleManager.CHECK_BLUETOOTH_RESULT_SUCCESS) {
			checkLocationPermission();
		} else if (bluetoothCheckResult == BleManager.CHECK_BLUETOOTH_RESULT_NONE) {
			// Not decided yet. Wait until onActivityResult
		}
	}


	protected void searchBandNow() {
		setSkipEnable(true);
		setRescanEnable(true);

		txtRescan.setClickable(false);
		ivRotateCircle.startAnimation(rotateAnimation);

		llSearching.setVisibility(View.VISIBLE);
		llRegisterBand.setVisibility(View.GONE);
		txtRescan.setVisibility(View.GONE);

		isSearchingBleNow = true;
		boolean _isBleEnabled = BleManager.sharedInstance().isBleEnabled();

		if (!_isBleEnabled) {
			BleManager.sharedInstance().enableAdapter();
		}

		BandService.sharedInstance().getNearbyBands(nearbyListener);
	}

	private void onClickedLinkBand(BandItem bandItem) {
		requestFrom = REQUEST_FROM_LINK;
		selectedBandItem = bandItem;

		SuperActivity parentActivity = getParentActivity();

		int checkBluetoothResult = BleManager.checkInitBluetooth(parentActivity);
		if (checkBluetoothResult == BleManager.CHECK_BLUETOOTH_RESULT_SUCCESS) {
			checkLocationPermission();
		} else if (checkBluetoothResult == BleManager.CHECK_BLUETOOTH_RESULT_FAILURE) {
			showErrorDialog(getString(R.string.bt_not_supported));
		} else if (checkBluetoothResult == BleManager.CHECK_BLUETOOTH_RESULT_NONE) {
		}
	}

	private void onLinkBand(BandItem bandItem) {
		if (bandItem == null || bandItem._state != BandItem.STATE_NORMAL)
			return;

		BlePeripheral peripheral = bandItem._peripheral;

		nRetry = BAND_RETRY_COUNT;
		EventManager.sharedInstance().post(eid_Step1ForBandGetDetails, peripheral, peripheral.getCode());

		// start linking
		setSkipEnable(false);
		setRescanEnable(false);

		bandAdapter.onLinkingBand(peripheral);
		bandAdapter.notifyDataSetChanged();
	}


	private void checkLocationPermission() {
		if (!KPHUtils.sharedInstance().checkGPSProviderEnabled(getParentActivity(), true)) {
			return;
		}

		if (ContextCompat.checkSelfPermission(getParentActivity(), KPHConstants.LOCATION_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
			PermissionDialogFragment permissionDialog = new PermissionDialogFragment();
			permissionDialog.setRequestHandler(new PermissionDialogFragment.RequestPermissionHandler() {
				@Override
				public void onRequest() {
					Logger.log(TAG, "checkLocationPermission : send request location permission.");
					ActivityCompat.requestPermissions(getParentActivity(),
							new String[] {KPHConstants.LOCATION_PERMISSION},
							KPHConstants.PERMISSION_REQUEST_LOCATION);
				}

				@Override
				public void onClose() {
					Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_LOCATION_PERMISSION_FAILED);
					LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
				}
			});

			getParentActivity().showDialogFragment(permissionDialog);
		} else {
			if (requestFrom == REQUEST_FROM_SEARCH)
				searchBandNow();
			else
				onLinkBand(selectedBandItem);

			Logger.log(TAG, "checkLocationPermission : have already location permission");
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(final SEvent event) {
		try {
			if (EventManager.isEvent(event, BleManager.kBLEManagerStateChanged)) {
				Integer obj = (Integer) event.object;
				int state = obj.intValue();

				if (state == BluetoothAdapter.STATE_ON && BleManager.sharedInstance().isBleEnabled()) {
					//searching band now
					if (isSearchingBleNow) {
						BandService.sharedInstance().getNearbyBands(nearbyListener);
					}
				}
			} else if (EventManager.isEvent(event, eid_Step1ForBandGetDetails)) {
				isProcessing = true;

				final BlePeripheral peripheral = (BlePeripheral) event.object;

				Logger.log(TAG, "onEventMainThread : STEP1 : getting Band Details for %s(%s)",
						peripheral.name(), peripheral.getCode());

				CBParamBandDetails param = CBParamBandDetails.makeParams(getSafeContext(), peripheral.getCode());
				KPHMissionService.sharedInstance().getDetailsForBand(param, new onBandActionListener() {
					@Override
					public void completed(Object object) {
						if (object != null) {
							bandDetails = (CBBandDetails.CBBandDetailsResult) object;
						} else {
							// TODO, error
						}

						Logger.log(TAG, "onEventMainThread : STEP1 : getting Band Details is successed, type=%s, version=%s,user name=%s",
								bandDetails.getType(), bandDetails.getVersion(), bandDetails.getUserName());
						nRetry = BACKEND_RETRY_COUNT;
						EventManager.sharedInstance().post(eid_Step2BandRegister, peripheral, peripheral.getCode());
					}

					@Override
					public void failed(int code, String message) {
						if (nRetry > 0) {
							nRetry--;
							EventManager.sharedInstance().post(eid_Step1ForBandGetDetails, peripheral, peripheral.getCode());
						} else {
							Logger.error(TAG, "onEventMainThread : STEP1 : failed getting band");
							showErrorDialog(BleManager.BT_ERROR_LINK_FAILED);
							updateUIState();
							isProcessing = false;
						}
					}

					@Override
					public void reportStatus(Object param) {
					}
				});

			} else if (EventManager.isEvent(event, eid_Step2BandRegister)) {
				if (!isProcessing) {
					return;
				}

				if (event.object == null ||
						!(event.object instanceof BlePeripheral) ||
						bandDetails == null) {
					isProcessing = false;
					throw new Exception(getSafeContext().getString(R.string.ble_invalid_peripheral));
				}

				peripheral = (BlePeripheral) event.object;

				Logger.log(TAG, "onEventMainThread : STEP2 : registering Band for device=%s, type=%s, version=%s",
						peripheral.getCode(), bandDetails.getType(), bandDetails.getVersion());

				// TODO Should be changed
				KPHUserService.sharedInstance().setTrackerToUser(
						KPHTracker.TRACKER_TYPE_NAME_KPBAND,
						peripheral.deviceIdByAddress(),
						userId,
						bandDetails.getType(),
						bandDetails.getVersion(),
						new onActionListener() {
							@Override
							public void completed(Object object) {
								if (!(object instanceof KPHTracker)) {
									return;
								}
								Logger.log(TAG, "onEventMainThread : STEP2 : registering band is successed");
								nRetry = BAND_RETRY_COUNT;
								EventManager.sharedInstance().post(eid_Step3ForBandSetInformation, peripheral, peripheral.getCode());
							}

							@Override
							public void failed(int code, String message) {
								if (code == -2 && nRetry > 0) {
									// if network error
									nRetry--;

									try {
										sleep(2000);
									} catch (InterruptedException ex) {
									}

									EventManager.sharedInstance().post(eid_Step2BandRegister, peripheral, message);
								} else if (nRetry < 0) {
									Logger.error(TAG, "onEventMainThread : STEP2 : failed register band(%s)", event.msg);
									isProcessing = false;
									showErrorDialog(SERVICE_ERROR_BASE + code);
									updateUIState();
								} else {
									// other case maybe error
									Logger.error(TAG, "onEventMainThread : STEP2 : failed register band(%s)", message);
									isProcessing = false;

									showErrorDialog(SERVICE_ERROR_BASE + Math.abs(code), message);
									updateUIState();
								}
							}
						});
			} else if (EventManager.isEvent(event, eid_Step3ForBandSetInformation)) {
				if (!isProcessing) {
					return;
				}

				int activeGoal = 0;
				KPHUserMissionStats activeMission = KPHMissionService.sharedInstance().getActiveUserMission();
				if (activeMission != null) {
					activeGoal = activeMission.getMissionGoal();
				}

				Logger.log(TAG, "onEventMainThread : STEP3 : setting User Information to the band, device=%s, new username=%s, active goal=%d",
						peripheral.getCode(), sUsername, activeGoal);

				Map<String, String> payload = new HashMap<>();
				payload.put("type", KPHTracker.TRACKER_TYPE_NAME_KPBAND);
				KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_TRACKER_LINK_START, payload);

				CBParamLinkBand param = CBParamLinkBand.makeParams(getSafeContext(), peripheral.getCode(), sUsername, activeGoal);
				KPHMissionService.sharedInstance().linkBandForBand(param, new onBandActionListener() {
					@Override
					public void completed(Object object) {
						// Completed linking band
						Logger.log(TAG, "onEventMainThread : STEP3 : SUCCEED set user info to the band");

						Map<String, String> payload = new HashMap<>();
						payload.put("type", KPHTracker.TRACKER_TYPE_NAME_KPBAND);
						KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_TRACKER_LINK_SUCCESS, payload);

						onSuccessLinkBand();
					}

					@Override
					public void failed(int code, String message) {
						if (nRetry > 0) {
							nRetry--;
							EventManager.sharedInstance().post(eid_Step3ForBandSetInformation, peripheral, peripheral.getCode());
						} else {
							Logger.error(TAG, "onEventMainThread : STEP3 : failed set user information");
							isProcessing = false;

							Map<String, String> payload = new HashMap<>();
							payload.put("error_code", "" + code);
							payload.put("error_description", message);
							payload.put("type", KPHTracker.TRACKER_TYPE_NAME_KPBAND);
							KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_TRACKER_LINK_ERROR, payload);

							showErrorDialog(BleManager.BT_ERROR_LINK_FAILED);
							updateUIState();

							if (getParentActivity() != null) {
								getParentActivity().showAlertDialog(
										getSafeContext().getResources().getString(R.string.error),
										getString(R.string.failed_linking_band),
										new AlertDialogHelper.AlertListener() {
											@Override
											public void onPositive() {
												Map<String, String> payload = new HashMap<>();
												payload.put("type", KPHTracker.TRACKER_TYPE_NAME_KPBAND);
												KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_TRACKER_LINK_ERROR_OK, payload);
											}
											@Override
											public void onNegative() {}
											@Override
											public void onCancelled() {}
										});
							}
						}
					}

					@Override
					public void reportStatus(Object param) {
					}
				});
			}
		} catch (Exception except) {
			Logger.error(TAG, "onEventMainThread : %s", getString(R.string.unknown_exception) + except.getMessage());

			isProcessing = false;
			showErrorDialog(BleManager.BT_ERROR_UNKNOWN);
			updateUIState();
		}
	}

	protected void onSuccessLinkBand() {
		// update UI
		llBandChain.setVisibility(View.GONE);
		rlBandLinked.setVisibility(View.VISIBLE);
		bandAdapter.onLinkedBand(peripheral);
		bandAdapter.notifyDataSetChanged();

		// goto next
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				gotoNext(true);
			}
		};
		handler.sendEmptyMessageDelayed(0, 300);
	}

	protected void gotoNext(boolean isLinked) {
		if (getParentActivity() == null)
			return;

		if (getParentActivity() instanceof OnboardingActivity) {
			sendLinkMessageToParentActivity(isLinked);
		} else if (getParentActivity() instanceof MainActivity) {
			if (isLinked)
				sendLinkMessageToParentActivity(isLinked);

			onClickedBackButton();
		}
	}


	private void sendLinkMessageToParentActivity(boolean isLinked) {
		Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_PROFILE_TRACKER_SELECTED);
		intent.putExtra(KPHConstants.PROFILE_DEVICE_SELECTED, isLinked);
		intent.putExtra(KPHConstants.PROFILE_DEVICE_TYPE, KPHUserService.TRACKER_TYPE_KIDPOWERBAND);
		intent.putExtra("band", peripheral != null ? peripheral.address() : "");
		LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
	}


	class BandListAdapter {
		ViewGroup containerView = null;
		ArrayList<BandItem> _bands;

		public BandListAdapter() {
			_bands = new ArrayList<>();
		}

		public View getView() {
			if (_bands.size() == 0)
				return null;

			LinearLayout listView = new LinearLayout(bandsListLayout.getContext());
			listView.setOrientation(LinearLayout.VERTICAL);
			listView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

			for (int i = 0; i < _bands.size(); i++) {
				BandItem bandItem = _bands.get(i);

				// for cell view
				BandItemHolder holder = new BandItemHolder();
				holder.createHolder(bandItem, bandItemClickedListener);
				if (holder.view == null)
					continue;

				listView.addView(holder.view);
			}

			return listView;
		}


		private View.OnClickListener bandItemClickedListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SuperActivity parentActivity = getParentActivity();
				if (parentActivity == null)
					return;

				BandItem item = (BandItem) v.getTag();
				onClickedLinkBand(item);
			}
		};


		public void setContainerView(ViewGroup containerView) {
			this.containerView = containerView;
		}

		public ArrayList<BandItem> getBands() {
			return this._bands;
		}

		public void setBands(ArrayList<BlePeripheral> _bands) {
			if (_bands == null)
				return;

			this._bands = new ArrayList<>();
			for (int i = 0; i < _bands.size(); i++) {
				BlePeripheral peripheral = _bands.get(i);

				BandItem bandItem = new BandItem();
				bandItem._peripheral = peripheral;
				bandItem._position = i;
				bandItem._state = BandItem.STATE_NORMAL;

				this._bands.add(bandItem);
			}
		}


		public void onLinkingBand(BlePeripheral peripheral) {
			for (BandItem item : _bands) {
				if (peripheral == item._peripheral) {
					item._state = BandItem.STATE_LINKING;
				} else {
					item._state = BandItem.STATE_DISABLED;
				}
			}
		}


		public void onLinkedBand(BlePeripheral peripheral) {
			for (BandItem item : _bands) {
				if (peripheral == item._peripheral) {
					item._state = BandItem.STATE_FINISHED;
				} else {
					item._state = BandItem.STATE_DISABLED;
				}
			}
		}


		public void notifyDataSetChanged() {
			if (containerView == null)
				return;

			containerView.removeAllViews();

			View view = getView();
			if (view != null)
				containerView.addView(view);
		}
	}


	private class BandItemHolder {
		public String			name			= null;

		public View				view			= null;
		public ImageView		ivBand			= null;
		public KPHTextView		txtBandName		= null;
		public ImageView		ivArrow			= null;
		public ImageView		ivState			= null;


		public void createHolder(BandItem bandItem, View.OnClickListener listener) {
			if (getParentActivity() == null)
				return;

			// Inflate view
			view = getParentActivity().getLayoutInflater().inflate(R.layout.item_registerband, null);

			BlePeripheral peripheral = bandItem._peripheral;

			view.setTag(bandItem);
			view.setOnClickListener(listener);

			this.name = peripheral != null ? peripheral.getCode() : getSafeContext().getString(R.string.unknown);
			this.ivBand = (ImageView) view.findViewById(R.id.ivBand);
			this.txtBandName = (KPHTextView) view.findViewById(R.id.txtBand);
			this.ivArrow = (ImageView) view.findViewById(R.id.ivEncloser);
			this.ivState = (ImageView) view.findViewById(R.id.ivStateImage);

			txtBandName.setText(name);

			switch (bandItem._state) {
				case BandItem.STATE_NORMAL:
					setEnable(true);
					break;
				case BandItem.STATE_DISABLED:
					setEnable(false);
					break;
				case BandItem.STATE_LINKING:
					setLinkingState(true);
					break;
				case BandItem.STATE_FINISHED:
					setLinkingState(false);
					break;
			}
		}

		/**
		 * change state to linking state
		 *
		 * @param isLinking : true : LINKING, false : LINKED
		 */
		public void setLinkingState(boolean isLinking) {
			if (ivBand == null)
				return;

			ivArrow.setVisibility(View.GONE);
			ivState.setVisibility(View.VISIBLE);

			Drawable drawable;
			if (isLinking) {
				drawable = UIManager.sharedInstance().getDrawable(R.drawable.rotate_circle);
				ivState.setImageDrawable(drawable);
				ivState.setAnimation(rotateAnimation);
				txtBandName.setText(txtBandName.getText() + " " + getString(R.string.linking));
			} else {
				drawable = UIManager.sharedInstance().getDrawable(R.drawable.ic_white_check);
				ivState.setImageDrawable(drawable);
				ivState.clearAnimation();
			}
		}

		/**
		 * change state to enabled or disabled state
		 *
		 * @param isEnabled
		 */
		public void setEnable(boolean isEnabled) {
			if (ivBand == null)
				return;

			ivState.setVisibility(View.GONE);
			ivArrow.setVisibility(View.VISIBLE);

			if (isEnabled) {
				ivBand.setColorFilter(UIManager.sharedInstance().getColor(R.color.kph_color_translucent_nocolor), PorterDuff.Mode.SRC_ATOP);
				txtBandName.setTextColor(Color.WHITE);
				ivArrow.setColorFilter(UIManager.sharedInstance().getColor(R.color.kph_color_translucent_nocolor), PorterDuff.Mode.SRC_ATOP);
			} else {
				ivBand.setColorFilter(UIManager.sharedInstance().getColor(R.color.kph_color_translucent_gray), PorterDuff.Mode.SRC_ATOP);
				txtBandName.setTextColor(UIManager.sharedInstance().getColor(R.color.kph_color_pinkish_grey));
				ivArrow.setColorFilter(UIManager.sharedInstance().getColor(R.color.kph_color_translucent_gray), PorterDuff.Mode.SRC_ATOP);
			}
		}
	}

	private class BandItem {
		public static final int STATE_DISABLED = 0;
		public static final int STATE_NORMAL = 1;
		public static final int STATE_LINKING = 2;
		public static final int STATE_FINISHED = 3;

		BlePeripheral _peripheral;
		int _position;
		int _state;
	}

}
