//
//  LoginViewController.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import SwiftyJSON
import React
class LoginViewController: UIViewController, UITextFieldDelegate {
    
    /* Outlets */
    @IBOutlet var emailContainer: UIView!
    @IBOutlet var passwordContainer: UIView!
    @IBOutlet var emailField: UITextField!
    @IBOutlet var passwordField: UITextField!
    
    /* Views */
    var modalScreen: UIView!
    var activityIndicator: UIActivityIndicatorView!
    var viewActivityIndicator: UIView!
    
    var firstTimeJoin = false
    
    @IBAction func forgotPasswordButtonPressed(_ sender: UIButton) {
        showPasswordResetAlert()
    }
    
    @IBAction func joinWithEventCodeButtonReleased(_ button: UIButton) {
        showJoinWithEventCodeAlert()
    }

    /* Button Pressed RELEASE */
    @IBAction func loginButtonPressed(_ button: UIButton) {
        
        if (isLoginValid()) {
            
            // Show loading modal view
            showLoadingDialog()
            
            // Close Keyboard
            self.view.endEditing(true)
            
            APIManager.loginUser(email: emailField.text!, password: passwordField.text!, completionHandler: { (responseJSON, error) in
                
                if (error == nil) {
                    let accessToken = responseJSON["access_token"].stringValue
                    let userId = responseJSON["id"].intValue
                    let email = responseJSON["email"].stringValue
                    let screenName = responseJSON["screenName"].stringValue
                    
                    CCStatics.setSavedUser(User(userId: userId, email: email, screenName: screenName, accessToken: accessToken))
                    
                    self.hideLoadingDialog()
                    self.clearInput()
                    
                    if (CCStatics.wasHealthKitEnabled() == false) {
                        self.performSegue(withIdentifier: "showDefaultTrackerAuthorizationViewController", sender: nil)
                    }
                    else {
                        self.performSegue(withIdentifier: "showDashboardViewController", sender: nil)
                    }
                }
                else {
                    self.showAlert(title: "Unauthorized", message: "Please try again with a valid e-mail and password.", defaultActionTitle: "OK")
                    self.hideLoadingDialog()
                }
                
            })
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Add corner radiuses to the text field container views and login button
        emailContainer.layer.cornerRadius = 3
        passwordContainer.layer.cornerRadius = 3
        
        setupLoadingDialog()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        if (firstTimeJoin) {
            showJoinWithEventCodeAlert()
        }
    }
    
    func showReactJSScreen(challenge: JSON) {
        performSegue(withIdentifier: "showOnboardingViewController", sender: challenge)
    }
    
    func showAlert(title: String, message: String, defaultActionTitle: String) {
        let alertController = UIAlertController(title: title, message: message, preferredStyle: .alert)
        alertController.addAction(UIAlertAction(title: defaultActionTitle, style: .default, handler: nil))
        
        self.present(alertController, animated: true, completion: nil)
    }
    
    
    func showPasswordResetAlert() {
        var resetEmailTextField: UITextField?
        let alertController = UIAlertController(title: "Forgot Password", message: "We'll send a link to your email", preferredStyle: .alert)
        alertController.addAction(UIAlertAction(title: "Reset Password", style: .default) {(action:UIAlertAction!) in
            
            #if KPW
                let brand:String? = "UNICEF Kid Power Workplace"
            #else
                let brand:String? = nil
            #endif
                
            APIManager.sendPasswordReset(email: (resetEmailTextField?.text)!, brand: brand, completionHandler: {(response, error) in
                
                if (error != nil) {
                    
                    if (resetEmailTextField?.text?.isEmpty)! {
                        self.showAlert(title: "Error", message: "User's email is required!", defaultActionTitle: "Close")
                    }
                    else if (!self.isValidEmail((resetEmailTextField?.text)!)) {
                       self.showAlert(title: "Error", message: "We haven't found this email", defaultActionTitle: "Close")
                    }
                    else {
                       self.showAlert(title: "Error", message: error!, defaultActionTitle: "Close")
                    }
                    
                }
                else {
                    self.showAlert(title: "Success", message: response["message"].stringValue, defaultActionTitle: "Close")
                }
                
            })
            
        })
        alertController.addAction(UIAlertAction(title: "Cancel", style: .default, handler: nil))
        
        alertController.addTextField { (textField) in
            textField.placeholder = "Email Address"
            textField.text = self.emailField.text
            resetEmailTextField = textField
        }
        
        self.present(alertController, animated: true, completion: nil)
        
    }
    
    func showJoinWithEventCodeAlert() {
        var eventCodeTextField: UITextField?
        let alertController = UIAlertController(title: "Enter Event Code", message: nil, preferredStyle: .alert)
        alertController.addAction(UIAlertAction(title: "Join Challenge", style: .default) {(action:UIAlertAction!) in
            
            APIManager.getChallengeByCode(eventCode: (eventCodeTextField?.text)!, completionHandler: { (responseJSON, error) in
                if (error == nil) {
                    let challenge = Challenge(json: responseJSON)
                    
                    let formatter = DateFormatter()
                    formatter.timeZone = TimeZone.autoupdatingCurrent
                    formatter.locale = Locale(identifier: "en_US_POSIX")
                    formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
                    
                    if (formatter.date(from: challenge.endDate)?.compare(Date()) == ComparisonResult.orderedAscending) {
                        self.showAlert(title: "Error", message: "Sorry! This challenge has already finished", defaultActionTitle: "Okay")
                    }
                    else {
                        // Start Onboarding
                        //self.performSegue(withIdentifier: "showRegisterViewController", sender: challenge)
                        
                        self.showReactJSScreen(challenge: responseJSON)
                        
                        
                    }
                }
                else {
                    self.showAlert(title: "Error", message: "Challenge not found!", defaultActionTitle: "Close")
                }
            })
        })
        
        alertController.addAction(UIAlertAction(title: "Cancel", style: .default, handler: nil))
        
        alertController.addTextField { (textField) in
            textField.placeholder = "Event Code"
            eventCodeTextField = textField
        }
        
        self.present(alertController, animated: true, completion: { (action) in
            self.firstTimeJoin = false
        })
        
    }
    
    func clearInput() {
        emailField.text? = ""
        passwordField.text? = ""
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
        titleLabel.text = "Logging In..."
        
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
    
    /**
     * Function used to validate form
     * Checks several error conditions
     **/
    func isLoginValid() -> Bool {
        var validForm = true;        
        
        // Is email text field empty
        if (emailField.text?.isEmpty == true) {
            validForm = false;
        }
        
        // Is password text field empty
        if (passwordField.text?.isEmpty == true) {
            validForm = false;
        }
        
        // Is email a valid e-mail address
        if (isValidEmail(emailField.text!) == false) {
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
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        
        if (segue.identifier == "showOnboardingViewController") {
            
            let viewController = segue.destination as! OnboardingViewController
            viewController.props = sender as? JSON
            
        }
        else if (segue.identifier == "showRegisterViewController") {
            
            let viewNavController = segue.destination as! RegisterNavigationViewController
            let viewController = viewNavController.topViewController as! RegisterPageViewController
            
            viewController.mCurrentChallenge = sender as? Challenge
            
        }
    }
    
}
