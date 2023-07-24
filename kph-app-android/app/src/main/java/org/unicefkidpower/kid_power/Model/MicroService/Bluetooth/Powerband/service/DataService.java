package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.service;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;

import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble.BleManager;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble.BlePeripheral;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.EventManager;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.PowerBandDevice;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.BandCommand;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.BandCommandParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdAniCelebDisplaySendParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetCalorieMissionGoalParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetDailyDataParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetDailySummaryDataParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetFirmwareVersionParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetMacAddressParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetStorageInformationParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetTotalPowerPointParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdGetUserNameParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdSetCalorieMissionGoalParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdSetDeviceTimeParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdSetMessageParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdSetPersonalInformationParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdSetTotalPowerPointParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdSetUserNameParser;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Powerband.command.CmdStaticImageSetParser;
import org.unicefkidpower.kid_power.Misc.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

/**
 * Created by donal_000 on 12/24/2014.
 */
public class DataService extends BleService {
	private static final String 	TAG = "DataService";

	public static final String 		kLocalCharacteristicNotificationFailed = "kLocalCharacteristicNotificationFailed";

	public static final String 		kLocalWriteDataFailed = "kLocalWriteDataFailed";


	// communication timeout in minutes
	// writing - reading,discovering
	public static final int BLECOMMUNICATE_TIMEOUT = 60;

	// timeout handler for descriptor write
	private Handler timeoutHandler = new Handler();
	private Runnable timeoutRunnable = new Runnable() {
		@Override
		public void run() {
			Logger.error(TAG, "SyncingDevice.DataService : characteristic notification timeout");
			EventManager.sharedInstance().post(kLocalCharacteristicNotificationFailed, _peripheral);
		}
	};

	private Handler writeDataTimeoutHandler = new Handler();
	private Runnable writeDataTimeoutRunnable = new Runnable() {
		@Override
		public void run() {
			Logger.error(TAG, "SyncingDevice.DataService : write data timeout!");
			if (_peripheral != null) {
				if (_peripheral.connectionState() == BlePeripheral.STATE_CONNECTED)
					EventManager.sharedInstance().post(kLocalWriteDataFailed, _peripheral);
			}
		}
	};

	public static UUID dataServiceId() {
		return UUID.fromString(BleManager.getLongUuidFromShortUuid("fff0"));
	}

	public static UUID dataTxId() {
		return UUID.fromString(BleManager.getLongUuidFromShortUuid("fff6"));
	}

	public static UUID dataRxId() {
		return UUID.fromString(BleManager.getLongUuidFromShortUuid("fff7"));
	}

	private BluetoothGattCharacteristic _txCh;
	private BluetoothGattCharacteristic _rxCh;

	protected HashMap<Byte, BandCommandParser> responseParserHashMap;
	protected HashMap<Byte, PowerBandDevice.WriteCommandCallback> callbacks;

	public DataService() {
		responseParserHashMap = new HashMap<Byte, BandCommandParser>();
		registerResponseParser(CmdGetUserNameParser.sharedInstance());
		registerResponseParser(CmdSetUserNameParser.sharedInstance());
		registerResponseParser(CmdGetStorageInformationParser.sharedInstance());
		registerResponseParser(CmdGetDailySummaryDataParser.sharedInstance());
		registerResponseParser(CmdSetDeviceTimeParser.sharedInstance());
		registerResponseParser(CmdSetPersonalInformationParser.sharedInstance());
		registerResponseParser(CmdSetMessageParser.sharedInstance());
		registerResponseParser(CmdGetDailyDataParser.sharedInstance());
		registerResponseParser(CmdSetCalorieMissionGoalParser.sharedInstance());
		registerResponseParser(CmdGetCalorieMissionGoalParser.sharedInstance());
		registerResponseParser(CmdStaticImageSetParser.sharedInstance());
		registerResponseParser(CmdAniCelebDisplaySendParser.sharedInstance());
		registerResponseParser(CmdGetMacAddressParser.sharedInstance());
		registerResponseParser(CmdGetFirmwareVersionParser.sharedInstance());
		registerResponseParser(CmdSetTotalPowerPointParser.sharedInstance());
		registerResponseParser(CmdGetTotalPowerPointParser.sharedInstance());

		callbacks = new HashMap<Byte, PowerBandDevice.WriteCommandCallback>();
	}

	protected void registerResponseParser(BandCommandParser responseParser) {
		responseParserHashMap.put(new Byte(responseParser.getSuccessCode()), responseParser);
		responseParserHashMap.put(new Byte(responseParser.getFailedCode()), responseParser);
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
				Logger.log(TAG, "setService : set notify for _rxCh");
				this._peripheral.setCharacteristicNotification(_rxCh, true);
			}
		}

		if (_txCh == null) {
			Logger.error(TAG, "setService : There is no tx Characteristic");
			_peripheral.disconnect();
			return;
		}

		if (_rxCh == null) {
			Logger.error(TAG, "setService : There is no rx Characteristic");
			_peripheral.disconnect();
			return;
		}
	}

	public boolean descriptorWrite(BluetoothGattDescriptor descriptor, boolean success) {

		// remove timeout handler
		this.timeoutHandler.removeCallbacks(this.timeoutRunnable);

		if (success == false) {
			EventManager.sharedInstance().post(kLocalCharacteristicNotificationFailed, _peripheral);
			return false;
		}
		return true;
	}

	public void readCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
		byte commandByte = value[0];
		//Logger.log(TAG, "readCharacteristic for dataservice - commandByte : %d", commandByte);

		BandCommandParser parser = responseParserHashMap.get(new Byte(commandByte));
		if (parser != null) {
			int ret = parser.parse(commandByte, value);
			parser._response.setPeripheral(_peripheral);

			PowerBandDevice.WriteCommandCallback callback = null;
			synchronized (callbacks) {
				callback = callbacks.get(parser._response.getCommandCode());
			}
			switch (ret) {
				case BandCommandParser.PARSE_FINISHED:
					writeDataTimeoutHandler.removeCallbacks(writeDataTimeoutRunnable);
					if (callback != null) {
						callback.onWrite(true, parser._response);
					}

					break;
				case BandCommandParser.PARSE_WAITING:

					break;
				case BandCommandParser.PARSE_CODE_INCORRECT:
					writeDataTimeoutHandler.removeCallbacks(writeDataTimeoutRunnable);
					if (callback != null) {
						callback.onWrite(false, parser._response);
					}
					break;
				case BandCommandParser.PARSE_ERROR:
					writeDataTimeoutHandler.removeCallbacks(writeDataTimeoutRunnable);
					if (callback != null) {
						callback.onWrite(false, parser._response);
					}
					break;
				default:
					break;
			}
		} else {
			Logger.error(TAG, "readCharacteristic : Parser not found, timeout error would be called");
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
			Logger.error(TAG, "writeData : WriteData failed, _peripheral.disconnect()");
			_peripheral.disconnect();
		}
	}
}
