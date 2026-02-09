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
                        LanguageKnowledgeCell(type: .catus, value: state.languageKnowledgeCatus)
                        LanguageKnowledgeCell(type: .dogus, value: state.languageKnowledgeDogus)
                        LanguageKnowledgeCell(type: .frogus, value: state.languageKnowledgeFrogus)
                        LanguageKnowledgeCell(type: .bober, value: state.languageKnowledgeBober)
                        LanguageKnowledgeCell(type: .fractal, value: state.languageKnowledgeFractal)
                        LanguageKnowledgeCell(type: .dragon, value: state.languageKnowledgeDragon)
                        LanguageKnowledgeCell(type: .alien, value: state.languageKnowledgeAlien)
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
                        ForEach(state.abilities, id: \.self) { item in
                            Text(item.toStringId().localized)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
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
