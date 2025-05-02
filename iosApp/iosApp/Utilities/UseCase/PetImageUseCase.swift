import SwiftUI
import shared

class PetImageUseCase {
    private let engine: KoinHelper = KoinHelper()

    func getPetImageName(for pet: Pet) -> String {
        switch pet.type {
        case .catus:
            return imageName(for: pet, prefix: "catus")
        case .dogus:
            return imageName(for: pet, prefix: "dogus")
        case .frogus:
            return imageName(for: pet, prefix: "frogus")
        }
    }

    private func imageName(for pet: Pet, prefix: String) -> String {
        let suffix: String

        switch pet.ageState {
        case .egg:
            suffix = "egg"
        case .newBorn:
            suffix = newbornImageSuffix(for: pet)
        case .teen:
            suffix = teenImageSuffix(for: pet)
        case .adult:
            suffix = adultImageSuffix(for: pet)
        case .old:
            suffix = oldImageSuffix(for: pet)
        }

        return "\(prefix)_\(suffix)"
    }

    private func newbornImageSuffix(for pet: Pet) -> String {
        if pet.sleep {
            return "newborn_sleep"
        } else if pet.isPooped {
            return "newborn_poop"
        } else if pet.illness {
            return "newborn_ill"
        } else if isPetHungryOrPsych(pet: pet) {
            return "newborn_hungry"
        } else if isPetLowHealth(pet: pet) {
            return "newborn_ill"
        } else {
            return "newborn_active"
        }
    }

    private func teenImageSuffix(for pet: Pet) -> String {
        if pet.sleep {
            return "teen_sleep"
        } else if pet.isPooped {
            return "teen_poop"
        } else if pet.illness {
            return "teen_ill"
        } else if isPetHungryOrPsych(pet: pet) {
            return "teen_hungry"
        } else if isPetLowHealth(pet: pet) {
            return "teen_ill"
        } else {
            return "teen_active"
        }
    }

    private func adultImageSuffix(for pet: Pet) -> String {
        if pet.sleep {
            return "adult_sleep"
        } else if pet.isPooped {
            return "adult_poop"
        } else if pet.illness {
            return "adult_ill"
        } else if isPetHungryOrPsych(pet: pet) {
            return "adult_hungry"
        } else if isPetLowHealth(pet: pet) {
            return "adult_ill"
        } else {
            return "adult_active"
        }
    }

    private func oldImageSuffix(for pet: Pet) -> String {
        if pet.sleep {
            return "old_sleep"
        } else if pet.isPooped {
            return "old_poop"
        } else if pet.illness {
            return "old_ill"
        } else if isPetHungryOrPsych(pet: pet) {
            return "old_hungry"
        } else if isPetLowHealth(pet: pet) {
            return "old_ill"
        } else {
            return "old_active"
        }
    }

    /**
     * Based on Engine.getHealthChange calculation
     * where zero point is defined as 2/3 of full satiety or psych
     */
    private func isPetHungryOrPsych(pet: Pet) -> Bool {
        return engine.getPetPsychFraction(pet: pet) < 0.66 ||
               engine.getPetHealthFraction(pet: pet) < 0.66
    }
    
    private func isPetLowHealth(pet: Pet) -> Bool {
        return engine.getPetHealthFraction(pet: pet) < 0.5
    }
}
