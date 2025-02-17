//
//  RNZendesk.swift
//  RNZendesk
//
//  Created by David Chavez on 24.04.18.
//  Copyright © 2018 David Chavez. All rights reserved.
//

import UIKit
import Foundation
import SupportSDK
import SupportProvidersSDK
import ZendeskCoreSDK
import CommonUISDK

@objc(RNZendesk)
class RNZendesk: RCTEventEmitter {

    override public static func requiresMainQueueSetup() -> Bool {
        return false;
    }
    
    @objc(constantsToExport)
    override func constantsToExport() -> [AnyHashable: Any] {
        return [:]
    }
    
    @objc(supportedEvents)
    override func supportedEvents() -> [String] {
        return []
    }
    
    
    // MARK: - Initialization

    @objc(initialize:)
    func initialize(config: [String: Any]) {
        guard
            let appId = config["appId"] as? String,
            let clientId = config["clientId"] as? String,
            let zendeskUrl = config["zendeskUrl"] as? String else { return }
        
        Zendesk.initialize(appId: appId, clientId: clientId, zendeskUrl: zendeskUrl)
        Support.initialize(withZendesk: Zendesk.instance)
    }
            
    @objc(registerPushToken:)
    func registerPushToken(token: String?) {
        guard let token = token else { return }
        let locale = NSLocale.preferredLanguages.first ?? "en"
        ZDKPushProvider(zendesk: (Zendesk.instance)!).register(deviceIdentifier: token, locale: locale) { (pushResponse, error) in
            if ((error) != nil) {
            print("Couldn't register device: \(token). Error: \(error)")
        } else {
            print("Successfully registered device: \(token)")
          }
        }
    }

    // MARK: - Indentification
    
    @objc(identifyJWT:)
    func identifyJWT(token: String?) {
        guard let token = token else { return }
        let identity = Identity.createJwt(token: token)
        Zendesk.instance?.setIdentity(identity)
    }
    
    @objc(identifyAnonymous:email:)
    func identifyAnonymous(name: String?, email: String?) {
        let identity = Identity.createAnonymous(name: name, email: email)
        Zendesk.instance?.setIdentity(identity)
    }
    
    // MARK: - UI Methods
    
    @objc(showHelpCenter:)
    func showHelpCenter(with options: [String: Any]) {
        DispatchQueue.main.async {
            let hcConfig = HelpCenterUiConfiguration()

            if let hideContactSupport = options["hideContactSupport"] as? Bool {
                hcConfig.showContactOptions = !hideContactSupport
            } else {
                hcConfig.showContactOptions = true
            }

            let helpCenter = HelpCenterUi.buildHelpCenterOverviewUi(withConfigs: [hcConfig])
            
            let nvc = UINavigationController(rootViewController: helpCenter)
            UIApplication.shared.keyWindow?.rootViewController?.present(nvc, animated: true, completion: nil)
        }
    }
    
    @objc(showNewTicket:)
    func showNewTicket(with options: [String: Any]) {
        DispatchQueue.main.async {
            let config = RequestUiConfiguration()
            if let tags = options["tags"] as? [String] {
                config.tags = tags
            }          
            var customList: [CustomField] = [];
            if let customFields = options["custom_fields"] as? [Any] {
                for item in customFields {
                    let result = item as! NSDictionary
                    
                    let customField = CustomField(fieldId: Int64(truncating: result["fieldId"] as! NSNumber), value: result["value"])
                    customList.append(customField)
                }
            }
            config.customFields = customList
            let requestScreen = RequestUi.buildRequestUi(with: [config])
            
            let nvc = UINavigationController(rootViewController: requestScreen)
            UIApplication.shared.keyWindow?.rootViewController?.present(nvc, animated: true, completion: nil)
        }
    }

    @objc(showTicketList)
    func showTicketList() {
        DispatchQueue.main.async {
            let requestListController = RequestUi.buildRequestList()

            let nvc = UINavigationController(rootViewController: requestListController)
            UIApplication.shared.keyWindow?.rootViewController?.present(nvc, animated: true)
        }
    }

    @objc(showSpecificTicket:)
    func showSpecificTicket(with requestID: String) {
        DispatchQueue.main.async {
            let requestTicketController = RequestUi.buildRequestUi(requestId: requestID)
            let nvc = UINavigationController(rootViewController: requestTicketController)
            UIApplication.shared.keyWindow?.rootViewController?.present(nvc, animated: true)
        }
    }
}
