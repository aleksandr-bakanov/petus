import SwiftUI
import shared

struct PetDetailsUiState {
    var title: String = ""
    var petType: PetType = PetType.frogus
    var petImage: String = ""
    var creationTime: String = ""
    var satietyFraction: CGFloat = 0
    var psychFraction: CGFloat = 0
    var healthFraction: CGFloat = 0
    var timeOfDeath: String = ""
}

enum PetDetailsScreenAction {
    case tapFeedButton
    case tapHealButton
    case tapPlayButton
    case tapPoopButton
    case tapCloseButton
}

struct DialogData {
    var text: String
    var answers: [String]
}

@MainActor final class PetDetailsViewModel: ObservableObject {
    
    var petId: Int64
    
    init(petId: Int64) {
        self.petId = petId
    }
    
    let koinHelper: KoinHelper = KoinHelper()
    let petImageUseCase = PetImageUseCase()
    let stringResourcesUseCase = StringResourcesUseCase()
    
    @Published var uiState: PetDetailsUiState = PetDetailsUiState()
    @Published var dialogData: DialogData? = nil
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
                if currentPet == nil && pet != nil {
                    do {
                        try await koinHelper.emitQuestEvent(event: QuestSystemEventUserOpenPetDetails(pet: p))
                    } catch {}
                }
                currentPet = p
                uiState = PetDetailsUiState(title: p.name,
                                            petType: p.type,
                                            petImage: petImageUseCase.getPetImageName(for: p),
                                            creationTime: "Born: \(p.creationTime.epochTimeToString)",
                                            satietyFraction: CGFloat(koinHelper.getPetSatietyFraction(pet: p)),
                                            psychFraction: CGFloat(koinHelper.getPetPsychFraction(pet: p)),
                                            healthFraction: CGFloat(koinHelper.getPetHealthFraction(pet: p)),
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
    
    func startDialog() {
        if let p = currentPet {
            Task {
                if let node = try await koinHelper.startDialog(pet: p) {
                    dialogData = await makeDialogData(node: node)
                }
                else {
                    dialogData = nil
                }
            }
        }
    }
    
    func chooseDialogAnswer(index: Int32) {
        Task {
            if let node = try await koinHelper.chooseDialogAnswer(index: index) {
                dialogData = await makeDialogData(node: node)
            }
            else {
                dialogData = nil
            }
        }
    }
    
    private func makeDialogData(node: DialogNode?) async -> DialogData? {
        guard let node = node else { return nil }
        guard let p = currentPet else { return nil }
        
        do {
            let maskedText = try await koinHelper.maskDialogText(
                petType: p.type,
                text: stringResourcesUseCase.getString(id: node.text)
            )

            let answers = node.answers.map {
                stringResourcesUseCase.getString(id: $0.text)
            }

            return DialogData(
                text: maskedText,
                answers: answers
            )
        } catch {
            return nil
        }
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
    
    func kill() {
        if let p = currentPet { Task { try await koinHelper.killPet(pet: p) } }
    }
    
    func resurrectPet() {
        if let p = currentPet { Task { try await koinHelper.resurrectPet(pet: p) } }
    }
    
    func changePetAgeState(state: AgeState) {
        if let p = currentPet { Task { try await koinHelper.changePetAgeState(pet: p, state: state) } }
    }
    
    func changePetPlace(place: Place) {
        if let p = currentPet { Task { try await koinHelper.changePetPlace(pet: p, place: place) } }
    }
}
