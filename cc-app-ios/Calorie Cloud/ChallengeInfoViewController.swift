//
//  ChallengeInfoViewController.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import AlamofireImage

class ChallengeInfoViewController: UIViewController {
    
    var mCurrentChallenge: [AnyHashable: Any]!
    
    @IBOutlet var challengeImage: UIImageView!
    @IBOutlet var challengeName: UILabel!
    @IBOutlet var challengeDates: UILabel!
    @IBOutlet var challengeDisclaimer: UILabel!
    @IBOutlet var challengeDonationRate: UILabel!
    @IBOutlet var challengeDonationRateDescription: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        adjustChallengeInfo()
    }
    
    func adjustChallengeInfo() {
        
        if (mCurrentChallenge["challengePic"] as? String != "") {
            challengeImage.af_setImage(withURL: URL(string: mCurrentChallenge["challengePic"] as! String)!)
        }
        else {
            challengeImage.af_setImage(withURL: URL(string: mCurrentChallenge["orgLogo"] as! String)!)
        }
        challengeName.text = mCurrentChallenge["challengeName"] as? String
        challengeDates.text = CCStatics.formatDateString(dateString: mCurrentChallenge["startDate"] as! String, toFormat: "dd MMMM yyyy", utcTimeZone: true) + " - " + CCStatics.formatDateString(dateString: mCurrentChallenge["endDate"] as! String, toFormat: "dd MMMM yyyy", utcTimeZone: true)
        
        if (mCurrentChallenge["brand"] as! String) ==  "unicef" {
                challengeDisclaimer.text = "The Power Points you earn during the challenge will be converted into life-saving food packets for malnourished kids. Ready to start?"
                challengeDonationRate.text = String.init(format: challengeDonationRate.text!, mCurrentChallenge["impactMultiplier"] as! String!, "5")
                
            challengeDonationRateDescription.text = String.init(format: challengeDonationRateDescription.text!, mCurrentChallenge["orgName"] as! String, mCurrentChallenge["impactMultiplier"] as! String, "packet(s) of therapeutic food", "five", "Power Point(s)", "earned")
        }
        
        else if (mCurrentChallenge["brand"] as! String) == "calorieCloud" {
            
                challengeDisclaimer.text = String.init(format: challengeDisclaimer.text!, mCurrentChallenge["orgName"] as! String)
            
                challengeDonationRate.text = String.init(format: challengeDonationRate.text!, mCurrentChallenge["impactMultiplier"] as! String, "1")
                
                challengeDonationRateDescription.text = String.init(format:challengeDonationRateDescription.text!, mCurrentChallenge["orgName"] as! String, mCurrentChallenge["impactMultiplier"] as! String, "calorie(s)", "one", "calorie(s)", "burned")
        }
        
        
    }
    
}
