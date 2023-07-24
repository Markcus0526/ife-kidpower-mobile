//
//  CCStatics.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import Foundation
import SwiftyJSON

class CCStatics {
    
    /* Colors */
    static var primaryColor:UIColor! = UIColor(red: 9.0/255.0, green: 97.0/255.0, blue: 157.0/255.0, alpha: 1)
    static var buttonDefaultColor:UIColor! = UIColor(red: 253.0/255.0, green: 181.0/255.0, blue: 21.0/255.0, alpha: 1)
    static var buttonPressedColor:UIColor! = UIColor(red: 255.0/255.0, green: 152.0/255.0, blue: 0.0/255.0, alpha: 1)
    static var altButtonDefaultColor:UIColor! = UIColor(red: 0.0/255.0, green: 173.0/255.0, blue: 239.0/255.0, alpha: 1)
    static var greyButtonDefaultColor:UIColor! = UIColor(red: 201.0/255.0, green: 201.0/255.0, blue: 201.0/255.0, alpha: 1)
    static var greyButtonPressedColor:UIColor! = UIColor(red: 181.0/255.0, green: 181.0/255.0, blue: 181.0/255.0, alpha: 1)
    static var darkGreyColor:UIColor! = UIColor(red: 92.0/255.0, green: 92.0/255.0, blue: 92.0/255.0, alpha: 1)
    
    /* Strings */
   
    #if STAGING
        static var BaseURL:String! = "https://staging.activeforgood.com"
    #else
        #if KPW
        static var BaseURL:String! = "https://workplaceapp.unicefkidpower.org"
        #else
        static var BaseURL:String! = "https://get.activeforgood.com"
        #endif
    #endif
    static var LoginURL:String! = BaseURL + "/api/v2/login"
    static var UsersURL:String! = BaseURL + "/api/v2/users/"
    static var LastSyncDateURL:String! = BaseURL + "/api/v2/users/lsd/"
    static var LastSyncDateDeviceURL:String! = BaseURL + "/api/v2/getLastSyncDate/user/"
    static var ActivitySummariesURL:String! = BaseURL + "/api/v2/activitySummaries"
    static var LogoutURL:String! = BaseURL + "/api/v2/logout"
    static var DefaultSourceURL:String! = BaseURL + "/api/v2/users/setDefaultDevice"
    static var DefaultDeviceURL:String! = BaseURL + "/api/v2/userDevices"
    static var PasswordResetURL:String! = BaseURL + "/api/v2/sendResetLink"
    static var AboutURL:String! = "http://caloriecloud.org/ios-about-us/"
    static var LearnMoreURL:String! = "http://caloriecloud.org/ios-learn-more/"
    #if KPW
    static var ZendeskURL:String! = "https://unicefkidpowerworkplace.zendesk.com"
    #else
    static var ZendeskURL:String! = "https://caloriecloud.zendesk.com"
    #endif
    static var JoinChallengeURL:String! = BaseURL + "/api/v2/challenges/challengeCode"
    static var CreateAccountURL:String! = BaseURL + "/api/v2/users"
    static var AccountCreationEmailURL:String! = BaseURL + "/api/v2/accountCreationEmail"
    static var JoinTeamURL:String! = BaseURL + "/api/v2/teamMembers/byChallenge"
    static var RegionsByChallengeURL:String! = BaseURL + "/api/v2/regions/getAllRegionsByChallengeId/"
    
    /* Challenge Info */
    static var ChallengeSingleTeam:Int! = 1
    static var ChallengeMultipleTeams:Int! = 2
    static var ChallengeRegionsTeams:Int! = 3
    
    /* Functions */
    static func isFirstLaunch() -> AnyObject? {
        return UserDefaults.standard.value(forKey: "isFirstLaunch") as AnyObject?
    }
    
    static func setFirstLaunch(_ firstLaunch: Bool) {
        UserDefaults.standard.setValue(firstLaunch, forKey: "isFirstLaunch")
        UserDefaults.standard.synchronize()
    }
    
    static func wasHealthKitEnabled() -> Bool {
        return UserDefaults.standard.bool(forKey: "wasHealthKitEnabled")
    }
    
