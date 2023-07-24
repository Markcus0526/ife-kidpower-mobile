package org.unicefkidpower.schools.powerband.service;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;

import org.greenrobot.eventbus.EventBus;
import org.unicefkidpower.schools.ble.BleManager;
import org.unicefkidpower.schools.ble.BlePeripheral;
import org.unicefkidpower.schools.event.SEvent;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.powerband.PowerBandDevice;
import org.unicefkidpower.schools.powerband.command.BandCommand;
import org.unicefkidpower.schools.powerband.command.BandCommandResponseParser;
import org.unicefkidpower.schools.powerband.command.CmdAniCelebDisplay.CmdAniCelebDisplayParser;
import org.unicefkidpower.schools.powerband.command.CmdDailyDetailedGet.CmdDailyDetailedGetParser;
import org.unicefkidpower.schools.powerband.command.CmdDailySummaryGet.CmdDailySummaryGetParser;
import org.unicefkidpower.schools.powerband.command.CmdDeviceFirmwareGet.CmdDeviceFirmwareGetParser;
import org.unicefkidpower.schools.powerband.command.CmdDeviceTimeGet.CmdDeviceTimeGetParser;
import org.unicefkidpower.schools.powerband.command.CmdDeviceTimeSet.CmdDeviceTimeSetParser;
import org.unicefkidpower.schools.powerband.command.CmdMessageSet.CmdMessageSetParser;
import org.unicefkidpower.schools.powerband.command.CmdNameSet.CmdNameSetParser;
import org.unicefkidpower.schools.powerband.command.CmdPersonalInfoSet.CmdPersonalInfoSetParser;
import org.unicefkidpower.schools.powerband.command.CmdStorageInfoGet.CmdStorageInfoGetParser;
import org.unicefkidpower.schools.powerband.command.CmdSetTotalPowerPoint.CmdSetTotalPowerPointParser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;


/**
 * Created by donal_000 on 12/24/2014.
 * Modified by dyl on 12/10/2015
 */
public class DataService extends BleService {
	public static final String TAG = "DataService";

	public static final String kLocalCharacteristicNotificationFailed = "kLocalCharacteristicNotificationFailed";

	public static final String kLocalWriteDataFailed = "kLocalWriteDataFailed";


	// communication timeout in minutes
	// writing - reading,discovering
	public static final int BLECOMMUNICATE_TIMEOUT = 60;
	protected HashMap<Byte, BandCommandResponseParser> responseParserHashMap;
	protected HashMap<Byte, PowerBandDevice.WriteCommandCallback> callbacks;
	// timeout handler for descriptor write
	private Handler timeoutHandler = new Handler();
	private Runnable timeoutRunnable = new Runnable() {
		@Override
		public void run() {
			Logger.error(TAG, "SyncingDevice.DataService - characteristic notification timeout");
			EventBus.getDefault().post(new SEvent(kLocalCharacteristicNotificationFailed, _peripheral));
		}
	};
	private Handler writeDataTimeoutHandler = new Handler();
	private Runnable writeDataTimeoutRunnable = new Runnable() {
		@Override
		public void run() {
			Logger.error(TAG, "SyncingDevice.DataService - write data timeout!");
			if (_peripheral != null) {
				if (_peripheral.connectionState() == BlePeripheral.STATE_CONNECTED)
					EventBus.getDefault().post(new SEvent(kLocalWriteDataFailed, _peripheral));
			}
		}
	};
	private BluetoothGattCharacteristic _txCh;
	private BluetoothGattCharacteristic _rxCh;

	public DataService() {
		responseParserHashMap = new HashMap<Byte, BandCommandResponseParser>();

		registerResponseParser(CmdNameSetParser.sharedInstance());
		registerResponseParser(CmdStorageInfoGetParser.sharedInstance());
		registerResponseParser(CmdDailySummaryGetParser.sharedInstance());
		registerResponseParser(CmdDeviceFirmwareGetParser.sharedInstance());
		registerResponseParser(CmdDeviceTimeGetParser.sharedInstance());
		registerResponseParser(CmdDeviceTimeSetParser.sharedInstance());
		registerResponseParser(CmdPersonalInfoSetParser.sharedInstance());
		registerResponseParser(CmdMessageSetParser.sharedInstance());
		registerResponseParser(CmdDailyDetailedGetParser.sharedInstance());
		registerResponseParser(CmdAniCelebDisplayParser.sharedInstance());
		registerResponseParser(CmdSetTotalPowerPointParser.sharedInstance());

		callbacks = new HashMap<Byte, PowerBandDevice.WriteCommandCallback>();
	}

