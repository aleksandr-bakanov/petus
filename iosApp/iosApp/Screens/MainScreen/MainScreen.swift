import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

struct MainScreen: View {
    
    @StateViewModel var viewModel: MainViewModel = MainViewModel()
    
    @State private var selectedTab = 2
    
    var body: some View {
        TabView(selection: $selectedTab) {
            if let state = viewModel.uiState.value {
                if state.showCemetery == true {
                    CemeteryScreen()
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
                UserProfileView()
                    .tabItem {
                        Label("Profile", systemImage: "face.smiling")
                    }
                    .tag(3)
            }
        }
        
    }
}
