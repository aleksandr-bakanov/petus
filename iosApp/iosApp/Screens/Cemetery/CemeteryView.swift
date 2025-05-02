import SwiftUI

struct CemeteryView: View {
    
    @StateObject var viewModel = CemeteryViewModel()
    
    var body: some View {
        NavigationStack(path: $viewModel.navigationPath) {
            VStack {
                List(viewModel.pets, id: \.pet.id) { data in
                    PetListCell(data: data,
                                onClick: { viewModel.tapOnPet(pet: data.pet) }
                    ).listRowInsets(EdgeInsets())
                }
            }
            .navigationTitle("Cemetery")
            .navigationDestination(for: CemeteryNavigation.self) { destination in
                switch destination {
                case .petDetails(let petId):
                    PetDetailsScreen(petId: petId)
                }
            }
            .task {
                await viewModel.loadPets()
            }
        }
    }
}
