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
                            Label("CemeteryScreenTitle", systemImage: "plus")
                        }
                        .tag(1)
                }
                ZooView()
                    .tabItem {
                        Label("ZooScreenTitle", systemImage: "house")
                    }
                    .tag(2)
                UserProfileView()
                    .tabItem {
                        Label("ProfileScreenTitle", systemImage: "face.smiling")
                    }
                    .tag(3)
            }
        }
        
    }
}
