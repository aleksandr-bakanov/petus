import SwiftUI
import CoreLocation
import BackgroundTasks
import shared

class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(_ application: UIApplication, supportedInterfaceOrientationsFor window: UIWindow?) -> UIInterfaceOrientationMask {
        .portrait
    }
}

@available(iOS 17.0, *) // To be able to use .onChange(of: scenePhase)
@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor var appDelegate: AppDelegate
    
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
