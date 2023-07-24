//
//  Challenge.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import SwiftyJSON

class Challenge {
    
    var _id: Int!
    var type: String!
    var name: String!
    var description: String!
    var startDate: String!
    var endDate: String!
    var fullSync: Bool!
    var adminCode: String!
    var participantCode: String!
    var imageSrc: String!
    var goal: Int!
    var impactMultiplier: String!
    var deviceSettings: Array<DeviceSetting>!
    var defaultTeamId: Int!
    var notifyDate: String!
    var notified: Bool!
    var isActive: Bool!
    var teamSelection: Int!
    var brand: String!
    var color: String!
    var createdAt: String!
    var updatedAt: String!
    var orgId: Int!
    var groupId: Int!
    var organization: Organization!
    var teams: Array<Team>!
    
    init(_id: Int, type: String, name: String, description: String, startDate: String, endDate: String, fullSync: Bool, adminCode: String, participantCode: String, imageSrc: String, goal: Int, impactMultiplier: String, deviceSettings: Array<DeviceSetting>, defaultTeamId: Int, notifyDate: String, notified: Bool, isActive: Bool, teamSelection: Int, brand: String, color: String, createdAt: String, updatedAt: String, orgId: Int, groupId: Int, organization: Organization, teams: Array<Team>) {
        
        self._id = _id
        self.type = type
        self.name = name
        self.description = description
        self.startDate = startDate
        self.endDate = endDate
        self.fullSync = fullSync
        self.adminCode = adminCode
        self.participantCode = participantCode
        self.imageSrc = imageSrc
        self.goal = goal
        self.impactMultiplier = impactMultiplier
        self.deviceSettings = deviceSettings
        self.defaultTeamId = defaultTeamId
        self.notifyDate = notifyDate
        self.notified = notified
        self.isActive = isActive
        self.teamSelection = teamSelection
        self.brand = brand
        self.color = color
        self.createdAt = createdAt
        self.updatedAt = updatedAt
        self.orgId = orgId
        self.groupId = groupId
        self.organization = organization
        self.teams = teams
        
    }
    
    init(json: JSON) {
        self._id = json["_id"].intValue
        self.type = json["type"].stringValue
        self.name = json["name"].stringValue
        self.description = json["description"].stringValue
        self.startDate = json["startDate"].stringValue
        self.endDate = json["endDate"].stringValue
        self.fullSync = json["fullSync"].boolValue
        self.adminCode = json["adminCode"].stringValue
        self.participantCode = json["participantCode"].stringValue
        self.imageSrc = json["imageSrc"].stringValue
        self.goal = json["goal"].intValue
        self.impactMultiplier = json["impactMultiplier"].stringValue
        self.deviceSettings = DeviceSetting.toArray(jsonArray: json["deviceSettings"].arrayValue)
        self.defaultTeamId = json["defaultTeamId"].intValue
        self.notifyDate = json["notifyDate"].stringValue
        self.notified = json["notified"].boolValue
        self.isActive = json["isActive"].boolValue
        self.teamSelection = json["teamSelection"].intValue
        self.brand = json["brand"].stringValue
        self.color = json["color"].stringValue
        self.createdAt = json["createdAt"].stringValue
        self.updatedAt = json["updatedAt"].stringValue
        self.orgId = json["orgId"].intValue
        self.groupId = json["groupId"].intValue
        self.organization = Organization(json: json["organization"])
        self.teams = Team.toArray(jsonArray: json["teams"].arrayValue)
        
    }
    

}
