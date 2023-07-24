package org.unicefkidpower.schools.server.apimanage;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by donal_000 on 2/4/2015.
 */
public interface ProgramService {
	@FormUrlEncoded
	@POST(APIManager.PROGRAM_REGCODE)
	public void regCode(@Field("regCode") String regCode,
						@Field("version") int version,
						RestCallback<ResRegCode> callback);

	public static class ResRegCode {
		public boolean valid;
		public String message;
		public int programId;
		public ResRegCodeCity[] cities;
	}

	public static class ResRegCodeCity {
		public int _id;
		public String name;
		public ResRegCodeGroup[] groups;
	}

    /*
	public static class  ResRegCodeCityArrayDeserializer  implements JsonDeserializer<ResRegCodeCityArray> {
        @Override
        public ResRegCodeCityArray deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
                throws JsonParseException {
            // Get the "group" element from the parsed JSON
            //JsonArray teams = je.getAsJsonObject().getAsJsonArray("teams");
            JsonArray cities = je.getAsJsonArray();
            ResRegCodeCityArray array = new ResRegCodeCityArray();
            array.cities = new ArrayList<ResRegCodeCity>();

            // Deserialize it. You use a new instance of Gson to avoid infinite recursion
            // to this deserializer
            for (int i = 0; i < cities.size(); i++) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(ResRegCodeCityArray.class, new ResRegCodeGroupArrayDeserializer())
                        .create();
                ResRegCodeCity team = gson.fromJson(cities.get(i), ResRegCodeCity.class);
                array.cities.add(team);
            }
            return array;
        }
    }

    public static class  ResRegCodeGroupArrayDeserializer  implements JsonDeserializer<ResRegCodeGroupArray> {
        @Override
        public ResRegCodeGroupArray deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
                throws JsonParseException {
            // Get the "group" element from the parsed JSON
            //JsonArray teams = je.getAsJsonObject().getAsJsonArray("teams");
            JsonArray groups = je.getAsJsonArray();
            ResRegCodeGroupArray array = new ResRegCodeGroupArray();
            array.groups = new ArrayList<ResRegCodeGroup>();

            // Deserialize it. You use a new instance of Gson to avoid infinite recursion
            // to this deserializer
            for (int i = 0; i < groups.size(); i++) {
                ResRegCodeGroup group = new Gson().fromJson(groups.get(i), ResRegCodeGroup.class);
                array.groups.add(group);
            }
            return array;
        }
    }
    */

	public static class ResRegCodeGroup {
		public int _id;
		public String name;
		public String regCode;
		public String startDate;
		public String endDate;
		public String height;
		public String weight;
		public String stride;
		public String message;
	}
}
