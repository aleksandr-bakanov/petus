import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

enum UserProfileNavigation: Hashable {
    case itemDetails(itemId: InventoryItemId)
}

struct UserProfileView: View {
    
    let showOnboardingLambda: () -> Void
    
    @StateViewModel var viewModel: UserProfileScreenViewModel = UserProfileScreenViewModel()
    @State private var navigationPath: [UserProfileNavigation] = []
    
    // Define 3 fixed-width columns
    private let inventoryColumns: [GridItem] = Array(
        repeating: GridItem(.flexible(), spacing: 8),
        count: 3
    )
    private let languagesColumns: [GridItem] = Array(
        repeating: GridItem(.flexible(), spacing: 8),
        count: 4
    )
    
    var body: some View {
        NavigationStack(path: $navigationPath) {
            ScrollView {
                if let state = viewModel.uiState.value {
                    VStack(alignment: .center, spacing: 16) {
                        if let weather = state.latestWeather {
                            Text(weather).font(.caption)
                        }
                        Image("user_profile_avatar")
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .frame(width: 112, height: 112)
                            .clipped()
                        Button(action: showOnboardingLambda) {
                            Text(NSLocalizedString("OnboardingHowToButtonTitle", comment: ""))
                                .font(.footnote)
                                .foregroundColor(.white)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 8)
                        }
                            .background(Color("PsychColor"))
                            .cornerRadius(8)
                            .fixedSize()
                        Text("ProfileScreenLanguagesLabel")
                        LazyVGrid(
                            columns: languagesColumns,
                            alignment: .center,
                            spacing: 8
                        ) {
                            ForEach(state.languages, id: \.type) { item in
                                LanguageCell(type: item.type, percentage: CGFloat(item.percentage))
                            }
                        }
                        .frame(maxHeight: .infinity) // Similar to heightIn max = 10000.dp
                        .padding(8) // Optional: for outer spacing
                        
                        Text("ProfileScreenInventoryLabel")
                        
                        LazyVGrid(
                            columns: inventoryColumns,
                            alignment: .center,
                            spacing: 8
                        ) {
                            ForEach(state.inventory, id: \.id) { item in
                                InventoryItemCell(item: item) {
                                    navigationPath.append(.itemDetails(itemId: item.id))
                                }
                            }
                        }
                        .frame(maxHeight: .infinity) // Similar to heightIn max = 10000.dp
                        .padding(8) // Optional: for outer spacing

                        Text("ProfileScreenAbilitiesLabel")
                        LazyVGrid(
                            columns: inventoryColumns,
                            alignment: .center,
                            spacing: 8
                        ) {
                            ForEach(state.abilities, id: \.ability) { item in
                                AbilityItemCell(item: item) {
                                    navigationPath.append(.itemDetails(itemId: item.ability))
                                }
                            }
                        }
                        .frame(maxHeight: .infinity) // Similar to heightIn max = 10000.dp
                        .padding(8) // Optional: for outer spacing
                        
                        Text("ProfileScreenMiscLabel")
                        CanPetsDieOfOldAgeRow(value: state.canPetsDieOfOldAge) { newValue in
                            viewModel.onAction(action: UserProfileScreenViewModelActionTapCanPetDieOfOldAgeSwitch(value: newValue))
                        }
                    }
                    .frame(maxWidth: .infinity) // Center horizontally
                    .padding()
                }
            }
            .navigationDestination(for: UserProfileNavigation.self) { destination in
                switch destination {
                case .itemDetails(let itemId):
                    ItemDetailsScreen(itemId: itemId)
                }
            }
        }
    }
}
