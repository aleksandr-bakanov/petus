import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

enum CemeteryNavigation: Hashable {
    case petDetails(petId: Int64)
}

struct CemeteryScreen: View {
    
    @StateViewModel var viewModel: CemeteryScreenViewModel = CemeteryScreenViewModel()
    @State private var navigationPath: [CemeteryNavigation] = []
    
    var body: some View {
        NavigationStack(path: $navigationPath) {
            VStack {
                if let state = viewModel.uiState.value {
                    List(state.pets, id: \.pet.id) { data in
                        PetListCell(data: data,
                                    onClick: { viewModel.onAction(action: CemeteryScreenViewModelActionTapOnPet(petId: data.pet.id)) }
                        ).listRowInsets(EdgeInsets())
                    }
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
