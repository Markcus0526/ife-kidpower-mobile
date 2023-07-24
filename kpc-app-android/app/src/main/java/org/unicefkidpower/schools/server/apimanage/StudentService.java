package org.unicefkidpower.schools.server.apimanage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

/**
 * Created by donal_000 on 1/13/2015.
 */
public interface StudentService {
	@FormUrlEncoded
	@POST(APIManager.STUDENT_BYDEVICEIDS)
	void byDeviceIds(@Field("devices") ArrayList<String> arrayDeviceIds,
					 RestCallback<List<ResByDeviceIds>> callback);

	@GET(APIManager.STUDENT_GETSTUDENTSBYTEAMID)
	void getStudentsByTeamId(@Header("x-access-token") String access_token,
							 @Path("teamId") int teamId,
							 RestCallback<ResGetStudentsByTeamId> callback);

	@FormUrlEncoded
	@POST(APIManager.STUDENT_STUDENTS)
	void createStudent(
			@Header("x-access-token") String access_token,
			@Field("name") String name,
			@Field("gender") String gender,
			@Field("deviceId") String deviceId,
			@Field("teamId") int teamId,
			@Field("isCoach") boolean isCoach,
			@Field("imageSrc") String imageSrc,
			RestCallback<ResCreateStudent> callback);

	@FormUrlEncoded
	@PUT(APIManager.STUDENT_UPDATE)
	void updateStudent(
			@Header("x-access-token") String access_token,
			@Path("id") String id,
			@Field("name") String name,
			@Field("gender") String gender,
			@Field("deviceId") String deviceId,
			@Field("teamId") int teamId,
			@Field("isCoach") boolean isCoach,
			@Field("imageSrc") String imageSrc,
			RestCallback<ResCreateStudent> callback);

	@FormUrlEncoded
	@PUT(APIManager.STUDENT_UPDATE)
	void updateStudentWithParam(
			@Header("x-access-token") String access_token,
			@Path("id") String id,
			@FieldMap Map<String, String> fields,
			RestCallback<ResCreateStudent> callback);

	@FormUrlEncoded
	@PUT(APIManager.STUDENT_UPDATE)
	void updateStudentName(
			@Header("x-access-token") String access_token,
			@Path("id") String id,
			@Field("name") String name,
			RestCallback<ResUpdateStudent> callback);

	@GET(APIManager.STUDENT_UPDATE)
	void getStudentData(
			@Header("x-access-token") String access_token,
			@Path("id") int id,
			RestCallback<ResGetStudentData> callback);

	@FormUrlEncoded
	@PUT(APIManager.STUDENT_UPDATE)
	void replacePowerBand(
			@Header("x-access-token") String access_token,
			@Path("id") String id,
			@Field("deviceId") String deviceId,
			RestCallback<ResUpdateStudent> callback);

	@DELETE(APIManager.STUDENT_UPDATE)
	void deleteStudent(
			@Header("x-access-token") String access_token,
			@Path("id") String id,
			RestCallback<String> callback
	);

	class ResByDeviceIds {
		public String deviceId;
		public String firstName;
		public String lastName;
		public String lastSyncDate;
		public String lastSyncDateSummary;
	}

	class ResCreateStudent {
		/*
		{
			imageSrc: "images/defaultPerson.jpg"
			deviceId2: ""
			isActive: true
			isCoach: false
			lastSyncDateDetail: ""
			lastSyncDateSummary: ""
			_id: 3
			name: "tn1"
			gender: "boy"
			deviceId: "12222"
			teamId: 1
			height: "150"
			weight: "125"
			stride: "75"
			message: "Good job!!!"
			updatedAt: "2015-01-13T03:00:15.015Z"
			createdAt: "2015-01-13T03:00:15.015Z"
			fullName: "tn1"
			team: {
				name: "Team One"
				grade: "4th Grade"
				startDate: "2014-12-01T00:00:00.000Z"
				endDate: "2015-02-28T00:00:00.000Z"
				height: "150"
				weight: "125"
				stride: "75"
				message: "Good job!!!"
			}
		}*/
		public String imageSrc;
		public String deviceId2;
		public boolean isActive;
		public boolean isCoach;
		public String lastSyncDateDetail;
		public String lastSyncDateSummary;
		public int _id;
		public String name;
		public String gender;
		public String deviceId;
		public int teamId;
		public String height;
		public String weight;
		public String stride;
		public String message;
		public String updatedAt;
		public String createdAt;
		public String fullName;
		public ResCreateStudentTeam team;
	}

