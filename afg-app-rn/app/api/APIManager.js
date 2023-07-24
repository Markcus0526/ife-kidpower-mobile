export default class APIManager {

    static CURRENT_CHALLENGES = "/api/v2/challenges/user/current";
    static UPCOMING_CHALLENGES = "/api/v2/challenges/user/upcoming";
    static PREVIOUS_CHALLENGES = "/api/v2/challenges/user/completed";
    static CHALLENGE_SUMMARY = "/api/v2/userSummary/challenge/";
    static LAST_SYNC_DATE_DEVICE = "/api/v2/getLastSyncDate/user/";
    static LATEST_CHALLENGE = "/api/v2/challenges/latest/user/";

    static getCurrentChallenge(BASE_URL, accessToken) {
        return fetch(BASE_URL + APIManager.CURRENT_CHALLENGES, {
            method: "GET",
            headers: {
                "X-Access-Token": accessToken
            }
        })
    }

    static getUpcomingChallenges(BASE_URL, accessToken) {
        return fetch(BASE_URL + APIManager.UPCOMING_CHALLENGES, {
            method: "GET",
            headers: {
                "X-Access-Token": accessToken
            }
        })
    }

    static getPreviousChallenges(BASE_URL, accessToken) {

        return fetch(BASE_URL + APIManager.PREVIOUS_CHALLENGES, {
            method: "GET",
            headers: {
                "X-Access-Token": accessToken
            }
        })

    }

    static getLastSyncDateDevice(BASE_URL, userID, accessToken) {
        return fetch(BASE_URL + APIManager.LAST_SYNC_DATE_DEVICE + userID, {
            method: "GET",
            headers: {
                "X-Access-Token": accessToken
            }
        })
    }

    static getChallengeSummary(BASE_URL, challengeID, userID, accessToken) {
        return fetch(BASE_URL + APIManager.CHALLENGE_SUMMARY + challengeID + "/user/" + userID, {
            method: "GET",
            headers: {
                "X-Access-Token": accessToken
            }
        })
    }

    static getLatestChallenge(BASE_URL, userID, accessToken) {
        return fetch(BASE_URL + APIManager.LATEST_CHALLENGE + userID, {
            method: "GET",
            headers: {
                "X-Access-Token": accessToken
            }
        })
    }
}