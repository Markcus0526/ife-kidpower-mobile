//
//  OnboardingViewController.swift
//  Calorie Cloud
//
//  Copyright © 2017 Calorie Cloud. All rights reserved.
//

import React
import SwiftyJSON

class OnboardingViewController: UIViewController {
    var props:JSON!
    
    func updateUI(_ position: NSNumber, _ total: NSNumber) {
        
        DispatchQueue.main.async {
            if (position.intValue == (total.intValue - 1)) {
                self.skipButton.isHidden = true
                self.nextButton.setTitle("Get Started", for: .normal)
            }
            else {
                if (self.skipButton.isHidden) {
                    self.skipButton.isHidden = false
                }
                
                if (self.nextButton.currentTitle != "Next") {
                    self.nextButton.setTitle("Next", for: .normal)
                }
                
            }
        }
        
    }
    
    @IBAction func nextButtonPressed(_ sender: UIButton) {
        EventEmitter.sharedInstance.dispatch(name: "OnboardingEvent", body: nil)
    }
    
    @IBAction func skipButtonPressed(_ sender: UIButton) {
       weak var pvc = self.presentingViewController
        dismiss(animated: true) {
             pvc?.performSegue(withIdentifier: "showRegisterViewController", sender: Challenge(json: self.props))
        }
    }
    @IBOutlet var nextButton: UIButton!
    @IBOutlet var skipButton: UIButton!
    override func viewDidLoad() {
        
        let rootView = SwiftRCTBridge.sharedInstance.viewForModule("Onboarding", initialProperties: props.dictionaryObject)
        
        rootView.frame = view.bounds
        view.insertSubview(rootView, at: 0)
    }
}
