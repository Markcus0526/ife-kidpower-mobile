package org.unicefkidpower.schools.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by donal_000 on 1/12/2015.
 */
public class User implements Parcelable {
	public static final String USERTYPE_TEACHER = "teacher";
	public static final String USERTYPE_UNDEFINED = "undefined";
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator() {
		@Override
		public Object createFromParcel(Parcel source) {
			return new User(source);
		}
		@Override
		public Object[] newArray(int size) {
			return new User[size];
		}
	};

	public int					_id;
	public String				_regCode;
	public String				_userType;
	public String				_firstName;
	public String				_lastName;
	public String				_access_token;
	public String				_fullName;
	public String				_nickname;
	public String				_email;
	public String				_sendEmailAt;
	public boolean				_emailSent;
	public String				_hostName;
	public boolean				_isActive;
	public String				_userIP;
	public String				_activationToken;
	public Group				_group;
	public ArrayList<Team>		_teams;

	public User() {
		_userType = USERTYPE_UNDEFINED;
		_teams = new ArrayList<Team>();
	}

	public User(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		try {
			if (_teams == null)
				_teams = new ArrayList<Team>();
			dest.writeList(_teams);

			dest.writeInt(_id);
			if (_regCode == null)
				_regCode = "";
			dest.writeString(_regCode);

			if (_userType == null)
				_userType = USERTYPE_TEACHER;
			dest.writeString(_userType);

			if (_firstName == null)
				_firstName = "";
			dest.writeString(_firstName);

			if (_lastName == null)
				_lastName = "";
			dest.writeString(_lastName);

			if (_access_token == null)
				_access_token = "";
			dest.writeString(_access_token);

			if (_fullName == null)
				_fullName = "";
			dest.writeString(_fullName);

			if (_nickname == null)
				_nickname = "";
			dest.writeString(_nickname);

			if (_email == null)
				_email = "";
			dest.writeString(_email);

			if (_sendEmailAt == null)
				_sendEmailAt = "";
			dest.writeString(_sendEmailAt);

			dest.writeInt(_emailSent == false ? 0 : 1);

			if (_hostName == null)
				_hostName = "";
			dest.writeString(_hostName);

			dest.writeInt((_isActive == false ? 0 : 1));

			if (_userIP == null)
				_userIP = "";
			dest.writeString(_userIP);

			if (_activationToken == null)
				_activationToken = "";
			dest.writeString(_activationToken);

			_group = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readFromParcel(Parcel in) {
		try {
			_teams = in.readArrayList(null);
			_id = in.readInt();
			_regCode = in.readString();
			_userType = in.readString();
			_firstName = in.readString();
			_lastName = in.readString();
			_access_token = in.readString();
			_fullName = in.readString();
			_nickname = in.readString();
			_email = in.readString();
			_sendEmailAt = in.readString();
			_emailSent = in.readInt() == 0 ? false : true;
			_hostName = in.readString();
			_isActive = in.readInt() == 0 ? false : true;
			_userIP = in.readString();
			_activationToken = in.readString();
			_group = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

