//
//  AboutUsViewController.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import WebKit

class AboutUsViewController: CCParentViewController, WKUIDelegate, WKNavigationDelegate {
    
    /* Outlets */
    var webView: WKWebView!
    @IBOutlet var loadingIndicator: UIActivityIndicatorView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        webView.loadHTMLString(CCStatics.aboutUsContent, baseURL: Bundle.main.url(forResource: "SourceSansPro-It", withExtension: "otf"))
    }
    
    override func loadView() {
        let webConfiguration = WKWebViewConfiguration()
        webConfiguration.preferences.javaScriptEnabled = true
        
        webView = WKWebView(frame: .zero, configuration: webConfiguration)
        webView.uiDelegate = self
        webView.navigationDelegate = self
        
        view = webView
        
        let centerX = UIScreen.main.bounds.size.width / 2.0 - (37.0 / 2.0)
        let centerY = UIScreen.main.bounds.size.height / 2.0 - (37.0 * 2.0)
        
        loadingIndicator = UIActivityIndicatorView(frame: CGRect(x: centerX , y: centerY, width: 37, height: 37))
        loadingIndicator.activityIndicatorViewStyle = .whiteLarge
        loadingIndicator.color = CCStatics.primaryColor
        loadingIndicator.hidesWhenStopped = true
        loadingIndicator.startAnimating()
        view.addSubview(loadingIndicator)
    }
    
    /**
     * Callback function: When webview finishes loading content, stop animating activity indicator and hide
    **/
    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        loadingIndicator.stopAnimating()
    }
    
    func webView(_ webView: WKWebView, createWebViewWith configuration: WKWebViewConfiguration, for navigationAction: WKNavigationAction, windowFeatures: WKWindowFeatures) -> WKWebView? {
        if navigationAction.targetFrame == nil {
            let url = navigationAction.request.url
            UIApplication.shared.openURL(url!)
        }
        return nil
    }
    
}
