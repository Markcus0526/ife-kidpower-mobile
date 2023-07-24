package org.unicefkidpower.schools.model;

import org.unicefkidpower.schools.KidpowerApplication;
import org.unicefkidpower.schools.server.apimanage.ProgramService;
import org.unicefkidpower.schools.server.apimanage.UserService;

import java.util.ArrayList;

/**
 * Created by donal_000 on 1/12/2015.
 */
public class GroupManager {
	public Group					_currentGroup;

	public static GroupManager sharedInstance() {
		return KidpowerApplication.sharedGroupManagerInstance();
	}

	public Group parseGroupForLogin(UserService.ResLoginForSuccess resLoginForSuccess) {
		 /*
        {
            _id: 5
            firstName: "Donald"
            lastName: "Pae"
            email: "donald@caloriecloud.org"
            userType: "teacher"
            groupId: 1
            activationToken: ""
            group: {
                name: "Group One"
                startDate: "2014-12-01T00:00:00.000Z"
                endDate: "2015-02-28T00:00:00.000Z"
                height: "150"
                weight: "125"
                stride: "75"
                message: "Good job!!!"
            }-
                teams: [3]
                    0:  {
                    _id: 1
                    name: "Team One"
                    grade: "4th Grade"
                    description: "Testing new API"
                    imageSrc: "images/logo4.png"
                    studentCount: 10
                    startDate: "2014-12-01T00:00:00.000Z"
                    endDate: "2015-02-28T00:00:00.000Z"
                    height: "150"
                    weight: "125"
                    stride: "75"
                    message: "Good job!!!"
                }-
                        1:  {
                    _id: 2
                    name: "Pirates of the Caribbean"
                    grade: "Grade 3"
                    description: ""
                    imageSrc: "images/logo4.png"
                    studentCount: 0
                    startDate: "2014-12-01T00:00:00.000Z"
                    endDate: "2015-02-28T00:00:00.000Z"
                    height: "150"
                    weight: "125"
                    stride: "75"
                    message: "Good job!!!"
                }-
                        2:  {
                    _id: 3
                    name: "Triangle Warriors"
                    grade: "Grade 3"
                    description: ""
                    imageSrc: "images/logo4.png"
                    studentCount: 0
                    startDate: "2014-12-01T00:00:00.000Z"
                    endDate: "2015-02-28T00:00:00.000Z"
                    height: "150"
                    weight: "125"
                    stride: "75"
                    message: "Good job!!!"
                }-
            id: 5
            access_token: "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjUsImRhdGUiOjE0MjIyNjIxNTE1NTYsImlhdCI6MTQyMjI2MjE1Mn0.-NqxwslTFB_ct6BBM0UC5cltLbZfDOAFXdd-EZprD0Q"
            userIP: "106.186.112.128"
        }
        */
		Group group = new Group();
		group._id = resLoginForSuccess.groupId;
		group._name = resLoginForSuccess.group.name;
		group._startDate = Utils.parseStartDate(resLoginForSuccess.group.startDate);
		group._endDate = Utils.parseEndDate(resLoginForSuccess.group.endDate);
		group._height = Utils.parseHeight(resLoginForSuccess.group.height);
		group._weight = Utils.parseWeight(resLoginForSuccess.group.weight);
		group._stride = Utils.parseStride(resLoginForSuccess.group.stride);
		group._message = resLoginForSuccess.group.message;
		return group;
	}

	public Group parseGroupForSignupRes(UserService.ResSignup resSignup) {
        /*
        group: {
            _id: 1
            name: "Group One"
            mdr_pid: ""
            regCode: "xyz-123"
            startDate: "2014-12-01T00:00:00.000Z"
            endDate: "2015-02-28T00:00:00.000Z"
            height: "150"
            weight: "125"
            stride: "75"
            message: "Good job!!!"
            createdAt: "2014-12-29T05:10:39.369Z"
            updatedAt: "2014-12-29T05:10:39.369Z"
            cityId: 1
        }*/
		Group group = new Group();
		UserService.ResSignupGroup resSignupGroup = resSignup.group;
		group._id = resSignupGroup._id;
		group._name = resSignupGroup.name;
		group._mdr_pid = resSignupGroup.mdr_pid;
		group._regCode = resSignupGroup.regCode;
		group._startDate = Utils.parseStartDate(resSignupGroup.startDate);
		group._endDate = Utils.parseEndDate(resSignupGroup.endDate);
		group._height = Float.parseFloat(resSignupGroup.height);
		group._weight = Float.parseFloat(resSignupGroup.weight);
		group._stride = Float.parseFloat(resSignupGroup.stride);
		group._message = resSignupGroup.message;
		group._createdAt = Utils.parseCreatedAt(resSignupGroup.createdAt);
		group._updatedAt = Utils.parseUpdatedAt(resSignupGroup.updateAt);
		group.cityId = resSignupGroup.cityId;

		return group;
	}


	public ArrayList<Group> parseGroupArrayForResRegCode(ProgramService.ResRegCodeCity resRegCodeCity) {
        /*groups: [2]
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
        }-*/
		ArrayList<Group> groups = new ArrayList<Group>();
		if (resRegCodeCity == null ||
				resRegCodeCity.groups == null)
			return groups;
		for (ProgramService.ResRegCodeGroup resGroup : resRegCodeCity.groups) {
			Group group = new Group();
			group._id = resGroup._id;
			group._name = resGroup.name;
			group._startDate = Utils.parseStartDate(resGroup.startDate);
			group._endDate = Utils.parseEndDate(resGroup.endDate);
			group._height = Utils.parseHeight(resGroup.height);
			group._weight = Utils.parseWeight(resGroup.weight);
			group._stride = Utils.parseStride(resGroup.stride);
			group._message = resGroup.message;

			groups.add(group);
		}

		return groups;
	}
}
