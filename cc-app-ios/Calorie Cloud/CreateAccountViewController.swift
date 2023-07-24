//
//  CreateAccountViewController.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

class CreateAccountViewController: UIViewController, UITextFieldDelegate, LoginDelegate {
    
    let healthManager = HealthKitManager()
    
    @IBOutlet var userNameContainer: UIView!
    @IBOutlet var emailContainer: UIView!
    @IBOutlet var passwordContainer: UIView!
    
    @IBOutlet var usernameTextField: UITextField!
    @IBOutlet var emailTextField: UITextField!
    @IBOutlet var passwordTextField: UITextField!
    
    /* Views */
    var modalScreen: UIView!
    var activityIndicator: UIActivityIndicatorView!
    var viewActivityIndicator: UIView!
    
    @IBOutlet var stepper: UIImageView!
    @IBAction func createAccountButtonReleased(_ button: UIButton) {        
        if (isFormValid()) {
            
            showLoadingDialog()
            
            // Show loading modal view
            showLoadingDialog()
            
            // Close Keyboard
            self.view.endEditing(true)
            
            APIManager.createUser(email: emailTextField.text!, password: passwordTextField.text!, username: usernameTextField.text!, completionHandler: {(response, error) in
                
                if (error == nil) {
                    
                    APIManager.loginUser(email: self.emailTextField.text!, password: self.passwordTextField.text!, completionHandler: {(response, error) in
                        
                        if (error == nil) {
                            
                            CCStatics.setSavedUser(User(json: response))
                            
                            let endDate = CCStatics.formatDateString(dateString: self.mCurrentChallenge.endDate, toFormat: "yyyy-MM-dd", utcTimeZone: true)
                            let daysLeft = CCStatics.daysBetweenDates(date1: Date(), date2: CCStatics.stringToDate(dateString: self.mCurrentChallenge.startDate))
                            
                            var contentType: Int?
                            
                            if (daysLeft < 0) {
                                contentType = 1;
                            }
                            else {
                                contentType = 2;
                            }
                            
                            let userId = response["_id"].intValue
                            let email = response["email"].stringValue
                            let screenName = response["screenName"].stringValue
                            let accessToken = response["access_token"].stringValue
                            let brand = self.mCurrentChallenge.brand
                            
                            APIManager.createCustomerIOAccount(userId: userId, email: email, screenName: screenName, endDate: endDate, daysLeft: daysLeft, brand: brand!, contentType: contentType!, completionHandler: { (response) in
                                
                                if (response.result.isSuccess) {
                                    
                                    if (self.mCurrentChallenge.teamSelection == CCStatics.ChallengeSingleTeam) {
                                        
                                        APIManager.joinTeam(challengeId: self.mCurrentChallenge._id, teamId: self.mCurrentChallenge.defaultTeamId, userId: userId, accessToken: accessToken, completionHandler: { (response, error) in
                                            
                                            self.hideLoadingDialog();
                                            
                                            if (error == nil) {
                                                CCStatics.setNewAuth(true)
                                                
                                                if (CCStatics.wasHealthKitEnabled()) {
                                                    self.showAuthorizationAlert()
                                                }
                                                else {
                                                    self.proceedToNextPage()
                                                }
                                            
                                            }
                                            
                                            else {
                                                self.showAlert(title: "Error", message: error!, defaultActionTitle: "Okay")
                                            }

                                        })
                                        
                                    }
                                    else {
                                        self.hideLoadingDialog();
                                        CCStatics.setNewAuth(true)
                                        self.proceedToNextPage()
                                    }
                                    
                                }
                                else {
                                    self.hideLoadingDialog();
                                    self.showAlert(title: "Error", message: error!, defaultActionTitle: "Okay")
                                }
                            })
                            
                            
                        }
                        else {
                            self.hideLoadingDialog()
                            self.showAlert(title: "Error", message: error!, defaultActionTitle: "Okay")
                        }
                    
                    })
                    
                }
                else {
                    self.hideLoadingDialog()
                    self.showAlert(title: "Error", message: error!, defaultActionTitle: "Okay")
                    
                }
                
                }
            )
            
        }
        
    }
    
    func showAlert(title: String, message: String, defaultActionTitle: String) {
        let alertController = UIAlertController(title: title, message: message, preferredStyle: .alert)
        alertController.addAction(UIAlertAction(title: defaultActionTitle, style: .default, handler: nil))
        
        self.present(alertController, animated: true, completion: nil)
    }
    
    
    @IBAction func createAccountButtonPressed(_ button: UIButton) {
        button.backgroundColor = CCStatics.buttonPressedColor
    }
    
