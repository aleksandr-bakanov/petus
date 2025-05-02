import SwiftUI

struct ZooView: View {
    
    @StateObject var viewModel = ZooViewModel()
    
    var body: some View {
        NavigationStack(path: $viewModel.navigationPath) {
            VStack {
                List(viewModel.pets, id: \.pet.id) { data in
                    PetListCell(data: data,
                                onClick: { viewModel.tapOnPet(pet: data.pet) }
                    ).listRowInsets(EdgeInsets())
                }
                
                ActionButton(title: "Create new pet", backgroundColor: .accentColor) {
                    viewModel.tapOnCreateNewPet()
                }
            }
            .navigationTitle("Zoo")
            .navigationDestination(for: ZooNavigation.self) { destination in
                switch destination {
                case .petDetails(let petId):
                    PetDetailsScreen(petId: petId)
                case .createPet:
                    PetCreationScreen()
                }
            }
        }
        .task {
            await viewModel.loadPets()
        }
    }
}
