import SwiftUI
import CoreLocation
import BackgroundTasks
import shared

@available(iOS 17.0, *) // To be able to use .onChange(of: scenePhase)
@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var scenePhase
    
    let locationBackgroundTaskManager: LocationBackgroundTaskManager
    let koinHelper: KoinHelper
    
    init() {
        KoinHelperKt.doInitKoin()
        
        locationBackgroundTaskManager = LocationBackgroundTaskManager()
        locationBackgroundTaskManager.checkIfBackgroundTaskExists()
        
        koinHelper = KoinHelper()
    }
    
	var body: some Scene {
		WindowGroup {
            MainScreen()
		}
        .onChange(of: scenePhase) {
            if scenePhase == .active {
                Task {
                    try await koinHelper.applicationDidBecomeActive()
                    
                    if (try await koinHelper.isTimeToFetchWeather().boolValue) {
                        locationBackgroundTaskManager.requestLocation()
                    }
                }
            }
        }
	}
}
