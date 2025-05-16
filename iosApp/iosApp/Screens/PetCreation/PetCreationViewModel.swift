import SwiftUI
import shared

@MainActor final class PetCreationViewModel: ObservableObject {
    
    let koinHelper: KoinHelper = KoinHelper()
    let stringResourcesUseCase = StringResourcesUseCase()
    
    var onFinish: (() -> Void)?
    
    @Published var name: String = ""
    @Published var type: PetType = PetType.dogus
    @Published var typeDescription: String
    @Published var availablePetTypes: [PetType] = []
    
    init() {
        typeDescription = stringResourcesUseCase.getString(id: koinHelper.getPetTypeDescription(type: PetType.dogus))
    }
    
    func loadData() async {
        do {
            availablePetTypes = try await Array(koinHelper.getAvailablePetTypes())
        }
        catch {
            
        }
    }
    
    func setNewType(type: PetType) {
        self.type = type
        typeDescription = stringResourcesUseCase.getString(id: koinHelper.getPetTypeDescription(type: type))
    }
    
    func createPet() {
        if (!name.isEmpty) {
            Task {
                try await koinHelper.createNewPet(name: name, type: type)
                onFinish?()
            }
        }
    }
}
