//
//  HealthKitManager.swift
//  Calorie Cloud
//
//  Copyright © 2016 Calorie Cloud. All rights reserved.
//

import HealthKit

class HealthKitManager {
    
    // Create a singleton object in order to utilize one shared instance of the HealthKitStore
    class var sharedInstance: HealthKitManager {
        struct Singleton {
            static let instance = HealthKitManager()
        }
        
        return Singleton.instance
    }
    
    let healthStore = HKHealthStore()
    
    var GlobalMainQueue: DispatchQueue {
        return DispatchQueue.main
    }
    
    let queryGroup = DispatchGroup()
    
    func isHealthKitInstalled() -> Bool {
        return HKHealthStore.isHealthDataAvailable()
    }
    
    /**
     * Function used to prompt the user for authorization to their HealthKit data
     **/
    func authorizeHealthKit(completion: ((_ success: Bool, _ error: NSError?) -> Void)!) {
        
        // State the health data types we want to read from HealthKit. In this case, we'll use Active Calories
        let healthDataToRead = Set(arrayLiteral: HKObjectType.quantityType(forIdentifier: HKQuantityTypeIdentifier.activeEnergyBurned)!, HKObjectType.quantityType(forIdentifier: HKQuantityTypeIdentifier.stepCount)!)
        
        // Check if HealthKit is supported on device
        if (!HKHealthStore.isHealthDataAvailable()) {
            // Let the user know that they can't use their phone as a tracker, but can still use the Calorie Cloud application
            let error = NSError.init(domain: "caloriecloud.org", code: 100, userInfo: nil)
            completion?(false, error)
        }
        
        // Request authorization to read/write the specified data types above
        self.healthStore.requestAuthorization(toShare: nil, read: healthDataToRead) { (success: Bool, error: Error?) -> Void in
            completion?(success, error as NSError?)
        }
    }
    
    func getActivityDataSince(_ startDateString: String, completion: ((Array<NSDictionary>?, Array<NSDictionary>?, NSError?) -> Void)!) {
        
        let formatter = DateFormatter()
        formatter.timeZone = TimeZone.autoupdatingCurrent
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        
        let stepsQuantityType = HKQuantityType.quantityType(forIdentifier: HKQuantityTypeIdentifier.stepCount)
        let calorieQuantityType = HKQuantityType.quantityType(forIdentifier: HKQuantityTypeIdentifier.activeEnergyBurned)
        
        let calendar = Calendar.current;
        
        var interval = DateComponents()
        interval.day = 1
        
        let anchorComponents = (calendar as NSCalendar).components([.day], from: Date())
        
        guard let anchorDate = calendar.date(from: anchorComponents) else {
            fatalError("*** Unable to create a valid date from the given compoenents ***")
        }
        
        let stepsQuery = HKStatisticsCollectionQuery(quantityType: stepsQuantityType!, quantitySamplePredicate: nil, options: .cumulativeSum, anchorDate: anchorDate, intervalComponents: interval)
        let calorieQuery = HKStatisticsCollectionQuery(quantityType: calorieQuantityType!, quantitySamplePredicate: nil, options: .cumulativeSum, anchorDate: anchorDate, intervalComponents: interval)
        
        var calorieResults = Array<NSDictionary>();
        var stepResults = Array<NSDictionary>();
        
        let resultsDateFormatter = DateFormatter()
        resultsDateFormatter.dateFormat = "yyyy-MM-dd"
        
        stepsQuery.initialResultsHandler = {
            query, results, error in
            
            guard let statsCollection = results else {
                fatalError("*** An error has occurred while calcuating the statistics: \(error?.localizedDescription) ***")
            }
            
            var startDate: Date
            let endDate = Date()
            
            if let formattedStartDate = formatter.date(from: startDateString) {
                startDate = formattedStartDate
            } else {
                startDate = (calendar as NSCalendar).date(byAdding: .day, value: -30, to: endDate, options: [])!
            }
            
            statsCollection.enumerateStatistics(from: startDate, to: endDate, with: { (statistics, stop) in
                if let quantity = statistics.sumQuantity() {
                    let date = resultsDateFormatter.string(from: statistics.startDate)
                    stepResults.append(["date": date, "source": "healthkit", "steps": quantity.doubleValue(for: HKUnit.count())])
                }
            })
        
            
            self.queryGroup.leave()
        }
        
        
        calorieQuery.initialResultsHandler = {
            query, results, error in
            
            guard let statsCollection = results else {
                fatalError("*** An error has occurred while calcuating the statistics: \(error?.localizedDescription) ***")
            }
            
            var startDate: Date
            let endDate = Date()
            
            if let formattedStartDate = formatter.date(from: startDateString) {
                startDate = formattedStartDate
            } else {
                startDate = (calendar as NSCalendar).date(byAdding: .day, value: -30, to: endDate, options: [])!
            }
            
            statsCollection.enumerateStatistics(from: startDate, to: endDate, with: { (statistics, stop) in
                if let quantity = statistics.sumQuantity() {
                    let date = resultsDateFormatter.string(from: statistics.startDate)
                    calorieResults.append(["date": date, "source": "healthkit", "calories":quantity.doubleValue(for: HKUnit.kilocalorie())])
                }
            })
            
            self.queryGroup.leave()
        }
        
        queryGroup.enter()
        GlobalMainQueue.async(group: queryGroup) {
            self.healthStore.execute(stepsQuery)
        }
        
        queryGroup.enter()
        GlobalMainQueue.async(group: queryGroup) {
            self.healthStore.execute(calorieQuery)
        }
        
        queryGroup.notify(queue: GlobalMainQueue) {
            completion(stepResults, calorieResults, nil)
        }

        calorieQuery.statisticsUpdateHandler = {
            query, stats, results, error in
            
            if (error == nil) {
                (UIApplication.shared.delegate as! AppDelegate).getCalorieData()
            }
           
        }
        
        stepsQuery.statisticsUpdateHandler = {
            query, stats, results, error in
            
            if (error == nil) {
                (UIApplication.shared.delegate as! AppDelegate).getStepData()
            }
        
        }
        
    }
    
    
    /**
     * Function used to get to get daily summary of Active Energy Burned (Active Calories) from a sample type
     * If start date is not defined, get daily summary starting 30 days in the past
     **/
    func getDailyActiveCaloriesSince(_ startDateString: String, completion: ((Array<NSDictionary>?, NSError?) -> Void)!) {
        
        let formatter = DateFormatter()
        formatter.timeZone = TimeZone.autoupdatingCurrent
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        
        let calorieQuantityType = HKQuantityType.quantityType(forIdentifier: HKQuantityTypeIdentifier.activeEnergyBurned)
        
        let calendar = Calendar.current;
        
        var interval = DateComponents()
        interval.day = 1
        
        let anchorComponents = (calendar as NSCalendar).components([.day], from: Date())
        
        guard let anchorDate = calendar.date(from: anchorComponents) else {
            fatalError("*** Unable to create a valid date from the given compoenents ***")
        }
        
        let calorieQuery = HKStatisticsCollectionQuery(quantityType: calorieQuantityType!, quantitySamplePredicate: nil, options: .cumulativeSum, anchorDate: anchorDate, intervalComponents: interval)
        var calorieResults = Array<NSDictionary>();
        let resultsDateFormatter = DateFormatter()
        resultsDateFormatter.dateFormat = "yyyy-MM-dd"
        
        calorieQuery.initialResultsHandler = {
            query, results, error in
            
            guard let statsCollection = results else {
                fatalError("*** An error has occurred while calcuating the statistics: \(error?.localizedDescription) ***")
            }
            
            var startDate: Date
            let endDate = Date()
            
            if let formattedStartDate = formatter.date(from: startDateString) {
                startDate = formattedStartDate
            } else {
                startDate = (calendar as NSCalendar).date(byAdding: .day, value: -30, to: endDate, options: [])!
            }
            
            statsCollection.enumerateStatistics(from: startDate, to: endDate, with: { (statistics, stop) in
                if let quantity = statistics.sumQuantity() {
                    let date = resultsDateFormatter.string(from: statistics.startDate)
                    calorieResults.append(["date": date, "source": "healthkit", "calories":quantity.doubleValue(for: HKUnit.kilocalorie())])
                }
            })
            
            completion(calorieResults, nil)
        }
        
        healthStore.execute(calorieQuery)
    }
    
