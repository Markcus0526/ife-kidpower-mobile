package org.unicefkidpower.schools.server.apimanage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import org.unicefkidpower.schools.define.config;

import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;

public class APIManager {
	// v2 apis
	public static final String API_PREFIX = "/api/v2";

	// user
	public static final String USER_LOGIN						= API_PREFIX + "/login";
	public static final String USER_SIGNUP						= API_PREFIX + "/users/signup";
	public static final String USER_LOGOUT						= API_PREFIX + "/logout";
	public static final String USER_UPDATE						= API_PREFIX + "/users/{id}";
	public static final String USER_VALIDATEPASSWORD			= API_PREFIX + "/validatePassword";
	public static final String USER_SENDRESETLINK				= API_PREFIX + "/sendResetLink";
	public static final String USER_CONFIGDEVICE				= API_PREFIX + "/userDevices";
	public static final String USER_ALLBANDS					= API_PREFIX + "/users/bands/{userId}";
	public static final String USER_FIND_APPLCATION				= API_PREFIX + "/users/email/{email}";
	public static final String USER_VERIFY_IDENTITY				= API_PREFIX + "/users/loginUserWithCode";

	// drift logs
	public static final String USER_DRIFT_LOGS					= API_PREFIX + "/driftLogs";

	// group
	public static final String GROUP_REGCODE					= API_PREFIX + "/groups/regCode/{code}";

	// student
	public static final String STUDENT_BYDEVICEIDS				= API_PREFIX + "/students/byDeviceIds";
	public static final String STUDENT_STUDENTS					= API_PREFIX + "/students";
	public static final String STUDENT_UPDATE					= API_PREFIX + "/students/{id}";
	public static final String STUDENT_GETSTUDENTSBYTEAMID		= API_PREFIX + "/students/getStudentsByTeamId/{teamId}";

	// team
	public static final String TEAM_GETALLTEAMSBYUSERID			= API_PREFIX + "/teams/getAllTeamsByUserId/{id}";
	public static final String TEAM_TEAMS						= API_PREFIX + "/teams";
	public static final String TEAM_UPDATE						= API_PREFIX + "/teams/{id}";
	public static final String TEAM_TEAMSUMMARY					= API_PREFIX + "/teamSummary/{teamId}";

	// leaderboard
	public static final String LEADERBOARD_TEAM					= API_PREFIX + "/leaderboard/all";
	public static final String LEADERBOARD_NEARBY				= API_PREFIX + "/leaderboard/nearby/team/{id}";
	public static final String LEADERBOARD_FAVORITE				= API_PREFIX + "/favorites/teams";

	// command
	public static final String COMMAND_UPLOADDAILYSUMMARY		= API_PREFIX + "/command/uploadDailySummary";
	public static final String COMMAND_UPDATESTATS				= API_PREFIX + "/command/updateStats/team/{teamId}";
	public static final String COMMAND_EXTRAPOINTSTUDENT		= API_PREFIX + "/command/extraPoint/student";
	public static final String COMMAND_EXTRAPOINTTEAM			= API_PREFIX + "/command/extraPoint/team";
	public static final String COMMAND_UPLOADDAILYDATA			= API_PREFIX + "/command/uploadActivityDetails/payload/{payload}";
	public static final String COMMAND_UPLOADACTIVITYDETAIL		= API_PREFIX + "/command/uploadActivityDetail";

	// program
	public static final String PROGRAM_REGCODE					= API_PREFIX + "/programs/regCode";

	// version check
	public static final String VERSION_CHECK					= API_PREFIX + "/versions/get/latest";

	private static final String DEVELOP_URL						= "https://kpc-main-staging.herokuapp.com";//"https://staging.unicefkidpower.org";
	private static final String PRODUCT_URL						= "https://go.unicefkidpower.org";


	// reset api
	// /api/command/activities/resetLastSyncDate/B4:99:4C:67:66:E4?date=2014-11-10
	public static Client provideClient(OkHttpClient client) {
		return new OkClient(client);
	}


	public static String provideHostUrl() {
		return !config.IS_PRODUCT
				? DEVELOP_URL
				: PRODUCT_URL;
	}

	public static Endpoint provideEndpoint() {
		return !config.IS_PRODUCT
				? Endpoints.newFixedEndpoint(DEVELOP_URL, "UNICEF Kid Power Staging Url")
				: Endpoints.newFixedEndpoint(PRODUCT_URL, "UNICEF Kid Power Production Url");
	}


	public static Converter provideConverter(Gson gson) {
		return new GsonConverter(gson);
	}


	public static Gson provideGson() {
		return new GsonBuilder()
				.registerTypeAdapter(UserService.ResLoginGroup.class, new UserService.ResLoginGroupDeserializer())
				.registerTypeAdapter(UserService.ResLoginTeamArray.class, new UserService.ResLoginTeamArrayDeserializer())
				.registerTypeAdapter(UserService.ResSignupGroup.class, new UserService.ResSignupGroupDeserializer())
				/*
				.registerTypeAdapter(ProgramService.ResRegCodeCityArray.class, new ProgramService.ResRegCodeCityArrayDeserializer())
				.registerTypeAdapter(ProgramService.ResRegCodeGroupArray.class, new ProgramService.ResRegCodeGroupArrayDeserializer())
				*/
				.registerTypeAdapter(TeamService.ResGetAllTeamByUserId.class, new TeamService.ResGetAllTeamByUserIdDeserializer())
				.registerTypeAdapter(TeamService.ResTeamSummaryByTeamId.class, new TeamService.ResTeamSummaryByTeamIdDeserializer())
				.registerTypeAdapter(TeamService.ResLeaderboardList.class, new TeamService.ResLeaderboardListDeserializer())

				.registerTypeAdapter(StudentService.ResGetStudentsByTeamId.class, new StudentService.ResGetAllStudentsByTeamIdDeserializer())
				.registerTypeAdapter(StudentService.ResGetStudentInfoWithTeam.class, new StudentService.ResGetStudentInfoWithTeamDeserializer())
				.registerTypeAdapter(StudentService.ResCreateStudentTeam.class, new StudentService.ResCreateStudentTeamDeserializer())
				.registerTypeAdapter(StudentService.ResGetStudentWithDisplayMessage.class, new StudentService.ResGetStudentWithDisplayMessageDeserializer())
				.create();
	}

	public static RestAdapter provideRestAdapter(Endpoint endpoint, Client client,
												 Converter converter) {
		return new RestAdapter.Builder()
				.setClient(client)
				.setEndpoint(endpoint)
				.setConverter(converter)
				.setLogLevel(/*BuildConfig.DEBUG
	            ? RestAdapter.LogLevel.FULL
	            : RestAdapter.LogLevel.NONE*/RestAdapter.LogLevel.FULL)
				.build();
	}

	public static UserService provideUserService(RestAdapter adapter) {
		return adapter.create(UserService.class);
	}

	public static GroupService provideGroupService(RestAdapter adapter) {
		return adapter.create(GroupService.class);
	}

	public static StudentService provideStudentService(RestAdapter adapter) {
		return adapter.create(StudentService.class);
	}

	public static TeamService provideTeamService(RestAdapter adapter) {
		return adapter.create(TeamService.class);
	}

	public static CommandService provideCommandService(RestAdapter adapter) {
		return adapter.create(CommandService.class);
	}

	public static ProgramService provideProgramService(RestAdapter adapter) {
		return adapter.create(ProgramService.class);
	}

}
