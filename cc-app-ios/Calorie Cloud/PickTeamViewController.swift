//
//  PickTeamViewController.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import AlamofireImage

class PickTeamViewController: UIViewController, UIPickerViewDataSource, UIPickerViewDelegate, UITextFieldDelegate {
    
    var healthManager = HealthKitManager()
    private var mCurrentChallenge: Challenge!
    private var pageViewController: RegisterPageViewController!
    private var mRegions: Array<Region>?
    private var mTeams: Array<Team>?

    @IBOutlet var challengeImage: UIImageView!
    @IBOutlet var regionContainer: UIView!
    @IBOutlet var teamContainer: UIView!
    @IBOutlet var regionField: CCTextField!
    @IBOutlet var teamField: CCTextField!
    @IBOutlet var stepper: UIImageView!
    
    var teamPickerView: UIPickerView?
    var regionPickerView: UIPickerView?
    
    /* Views */
    var modalScreen: UIView!
    var activityIndicator: UIActivityIndicatorView!
    var viewActivityIndicator: UIView!
    
    @IBAction func regionFieldEditStart(_ textField: UITextField) {
    
        if (textField.text?.isEmpty)! {
            regionPickerView?.delegate?.pickerView!(regionPickerView!, didSelectRow: 0, inComponent: 0)
        }
        
    }
    @IBAction func teamFieldEditStart(_ textField: UITextField) {
    
        if (textField.text?.isEmpty)! {
            teamPickerView?.delegate?.pickerView!(teamPickerView!, didSelectRow: 0, inComponent: 0)
        }
        
    }
    @IBAction func joinChallengeButtonReleased(_ button: UIButton) {
        var isFormValid = false
        
        switch (mCurrentChallenge.teamSelection) {
            case CCStatics.ChallengeMultipleTeams:
                
                if ((teamField.text?.isEmpty)! == false) {
                    isFormValid = true
                }
                else {
                    showAlert(title: "Error", message: "Please select a team to join the challenge", defaultActionTitle: "Okay")
                }
                
                break;
            
            case CCStatics.ChallengeRegionsTeams:
                
                if ((regionField.text?.isEmpty)! == false && (teamField.text?.isEmpty)! == false) {
                    isFormValid = true
                }
                else {
                    showAlert(title: "Error", message: "Please select a region and a team to join the challenge", defaultActionTitle: "Okay")
                }
                
                break;
            
            default: break;
        }
        
        if (isFormValid) {
            
            showLoadingDialog()
            
            let selectedTeam = (mCurrentChallenge.teamSelection == CCStatics.ChallengeRegionsTeams) ? mTeams?[(teamPickerView?.selectedRow(inComponent: 0))!]._id: mCurrentChallenge.teams[(teamPickerView?.selectedRow(inComponent: 0))!]._id
            
            let savedUser = User(serializedUser: CCStatics.getSavedUser()!)
            
            APIManager.joinTeam(challengeId: mCurrentChallenge._id, teamId: selectedTeam!, userId: savedUser.userId, accessToken: savedUser.accessToken, completionHandler: { (response, error) in
                
                self.hideLoadingDialog()
                
                if (error == nil) {
                
                    if (CCStatics.wasHealthKitEnabled()) {
                        self.showAuthorizationAlert()
                    }
                    else {
                        self.proceedToNextPage()
                    }
                
                }
                else {
                    if (error == "Validation error") {
                        
                        let alertController = UIAlertController(title: "Error", message: "You are already part of this challenge", preferredStyle: .alert)
                        alertController.addAction(UIAlertAction(title: "Okay", style: .default, handler: {(action) in
                            self.goToTrackerViewController()
                        }))
                        
                        self.present(alertController, animated: true, completion: nil)
                        
                    }
                    else {
                        self.showAlert(title: "Error", message: error!, defaultActionTitle: "Close")
                        
                    }
                }
            })
        }
    }
    
    @IBAction func regionValueChanged(_ textfield: UITextField) {
        
        let selectedRegion = mRegions?[(regionPickerView?.selectedRow(inComponent: 0))!]

        mTeams = selectedRegion?.teams
        
        if (mTeams?.count != 0) {
            teamField.isEnabled = true
            
            teamPickerView?.tag = 3
            teamPickerView?.delegate = self
            teamField.inputView = teamPickerView
            
            teamPickerView?.delegate?.pickerView!(teamPickerView!, didSelectRow: 0, inComponent: 0)
        }
        else {
            teamField.isEnabled = false
            teamField.text = nil
        }
    
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        pageViewController = self.parent as! RegisterPageViewController
        mCurrentChallenge = pageViewController.mCurrentChallenge
        
        pageViewController.title = "CHOOSE TEAM"
        
        regionContainer.layer.cornerRadius = 3
        teamContainer.layer.cornerRadius = 3
        
        adjustChallengeUI()
        
        setupLoadingDialog()
    
    }
    
