//
//  DashboardViewController.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import React
import SwiftyJSON

class MyStatsViewController: CCParentViewController {
    
    var props = ["baseURL": CCStatics.BaseURL, "User": CCStatics.getSavedUser()!, "hkEnabled": CCStatics.wasHealthKitEnabled()] as [String : Any]
    
    let savedUser = User(serializedUser: CCStatics.getSavedUser()!)
    let healthManager = HealthKitManager()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let rootView = SwiftRCTBridge.sharedInstance.viewForModule("Dashboard", initialProperties: self.props)
        rootView.frame = self.view.bounds
        
        self.view.addSubview(rootView)
        
        if (CCStatics.wasHealthKitEnabled()) {
            getActivityData();
        }
    
    }
    
    func getActivityData() {
        
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        
        APIManager.getLastSyncDate(userId: self.savedUser.userId, accessToken: self.savedUser.accessToken) { (responseJSON, error) in
            
            let lastSyncDate = responseJSON["lsd"].stringValue
            
            self.healthManager.getActivityDataSince(lastSyncDate, completion: { (stepResults, calResults, error) in
                
                if (error == nil) {
                    
                    if (JSON(appDelegate.createResultsDictionary(calorieResults: calResults!, stepResults: stepResults!))["activity"].count > 0) {
                        
                        APIManager.postActivityData(userId: self.savedUser.userId, accessToken: self.savedUser.accessToken, activityData: (appDelegate.createResultsDictionary(calorieResults: calResults!, stepResults: stepResults!) as? [String: AnyObject])!, completionHandler: { (responseJSON, error) in
                            
                        })
                    }
                }
            })
        }
    }
    
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if (segue.identifier == "showChallengeInfoViewController") {
            
            (segue.destination as! ChallengeInfoViewController).mCurrentChallenge = sender as! [AnyHashable : Any]
            
        }
    }
}