	public static UUID dataServiceId() {
		return UUID.fromString(BleManager.getLongUuidFromShortUuid("fff0"));
	}

	public static UUID dataTxId() {
		return UUID.fromString(BleManager.getLongUuidFromShortUuid("fff6"));
	}

	public static UUID dataRxId() {
		return UUID.fromString(BleManager.getLongUuidFromShortUuid("fff7"));
	}

	private void registerResponseParser(BandCommandResponseParser responseParser) {
		responseParserHashMap.put(responseParser.getSuccessCode(), responseParser);
		responseParserHashMap.put(responseParser.getFailureCode(), responseParser);
	}

	@Override
	public void setService(BlePeripheral peripheral, BluetoothGattService service) {
		this._service = service;
		this._peripheral = peripheral;

		Iterator ci = service.getCharacteristics().iterator();
		while (ci.hasNext()) {
			BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) ci.next();
			if (ch.getUuid().equals(dataTxId())) {
				_txCh = ch;
			} else if (ch.getUuid().equals(dataRxId())) {
				_rxCh = ch;

				// set timeout handler
				this.timeoutHandler.postDelayed(timeoutRunnable, BLECOMMUNICATE_TIMEOUT * 1000);
				// set notify
				Logger.log(TAG, "dataservice - set notify for _rxCh");
				this._peripheral.setCharacteristicNotification(_rxCh, true);
			}
		}

		if (_txCh == null) {
			Logger.error(TAG, "Error - there is no tx Characteristic");
			_peripheral.disconnect();
			return;
		}

		if (_rxCh == null) {
			Logger.error(TAG, "Error - there is no rx Characteristic");
			_peripheral.disconnect();
			return;
		}
	}

	public boolean descriptorWrite(BluetoothGattDescriptor descriptor, boolean success) {

		// remove timeout handler
		this.timeoutHandler.removeCallbacks(this.timeoutRunnable);

		if (success == false) {
			EventBus.getDefault().post(new SEvent(kLocalCharacteristicNotificationFailed, _peripheral));
			return false;
		}
		return true;
	}

	public void readCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
		byte commandByte = value[0];
		// Logger.log(TAG, "readCharacteristic for dataservice - commandByte : %d", commandByte);

		BandCommandResponseParser parser = responseParserHashMap.get(new Byte(commandByte));
		if (parser != null) {
			int ret = parser.parse(commandByte, value);
			parser._response.setPeripheral(_peripheral);

			PowerBandDevice.WriteCommandCallback callback = null;
			synchronized (callbacks) {
				callback = callbacks.get(parser._response.getCommandCode());
			}
			switch (ret) {
				case BandCommandResponseParser.PARSE_FINISHED:
					writeDataTimeoutHandler.removeCallbacks(writeDataTimeoutRunnable);
					if (callback != null) {
						callback.onWrite(true, parser._response);
					}

					break;
				case BandCommandResponseParser.PARSE_WAITING:

					break;
				case BandCommandResponseParser.PARSE_CODE_INCORRECT:
					writeDataTimeoutHandler.removeCallbacks(writeDataTimeoutRunnable);
					if (callback != null) {
						callback.onWrite(false, parser._response);
					}
					break;
				case BandCommandResponseParser.PARSE_ERROR:
					writeDataTimeoutHandler.removeCallbacks(writeDataTimeoutRunnable);
					if (callback != null) {
						callback.onWrite(false, parser._response);
					}
					break;
				default:
					break;
			}
		} else {
			Logger.error(TAG, "readCharacteristic, parser not found, timeout error would be called");
			// timeout error would be called
		}
	}

	public void writeCommand(BandCommand command, PowerBandDevice.WriteCommandCallback callback) {
		synchronized (callbacks) {
			callbacks.put(command._code, callback);
		}

		int blocks = command.getBlockCount();

		if (blocks == 0) {
			callback.onWrite(false, null);
			return;
		}

		for (int idx = 0; idx < blocks; idx++) {
			byte[] writeBytes = command.getBytes(idx);
			if (writeBytes == null) {
				callback.onWrite(false, null);
				return;
			} else {
				writeData(writeBytes);
			}
		}
	}

	protected void writeData(byte[] data) {

		// set timeout handler
		writeDataTimeoutHandler.postDelayed(writeDataTimeoutRunnable, BLECOMMUNICATE_TIMEOUT * 1000);

		_txCh.setValue(data);
		boolean ret = _peripheral.writeCharacteristic(_txCh);
		if (ret == false) {
			Logger.error(TAG, "DataService writeData failed - _peripheral.disconnect()");
			_peripheral.disconnect();
		}
	}
}
