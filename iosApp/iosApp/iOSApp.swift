import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        KoinHelperKt.doInitKoin()
        
        LocationHelperKt.doInitLocationManager()
    }
    
	var body: some Scene {
		WindowGroup {
            ContentView(viewModel: .init())
		}
	}
}
