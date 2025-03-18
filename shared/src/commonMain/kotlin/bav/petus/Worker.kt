package bav.petus

import kotlin.random.Random
import bav.petus.model.AgeState
import bav.petus.model.Pet
import bav.petus.model.PetType
import bav.petus.model.SleepState
import bav.petus.model.WeatherAttitude
import bav.petus.repo.PetsRepository
import bav.petus.core.time.TimeRepository
import bav.petus.repo.WeatherRepository
import bav.petus.useCase.WeatherAttitudeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class Worker(
    private val scope: CoroutineScope,
    private val weatherAttitudeUseCase: WeatherAttitudeUseCase,
    private val weatherRepository: WeatherRepository,
    private val timeRepository: TimeRepository,
    private val petsRepository: PetsRepository,
) {
    fun worker() {
        scope.launch {
            timeRepository.incrementTick()

            val currentWeather = async { weatherRepository.getWeather() }.await()
            val currentTick = timeRepository.getCurrentTick()

            for (pet in petsRepository.getAllPets()) {

                if (pet.isDead) continue

                val petAge = currentTick - pet.creationTick
                pet.ageState = getPetAgeState(pet, petAge)
                when (pet.ageState) {
                    AgeState.Egg -> {
                        if (petAge == getAgeToBeNewborn(pet)) {
//                            givePetBirth(pet)
                        }
                    }

                    AgeState.NewBorn,
                    AgeState.Teen,
                    AgeState.Adult,
                    AgeState.Old -> {
                        pet.satiety -= getHunger(pet)
//                        pet.psych -= getPsych(pet, weatherAttitudeUseCase.convertDtoToAttitude(pet.type, currentWeather))
                        pet.health += getHealthChange(pet)

                        pet.satiety = pet.satiety.coerceIn(0f, getFullSatietyForPetType(pet))
                        pet.psych = pet.psych.coerceIn(0f, getFullPsychForPetType(pet))
                        pet.health = pet.health.coerceIn(0f, getFullHealthForPetType(pet))

                        // DEAD
                        if (pet.health <= 0) {
                            makePetDead(pet)
                        }
                        // ALIVE
                        else {
                            if (pet.illness.not()) {
                                pet.illness = Random.Default.nextFloat() < pet.illnessPossibility
                            }

                            // Whether it's time to switch between sleep and active states
                            val timeInLatestSleepActiveState = currentTick - pet.activeSleepTick
                            if (timeInLatestSleepActiveState == getTimeInState(pet, pet.activeSleepState)) {
                                pet.activeSleepTick = currentTick
                                pet.activeSleepState = pet.activeSleepState.not()
                            }

                            // POOP
                            if (timeInLatestSleepActiveState == 1L) {
                                pet.isPooped = true
                            }
                        }
                    }
                }

                // Additional checks for Old pets
                if (pet.ageState == AgeState.Old) {
                    // Random death
                    if (Random.Default.nextFloat() < pet.deathOfOldAgePossibility) {
                        makePetDead(pet)
                    } else {
                        pet.deathOfOldAgePossibility += DEATH_OF_OLD_AGE_POSSIBILITY_INC
                    }
                }

                // Save pet changes

            }
        }

    }
}


const val DEATH_OF_OLD_AGE_POSSIBILITY_INC = 0.01f

fun makePetDead(pet: Pet) {
    pet.isDead = true
    pet.timeOfDeath = getTimeOfDeath()
}

fun getTimeOfDeath(): String {
    return "12.12.2012"
}

/**
 * Returns basic health change per one tick for pet type that will be modified
 * by satiety and psych status.
 */
fun getBasicHealthChange(pet: Pet): Float {
    return when (pet.type) {
        PetType.Catus -> 10f
        PetType.Dogus -> 10f
        PetType.Frogus -> 10f
    }
}

/**
 * Health change depends on pet's satiety and psych.
 * If satiety or psych is more that 2/3 of respectful full parameter then health increases,
 * otherwise - decreases.
 * -100            0     100
 *   |------|------|------|
 */
