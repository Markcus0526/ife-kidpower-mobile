package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ruifeng Shi on 3/10/2017.
 */

public class KPHFacebook {
	@SerializedName("handle")
	public String handle = "";

	@SerializedName("gender")
	public String gender = "";

	@SerializedName("dob")
	public String dob = "";

	@SerializedName("facebook")
	public KPHFacebookAuth facebook = new KPHFacebookAuth();

	public class KPHFacebookAuth {
		@SerializedName("fbId")
		public String fbId;

		@SerializedName("email")
		public String email;

		@SerializedName("accessToken")
		public String accessToken;
	}
}
