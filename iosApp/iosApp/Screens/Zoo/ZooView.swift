import SwiftUI

struct ZooView: View {
    
    @StateObject var viewModel = ZooViewModel()
    
    var body: some View {
        ZStack {
            NavigationView {
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
            }
            .task {
                await viewModel.loadPets()
            }
            
            if viewModel.isShowingDetails {
                PetDetailsScreen(petId: viewModel.selectedPet?.id ?? 0,
                               isShowingDetail: $viewModel.isShowingDetails
                )
            }
            
            if viewModel.isShowingPetCreation {
                PetCreationScreen(isShowingPetCreation: $viewModel.isShowingPetCreation)
            }
        }
    }
}
