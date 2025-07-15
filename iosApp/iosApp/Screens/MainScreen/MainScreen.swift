import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

struct MainScreen: View {
    
    @StateViewModel var viewModel: MainViewModel = MainViewModel()
    
    @State private var selectedTab = 2
    
    var body: some View {
        ZStack {
            if let state = viewModel.uiState.value {
                TabView(selection: $selectedTab) {
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
                    QuestStatusView()
                        .tabItem {
                            Label("QuestsScreenTitle", systemImage: "info.circle")
                        }
                        .tag(4)
                }
                
                if let gus = state.gameUpdateState {
                    VStack {
                        StatBar(color: Color("SatietyColor"),
                                fraction: CGFloat(gus.fraction),
                                tweenDuration: 0.1,
                                frameHeight: 4
                        )
                        Spacer()
                    }
                }
                
                if !state.notifications.isEmpty {
                    VStack(alignment: .leading, spacing: 0) {
                        ForEach(state.notifications, id: \.id) { notification in
                            UserNotificationCell(notification: notification) { id in
                                viewModel.onAction(action: MainViewModelActionTapOnNotification(id: notification.id))
                            }
                        }
                        Spacer()
                    }
                }
            }
        }
    }
}
