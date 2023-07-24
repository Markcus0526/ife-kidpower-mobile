package org.unicefkidpower.schools.model;

import android.content.Context;

import org.unicefkidpower.schools.KidpowerApplication;
import org.unicefkidpower.schools.server.apimanage.UserService;

/**
 * Created by donal_000 on 1/12/2015.
 */
public class UserManager {
	public User _currentUser;
	private Context _context;

	public UserManager(Context context) {
		_context = context;
		_currentUser = null;
	}

	public static UserManager sharedInstance() {
		return KidpowerApplication.sharedUserManagerInstance();
	}

	public static UserManager initialize(Context context) {
		return KidpowerApplication.initUserManager(context);
	}

	public User parseUserForLogin(UserService.ResLoginForSuccess resLoginForSuccess) {
		User user = new User();
		user._id = resLoginForSuccess.id;
		user._firstName = resLoginForSuccess.firstName;
		user._lastName = resLoginForSuccess.lastName;
		user._nickname = resLoginForSuccess.nickname;
		user._email = resLoginForSuccess.email;
		user._userType = resLoginForSuccess.userType;

		// server response doesn't contain group, ahh :(
		user._group = new Group();
		user._group._id = resLoginForSuccess.groupId;

		user._teams = TeamManager.sharedInstance().parseTeamForResLogin(resLoginForSuccess);
		user._access_token = resLoginForSuccess.access_token;
		user._userIP = resLoginForSuccess.userIP;
		return user;
	}

	public void updateUserInfo(UserService.ResUserUpdate resUserUpdate) {
		if (UserManager.sharedInstance()._currentUser == null) {
			UserManager.sharedInstance()._currentUser = new User();
		}

		UserManager.sharedInstance()._currentUser._id = resUserUpdate._id;
		UserManager.sharedInstance()._currentUser._firstName = resUserUpdate.firstName;
		UserManager.sharedInstance()._currentUser._lastName = resUserUpdate.lastName;
		UserManager.sharedInstance()._currentUser._email = resUserUpdate.email;
		UserManager.sharedInstance()._currentUser._userType = resUserUpdate.userType;
		UserManager.sharedInstance()._currentUser._activationToken = resUserUpdate.activationToken;
		UserManager.sharedInstance()._currentUser._sendEmailAt = resUserUpdate.sendEmailAt;
		UserManager.sharedInstance()._currentUser._emailSent = resUserUpdate.emailSent;
		UserManager.sharedInstance()._currentUser._hostName = resUserUpdate.hostName;
		UserManager.sharedInstance()._currentUser._isActive = resUserUpdate.isActive;
		UserManager.sharedInstance()._currentUser._nickname = resUserUpdate.nickname;
	}

	public User parseUserForSignupRes(UserService.ResSignup resSignup) {
		/*
    {
        isActive: true
        _id: 12
        email: "test2@caloriecloud.org"
        firstName: "fn2"
        lastName: "ln2"
        userType: "teacher"
        nickname: "nn2"
        groupId: 1
        activationToken: "378e75c8-584d-4349-94d6-45bbcaee5497"
        fullName: "fn2 ln2"
        id: 12
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
        }
        access_token: "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEyLCJkYXRlIjoxNDIxMDU5MjI1MTI4LCJpYXQiOjE0MjEwNTkyMjV9.h-1fjLysI0qfOL8Y_GalkT-ZpIfTmt3YXsaaNlP3Ljs"
        userIP: "106.186.112.128"
    }
    */

		String userType = resSignup.userType;
		User user = null;
		if (userType.equals(User.USERTYPE_TEACHER)) {
			Teacher teacher = new Teacher();
			user = teacher;
		} else {
			user = new User();
		}

		user._isActive = resSignup.isActive;
		user._id = resSignup.id;
		user._email = resSignup.email;
		user._firstName = resSignup.firstName;
		user._lastName = resSignup.lastName;
		user._nickname = resSignup.nickname;
		user._activationToken = resSignup.activationToken;
		user._access_token = resSignup.access_token;
		user._fullName = resSignup.fullName;
		user._group = GroupManager.sharedInstance().parseGroupForSignupRes(resSignup);
		user._regCode = user._group._regCode;
		user._userIP = resSignup.userIP;

		return user;
	}

	public boolean updateTeamForCurrentuser(Team team) {
		boolean bRet = false;

		if (_currentUser._teams == null)
			return false;

		for (int i = 0; i < _currentUser._teams.size(); i++) {
			if (_currentUser._teams.get(i)._id == team._id) {
				_currentUser._teams.get(i)._name = team._name;
				_currentUser._teams.get(i)._grade = team._grade;
				_currentUser._teams.get(i)._description = team._description;
				_currentUser._teams.get(i)._imageSrc = team._imageSrc;
				//_currentUser._teams.get(i)._studentCount = team._studentCount;
				_currentUser._teams.get(i)._startDate = team._startDate;
				_currentUser._teams.get(i)._endDate = team._endDate;
				_currentUser._teams.get(i)._height = team._height;
				_currentUser._teams.get(i)._weight = team._weight;
				_currentUser._teams.get(i)._stride = team._stride;
				_currentUser._teams.get(i)._message = team._message;

				bRet = true;
				break;
			}
		}

		return bRet;
	}
}
