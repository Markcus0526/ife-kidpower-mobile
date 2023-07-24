package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth;

import android.os.Handler;
import android.os.Message;

import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble.BleManager;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble.BlePeripheral;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.CBBandCommandHelper;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.CBBandCompletedMission;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.CBBandDetails;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.CBBandLinkBand;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.CBBandStartMission;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.CBBandSync;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamBandDetails;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamLinkBand;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamSimple;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamStartMission;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamSync;
import org.unicefkidpower.kid_power.Misc.onBandActionListener;

import java.util.ArrayList;


/**
 * Created by Dayong Li on 9/21/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class BandService {
	private static BandService				instance;
	private static BleManager				bleManager;

	private static boolean					finding = false;
	private static CBBandCommandHelper		lastCommand = null;

	public static final int					TIMEOUT_FOR_SEARCHING = 20 * 1000;	// default searching time, exclude enabling, checking


	public static BandService sharedInstance() {
		if (instance == null)
			instance = new BandService();

		return instance;
	}

	public BandService() {
		bleManager = BleManager.sharedInstance();
	}

	public void stopFinding() {
		if (finding) {
			bleManager.stopScan();
		}
	}

	public void getNearbyBands(final onBandActionListener completeListener) {
		if (completeListener == null)
			return;

		finding = true;
		bleManager.scanForPeripheralsWithServices(null, true);

		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				finding = false;
				if (msg.what == 0) {
					bleManager.stopScan();

					ArrayList<BlePeripheral> bands = bleManager.getScannedPeripherals();
					completeListener.completed(bands);
				}
			}
		};

		handler.sendEmptyMessageDelayed(0, TIMEOUT_FOR_SEARCHING);
	}


	public void stopLastCommand() {
		if (lastCommand == null)
			return;

		lastCommand.stop();
		lastCommand = null;
	}


	public void getBandDetails(CBParamBandDetails param, final onBandActionListener listener) {
		assert (listener != null);
		lastCommand = new CBBandDetails(param, new onBandActionListener() {
			@Override
			public void completed(Object object) {
				listener.completed(object);
				lastCommand = null;
			}

			@Override
			public void failed(int code, String message) {
				listener.failed(code, message);
				lastCommand = null;
			}

			@Override
			public void reportStatus(Object param) {
				listener.reportStatus(param);
			}
		});
		lastCommand.execute();
	}


	public void linkBand(CBParamLinkBand param, final onBandActionListener listener) {
		assert (listener != null);
		lastCommand = new CBBandLinkBand(param, new onBandActionListener() {
			@Override
			public void completed(Object object) {
				listener.completed(object);
				lastCommand = null;
			}

			@Override
			public void failed(int code, String message) {
				listener.failed(code, message);
				lastCommand = null;
			}

			@Override
			public void reportStatus(Object param) {
				listener.reportStatus(param);
			}
		});
		lastCommand.execute();
	}


	public void setMissionGoal(CBParamStartMission param, final onBandActionListener listener) {
		assert (listener != null);
		lastCommand = new CBBandStartMission(param, new onBandActionListener() {
			@Override
			public void completed(Object object) {
				listener.completed(object);
				lastCommand = null;
			}

			@Override
			public void failed(int code, String message) {
				listener.failed(code, message);
				lastCommand = null;
			}

			@Override
			public void reportStatus(Object param) {
				listener.reportStatus(param);
			}
		});
		lastCommand.execute();
	}

	public void disableMissionGoal(CBParamSimple param, final onBandActionListener listener) {
		assert (listener != null);
		lastCommand = new CBBandCompletedMission(param, new onBandActionListener() {
			@Override
			public void completed(Object object) {
				listener.completed(object);
				lastCommand = null;
			}

			@Override
			public void failed(int code, String message) {
				listener.failed(code, message);
				lastCommand = null;
			}

			@Override
			public void reportStatus(Object param) {
				listener.reportStatus(param);
			}
		});
		lastCommand.execute();
	}

	public void getDetailedActivityForDeviceCode(CBParamSync param, final onBandActionListener listener) {
		assert (listener != null);
		lastCommand = new CBBandSync(param, new onBandActionListener() {
			@Override
			public void completed(Object object) {
				listener.completed(object);
				lastCommand = null;
			}

			@Override
			public void failed(int code, String message) {
				listener.failed(code, message);
				lastCommand = null;
			}

			@Override
			public void reportStatus(Object param) {
				listener.reportStatus(param);
			}
		});
		lastCommand.execute();
	}

}
