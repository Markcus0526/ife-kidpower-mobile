//
//  CCParentViewController.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

class CCParentViewController: UIViewController {
    
    /* Outlets */
    @IBOutlet var menuButton: UIBarButtonItem!
    @IBOutlet var rightBarNavItem: UIBarButtonItem!
    
    override func viewDidLoad() {
        if (self.revealViewController() != nil) {
            menuButton.target = self.revealViewController()
            menuButton.action = #selector(SWRevealViewController.revealToggle(_:))
            self.view.addGestureRecognizer(self.revealViewController().panGestureRecognizer())
            self.revealViewController().rearViewRevealWidth = 230
            self.revealViewController().bounceBackOnOverdraw = false
            self.revealViewController().bounceBackOnLeftOverdraw = false
            self.revealViewController().frontViewShadowRadius = 0
            self.revealViewController().rearViewRevealOverdraw = 0
            self.revealViewController().rearViewRevealDisplacement = 0
            self.revealViewController().clipsViewsToBounds = true
            self.revealViewController().toggleAnimationType = SWRevealToggleAnimationType.easeOut
            self.revealViewController().toggleAnimationDuration = 0.25
        }
        
        if (rightBarNavItem != nil) {
            rightBarNavItem.image = UIImage.init()
            rightBarNavItem.setBackgroundImage(UIImage.init(cgImage: #imageLiteral(resourceName: "cc-icon-alternate").cgImage!, scale: 5, orientation: #imageLiteral(resourceName: "cc-icon-alternate").imageOrientation), for: .normal, barMetrics: .default)
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
    }
}
