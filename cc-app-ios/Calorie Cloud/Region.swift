//
//  Region.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import SwiftyJSON

class Region {
    
    var _id: Int!
    var name: String!
    var challengeId: Int!
    var challenge: Challenge!
    var teams: Array<Team>!
    
    init(_id: Int, challenge: Challenge, challengeId: Int, name: String, teams: Array<Team>) {
        self._id = _id
        self.challenge = challenge
        self.challengeId = challengeId
        self.name = name
        self.teams = teams
    }
    
    init(json: JSON) {
        self._id = json["_id"].intValue
        self.name = json["name"].stringValue
        self.challengeId = json["challengeId"].intValue
        self.challenge = Challenge(json: json["challenge"])
        self.teams = Team.toArray(jsonArray: json["teams"].arrayValue)
    }
    
    static func toArray(jsonArray: [JSON]) -> Array<Region> {
        var arrayList = Array<Region>()
        
        jsonArray.forEach { (json) in
            arrayList.append(Region(json: json))
        }
        
        return arrayList
    }
}
