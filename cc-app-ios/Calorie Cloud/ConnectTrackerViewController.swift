//
//  ConnectTrackerViewController.swift
//  Calorie Cloud
//
//  Copyright © 2017 Calorie Cloud. All rights reserved.
//

import React

class ConnectTrackerViewController: UIViewController {
    
    @IBOutlet var containerView: UIView!
    @IBOutlet var stepper: UIImageView!
    
    //let savedUser = User(serializedUser: CCStatics.getSavedUser()!)
    let healthManager = HealthKitManager()
    var rootView: RCTRootView!
    var pageViewController: RegisterPageViewController!
    private var mCurrentChallenge: Challenge!
    var props : [String: Any] = ["phoneTrackerEnabled":CCStatics.wasHealthKitEnabled(), "fromOnboarding": true]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        pageViewController = self.parent as! RegisterPageViewController
        mCurrentChallenge = pageViewController.mCurrentChallenge
        
        if (pageViewController.getTotalPageCount() <= 3 && (pageViewController.registerPages.last?.isKind(of: OnboardingAuthorizationViewController.self))!) {
            stepper.image = UIImage.init(named: "step12s")?.af_imageScaled(to: CGSize.init(width: 80, height: 22))
            stepper.contentMode = .center
        }
        else if (pageViewController.getTotalPageCount() < 3) {
            stepper.image = UIImage.init(named: "step12s")?.af_imageScaled(to: CGSize.init(width: 80, height: 22))
            stepper.contentMode = .center
        }
        
        pageViewController.title = "CONNECT TRACKER"
        
        rootView = SwiftRCTBridge.sharedInstance.viewForModule("Tracker", initialProperties: props)
        rootView.frame = CGRect(x: self.containerView.frame.minX, y: self.containerView.frame.minY, width: self.view.frame.width, height: self.containerView.frame.height)
        
        self.view.addSubview(rootView)
    }
    
    
    func showAlert(title: String, message: String) {
        let alertController = UIAlertController(title: title, message: message, preferredStyle: .alert)
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
                                
                                self.showAlert(title: "Success", message: "This device is now linked to your account as your current activity tracker")
                                
                                self.rootView.appProperties.updateValue(true, forKey: "phoneTrackerEnabled")
                                
                            }
                            
                        })
                        
                    }
                    
                })
            }
        }
    }
    

    
}
