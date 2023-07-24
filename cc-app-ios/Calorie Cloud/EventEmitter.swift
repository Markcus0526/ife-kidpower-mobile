//
//  EventEmitter.swift
//  Calorie Cloud
//
//  Copyright © 2017 Calorie Cloud. All rights reserved.
//

class EventEmitter {

public static var sharedInstance = EventEmitter()
private static var eventEmitter: ReactNativeEventEmitter!

func registerEventEmitter(eventEmitter: ReactNativeEventEmitter) {
    EventEmitter.eventEmitter = eventEmitter
}

func dispatch(name: String, body: Any?) {
    EventEmitter.eventEmitter.sendEvent(withName: name, body: body)
}

lazy var allEvents: [String] = {
    return ["DashboardEvent", "OnboardingEvent", "TrackerEvent"]
}()

}

