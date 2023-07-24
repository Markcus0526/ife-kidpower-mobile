//
//  Team.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import SwiftyJSON

class Team {
    
    var _id: Int!
    var name: String!
    var imageSrc: String!
    var lastSyncDate: String!
    var challengeId: Int!
    var regionId: Int!
    var challenge: Challenge!
    var region: Region!
    
    init(_id: Int, name: String, imageSrc: String, lastSyncDate: String, challengeId: Int, regionId: Int, challenge: Challenge, region: Region) {
        self._id = _id
        self.name = name
        self.imageSrc = imageSrc
        self.lastSyncDate = lastSyncDate
        self.challengeId = challengeId
        self.regionId = regionId
        self.challenge = challenge
        self.region = region
    }
    
    init(json: JSON) {
        self._id = json["_id"].intValue
        self.name = json["name"].stringValue
        self.imageSrc = json["imageSrc"].stringValue
        self.challengeId = json["challengeId"].intValue
        self.regionId = json["regionId"].intValue
        self.challenge = Challenge(json: json["challenge"])
        self.region = Region(json: json["region"])
    }
    
    static func toArray(jsonArray: [JSON]) -> Array<Team> {
        var arrayList = Array<Team>()
        
        jsonArray.forEach { (json) in
            arrayList.append(Team(json: json))
        }
        
        return arrayList
    }
    
}
