import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

enum CemeteryNavigation: Hashable {
    case petDetails(petId: Int64)
}

struct CemeteryScreen: View {
    
    @StateViewModel var viewModel: CemeteryScreenViewModel = CemeteryScreenViewModel()
    @State private var navigationPath: [CemeteryNavigation] = []
    
    // Two-column grid layout
    private let columns = [
        GridItem(.flexible(), spacing: 16),
        GridItem(.flexible(), spacing: 16)
    ]
    
    var body: some View {
        NavigationStack(path: $navigationPath) {
            ScrollView {
                if let state = viewModel.uiState.value {
                    LazyVGrid(columns: columns, spacing: 16) {
                        ForEach(state.pets, id: \.pet.id) { data in
                            PetCemeteryListCell(data: data) {
                                viewModel.onAction(action: CemeteryScreenViewModelActionTapOnPet(petId: data.pet.id))
                            }
                        }
                    }
                    .padding(16)
                }
            }
            .navigationDestination(for: CemeteryNavigation.self) { destination in
                switch destination {
                case .petDetails(let petId):
                    PetDetailsScreen(petId: petId) { petId in
                        // Cannot speak to pets from cemetery
                    }
                }
            }
            .task {
                for await navigation in viewModel.navigation {
                    switch navigation {
                    case is CemeteryScreenViewModelNavigationToDetails:
                        if let nav = navigation as? CemeteryScreenViewModelNavigationToDetails {
                            navigationPath.append(.petDetails(petId: nav.petId))
                        }
                    default:
                        break
                    }
                }
            }
        }
    }
}
