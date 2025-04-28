import SwiftUI

struct CemeteryView: View {
    
    @StateObject var viewModel = CemeteryViewModel()
    
    var body: some View {
        ZStack {
            NavigationView {
                VStack {
                    List(viewModel.pets, id: \.pet.id) { data in
                        PetListCell(data: data,
                                    onClick: { viewModel.tapOnPet(pet: data.pet) }
                        ).listRowInsets(EdgeInsets())
                    }
                }
                .navigationTitle("Cemetery")
            }
            .task {
                await viewModel.loadPets()
            }
            
            if viewModel.isShowingDetails {
                PetDetailsScreen(petId: viewModel.selectedPet?.id ?? 0,
                               isShowingDetail: $viewModel.isShowingDetails
                )
            }
        }
    }
}
