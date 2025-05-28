package bav.petus.useCase

import bav.petus.core.engine.Engine
import bav.petus.core.resources.ImageId
import bav.petus.model.AgeState
import bav.petus.model.BodyState
import bav.petus.model.BurialType
import bav.petus.model.Pet
import bav.petus.model.PetType
import bav.petus.model.Place

class PetImageUseCase(
    private val engine: Engine,
) {
    fun getPetImageId(pet: Pet): ImageId {
        if (pet.burialType == BurialType.Exhumated) return ImageId.DugOutGrave
        return when (pet.type) {
            PetType.Catus -> {
                if (isPetInGrave(pet)) return ImageId.CatGrave
                when (pet.ageState) {
                    AgeState.Egg -> ImageId.CatEgg
                    AgeState.NewBorn -> {
                        when {
                            pet.bodyState == BodyState.Dead -> ImageId.CatNewbornDead
                            pet.sleep -> ImageId.CatNewbornSleep
                            pet.isPooped -> ImageId.CatNewbornPoop
                            pet.illness -> ImageId.CatNewbornIll
                            isPetHungryOrBored(pet) -> ImageId.CatNewbornHungry
                            isPetLowHealth(pet) -> ImageId.CatNewbornIll
                            else -> ImageId.CatNewbornActive
                        }
                    }
                    AgeState.Teen,
                    AgeState.Adult -> {
                        when {
                            pet.bodyState == BodyState.Dead -> ImageId.CatAdultDead
                            pet.sleep -> ImageId.CatAdultSleep
                            pet.isPooped -> ImageId.CatAdultPoop
                            pet.illness -> ImageId.CatAdultIll
                            isPetHungryOrBored(pet) -> ImageId.CatAdultHungry
                            isPetLowHealth(pet) -> ImageId.CatAdultIll
                            else -> ImageId.CatAdultActive
                        }
                    }
                    AgeState.Old -> {
                        when {
                            pet.bodyState == BodyState.Dead -> ImageId.CatOldDead
                            pet.sleep -> ImageId.CatOldSleep
                            pet.isPooped -> ImageId.CatOldPoop
                            pet.illness -> ImageId.CatOldIll
                            isPetHungryOrBored(pet) -> ImageId.CatOldHungry
                            isPetLowHealth(pet) -> ImageId.CatOldIll
                            else -> ImageId.CatOldActive
                        }
                    }
                }
            }
            PetType.Dogus -> {
                if (isPetInGrave(pet)) return ImageId.DogGrave
                when (pet.ageState) {
                    AgeState.Egg -> ImageId.DogEgg
                    AgeState.NewBorn -> {
                        when {
                            pet.bodyState == BodyState.Dead -> ImageId.DogNewbornDead
                            pet.sleep -> ImageId.DogNewbornSleep
                            pet.isPooped -> ImageId.DogNewbornPoop
                            pet.illness -> ImageId.DogNewbornIll
                            isPetHungryOrBored(pet) -> ImageId.DogNewbornHungry
                            isPetLowHealth(pet) -> ImageId.DogNewbornIll
                            else -> ImageId.DogNewbornActive
                        }
                    }
                    AgeState.Teen,
                    AgeState.Adult -> {
                        when {
                            pet.bodyState == BodyState.Dead -> ImageId.DogAdultDead
                            pet.sleep -> ImageId.DogAdultSleep
                            pet.isPooped -> ImageId.DogAdultPoop
                            pet.illness -> ImageId.DogAdultIll
                            isPetHungryOrBored(pet) -> ImageId.DogAdultHungry
                            isPetLowHealth(pet) -> ImageId.DogAdultIll
                            else -> ImageId.DogAdultActive
                        }
                    }
                    AgeState.Old -> {
                        when {
                            pet.bodyState == BodyState.Dead -> ImageId.DogOldDead
                            pet.sleep -> ImageId.DogOldSleep
                            pet.isPooped -> ImageId.DogOldPoop
                            pet.illness -> ImageId.DogOldIll
                            isPetHungryOrBored(pet) -> ImageId.DogOldHungry
                            isPetLowHealth(pet) -> ImageId.DogOldIll
                            else -> ImageId.DogOldActive
                        }
                    }
                }
            }
            PetType.Frogus -> {
                if (isPetInGrave(pet)) return ImageId.FrogGrave
                when (pet.ageState) {
                    AgeState.Egg -> ImageId.FrogEgg
                    AgeState.NewBorn -> {
                        when {
                            pet.bodyState == BodyState.Dead -> ImageId.FrogNewbornDead
                            pet.sleep -> ImageId.FrogNewbornSleep
                            pet.isPooped -> ImageId.FrogNewbornPoop
                            pet.illness -> ImageId.FrogNewbornIll
                            isPetHungryOrBored(pet) -> ImageId.FrogNewbornHungry
                            isPetLowHealth(pet) -> ImageId.FrogNewbornIll
                            else -> ImageId.FrogNewbornActive
                        }
                    }
                    AgeState.Teen,
                    AgeState.Adult -> {
                        when {
                            pet.bodyState == BodyState.Dead -> ImageId.FrogAdultDead
                            pet.sleep -> ImageId.FrogAdultSleep
                            pet.isPooped -> ImageId.FrogAdultPoop
                            pet.illness -> ImageId.FrogAdultIll
                            isPetHungryOrBored(pet) -> ImageId.FrogAdultHungry
                            isPetLowHealth(pet) -> ImageId.FrogAdultIll
                            else -> ImageId.FrogAdultActive
                        }
                    }
                    AgeState.Old -> {
                        when {
                            pet.bodyState == BodyState.Dead -> ImageId.FrogOldDead
                            pet.sleep -> ImageId.FrogOldSleep
                            pet.isPooped -> ImageId.FrogOldPoop
                            pet.illness -> ImageId.FrogOldIll
                            isPetHungryOrBored(pet) -> ImageId.FrogOldHungry
                            isPetLowHealth(pet) -> ImageId.FrogOldIll
                            else -> ImageId.FrogOldActive
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
    private fun isPetHungryOrBored(pet: Pet): Boolean {
        return engine.isPetBored(pet) || engine.isPetHungry(pet)
    }

    private fun isPetLowHealth(pet: Pet): Boolean {
        return engine.isPetLowHealth(pet)
    }

    private fun isPetInGrave(pet: Pet): Boolean {
        return pet.place == Place.Cemetery && pet.burialType == BurialType.Buried
    }
}