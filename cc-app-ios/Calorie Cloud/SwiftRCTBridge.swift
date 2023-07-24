//
//  Bridge.swift
//  Calorie Cloud
//
//  Copyright © 2017 Calorie Cloud. All rights reserved.
//

import React

class SwiftRCTBridge : NSObject {
    static let sharedInstance = SwiftRCTBridge()
    var bridge: RCTBridge?
    
    func createBridgeIfNeeded() -> RCTBridge {
        if bridge == nil {
            bridge = RCTBridge.init(delegate: self, launchOptions: nil)
        }
        return bridge!
    }
    
    func viewForModule(_ moduleName: String, initialProperties: [String : Any]?) -> RCTRootView {
        let viewBridge = createBridgeIfNeeded()
        let rootView: RCTRootView = RCTRootView(bridge: viewBridge, moduleName: moduleName, initialProperties: initialProperties)
        return rootView
    }
}

extension SwiftRCTBridge: RCTBridgeDelegate {
    func sourceURL(for bridge: RCTBridge!) -> URL! {
        return (UIApplication.shared.delegate as! AppDelegate).RNBundleURL
    }
}
