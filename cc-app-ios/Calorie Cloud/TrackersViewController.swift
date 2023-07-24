//
//  TrackersViewController.swift
//  Calorie Cloud
//
//  Copyright © 2017 Calorie Cloud. All rights reserved.
//

import React

class TrackersViewController: CCParentViewController {
    
    let savedUser = User(serializedUser: CCStatics.getSavedUser()!)
    let healthManager = HealthKitManager()
    var rootView: RCTRootView!
    var props = ["lastSynced": "Last Sync: ...", "baseURL": CCStatics.BaseURL, "User": CCStatics.getSavedUser()!, "fromOnboarding": false, "phoneTrackerEnabled": CCStatics.wasHealthKitEnabled()] as [String : Any]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        rootView = SwiftRCTBridge.sharedInstance.viewForModule("Tracker", initialProperties: self.props)
        rootView.frame = self.view.bounds
        
        self.view.addSubview(rootView)
        
    }
    
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        NotificationCenter.default.removeObserver(self)
    }
    
    func showAlert(title: String, message: String) {
        let alertController = UIAlertController(title: title, message: message, preferredStyle: .alert)
        alertController.addAction(UIAlertAction.init(title: "OK", style: .default, handler: nil))
        self.present(alertController, animated: true, completion: nil)
    }
    
    
    /**
     * Function to prompt user to authorize HealthKit integration with our application.
     * Only brings up the screen if the application hasn't received authorization. Otherwise, this is ignored.
     **/
    func getHealthKitPermission() {
        let savedUser = User(serializedUser: CCStatics.getSavedUser()!)
        
        healthManager.authorizeHealthKit { (success, error) in
            if ((error) != nil) {
                // Display error message here
                self.showAlert(title: "Error", message: (error?.description)!)
            }
            else {
                // Set the source as 'healthkit'
                APIManager.setDefaultSource(userId: savedUser.userId, accessToken: savedUser.accessToken, completionHandler: { (responseJSON, error) in
                    
                    if (error == nil) {
                        
                        APIManager.setDefaultDevice(userId: savedUser.userId, accessToken: savedUser.accessToken, completionHandler: { (responseJSON, error) in
                            
                            if (error == nil) {
                                
                                CCStatics.setHealthKitEnabled(true)
                                self.healthManager.enableBackgroundDelivery()
                                
                                self.showAlert(title: "Success", message: "This device is now linked to your account")
                                
                                self.rootView.appProperties.updateValue(true, forKey: "phoneTrackerEnabled")
                                
                            }
                            
                        })
                        
                    }
                    
                })
            }
        }
    }
    
    
}
