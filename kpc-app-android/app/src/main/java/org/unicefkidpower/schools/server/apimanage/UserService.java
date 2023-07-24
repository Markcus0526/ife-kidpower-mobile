package org.unicefkidpower.schools.server.apimanage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by donal_000 on 1/13/2015.
 */
public interface UserService {
	@FormUrlEncoded
	@POST(APIManager.USER_LOGIN)
	void login(@Field("email") String email,
			   @Field("password") String password,
			   RestCallback<ResLoginForSuccess> callback);

	@FormUrlEncoded
	@POST(APIManager.USER_SIGNUP)
	void signup(@Field("groupId") int groupId,
				@Field("email") String email,
				@Field("password") String password,
				@Field("firstName") String firstName,
				@Field("lastName") String lastName,
				@Field("userType") String userType,
				@Field("nickname") String nickname,
				@Field("language") String language,/*,
					@Field("sendEmailAt") String sendEmailAt,
					@Field("emailSent") String emailSent,
					@Field("hostName") String hostName,*/
				RestCallback<ResSignup> callback);

	@FormUrlEncoded
	@POST(APIManager.USER_LOGOUT)
	void logout(@Header("access_token") String access_token, RestCallback<ResLogout> callback);

	@FormUrlEncoded
	@PUT(APIManager.USER_UPDATE)
	void usernameupdate(@Field("firstName") String firstName,
						@Field("lastName") String lastName,
						@Field("nickname") String nickname,
						@Header("x-access-token") String access_token,
						@Path("id") String id,
						RestCallback<ResUserUpdate> callback);

	@FormUrlEncoded
	@PUT(APIManager.USER_UPDATE)
	void useremailupdate(@Field("email") String email,
						 @Header("x-access-token") String access_token,
						 @Path("id") String id,
						 RestCallback<ResUserUpdate> callback);

	@FormUrlEncoded
	@PUT(APIManager.USER_UPDATE)
	void updateUserPassword(
			@Field("password") String password,
			@Header("x-access-token") String access_token,
			@Path("id") String id,
			RestCallback<ResUserUpdate> callback
	);

	@FormUrlEncoded
	@PUT(APIManager.USER_UPDATE)
	void updateUserInfo(
			@Header("x-access-token") String access_token,
			@Field("title") String title,
			@Field("firstName") String firstName,
			@Field("lastName") String lastName,
			@Field("nickname") String nickname,
			@Field("address1") String address1,
			@Field("address2") String address2,
			@Field("city") String city,
			@Field("state") String state,
			@Field("postcode") String postcode,
			@Field("email") String email,
			@Field("cellPhone") String cellPhone,
			@Field("contactPreference") int contactPreference,
			@Path("id") String id,
			RestCallback<ResUserUpdate> callback
	);

	@FormUrlEncoded
	@POST(APIManager.USER_VALIDATEPASSWORD)
	void validatePassword(@Header("x-access-token") String access_token,
						  @Field("password") String password,
						  RestCallback<ResValidatePassword> callback);

	@FormUrlEncoded
	@POST(APIManager.USER_SENDRESETLINK)
	void sendResetLink(@Header("x-access-token") String access_token,
					   @Field("email") String email,
					   RestCallback<ResSendResetLinkForSuccess> callback);

	@FormUrlEncoded
	@POST(APIManager.USER_CONFIGDEVICE)
	void userDevices(@Header("x-access-token") String access_token,
					 @Field("device") String device,
					 @Field("imei") String imei,
					 @Field("versionCode") String versionCode,
					 @Field("imsi") String imsi,
					 @Field("iccid") String iccid,
					 @Field("udid") String udid,
					 @Field("os") String os,
					 RestCallback<ResUserDevices> callback);

	@FormUrlEncoded
	@POST(APIManager.USER_DRIFT_LOGS)
	void driftLogs(@Header("x-access-token") String access_token,
				   @Field("deviceId") String device,
				   @Field("drift") String drift,
				   RestCallback<ResUserDevices> callback);

	@GET(APIManager.USER_FIND_APPLCATION)
	void userFindApplication(
			@Path("email") String email,
			@Query("email") boolean isEmail,
			RestCallback<String> callback
	);

	@FormUrlEncoded
	@POST(APIManager.USER_VERIFY_IDENTITY)
	void verifyIdentity(@Field("email") String email,
						@Field("verificationCode") String verificationCode,
						RestCallback<ResLoginWithCode> callback);

