//
//  OnboardingAuthorizationViewController.swift
//  Calorie Cloud
//
//  Copyright © 2017 Calorie Cloud. All rights reserved.
//

class OnboardingAuthorizationViewController: UIViewController {
    
    private var pageViewController: RegisterPageViewController!
    private var healthManager = HealthKitManager()
    
    @IBAction func authorizeButtonPressed(_ sender: CCButton) {
        getHealthKitPermission()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        pageViewController = self.parent as! RegisterPageViewController
        pageViewController.title = "CONNECT TRACKER"
    
    }
    
    func showAlert(title: String, message: String, defaultActionTitle: String) {
        let alertController = UIAlertController(title: title, message: message, preferredStyle: .alert)
        alertController.addAction(UIAlertAction(title: defaultActionTitle, style: .default, handler: nil))
        
        self.present(alertController, animated: true, completion: nil)
    }
    
    func goToTrackerViewController() {
        
        let parent = pageViewController.parent?.presentingViewController
        
        dismiss(animated: true, completion: {
            parent!.performSegue(withIdentifier: "showTrackerViewController", sender: self)
        })
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
                self.showAlert(title: "Error", message: "The Health app is not installed on this device. In order to use this application, it must be installed!", defaultActionTitle: "Okay")
            }
            else {
                // Set the source as 'healthkit'
                APIManager.setDefaultSource(userId: savedUser.userId, accessToken: savedUser.accessToken, completionHandler: { (responseJSON, error) in
                    
                    if (error == nil) {
                        
                        APIManager.setDefaultDevice(userId: savedUser.userId, accessToken: savedUser.accessToken, completionHandler: { (responseJSON, error) in
                            
                            if (error == nil) {
                                
                                CCStatics.setHealthKitEnabled(true)
                                CCStatics.setNewAuth(false)
                                self.healthManager.enableBackgroundDelivery()
                                
                                let alertController = UIAlertController(title: "Success", message: "You are in the challenge! The first sync may take a little while. In the meantime, why not enjoy getting active?", preferredStyle: .alert)
                                
                                alertController.addAction(UIAlertAction(title: "Okay", style: .default, handler: { (action) in
                                    self.goToTrackerViewController()
                                }))
                                
                                self.present(alertController, animated: true, completion: nil)
                                
                            }
                        })
                    }
                })
            }
        }
    }
    
}
