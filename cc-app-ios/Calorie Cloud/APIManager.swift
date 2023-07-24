//
//  APIManager.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import Alamofire
import SwiftyJSON
import DeviceKit

class APIManager {
    
    static func postActivityData(userId: Int, accessToken: String, activityData: [String: AnyObject], completionHandler: @escaping(JSON, String?) -> Void) {
        
        Alamofire.request(CCStatics.ActivitySummariesURL, method: .post, parameters: activityData, encoding: JSONEncoding.default, headers: ["x-access-token": accessToken]).responseString { (response) in
            
            do {
                let responseJSON = try JSON(data: response.data!)
                var error: String?
                
                switch (responseJSON) {
                    
                case JSON.null:
                    error = response.result.value
                    break;
                    
                default:
                    break;
                    
                }
                
                completionHandler(responseJSON, error)
            }
            catch {
                completionHandler(JSON.null, error.localizedDescription)
            }
            
        }
        
    }
    
    static func setDefaultDevice(userId: Int, accessToken: String, completionHandler: @escaping(JSON, String?) -> Void) {
        
        Alamofire.request(CCStatics.DefaultDeviceURL, method: .post, parameters: ["userId": userId, "device": Device().description], headers:["x-access-token": accessToken]).responseString(completionHandler: { (response) in
        
            do {
                let responseJSON = try JSON(data: response.data!)
                var error: String?
                
                switch (responseJSON) {
                    
                case JSON.null:
                    error = response.result.value
                    break;
                    
                default:
                    break;
                    
                }
                
                completionHandler(responseJSON, error)
            }
            catch {
                completionHandler(JSON.null, error.localizedDescription)
            }
            
        
        })
    }
    
    static func setDefaultSource(userId: Int, accessToken: String, completionHandler: @escaping(JSON, String?) -> Void) {
        
        Alamofire.request(CCStatics.DefaultSourceURL, method: .post, parameters: ["userId": userId, "deviceName":"healthkit"], headers:["x-access-token": accessToken]).responseString(completionHandler: { (response) in
            
            do {
                let responseJSON = try JSON(data: response.data!)
                
                var error: String?
                
                switch (responseJSON) {
                    
                case JSON.null:
                    error = response.result.value
                    break;
                    
                default:
                    break;
                    
                }
                
                completionHandler(responseJSON, error)
            }
            catch {
                completionHandler(JSON.null, error.localizedDescription)
            }
            
            
        })
        
        
    }
    
    static func getLastSyncDate(userId: Int, accessToken: String, completionHandler: @escaping (JSON, String?) -> Void) {
        
        Alamofire.request(CCStatics.LastSyncDateURL+"\(userId)", headers: ["x-access-token":accessToken]).responseString { (response) in
            
            do {
                let responseJSON = try JSON(data: response.data!)
                
                var error: String?
                
                switch (responseJSON) {
                    
                case JSON.null:
                    error = response.result.value
                    break;
                    
                default:
                    break;
                    
                }
                
                completionHandler(responseJSON, error)
            }
            catch {
                completionHandler(JSON.null, error.localizedDescription)
            }
        
        }
        
    }
    
    static func createUser(email: String, password: String, username: String, completionHandler: @escaping (JSON, String?) -> Void) {
        
        let createUserParameters = ["email": email, "password": password, "screenName": username]
        
        Alamofire.request(CCStatics.CreateAccountURL, method: .post, parameters: createUserParameters).responseString { (response) in
            
            do {
                let responseJSON = try JSON(data: response.data!)
                
                var error: String?
                
                switch (responseJSON) {
                    
                case JSON.null:
                    error = response.result.value
                    break;
                    
                default:
                    break;
                    
                }
                
                completionHandler(responseJSON, error)
            }
            catch {
                completionHandler(JSON.null, error.localizedDescription)
            }
        }
    }
    
    static func loginUser(email: String, password: String, completionHandler: @escaping (JSON, String?) -> Void) {
        
        let loginUserParameters = ["email": email, "password": password]
    
        Alamofire.request(CCStatics.LoginURL, method: .post, parameters: loginUserParameters).responseString { (response) in
            
            do {
                let responseJSON = try JSON(data: response.data!)
                var error: String?
                
                switch (responseJSON) {
                    
                case JSON.null:
                    error = response.result.value
                    break;
                    
                default:
                    break;
                    
                }
                
                completionHandler(responseJSON, error)
            }
            catch {
                completionHandler(JSON.null, error.localizedDescription)
            }
            
        }
        
    }
    
    static func logoutUser(accessToken: String, completionHandler: @escaping(String?) -> Void) {
        
        Alamofire.request(CCStatics.LogoutURL, method: .post, headers: ["x-access-token": accessToken]).responseString { (response) in
            
            completionHandler(response.result.value)
        }
        
    }
    
