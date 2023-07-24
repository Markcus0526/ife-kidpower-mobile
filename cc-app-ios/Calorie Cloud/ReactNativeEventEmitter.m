//
//  ReactNativeEventEmitter.m
//  Calorie Cloud
//
//  Copyright © 2017 Calorie Cloud. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(ReactNativeEventEmitter, RCTEventEmitter)
RCT_EXTERN_METHOD(supportedEvents)
RCT_EXTERN_METHOD(dismissPresentedViewController:(nonnull NSNumber *)reactTag)
RCT_EXTERN_METHOD(updateUI: (nonnull NSNumber *) reactTag: (nonnull NSNumber *) position: (nonnull NSNumber *) total)
RCT_EXTERN_METHOD(loadChallengeInfo: (nonnull NSNumber *) reactTag: (nonnull NSDictionary *) challenge)
RCT_EXTERN_METHOD(loadHealthAuthScreen: (nonnull NSNumber *) reactTag: (nonnull NSNumber *) fromOnboarding)
@end

