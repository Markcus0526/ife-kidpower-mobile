package org.unicefkidpower.schools.server.apimanage;

import org.unicefkidpower.schools.model.UploadActivityData;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by donal_000 on 2/3/2015.
 */
public interface CommandService {
	@POST(APIManager.COMMAND_UPDATESTATS)
	void updateStats(
			@Header("x-access-token") String access_token,
			@Path("teamId") int teamId,
			RestCallback<ResUpdateStats> callback);

	@FormUrlEncoded
	@POST(APIManager.COMMAND_EXTRAPOINTTEAM)
	void extraPointTeam(
			@Header("x-access-token") String access_token,
			@Field("teamId") int teamId,
			@Field("points") int points,
			RestCallback<List<ResExtraPointTeam>> callback
	);

	@FormUrlEncoded
	@POST(APIManager.COMMAND_EXTRAPOINTSTUDENT)
	void extraPointStudent(
			@Header("x-access-token") String access_token,
			@Field("studentId") int studentId,
			@Field("points") int points,
			RestCallback<ResExtraPointStudent> callback
	);

	@GET(APIManager.USER_ALLBANDS)
	void getAllBands(
			@Header("x-access-token") String access_token,
			@Path("userId") long userId,
			RestCallback<List<FilteringDevice>> deviceIds
	);

	@POST(APIManager.COMMAND_UPLOADACTIVITYDETAIL)
	void uploadActivityDetail(@Body UploadActivityData uploadDates,
							  RestCallback<ResUploadDailyData> callback
	);

	public static class ResUploadDailySummary {
		//
	}

	public static class ResUpdateStats {
		//
	}

	public static class ResExtraPointTeam {
		//
	}

	public static class ResExtraPointStudent {
		//
	}

	public static class ResUploadDailyData {
		//
	}

	public static class FilteringDevice {
		public String deviceId;
	}
}
