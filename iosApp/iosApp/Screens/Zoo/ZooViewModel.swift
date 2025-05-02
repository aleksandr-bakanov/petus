import SwiftUI
import shared

enum ZooNavigation: Hashable {
    case petDetails(petId: Int64)
    case createPet
}

@MainActor final class ZooViewModel: ObservableObject {
    
    let koinHelper: KoinHelper = KoinHelper()
    let petImageUseCase = PetImageUseCase()
    
    @Published var pets: [PetThumbnailUiData] = []
    @Published var navigationPath: [ZooNavigation] = []
    
    func loadPets() async {
        let petsFlow = koinHelper.getAllAlivePetsFlow()
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
    
    func tapOnCreateNewPet() {
        navigationPath.append(.createPet)
    }
}
