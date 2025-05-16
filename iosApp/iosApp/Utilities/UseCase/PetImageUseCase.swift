import SwiftUI
import shared

class PetImageUseCase {
    private let engine: KoinHelper = KoinHelper()
    
    func getPetImageName(for pet: Pet) -> String {
        if pet.burialType == BurialType.exhumated { return "dug_out_grave" }
        switch pet.type {
        case .catus:
            switch pet.ageState {
            case .egg:
                return "catus_egg"
            case .newBorn:
                if pet.bodyState == BodyState.dead { return "catus_newborn_dead" }
                if pet.sleep { return "catus_newborn_sleep" }
                if pet.isPooped { return "catus_newborn_poop" }
                if pet.illness { return "catus_newborn_ill" }
                if isPetHungryOrPsych(pet: pet) { return "catus_newborn_hungry" }
                if isPetLowHealth(pet: pet) { return "catus_newborn_ill" }
                else { return "catus_newborn_active" }
            case .teen, .adult:
                if pet.bodyState == BodyState.dead { return "catus_adult_dead" }
                if pet.sleep { return "catus_adult_sleep" }
                if pet.isPooped { return "catus_adult_poop" }
                if pet.illness { return "catus_adult_ill" }
                if isPetHungryOrPsych(pet: pet) { return "catus_adult_hungry" }
                if isPetLowHealth(pet: pet) { return "catus_adult_ill" }
                else { return "catus_adult_active" }
            case .old:
                if pet.bodyState == BodyState.dead { return "catus_old_dead" }
                if pet.sleep { return "catus_old_sleep" }
                if pet.isPooped { return "catus_old_poop" }
                if pet.illness { return "catus_old_ill" }
                if isPetHungryOrPsych(pet: pet) { return "catus_old_hungry" }
                if isPetLowHealth(pet: pet) { return "catus_old_ill" }
                else { return "catus_old_active" }
            }
            
        case .dogus:
            switch pet.ageState {
            case .egg:
                return "dogus_egg"
            case .newBorn:
                if pet.bodyState == BodyState.dead { return "dogus_dead" }
                if pet.sleep { return "dogus_newborn_sleep" }
                if pet.isPooped { return "dogus_newborn_poop" }
                if pet.illness { return "dogus_newborn_ill" }
                if isPetHungryOrPsych(pet: pet) { return "dogus_newborn_hungry" }
                if isPetLowHealth(pet: pet) { return "dogus_newborn_ill" }
                else { return "dogus_newborn_active" }
            case .teen, .adult:
                if pet.bodyState == BodyState.dead { return "dogus_dead" }
                if pet.sleep { return "dogus_adult_sleep" }
                if pet.isPooped { return "dogus_adult_poop" }
                if pet.illness { return "dogus_adult_ill" }
                if isPetHungryOrPsych(pet: pet) { return "dogus_adult_hungry" }
                if isPetLowHealth(pet: pet) { return "dogus_adult_ill" }
                else { return "dogus_adult_active" }
            case .old:
                if pet.bodyState == BodyState.dead { return "dogus_dead" }
                if pet.sleep { return "dogus_old_sleep" }
                if pet.isPooped { return "dogus_old_poop" }
                if pet.illness { return "dogus_old_ill" }
                if isPetHungryOrPsych(pet: pet) { return "dogus_old_hungry" }
                if isPetLowHealth(pet: pet) { return "dogus_old_ill" }
                else { return "dogus_old_active" }
            }
            
        case .frogus:
            switch pet.ageState {
            case .egg:
                return "frogus_egg"
            case .newBorn:
                if pet.bodyState == BodyState.dead { return "frogus_dead" }
                if pet.sleep { return "frogus_newborn_sleep" }
                if pet.isPooped { return "frogus_newborn_poop" }
                if pet.illness { return "frogus_newborn_ill" }
                if isPetHungryOrPsych(pet: pet) { return "frogus_newborn_hungry" }
                if isPetLowHealth(pet: pet) { return "frogus_newborn_ill" }
                else { return "frogus_newborn_active" }
            case .teen, .adult:
                if pet.bodyState == BodyState.dead { return "frogus_dead" }
                if pet.sleep { return "frogus_adult_sleep" }
                if pet.isPooped { return "frogus_adult_poop" }
                if pet.illness { return "frogus_adult_ill" }
                if isPetHungryOrPsych(pet: pet) { return "frogus_adult_hungry" }
                if isPetLowHealth(pet: pet) { return "frogus_adult_ill" }
                else { return "frogus_adult_active" }
            case .old:
                if pet.bodyState == BodyState.dead { return "frogus_dead" }
                if pet.sleep { return "frogus_old_sleep" }
                if pet.isPooped { return "frogus_old_poop" }
                if pet.illness { return "frogus_old_ill" }
                if isPetHungryOrPsych(pet: pet) { return "frogus_old_hungry" }
                if isPetLowHealth(pet: pet) { return "frogus_old_ill" }
                else { return "frogus_old_active" }
            }
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