    /**
     * Function used to get daily summary of Step Counts from a specified start date
     * If start date is not defined, get daily summary starting 30 days in the past
     **/
    func getDailyStepCountSince(_ startDateString: String, completion: ((Array<NSDictionary>?, NSError?) -> Void)!) {
        
        let formatter = DateFormatter()
        formatter.timeZone = TimeZone.autoupdatingCurrent
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        
        let stepsQuantityType = HKQuantityType.quantityType(forIdentifier: HKQuantityTypeIdentifier.stepCount)
        
        let calendar = Calendar.current;
        
        var interval = DateComponents()
        interval.day = 1
        
        let anchorComponents = (calendar as NSCalendar).components([.day], from: Date())
        
        guard let anchorDate = calendar.date(from: anchorComponents) else {
            fatalError("*** Unable to create a valid date from the given compoenents ***")
        }
        
        let stepsQuery = HKStatisticsCollectionQuery(quantityType: stepsQuantityType!, quantitySamplePredicate: nil, options: .cumulativeSum, anchorDate: anchorDate, intervalComponents: interval)
        
        var stepResults = Array<NSDictionary>();
        let resultsDateFormatter = DateFormatter()
        resultsDateFormatter.dateFormat = "yyyy-MM-dd"
        
        stepsQuery.initialResultsHandler = {
            query, results, error in
            
            guard let statsCollection = results else {
                fatalError("*** An error has occurred while calcuating the statistics: \(error?.localizedDescription) ***")
            }
            
            var startDate: Date
            let endDate = Date()
            
            if let formattedStartDate = formatter.date(from: startDateString) {
                startDate = formattedStartDate
            } else {
                startDate = (calendar as NSCalendar).date(byAdding: .day, value: -30, to: endDate, options: [])!
            }
            
            statsCollection.enumerateStatistics(from: startDate, to: endDate, with: { (statistics, stop) in
                if let quantity = statistics.sumQuantity() {
                    let date = resultsDateFormatter.string(from: statistics.startDate)
                    stepResults.append(["date": date, "source": "healthkit", "steps": quantity.doubleValue(for: HKUnit.count())])
                }
            })
            
            completion(stepResults, nil)
        }
        
        healthStore.execute(stepsQuery)
    }
    

    func enableBackgroundDelivery() {
        
        let steps = HKObjectType.quantityType(forIdentifier: HKQuantityTypeIdentifier.stepCount)!
        healthStore.enableBackgroundDelivery(for: steps, frequency: .hourly) { (success, error) in
            if (error != nil) {
                print("An error has occurred")
            }
        }
        
        let activeCalories = HKObjectType.quantityType(forIdentifier: HKQuantityTypeIdentifier.activeEnergyBurned)!
        healthStore.enableBackgroundDelivery(for: activeCalories, frequency: .hourly) { (success, error) in
            if (error != nil) {
                print("An error has occurred")
            }
        }
        
    }

    func disableBackgroundDelivery() {
        healthStore.disableAllBackgroundDelivery { (success, error) in
            if error != nil {
                print("An error has occurred")
            }
        }
    }
}
