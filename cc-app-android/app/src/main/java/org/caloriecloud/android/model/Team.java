package org.caloriecloud.android.model;

import java.io.Serializable;

public class Team implements Serializable{

    private int _id;
    private String name;
    private String imageSrc;
    private String lastSyncDate;
    private int challengeId;
    private int regionId;
    private Challenge challenge;
    private Region region;

    public Team(int _id, Challenge challenge, int challengeId, String imageSrc, String lastSyncDate, String name, int regionId, Region region) {
        this._id = _id;
        this.challenge = challenge;
        this.challengeId = challengeId;
        this.imageSrc = imageSrc;
        this.lastSyncDate = lastSyncDate;
        this.name = name;
        this.regionId = regionId;
        this.region = region;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
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

    public String getImageSrc() {
        return imageSrc;
    }

    public void setImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;
    }

    public String getLastSyncDate() {
        return lastSyncDate;
    }

    public void setLastSyncDate(String lastSyncDate) {
        this.lastSyncDate = lastSyncDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRegionId() {
        return regionId;
    }

    public void setRegionId(int regionId) {
        this.regionId = regionId;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }
}
