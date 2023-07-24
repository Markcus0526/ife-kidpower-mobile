//
//  MenuViewController.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import SwiftyJSON
import ZendeskSDK

class MenuViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {
    
    let savedUser = User(serializedUser: CCStatics.getSavedUser()!)
    let healthManager = HealthKitManager()
    
    @IBOutlet var userLabel: UILabel!
    var modalView: UIView!
    
    enum MenuItems: Int {
        case myStats = 0
        case trackers = 1
        case aboutUs = 2
        case getHelp = 3
        case logout = 4
    }
    
    var drawerLabels = [
        "My Stats",
        "Trackers",
        "About",
        "Get Help",
        "Logout"
    ]
    
    var drawerImages = [
        "ic_favorite_border_white",
        "ic_settings_white",
        "ic_info_outline_white",
        "ic_help_outline_white",
        "ic_subdirectory_arrow_left_white"
    ]

    override func viewDidLoad() {
        userLabel.text = savedUser.screenName
    }
    
    // When menu is open, disable front view controller gestures and show transparent overlay
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        self.revealViewController().view.addGestureRecognizer(self.revealViewController().panGestureRecognizer())
        self.revealViewController().frontViewController.revealViewController().tapGestureRecognizer()
        self.revealViewController().frontViewController.view.isUserInteractionEnabled = false
        
        modalView = UIView(frame: CGRect(x: 0, y: 0, width: self.revealViewController().frontViewController.view.frame.width, height: self.revealViewController().frontViewController.view.frame.height))
        modalView.backgroundColor = UIColor.black
        modalView.alpha = 0.5
        
        self.revealViewController().frontViewController.view.addSubview(modalView)
    }
    
    // When menu is closed, enable front view controller gestures
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
    self.revealViewController().frontViewController.view.addGestureRecognizer(self.revealViewController().panGestureRecognizer())
        self.revealViewController().frontViewController.view.isUserInteractionEnabled = true
        
        self.modalView.removeFromSuperview()
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return drawerImages.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell{
        let cell : UITableViewCell = tableView.dequeueReusableCell(withIdentifier: "menuItem", for: indexPath) as UITableViewCell
        
        cell.textLabel!.text = self.drawerLabels[(indexPath as NSIndexPath).row]
        cell.textLabel!.textColor = UIColor.white
        cell.textLabel!.font = UIFont(name: "SourceSansPro-Semibold", size: 18)
        cell.imageView!.image = UIImage(named: self.drawerImages[(indexPath as NSIndexPath).row])
        
        return cell
    }
    
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        var controller:UIViewController!
        
        tableView.deselectRow(at: indexPath, animated: true)
        switch ((indexPath as NSIndexPath).row) {
            case MenuItems.myStats.rawValue:
                controller = self.storyboard?.instantiateViewController(withIdentifier: "DashboardNavigationController")
                self.revealViewController().pushFrontViewController(controller, animated: true)
            break;
            
            case MenuItems.aboutUs.rawValue:
                controller = self.storyboard?.instantiateViewController(withIdentifier: "AboutUsNavigationController")
                self.revealViewController().pushFrontViewController(controller, animated: true)
            break;
            
            case MenuItems.getHelp.rawValue:
                let helpCenterContentModel = ZDKHelpCenterOverviewContentModel.defaultContent()
                //helpCenterContentModel?.groupType = .section
                // Load the KPW Help Center if KPW, otherwise load the CC Help Center
                #if KPW
                    helpCenterContentModel?.groupIds = ["207346167"]
                #else
                    helpCenterContentModel?.groupIds = ["205542167"]
                #endif
                
                helpCenterContentModel?.hideContactSupport = true
                
                UINavigationBar.appearance().tintColor = UIColor.white
                UINavigationBar.appearance().barTintColor = CCStatics.primaryColor
                UINavigationBar.appearance().isTranslucent = false
            
                UINavigationBar.appearance().titleTextAttributes = [NSForegroundColorAttributeName : UIColor.white, NSFontAttributeName: UIFont.init(name: "SourceSansPro-Regular", size: 18.0)!]
                ZDKHelpCenter.setNavBarConversationsUIType(.none)
                self.revealViewController().revealToggle(animated: true)
                ZDKHelpCenter.presentOverview(self.revealViewController().frontViewController, with: helpCenterContentModel)
                
            break;
            
            case MenuItems.trackers.rawValue:
                controller = self.storyboard?.instantiateViewController(withIdentifier: "TrackerNavigationController")
                self.revealViewController().pushFrontViewController(controller, animated: true)
            break;
            
            case MenuItems.logout.rawValue:
                showLogoutAlert()
            break;
        
            default:
            break;
        }
    }
    
    func showLogoutAlert() {
        let alertController = UIAlertController(title: "Logout", message: "Are you sure you want to logout? Note: This will prevent the application from sending data to your account.", preferredStyle: .alert)
        alertController.addAction(UIAlertAction(title: "No", style: .default) { (action:UIAlertAction!) in })
        alertController.addAction(UIAlertAction(title: "Yes", style: .default) { (action:UIAlertAction!) in
            
            APIManager.logoutUser(accessToken: self.savedUser.accessToken, completionHandler: { (response) in
                
                if (response == "OK") {
                    CCStatics.logoutUser()
                    self.healthManager.disableBackgroundDelivery()
                    
                    self.revealViewController().revealToggle(animated: true)
                    
                    if (UIApplication.shared.delegate!.window!!.rootViewController!.isKind(of: SWRevealViewController.self)) {
                        self.revealViewController().performSegue(withIdentifier: "showLoginViewController", sender: nil)
                    }
                    else {
                        self.revealViewController().frontViewController.dismiss(animated: true, completion: nil)
                    }
                }
            })
            
        })
        
        self.present(alertController, animated: true, completion: nil)
    }

}
