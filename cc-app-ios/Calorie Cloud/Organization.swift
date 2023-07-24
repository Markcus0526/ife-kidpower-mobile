//
//  Organization.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import SwiftyJSON

class Organization {
    
    var _id: Int!
    var name: String!
    var imageSrc: String!
    var createdAt: String!
    var updatedAt: String!
    
    init(_id: Int, name: String, imageSrc: String, createdAt: String, updatedAt: String) {
        self._id = _id
        self.name = name
        self.imageSrc = imageSrc
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }
    
    init(json: JSON) {
        self._id = json["_id"].intValue
        self.name = json["name"].stringValue
        self.imageSrc = json["imageSrc"].stringValue
        self.createdAt = json["createdAt"].stringValue
        self.updatedAt = json["updatedAt"].stringValue
    }
    
}
