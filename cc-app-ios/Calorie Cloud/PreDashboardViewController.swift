//
//  PreDashboardViewController.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

class PreDashboardViewController: UIViewController {
    
    @IBAction func visitDashboardButtonReleased(_ button: UIButton) {
        let parent = self.presentingViewController
        
        self.dismiss(animated: true, completion: {
            parent!.performSegue(withIdentifier: "showDashboardViewController", sender: self)
        })
    }
    
    override func viewDidLoad() {
        super.viewDidLoad();
    }
}
