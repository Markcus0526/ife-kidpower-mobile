package org.unicefkidpower.schools.model;

import org.unicefkidpower.schools.KidpowerApplication;
import org.unicefkidpower.schools.context.UserContext;
import org.unicefkidpower.schools.server.apimanage.TeamService;
import org.unicefkidpower.schools.server.apimanage.UserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by donal_000 on 1/13/2015.
 */
public class TeamManager {
	public static final String				DEFAULT_TEAMNAME = "TBD";
	public static final int					DEFAULT_TEAMGRADE = 3;
	public ArrayList<TeamGrade>				_teamGrades;


	public TeamManager() {
		_teamGrades = getTeamGrades();
	}

	public static TeamManager sharedInstance() {
		return KidpowerApplication.sharedTeamManagerInstance();
	}

	public ArrayList<Team> parseTeamForResLogin(UserService.ResLoginForSuccess resLoginForSuccess) {
		UserService.ResLoginTeamArray teamArray = resLoginForSuccess.teams;
		ArrayList<Team> teams = new ArrayList<Team>();
		if (teamArray == null ||
				teamArray.teams == null)
			return teams;

		for (UserService.ResLoginTeam resLoginTeam : teamArray.teams) {
			Team team = new Team();
			team._id = resLoginTeam._id;
			team._name = resLoginTeam.name;
			team._grade = resLoginTeam.grade;
			team._description = resLoginTeam.description;
			team._imageSrc = resLoginTeam.imageSrc;
			//team._studentCount = resLoginTeam.studentCount;
			team._startDate = Utils.parseStartDate(resLoginTeam.startDate);
			team._endDate = Utils.parseEndDate(resLoginTeam.endDate);
			team._height = Utils.parseHeight(resLoginTeam.height);
			team._weight = Utils.parseWeight(resLoginTeam.weight);
			team._stride = Utils.parseStride(resLoginTeam.stride);
			team._message = resLoginTeam.message;
			teams.add(team);
		}

		Collections.sort(teams, new Comparator<Team>() {
			@Override
			public int compare(Team lhs, Team rhs) {
				return lhs._name.compareToIgnoreCase(rhs._name);
			}
		});

		return teams;
	}


	public Team parseTeamForResGetAllTeamsByUserId(TeamService.ResGetAllTeamByUserIdTeam resTeam) {
		/*
		{
            "_id": 1,
                "name": "Team One",
                "grade": "4th Grade",
                "description": "Testing new API",
                "imageSrc": "images/logo4.png",
                "studentCount": 10,
                "startDate": "2014-12-01T00:00:00.000Z",
                "endDate": "2015-02-28T00:00:00.000Z",
                "height": "150",
                "weight": "125",
                "stride": "75",
                "message": "Good job!!!" }
        */

		Team team = new Team();
		team._id = resTeam._id;
		team._name = resTeam.name;
		team._grade = resTeam.grade;
		team._description = resTeam.description;
		team._imageSrc = resTeam.imageSrc;
		team._startDate = Utils.parseStartDate(resTeam.startDate);
		team._endDate = Utils.parseEndDate(resTeam.endDate);
		team._height = Utils.parseHeight(resTeam.height);
		team._weight = Utils.parseWeight(resTeam.weight);
		team._stride = Utils.parseStride(resTeam.stride);
		team._message = resTeam.message;
		return team;
	}


	public ArrayList<Team> parseTeamArrayForResGetAllTeamsByUserId(TeamService.ResGetAllTeamByUserId resTeams) {
		ArrayList<Team> teams = new ArrayList<Team>();
		if (resTeams == null || resTeams.teams == null)
			return teams;

		for (TeamService.ResGetAllTeamByUserIdTeam resTeam : resTeams.teams) {
			Team team = parseTeamForResGetAllTeamsByUserId(resTeam);
			teams.add(team);
		}

		Collections.sort(teams, new Comparator<Team>() {
			@Override
			public int compare(Team lhs, Team rhs) {
				return lhs._name.compareToIgnoreCase(rhs._name);
			}
		});

		return teams;
	}


	// utils
	protected ArrayList<TeamGrade> getTeamGrades() {
		ArrayList<TeamGrade> grades = new ArrayList<TeamGrade>();

		if (UserContext.sharedInstance().getAppLocale().getLanguage().equalsIgnoreCase("nl")) {
			// NL instance
			grades.add(new TeamGrade("5", "Groep 5"));
			grades.add(new TeamGrade("6", "Groep 6"));
			grades.add(new TeamGrade("7", "Groep 7"));
			grades.add(new TeamGrade("8", "Groep 8"));
			grades.add(new TeamGrade("o", "Overig"));
		} else if (UserContext.sharedInstance().getAppLocale().getCountry().equalsIgnoreCase("gb") ||
				UserContext.sharedInstance().getAppLocale().getCountry().equalsIgnoreCase("uk")) {
			// UK instance
			grades.add(new TeamGrade("7", "Year 7"));
			grades.add(new TeamGrade("8", "Year 8"));
			grades.add(new TeamGrade("9", "Year 9"));
			grades.add(new TeamGrade("o", "Mixed Years"));
		} else {
			// US instance
			grades.add(new TeamGrade("3", "3rd Grade"));
			grades.add(new TeamGrade("4", "4th Grade"));
			grades.add(new TeamGrade("5", "5th Grade"));
			grades.add(new TeamGrade("6", "6th Grade"));
			grades.add(new TeamGrade("7", "7th Grade"));
			grades.add(new TeamGrade("8", "8th Grade"));
			grades.add(new TeamGrade("o", "Other"));
		}

		return grades;
	}


	public TeamGrade teamGradeWithValue(String value) {
		for (TeamGrade teamGrade : _teamGrades) {
			if (teamGrade.value.equals(value))
				return teamGrade;
		}
		return null;
	}


	public Team parseTeamFromResCreateTeam(TeamService.ResCreateTeam resPostTeam) {
		Team team = new Team();
		team._id = resPostTeam._id;
		team._name = resPostTeam.name;
		team._grade = resPostTeam.grade;
		team._description = resPostTeam.description;
		team._imageSrc = resPostTeam.imageSrc;
		team._startDate = Utils.parseStartDate(resPostTeam.startDate);
		team._endDate = Utils.parseEndDate(resPostTeam.endDate);
		team._height = Utils.parseHeight(resPostTeam.height);
		team._weight = Utils.parseWeight(resPostTeam.weight);
		team._stride = Utils.parseStride(resPostTeam.stride);
		team._message = resPostTeam.message;

		return team;
	}


	public boolean updateStudentListForTeam(int teamId, ArrayList<Student> students) {
		if (UserManager.sharedInstance()._currentUser == null ||
				UserManager.sharedInstance()._currentUser._teams == null)
			return false;

		Team matchingTeam = null;
		for (Team existTeam : UserManager.sharedInstance()._currentUser._teams) {
			if (existTeam._id == teamId) {
				matchingTeam = existTeam;
				break;
			}
		}

		if (matchingTeam == null)
			return false;

		matchingTeam._students = students;
		return true;
	}


	public static class TeamGrade {
		public String value;
		public String desc;

		public TeamGrade() {
			this.value = "";
			this.desc = "";
		}

		public TeamGrade(String value, String desc) {
			this.value = value;
			this.desc = desc;
		}
	}
}
