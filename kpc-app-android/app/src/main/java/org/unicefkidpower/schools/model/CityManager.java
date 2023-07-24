package org.unicefkidpower.schools.model;

import org.unicefkidpower.schools.KidpowerApplication;
import org.unicefkidpower.schools.server.apimanage.ProgramService;

import java.util.ArrayList;

/**
 * Created by donal_000 on 2/11/2015.
 */
public class CityManager {
	public ArrayList<City> _cities = new ArrayList<City>();

	public static CityManager sharedInstance() {
		return KidpowerApplication.sharedCityManagerInstance();
	}

	public ArrayList<City> parseCityArrayForResRegCode(ProgramService.ResRegCode resRegCode) {
		/*
        cities: [2]
        0:  {
            _id: 1
            name: "New York"
            groups: [2]
            0:  {
                _id: 1
                name: "JP Elementary"
                regCode: "RUTF"
                startDate: "2014-12-01T00:00:00.000Z"
                endDate: "2015-02-28T00:00:00.000Z"
                height: "150"
                weight: "125"
                stride: "75"
                message: "Good job!!!"
            }-
                    1:  {
                _id: 2
                name: "Samuel Adams Charter"
                regCode: "xyz-123"
                startDate: "2014-12-01T00:00:00.000Z"
                endDate: "2015-02-28T00:00:00.000Z"
                height: "150"
                weight: "125"
                stride: "75"
                message: "Good job!!!"
            }-
                    -
        }-
                1:  {
            _id: 2
            name: "Boston"
            groups: [0]
        }*/

		ArrayList<City> cities = new ArrayList<City>();
		if (resRegCode.cities == null)
			return cities;

		for (ProgramService.ResRegCodeCity resCity : resRegCode.cities) {
			City city = new City();
			city._id = resCity._id;
			city._name = resCity.name;
			city._groups = new ArrayList<Group>();
			if (resCity.groups != null) {
				city._groups = GroupManager.sharedInstance().parseGroupArrayForResRegCode(resCity);
			}

			cities.add(city);
		}

		return cities;
	}
}
