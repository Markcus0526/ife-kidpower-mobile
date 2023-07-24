package org.caloriecloud.android.model;

import java.util.HashMap;
import java.util.Map;

public class User {

    private int userId;
    private String screenName;
    private String accessToken;
    private String email;

    public User(int userId, String email, String screenName, String accessToken) {
        this.userId = userId;
        this.screenName = screenName;
        this.email = email;
        this.accessToken = accessToken;

    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
