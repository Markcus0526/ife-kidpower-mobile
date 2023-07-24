//
//  DeviceSetting.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import SwiftyJSON

class DeviceSetting {
    
    var type: String!
    var height: Int!
    var weight: Int!
    var stride: Int!
    var message: String!
    
    init(height: Int, message: String, stride: Int, type: String, weight: Int) {
        self.height = height
        self.message = message
        self.stride = stride
        self.type = type
        self.weight = weight
    }
    
    init(json: JSON) {
        self.type = json["type"].stringValue
        self.height = json["heaight"].intValue
        self.weight = json["weight"].intValue
        self.stride = json["stride"].intValue
        self.message = json["message"].stringValue
    }
    
    static func toArray(jsonArray: [JSON]) -> Array<DeviceSetting> {
        var arrayList = Array<DeviceSetting>()
        
        jsonArray.forEach { (json) in
            arrayList.append(DeviceSetting(json: json))
        }
        
        return arrayList
    }

}
