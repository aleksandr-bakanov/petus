package bav.petus.useCase

import bav.petus.core.engine.Engine
import bav.petus.core.resources.ImageId
import bav.petus.model.AgeState
import bav.petus.model.BodyState
import bav.petus.model.BurialType
import bav.petus.model.DragonType
import bav.petus.model.FractalType
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
                            pet.bodyState == BodyState.Zombie -> ImageId.CatNewbornZombie
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
                            pet.bodyState == BodyState.Zombie -> ImageId.CatAdultZombie
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
                            pet.bodyState == BodyState.Zombie -> ImageId.CatOldZombie
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
                            pet.bodyState == BodyState.Zombie -> ImageId.DogNewbornZombie
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
                            pet.bodyState == BodyState.Zombie -> ImageId.DogAdultZombie
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
                            pet.bodyState == BodyState.Zombie -> ImageId.DogOldZombie
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
                            pet.bodyState == BodyState.Zombie -> ImageId.FrogNewbornZombie
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
                            pet.bodyState == BodyState.Zombie -> ImageId.FrogAdultZombie
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
                            pet.bodyState == BodyState.Zombie -> ImageId.FrogOldZombie
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

            PetType.Bober -> {
                if (isPetInGrave(pet)) return ImageId.BoberGrave
                when (pet.ageState) {
                    AgeState.Egg -> ImageId.BoberEgg
                    AgeState.NewBorn -> {
                        when {
                            pet.bodyState == BodyState.Zombie -> ImageId.BoberNewbornZombie
                            pet.bodyState == BodyState.Dead -> ImageId.BoberNewbornDead
                            pet.sleep -> ImageId.BoberNewbornSleep
                            pet.isPooped -> ImageId.BoberNewbornPoop
                            pet.illness -> ImageId.BoberNewbornIll
                            isPetHungryOrBored(pet) -> ImageId.BoberNewbornHungry
                            isPetLowHealth(pet) -> ImageId.BoberNewbornIll
                            else -> ImageId.BoberNewbornActive
                        }
                    }
                    AgeState.Teen,
                    AgeState.Adult -> {
                        when {
                            pet.bodyState == BodyState.Zombie -> ImageId.BoberAdultZombie
                            pet.bodyState == BodyState.Dead -> ImageId.BoberAdultDead
                            pet.sleep -> ImageId.BoberAdultSleep
                            pet.isPooped -> ImageId.BoberAdultPoop
                            pet.illness -> ImageId.BoberAdultIll
                            isPetHungryOrBored(pet) -> ImageId.BoberAdultHungry
                            isPetLowHealth(pet) -> ImageId.BoberAdultIll
                            else -> ImageId.BoberAdultActive
                        }
                    }
                    AgeState.Old -> {
                        when {
                            pet.bodyState == BodyState.Zombie -> ImageId.BoberOldZombie
                            pet.bodyState == BodyState.Dead -> ImageId.BoberOldDead
                            pet.sleep -> ImageId.BoberOldSleep
                            pet.isPooped -> ImageId.BoberOldPoop
                            pet.illness -> ImageId.BoberOldIll
                            isPetHungryOrBored(pet) -> ImageId.BoberOldHungry
                            isPetLowHealth(pet) -> ImageId.BoberOldIll
                            else -> ImageId.BoberOldActive
                        }
                    }
                }
            }

            PetType.Fractal -> {
                when (pet.ageState) {
                    AgeState.Egg -> ImageId.FractalEgg
                    AgeState.NewBorn -> {
                        when {
                            pet.sleep -> ImageId.FractalNewbornSleep
                            pet.isPooped -> ImageId.FractalNewbornPoop
                            pet.illness -> ImageId.FractalNewbornIll
                            isPetHungryOrBored(pet) -> ImageId.FractalNewbornHungry
                            isPetLowHealth(pet) -> ImageId.FractalNewbornIll
                            else -> ImageId.FractalNewbornActive
                        }
                    }
                    AgeState.Teen,
                    AgeState.Adult,
                    AgeState.Old -> {
                        when (pet.fractalType) {
                            FractalType.Gosper -> when {
                                pet.sleep -> ImageId.FractalGosperSleep
                                pet.isPooped -> ImageId.FractalGosperPoop
                                pet.illness -> ImageId.FractalGosperIll
                                isPetHungryOrBored(pet) -> ImageId.FractalGosperHungry
                                isPetLowHealth(pet) -> ImageId.FractalGosperIll
                                else -> ImageId.FractalGosperActive
                            }
                            FractalType.Koch -> when {
                                pet.sleep -> ImageId.FractalKochSleep
                                pet.isPooped -> ImageId.FractalKochPoop
                                pet.illness -> ImageId.FractalKochIll
                                isPetHungryOrBored(pet) -> ImageId.FractalKochHungry
                                isPetLowHealth(pet) -> ImageId.FractalKochIll
                                else -> ImageId.FractalKochActive
                            }
                            FractalType.Sponge -> when {
                                pet.sleep -> ImageId.FractalSpongeSleep
                                pet.isPooped -> ImageId.FractalSpongePoop
                                pet.illness -> ImageId.FractalSpongeIll
                                isPetHungryOrBored(pet) -> ImageId.FractalSpongeHungry
                                isPetLowHealth(pet) -> ImageId.FractalSpongeIll
                                else -> ImageId.FractalSpongeActive
                            }
                        }
                    }
                }
            }

            PetType.Dragon -> {
                if (isPetInGrave(pet)) return ImageId.DragonGrave
                when (pet.ageState) {
                    AgeState.Egg -> ImageId.DragonEgg
                    AgeState.NewBorn -> {
                        when (pet.dragonType) {
                            DragonType.Red -> when {
                                pet.bodyState == BodyState.Zombie -> ImageId.DragonRedNewbornZombie
                                pet.bodyState == BodyState.Dead -> ImageId.DragonRedNewbornDead
                                pet.sleep -> ImageId.DragonRedNewbornSleep
                                pet.isPooped -> ImageId.DragonRedNewbornPoop
                                pet.illness -> ImageId.DragonRedNewbornIll
                                isPetHungryOrBored(pet) -> ImageId.DragonRedNewbornHungry
                                isPetLowHealth(pet) -> ImageId.DragonRedNewbornIll
                                else -> ImageId.DragonRedNewbornActive
                            }
                            DragonType.Blue -> when {
                                pet.bodyState == BodyState.Zombie -> ImageId.DragonBlueNewbornZombie
                                pet.bodyState == BodyState.Dead -> ImageId.DragonBlueNewbornDead
                                pet.sleep -> ImageId.DragonBlueNewbornSleep
                                pet.isPooped -> ImageId.DragonBlueNewbornPoop
                                pet.illness -> ImageId.DragonBlueNewbornIll
                                isPetHungryOrBored(pet) -> ImageId.DragonBlueNewbornHungry
                                isPetLowHealth(pet) -> ImageId.DragonBlueNewbornIll
                                else -> ImageId.DragonBlueNewbornActive
                            }
                            DragonType.Void -> when {
                                pet.bodyState == BodyState.Zombie -> ImageId.DragonVoidNewbornZombie
                                pet.bodyState == BodyState.Dead -> ImageId.DragonVoidNewbornDead
                                pet.sleep -> ImageId.DragonVoidNewbornSleep
                                pet.isPooped -> ImageId.DragonVoidNewbornPoop
                                pet.illness -> ImageId.DragonVoidNewbornIll
                                isPetHungryOrBored(pet) -> ImageId.DragonVoidNewbornHungry
                                isPetLowHealth(pet) -> ImageId.DragonVoidNewbornIll
                                else -> ImageId.DragonVoidNewbornActive
                            }
                        }
                    }
                    AgeState.Teen,
                    AgeState.Adult -> {
                        when (pet.dragonType) {
                            DragonType.Red -> when {
                                pet.bodyState == BodyState.Zombie -> ImageId.DragonRedAdultZombie
                                pet.bodyState == BodyState.Dead -> ImageId.DragonRedAdultDead
                                pet.sleep -> ImageId.DragonRedAdultSleep
                                pet.isPooped -> ImageId.DragonRedAdultPoop
                                pet.illness -> ImageId.DragonRedAdultIll
                                isPetHungryOrBored(pet) -> ImageId.DragonRedAdultHungry
                                isPetLowHealth(pet) -> ImageId.DragonRedAdultIll
                                else -> ImageId.DragonRedAdultActive
                            }
                            DragonType.Blue -> when {
                                pet.bodyState == BodyState.Zombie -> ImageId.DragonBlueAdultZombie
                                pet.bodyState == BodyState.Dead -> ImageId.DragonBlueAdultDead
                                pet.sleep -> ImageId.DragonBlueAdultSleep
                                pet.isPooped -> ImageId.DragonBlueAdultPoop
                                pet.illness -> ImageId.DragonBlueAdultIll
                                isPetHungryOrBored(pet) -> ImageId.DragonBlueAdultHungry
                                isPetLowHealth(pet) -> ImageId.DragonBlueAdultIll
                                else -> ImageId.DragonBlueAdultActive
                            }
                            DragonType.Void -> when {
                                pet.bodyState == BodyState.Zombie -> ImageId.DragonVoidAdultZombie
                                pet.bodyState == BodyState.Dead -> ImageId.DragonVoidAdultDead
                                pet.sleep -> ImageId.DragonVoidAdultSleep
                                pet.isPooped -> ImageId.DragonVoidAdultPoop
                                pet.illness -> ImageId.DragonVoidAdultIll
                                isPetHungryOrBored(pet) -> ImageId.DragonVoidAdultHungry
                                isPetLowHealth(pet) -> ImageId.DragonVoidAdultIll
                                else -> ImageId.DragonVoidAdultActive
                            }
                        }
                    }
                    AgeState.Old -> {
                        when (pet.dragonType) {
                            DragonType.Red -> when {
                                pet.bodyState == BodyState.Zombie -> ImageId.DragonRedOldZombie
                                pet.bodyState == BodyState.Dead -> ImageId.DragonRedOldDead
                                pet.sleep -> ImageId.DragonRedOldSleep
                                pet.isPooped -> ImageId.DragonRedOldPoop
                                pet.illness -> ImageId.DragonRedOldIll
                                isPetHungryOrBored(pet) -> ImageId.DragonRedOldHungry
                                isPetLowHealth(pet) -> ImageId.DragonRedOldIll
                                else -> ImageId.DragonRedOldActive
                            }
                            DragonType.Blue -> when {
                                pet.bodyState == BodyState.Zombie -> ImageId.DragonBlueOldZombie
                                pet.bodyState == BodyState.Dead -> ImageId.DragonBlueOldDead
                                pet.sleep -> ImageId.DragonBlueOldSleep
                                pet.isPooped -> ImageId.DragonBlueOldPoop
                                pet.illness -> ImageId.DragonBlueOldIll
                                isPetHungryOrBored(pet) -> ImageId.DragonBlueOldHungry
                                isPetLowHealth(pet) -> ImageId.DragonBlueOldIll
                                else -> ImageId.DragonBlueOldActive
                            }
                            DragonType.Void -> when {
                                pet.bodyState == BodyState.Zombie -> ImageId.DragonVoidOldZombie
                                pet.bodyState == BodyState.Dead -> ImageId.DragonVoidOldDead
                                pet.sleep -> ImageId.DragonVoidOldSleep
                                pet.isPooped -> ImageId.DragonVoidOldPoop
                                pet.illness -> ImageId.DragonVoidOldIll
                                isPetHungryOrBored(pet) -> ImageId.DragonVoidOldHungry
                                isPetLowHealth(pet) -> ImageId.DragonVoidOldIll
                                else -> ImageId.DragonVoidOldActive
                            }
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