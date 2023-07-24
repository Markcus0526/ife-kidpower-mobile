package org.unicefkidpower.schools.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.unicefkidpower.schools.helper.OSDate;
import org.unicefkidpower.schools.server.apimanage.StudentService;

import java.util.Date;

/**
 * Created by donal_000 on 1/12/2015.
 */
public class Student implements Parcelable {
	public static final int BAND_CODE_LENGTH = 5;

	public static final String GENDER_BOY = "boy";
	public static final String GENDER_GIRL = "girl";
	public static final String GENDER_UNDEFINED = "undefined";
	public static final int POWERBAND_RECORD_DAYS_COUNT = 30;

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static final Parcelable.Creator<Student> CREATOR = new Parcelable.Creator() {
		@Override
		public Object createFromParcel(Parcel source) {
			return new Student(source);
		}
		@Override
		public Object[] newArray(int size) {
			return new Student[size];
		}
	};

	public int				_id;
	public String			_name;
	public String			_gender;
	public String			_imageSrc;
	public float			_height;
	public float			_weight;
	public float			_stride;
	public String			_message;
	public Date				_lastSyncDateDetail;
	public int				_teamId;
	public int				_steps;
	public float			_miles;
	public int				_packets;
	public int				_powerPoints;
	public StudentService.ResGetStudentWithDisplayMessage _displayMessage;
	public boolean			_isActive;
	public boolean			_isCoach;
	public Date				_updatedAt;
	public Date				_createdAt;
	public String			_fullName;

	protected String		_deviceId;
	protected String		_deviceId2;

	protected String		_deviceCode;
	protected String		_deviceCode2;

	public Student() {
		_id = 0;
		_name = "";
		_gender = GENDER_UNDEFINED;
		_deviceId = "";
		_deviceId2 = "";
		_imageSrc = "";
		_height = 0.f;
		_weight = 0.f;
		_stride = 0.f;
		_message = "";
		_lastSyncDateDetail = null;
		_teamId = 0;
		_steps = 0;
		_miles = 0.f;
		_packets = 0;
		_powerPoints = 0;

		_isActive = false;
		_isCoach = false;
		_updatedAt = null;
		_createdAt = null;
		_fullName = "";
	}

	public Student(Parcel in) {
		readFromParcel(in);
	}

	public static Date getResetDate() {
		return new OSDate().offsetDay(-(POWERBAND_RECORD_DAYS_COUNT - 1));
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		try {
			dest.writeInt(_id);
			if (_name == null)
				_name = "";
			dest.writeString(_name);

			if (_gender == null)
				_gender = TeamManager.sharedInstance()._teamGrades.get(0).value;
			dest.writeString(_gender);

			if (_deviceId == null)
				_deviceId = "";
			dest.writeString(_deviceId);

			if (_deviceId2 == null)
				_deviceId2 = "";
			dest.writeString(_deviceId2);

			if (_imageSrc == null)
				_imageSrc = "";
			dest.writeString(_imageSrc);

			dest.writeFloat(_height);
			dest.writeFloat(_weight);
			dest.writeFloat(_stride);

			if (_message == null)
				_message = "";
			dest.writeString(_message);

			String strLastSyncDateDetail = Utils.toJsonStringWithDate(_lastSyncDateDetail);
			if (strLastSyncDateDetail == null)
				strLastSyncDateDetail = "";
			dest.writeString(strLastSyncDateDetail);

			dest.writeInt(_teamId);
			dest.writeInt(_steps);
			dest.writeFloat(_miles);
			dest.writeInt(_packets);
			dest.writeInt(_powerPoints);


			int isActive = (int) (_isActive ? 1 : 0);
			dest.writeInt(isActive);
			int isCoach = (int) (_isCoach ? 1 : 0);
			dest.writeInt(isCoach);

			String strUpdatedAt = Utils.toJsonStringWithDate(_updatedAt);
			if (strUpdatedAt == null)
				strUpdatedAt = "";
			dest.writeString(strUpdatedAt);

			String strCreatedAt = Utils.toJsonStringWithDate(_createdAt);
			if (strCreatedAt == null)
				strCreatedAt = "";
			dest.writeString(strCreatedAt);

			if (_fullName == null)
				_fullName = "";
			dest.writeString(_fullName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readFromParcel(Parcel in) {
		try {
			String szBuf = "";

			_id = in.readInt();
			_name = in.readString();
			_gender = in.readString();
			_deviceId = in.readString();
			_deviceId2 = in.readString();
			_imageSrc = in.readString();
			_height = in.readFloat();
			_weight = in.readFloat();
			_stride = in.readFloat();
			_message = in.readString();
			szBuf = in.readString();
			if (szBuf.length() == 0)
				_lastSyncDateDetail = null;
			else
				_lastSyncDateDetail = Utils.parseLastSyncDateDetail(szBuf);

			_teamId = in.readInt();
			_steps = in.readInt();
			_miles = in.readFloat();
			_packets = in.readInt();
			_powerPoints = in.readInt();


			_isActive = in.readByte() != 0;
			_isCoach = in.readByte() != 0;
			szBuf = in.readString();
			if (szBuf.length() == 0)
				_updatedAt = null;
			else
				_updatedAt = Utils.parseUpdatedAt(szBuf);
			szBuf = in.readString();
			if (szBuf.length() == 0)
				_createdAt = null;
			else
				_createdAt = Utils.parseCreatedAt(szBuf);
			_fullName = in.readString();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isUnlinkedBand() {
		return TextUtils.isEmpty(_deviceId);
	}

	public String getDeviceId() {
		return _deviceId;
	}

	public String getDeviceCode() {
		if (_deviceId == null || _deviceId.length() < BAND_CODE_LENGTH) {
			return "";
		}

		if (_deviceCode == null || _deviceCode.isEmpty()) {
			_deviceCode = _deviceId.substring(_deviceId.length() - BAND_CODE_LENGTH);
		}
		return _deviceCode;
	}

	public void setDeviceId(String deviceId) {
		_deviceId = deviceId;
		_deviceCode = null;
	}

	public String getDeviceId2() {
		return _deviceId2;
	}

	public String getDeviceCode2() {
		if (_deviceId2 == null || _deviceId2.length() < BAND_CODE_LENGTH) {
			throw new IllegalArgumentException();
		}

		if (_deviceCode2 == null || _deviceCode2.isEmpty()) {
			_deviceCode2 = _deviceId2.substring(_deviceId2.length() - BAND_CODE_LENGTH);
		}
		return _deviceCode2;
	}

	public void setDeviceId2(String deviceId) {
		_deviceId2 = deviceId;
		_deviceCode2 = null;
	}

}
