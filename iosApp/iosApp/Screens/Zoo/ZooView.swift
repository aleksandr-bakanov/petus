import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

enum ZooNavigation: Hashable {
    case petDetails(petId: Int64)
    case createPet
    case dialog(petId: Int64)
}

struct ZooView: View {
    
    @StateViewModel var viewModel: ZooScreenViewModel = ZooScreenViewModel()
    @State private var navigationPath: [ZooNavigation] = []

    var body: some View {
        NavigationStack(path: $navigationPath) {
            VStack(spacing: 0) {
                if let state = viewModel.uiState.value {
                    // Pets list
                    ScrollView {
                        LazyVStack(spacing: 4) {
                            ForEach(state.pets, id: \.pet.id) { data in
                                PetListCell(data: data) {
                                    navigationPath.append(.petDetails(petId: data.pet.id))
                                }
                            }
                        }
                        .padding(8)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    
                    // Create pet button (conditionally)
                    if state.showCreateNewPetButton {
                        ActionButton(title: NSLocalizedString("CreatePetButtonTitle", comment: ""),
                                     backgroundColor: .accentColor) {
                            navigationPath.append(.createPet)
                        }
                    }
                }
            }
            .navigationDestination(for: ZooNavigation.self) { destination in
                switch destination {
                case .petDetails(let petId):
                    PetDetailsScreen(petId: petId) { petId in
                        navigationPath.append(.dialog(petId: petId))
                    }
                case .createPet:
                    PetCreationScreen()
                case .dialog(let petId):
                    DialogScreen(id: petId)
                }
            }
        }
        .task {
            for await navigation in viewModel.navigation {
                switch navigation {
                case is ZooScreenViewModelNavigationToDetails:
                    if let nav = navigation as? ZooScreenViewModelNavigationToDetails {
                        navigationPath.append(.petDetails(petId: nav.petId))
                    }
                case is ZooScreenViewModelNavigationToNewPetCreation:
                    navigationPath.append(.createPet)
                default:
                    break
                }
            }
        }
    }
}
