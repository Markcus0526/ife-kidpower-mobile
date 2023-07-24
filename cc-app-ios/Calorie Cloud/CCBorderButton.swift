//
//  CCButton.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import UIKit

@IBDesignable class CCBorderButton: UIButton {
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        layer.cornerRadius = 5
        layer.borderColor = highlightedColor.cgColor
        layer.borderWidth = 2
    }
    
    @IBInspectable var highlightedColor: UIColor = CCStatics.darkGreyColor
    @IBInspectable var restingColor: UIColor = CCStatics.buttonDefaultColor
    
    override var isHighlighted: Bool {
        didSet {
            if isHighlighted {
                backgroundColor = highlightedColor
            } else {
                backgroundColor = restingColor
            }
        }
    }
    
}
