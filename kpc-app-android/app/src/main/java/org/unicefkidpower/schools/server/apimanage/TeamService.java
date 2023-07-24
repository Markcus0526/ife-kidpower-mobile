package org.unicefkidpower.schools.server.apimanage;

import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.json.JSONObject;
import org.unicefkidpower.schools.context.UserContext;

import java.lang.reflect.Type;
import java.util.ArrayList;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

/**
 * Created by donal_000 on 1/26/2015.
 */
public interface TeamService {
	@GET(APIManager.TEAM_GETALLTEAMSBYUSERID)
	void getAllTeamsByUserId(@Header("x-access-token") String access_token,
							 @Path("id") int id,
							 RestCallback<ResGetAllTeamByUserId> callback);

	@FormUrlEncoded
	@POST(APIManager.TEAM_TEAMS)
	void createTeam(@Header("x-access-token") String access_token,
					@Field("name") String name,
					@Field("userId") int userId,
					@Field("grade") String grade,
					RestCallback<ResCreateTeam> callback);

	@FormUrlEncoded
	@PUT(APIManager.TEAM_UPDATE)
	void updateTeam(@Header("x-access-token") String access_token,
					@Path("id") String id,
					@Field("name") String name,
					@Field("userId") int userId,
					@Field("grade") String grade,
					RestCallback<ResCreateTeam> callback);

	@POST(APIManager.TEAM_TEAMSUMMARY)
	void getTeamSummaryByTeamId(@Header("x-access-token") String access_token,
								@Header("version") String version,
								@Path("teamId") int teamId,
								RestCallback<ResTeamSummaryByTeamId> callback);

	@FormUrlEncoded
	@POST(APIManager.LEADERBOARD_TEAM)
	void getLeaderboardByTeamId(@Header("x-access-token") String access_token,
								@Field("teamId") int teamId,
								@Field("page") int page,
								@Field("searchText") String searchText,
								RestCallback<ResLeaderboardList> callback);

	@GET(APIManager.LEADERBOARD_NEARBY)
	void getLeaderboardNearbyByTeamId(@Header("x-access-token") String access_token,
								@Path("id") int id,
								RestCallback<ResLeaderboardList> callback);

	@GET(APIManager.LEADERBOARD_FAVORITE)
	void getLeaderboardFovaritesTeams(@Header("x-access-token") String access_token,
										 RestCallback<ResLeaderboardList> callback);

	class ResGetAllTeamByUserId {
		public ArrayList<ResGetAllTeamByUserIdTeam> teams;
	}

	class ResGetAllTeamByUserIdTeam {
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

	class ResGetAllTeamByUserIdDeserializer implements JsonDeserializer<ResGetAllTeamByUserId> {
		@Override
		public ResGetAllTeamByUserId deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
				throws JsonParseException {
			// Get the "group" element from the parsed JSON
			//JsonArray teams = je.getAsJsonObject().getAsJsonArray("teams");
			JsonArray teams = je.getAsJsonArray();
			ResGetAllTeamByUserId array = new ResGetAllTeamByUserId();
			array.teams = new ArrayList<ResGetAllTeamByUserIdTeam>();

			// Deserialize it. You use a new instance of Gson to avoid infinite recursion
			// to this deserializer
			for (int i = 0; i < teams.size(); i++) {
				ResGetAllTeamByUserIdTeam team = new Gson().fromJson(teams.get(i), ResGetAllTeamByUserIdTeam.class);
				array.teams.add(team);
			}
			return array;
		}
	}

	class ResCreateTeam {
		public String grade;
		public String description;
		public String imageSrc;
		public boolean hideOnHomePage;
		public int studentCount;
		public String lastSyncDate;
		public int _id;
		public String name;
		public int userId;
		public String startDate;
		public String endDate;
		public String height;
		public String weight;
		public String stride;
		public String message;
		public String updatedAt;
		public String createdAt;
		public String fullName;
	}

	class ResTeamSummaryByTeamId {
		public ResTeamSummary resTeamSummary;
//        public ArrayList<ResLeaderboard> resLeaderboards;
	}


	class ResTeamSummary {
		public int rank;
		public int _id;
		public String name;
		public String grade;
		public int groupId;
		public String groupName;
		public int teacherId;
		public String teacherName;
		public String averagePoints;
		public int averagePackets;
		public int totalPackets;
		public int averageStepsPerStudentDay;
		public String startDate;
		public String endDate;
		public String height;
		public String weight;
		public String stride;
		public String deviceMessage;
		public int cityId;
		public String cityName;
		public int totalPoints;
		public int totalSteps;
		public String totalDistance;
		public String unit;
		public int totalDays;
		public int totalStudents;
		public String dailyAverage;

		public StudentService.ResGetStudentWithDisplayMessage _displayMessage;
	}


	class ResLeaderboardList {
		//public int totalPages;
		//public int myTeamsPage;
		//public int totalTeams;
		public ArrayList<ResLeaderboard> leaderboard;
	}


	class ResLeaderboard {
		public int rank;
		public int _id;
		public String name;
		public String grade;
		public int groupId;
		public String groupName;
		public int teacherId;
		public String teacherName;
		public float averagePoints;
		public float averagePackets;
		public int totalPackets;
		public int averageStepsPerStudentDay;
		public boolean isFavorite;
	}

	class ResTeamSummaryByTeamIdDeserializer implements JsonDeserializer<ResTeamSummaryByTeamId> {
		@Override
		public ResTeamSummaryByTeamId deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
				throws JsonParseException {
			ResTeamSummaryByTeamId resTeamSummaryByTeamId = new ResTeamSummaryByTeamId();

			JsonObject resObject = je.getAsJsonObject().getAsJsonObject("teamSummary");
			resTeamSummaryByTeamId.resTeamSummary = new Gson().fromJson(resObject, ResTeamSummary.class);

			JsonObject disMesObject = resObject.getAsJsonObject().getAsJsonObject("displayMessage");
			resTeamSummaryByTeamId.resTeamSummary._displayMessage = new Gson().fromJson(disMesObject, StudentService.ResGetStudentWithDisplayMessage.class);

/*
			JsonArray arrLeaderboards = je.getAsJsonObject().getAsJsonArray("leaderboard");
            resTeamSummaryByTeamId.resLeaderboards = new ArrayList<>();

            // Deserialize it. You use a new instance of Gson to avoid infinite recursion
            // to this deserializer
            for (int i = 0; i < arrLeaderboards.size(); i++) {
                ResLeaderboard resLeaderboard = new Gson().fromJson(arrLeaderboards.get(i), ResLeaderboard.class);
                resTeamSummaryByTeamId.resLeaderboards.add(resLeaderboard);
            }
*/

			return resTeamSummaryByTeamId;
		}
	}


	class ResLeaderboardListDeserializer implements JsonDeserializer<ResLeaderboardList> {
		@Override
		public ResLeaderboardList deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
				throws JsonParseException {
			ResLeaderboardList result = new ResLeaderboardList();

			JsonObject resObject = je.getAsJsonObject();

			//result.totalPages = resObject.get("totalPages").getAsInt();
			//result.myTeamsPage = resObject.get("myTeamsPage").getAsInt();
			//result.totalTeams = resObject.get("totalTeams").getAsInt();

			JsonArray arrLeaderboards = resObject.getAsJsonArray("leaderboard");
			result.leaderboard = new ArrayList<>();
			for (int i = 0; i < arrLeaderboards.size(); i++) {
				ResLeaderboard leaderboard = new Gson().fromJson(arrLeaderboards.get(i), ResLeaderboard.class);
				result.leaderboard.add(leaderboard);
			}

			return result;
		}
	}


}