    @IBAction func existingAccountButtonReleased(_ button: UIButton) {
        performSegue(withIdentifier: "showExistingAccountViewController", sender: nil)
    }

    
    var mCurrentChallenge: Challenge!
    
    private var pageViewController: RegisterPageViewController!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        pageViewController = self.parent as! RegisterPageViewController
        
        if (pageViewController.getTotalPageCount() <= 3 && (pageViewController.registerPages.last?.isKind(of: OnboardingAuthorizationViewController.self))!) {
            stepper.image = UIImage.init(named: "step1s2")?.af_imageScaled(to: CGSize.init(width: 80, height: 22))
            stepper.contentMode = .center
        }
        else if (pageViewController.getTotalPageCount() < 3) {
            stepper.image = UIImage.init(named: "step1s2")?.af_imageScaled(to: CGSize.init(width: 80, height: 22))
            stepper.contentMode = .center
        }
    
        let rightBarNavItem = pageViewController.navigationItem.rightBarButtonItem
        if (rightBarNavItem != nil) {
            rightBarNavItem!.image = UIImage.init()
            rightBarNavItem!.setBackgroundImage(UIImage.init(cgImage: #imageLiteral(resourceName: "cc-icon-alternate").cgImage!, scale: 5, orientation: #imageLiteral(resourceName: "cc-icon-alternate").imageOrientation), for: .normal, barMetrics: .default)
        }
        
        mCurrentChallenge = pageViewController.mCurrentChallenge
        
        pageViewController.title = "CREATE ACCOUNT"
    
        
        userNameContainer.layer.cornerRadius = 3
        emailContainer.layer.cornerRadius = 3
        passwordContainer.layer.cornerRadius = 3
        
        setupLoadingDialog()
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

    
    
    /**
     * Function used to validate form
     * Checks several error conditions
     **/
    func isFormValid() -> Bool {
        var validForm = true;
        
        // Is email text field empty
        if (emailTextField.text?.isEmpty == true) {
            validForm = false;
        }
        
        if (usernameTextField.text?.isEmpty == true) {
            validForm = false;
        }
        
        // Is password text field empty
        if (passwordTextField.text?.isEmpty == true) {
            validForm = false;
        }
        
        // Is email a valid e-mail address
        if (isValidEmail(emailTextField.text!) == false) {
            validForm = false;
        }
        
        return validForm;
    }
    
    /**
     * Function used to validate a string input
     * Returns true if input is a valid email address
     **/
    func isValidEmail(_ testStr:String) -> Bool {
        
        let emailRegEx = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
        let emailTest = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        let result = emailTest.evaluate(with: testStr)
        
        return result
        
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
    
    /*********************************/
    /* Text Field Callback Functions */
    /*********************************/
    
    // When tapping anywhere outside keyboard, close keyboard
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        self.view.endEditing(true)
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        
        let nextTag = textField.tag + 1
        let nextResponder = self.view.viewWithTag(nextTag) as UIResponder!
        
        if (nextResponder != nil) {
            nextResponder?.becomeFirstResponder()
        }
        else {
            textField.resignFirstResponder()
        }
        
        return false
    }
    
    func onSuccessLogin() {
        
        let savedUser = User(serializedUser: CCStatics.getSavedUser()!)
        
        if (mCurrentChallenge.teamSelection == CCStatics.ChallengeSingleTeam) {
            
            APIManager.joinTeam(challengeId: self.mCurrentChallenge._id, teamId: self.mCurrentChallenge.defaultTeamId, userId: savedUser.userId, accessToken: savedUser.accessToken, completionHandler: { (response, error) in
                
                self.hideLoadingDialog();
                
                if (error == nil) {
                    
                    if (self.mCurrentChallenge.teamSelection == CCStatics.ChallengeSingleTeam) {
                        self.goToTrackerViewController()
                    }
                    else {
                        self.proceedToNextPage()
                    }
                    
                }
                else {
                    if (error == "Validation error"){
                        
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
        else {
            self.hideLoadingDialog()
            proceedToNextPage()
        }
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if (segue.identifier == "showExistingAccountViewController") {
        
            let existingAccountViewController = segue.destination as! ExistingAccountViewController
            existingAccountViewController.delegate = self
        
        }
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
