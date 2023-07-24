//
//  ReactNativeEventEmitter.swift
//  Calorie Cloud
//
//  Copyright © 2017 Calorie Cloud. All rights reserved.
//

import React
import Foundation

@objc(ReactNativeEventEmitter)
open class ReactNativeEventEmitter: RCTEventEmitter {
    
    override init() {
        super.init()
        EventEmitter.sharedInstance.registerEventEmitter(eventEmitter: self)
    }
    
    @objc open override func supportedEvents() -> [String] {
        return EventEmitter.sharedInstance.allEvents
    }
    
    @objc func dismissPresentedViewController(_ reactTag: NSNumber) {
        DispatchQueue.main.async {
            if let view = SwiftRCTBridge.sharedInstance.bridge?.uiManager.view(forReactTag: reactTag) {
                let presentedViewController = (view.reactViewController() as! OnboardingViewController)
                presentedViewController.skipButtonPressed(presentedViewController.skipButton)
            }
        }
    }
    
    @objc func updateUI(_ reactTag: NSNumber, _ position: NSNumber, _ total: NSNumber) {
        
        DispatchQueue.main.async {
            if let view = SwiftRCTBridge.sharedInstance.bridge?.uiManager.view(forReactTag: reactTag) {
                let presentedViewController = (view.reactViewController() as! OnboardingViewController)
                presentedViewController.updateUI(position, total)
            }
        }
        
    }
    
    @objc func loadChallengeInfo (_ reactTag: NSNumber, _ challenge: NSDictionary) {
        
        DispatchQueue.main.async {
           
            if let view = SwiftRCTBridge.sharedInstance.bridge?.uiManager.view(forReactTag: reactTag) {
                view.reactViewController().performSegue(withIdentifier: "showChallengeInfoViewController", sender: challenge)
            }

        }
        
    }
    
    @objc func loadHealthAuthScreen (_ reactTag: NSNumber, _ fromOnboarding: NSNumber ) {
        
        DispatchQueue.main.async {
            
            if let view = SwiftRCTBridge.sharedInstance.bridge?.uiManager.view(forReactTag: reactTag) {
                if (fromOnboarding.intValue == 1) {
                    let vc = view.reactViewController() as! ConnectTrackerViewController
                    
                    if (!CCStatics.wasHealthKitEnabled()) {
                        vc.pageViewController.setViewControllers([(vc.pageViewController.pageViewController(vc.pageViewController, viewControllerAfter: vc))!], direction: .forward, animated: true, completion: nil)
                    }
                    else {
                        
                        let parent = vc.pageViewController.parent?.presentingViewController
                        
                        vc.dismiss(animated: true, completion: {
                            parent!.performSegue(withIdentifier: "showDashboardViewController", sender: self)
                        })
                        
                    }
                }
                else {
                    let vc = view.reactViewController() as! TrackersViewController
                    vc.getHealthKitPermission();
                }
            
            }
        
        }
        
    }
    
}

