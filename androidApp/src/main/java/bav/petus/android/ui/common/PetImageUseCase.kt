package bav.petus.android.ui.common

import bav.petus.android.R
import bav.petus.core.engine.Engine
import bav.petus.model.AgeState
import bav.petus.model.Pet
import bav.petus.model.PetType

class PetImageUseCase(
    private val engine: Engine,
) {
    fun getPetImageResId(pet: Pet): Int {
        return when (pet.type) {
            PetType.Catus -> {
                when (pet.ageState) {
                    AgeState.Egg -> R.drawable.catus_egg
                    AgeState.NewBorn -> {
                        when {
                            pet.sleep -> R.drawable.catus_newborn_sleep
                            pet.illness -> R.drawable.catus_newborn_ill
                            isPetHungryOrPsych(pet) -> R.drawable.catus_newborn_hungry_or_psych
                            else -> R.drawable.catus_newborn_active
                        }
                    }
                    AgeState.Teen -> {
                        when {
                            pet.sleep -> R.drawable.catus_teen_sleep
                            pet.illness -> R.drawable.catus_teen_ill
                            isPetHungryOrPsych(pet) -> R.drawable.catus_teen_hungry
                            else -> R.drawable.catus_teen_active
                        }
                    }
                    AgeState.Adult -> {
                        when {
                            pet.sleep -> R.drawable.catus_adult_sleep
                            pet.illness -> R.drawable.catus_adult_ill
                            isPetHungryOrPsych(pet) -> R.drawable.catus_adult_hungry
                            else -> R.drawable.catus_adult_active
                        }
                    }
                    AgeState.Old -> {
                        when {
                            pet.sleep -> R.drawable.catus_old_sleep
                            pet.illness -> R.drawable.catus_old_ill
                            isPetHungryOrPsych(pet) -> R.drawable.catus_old_hungry
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
                            pet.sleep -> R.drawable.dogus_newborn_sleep
                            pet.illness -> R.drawable.dogus_newborn_ill
                            isPetHungryOrPsych(pet) -> R.drawable.dogus_newborn_hungry
                            else -> R.drawable.dogus_newborn_active
                        }
                    }
                    AgeState.Teen -> {
                        when {
                            pet.sleep -> R.drawable.dogus_teen_sleep
                            pet.illness -> R.drawable.dogus_teen_ill
                            isPetHungryOrPsych(pet) -> R.drawable.dogus_teen_hungry
                            else -> R.drawable.dogus_teen_active
                        }
                    }
                    AgeState.Adult -> {
                        when {
                            pet.sleep -> R.drawable.dogus_adult_sleep
                            pet.illness -> R.drawable.dogus_adult_ill
                            isPetHungryOrPsych(pet) -> R.drawable.dogus_adult_hungry
                            else -> R.drawable.dogus_adult_active
                        }
                    }
                    AgeState.Old -> {
                        when {
                            pet.sleep -> R.drawable.dogus_old_sleep
                            pet.illness -> R.drawable.dogus_old_ill
                            isPetHungryOrPsych(pet) -> R.drawable.dogus_old_hungry
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
                            pet.sleep -> R.drawable.frogus_newborn_sleep
                            pet.illness -> R.drawable.frogus_newborn_ill
                            isPetHungryOrPsych(pet) -> R.drawable.frogus_newborn_hungry
                            else -> R.drawable.frogus_newborn_active
                        }
                    }
                    AgeState.Teen -> {
                        when {
                            pet.sleep -> R.drawable.frogus_teen_sleep
                            pet.illness -> R.drawable.frogus_teen_ill
                            isPetHungryOrPsych(pet) -> R.drawable.frogus_teen_hungry
                            else -> R.drawable.frogus_teen_active
                        }
                    }
                    AgeState.Adult -> {
                        when {
                            pet.sleep -> R.drawable.frogus_adult_sleep
                            pet.illness -> R.drawable.frogus_adult_ill
                            isPetHungryOrPsych(pet) -> R.drawable.frogus_adult_hungry
                            else -> R.drawable.frogus_adult_active
                        }
                    }
                    AgeState.Old -> {
                        when {
                            pet.sleep -> R.drawable.frogus_old_sleep
                            pet.illness -> R.drawable.frogus_old_ill
                            isPetHungryOrPsych(pet) -> R.drawable.frogus_old_hungry
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
}