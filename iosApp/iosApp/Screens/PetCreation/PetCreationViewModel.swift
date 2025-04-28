import SwiftUI
import shared

@MainActor final class PetCreationViewModel: ObservableObject {
    
    let koinHelper: KoinHelper = KoinHelper()
    
    @Binding var isShowingPetCreation: Bool
    @Published var name: String = ""
    @Published var type: PetType = PetType.dogus
    @Published var typeDescription: String
    
    init(isShowingPetCreation: Binding<Bool>) {
        _isShowingPetCreation = isShowingPetCreation
        typeDescription = koinHelper.getPetTypeDescription(type: PetType.dogus)
    }
    
    func setNewType(type: PetType) {
        self.type = type
        typeDescription = koinHelper.getPetTypeDescription(type: type)
    }
    
    func createPet() {
        if (!name.isEmpty) {
            Task {
                try await koinHelper.createNewPet(name: name, type: type)
                isShowingPetCreation = false
            }
        }
    }
}
