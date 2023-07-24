package org.unicefkidpower.schools.context;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import org.unicefkidpower.schools.model.Group;
import org.unicefkidpower.schools.model.ReportConfiguration;
import org.unicefkidpower.schools.model.Team;
import org.unicefkidpower.schools.model.User;

import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Created by donal_000 on 2/27/2015.
 */
public class UserContext {
	public static UserContext		sharedInstance;

	protected Context				contextInstance;
	protected SharedPreferences		sharedPreferences;

	public UserContext(Context context) {
		contextInstance = context;

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static UserContext initialize(Context context) {
		if (sharedInstance == null)
			sharedInstance = new UserContext(context);
		return sharedInstance;
	}

	public static UserContext sharedInstance() {
		return sharedInstance;
	}

	public boolean isLoggedIn() {
		return sharedPreferences.getBoolean("isLoggedIn", false);
	}

	public void setLoggedIn(boolean isLoggedIn) {
		sharedPreferences.edit().putBoolean("isLoggedIn", isLoggedIn).apply();
	}

	public String lastUserName() {
		return sharedPreferences.getString("lastUserName", "");
	}

	public void setLastUserName(String userName) {
		sharedPreferences.edit().putString("lastUserName", userName).apply();
	}

	public String lastUserPassword() {
		return sharedPreferences.getString("lastUserPassword", "");
	}

	public void setLastUserPassword(String userPassword) {
		sharedPreferences.edit().putString("lastUserPassword", userPassword).apply();
	}

	public boolean isSignUp() {
		return sharedPreferences.getBoolean("isSignUp", false);
	}

	public void setSignUp(boolean isSignUp) {
		sharedPreferences.edit().putBoolean("isSignUp", isSignUp).apply();
	}

	public int signUpStage() {
		return sharedPreferences.getInt("signUpStage", 0);
	}

	public void setSignUpStage(int signUpStage) {
		sharedPreferences.edit().putInt("signUpStage", signUpStage).apply();
	}

	public void setLastConfiguration(ReportConfiguration config) {
		sharedPreferences.edit().putString("lastConfig", config.encode2JsonString()).apply();
	}

	public ReportConfiguration getLastConfiguration() {
		ReportConfiguration result;

		String contents = sharedPreferences.getString("lastConfig", "");
		result = ReportConfiguration.decodeFromJSONString(contents);

		return result;
	}


	/**
	 * @param language Language abbreviation
	 * @return true if the new language and old language are different, otherwise returns false
	 */
	public boolean setUserLanguage(String language) {
		String userLanguage = language;
		if (TextUtils.isEmpty(userLanguage)) {
			userLanguage = "EN-US";
		}

		Locale locale = new Locale("en", "US");

		if (userLanguage.toLowerCase().contains("nl")) {
			locale = new Locale("nl", "NL");
		} else if (userLanguage.toLowerCase().contains("uk") || userLanguage.toLowerCase().contains("gb")) {
			locale = new Locale("en", "GB");
		}

		boolean isDifferent = !locale.getCountry().equalsIgnoreCase(getAppLocale().getCountry()) ||
				!locale.getLanguage().equalsIgnoreCase(getAppLocale().getLanguage());

		setAppLocale(locale);

		return isDifferent;
	}

	public User getUser() {
		User user = new User();
		user._id = sharedPreferences.getInt("user._id", 0);
		user._regCode = sharedPreferences.getString("user._regCode", "");
		user._firstName = sharedPreferences.getString("user._firstName", "");
		user._lastName = sharedPreferences.getString("user._lastName", "");
		user._fullName = sharedPreferences.getString("user._fullName", "");
		user._nickname = sharedPreferences.getString("user._nickname", "");
		user._userType = sharedPreferences.getString("user._userType", User.USERTYPE_TEACHER);
		user._access_token = sharedPreferences.getString("user._access_token", "");

		user._group = new Group();
		user._group._id = sharedPreferences.getInt("user._group._id", 0);

		user._teams = new ArrayList<>();
		Team team = new Team();
		team._id = sharedPreferences.getInt("user._team._id", 0);
		user._teams.add(team);

		return user;
	}

	public void setUser(User user) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt("user._id", user._id);
		editor.putString("user._regCode", user._regCode);
		editor.putString("user._firstName", user._firstName);
		editor.putString("user._lastName", user._lastName);
		editor.putString("user._fullName", user._fullName);
		editor.putString("user._nickname", user._nickname);
		editor.putString("user._userType", user._userType);
		editor.putString("user._access_token", user._access_token);

		if (user._group != null)
			editor.putInt("user._group._id", user._group._id);

		if (user._teams.size() > 0) {
			Team team = user._teams.get(0);
			editor.putInt("user._team._id", team._id);
		}

		editor.apply();
	}

	public Locale getAppLocale(Context context) {
		String appLocale = sharedPreferences.getString("app_locale", "");
		if (appLocale.length() == 0) {
			// This is first launch. We need to set the locale according to the system locale.
			String language = Locale.getDefault().getLanguage();
			String country = Locale.getDefault().getCountry();

			if (language.equalsIgnoreCase("nl") || country.equalsIgnoreCase("UK") || country.equalsIgnoreCase("GB")) {
				appLocale = language + "_" + country;
			} else {
				appLocale = "en_US";
			}
		}

		StringTokenizer tempStringTokenizer = new StringTokenizer(appLocale, "_");

		String language = "en";
		if (tempStringTokenizer.hasMoreTokens())
			language = (String) tempStringTokenizer.nextElement();
		String country = "US";
		if (tempStringTokenizer.hasMoreTokens())
			country = (String) tempStringTokenizer.nextElement();

		return new Locale(language, country);
	}


	public Locale getAppLocale() {
		return getAppLocale(null);
	}

	public void setAppLocale(Locale locale) {
		Log.d("KPC_ANDROID", "language : " + locale.getLanguage() + ", country : " + locale.getCountry());

		Resources res = contextInstance.getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = locale;
		res.updateConfiguration(conf, dm);

		String language = locale.getLanguage();
		String country = locale.getCountry();

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("app_locale", language + "_" + country);
		editor.apply();
	}

	public Team team() {
		Team team = new Team();

		team._id = sharedPreferences.getInt("team._id", 0);
		team._name = sharedPreferences.getString("team_name", "");

		return team;
	}

	public void setTeam(Team team) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt("team._id", team._id);
		editor.putString("team._name", team._name);
		editor.apply();
	}

	public Group group() {
		Group group = new Group();
		group._id = sharedPreferences.getInt("group._id", 0);
		return group;
	}

	public void setGroup(Group group) {
		sharedPreferences.edit().putInt("group._id", group._id).apply();
	}

}
