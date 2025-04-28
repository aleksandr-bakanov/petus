import SwiftUI
import shared

struct PetDetailsUiState {
    var title: String = ""
    var petType: PetType = PetType.frogus
    var petImage: String = ""
    var creationTime: String = ""
    var ageState: String = ""
    var sleepState: String = ""
    var satiety: String = ""
    var psych: String = ""
    var health: String = ""
    var satietyFraction: CGFloat = 0
    var psychFraction: CGFloat = 0
    var healthFraction: CGFloat = 0
    var illness: String = ""
    var pooped: String = ""
    var timeOfDeath: String = ""
}

enum PetDetailsScreenAction {
    case tapFeedButton
    case tapHealButton
    case tapPlayButton
    case tapPoopButton
    case tapCloseButton
}

@MainActor final class PetDetailsViewModel: ObservableObject {
    
    var petId: Int64
    
    init(petId: Int64) {
        self.petId = petId
    }
    
    let koinHelper: KoinHelper = KoinHelper()
    let petImageUseCase = PetImageUseCase()
    
    @Published var uiState: PetDetailsUiState = PetDetailsUiState()
    @Published var showPlayButton: Bool = false
    @Published var showHealButton: Bool = false
    @Published var showPoopButton: Bool = false
    @Published var showFeedButton: Bool = false
    @Published var showWakeUpButton: Bool = false
    
    func isAnyButtonShown() -> Bool {
        return showPlayButton || showHealButton || showPoopButton || showFeedButton || showWakeUpButton
    }
    
    var currentPet: Pet? = nil
    
    func loadPet() async {
        let petFlow = koinHelper.getPetByIdFlow(petId: petId)
        for await pet in petFlow {
            if let p = pet {
                currentPet = p
                uiState = PetDetailsUiState(title: "\(p.type.name) \(p.name)",
                                            petType: p.type,
                                            petImage: petImageUseCase.getPetImageName(for: p),
                                            creationTime: "Born: \(p.creationTime.epochTimeToString)",
                                            ageState: "Age state: \(p.ageState.name)",
                                            sleepState: getSleepActiveStateString(pet: p),
                                            satiety: "Satiety: \(p.satiety)",
                                            psych: "Psych: \(p.psych)",
                                            health: "Health: \(p.health)",
                                            satietyFraction: CGFloat(koinHelper.getPetSatietyFraction(pet: p)),
                                            psychFraction: CGFloat(koinHelper.getPetPsychFraction(pet: p)),
                                            healthFraction: CGFloat(koinHelper.getPetHealthFraction(pet: p)),
                                            illness: "Illness: \(p.illness)",
                                            pooped: "Pooped: \(p.isPooped)",
                                            timeOfDeath: "Time of death: \(p.timeOfDeath.epochTimeToString)"
                )
                showPlayButton = koinHelper.isAllowedToPlayWithPet(pet: p)
                showHealButton = koinHelper.isAllowedToHealPet(pet: p)
                showPoopButton = koinHelper.isAllowedToCleanAfterPet(pet: p)
                showFeedButton = koinHelper.isAllowedToFeedPet(pet: p)
                showWakeUpButton = koinHelper.isAllowedToWakeUpPet(pet: p)
            }
        }
    }
    
    private func getSleepActiveStateString(pet: Pet) -> String {
        let action = pet.sleep ? "will awake at" : "will fall asleep at"
        let timestamp = koinHelper.getNextSleepStateChangeTimestampString(pet: pet)

        return "Sleep or active: \(pet.activeSleepState.name) (\(action) \(timestamp))"
    }
    
    func feedPet() {
        if let p = currentPet {
            Task {
                try await koinHelper.feedPet(pet: p)
            }
        }
    }
    
    func playWithPet() {
        if let p = currentPet {
            Task {
                try await koinHelper.playWithPet(pet: p)
            }
        }
    }
    
    func cleanAfterPet() {
        if let p = currentPet {
            Task {
                try await koinHelper.cleanAfterPet(pet: p)
            }
        }
    }
    
    func healPetIllness() {
        if let p = currentPet {
            Task {
                try await koinHelper.healPetIllness(pet: p)
            }
        }
    }
    
    func wakeUpPet() {
        if let p = currentPet {
            Task {
                try await koinHelper.wakeUpPet(pet: p)
            }
        }
    }
}
