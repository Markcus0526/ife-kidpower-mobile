//
//  RegisterPageViewController.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

class RegisterPageViewController: UIPageViewController {
    
    var registerPages = [UIViewController]();
    
    @IBAction func cancelButtonPressed(_ button: AnyObject) {
        dismiss(animated: true)
    }
    
    var mCurrentChallenge: Challenge!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let createAccountViewController = storyboard?.instantiateViewController(withIdentifier: "CreateAccountViewController") as! CreateAccountViewController
        
        registerPages.append(createAccountViewController)
        
        if (mCurrentChallenge.teamSelection == CCStatics.ChallengeMultipleTeams || mCurrentChallenge.teamSelection == CCStatics.ChallengeRegionsTeams) {
 
            let pickTeamViewController = storyboard?.instantiateViewController(withIdentifier: "PickTeamViewController") as! PickTeamViewController
            
            registerPages.append(pickTeamViewController)
        }
        
        let connectTrackerViewController = storyboard?.instantiateViewController(withIdentifier: "ConnectTrackerViewController") as! ConnectTrackerViewController
        
        registerPages.append(connectTrackerViewController)
        
        if (!CCStatics.wasHealthKitEnabled()) {
           let onboardingAuthorizationViewController = storyboard?.instantiateViewController(withIdentifier: "OnboardingAuthorizationViewController") as! OnboardingAuthorizationViewController
            
           registerPages.append(onboardingAuthorizationViewController)
        }
        
        print(registerPages)
        
        setViewControllers([createAccountViewController], direction: .forward, animated: true, completion: nil)
        
    }
    
    func getTotalPageCount() -> Int {
        return registerPages.count
    }
    
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerAfter viewController: UIViewController) -> UIViewController? {
        
        var index = registerPages.index(of: viewController)! + 1;
        
        if (index >= getTotalPageCount()) {
            return nil
        }
        
        return registerPages[index]
        
    }
    
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerBefore viewController: UIViewController) -> UIViewController? {
        
        var index = registerPages.index(of: viewController)
        
        if ((index?.hashValue)! <= 0) {
            return nil
        }
        
        index = index! - 1
        
        return registerPages[index!]
    }
}
