//
//  RegisterNavigationViewController.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

class RegisterNavigationViewController: UINavigationController {
    
    override func viewDidLoad() {
        super.viewDidLoad()
        //UIApplication.shared.statusBarStyle = .lightContent
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        UIApplication.shared.statusBarStyle = .lightContent
        
    }
}
