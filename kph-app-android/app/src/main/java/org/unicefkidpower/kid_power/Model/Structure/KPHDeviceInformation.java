package org.unicefkidpower.kid_power.Model.Structure;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.google.gson.annotations.SerializedName;
import com.jaredrummler.android.device.BuildConfig;
import com.jaredrummler.android.device.DeviceName;

import org.json.JSONObject;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;

/**
 * Created by Dayong Li on 5/4/2017.
 */

public class KPHDeviceInformation {
	@SerializedName("_id")
	public int id = 0;

	@SerializedName("device")
	public String device = "";

	@SerializedName("imei")
	public String imei = "";

	@SerializedName("iccid")
	public String iccid = "";

	@SerializedName("imsi")
	public String imsi = "";

	@SerializedName("udid")
	public String udid = "";

	@SerializedName("versionCode")
	public String versionCode = "";

	@SerializedName("userId")
	public int userId = 0;

	@SerializedName("pushId")
	public String pushId = "";


	public KPHDeviceInformation() {}

	public KPHDeviceInformation(
			String device,
			String imei,
			String iccid,
			String imsi,
			String udid,
			String versionCode,
			int userId,
			String pushId
	) {
		this.device = device;
		this.imei = imei;
		this.iccid = iccid;
		this.imsi = imsi;
		this.udid = udid;
		this.versionCode = versionCode;
		this.userId = userId;
		this.pushId = pushId;
	}

	public static KPHDeviceInformation getCurrentDeviceInformation(Context context) {
		String deviceName = DeviceName.getDeviceName();
		String imei = "";//KPHUtils.sharedInstance().getIMEI(); // removed the permission of phone device as the requirement by 08-06-2017
		String iccid = "";//KPHUtils.sharedInstance().getICCID(); // removed the permission of phone device as the requirement by 08-06-2017
		String imsi = KPHUtils.sharedInstance().getIMSI();			// Temporarily return empty string
		String udid = KPHUtils.sharedInstance().getDeviceIdentifier();
		int versionCode = 0;
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			versionCode = pInfo.versionCode;
		} catch (Exception ex) {
			ex.printStackTrace();
			versionCode = -1;
		}

		int userId = KPHUserService.sharedInstance().getUserData() == null ? 0 : KPHUserService.sharedInstance().getUserData().getId();
		String pushId = KPHUtils.sharedInstance().getPushID();

		return new KPHDeviceInformation(deviceName, imei, iccid, imsi, udid, "" + versionCode, userId, pushId);
	}

	public boolean isSameInformation(KPHDeviceInformation information) {
		if (information == null)
			return false;

		if (!this.device.equals(information.device))
			return false;

		if (!this.imei.equals(information.imei))
			return false;

		if (!this.iccid.equals(information.iccid))
			return false;

		if (!this.imsi.equals(information.imsi))
			return false;

		if (!this.udid.equals(information.udid))
			return false;

		if (!this.versionCode.equals(information.versionCode))
			return false;

		if (this.userId != information.userId)
			return false;

		if (!this.pushId.equals(information.pushId))
			return false;

		return true;
	}


	public JSONObject encodeToJSON() {
		JSONObject result = new JSONObject();

		try { result.put("device", device); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("imei", imei); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("iccid", iccid); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("imsi", imsi); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("udid", udid); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("versionCode", versionCode); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("userId", userId); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("pushId", pushId); } catch (Exception ex) { ex.printStackTrace(); }

		return result;
	}


	public static KPHDeviceInformation decodeFromJSON(JSONObject jsonObject) {
		String device = "";
		String imei = "";
		String iccid = "";
		String imsi = "";
		String udid = "";
		String versionCode = "";
		int userId = 0;
		String pushId = "";

		device = jsonObject.optString("device", "");
		imei = jsonObject.optString("imei", "");
		iccid = jsonObject.optString("iccid", "");
		imsi = jsonObject.optString("imsi", "");
		udid = jsonObject.optString("udid", "");
		versionCode = jsonObject.optString("versionCode", "");
		userId = jsonObject.optInt("userId", 0);
		pushId = jsonObject.optString("pushId", "");

		return new KPHDeviceInformation(device, imei, iccid, imsi, udid, versionCode, userId, pushId);
	}
}