	class ResUpdateStudent {
		public int _id;
		public String name;
		public String gender;
		public String deviceId;
		public String deviceId2;
		public String imageSrc;
		public String height;
		public String weight;
		public String stride;
		public String message;
		public String lastSyncDateDetail;
		public String lastSyncDateSummary;
		public int teamId;
		public int steps;
		public float miles;
		public int packets;
		public int powerPoints;
		/**/
	}

	class ResCreateStudentTeam {
		public String name;
		public String grade;
		public String startDate;
		public String endDate;
		public String height;
		public String weight;
		public String stride;
		public String message;
	}

	class ResCreateStudentTeamDeserializer implements JsonDeserializer<ResCreateStudentTeam> {
		@Override
		public ResCreateStudentTeam deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
				throws JsonParseException {
			// Get the "group" element from the parsed JSON
			// JsonElement group = je.getAsJsonObject().get("group");

			// Deserialize it. You use a new instance of Gson to avoid infinite recursion
			// to this deserializer
			//return new Gson().fromJson(group, ResLoginGroup.class);
			return new Gson().fromJson(je, ResCreateStudentTeam.class);
		}
	}

	class ResGetStudentData {
		public int _id;
		public String name;
		public String gender;
		public String deviceId;
		public String deviceId2;
		public String imageSrc;
		public String height;
		public String weight;
		public String stride;
		public String message;
		public String lastSyncDateDetail;
		public String lastSyncDateSummary;
		public int teamId;
		public int steps;
		public float miles;
		public int packets;
		public int powerPoints;
		public ResGetStudentWithDisplayMessage displayMessage;
	}

	class ResGetStudentWithDisplayMessage {
		public String message;
		public String url;
	}

	class ResGetStudentWithDisplayMessageDeserializer implements JsonDeserializer<ResGetStudentWithDisplayMessage> {
		@Override
		public ResGetStudentWithDisplayMessage deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
				throws JsonParseException {
			// Get the "group" element from the parsed JSON
			// JsonElement group = je.getAsJsonObject().get("group");

			// Deserialize it. You use a new instance of Gson to avoid infinite recursion
			// to this deserializer
			//return new Gson().fromJson(group, ResLoginGroup.class);
			return new Gson().fromJson(je, ResGetStudentWithDisplayMessage.class);
		}
	}

	class ResGetStudentInfoWithTeam {
		public String name;
		public String grade;
		public String description;
		public String startDate;
		public String endDate;
		public String imageSrc;
		public int studentCount;
	}

	class ResGetStudentInfoWithTeamDeserializer implements JsonDeserializer<ResGetStudentInfoWithTeam> {
		@Override
		public ResGetStudentInfoWithTeam deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
				throws JsonParseException {
			// Get the "group" element from the parsed JSON
			// JsonElement group = je.getAsJsonObject().get("group");

			// Deserialize it. You use a new instance of Gson to avoid infinite recursion
			// to this deserializer
			//return new Gson().fromJson(group, ResLoginGroup.class);
			return new Gson().fromJson(je, ResGetStudentInfoWithTeam.class);
		}
	}

	class ResGetStudentsByTeamId {
		public ArrayList<ResGetStudentsByTeamIdStudent> students;
	}

	class ResGetStudentsByTeamIdStudent {
		public int _id;
		public String name;
		public String gender;
		public String deviceId;
		public String deviceId2;
		public String imageSrc;
		public boolean isCoach;
		public String height;
		public String weight;
		public String stride;
		public String message;
		public String lastSyncDateDetail;
		public String lastSyncDateSummary;
		public int teamId;
		public int powerPoints;
	}

	class ResGetAllStudentsByTeamIdDeserializer implements JsonDeserializer<ResGetStudentsByTeamId> {
		@Override
		public ResGetStudentsByTeamId deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
				throws JsonParseException {
			// Get the "group" element from the parsed JSON
			JsonArray teams = je.getAsJsonArray();
			ResGetStudentsByTeamId array = new ResGetStudentsByTeamId();
			array.students = new ArrayList<>();

			// Deserialize it. You use a new instance of Gson to avoid infinite recursion
			// to this deserializer
			for (int i = 0; i < teams.size(); i++) {
				ResGetStudentsByTeamIdStudent student = new Gson().fromJson(teams.get(i), ResGetStudentsByTeamIdStudent.class);
				array.students.add(student);
			}
			return array;
		}
	}
}
