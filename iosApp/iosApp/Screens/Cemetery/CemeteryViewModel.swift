import SwiftUI
import shared

@MainActor final class CemeteryViewModel: ObservableObject {
    
    let koinHelper: KoinHelper = KoinHelper()
    let petImageUseCase = PetImageUseCase()
    
    @Published var pets: [PetThumbnailUiData] = []
    
    @Published var isShowingDetails = false
    @Published var selectedPet: Pet?
    
    func loadPets() async {
        let petsFlow = koinHelper.getAllDeadPetsFlow()
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
        selectedPet = pet
        isShowingDetails = true
    }
}
