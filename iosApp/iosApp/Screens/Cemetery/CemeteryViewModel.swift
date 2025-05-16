import SwiftUI
import shared

enum CemeteryNavigation: Hashable {
    case petDetails(petId: Int64)
}

@MainActor final class CemeteryViewModel: ObservableObject {
    
    let koinHelper: KoinHelper = KoinHelper()
    let petImageUseCase = PetImageUseCase()
    
    @Published var pets: [PetThumbnailUiData] = []
    @Published var navigationPath: [CemeteryNavigation] = []
    
    func loadPets() async {
        let petsFlow = koinHelper.getAllPetsInCemeteryFlow()
        for await pets in petsFlow {
            self.pets = pets.map { pet in
                PetThumbnailUiData(petImageName: petImageUseCase.getPetImageName(for: pet),
                                   pet: pet,
                                   satietyFraction: CGFloat(koinHelper.getPetSatietyFraction(pet: pet)),
                                   psychFraction: CGFloat(koinHelper.getPetPsychFraction(pet: pet)),
                                   healthFraction: CGFloat(koinHelper.getPetHealthFraction(pet: pet)))
            }
        }
    }
    
    func tapOnPet(pet: Pet) {
        navigationPath.append(.petDetails(petId: pet.id))
    }
}