fun getHealthChange(pet: Pet): Float {
    val maxHealthChange = getBasicHealthChange(pet)

    var result = 0f

    // HUNGER
    val fullSatiety = getFullSatietyForPetType(pet)
    val oneThirdsSatiety = fullSatiety / 3f
    val twoThirdsSatiety = oneThirdsSatiety * 2f
    val satietyZeroPosition = twoThirdsSatiety
    if (pet.satiety > satietyZeroPosition) {
        result += ((pet.satiety - satietyZeroPosition) / oneThirdsSatiety) * maxHealthChange
    } else {
        result -= ((satietyZeroPosition - pet.satiety) / twoThirdsSatiety) * maxHealthChange
    }

    // PSYCH
    val fullPsych = getFullPsychForPetType(pet)
    val oneThirdsPsych = fullPsych / 3
    val twoThirdsPsych = oneThirdsPsych * 2
    val psychZeroPosition = twoThirdsPsych
    if (pet.psych > psychZeroPosition) {
        result += ((pet.psych - psychZeroPosition) / oneThirdsPsych) * maxHealthChange
    } else {
        result -= ((psychZeroPosition - pet.psych) / twoThirdsPsych) * maxHealthChange
    }

    return result
}

//fun createPet(name: String) {
//    val pet = Pet(
//        name = name,
//        creationTick = globalTickCounter // Current (last) tick
//    )
//    pet.health = getFullHealthForPetType(pet)
//    pet.psych = getFullPsychForPetType(pet)
//    pet.satiety = getFullSatietyForPetType(pet)
//    pet.activeSleepState = SleepState.Sleep
//    pet.illnessPossibility = getRandomIllnessPossibility()
//    pet.deathOfOldAgePossibility = 0f
//}
//
//fun givePetBirth(pet: Pet) {
//    pet.activeSleepState = SleepState.Active
//    pet.activeSleepTick = globalTickCounter
//}

fun FirstAppStart() {
//    globalTickCounter = 0
    //startWorker()
}

/**
 * Calculates change in satiety of a pet during one tick.
 * Result is always positive since pet always wastes energy.
 * Result must be **subtracted** from pet's satiety.
 */
fun getHunger(pet: Pet): Float {
    val basicHunger = getHungerBasedOnPetTypeAndAgeState(pet)

    var resultHunger = 0f

    // SLEEP / ACTIVE
    if (pet.sleep) resultHunger += basicHunger * 0.5f
    else resultHunger += basicHunger

    // ILLNESS
    if (pet.illness) resultHunger += basicHunger * 1.5f

    return resultHunger
}

/**
 * Calculates change in psych for a pet during one tick.
 * Result may be positive or negative. Result must be **subtracted** from pet's psych.
 */
fun getPsych(pet: Pet, weatherAttitude: WeatherAttitude): Float {
    val basicPsych = getPsychBasedOnPetTypeAndAgeState(pet)

    var resultPsych = 0f

    // SLEEP / ACTIVE
    if (pet.sleep) resultPsych -= basicPsych * 1.25f
    else resultPsych += basicPsych

    // ILLNESS
    if (pet.illness) resultPsych += basicPsych * 2f

    // CLOUDINESS
    if (pet.sleep.not()) resultPsych += (basicPsych * BASIC_CLOUDINESS_MULTIPLIER * weatherAttitude.toCloudPercentage).toFloat()

    // TEMPERATURE
    if (pet.sleep) resultPsych += (basicPsych * SLEEP_TEMPERATURE_MULTIPLIER * weatherAttitude.toTemperature).toFloat()
    else resultPsych += (basicPsych * ACTIVE_TEMPERATURE_MULTIPLIER * weatherAttitude.toTemperature).toFloat()

    // HUMIDITY
    // TODO
    // WIND SPEED
    // TODO

    // POOP
    if (pet.isPooped) resultPsych += basicPsych * 0.5f

    // HUNGER
    if (pet.satiety <= 0) resultPsych += basicPsych * 3f

    return resultPsych
}

const val BASIC_CLOUDINESS_MULTIPLIER = 1f
const val ACTIVE_TEMPERATURE_MULTIPLIER = 0.5f
const val SLEEP_TEMPERATURE_MULTIPLIER = 0.25f

const val FEED_PORTION = 50f

fun feedPet(pet: Pet) {
    pet.satiety += FEED_PORTION
    pet.satiety = pet.satiety.coerceIn(0f, getFullSatietyForPetType(pet))
}

const val PLAY_PSYCH_PORTION = 50f

fun playWithPet(pet: Pet) {
    pet.psych += PLAY_PSYCH_PORTION
    pet.psych = pet.psych.coerceIn(0f, getFullPsychForPetType(pet))
}