    static func setHealthKitEnabled(_ enabled: Bool) {
        UserDefaults.standard.set(enabled, forKey: "wasHealthKitEnabled")
        UserDefaults.standard.synchronize()
    }
    
    static func getSavedUser() -> NSDictionary? {
        return UserDefaults.standard.dictionary(forKey: "User") as NSDictionary?
    }
    
    static func setSavedUser(_ user: AnyObject) {
        UserDefaults.standard.setValue((user as! User).toDictionary(), forKey: "User")
        UserDefaults.standard.synchronize()
    }
    
    static func logoutUser() {
        UserDefaults.standard.setValue(nil, forKey: "User")
    }
    
    static func setNewAuth(_ newAuth: Bool) {
        UserDefaults.standard.set(newAuth, forKey: "newAuth")
        UserDefaults.standard.synchronize()
    }
    
    static func isNewAuth() -> Bool {
        return UserDefaults.standard.bool(forKey: "newAuth")
    }
    
    static func formatDateString(dateString: String, toFormat: String, utcTimeZone: Bool) -> String {
        
        let formatter = DateFormatter()
        formatter.timeZone = TimeZone.autoupdatingCurrent
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        
        let resultsDateFormatter = DateFormatter()
        resultsDateFormatter.dateFormat = toFormat
        
        if (utcTimeZone) {
            resultsDateFormatter.timeZone = TimeZone.init(abbreviation: "UTC")
        }
        
        let startDate = formatter.date(from: dateString)
        
        return resultsDateFormatter.string(from: startDate!)
    }
    
    static func daysBetweenDates(date1: Date, date2: Date) -> Int {
        let calendar = NSCalendar.current
        
        let components = calendar.dateComponents([Calendar.Component.day], from: date2, to: date1)
        
        return components.day!
    }
    
    static func stringToDate(dateString: String) -> Date {
        
        let formatter = DateFormatter()
        formatter.timeZone = TimeZone.init(abbreviation: "UTC")
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        
        let date = formatter.date(from: dateString)
        
        return date!
    }
    
    static func trackerDisplayName(_ shortname: String) -> String {
        
        var trackerDisplay: String!
        
        switch (shortname) {
        case "movesapp":
            trackerDisplay = "Moves"
            break
            
        case "fitbit":
            trackerDisplay = "Fitbit"
            break
            
        case "jawbone_up":
            trackerDisplay = "Jawbone Up"
            break
            
        case "garmin_connect":
            trackerDisplay = "Garmin"
            break
            
        case "healthgraph":
            trackerDisplay = "Runkeeper"
            break
            
        case "pivotal_living":
            trackerDisplay = "Pivotal Living"
            break
            
        case "nikeplus":
            trackerDisplay = "Nike+"
            break
            
        case "misfit":
            trackerDisplay = "Misfit"
            break
            
        case "striiv":
            trackerDisplay = "Striiv"
            break
            
        case "kpb":
            trackerDisplay = "Kid Power Band"
            break
            
        case "googlefit":
            trackerDisplay = "Google Fit"
            break
            
        case "healthkit":
            trackerDisplay = "Health Kit"
            break
            
        default:
            trackerDisplay = shortname
            break
            
        }
        
        return trackerDisplay;
        
    }
    
    static func connectedDevicesToString(connectedDevices: [String]!) -> String {
        
        var output = ""
        let count = connectedDevices.count
        
        if count == 1 {
            return trackerDisplayName(connectedDevices[0])
        }
        else if count == 2 {
            return trackerDisplayName(connectedDevices[0]) + " and " + trackerDisplayName(connectedDevices[1])
        }
        else if count >= 3 {
            
            for x in 0 ..< count {
                
                // Last element: include 'and' in output
                if (x == count - 1) {
                    output += "and \(trackerDisplayName(connectedDevices[x])) "
                }
                else {
                    output += "\(trackerDisplayName(connectedDevices[x])), "
                }
            }
            
        }
        
        return output
    }
    
