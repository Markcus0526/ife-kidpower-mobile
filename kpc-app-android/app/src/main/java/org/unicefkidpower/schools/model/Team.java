package org.unicefkidpower.schools.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by donal_000 on 1/12/2015.
 */
public class Team implements Parcelable {
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static final Parcelable.Creator<Team> CREATOR = new Parcelable.Creator() {
		@Override
		public Object createFromParcel(Parcel source) {
			return new Team(source);
		}
		@Override
		public Object[] newArray(int size) {
			return new Team[size];
		}
	};

	public int						_id;
	public String					_name;
	public String					_grade;
	public String					_description;
	public String					_imageSrc;
	public Date						_startDate;
	public Date						_endDate;
	public float					_height;
	public float					_weight;
	public float 					_stride;
	public String					_message;
	public ArrayList<Student>		_students;

	public Team() {
		_id = 0;
		_name = "";
		_grade = TeamManager.sharedInstance()._teamGrades.get(0).value;
		_description = "";
		_imageSrc = "";
		//_studentCount = 0;
		_startDate = new Date();
		_endDate = new Date();
		_height = 0.f;
		_weight = 0.f;
		_stride = 0.f;
		_message = "";
		_students = new ArrayList<Student>();
	}

	public Team(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		try {
			if (_students == null)
				_students = new ArrayList<Student>();
			dest.writeList(_students);

			dest.writeInt(_id);

			if (_name == null)
				_name = "";

			dest.writeString(_name);

			if (_grade == null)
				_grade = TeamManager.sharedInstance()._teamGrades.get(0).value;

			dest.writeString(_grade);

			if (_description == null)
				_description = "";

			dest.writeString(_description);

			if (_imageSrc == null)
				_imageSrc = "";

			dest.writeString(_imageSrc);

			//dest.writeInt(_studentCount);

			String strStartDate = Utils.toJsonStringWithDate(_startDate);
			if (strStartDate == null)
				strStartDate = "";
			dest.writeString(strStartDate);

			String strEndDate = Utils.toJsonStringWithDate(_endDate);
			if (strEndDate == null)
				strEndDate = "";

			dest.writeString(strEndDate);

			dest.writeFloat(_height);
			dest.writeFloat(_weight);
			dest.writeFloat(_stride);

			if (_message == null)
				_message = "";

			dest.writeString(_message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readFromParcel(Parcel in) {
		String szBuf = "";

		_students = in.readArrayList(null);

		_id = in.readInt();
		_name = in.readString();
		_grade = in.readString();
		_description = in.readString();
		_imageSrc = in.readString();
		//_studentCount = in.readInt();
		szBuf = in.readString();
		if (szBuf.length() == 0)
			_startDate = null;
		else
			_startDate = Utils.parseStartDate(szBuf);
		szBuf = in.readString();
		if (szBuf.length() == 0)
			_endDate = null;
		else
			_endDate = Utils.parseEndDate(szBuf);
		_height = in.readFloat();
		_weight = in.readFloat();
		_stride = in.readFloat();
		_message = in.readString();
	}
}
