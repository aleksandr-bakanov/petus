import SwiftUI

struct MainScreen: View {
    
    @State private var selectedTab = 2
    
    var body: some View {
        TabView(selection: $selectedTab) {
            CemeteryView()
                .tabItem {
                    Label("Cemetery", systemImage: "plus")
                }
                .tag(1)
            ZooView()
                .tabItem {
                    Label("Zoo", systemImage: "house")
                }
                .tag(2)
            WeatherReportView()
                .tabItem {
                    Label("Weather", systemImage: "cloud")
                }
                .tag(3)
        }
    }
}
