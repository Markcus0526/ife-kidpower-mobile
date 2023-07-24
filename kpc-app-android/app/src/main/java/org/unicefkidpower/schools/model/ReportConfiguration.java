package org.unicefkidpower.schools.model;

import org.json.JSONObject;

/**
 * Created by Ruifeng Shi on 3/23/2017.
 */

public class ReportConfiguration {
	public String deviceModel = "";
	public String IMEI = "";
	public String versionCode = "";
	public String IMSI = "";
	public String ICCID = "";
	public String UDID = "";
	public String OS = "";

	public ReportConfiguration() {}

	public ReportConfiguration(String deviceModel,
							   String IMEI,
							   String versionCode,
							   String IMSI,
							   String ICCID,
							   String UDID,
							   String OS) {
		this.deviceModel = deviceModel;
		this.IMEI = IMEI;
		this.versionCode = versionCode;
		this.IMSI = IMSI;
		this.ICCID = ICCID;
		this.UDID = UDID;
		this.OS = OS;
	}

	public String encode2JsonString() {
		JSONObject result = new JSONObject();

		try { result.put("deviceModel", deviceModel); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("IMEI", IMEI); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("versionCode", versionCode); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("IMSI", IMSI); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("ICCID", ICCID); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("UDID", UDID); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("OS", OS); } catch (Exception ex) { ex.printStackTrace(); }

		return result.toString();
	}

	public static ReportConfiguration decodeFromJSONString(String contents) {
		ReportConfiguration result = new ReportConfiguration();

		try {
			JSONObject jsonObject = new JSONObject(contents);

			result.deviceModel = jsonObject.optString("deviceModel");
			result.IMEI = jsonObject.optString("IMEI");
			result.versionCode = jsonObject.optString("versionCode");
			result.IMSI = jsonObject.optString("IMSI");
			result.ICCID = jsonObject.optString("ICCID");
			result.UDID = jsonObject.optString("UDID");
			result.OS = jsonObject.optString("OS");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return result;
	}
}
