//
//  DefaultTrackerAuthorizationViewController.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import DeviceKit

class DefaultTrackerAuthorizationViewController: UIViewController {
    
    /* Button Pressed RELEASE */
    @IBAction func authorizeButtonPressed(_ button: UIButton) {
        self.getHealthKitPermission()
    }
    
    let healthManager = HealthKitManager()
    let savedUser = User(serializedUser: CCStatics.getSavedUser()!)
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    /**
     * Function to prompt user to authorize HealthKit integration with our application.
     * Only brings up the screen if the application hasn't received authorization. Otherwise, this is ignored.
     **/
    func getHealthKitPermission() {
        healthManager.authorizeHealthKit { (success, error) in
            if ((error) != nil) {
                // Display error message here
                self.showHKAlert(title: "Error", message: "The Health app is not installed on this device. In order to use this application, it must be installed!", defaultActionTitle: "Okay")
                
            }
            else {
                self.healthManager.enableBackgroundDelivery()
            
                APIManager.setDefaultSource(userId: self.savedUser.userId, accessToken: self.savedUser.accessToken, completionHandler: { (responseJSON, error) in
                    
                    if (error == nil) {
                        
                        APIManager.setDefaultDevice(userId: self.savedUser.userId, accessToken: self.savedUser.accessToken, completionHandler: { (responseJSON, error) in
                            
                            
                            if (error == nil) {
                                
                                CCStatics.setHealthKitEnabled(true)
                                
                                let parent = self.presentingViewController
                                
                                self.dismiss(animated: true, completion: {
                                    parent!.performSegue(withIdentifier: "showPreDashboardViewController", sender: self)
                                })
                                
                            }
                            
                        })
                        
                    }
                    
                })
            }
        }
    }
    
    func showHKAlert(title: String, message: String, defaultActionTitle: String) {
        let alertController = UIAlertController(title: title, message: message, preferredStyle: .alert)
        alertController.addAction(UIAlertAction(title: defaultActionTitle, style: .default, handler: {(action) in
            CCStatics.logoutUser()
            self.dismiss(animated: true, completion: nil)
        }))
        
        self.present(alertController, animated: true, completion: nil)
    }

    
}
