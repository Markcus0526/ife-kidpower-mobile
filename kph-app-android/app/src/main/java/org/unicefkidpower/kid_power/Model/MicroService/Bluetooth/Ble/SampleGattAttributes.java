/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Ble;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
	private static HashMap<String, String> attributes = new HashMap<>();
	public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
	public static String BLOOD_PRESSURE_MEASUREMENT = "00002a35-0000-1000-8000-00805f9b34fb";
	public static String BODY_WEIGHT_MEASUREMENT = "00008a21-0000-1000-8000-00805f9b34fb";
	public static String PEDOMETER_MEASUREMENT = "0000fff7-0000-1000-8000-00805f9b34fb";
	public static String PEDOMETER_MEASUREMENT2 = "0000fff6-0000-1000-8000-00805f9b34fb";
	public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

	static {
		// Services.
		attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access Service");
		attributes.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute Service");
		attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
		attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
		attributes.put("6c721826-5bf1-4f64-9170-381c08ec57ee", "HRM ? Service");
		attributes.put("0000180f-0000-1000-8000-00805f9b34fb", "Battery Service");
		attributes.put("00001810-0000-1000-8000-00805f9b34fb", "Blood Pressure Service");
		attributes.put("000078a2-0000-1000-8000-00805f9b34fb", "Body Weight Service");
		attributes.put("0000fff0-0000-1000-8000-00805f9b34fb", "Pedometer Service");
		attributes.put("0000ffe0-0000-1000-8000-00805f9b34fb", "Pedometer ? Service");
		// Characteristics.
		attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
		attributes.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
		attributes.put("00002a02-0000-1000-8000-00805f9b34fb", "Peripheral Privacy Flag");
		attributes.put("00002a03-0000-1000-8000-00805f9b34fb", "Reconnection Address");
		attributes.put("00002a04-0000-1000-8000-00805f9b34fb", "Peripheral Preferred Connection Parameters");
		attributes.put("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed");
		attributes.put("00002a19-0000-1000-8000-00805f9b34fb", "Battery Level");
		attributes.put("00002a23-0000-1000-8000-00805f9b34fb", "System ID");
		attributes.put("00002a24-0000-1000-8000-00805f9b34fb", "Model Number String");
		attributes.put("00002a25-0000-1000-8000-00805f9b34fb", "Serial Number String");
		attributes.put("00002a26-0000-1000-8000-00805f9b34fb", "Firmware Revision String");
		attributes.put("00002a27-0000-1000-8000-00805f9b34fb", "Hardware Revision String");
		attributes.put("00002a28-0000-1000-8000-00805f9b34fb", "Software Revision String");
		attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
		attributes.put("00002a2a-0000-1000-8000-00805f9b34fb", "IEEE 11073-20601 Regulatory Certification Data List");
		attributes.put("00002a50-0000-1000-8000-00805f9b34fb", "PnP ID");
		//Heart Rate
		attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");//2a37
		attributes.put("00002a38-0000-1000-8000-00805f9b34fb", "Body Sensor Location");
		attributes.put("6c722a0a-5bf1-4f64-9170-381c08ec57ee", "HRM ? Chara");
		attributes.put("6c722a0b-5bf1-4f64-9170-381c08ec57ee", "HRM ? Chara");
		//Blood Pressure
		attributes.put(BLOOD_PRESSURE_MEASUREMENT, "Blood Pressure Measurement");//2a35
		attributes.put("00002a36-0000-1000-8000-00805f9b34fb", "Intermediate Cuff Pressure");
		attributes.put("00002a49-0000-1000-8000-00805f9b34fb", "Blood Pressure Feature");
		//Body Weight
		attributes.put("00008a20-0000-1000-8000-00805f9b34fb", "Body Weight ? Chara");
		attributes.put(BODY_WEIGHT_MEASUREMENT, "Body Weight Measurement");//8a21
		attributes.put("00008a22-0000-1000-8000-00805f9b34fb", "Body Weight ? Chara");
		attributes.put("00008a81-0000-1000-8000-00805f9b34fb", "Body Weight ? Chara");
		attributes.put("00008a82-0000-1000-8000-00805f9b34fb", "Body Weight ? Chara");
		//Pedometer
		attributes.put("0000fff1-0000-1000-8000-00805f9b34fb", "Pedometer ? Chara");
		attributes.put("0000fff2-0000-1000-8000-00805f9b34fb", "Pedometer ? Chara");
		attributes.put("0000fff3-0000-1000-8000-00805f9b34fb", "Pedometer ? Chara");
		attributes.put("0000fff4-0000-1000-8000-00805f9b34fb", "Pedometer ? Chara");
		attributes.put("0000fff5-0000-1000-8000-00805f9b34fb", "Pedometer Bluetooth Pairing Request");
		attributes.put("0000fff6-0000-1000-8000-00805f9b34fb", "TX (cmd to Pedo)");
		attributes.put("0000fff7-0000-1000-8000-00805f9b34fb", "RX (cmd to Pedo)");
		attributes.put("0000ffe1-0000-1000-8000-00805f9b34fb", "Pedometer ? Chara");
		attributes.put("00000007-0000-1000-8000-00805f9b34fb", "Read Someday's Total Activity data");
		// attributes.put("0xFFF0", "Pedometer ? Chara");
	}

	public static String lookup(String uuid, String defaultName) {
		String name = attributes.get(uuid);
		return name == null ? defaultName : name;
	}
}