    func adjustChallengeUI() {
        
        if (!mCurrentChallenge.imageSrc.isEmpty) {
            challengeImage.af_setImage(withURL: URL(string: mCurrentChallenge.imageSrc)!)
        }
        else {
            challengeImage.af_setImage(withURL: URL(string: mCurrentChallenge.organization.imageSrc)!)
        }
        
        teamPickerView = UIPickerView()
        
        if (mCurrentChallenge.teamSelection == CCStatics.ChallengeMultipleTeams) {
            
            regionContainer.removeFromSuperview()
        
            let topConstraint = teamContainer.superview?.constraints.first(where: { (constraint) -> Bool in
                constraint.identifier == "teamContainerTopSpace"
            })
            
            topConstraint?.constant = 8
            
            teamPickerView?.tag = 1
            teamPickerView?.delegate = self
            teamField.inputView = teamPickerView
            
        
        }
        else {
            
            teamField.isEnabled = false
            
            APIManager.getRegionsByChallengeId(challengeId: mCurrentChallenge._id, completionHandler: { (response, error) in
                
                if (error == nil) {
                    self.mRegions = Region.toArray(jsonArray: response.arrayValue)
                }
            })
            
            regionPickerView = UIPickerView()
            regionPickerView?.tag = 2
            regionPickerView?.delegate = self
            regionField.inputView = regionPickerView
        }
        
    }
    
    func showAlert(title: String, message: String, defaultActionTitle: String) {
        let alertController = UIAlertController(title: title, message: message, preferredStyle: .alert)
        alertController.addAction(UIAlertAction(title: defaultActionTitle, style: .default, handler: nil))
        
        self.present(alertController, animated: true, completion: nil)
    }
    
    func showAuthorizationAlert() {
        let alertController = UIAlertController(title: "Success", message: "You have joined the challenge. The last step is to authorize your phone as an activity tracker. Don't worry, you can switch trackers later.", preferredStyle: .alert)
        
        alertController.addAction(UIAlertAction(title: "Authorize", style: .default, handler: { (action) in
            self.getHealthKitPermission()
        }))
        
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
                                
                                let alertController = UIAlertController(title: "Success", message: "This device is now linked to your account as your current activity tracker", preferredStyle: .alert)
                                
                                alertController.addAction(UIAlertAction(title: "Close", style: .default, handler: { (action) in
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
    
    
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        
        var returnCount: Int?
        
        switch (pickerView.tag) {
            case 1:
                returnCount = mCurrentChallenge.teams.count
        
            case 2:
                returnCount = mRegions!.count
                break;
            
            case 3:
                returnCount = mTeams!.count
            
            default:
                returnCount = 0
                break;
        }
        
        return returnCount!
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        
        var returnString: String?
        
        switch (pickerView.tag) {
            
            case 1:
                returnString = mCurrentChallenge.teams[row].name
                break;
            
            case 2:
                returnString = mRegions![row].name
                break;
            
            case 3:
                returnString = mTeams?[row].name
            
            default:
                returnString = nil
                break;
            
        }
        
        return returnString
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        switch (pickerView.tag) {
            case 1:
                teamField.text = mCurrentChallenge.teams[row].name
                break;
            case 2:
                regionField.text = mRegions![row].name
                break;
            case 3:
                teamField.text = mTeams?[row].name
            default:
                break;
        }
        
    }
    
    
    // When tapping anywhere outside keyboard, close keyboard
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        self.view.endEditing(true)
    }
    
    func setupLoadingDialog() {
        
        let width: CGFloat = 200.0
        let height: CGFloat = 50.0
        let x = self.view.frame.width/2.0 - width/2.0
        let y = self.view.frame.height/2.0 - height/2.0
        
        modalScreen = UIView(frame: CGRect(x: 0, y: 0, width: self.view.frame.width, height: self.view.frame.height))
        modalScreen.backgroundColor = UIColor.black
        modalScreen.alpha = 0.50
        
        viewActivityIndicator = UIView(frame: CGRect(x: x, y: y, width: width, height: height))
        viewActivityIndicator.backgroundColor = UIColor(red: 255.0/255.0, green: 255.0/255.0, blue: 255.0/255.0, alpha: 1)
        viewActivityIndicator.layer.cornerRadius = 10
        
        activityIndicator = UIActivityIndicatorView(frame: CGRect(x: 0, y: 0, width: 50, height: 50))
        activityIndicator.color = UIColor.black
        activityIndicator.hidesWhenStopped = false
        
        let titleLabel = UILabel(frame: CGRect(x: 60, y: 0, width: 200, height: 50))
        titleLabel.text = "Loading..."
        
        viewActivityIndicator.addSubview(activityIndicator)
        viewActivityIndicator.addSubview(titleLabel)
        activityIndicator.startAnimating()
        
    }
    
    func showLoadingDialog() {
        self.view.addSubview(modalScreen)
        self.view.addSubview(viewActivityIndicator)
    }
    
    func hideLoadingDialog() {
        self.modalScreen.removeFromSuperview()
        self.viewActivityIndicator.removeFromSuperview()
    }
    
    func proceedToNextPage() {
        self.pageViewController.setViewControllers([pageViewController.pageViewController(pageViewController, viewControllerAfter: self)!], direction: .forward, animated: true, completion: nil)
    }
    
    func goToTrackerViewController() {
        
        let parent = pageViewController.parent?.presentingViewController
        
        dismiss(animated: true, completion: {
            parent!.performSegue(withIdentifier: "showTrackerViewController", sender: self)
        })
    }

}
