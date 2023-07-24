package org.unicefkidpower.kid_power.Model.MicroService.WebService;

import com.google.gson.annotations.SerializedName;

import org.unicefkidpower.kid_power.Model.Structure.KPHSyncSnapshots;
import org.unicefkidpower.kid_power.Model.Structure.KPHUploadActivityData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMission;

import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Implementation of Retrofit Endpoint
 * Created by Dayong Li on 22/08/15.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public interface SyncRestEndpoint {
	@GET("/api/v1/snapshots/latest/device/{deviceId}/user/{userId}")
	void getLastSnapshot(@Path("deviceId") String deviceId,
						 @Path("userId") int userId,
						 RestCallback<KPHSyncSnapshots> callback);

	@POST("/api/v1/command/uploadActivityDetails")
	void uploadActivityDetails(@Body KPHUploadActivityData uploadDates,
							   RestCallback<_SyncSnapResult> callback);

	//////////////////////////////////////////////////////////////////
	@FormUrlEncoded
	@POST("/api/v1/snapshots/report/missionEnabled")
	void missionEnabled(@Field("userId") int userId,
						@Field("missionId") long missionId,
						@Field("missionEnabled") boolean missionEnabled,
						RestCallback<_MissionResult> callback);

	@FormUrlEncoded
	@POST("/api/v1/snapshots/report/missionDisabled")
	void missionDisabled(@Field("userId") int userId,
						 @Field("missionId") long missionId,
						 @Field("missionDisabled") boolean missionDisabled,
						 RestCallback<_MissionResult> callback);


	class _MissionResult {
		@SerializedName("status")
		public String status;

		@SerializedName("mission")
		public KPHUserMission mission;
	}


	class _SyncSnapResult {
		@SerializedName("status")
		public String status;

		@SerializedName("snapshot")
		public KPHSyncSnapshots snapshot;

	}

}