/**
 * Returns basic consumption of satiety per one tick based on pet type and age
 * presuming pet is active
 */
fun getHungerBasedOnPetTypeAndAgeState(pet: Pet): Float {
    var hunger = 0f
    petsHunger[pet.type]?.let { hungerByAgeState ->
        hungerByAgeState[pet.ageState]?.let { hunger = it }
    }
    return hunger
}

/**
 * Returns basic consumption of psych per one tick based on pet type and age
 * presuming pet is active
 */
fun getPsychBasedOnPetTypeAndAgeState(pet: Pet): Float {
    var psych = 0f
    petsPsych[pet.type]?.let { psychByAgeState ->
        psychByAgeState[pet.ageState]?.let { psych = it }
    }
    return psych
}

fun getFullHealthForPetType(pet: Pet): Float {
    return when (pet.type) {
        PetType.Catus -> 100f
        PetType.Dogus -> 150f
        PetType.Frogus -> 50f
    }
}

fun getFullPsychForPetType(pet: Pet): Float {
    return when (pet.type) {
        PetType.Catus -> 150f
        PetType.Dogus -> 100f
        PetType.Frogus -> 50f
    }
}

fun getFullSatietyForPetType(pet: Pet): Float {
    return when (pet.type) {
        PetType.Catus -> 100f
        PetType.Dogus -> 100f
        PetType.Frogus -> 100f
    }
}

fun getTimeInState(pet: Pet, state: SleepState): Long {
    var time = 12L
    petsActiveSleepTimes[pet.type]?.let { times ->
        times[state]?.let { time = it }
    }
    return time
}

const val MAXIMUM_ILLNESS_POSSIBILITY_ON_CREATION = 0.2f
/**
 * Returns random illness possibility in range 0 < p < MAXIMUM_ILLNESS_POSSIBILITY_ON_CREATION
 */
fun getRandomIllnessPossibility(): Float {
    return Random.Default.nextFloat() * MAXIMUM_ILLNESS_POSSIBILITY_ON_CREATION
}

fun getPetAgeState(pet: Pet, petAge: Long): AgeState {
    var state = AgeState.Egg
    petsAges[pet.type]?.let { ages ->
        for (pair in ages) {
            if (pair.value.contains(petAge)) {
                state = pair.key
                break
            }
        }
    }
    return state
}

fun getAgeToBeNewborn(pet: Pet): Long {
    var age = 3L
    petsAges[pet.type]?.let { ages ->
        ages[AgeState.Egg]?.let { range ->
            age = range.last
        }
    }
    return age
}

val commonPetAgeToTicksTable = mapOf(
    AgeState.Egg to 0L..3L,
    AgeState.NewBorn to 4L..10L,
    AgeState.Teen to 11L..20L,
    AgeState.Adult to 21L..30L,
    AgeState.Old to 31L..Long.MAX_VALUE
)

val petsAges = mapOf(
    PetType.Catus to commonPetAgeToTicksTable,
    PetType.Dogus to commonPetAgeToTicksTable,
    PetType.Frogus to commonPetAgeToTicksTable,
)

val commonPetHungerTable = mapOf(
    AgeState.Egg to 0f,
    AgeState.NewBorn to 10f,
    AgeState.Teen to 8f,
    AgeState.Adult to 6f,
    AgeState.Old to 3f
)

val petsHunger = mapOf(
    PetType.Catus to commonPetHungerTable,
    PetType.Dogus to commonPetHungerTable,
    PetType.Frogus to commonPetHungerTable,
)

val commonPetPsychTable = mapOf(
    AgeState.Egg to 0f,
    AgeState.NewBorn to 3f,
    AgeState.Teen to 6f,
    AgeState.Adult to 8f,
    AgeState.Old to 10f
)

val petsPsych = mapOf(
    PetType.Catus to commonPetPsychTable,
    PetType.Dogus to commonPetPsychTable,
    PetType.Frogus to commonPetPsychTable,
)

val commonActiveSleepTimesTable = mapOf(
    SleepState.Active to 12L,
    SleepState.Sleep to 12L,
)

val petsActiveSleepTimes = mapOf(
    PetType.Catus to commonActiveSleepTimesTable,
    PetType.Dogus to commonActiveSleepTimesTable,
    PetType.Frogus to commonActiveSleepTimesTable,
)