    #if KPW
    static var aboutUsContent:String = "<html><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0\"><head><style type=\"text/css\">@font-face{font-family: 'SourceSansPro-It'; src: url('SourceSansPro-It.otf');}@font-face{font-family: 'SourceSansPro-Bold'; src: url('SourceSansPro-Bold.otf');}strong{font-family: 'SourceSansPro-Bold';}p{font-family:'SourceSansPro-It';}body{padding:5px;color: #666; line-height: 1.7em;}a{font-family: 'SourceSansPro-It'; color: #00a4e4; text-decoration:none;}</style></head><body> <div> <p>This application is connected to the Health App. Your activity data will be updated in the background every hour or when you open the app. This utilizes the built-in technology in your mobile device without consuming additional battery.</p><p>The UNICEF Kid Power Workplace is a program by UNICEF USA which helps save and protect the world's most vulnerable children.</p><p>Web: <a href=\"http://workplace.unicefkidpower.org\" target=\"_blank\">workplace.unicefkidpower.org</a></p><p>Email: <a href=\"mailto:workplace@unicefkidpower.org\">workplace@unicefkidpower.org</a></p><p>Phone: <a href=\"tel:3033091423\">303-309-1423</a></p><p><strong>Are you in a challenge and need help?</strong> Visit our <a target=\"_blank\" href=\"http://unicefkidpowerworkplace.zendesk.com/hc/en-us\" target=\"_blank\">Help Center</a> or email <a href=\"mailto:workplace@unicefkidpower.org\">workplace@unicefkidpower.org</a>.</p><p><strong>Interested in how RUTF (Ready to Use Therapeutic Food) has revolutionized the treatment of severe acute malnutrition among children?</strong> Read this <a target=\"_blank\" href=\"http://www.unicef.org/media/files/Position_Paper_Ready-to-use_therapeutic_food_for_children_with_severe_acute_malnutrition__June_2013.pdf\" target=\"_blank\">position paper from UNICEF</a>.</p><p><a target=\"_blank\" href=\"http://www.caloriecloud.org/privacy-app/\">Privacy Policy</a></p></div></body></html>"
    #else
    static var aboutUsContent:String = "<html><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0\"><head><style type=\"text/css\">@font-face{font-family: 'SourceSansPro-It'; src: url('SourceSansPro-It.otf');}@font-face{font-family: 'SourceSansPro-Bold'; src: url('SourceSansPro-Bold.otf');}strong{font-family: 'SourceSansPro-Bold';}p{font-family:'SourceSansPro-It';}body{padding:5px;color: #666; line-height: 1.7em;}a{font-family: 'SourceSansPro-It'; color: #00a4e4; text-decoration:none;}</style></head><body> <div> <p>This application is connected to the Health App. Your activity data will be updated in the background every hour or when you open the app. This utilizes the built-in technology in your mobile device without consuming additional battery.</p><p>Calorie Cloud provides solutions that connect getting active with helping malnourished children and is registered in the US as a 501c3 non-profit corporation.</p><p>Web: <a href=\"http://www.caloriecloud.org\" target=\"_blank\">caloriecloud.org</a></p><p>Email: <a href=\"mailto:hello@caloriecloud.org\">hello@caloriecloud.org</a></p><p>Phone: <a href=\"tel:3033091423\">303-309-1423</a></p><p><strong>Are you in a challenge and need help?</strong> Visit our <a target=\"_blank\" href=\"http://support.caloriecloud.org/hc/en-us\" target=\"_blank\">Help Center</a> or email <a href=\"mailto:support@caloriecloud.org\">support@caloriecloud.org</a>.</p><p><strong>Interested in how RUTF (Ready to Use Therapeutic Food) has revolutionized the treatment of severe acute malnutrition among children?</strong> Read this <a target=\"_blank\" href=\"http://www.unicef.org/media/files/Position_Paper_Ready-to-use_therapeutic_food_for_children_with_severe_acute_malnutrition__June_2013.pdf\" target=\"_blank\">position paper from UNICEF</a>.</p><p><a target=\"_blank\" href=\"http://www.caloriecloud.org/privacy-app/\">Privacy Policy</a></p></div></body></html>"
    #endif
    
    
    
}
