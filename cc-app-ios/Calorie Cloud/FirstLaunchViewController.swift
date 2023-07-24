//
//  FirstLaunchViewController.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

class FirstLaunchViewController: UIViewController {
    
    /* Button Pressed RELEASE */
    @IBAction func loginButtonReleased(_ button: UIButton) {
        CCStatics.setFirstLaunch(false)
        self.dismiss(animated: true)
        self.performSegue(withIdentifier: "showLoginViewController", sender: nil)
    }
    
    /* Button Pressed RELEASE */
    @IBAction func joinButtonReleased(_ button: UIButton) {
        CCStatics.setFirstLaunch(false)
        self.dismiss(animated: true)
        self.performSegue(withIdentifier: "showLoginViewController", sender: true)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if (segue.identifier == "showLoginViewController") {
            if (sender as? Bool == true) {
                (segue.destination as! LoginViewController).firstTimeJoin = true
            }
        }
    }
    
}
