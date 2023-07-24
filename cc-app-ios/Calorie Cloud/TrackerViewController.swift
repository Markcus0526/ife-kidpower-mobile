//
//  SettingsViewController.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import SwiftyJSON

class TrackerViewController: CCParentViewController {
    
    /* Outlets */
    @IBOutlet var defaultDeviceLabel: UILabel!
    @IBOutlet var lastSyncDateLabel: UILabel!
    @IBOutlet var connectedLabel: UILabel!
    @IBOutlet var connectedIcon: UIImageView!
    
    @IBOutlet var trackerContainer: UIView!
    @IBOutlet var authorizeButton: CCBorderButton!
    @IBAction func authorizeButonPressed(_ sender: CCBorderButton) {
        getHealthKitPermission()
    }
    
    
    let savedUser = User(serializedUser: CCStatics.getSavedUser()!)
    let healthManager = HealthKitManager()
    
    @IBAction func manageTrackersPressed(_ button: UIButton) {
        let trackersURL = CCStatics.BaseURL + "/#/user-trackers/accessToken/" + savedUser.accessToken
        UIApplication.shared.openURL(URL(string: trackersURL)!)
        
    }
    
    @IBAction func visitDashboardPressed(_ button: UIButton) {
        
        let dashboardURL = CCStatics.BaseURL + "/#/user-dashboard/accessToken/" + savedUser.accessToken
        UIApplication.shared.openURL(URL(string: dashboardURL)!)
        
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        NotificationCenter.default.addObserver(self, selector: #selector(TrackerViewController.didEnterForeground), name: NSNotification.Name.UIApplicationWillEnterForeground, object: nil)
        adjustUI()
    }
    
    func didEnterForeground() {
        
        if (CCStatics.wasHealthKitEnabled()) {
            getTrackerInfo()
        }
        
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        NotificationCenter.default.removeObserver(self)
    }
    
    func adjustUI() {
        if (CCStatics.wasHealthKitEnabled()) {
            
            authorizeButton.removeFromSuperview()
            
            trackerContainer.constraints.first(where: { (constraint) -> Bool in constraint.identifier == "lastSyncDateLabelContraint"})?.isActive = true
            
            let trackerBottomLogoContraint = self.view.constraints.first(where: { (constraint) -> Bool in
                constraint.identifier == "trackerBottomIcon"
            })
            
            trackerBottomLogoContraint?.constant = 75
            
            getActivityData()
        }
        else {
            
            lastSyncDateLabel.text = "You still need to authorize your phone as an activity tracker"
            
            authorizeButton.isEnabled = true
            authorizeButton.isUserInteractionEnabled = true
            authorizeButton.alpha = 1
            
            trackerContainer.constraints.first(where: { (constraint) -> Bool in constraint.identifier == "lastSyncDateLabelContraint"})?.isActive = false
            
            let trackerBottomLogoContraint = self.view.constraints.first(where: { (constraint) -> Bool in
                constraint.identifier == "trackerBottomIcon"
            })
            
            trackerBottomLogoContraint?.constant = 35
        }
    }
    
    
    func showAlert(title: String, message: String, defaultActionTitle: String) {
        let alertController = UIAlertController(title: title, message: message, preferredStyle: .alert)
        alertController.addAction(UIAlertAction(title: defaultActionTitle, style: .default, handler: {(action) in
            self.getTrackerInfo()
        }))
        
        self.present(alertController, animated: true, completion: nil)
    }
    
        
    func getTrackerInfo() {
        
        APIManager.getLastSyncDateDevice(userId: savedUser.userId, accessToken: savedUser.accessToken) { (responseJSON, error) in
            
            if (error == nil) {
                
                let lastSyncDate = responseJSON["updatedAt"].stringValue
                let source = responseJSON["source"].stringValue
                
                if (lastSyncDate != "") {
                    let lastSynced = NSMutableAttributedString(string: "Last Synced: \n")
                    let dateString = NSAttributedString(string: CCStatics.formatDateString(dateString: lastSyncDate, toFormat: "yyyy-MM-dd hh:mm a", utcTimeZone: false), attributes: [NSFontAttributeName: UIFont(name: "SourceSansPro-It", size: 14.0)!])
                    lastSynced.append(dateString)
                    self.lastSyncDateLabel.attributedText = lastSynced
                }
                else {
                    self.lastSyncDateLabel.text = "Not synced yet"
                }
                
                if (source != "") {
                    self.connectedLabel.alpha = 1
                    self.connectedIcon.alpha = 1
                    
                    self.defaultDeviceLabel.text = CCStatics.trackerDisplayName(source)
                    
                }
                else {
                    self.defaultDeviceLabel.text = "No connected devices"
                }

                
            }
        }
    }
    
    
    func getActivityData() {
        
        lastSyncDateLabel.attributedText = NSAttributedString(string: "Syncing data. We're preparing your\nstats but you can still get active!", attributes: [NSFontAttributeName: UIFont(name: "SourceSansPro-It", size: 14.0)!])

        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        
        APIManager.getLastSyncDate(userId: self.savedUser.userId, accessToken: self.savedUser.accessToken) { (responseJSON, error) in
            
            let lastSyncDate = responseJSON["lsd"].stringValue
            
            self.healthManager.getActivityDataSince(lastSyncDate, completion: { (stepResults, calResults, error) in
                
                if (error == nil) {
                    
                    if (JSON(appDelegate.createResultsDictionary(calorieResults: calResults!, stepResults: stepResults!))["activity"].count > 0) {
                        
                        
                        APIManager.postActivityData(userId: self.savedUser.userId, accessToken: self.savedUser.accessToken, activityData: (appDelegate.createResultsDictionary(calorieResults: calResults!, stepResults: stepResults!) as? [String: AnyObject])!, completionHandler: { (responseJSON, error) in
                            
                            if (error == nil) {
                               self.getTrackerInfo()
                            }
                        })
                    }
                    else {
                        self.getTrackerInfo()
                    }
                }
                else {
                    self.getTrackerInfo()
                }
            })
            
        }

    }
    
    func showHKAlert(title: String, message: String, defaultActionTitle: String) {
        let alertController = UIAlertController(title: title, message: message, preferredStyle: .alert)
        alertController.addAction(UIAlertAction(title: defaultActionTitle, style: .default, handler: nil))
        
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
                self.showHKAlert(title: "Error", message: "The Health app is not installed on this device. In order to use this application, This application requires Health Kit connectivity!", defaultActionTitle: "Okay")
            }
            else {
                // Set the source as 'healthkit'
                APIManager.setDefaultSource(userId: savedUser.userId, accessToken: savedUser.accessToken, completionHandler: { (responseJSON, error) in
                    
                    if (error == nil) {
                        
                        APIManager.setDefaultDevice(userId: savedUser.userId, accessToken: savedUser.accessToken, completionHandler: { (responseJSON, error) in
                            
                            if (error == nil) {
                                
                                CCStatics.setHealthKitEnabled(true)
                                self.healthManager.enableBackgroundDelivery()
                                
                                let alertController = UIAlertController(title: "Success", message: "This device is now linked to your account as your current activity tracker", preferredStyle: .alert)
                                
                                alertController.addAction(UIAlertAction(title: "Close", style: .default, handler: { (action) in
                                    
                                    self.adjustUI()
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