	public static class ResLoginForSuccess {
		public int _id;
		public String firstName;
		public String lastName;
		public String nickname;
		public String email;
		public String userType;
		public String language;
		public int groupId;
		public String activationToken;
		public ResLoginGroup group;
		public ResLoginTeamArray teams;
		public int id;
		public String access_token;
		public String userIP;
	}

	public static class ResLoginGroup {
		public String name;
		public String startDate;
		public String endDate;
		public String height;
		public String weight;
		public String stride;
		public String message;
	}

	public static class ResLoginTeam {
		public int _id;
		public String name;
		public String grade;
		public String description;
		public String imageSrc;
		public int studentCount;
		public String startDate;
		public String endDate;
		public String height;
		public String weight;
		public String stride;
		public String message;
	}

	public static class ResSendResetLinkForSuccess {
		public int status;
		public String message;
	}

	public static class ResLoginTeamArray {
		public ArrayList<ResLoginTeam> teams;
	}

	public static class ResUserDevices {
		public long _id;
		public String device;
		public String imei;
		public String iccid;
		public String versionCode;
		public String createdAt;
		public String updatedAt;
		public long userId;
	}

	public static class ResLogout {
	}

	public static class ResLoginGroupDeserializer implements JsonDeserializer<ResLoginGroup> {
		@Override
		public ResLoginGroup deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
				throws JsonParseException {
			// Get the "group" element from the parsed JSON
			// JsonElement group = je.getAsJsonObject().get("group");

			// Deserialize it. You use a new instance of Gson to avoid infinite recursion
			// to this deserializer
			//return new Gson().fromJson(group, ResLoginGroup.class);
			return new Gson().fromJson(je, ResLoginGroup.class);
		}
	}

	public static class ResLoginTeamArrayDeserializer implements JsonDeserializer<ResLoginTeamArray> {
		@Override
		public ResLoginTeamArray deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
				throws JsonParseException {
			// Get the "group" element from the parsed JSON
			//JsonArray teams = je.getAsJsonObject().getAsJsonArray("teams");
			JsonArray teams = je.getAsJsonArray();
			ResLoginTeamArray array = new ResLoginTeamArray();
			array.teams = new ArrayList<ResLoginTeam>();

			// Deserialize it. You use a new instance of Gson to avoid infinite recursion
			// to this deserializer
			for (int i = 0; i < teams.size(); i++) {
				ResLoginTeam team = new Gson().fromJson(teams.get(i), ResLoginTeam.class);
				array.teams.add(team);
			}
			return array;
		}
	}

	public static class ResSignup {
		public boolean isActive;
		public int _id;
		public String email;
		public String firstName;
		public String lastName;
		public String userType;
		public String nickname;
		public String language;
		public int groupId;
		public String activationToken;
		public String fullName;
		public int id;
		public ResSignupGroup group;
		public String access_token;
		public String userIP;
	}

	public static class ResSignupGroup {
		public int _id;
		public String name;
		public String mdr_pid;
		public String regCode;
		public String startDate;
		public String endDate;
		public String height;
		public String weight;
		public String stride;
		public String message;
		public String createdAt;
		public String updateAt;
		public int cityId;
	}

	public static class ResUserUpdate {
		public int _id;
		public String firstName;
		public String lastName;
		public String email;
		public String userType;
		public String activationToken;
		public String sendEmailAt;
		public boolean emailSent;
		public String hostName;
		public boolean isActive;
		public String createdAt;
		public String updatedAt;
		public int groupId;
		public String nickname;
	}

	public static class ResLoginWithCode {
		public int _id;
		public String firstName;
		public String lastName;
		public String nickName;
		public String language;
		public String email;
		public String userType;
		public int groupId;
		public String address1;
		public String address2;
		public String city;
		public String state;
		public String postCode;
		public String title;
		public int contactPreference;
		public String cellPhone;
		public ResLoginGroup group;
		public int id;
		public ArrayList<ResLoginTeam> teams;
		public String access_token;
		public String userIP;
	}

	public static class ResSignupGroupDeserializer implements JsonDeserializer<ResSignupGroup> {
		@Override
		public ResSignupGroup deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
				throws JsonParseException {
			// Get the "group" element from the parsed JSON
			// JsonElement group = je.getAsJsonObject().get("group");

			// Deserialize it. You use a new instance of Gson to avoid infinite recursion
			// to this deserializer
			//return new Gson().fromJson(group, ResLoginGroup.class);
			return new Gson().fromJson(je, ResSignupGroup.class);
		}
	}

	public static class ResValidatePassword {
		public boolean validate;
	}


	class ResFindApplication {
	}

}
