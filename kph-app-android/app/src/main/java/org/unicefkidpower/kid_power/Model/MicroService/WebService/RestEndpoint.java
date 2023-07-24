package org.unicefkidpower.kid_power.Model.MicroService.WebService;

import org.json.JSONObject;
import org.unicefkidpower.kid_power.Model.Structure.BaseResponse;
import org.unicefkidpower.kid_power.Model.Structure.BaseStringStatusResponse;
import org.unicefkidpower.kid_power.Model.Structure.KPHDeviceInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHFacebook;
import org.unicefkidpower.kid_power.Model.Structure.KPHLogoutResult;
import org.unicefkidpower.kid_power.Model.Structure.KPHTracker;
import org.unicefkidpower.kid_power.Model.Structure.KPHBlock;
import org.unicefkidpower.kid_power.Model.Structure.KPHCheerInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHCreditVerify;
import org.unicefkidpower.kid_power.Model.Structure.KPHDeLinkTrackerResponse;
import org.unicefkidpower.kid_power.Model.Structure.KPHDelightInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHFollower;
import org.unicefkidpower.kid_power.Model.Structure.KPHMessage;
import org.unicefkidpower.kid_power.Model.Structure.KPHMission;
import org.unicefkidpower.kid_power.Model.Structure.KPHTravelLogUnreadItemCount;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserEmailAvailability;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMission;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMissionStats;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserNameAvailability;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserStats;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserSummary;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserTravelLogResponse;
import org.unicefkidpower.kid_power.Model.Structure.KPHVersion;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Implementation of Retrofit Endpoint
 * Created by Dayong Li on 22/08/15.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public interface RestEndpoint {
	@FormUrlEncoded
	@POST("/authenticate")
	void authenticate(@Field("email") String email,
					  @Field("device_uid") String deviceUId,
					  RestCallback<String> callback);

	@GET("/app/versionCheck")
	void checkVersion(@Query("device_uid") String dev_uid,
					  @Query("platform") String platform,
					  @Query("version") String version,
					  RestCallback<String> callback);

	@FormUrlEncoded
	@POST("/api/v1/users/isHandleAvailable")
	void isHandleAvailable(@Field("handle") String handle,
						   RestCallback<KPHUserNameAvailability> callback);

	@FormUrlEncoded
	@POST("/api/v1/users/isEmailAvailable")
	void isEmailAvailable(@Field("email") String email,
						  RestCallback<KPHUserEmailAvailability> callback);

	@FormUrlEncoded
	@POST("/api/v1/users/signup")
	void signup(@Field("handle") String handle,
				@Field("email") String email,
				@Field("password") String password,
				@Field("friendlyName") String friendlyName,
				@Field("dob") String dob,
				@Field("avatarId") String avatarId,
				RestCallback<KPHUserData> callback);

	@FormUrlEncoded
	@POST("/api/v1/users/signup")
	void signup(@Field("handle") String handle,
				@Field("email") String email,
				@Field("password") String password,
				@Field("friendlyName") String friendlyName,
				@Field("gender") String gender,
				@Field("dob") String dob,
				@Field("avatarId") String avatarId,
				RestCallback<KPHUserData> callback);

	@POST("/api/v1/users/signup")
	void signupFBAccount(@Body KPHFacebook facebook,
						 RestCallback<KPHUserData> callback);

	@FormUrlEncoded
	@POST("/api/v1/users/createAgentAccount")
	void createAgentAccount(@Field("email") String email,
							@Field("password") String password,
							@Field("handle") String handle,
							@Field("friendlyName") String friendlyName,
							@Field("gender") String gender,
							@Field("dob") String dob,
							@Field("avatarIid") String avatarId,
							RestCallback<String> callback);

	@FormUrlEncoded
	@POST("/api/v1/users/verifyAdult")
	void verifyAdult(@Field("stripeCustomerToken") String stripeCustomerToken,
					 @Field("postCode") String zipCode,
					 RestCallback<KPHUserData> callback);

	@FormUrlEncoded
	@POST("/api/v1/users/createSubAccount")
	void createSubAccount(@Field("parentId") int parentId,
						  @Field("handle") String handle,
						  @Field("password") String password,
						  @Field("friendlyName") String friendlyName,
						  @Field("gender") String gender,
						  @Field("dob") String dob,
						  @Field("avatarId") String avatarId,
						  RestCallback<KPHUserData> callback);

	@FormUrlEncoded
	@POST("/api/v1/users/createSubAccount")
	void createSubAccount(@Field("parentId") int parentId,
						  @Field("handle") String handle,
						  @Field("password") String password,
						  @Field("friendlyName") String friendlyName,
						  @Field("dob") String dob,
						  @Field("avatarId") String avatarId,
						  RestCallback<KPHUserData> callback);

	@GET("/api/v1/users")
	void getUsers(@Query("parentId") String parentId,
				  @Query("userType") String userType,
				  RestCallback<String> callback);

	@GET("/api/v1/users/{userId}")
	void getUser(
			@Path("userId") int userId,
			RestCallback<KPHUserData> callback
	);

	@FormUrlEncoded
	@PUT("/api/v1/users/{userId}")
	void updateUser(
			@Path("userId") int userId,
			@Field("handle") String handle,
			@Field("email") String email,
			@Field("friendlyName") String FriendlyName,
			@Field("gender") String gender,
			@Field("dob") String dob,
			@Field("avatarId") String avatarId,
			RestCallback<KPHUserData> callback
	);

	@FormUrlEncoded
	@PUT("/api/v1/users/{userId}")
	void updateUser(
			@Path("userId") int userId,
			@Field("handle") String handle,
			@Field("friendlyName") String FriendlyName,
			@Field("gender") String gender,
			@Field("dob") String dob,
			@Field("avatarId") String avatarId,
			RestCallback<KPHUserData> callback
	);


	@FormUrlEncoded
	@PUT("/api/v1/users/{userId}")
	void updatePassword(
			@Path("userId") int userId,
			@Field("password") String password,
			RestCallback<KPHUserData> callback
	);


	//Delete /api/v1/users/:userId

	// getV1 the latest build version to update
	//
	@GET("/api/v1/versions/latest/android")
	void version(
			RestCallback<KPHVersion> callback
	);

	@FormUrlEncoded
	@POST("/api/v1/login")
	void login(
			@Field("username") String username,
			@Field("password") String password,
			RestCallback<KPHUserData> callback
	);

	@POST("/api/v1/login")
	void loginFBAccount(
			@Body KPHFacebook facebook,
			RestCallback<KPHUserData> callback
	);

	@FormUrlEncoded
	@POST("/api/v1/users/loginOtherUser")
	void loginOtherUser(
			@Field("userId") long userId,
			RestCallback<KPHUserData> callback
	);

	@FormUrlEncoded
	@POST("/api/v1/users/loginOtherUser")
	void loginOtherUser(
			@Field("userId") long userId,
			@Field("password") String password,
			RestCallback<KPHUserData> callback
	);

	@POST("/api/v1/logout")
	void logout(
			@Header("user") long userId,
			RestCallback<KPHLogoutResult> callback);

	@FormUrlEncoded
	@POST("/api/v1/users/sendResetLink")
	void sendResetLink(@Field("username") String username,
					   RestCallback<BaseResponse> callback);

	@FormUrlEncoded
	@POST("/api/v1/users/getUserByRequestId")
	void getUserByRequestId(@Field("requestId") String requestId,
							RestCallback<String> callback);

	@FormUrlEncoded
	@POST("/api/v1/users/resetPassword")
	void resetPassword(@Field("requesstId") String requestId,
					   @Field("handle") String handle,
					   @Field("newPassword") String newPassword,
					   RestCallback<String> callback);

	@FormUrlEncoded
	@POST("/api/v1/users/byDeviceIds")
	void byDeviceIds(
			@Field("devices") String devices,
			RestCallback<String> callback
	);

	@FormUrlEncoded
	@POST("/api/v1/userDevices")
	void createDeviceRecord(
			@Field("device") String deviceName,
			@Field("imei") String imei,
			@Field("iccid") String iccid,
			@Field("imsi") String imsi,
			@Field("udid") String udid,
			@Field("versionCode") String versionCode,
			@Field("userId") int userId,
			@Field("pushId") String pushId,
			RestCallback<KPHDeviceInformation> callback
	);


	@FormUrlEncoded
	@POST("/api/v1/users/searchUsers")
	void searchUsers(@Field("userId") int userId,
					 @Field("searchText") String searchText,
					 RestCallback<KPHUserSummary> callback);

	@GET("/api/v1/users/childrenSummary/{parentId}")
	void getChildrenSummary(
			@Path("parentId") long parentId,
			RestCallback<List<KPHUserSummary>> callback
	);

	////////////////////////////////////////////////////////////////

	@FormUrlEncoded
	@POST("/api/v1/trackers")
	void setTracker(@Field("name") String deviceName,
					@Field("deviceId") String deviceId,
					@Field("userId") int userId,
					@Field("type") String type,
					@Field("version") String version,
					RestCallback<KPHTracker> callback);

	@GET("/api/v1/trackers")
	void getTrackers(RestCallback<List<KPHTracker>> callback);

	@GET("/api/v1/trackers/{recordId}")
	void getTrackerById(@Path("recordId") long recordId,
						RestCallback<KPHTracker> callback);

	@GET("/api/v1/trackers/device/{deviceId}")
	void getTrackerByDeviceId(@Path("deviceId") String deviceId,
							  RestCallback<List<KPHTracker>> callback);

	@GET("/api/v1/trackers/user/{userid}")
	void getTrackersByUserId(@Path("userid") long userid,
							 RestCallback<List<KPHTracker>> callback);

	@GET("/api/v1/trackers/current/user/{userid}")
	void getCurrentTrackerByUserId(@Path("userid") long userid,
								   RestCallback<KPHTracker> callback);

	@DELETE("/api/v1/trackers/{recordId}")
	void deleteTrackerById(@Path("recordId") long recordId,
						   RestCallback<String> callback);

	@GET("/api/v1/trackers/summary/user/{id}")
	void getUserTrackerStats(@Path("id") long userId,
							 RestCallback<KPHUserStats> callback);

	@FormUrlEncoded
	@POST("/api/v1/users/deLinkBand")
	void deLinkTracker(
			@Field("userId") int userId,
			@Field("deviceId") String deviceId,
			RestCallback<KPHDeLinkTrackerResponse> callback
	);

	/////////////////////////////////////////////////////////////////

	@GET("/api/v1/missions")
	void getMissions(
			RestCallback<List<KPHMission>> callback
	);

	@GET("/api/v1/delights")
	void getDelights(
			RestCallback<List<KPHDelightInformation>> callback
	);

	@GET("/api/v1/cheers")
	void getCheers(
			RestCallback<List<KPHCheerInformation>> callback
	);

	/**
	 * @GET("/api/v1/missions/{missionId}") void getMissionByMissionId(
	 * @Path("missionId") int missionId,
	 * RestCallback<KPHMission> callback
	 * );
	 * @FormUrlEncoded
	 * @POST("/api/v1/missions") void createMission(
	 * @Field("name") String name,
	 * @Field("goal") int goal,
	 * @Field("timeToComplete") int timeToComplete,
	 * RestCallback<KPHMission> callback
	 * );
	 * @PUT("/api/v1/missions/{missionId}") void updateMission(
	 * @Path("missionId") int missionId,
	 * RestCallback<KPHMission> callback
	 * );
	 * @DELETE("/api/v1/missions/{missionId}") void deleteMissionByMissionId(
	 * @Path("missionId") int missionId,
	 * RestCallback<KPHMission> callback
	 * );
	 **/
	//////////////////////////////////////////////////////////////////
	@FormUrlEncoded
	@POST("/api/v1/userMissions")
	void createUserMission(
			@Field("missionId") int missionId,
			@Field("userId") int userId,
			RestCallback<KPHUserMission> callback
	);

	@GET("/api/v1/userMissions/{userMissionId}")
	void getUserMissionByUserMissionId(
			@Path("userMissionId") int userMissionId,
			RestCallback<KPHUserMission> callback
	);

	@GET("/api/v1/userMissions/all/user/{userId}")
	void getUserMissionsAllForUser(
			@Path("userId") long userId,
			RestCallback<List<KPHUserMission>> callback
	);

	@GET("/api/v1/userMissions/current/user/{userId}")
	void getCurrentMissionForUser(
			@Path("userId") long userId,
			RestCallback<KPHUserMission> callback
	);

	@PUT("/api/v1/userMissions/{userMissionId}")
	void updateUserMissionByUserMissionId(
			@Path("userMissionId") int userMissionId,
			RestCallback<KPHUserMission> callback
	);

	@DELETE("/api/v1/userMissions/{userMissionId}")
	void deleteUserMissionByUserMissionId(
			@Path("userMissionId") int userMissionId,
			RestCallback<KPHUserMission> callback
	);

	@GET("/api/v1/userMissions/progress/user/{userId}")
	void getUserMissionProgressForUser(
			@Path("userId") long userId,
			RestCallback<List<KPHUserMissionStats>> callback
	);

	//////////////////////////////////////////////////////////////////
	@GET("/api/v1/users/travelLog/{userId}")
	void getUserTravelLog(
			@Path("userId") long userId,
			RestCallback<KPHUserTravelLogResponse> callback
	);

	@GET("/api/v1/users/message/markRead/{messageId}")
	void markTravelLogItemAsRead(
			@Path("messageId") long messageId,
			RestCallback<KPHTravelLogUnreadItemCount> callback
	);

	///////////////////////////Follow/////////////////////////////////

	@FormUrlEncoded
	@POST("/api/v1/followers")
	void follow(
			@Field("followerId") int followerId,
			@Field("followingId") int followingId,
			RestCallback<KPHFollower> callback
	);

	@FormUrlEncoded
	@POST("/api/v1/followers/un-follow")
	void unfollow(
			@Field("followerId") int followerId,
			@Field("followingId") int followingId,
			RestCallback<JSONObject> callback
	);

	@GET("/api/v1/followers/following/{followerId}")
	void getFollowingsList(
			@Path("followerId") int followerId,
			RestCallback<List<KPHUserSummary>> callback
	);

	@GET("/api/v1/followers/follower/{followingId}")
	void getFollowersList(
			@Path("followingId") int followingId,
			RestCallback<List<KPHUserSummary>> callback
	);

	///////////////////////////Blocks////////////////////////////////////
	@FormUrlEncoded
	@POST("/api/v1/blocks")
	void blockUser(
			@Field("blockerId") int blockerId,
			@Field("blockedId") int blockedId,
			RestCallback<KPHBlock> callback
	);

	@FormUrlEncoded
	@POST("/api/v1/blocks/un-block")
	void unblockUser(
			@Field("blockerId") int blockerId,
			@Field("blockedId") int blockedId,
			RestCallback<BaseStringStatusResponse> callback
	);

	@GET("/api/v1/blocks/blocked/{userId}")
	void getBlockedUserList(
			@Path("userId") long userId,
			RestCallback<List<KPHBlock>> callback
	);

	@GET("/api/v1/blocks/blockers/{userId}")
	void getBlockerUserList(
			@Path("userId") long userId,
			RestCallback<List<KPHBlock>> callback
	);

	@POST("/api/v1/users/verifyPurchase")
	void verifyPurchase(
			@Body KPHCreditVerify uploadDates,
			RestCallback<KPHCreditVerify.KPHCreditVerifyResult> callback
	);

	///////////////////////////Cheers////////////////////////////////////
	@FormUrlEncoded
	@POST("/api/v1/users/message")
	void sendMessage(
			@Field("recipientId") int recipientId,
			@Field("type") String type,
			@Field("senderName") String senderName,
			@Field("message") String message,
			RestCallback<KPHMessage> callback
	);

	@FormUrlEncoded
	@POST("/api/v1/users/message")
	void sendMessage(
			@Field("recipientId") int recipientId,
			@Field("type") String type,
			RestCallback<KPHMessage> callback
	);

	@FormUrlEncoded
	@POST("/api/v1/users/message")
	void sendMessage(
			@Field("recipientId") int recipientId,
			@Field("type") String type,
			@Field("cheerId") long cheerId,
			RestCallback<KPHMessage> callback
	);
}
