package org.unicefkidpower.schools.model;

import org.unicefkidpower.schools.KidpowerApplication;
import org.unicefkidpower.schools.server.apimanage.StudentService;

import java.util.ArrayList;

/**
 * Created by donal_000 on 1/13/2015.
 */
public class StudentManager {
	public static StudentManager sharedInstance() {
		return KidpowerApplication.sharedStudentManagerInstance();
	}

	public ArrayList<Student> parseStudentsForResGetAllStudentsByTeamId(StudentService.ResGetStudentsByTeamId resGetStudentsByTeamId) {
		/*
        0:  {
            _id: 945
            name: "RUI"
            gender: "undefined"
            deviceId: "1DB6C"
            deviceId2: ""
            imageSrc: ""
            isCoach: true
            height: "150"
            weight: "125"
            stride: "75"
            message: "Good job!!!"
            lastSyncDateDetail: ""
            lastSyncDateSummary: ""
            teamId: 62
            team: {
                name: "rui's team"
                grade: "O"
                description: ""
                startDate: "2014-12-01T00:00:00.000Z"
                endDate: "2015-02-28T00:00:00.000Z"
                imageSrc: "images/logo4.png"
                studentCount: 0
            }-
        }*/

		ArrayList<Student> students = new ArrayList<Student>();

		for (StudentService.ResGetStudentsByTeamIdStudent resStudent : resGetStudentsByTeamId.students) {
			Student student = new Student();
			student._id = resStudent._id;
			student._name = resStudent.name;
			student._deviceId = resStudent.deviceId;
			student._deviceId2 = resStudent.deviceId2;
			student._imageSrc = resStudent.imageSrc;
			student._isCoach = resStudent.isCoach;
			student._height = Utils.parseHeight(resStudent.height);
			student._weight = Utils.parseWeight(resStudent.weight);
			student._stride = Utils.parseStride(resStudent.stride);
			student._message = resStudent.message;
			student._lastSyncDateDetail = Utils.parseLastSyncDateDetail(resStudent.lastSyncDateDetail);
			student._teamId = resStudent.teamId;
			student._powerPoints = resStudent.powerPoints;

			students.add(student);
		}
		return students;
	}

	public Student updateStudentFromResGetData(StudentService.ResGetStudentData resGetStudentData) {
		Student existStudent = null;
		Team existTeam = null;
		if (UserManager.sharedInstance()._currentUser == null ||
				UserManager.sharedInstance()._currentUser._teams == null) {
			//
		} else {
			for (Team team : UserManager.sharedInstance()._currentUser._teams) {
				if (team._id == resGetStudentData.teamId) {
					existTeam = team;
					break;
				}
			}
			if (existTeam != null &&
					existTeam._students != null) {
				for (Student student : existTeam._students) {
					if (student._id == resGetStudentData._id) {
						existStudent = student;
						break;
					}
				}
			}
		}

		if (existStudent == null) {
			existStudent = new Student();
			if (existTeam != null) {
				if (existTeam._students == null) {
					existTeam._students = new ArrayList<Student>();
				}
				existTeam._students.add(existStudent);
			}
		}
		existStudent._id = resGetStudentData._id;
		existStudent._name = resGetStudentData.name;
		existStudent._gender = resGetStudentData.gender;
		existStudent._deviceId = resGetStudentData.deviceId;
		existStudent._deviceId2 = resGetStudentData.deviceId2;
		existStudent._imageSrc = resGetStudentData.imageSrc;
		existStudent._height = Utils.parseHeight(resGetStudentData.height);
		existStudent._weight = Utils.parseWeight(resGetStudentData.weight);
		existStudent._stride = Utils.parseStride(resGetStudentData.stride);
		existStudent._message = resGetStudentData.message;
		existStudent._teamId = resGetStudentData.teamId;
		existStudent._steps = resGetStudentData.steps;
		existStudent._miles = resGetStudentData.miles;
		existStudent._packets = resGetStudentData.packets;
		existStudent._powerPoints = resGetStudentData.powerPoints;
		existStudent._displayMessage = resGetStudentData.displayMessage;
		existStudent._lastSyncDateDetail = Utils.parseLastSyncDateDetail(resGetStudentData.lastSyncDateDetail);
		return existStudent;
	}
}
