package org.caloriecloud.android.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Region implements Serializable {

    private int _id;
    private String name;
    private int challengeId;
    private Challenge challenge;
    private ArrayList<Team> teams;

    public Region(int _id, Challenge challenge, int challengeId, String name, ArrayList<Team> teams) {
        this._id = _id;
        this.challenge = challenge;
        this.challengeId = challengeId;
        this.name = name;
        this.teams = teams;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public int getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(int challengeId) {
        this.challengeId = challengeId;
    }

    public ArrayList<Team> getTeams() {
        return teams;
    }

    public void setTeams(ArrayList<Team> teams) {
        this.teams = teams;
    }
}
