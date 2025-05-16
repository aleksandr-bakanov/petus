package bav.petus.android.ui.common

import bav.petus.android.R
import bav.petus.core.engine.Engine
import bav.petus.model.AgeState
import bav.petus.model.BodyState
import bav.petus.model.BurialType
import bav.petus.model.Pet
import bav.petus.model.PetType

class PetImageUseCase(
    private val engine: Engine,
) {
    fun getPetImageResId(pet: Pet): Int {
        if (pet.burialType == BurialType.Exhumated) return R.drawable.dug_out_grave
        return when (pet.type) {
            PetType.Catus -> {
                when (pet.ageState) {
                    AgeState.Egg -> R.drawable.catus_egg
                    AgeState.NewBorn -> {
                        when {
                            pet.bodyState == BodyState.Dead -> R.drawable.catus_newborn_dead
                            pet.sleep -> R.drawable.catus_newborn_sleep
                            pet.isPooped -> R.drawable.catus_newborn_poop
                            pet.illness -> R.drawable.catus_newborn_ill
                            isPetHungryOrPsych(pet) -> R.drawable.catus_newborn_hungry_or_psych
                            isPetLowHealth(pet) -> R.drawable.catus_newborn_ill
                            else -> R.drawable.catus_newborn_active
                        }
                    }
                    AgeState.Teen,
                    AgeState.Adult -> {
                        when {
                            pet.bodyState == BodyState.Dead -> R.drawable.catus_adult_dead
                            pet.sleep -> R.drawable.catus_adult_sleep
                            pet.isPooped -> R.drawable.catus_adult_poop
                            pet.illness -> R.drawable.catus_adult_ill
                            isPetHungryOrPsych(pet) -> R.drawable.catus_adult_hungry
                            isPetLowHealth(pet) -> R.drawable.catus_adult_ill
                            else -> R.drawable.catus_adult_active
                        }
                    }
                    AgeState.Old -> {
                        when {
                            pet.bodyState == BodyState.Dead -> R.drawable.catus_old_dead
                            pet.sleep -> R.drawable.catus_old_sleep
                            pet.isPooped -> R.drawable.catus_old_poop
                            pet.illness -> R.drawable.catus_old_ill
                            isPetHungryOrPsych(pet) -> R.drawable.catus_old_hungry
                            isPetLowHealth(pet) -> R.drawable.catus_old_ill
                            else -> R.drawable.catus_old_active
                        }
                    }
                }
            }
            PetType.Dogus -> {
                when (pet.ageState) {
                    AgeState.Egg -> R.drawable.dogus_egg
                    AgeState.NewBorn -> {
                        when {
                            pet.bodyState == BodyState.Dead -> R.drawable.dogus_dead
                            pet.sleep -> R.drawable.dogus_newborn_sleep
                            pet.isPooped -> R.drawable.dogus_newborn_poop
                            pet.illness -> R.drawable.dogus_newborn_ill
                            isPetHungryOrPsych(pet) -> R.drawable.dogus_newborn_hungry
                            isPetLowHealth(pet) -> R.drawable.dogus_newborn_ill
                            else -> R.drawable.dogus_newborn_active
                        }
                    }
                    AgeState.Teen,
                    AgeState.Adult -> {
                        when {
                            pet.bodyState == BodyState.Dead -> R.drawable.dogus_dead
                            pet.sleep -> R.drawable.dogus_adult_sleep
                            pet.isPooped -> R.drawable.dogus_adult_poop
                            pet.illness -> R.drawable.dogus_adult_ill
                            isPetHungryOrPsych(pet) -> R.drawable.dogus_adult_hungry
                            isPetLowHealth(pet) -> R.drawable.dogus_adult_ill
                            else -> R.drawable.dogus_adult_active
                        }
                    }
                    AgeState.Old -> {
                        when {
                            pet.bodyState == BodyState.Dead -> R.drawable.dogus_dead
                            pet.sleep -> R.drawable.dogus_old_sleep
                            pet.isPooped -> R.drawable.dogus_old_poop
                            pet.illness -> R.drawable.dogus_old_ill
                            isPetHungryOrPsych(pet) -> R.drawable.dogus_old_hungry
                            isPetLowHealth(pet) -> R.drawable.dogus_old_ill
                            else -> R.drawable.dogus_old_active
                        }
                    }
                }
            }
            PetType.Frogus -> {
                when (pet.ageState) {
                    AgeState.Egg -> R.drawable.frogus_egg
                    AgeState.NewBorn -> {
                        when {
                            pet.bodyState == BodyState.Dead -> R.drawable.frogus_dead
                            pet.sleep -> R.drawable.frogus_newborn_sleep
                            pet.isPooped -> R.drawable.frogus_newborn_poop
                            pet.illness -> R.drawable.frogus_newborn_ill
                            isPetHungryOrPsych(pet) -> R.drawable.frogus_newborn_hungry
                            isPetLowHealth(pet) -> R.drawable.frogus_newborn_ill
                            else -> R.drawable.frogus_newborn_active
                        }
                    }
                    AgeState.Teen,
                    AgeState.Adult -> {
                        when {
                            pet.bodyState == BodyState.Dead -> R.drawable.frogus_dead
                            pet.sleep -> R.drawable.frogus_adult_sleep
                            pet.isPooped -> R.drawable.frogus_adult_poop
                            pet.illness -> R.drawable.frogus_adult_ill
                            isPetHungryOrPsych(pet) -> R.drawable.frogus_adult_hungry
                            isPetLowHealth(pet) -> R.drawable.frogus_adult_ill
                            else -> R.drawable.frogus_adult_active
                        }
                    }
                    AgeState.Old -> {
                        when {
                            pet.bodyState == BodyState.Dead -> R.drawable.frogus_dead
                            pet.sleep -> R.drawable.frogus_old_sleep
                            pet.isPooped -> R.drawable.frogus_old_poop
                            pet.illness -> R.drawable.frogus_old_ill
                            isPetHungryOrPsych(pet) -> R.drawable.frogus_old_hungry
                            isPetLowHealth(pet) -> R.drawable.frogus_old_ill
                            else -> R.drawable.frogus_old_active
                        }
                    }
                }
            }
        }
    }

    /**
     * Based on [Engine.getHealthChange] calculation
     * where zero point is defined as 2/3 of full satiety of psych
     */
    private fun isPetHungryOrPsych(pet: Pet): Boolean {
        return engine.getPetPsychFraction(pet) < 0.66f ||
                engine.getPetSatietyFraction(pet) < 0.66f
    }

    private fun isPetLowHealth(pet: Pet): Boolean {
        return engine.getPetHealthFraction(pet) < 0.5f
    }
}