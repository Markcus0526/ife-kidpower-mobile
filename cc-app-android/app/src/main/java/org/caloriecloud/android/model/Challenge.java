package org.caloriecloud.android.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Challenge implements Serializable{
    private int _id;
    private String type;
    private String name;
    private String description;
    private String startDate;
    private String endDate;
    private boolean fullSync;
    private String adminCode;
    private String participantCode;
    private String imageSrc;
    private int goal;
    private String impactMultiplier;
    private ArrayList<DeviceSetting> deviceSettings;
    private int defaultTeamId;
    private String notifyDate;
    private boolean notified;
    private boolean isActive;
    private int teamSelection;
    private String brand;
    private String color;
    private String createdAt;
    private String updatedAt;
    private int orgId;
    private int groupId;
    private Organization organization;
    private ArrayList<Team> teams;

    public Challenge(int _id, String adminCode, String brand, String color, String createdAt, int defaultTeamId, String description, ArrayList<DeviceSetting> deviceSettings, String endDate, boolean fullSync, int goal, int groupId, String imageSrc, String impactMultiplier, boolean isActive, String name, boolean notified, String notifyDate, Organization organization, int orgId, String participantCode, String startDate, ArrayList<Team> teams, int teamSelection, String type, String updatedAt) {
        this._id = _id;
        this.adminCode = adminCode;
        this.brand = brand;
        this.color = color;
        this.createdAt = createdAt;
        this.defaultTeamId = defaultTeamId;
        this.description = description;
        this.deviceSettings = deviceSettings;
        this.endDate = endDate;
        this.fullSync = fullSync;
        this.goal = goal;
        this.groupId = groupId;
        this.imageSrc = imageSrc;
        this.impactMultiplier = impactMultiplier;
        this.isActive = isActive;
        this.name = name;
        this.notified = notified;
        this.notifyDate = notifyDate;
        this.organization = organization;
        this.orgId = orgId;
        this.participantCode = participantCode;
        this.startDate = startDate;
        this.teams = teams;
        this.teamSelection = teamSelection;
        this.type = type;
        this.updatedAt = updatedAt;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getDefaultTeamId() {
        return defaultTeamId;
    }

    public void setDefaultTeamId(int defaultTeamId) {
        this.defaultTeamId = defaultTeamId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<DeviceSetting> getDeviceSettings() {
        return deviceSettings;
    }

    public void setDeviceSettings(ArrayList<DeviceSetting> deviceSettings) {
        this.deviceSettings = deviceSettings;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isFullSync() {
        return fullSync;
    }

    public void setFullSync(boolean fullSync) {
        this.fullSync = fullSync;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getImageSrc() {
        return imageSrc;
    }

    public void setImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;
    }

    public String getImpactMultiplier() {
        return impactMultiplier;
    }

    public void setImpactMultiplier(String impactMultiplier) {
        this.impactMultiplier = impactMultiplier;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public String getNotifyDate() {
        return notifyDate;
    }

    public void setNotifyDate(String notifyDate) {
        this.notifyDate = notifyDate;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public String getParticipantCode() {
        return participantCode;
    }

    public void setParticipantCode(String participantCode) {
        this.participantCode = participantCode;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public ArrayList<Team> getTeams() {
        return teams;
    }

    public void setTeams(ArrayList<Team> teams) {
        this.teams = teams;
    }

    public int getTeamSelection() {
        return teamSelection;
    }

    public void setTeamSelection(int teamSelection) {
        this.teamSelection = teamSelection;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }


}