    static func sendPasswordReset(email: String, brand: String?, completionHandler: @escaping(JSON, String?) -> Void) {
            
        let fromOrg = (brand == nil) ? "Calorie Cloud" : brand!
        let fromEmail = (brand == nil) ? "hello@caloriecloud.org" : "workplace@unicefkidpower.org"
        
        Alamofire.request(CCStatics.PasswordResetURL, method: .post, parameters: ["email":email, "fromOrg": fromOrg, "from": fromEmail]).responseString{ (response) in
            
            do {
                let responseJSON = try JSON(data: response.data!)
                var error: String?
                
                switch (responseJSON) {
                    
                case JSON.null:
                    
                    if (response.result.value == "User not found!") {
                        error = "This email is not associated with any existing account"
                    }
                    else {
                        error = response.result.value
                    }
                    break;
                    
                default:
                    break;
                    
                }
                
                completionHandler(responseJSON, error)
            }
            catch {
                completionHandler(JSON.null, error.localizedDescription)
            }
            
        }
        
        
    }
    
    static func createCustomerIOAccount(userId: Int, email: String, screenName: String, endDate: String, daysLeft: Int, brand: String, contentType: Int, completionHandler: @escaping(DataResponse<Any>) -> Void) {
        
        let fromOrg = (brand == "calorieCloud") ? "Calorie Cloud" : "UNICEF Kid Power Workplace"
        let fromEmail = (brand == "calorieCloud") ? "hello@caloriecloud.org" : "workplace@unicefkidpower.org"
        
        let createCustomerIOAccountParameters = ["userId": userId, "email": email, "screenName": screenName, "endDate": endDate, "daysLeft": daysLeft, "brand": brand, "from": fromEmail, "fromOrg": fromOrg, "contentType": contentType] as [String : Any]
        
        Alamofire.request(CCStatics.AccountCreationEmailURL, method: .post, parameters: createCustomerIOAccountParameters).responseJSON { (response) in
            completionHandler(response)
        }
        
    }
    
    static func joinTeam(challengeId: Int, teamId: Int, userId: Int, accessToken: String, completionHandler: @escaping(JSON, String?) -> Void) {
        
        let joinTeamParameters = ["challengeId": challengeId, "teamId": teamId, "userId": userId]
        
        Alamofire.request(CCStatics.JoinTeamURL, method: .post, parameters: joinTeamParameters, headers: ["x-access-token": accessToken]).responseString { (response) in
            
            do {
                let responseJSON = try JSON(data: response.data!)
                var error: String?
                
                switch (responseJSON) {
                    
                case JSON.null:
                    error = response.result.value
                    break;
                    
                default:
                    break;
                    
                }
                
                completionHandler(responseJSON, error)
            }
            catch {
                completionHandler(JSON.null, error.localizedDescription)
            }
        
        }
        
    }
    
    static func getRegionsByChallengeId(challengeId: Int, completionHandler: @escaping(JSON, String?) -> Void) {
        
        Alamofire.request(CCStatics.RegionsByChallengeURL + "\(challengeId)").responseString { (response) in
            
            do {
                let responseJSON = try JSON(data: response.data!)
                var error: String?
                
                switch (responseJSON) {
                    
                case JSON.null:
                    error = response.result.value
                    break;
                    
                default:
                    break;
                    
                }
                
                completionHandler(responseJSON, error)
            }
            catch {
                completionHandler(JSON.null, error.localizedDescription)
            }
            
        }
    
    }
    
    static func getChallengeByCode(eventCode: String, completionHandler: @escaping(JSON, String?) -> Void) {
        Alamofire.request(CCStatics.JoinChallengeURL, method: .post, parameters: ["challengeCode":eventCode]).responseString{ (response) in
            
            do {
                let responseJSON = try JSON(data: response.data!)
                var error: String?
                
                switch (responseJSON) {
                    
                case JSON.null:
                    error = response.result.value
                    break;
                    
                default:
                    break;
                    
                }
                
                completionHandler(responseJSON, error)
            }
            catch {
                completionHandler(JSON.null, error.localizedDescription)
            }
            
        }
    }
    
    static func getLastSyncDateDevice(userId: Int, accessToken: String, completionHandler: @escaping(JSON, String?) -> Void) {
        Alamofire.request(CCStatics.LastSyncDateDeviceURL + "\(userId)", headers: ["x-access-token":accessToken]).responseString{ (response) in
            
            do {
                let responseJSON = try JSON(data: response.data!)
                var error: String?
                
                switch (responseJSON) {
                    
                case JSON.null:
                    error = response.result.value
                    break;
                    
                default:
                    break;
                    
                }
                
                completionHandler(responseJSON, error)
            }
            catch {
                completionHandler(JSON.null, error.localizedDescription)
            }
        }
    }
    
    static func getUser(userId: Int, accessToken: String, completionHandler: @escaping(JSON, String?) -> Void) {
        Alamofire.request(CCStatics.UsersURL + "\(userId)", headers: ["x-access-token":accessToken]).responseString{ (response) in
            
            do {
                let responseJSON = try JSON(data: response.data!)
                var error: String?
                
                switch (responseJSON) {
                    
                case JSON.null:
                    error = response.result.value
                    break;
                    
                default:
                    break;
                    
                }
                
                completionHandler(responseJSON, error)
            }
            catch {
                completionHandler(JSON.null, error.localizedDescription)
            }
            
        }
    }
    
}
