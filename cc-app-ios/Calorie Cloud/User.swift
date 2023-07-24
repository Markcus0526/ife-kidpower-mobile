//
//  User.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import SwiftyJSON

class User {
    
    var userId: Int!
    var accessToken: String!
    var email: String!
    var screenName: String!
    
    init (userId: Int, email: String, screenName: String, accessToken: String) {
        self.userId = userId
        self.email = email
        self.screenName = screenName
        self.accessToken = accessToken
    }
    
    init (json: JSON) {
        self.userId = json["_id"].intValue
        self.email = json["email"].stringValue
        self.screenName = json["screenName"].stringValue
        self.accessToken = json["access_token"].stringValue
    }
    
    init (serializedUser: NSDictionary?) {
        self.userId = serializedUser?["userId"] as? Int
        self.email = serializedUser?["email"] as? String
        self.screenName = serializedUser?["screenName"] as? String
        self.accessToken = serializedUser?["accessToken"] as? String
    }
    
    func toDictionary() -> NSDictionary {
        return [
            "userId": self.userId,
            "email": self.email,
            "screenName": self.screenName,
            "accessToken": self.accessToken
        ]
    }
    
    
    
}
