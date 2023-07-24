package org.caloriecloud.android.util;

import android.os.Build;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class APIManager {

    private static OkHttpClient requestClient = OkHttpSharedClient.getInstance().getClient();

    public static void getRegionsByChallengeId(int challengeId, Callback callback) {
        final Request getRegionsRequest = new Request.Builder()
                .url(CCStatics.REGIONS_BY_CHALLENGE_URL + challengeId)
                .get()
                .build();

        requestClient.newCall(getRegionsRequest).enqueue(callback);
    }

    public static void joinTeam(int userId, int challengeId, int teamId, String accessToken, final Callback callback) {
        final FormBody teamBody = new FormBody.Builder()
                .add("challengeId", Integer.toString(challengeId))
                .add("teamId", Integer.toString(teamId))
                .add("userId", Integer.toString(userId))
                .build();

        final Request joinTeamRequest = new Request.Builder()
                .url(CCStatics.JOIN_TEAM_URL)
                .header("x-access-token", accessToken)
                .post(teamBody)
                .build();

        requestClient.newCall(joinTeamRequest).enqueue(callback);
    }

    public static void createCustomerIOAccount(int userId, String screenName, String endDate, int daysLeft, String brand, int contentType, Callback callback) {

        String fromOrg = (brand.equals("calorieCloud")) ? "Calorie Cloud" : "UNICEF Kid Power Workplace";
        String fromEmail = (brand.equals("calorieCloud")) ? "hello@caloriecloud.org" : "workplace@unicefkidpower.org";

        final FormBody accountBody = new FormBody.Builder()
                .add("contentType", Integer.toString(contentType))
                .add("daysLeft", Integer.toString(daysLeft))
                .add("userId", Integer.toString(userId))
                .add("fromOrg", fromOrg)
                .add("from", fromEmail)
                .add("screenName", screenName)
                .add("endDate", endDate)
                .add("brand", brand)
                .build();

        final Request accountCreationEmailRequest = new Request.Builder()
                .url(CCStatics.ACCOUNT_CREATION_EMAIL_URL)
                .post(accountBody)
                .build();

        requestClient.newCall(accountCreationEmailRequest).enqueue(callback);

    }

    public static void createUser(String screenName, String email, String password, Callback callback) {

        final RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .addEncoded("password", password)
                .add("screenName", screenName)
                .build();

        final Request createAccountRequest = new Request.Builder()
                .url(CCStatics.CREATE_ACCOUNT_URL)
                .post(formBody)
                .build();

        requestClient.newCall(createAccountRequest).enqueue(callback);

    }

    public static void getLastSyncDate(int userId, String accessToken, Callback callback) {
        Request lastSyncDateRequest = new Request.Builder()
                .url(CCStatics.LAST_SYNC_DATE_URL + userId)
                .header("x-access-token", accessToken)
                .get()
                .build();

        requestClient.newCall(lastSyncDateRequest).enqueue(callback);
    }

    public static void logoutUser(int userId, String accessToken, Callback callback) {

        RequestBody formBody = new FormBody.Builder()
                .add("userId", Integer.toString(userId))
                .build();

        Request logoutRequest = new Request.Builder()
                .url(CCStatics.LOGOUT_URL)
                .header("x-access-token", accessToken)
                .post(formBody)
                .build();

        requestClient.newCall(logoutRequest).enqueue(callback);
    }

    public static void setDefaultSource(int userId, String accessToken, Callback callback) {

        RequestBody formBody = new FormBody.Builder()
                .add("userId", Integer.toString(userId))
                .add("deviceName", "googlefit")
                .build();

        Request sourceRequest = new Request.Builder()
                .url(CCStatics.DEFAULT_SOURCE_URL)
                .post(formBody)
                .header("x-access-token", accessToken)
                .build();

        requestClient.newCall(sourceRequest).enqueue(callback);
    }

    public static void setDefaultDevice(int userId, String accessToken, Callback callback) {

        RequestBody formBody = new FormBody.Builder()
                .add("userId", Integer.toString(userId))
                .add("device", Build.MODEL)
                .build();

        Request deviceRequest = new Request.Builder()
                .url(CCStatics.DEFAULT_DEVICE_URL)
                .post(formBody)
                .header("x-access-token", accessToken)
                .build();


        requestClient.newCall(deviceRequest).enqueue(callback);
    }

    public static void loginUser(String email, String password, Callback callback) {

        final RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .addEncoded("password", password)
                .build();

        final Request loginRequest = new Request.Builder()
                .url(CCStatics.LOGIN_URL)
                .post(formBody)
                .build();

        requestClient.newCall(loginRequest).enqueue(callback);
    }

    public static void getChallengeWithEventCode(String eventCode, Callback callback) {

        final RequestBody formBody = new FormBody.Builder()
                .add("challengeCode", eventCode)
                .build();

        final Request registerRequest = new Request.Builder()
                .url(CCStatics.JOIN_CHALLENGE_URL)
                .post(formBody)
                .build();

        requestClient.newCall(registerRequest).enqueue(callback);
    }

    public static void sendPasswordReset(String email, String brand, Callback callback) {

        String fromOrg = (brand == null) ? "Calorie Cloud" : brand;
        String fromEmail = (brand == null) ? "hello@caloriecloud.org" : "workplace@unicefkidpower.org";

        final RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("fromOrg", fromOrg)
                .add("from", fromEmail)
                .build();

        final Request passwordResetRequest = new Request.Builder()
                .url(CCStatics.PASSWORD_RESET_URL)
                .post(formBody)
                .build();

        requestClient.newCall(passwordResetRequest).enqueue(callback);
    }

    public static void getLastSyncDateDevice(int userId, String accessToken, Callback callback) {
        Request lastSyncDateRequest = new Request.Builder()
                .url(CCStatics.LAST_SYNC_DATE_DEVICE_URL + userId)
                .header("x-access-token", accessToken)
                .get()
                .build();

        requestClient.newCall(lastSyncDateRequest).enqueue(callback);
    }

    public static void getUser(int userId, String accessToken, Callback callback) {
        Request lastSyncDateRequest = new Request.Builder()
                .url(CCStatics.USERS_URL + userId)
                .header("x-access-token", accessToken)
                .get()
                .build();

        requestClient.newCall(lastSyncDateRequest).enqueue(callback);
    }

}
