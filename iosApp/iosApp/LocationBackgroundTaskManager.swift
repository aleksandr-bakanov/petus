import BackgroundTasks
import CoreLocation
import shared

class LocationBackgroundTaskManager: NSObject {
    
    let BACKGROUND_TASK_ID = "bav.petus.weather_update"
    
    let koinHelper = KoinHelper()
    
    let locationManager = CLLocationManager()
    var currentTask: BGAppRefreshTask?

    override init() {
        super.init()
        
        // Configure CLLocationManager
        locationManager.delegate = self
        locationManager.requestAlwaysAuthorization() // Make sure this is done before requesting location
    }
    
    func checkIfBackgroundTaskExists() {
        Task {
            let requests = await BGTaskScheduler.shared.pendingTaskRequests()
            print("checkIfBackgroundTaskExists requests = " + requests.description + "; size = " + requests.count.description)
            if (requests.isEmpty) {
                registerAndScheduleBackgroundTask()
            }
        }
    }
    
    func registerAndScheduleBackgroundTask() {
        registerBackgroundTask()
        scheduleBackgroundTask()
    }
    
    func registerBackgroundTask() {
        let result = BGTaskScheduler.shared.register(forTaskWithIdentifier: BACKGROUND_TASK_ID, using: nil) { task in
            self.handleAppRefresh(task: task as! BGAppRefreshTask)
        }
        print("registerBackgroundTask result = " + result.description)
    }

    func scheduleBackgroundTask() {
        let request = BGAppRefreshTaskRequest(identifier: BACKGROUND_TASK_ID)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 3 * 60 * 60) // Start in 3 hours
        
        do {
            // Submitting a task request for an unexecuted task that’s already in the queue replaces the previous task request.
            // There can be a total of 1 refresh task and 10 processing tasks scheduled at any time. Trying to schedule more tasks returns BGTaskScheduler.Error.Code.tooManyPendingTaskRequests.
            try BGTaskScheduler.shared.submit(request)
        } catch {
            print("Failed to submit background task: \(error)")
        }
        
        print("scheduleBackgroundTask end")
    }
    
    func handleAppRefresh(task: BGAppRefreshTask) {
        currentTask = task
        // Request location once
        requestLocation()

        task.expirationHandler = {
            // Handle expiration
            print("Background task expired.")
        }
        
        // Don't call task.setTaskCompleted yet because we need to wait for the location update
    }
    
    func requestLocation() {
        locationManager.requestLocation()
    }
}

extension LocationBackgroundTaskManager: CLLocationManagerDelegate {
    
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        switch manager.authorizationStatus {
        case .authorizedWhenInUse:  // Location services are available.
            print("locationManagerDidChangeAuthorization authorizedWhenInUse")
            manager.requestAlwaysAuthorization()
            break
            
        case .authorizedAlways:
            print("locationManagerDidChangeAuthorization authorizedAlways")
            break
                
        case .restricted, .denied:  // Location services currently unavailable.
            print("locationManagerDidChangeAuthorization restricted or denied")
            break
            
        case .notDetermined:        // Authorization not determined yet.
            print("locationManagerDidChangeAuthorization notDetermined")
            manager.requestWhenInUseAuthorization()
            break
            
        default:
            break
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let location = locations.last {
            // Handle the location here
            print("Location in background: \(location.coordinate)")
            
            Task {
                let latitude: Double = location.coordinate.latitude
                let longitude: Double = location.coordinate.longitude
                do {
                    try await koinHelper.retrieveWeatherInBackground(
                        latitude: KotlinDouble(double: latitude),
                        longitude: KotlinDouble(double: longitude),
                        info: nil
                    )
                    
                    currentTask?.setTaskCompleted(success: true)
                    currentTask = nil
                    checkIfBackgroundTaskExists()
                } catch {
                    print(error.localizedDescription)
                    currentTask?.setTaskCompleted(success: false)
                    currentTask = nil
                    checkIfBackgroundTaskExists()
                }
            }
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print("Failed to get location: \(error)")
        // Complete the task even if location retrieval failed
        currentTask?.setTaskCompleted(success: false)
        currentTask = nil
        checkIfBackgroundTaskExists()
    }
}
