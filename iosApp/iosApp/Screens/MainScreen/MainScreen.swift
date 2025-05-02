import SwiftUI

struct MainScreen: View {
    
    @StateObject var viewModel = MainScreenViewModel()
    
    @State private var selectedTab = 2
    
    var body: some View {
        TabView(selection: $selectedTab) {
            if !viewModel.uiState.isInitial {
                if viewModel.uiState.showCemetery == true {
                    CemeteryView()
                        .tabItem {
                            Label("Cemetery", systemImage: "plus")
                        }
                        .tag(1)
                }
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
        .task {
            await viewModel.initialLoad()
        }
        
    }
}
