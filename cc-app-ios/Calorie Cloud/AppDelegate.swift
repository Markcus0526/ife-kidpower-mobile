//
//  AppDelegate.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import ZendeskSDK
import Alamofire
import SwiftyJSON
import Fabric
import Crashlytics
import React
import CodePush

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var window: UIWindow?
    let healthManager = HealthKitManager()
    var RNBundleURL:URL!
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
        
        #if DEBUG
            RNBundleURL = URL(string: "http://localhost:8081/index.bundle?platform=ios")
        #else
            RNBundleURL = CodePush.bundleURL()
        #endif
        
        // Initialize appropriate Zendesk Help Center
        #if KPW
            ZDKConfig.instance().initialize(withAppId: "860ca53492da9c30497ec11ec44c8a46aa531477b231b36f",zendeskUrl: CCStatics.ZendeskURL, clientId: "mobile_sdk_client_de4242c53346a34b54f7");        ZDKConfig.instance().userIdentity = ZDKAnonymousIdentity()
        #else
            ZDKConfig.instance().initialize(withAppId: "33e09348084a9fc6a82b069c26013e657b3a719e784f40e9",zendeskUrl: CCStatics.ZendeskURL, clientId: "mobile_sdk_client_eb6153ee6354556bce48");           ZDKConfig.instance().userIdentity = ZDKAnonymousIdentity()
        #endif
        
        // Initialize Crashlytics only on Production Build
        #if STAGING
        #else
            Fabric.with([Crashlytics.self])
        #endif
        
        // Application logic to load appropriate screen
        self.window = UIWindow(frame: UIScreen.main.bounds)
        
        #if KPW
            let storyboard = UIStoryboard(name: "Main-KPW", bundle: nil)
        #else
            let storyboard = UIStoryboard(name: "Main", bundle: nil)
        #endif
        
        let initialViewController: UIViewController
        
        // Check if this is the first application launch
        if (CCStatics.isFirstLaunch() == nil) {
            initialViewController = storyboard.instantiateViewController(withIdentifier: "FirstLaunchViewController")
        }
        else {
            
            // If userId is already stored in preferences, load main screen
            if (CCStatics.getSavedUser() != nil) {
                initialViewController = storyboard.instantiateViewController(withIdentifier: "SWRevealViewController")
                
                if (healthManager.isHealthKitInstalled()) {
                    if (CCStatics.wasHealthKitEnabled()) {
                        healthManager.enableBackgroundDelivery()
                    }
                }
                
            }
            
            // otherwise, show login screen
            else {
                initialViewController = storyboard.instantiateViewController(withIdentifier: "LoginViewController")
            }
        }
        
        self.window?.rootViewController = initialViewController
        self.window?.makeKeyAndVisible()
        
        return true
    }

    func applicationWillResignActive(_ application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    }

    func applicationWillEnterForeground(_ application: UIApplication) {
        // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }

    func applicationWillTerminate(_ application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    }
    
    
    /**
     * Function used to create dictionary of data retrieved from HealthKit
     * Merges step and calorie data if both are present, otherwise sends calories data only
     **/
    func createResultsDictionary(calorieResults: Array<AnyObject>, stepResults: Array<AnyObject>) -> NSDictionary {
        
        let savedUser = User(serializedUser: CCStatics.getSavedUser())
        
        var results:NSDictionary = [:]
        
        if (stepResults.count == 0) {
            // Omit step count data
            results = ["userId": savedUser.userId, "activity": calorieResults]
        }
        else {
            // Merge step data and active calorie data
            var mergedResults = Array<AnyObject>()
            mergedResults.append(contentsOf: stepResults)
            
            if (calorieResults.count != 0) {
                mergedResults.append(contentsOf: calorieResults)
            }
            
            results = ["userId": savedUser.userId, "activity": mergedResults]
            
        }
        
        return results as NSDictionary
        
    }
    

    
    func getCalorieData() {
        
        if (CCStatics.getSavedUser() != nil) {
            
            let savedUser = User(serializedUser: CCStatics.getSavedUser())
            
            Alamofire.request(CCStatics.LastSyncDateURL+"\(savedUser.userId!)", headers: ["x-access-token": savedUser.accessToken]).responseJSON { (response) in
                
                do {
                    let responseJSON = try JSON(data: response.data!)
                    
                    
                    let lastSyncDate = responseJSON["lsd"].stringValue
                    
                    self.healthManager.getDailyActiveCaloriesSince(lastSyncDate, completion: { (calResults, error) in
                        
                        if (error == nil) {
                            
                            if (JSON(self.createResultsDictionary(calorieResults: calResults!, stepResults: Array()))["activity"].count > 0) {
                                Alamofire.request(CCStatics.ActivitySummariesURL, method: .post, parameters: self.createResultsDictionary(calorieResults: calResults!, stepResults: Array()) as? [String: AnyObject], encoding: JSONEncoding.default, headers: ["x-access-token":savedUser.accessToken]).responseString { (response) in
                                    print(response)
                                }
                            }
                        }
                    })
    
                }
            
                catch {
                    print("An error has occurred")
                }
            }
        }
    }
    
    func getStepData() {
        if (CCStatics.getSavedUser() != nil) {
            
            let savedUser = User(serializedUser: CCStatics.getSavedUser())
            
            Alamofire.request(CCStatics.LastSyncDateURL+"\(savedUser.userId!)", headers: ["x-access-token": savedUser.accessToken]).responseJSON { (response) in
                
                do {
                    let responseJSON = try JSON(data: response.data!)
                    let lastSyncDate = responseJSON["lsd"].stringValue
                    
                    self.healthManager.getDailyStepCountSince(lastSyncDate, completion: { (stepResults, error) in
                        
                        if (error == nil) {
                            
                            if (JSON(self.createResultsDictionary(calorieResults: Array(), stepResults: stepResults!))["activity"].count > 0) {
                                Alamofire.request(CCStatics.ActivitySummariesURL, method: .post, parameters: self.createResultsDictionary(calorieResults: Array(), stepResults: stepResults!) as? [String: AnyObject], encoding: JSONEncoding.default, headers: ["x-access-token":savedUser.accessToken]).responseString { (response) in
                                    print(response)
                                }
                            }
                        }
                    })
                }
                catch {
                    print("An error has occurred")
                }
            
            }
        }
    }

}

