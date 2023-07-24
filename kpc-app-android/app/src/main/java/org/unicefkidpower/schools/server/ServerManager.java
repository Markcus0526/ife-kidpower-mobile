package org.unicefkidpower.schools.server;

import android.content.Context;
import android.text.TextUtils;

import com.squareup.okhttp.OkHttpClient;

import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.context.UserContext;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.model.DailyActivityData;
import org.unicefkidpower.schools.model.DailyActivityData.DetailData;
import org.unicefkidpower.schools.model.Student;
import org.unicefkidpower.schools.model.UploadActivityData;
import org.unicefkidpower.schools.model.UserManager;
import org.unicefkidpower.schools.server.apimanage.APIManager;
import org.unicefkidpower.schools.server.apimanage.CommandService;
import org.unicefkidpower.schools.server.apimanage.CommandService.FilteringDevice;
import org.unicefkidpower.schools.server.apimanage.ProgramService;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.StudentService;
import org.unicefkidpower.schools.server.apimanage.TeamService;
import org.unicefkidpower.schools.server.apimanage.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ServerManager {
	static final String TAG = "ServerManager";
	static final int TIMEOUT_APICALLING = 60; // seconds
	static ServerManager _instance;

	RestAdapter mAdapter;
	String _access_token;

	List<FilteringDevice> _registeredBands;

	private ServerManager() {
		// create client
		OkHttpClient okHttpClient = new OkHttpClient();
		okHttpClient.setReadTimeout(TIMEOUT_APICALLING * 1000, TimeUnit.MILLISECONDS);

		mAdapter = APIManager.provideRestAdapter(APIManager.provideEndpoint(),
				APIManager.provideClient(okHttpClient),
				APIManager.provideConverter(APIManager.provideGson()));

		_registeredBands = new ArrayList<>();
	}

	public static ServerManager sharedInstance() {
		if (_instance == null)
			_instance = new ServerManager();
		return _instance;
	}

	public static String getResponseAsString(Response response) {
		if (response == null)
			return null;
		if (response.getBody() == null)
			return null;
		try {
			if (response.getBody().in() == null)
				return null;
		} catch (Exception e) {
			return null;
		}

		//Try to get response body
		BufferedReader reader = null;
		StringBuilder sb = new StringBuilder();
		try {
			reader = new BufferedReader(new InputStreamReader(response.getBody().in()));

			String line;

			try {
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		String result = sb.toString();
		return result;
	}

	public static String getErrorMessageForRetrofitError(RetrofitError error, Context context) {
		if (error == null)
			return context.getString(R.string.error_unknown);
		if (error.getKind() == RetrofitError.Kind.CONVERSION) {
			return context.getString(R.string.error_parse_response);
		} else if (error.getKind() == RetrofitError.Kind.HTTP) {
			Response response = error.getResponse();
			//return getResponseAsString(response);
			//return context.getString(R.string.error_server);
			if (response == null)
				return context.getString(R.string.error_server);
			return getMessageForStatusCode(response.getStatus(), context);
		} else if (error.getKind() == RetrofitError.Kind.NETWORK) {
			return context.getString(R.string.error_network);
		} else if (error.getKind() == RetrofitError.Kind.UNEXPECTED) {
			return context.getString(R.string.error_unexpected);
		}
		return null;
	}

	public static String getMessageForStatusCode(int statusCode, Context context) {
		switch (statusCode) {
			case 400: // Bad request
				return context.getString(R.string.neterror_400);
			case 401: // Unauthorized
				return context.getString(R.string.neterror_401);
			case 402: // Payment Required
			case 403: // Forbidden
			case 404: // Not Found
			case 405: // Method Not Allowed
			case 406: // Not Acceptable
			case 407: // Proxy Authentication Required
			case 409: // Conflict
			case 410: // Gone
			case 411: // Length Required
			case 412: // Precondition Failed
			case 413: // Request Entity Too Large
			case 414: // Request-URI Too Long
			case 415: // Unsupported Media Type
			case 416: // Requested Range Not Satisfiable
			case 417: // Expectation Failed
			default:
				return context.getString(R.string.neterror_server);
			case 408: // Request Timeout
				return context.getString(R.string.neterror_408);
			case 503:
				return context.getString(R.string.neterror_503);
			case 500: // Internal Server Error
			case 501: // Not Implemented
			case 502: // Bad Gateway
			case 504: // Gateway Timeout
			case 505: // HTTP Version Not Supported
			case 511: // Network Authentication Required
				return context.getString(R.string.neterror_server);
		}
	}

	public String getHostUrl() {
		return APIManager.provideHostUrl();
	}

	public void setAccessToken(String accessToken) {
		_access_token = accessToken;
	}

	// login
	public void login(final String email, final String password, final RestCallback<UserService.ResLoginForSuccess> callback) {
		UserService userService = APIManager.provideUserService(mAdapter);
		userService.login(email, password,
				new RestCallback<UserService.ResLoginForSuccess>() {
					@Override
					public void failure(RetrofitError retrofitError, String message) {
						Logger.error(TAG, "login failed - email : %s, , error : %s", email, retrofitError);
						callback.failure(retrofitError);
					}

					@Override
					public void success(UserService.ResLoginForSuccess resLoginForSuccess, Response response) {
						_access_token = resLoginForSuccess.access_token;
						UserContext.sharedInstance().setUserLanguage(resLoginForSuccess.language);
						Logger.log(TAG, "login success, access_token : " + _access_token);
						callback.success(resLoginForSuccess, response);
					}
				});
	}

	// regCode
	public void regCode(final String regCode, final int version, final RestCallback<ProgramService.ResRegCode> callback) {
		ProgramService programService = APIManager.provideProgramService(mAdapter);
		programService.regCode(regCode, version, callback);
	}

	// signup
	public void signup(final int groupId, final String email, final String password, final String firstName,
					   final String lastName, final String nickname, final String userType,
					   final RestCallback<UserService.ResSignup> callback) {
		UserService userService = APIManager.provideUserService(mAdapter);
		userService.signup(groupId, email, password, firstName, lastName, userType, nickname,
				UserContext.sharedInstance().getAppLocale().getCountry(), new RestCallback<UserService.ResSignup>() {
					@Override
					public void success(UserService.ResSignup resSignup, Response response) {
						String access_token = resSignup.access_token;
						_access_token = access_token;
						UserContext.sharedInstance().setUserLanguage(resSignup.language);
						callback.success(resSignup, response);
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						callback.failure(retrofitError);
					}
				});
	}

	// update user info
	public void usernameupdate(final String firstName, final String lastName, final String nickname,
							   final RestCallback<UserService.ResUserUpdate> callback) {
		UserService userService = APIManager.provideUserService(mAdapter);
		userService.usernameupdate(firstName, lastName, nickname, _access_token, Integer.toString(UserManager.sharedInstance()._currentUser._id), new RestCallback<UserService.ResUserUpdate>() {
			@Override
			public void success(UserService.ResUserUpdate resUserUpdate, Response response) {
				callback.success(resUserUpdate, response);
			}

			@Override
			public void failure(RetrofitError retrofitError, String message) {
				callback.failure(retrofitError);
			}
		});
	}

	// update user info
	public void useremailupdate(final String email,
								final RestCallback<UserService.ResUserUpdate> callback) {
		UserService userService = APIManager.provideUserService(mAdapter);
		userService.useremailupdate(email, _access_token, Integer.toString(UserManager.sharedInstance()._currentUser._id), new RestCallback<UserService.ResUserUpdate>() {
			@Override
			public void success(UserService.ResUserUpdate resUserUpdate, Response response) {
				callback.success(resUserUpdate, response);
			}

			@Override
			public void failure(RetrofitError retrofitError, String message) {
				callback.failure(retrofitError);
			}
		});
	}

	public void updateUserPassword(String userId, String password, final RestCallback<UserService.ResUserUpdate> callback) {
		UserService userService = APIManager.provideUserService(mAdapter);
		userService.updateUserPassword(
				password,
				_access_token,
				userId,
				new RestCallback<UserService.ResUserUpdate>() {
					@Override
					public void success(UserService.ResUserUpdate resUserUpdate, Response response) {
						callback.success(resUserUpdate, response);
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						callback.failure(retrofitError);
					}
				}
		);
	}

	public void updateUserInfo(String title, String firstName, String lastName, String nickname,
							   String address1, String address2, String city, String state,
							   String postCode, String email, String phone, int contactPreference,
							   String userId, final RestCallback<UserService.ResUserUpdate> callback
	) {
		UserService userService = APIManager.provideUserService(mAdapter);
		userService.updateUserInfo(
				_access_token, title, firstName, lastName, nickname, address1, address2, city, state,
				postCode, email, phone, contactPreference, userId,
				new RestCallback<UserService.ResUserUpdate>() {

					@Override
					public void success(UserService.ResUserUpdate resUserUpdate, Response response) {
						callback.success(resUserUpdate, response);
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						callback.failure(retrofitError);
					}
				}
		);

	}

	public void userDevices(final String device,
							final String imei,
							final String versionCode,
							final String imsi,
							final String iccid,
							final String udid,
							final String os,
							final RestCallback<UserService.ResUserDevices> callback) {
		UserService userService = APIManager.provideUserService(mAdapter);
		userService.userDevices(
				_access_token,
				device,
				imei,
				versionCode,
				imsi,
				iccid,
				udid,
				os,
				new RestCallback<UserService.ResUserDevices>() {
					@Override
					public void success(UserService.ResUserDevices resUserDevices, Response response) {
						callback.success(resUserDevices, response);
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						callback.failure(retrofitError);
					}
				}
		);
	}

	public void findApplication(final String email, final RestCallback<String> callback) {
		UserService userService = APIManager.provideUserService(mAdapter);
		userService.userFindApplication(email, true, new RestCallback<String>() {
			@Override
			public void failure(RetrofitError retrofitError, String message) {
				callback.failure(retrofitError);
			}

			@Override
			public void success(String resFindApplication, Response response) {
				callback.success(resFindApplication, response);
			}
		});
	}

	public void verifyIdentity(final String email, final String verificationCode, final RestCallback<UserService.ResLoginWithCode> callback) {
		UserService userService = APIManager.provideUserService(mAdapter);
		userService.verifyIdentity(email, verificationCode, new RestCallback<UserService.ResLoginWithCode>() {
			@Override
			public void failure(RetrofitError retrofitError, String message) {
				callback.failure(retrofitError);
			}

			@Override
			public void success(UserService.ResLoginWithCode resLoginWithCode, Response response) {
				callback.success(resLoginWithCode, response);
			}
		});
	}

	public void sendResetLink(final String email, final RestCallback<UserService.ResSendResetLinkForSuccess> callback) {
		UserService userService = APIManager.provideUserService(mAdapter);
		userService.sendResetLink(_access_token, email,
				new RestCallback<UserService.ResSendResetLinkForSuccess>() {

					@Override
					public void failure(RetrofitError retrofitError, String message) {
						Logger.error(TAG, "password reset failed - email : %s, error : %s", email, retrofitError);
						callback.failure(retrofitError);
					}

					@Override
					public void success(UserService.ResSendResetLinkForSuccess resSendResetLinkForSuccess, Response response) {
						Logger.log(TAG, "password reset success, access_token : " + _access_token);
						callback.success(resSendResetLinkForSuccess, response);
					}
				});
	}

	public void sendDriftLogs(final String deviceId,
							  final String drift,
							  final RestCallback<UserService.ResUserDevices> callback) {
		UserService userService = APIManager.provideUserService(mAdapter);
		userService.driftLogs(_access_token, deviceId, drift, new RestCallback<UserService.ResUserDevices>() {
			@Override
			public void success(UserService.ResUserDevices resUserDevices, Response response) {
				callback.success(resUserDevices, response);
			}

			@Override
			public void failure(RetrofitError retrofitError, String message) {
				callback.failure(retrofitError);
			}
		});
	}


	// byDeviceIds
	public void byDeviceIds(final ArrayList<String> arrayDeviceIds,
							final RestCallback<List<StudentService.ResByDeviceIds>> callback) {
		StudentService studentService = APIManager.provideStudentService(mAdapter);
		studentService.byDeviceIds(arrayDeviceIds, callback);
	}

	// register student
	public void createStudent(final String name,
							  final String gender,
							  final String deviceId,
							  final int teamId,
							  final boolean isCoach,
							  final String imageSrc,
							  RestCallback<StudentService.ResCreateStudent> callback) {
		StudentService studentService = APIManager.provideStudentService(mAdapter);
		studentService.createStudent(_access_token, name, gender, deviceId, teamId, isCoach, imageSrc, callback);
	}

	// update student name
	public void updateStudentName(final int id,
								  final String name,
								  RestCallback<StudentService.ResUpdateStudent> callback) {
		StudentService studentService = APIManager.provideStudentService(mAdapter);
		studentService.updateStudentName(_access_token, id + "", name, callback);
	}

	// update student
	public void updateStudent(
			Student student,
			RestCallback<StudentService.ResCreateStudent> callback
	) {
		StudentService studentService = APIManager.provideStudentService(mAdapter);
		studentService.updateStudent(
				_access_token,
				student._id + "",
				student._name,
				student._gender,
				student.getDeviceId(),
				student._teamId,
				student._isCoach,
				student._imageSrc,
				callback
		);
	}

	// update student
	public void updateStudentWithParam(
			final int id,
			Map<String, String> fields,
			RestCallback<StudentService.ResCreateStudent> callback
	) {
		StudentService studentService = APIManager.provideStudentService(mAdapter);
		studentService.updateStudentWithParam(
				_access_token,
				id + "",
				fields,
				callback
		);
	}

	public void getStudentData(final int id,
							   RestCallback<StudentService.ResGetStudentData> callback) {
		StudentService studentService = APIManager.provideStudentService(mAdapter);
		studentService.getStudentData(_access_token, id, callback);
	}


	// logout
	public void logout(final RestCallback<UserService.ResLogout> callback) {
		UserService userService = APIManager.provideUserService(mAdapter);
		userService.logout(_access_token, callback);
	}

	// teams apis ---
	public void getAllTeamsByUserId(final int userId,
									final RestCallback<TeamService.ResGetAllTeamByUserId> callback) {
		TeamService teamService = APIManager.provideTeamService(mAdapter);
		teamService.getAllTeamsByUserId(_access_token, userId, callback);
	}

	public void getAllStudentsByTeamId(final int teamId,
									   final RestCallback<StudentService.ResGetStudentsByTeamId> callback) {
		StudentService studentService = APIManager.provideStudentService(mAdapter);
		studentService.getStudentsByTeamId(_access_token, teamId, callback);
	}

	public void createTeam(final String name, final int userId, final String grade,
						   final RestCallback<TeamService.ResCreateTeam> callback) {
		TeamService teamService = APIManager.provideTeamService(mAdapter);
		teamService.createTeam(_access_token, name, userId, grade, callback);
	}

	public void updateTeam(final int id, final String name, final int userId, final String grade,
						   final RestCallback<TeamService.ResCreateTeam> callback) {
		TeamService teamService = APIManager.provideTeamService(mAdapter);
		teamService.updateTeam(_access_token, id + "", name, userId, grade, callback);
	}

	public void getTeamSummaryByTeamId(final int teamId, final RestCallback<TeamService.ResTeamSummaryByTeamId> callback) {
		TeamService teamService = APIManager.provideTeamService(mAdapter);
		teamService.getTeamSummaryByTeamId(_access_token, "2.3", teamId, callback);
	}

	public void getLeaderboardsByTeamId(int teamId, int page, String searchText, RestCallback<TeamService.ResLeaderboardList> callback) {
		TeamService teamService = APIManager.provideTeamService(mAdapter);
		teamService.getLeaderboardByTeamId(_access_token, teamId, page, searchText, callback);
	}

	public void getLeaderboardNearbyByTeamId(final int teamId, RestCallback<TeamService.ResLeaderboardList> callback) {
		TeamService teamService = APIManager.provideTeamService(mAdapter);
		teamService.getLeaderboardNearbyByTeamId(_access_token, teamId, callback);
	}

	public void getLeaderboardFovaritesTeams(RestCallback<TeamService.ResLeaderboardList> callback) {
		TeamService teamService = APIManager.provideTeamService(mAdapter);
		teamService.getLeaderboardFovaritesTeams(_access_token, callback);
	}

	public void updateStatsForTeam(final int teamId, final RestCallback<CommandService.ResUpdateStats> callback) {
		CommandService commandService = APIManager.provideCommandService(mAdapter);
		commandService.updateStats(_access_token, teamId, callback);
	}

	public void validatePassword(final String password, final RestCallback<UserService.ResValidatePassword> callback) {
		UserService userService = APIManager.provideUserService(mAdapter);
		userService.validatePassword(_access_token, password, new RestCallback<UserService.ResValidatePassword>() {
			@Override
			public void success(UserService.ResValidatePassword resValidatePassword, Response response) {
				callback.success(resValidatePassword, response);
			}

			@Override
			public void failure(RetrofitError retrofitError, String message) {
				callback.failure(retrofitError);
			}
		});
	}

	public void extraPointTeam(final int teamId, final int points, final RestCallback<List<CommandService.ResExtraPointTeam>> callback) {
		CommandService commandService = APIManager.provideCommandService(mAdapter);
		commandService.extraPointTeam(_access_token, teamId, points, callback);
	}

	public void extraPointStudent(final int studentId, final int points, final RestCallback<CommandService.ResExtraPointStudent> callback) {
		CommandService commandService = APIManager.provideCommandService(mAdapter);
		commandService.extraPointStudent(_access_token, studentId, points, callback);
	}

	public void replacePowerBand(final int studentId, final String deviceId, final RestCallback<StudentService.ResUpdateStudent> callback) {
		StudentService studentService = APIManager.provideStudentService(mAdapter);
		studentService.replacePowerBand(_access_token, studentId + "", deviceId, callback);
	}

	public void deleteStudent(final int studentId, final RestCallback<String> callback) {
		APIManager.provideStudentService(mAdapter).deleteStudent(_access_token, studentId + "", callback);
	}

	public void uploadDailyData(String deviceId,
								ArrayList<DailyActivityData> datas,
								final RestCallback<CommandService.ResUploadDailyData> callback) {
		Logger.log(TAG, "upload DailyData");
		CommandService service = APIManager.provideCommandService(mAdapter);

		for (DailyActivityData data : datas) {

			DetailData summary = new DetailData();
			summary.steps = 0;
			summary.calories = 0;
			summary.distance = 0;
			//summary.run_steps = 0;

			for (DetailData ddd : data.data) {
				summary.steps += ddd.steps;
				summary.calories += ddd.calories;
				summary.distance += ddd.distance;
				//summary.run_steps += ddd.run_steps;
			}

			Logger.log(TAG, "uploading daily detail data for date = %s : steps=%d, cal=%f, dist=%f, mvpa=%d, vpa=%d, duration=%d",
					data.date, summary.steps, summary.calories, summary.distance,
					data.summary.duration, data.summary.mvpa, data.summary.vpa);
		}

		UploadActivityData uploadData = new UploadActivityData();
		uploadData.deviceId = deviceId;
		uploadData.dates = datas;

		service.uploadActivityDetail(uploadData, callback);
	}

	public void loadAllBand(int userId, final RestCallback<List<FilteringDevice>> callback) {
		CommandService service = APIManager.provideCommandService(mAdapter);
		service.getAllBands(_access_token, userId, new RestCallback<List<FilteringDevice>>() {
			@Override
			public void failure(RetrofitError retrofitError, String message) {
				callback.failure(retrofitError, message);
			}

			@Override
			public void success(List<FilteringDevice> resDeviceIds, Response response) {

				_registeredBands = resDeviceIds;

				callback.success(resDeviceIds, response);
			}
		});
	}

	public List<FilteringDevice> getAllBands() {
		return _registeredBands;
	}

	public void didLinkedBand(String bandId) {
		if (TextUtils.isEmpty(bandId))
			return;

		String lBandId = bandId;

		synchronized (_registeredBands) {
			for (FilteringDevice device : _registeredBands) {
				if (device.deviceId.equals(lBandId)) {
					// already linked
					return;
				}
			}

			FilteringDevice newDevice = new FilteringDevice();
			newDevice.deviceId = lBandId;
			_registeredBands.add(newDevice);
		}
	}

	public void didDeLinkedBand(String bandId) {
		if (TextUtils.isEmpty(bandId))
			return;

		String lBandId = bandId;
		if (_registeredBands == null)
			return;

		synchronized (_registeredBands) {
			for (FilteringDevice device : _registeredBands) {
				if (device.deviceId.equalsIgnoreCase(lBandId)) {
					// found this device, will remove
					_registeredBands.remove(device);
					break;
				}
			}
		}
	}
}
