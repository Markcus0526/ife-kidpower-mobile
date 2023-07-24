//
//  CCButton.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import UIKit

@IBDesignable class CCButton: UIButton {
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        layer.cornerRadius = 5
    }
    
    @IBInspectable var highlightedColor: UIColor = CCStatics.buttonPressedColor
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
