package org.unicefkidpower.kid_power.Model.Structure;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dayong Li on 2/13/2017.
 */

public class TrackerSyncResult {
	public int							newSteps;
	public double						newCalories;
	public double						newDuration;
	public int							newPowerPoints;
	public List<KPHDailyDetailData>		activities;


	public TrackerSyncResult() {
		activities = new ArrayList<>();
		newSteps = 0;
		newCalories = 0;
		newDuration = 0;
		newPowerPoints = 0;
	}

	public JSONObject encodeToJSON() {
		JSONObject result = new JSONObject();

		try { result.put("newSteps", newSteps); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("newCalories", newCalories); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("newDuration", newDuration); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.put("newPowerPoints", newPowerPoints); } catch (Exception ex) { ex.printStackTrace(); }

		JSONArray activitiesArray = new JSONArray();
		if (activities != null) {
			for (int i = 0; i < activities.size(); i++) {
				activitiesArray.put(activities.get(i).encodeToJSON());
			}
		}

		try { result.put("activities", activitiesArray); } catch (Exception ex) { ex.printStackTrace(); }

		return result;
	}

	public static TrackerSyncResult decodeFromJSON(JSONObject jsonObject) {
		TrackerSyncResult result = new TrackerSyncResult();

		try { result.newSteps = jsonObject.getInt("newSteps"); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.newCalories = jsonObject.getDouble("newCalories"); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.newDuration = jsonObject.getDouble("newDuration"); } catch (Exception ex) { ex.printStackTrace(); }
		try { result.newPowerPoints = jsonObject.getInt("newPowerPoints"); } catch (Exception ex) { ex.printStackTrace(); }

		try {
			JSONArray activitiesArray = jsonObject.getJSONArray("activities");;
			for (int i = 0; i < activitiesArray.length(); i++) {
				KPHDailyDetailData daily = KPHDailyDetailData.decodeFromJSON(activitiesArray.getJSONObject(i));
				result.activities.add(daily);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return result;
	}

}