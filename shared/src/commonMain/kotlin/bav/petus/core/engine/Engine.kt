package bav.petus.core.engine

import bav.petus.cache.WeatherRecord
import bav.petus.core.time.TimeRepository
import bav.petus.core.time.getTimestampSecondsSinceEpoch
import bav.petus.model.AgeState
import bav.petus.model.Pet
import bav.petus.model.PetType
import bav.petus.model.SleepState
import bav.petus.model.WeatherAttitude
import bav.petus.repo.PetsRepository
import bav.petus.repo.WeatherRepository
import bav.petus.useCase.WeatherAttitudeUseCase
import kotlin.random.Random

class Engine(
    private val timeRepo: TimeRepository,
    private val petsRepo: PetsRepository,
    private val weatherRepo: WeatherRepository,
    private val weatherAttitudeUseCase: WeatherAttitudeUseCase,
) {

    suspend fun createNewPet(
        name: String,
        type: PetType,
    ) {
        val pet = Pet(
            name = name,
            type = type,
            creationTime = getTimestampSecondsSinceEpoch(),
            illnessPossibility = getRandomIllnessPossibility(),
            health = getFullHealthForPetType(type),
            psych = getFullPsychForPetType(type),
            satiety = getFullSatietyForPetType(type),
        )
        petsRepo.insertPet(pet)
    }

    fun isAllowedToFeedPet(pet: Pet): Boolean {
        return pet.isDead.not() &&
                pet.ageState != AgeState.Egg &&
                pet.satiety < getFullSatietyForPetType(pet.type) &&
                pet.sleep.not()
    }

    suspend fun feedPet(pet: Pet) {
        val newPet = pet.copy(
            satiety = getFullSatietyForPetType(pet.type)
        )
        petsRepo.updatePet(newPet)
    }

    fun isAllowedToPlayWithPet(pet: Pet): Boolean {
        return pet.isDead.not() &&
                pet.ageState != AgeState.Egg &&
                pet.psych < getFullPsychForPetType(pet.type) &&
                pet.sleep.not()
    }

    suspend fun playWithPet(pet: Pet) {
        val newPet = pet.copy(
            psych = getFullPsychForPetType(pet.type)
        )
        petsRepo.updatePet(newPet)
    }

    fun isAllowedToCleanAfterPet(pet: Pet): Boolean {
        return pet.isPooped
    }

    suspend fun cleanAfterPet(pet: Pet) {
        val newPet = pet.copy(
            isPooped = false,
        )
        petsRepo.updatePet(newPet)
    }

    fun isAllowedToHealPet(pet: Pet): Boolean {
        return pet.illness && pet.isDead.not()
    }

    suspend fun healPetIllness(pet: Pet) {
        val newPet = pet.copy(
            illness = false,
        )
        petsRepo.updatePet(newPet)
    }

    fun isAllowedToWakeUpPet(pet: Pet): Boolean {
        return pet.sleep && pet.isDead.not() && pet.ageState != AgeState.Egg
    }

    suspend fun wakeUpPet(pet: Pet) {
        val now = getTimestampSecondsSinceEpoch()
        val newPet = pet.copy(
            activeSleepState = SleepState.Active,
            lastActiveSleepSwitchTimestamp = now,
            isPooped = true,
        )
        petsRepo.updatePet(newPet)
    }

    fun getPetSatietyFraction(pet: Pet): Float {
        return pet.satiety / getFullSatietyForPetType(pet.type)
    }

    fun getPetPsychFraction(pet: Pet): Float {
        return pet.psych / getFullPsychForPetType(pet.type)
    }

    fun getPetHealthFraction(pet: Pet): Float {
        return pet.health / getFullHealthForPetType(pet.type)
    }

    fun getSecondsToNextSleepStateChange(pet: Pet): Long {
        val now = getTimestampSecondsSinceEpoch()
        val passedSinceLastChange = now - pet.lastActiveSleepSwitchTimestamp
        val inCurrentState = getTimeInState(pet, pet.activeSleepState)
        return inCurrentState - passedSinceLastChange
    }

    fun getNextSleepStateChangeTimestamp(pet: Pet): Long {
        return if (pet.ageState == AgeState.Egg) {
            pet.creationTime + commonPetAgeToSecondsTable[AgeState.Egg]!!.last
        } else {
            pet.lastActiveSleepSwitchTimestamp + getTimeInState(pet, pet.activeSleepState)
        }
    }

    fun getPetTypeDescription(type: PetType): String {
        return when (type) {
            PetType.Catus -> "Catus: sleeps 16 hours, awake for 8 hours. Likes warm sunny days. Likes small wind."
            PetType.Dogus -> "Dogus: sleeps 8 hours, awake for 16 hours. Likes warm sunny days. Likes medium wind."
            PetType.Frogus -> "Frogus: sleeps 12 hours, awake for 12 hours. Likes cold cloudy days. Doesn't like wind."
        }
    }

    suspend fun updateGameState() {
        val currentTime = getTimestampSecondsSinceEpoch()
        val lastTimestamp = timeRepo.getLastTimestamp()

        // Very first application start, no need to update anything
        if (lastTimestamp == 0L) {
            timeRepo.saveLastTimestamp(currentTime)
            return
        }

        // There is not enough time passed, just return
        if (currentTime - lastTimestamp < TimeRepository.CYCLE_PERIOD_IN_SECONDS) {
            return
        }

        // At least one period passed, let's update game state
        var pets = petsRepo.getAllAlivePets()
        val periodsPassed =
            (currentTime - lastTimestamp) / TimeRepository.CYCLE_PERIOD_IN_SECONDS

        for (periodIndex in 0 until periodsPassed) {
            // periodTime is a start point of a certain period - this may matter only for selection
            // of weather record. For the rest of the calculations it shouldn't matter.
            val periodTime =
                lastTimestamp + periodIndex * TimeRepository.CYCLE_PERIOD_IN_SECONDS
            val weatherRecord = weatherRepo.getClosestWeather(
                timestamp = periodTime
            )
            // Update all pets
            pets = pets.map { updatePet(it, weatherRecord, periodTime) }
        }

        // Save results in DB
        pets.forEach {
            petsRepo.updatePet(it)
        }

        // Saving latest periodTime as new lastTimestamp
        timeRepo.saveLastTimestamp(lastTimestamp + periodsPassed * TimeRepository.CYCLE_PERIOD_IN_SECONDS)
    }

    /**
     * Executes changes of the pet during one game cycle
     *
     * @param pet Pet object
     * @param weatherRecord Info about weather closest to [periodTimestamp] if known
     * @param periodTimestamp Timestamp of a certain tick (cycle) - granulated
     *   by [TimeRepository.CYCLE_PERIOD_IN_SECONDS].
     * @return Resulting pet object
     */
    private fun updatePet(
        pet: Pet,
        weatherRecord: WeatherRecord?,
        periodTimestamp: Long,
    ): Pet {
        if (pet.isDead) return pet

        var newPet = pet.copy()

        val petAgeInSeconds = periodTimestamp - pet.creationTime
        newPet = newPet.copy(
            ageState = getPetAgeState(newPet.type, petAgeInSeconds),
        )

        // If pet became new born on this cycle we need to awake him
        // and remember this first time of awakening
        if (pet.ageState == AgeState.Egg && newPet.ageState == AgeState.NewBorn) {
            newPet = newPet.copy(
                activeSleepState = SleepState.Active,
                lastActiveSleepSwitchTimestamp = periodTimestamp,
            )
        }

        when (newPet.ageState) {
            AgeState.Egg -> {
                // Do nothing
            }

            AgeState.NewBorn,
            AgeState.Teen,
            AgeState.Adult,
            AgeState.Old -> {
                val newSatiety = newPet.satiety - getHunger(newPet)
                newPet = newPet.copy(
                    satiety = newSatiety.coerceIn(0f, getFullSatietyForPetType(pet.type))
                )

                val weatherAttitude = weatherAttitudeUseCase.convertWeatherRecordToAttitude(
                    petType = newPet.type,
                    record = weatherRecord,
                )
                val newPsych = newPet.psych - getPsych(newPet, weatherAttitude)
                newPet = newPet.copy(
                    psych = newPsych.coerceIn(0f, getFullPsychForPetType(pet.type))
                )

                val newHealth = newPet.health + getHealthChange(newPet)
                newPet = newPet.copy(
                    health = newHealth.coerceIn(0f, getFullHealthForPetType(pet.type))
                )

                // DEAD
                if (newPet.health <= 0f) {
                    newPet = makePetDead(newPet, periodTimestamp)
                }
                // ALIVE
                else {
                    if (newPet.illness.not()) {
                        newPet = newPet.copy(
                            illness = Random.Default.nextFloat() < newPet.illnessPossibility
                        )
                    }

                    // Whether it's time to switch between sleep and active states
                    val timeInLatestSleepActiveState = periodTimestamp - newPet.lastActiveSleepSwitchTimestamp
                    if (timeInLatestSleepActiveState >= getTimeInState(
                            newPet,
                            newPet.activeSleepState
                        )
                    ) {
                        // Time to switch states
                        newPet = newPet.copy(
                            lastActiveSleepSwitchTimestamp = periodTimestamp,
                            activeSleepState = newPet.activeSleepState.not()
                        )

                        // If pet woke up then it immediately poops
                        if (newPet.activeSleepState == SleepState.Active) {
                            newPet = newPet.copy(isPooped = true)
                        }
                    }
                }
            }
        }

        // Additional checks for Old pets
        if (newPet.ageState == AgeState.Old) {
            // Random death
            if (Random.Default.nextFloat() < newPet.deathOfOldAgePossibility) {
                newPet = makePetDead(newPet, periodTimestamp)
            } else {
                newPet = newPet.copy(
                    deathOfOldAgePossibility =
                        newPet.deathOfOldAgePossibility + DEATH_OF_OLD_AGE_POSSIBILITY_INC
                )
            }
        }

        return newPet
    }

    private fun makePetDead(pet: Pet, periodTime: Long): Pet {
        // TODO: save some info about cause of death
        return pet.copy(
            isDead = true,
            timeOfDeath = periodTime,
        )
    }

    private fun getPetAgeState(petType: PetType, petAgeInSeconds: Long): AgeState {
        var state = AgeState.Egg
        petsAges[petType]?.let { ages ->
            for (pair in ages) {
                if (pair.value.contains(petAgeInSeconds)) {
                    state = pair.key
                    break
                }
            }
        }
        return state
    }

    /**
     * Calculates change in satiety of a pet during one tick.
     * Result is always positive since pet always wastes energy.
     * Result must be **subtracted** from pet's satiety.
     */
    private fun getHunger(pet: Pet): Float {
        val basicHunger = getHungerBasedOnPetTypeAndAgeState(pet) * TimeRepository.CYCLE_PERIOD_IN_SECONDS

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
     * Result may be positive or negative.
     * Result must be **subtracted** from pet's psych.
     */
    private fun getPsych(pet: Pet, weatherAttitude: WeatherAttitude): Float {
        val basicPsych = getPsychBasedOnPetTypeAndAgeState(pet) * TimeRepository.CYCLE_PERIOD_IN_SECONDS

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
        if (pet.satiety <= 0f) resultPsych += basicPsych * 3f

        return resultPsych
    }

    /**
     * Health change depends on pet's satiety and psych.
     * If satiety or psych is more that 2/3 of respectful full parameter then health increases,
     * otherwise - decreases.
     * -100            0     100
     *   |------|------|------|
     * Result must be **added** to pet's health.
     */
    private fun getHealthChange(pet: Pet): Float {
        val maxHealthChange = getBasicHealthChange(pet.type)

        var result = 0f

        // HUNGER
        val fullSatiety = getFullSatietyForPetType(pet.type)
        val oneThirdsSatiety = fullSatiety / 3f
        val twoThirdsSatiety = oneThirdsSatiety * 2f
        val satietyZeroPosition = twoThirdsSatiety
        if (pet.satiety > satietyZeroPosition) {
            result += ((pet.satiety - satietyZeroPosition) / oneThirdsSatiety) * maxHealthChange
        } else {
            result -= ((satietyZeroPosition - pet.satiety) / twoThirdsSatiety) * maxHealthChange
        }

        // PSYCH
        val fullPsych = getFullPsychForPetType(pet.type)
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

    /**
     * Returns basic consumption of satiety per one second based on pet type and age
     * presuming pet is active
     */
    private fun getHungerBasedOnPetTypeAndAgeState(pet: Pet): Float {
        var hunger = 0f
        petsHunger[pet.type]?.let { hungerByAgeState ->
            hungerByAgeState[pet.ageState]?.let { hunger = it }
        }
        return hunger
    }

    /**
     * Returns basic consumption of psych per one second based on pet type and age
     * presuming pet is active
     */
    private fun getPsychBasedOnPetTypeAndAgeState(pet: Pet): Float {
        var psych = 0f
        petsPsych[pet.type]?.let { psychByAgeState ->
            psychByAgeState[pet.ageState]?.let { psych = it }
        }
        return psych
    }

    /**
     * Returns basic health change per one tick for pet type that will be modified
     * by satiety and psych status.
     */
    private fun getBasicHealthChange(type: PetType): Float {
        return when (type) {
            PetType.Catus -> 500f
            PetType.Dogus -> 500f
            PetType.Frogus -> 500f
        }
    }

    fun getFullHealthForPetType(type: PetType): Float {
        return when (type) {
            PetType.Catus -> 150_000f
            PetType.Dogus -> 200_000f
            PetType.Frogus -> 100_000f
        }
    }

    fun getFullPsychForPetType(type: PetType): Float {
        return when (type) {
            PetType.Catus -> 50_000f
            PetType.Dogus -> 40_000f
            PetType.Frogus -> 30_000f
        }
    }

    fun getFullSatietyForPetType(type: PetType): Float {
        return when (type) {
            PetType.Catus -> 50_000f
            PetType.Dogus -> 50_000f
            PetType.Frogus -> 50_000f
        }
    }

    /**
     * @return Amount in seconds
     */
    private fun getTimeInState(pet: Pet, state: SleepState): Long {
        var time = 43_200L
        petsActiveSleepTimes[pet.type]?.let { times ->
            times[state]?.let { time = it }
        }
        return time
    }

    /**
     * Returns random illness possibility in range 0 < p < MAXIMUM_ILLNESS_POSSIBILITY_ON_CREATION
     */
    private fun getRandomIllnessPossibility(): Float {
        return Random.Default.nextFloat() * MAXIMUM_ILLNESS_POSSIBILITY_ON_CREATION
    }

    companion object {
        // Ranges in seconds
        private val commonPetAgeToSecondsTable = mapOf(
            AgeState.Egg to 0L..10_800L,
            AgeState.NewBorn to 10_801L..97_200L,
            AgeState.Teen to 97_201L..183_600L,
            AgeState.Adult to 183_601L..270_000L,
            AgeState.Old to 270_001L..Long.MAX_VALUE
        )

        private val petsAges = mapOf(
            PetType.Catus to commonPetAgeToSecondsTable,
            PetType.Dogus to commonPetAgeToSecondsTable,
            PetType.Frogus to commonPetAgeToSecondsTable,
        )

        // Represents 'hunger speed' how much calories
        // are burned in 1 second
        private val commonPetHungerTable = mapOf(
            AgeState.Egg to 0f,
            AgeState.NewBorn to 1f,
            AgeState.Teen to 0.8f,
            AgeState.Adult to 0.6f,
            AgeState.Old to 0.3f
        )

        private val petsHunger = mapOf(
            PetType.Catus to commonPetHungerTable,
            PetType.Dogus to commonPetHungerTable,
            PetType.Frogus to commonPetHungerTable,
        )

        // Represents 'psych exhaustion speed' how much brain cells
        // are burned in 1 second
        private val commonPetPsychTable = mapOf(
            AgeState.Egg to 0f,
            AgeState.NewBorn to 0.3f,
            AgeState.Teen to 0.6f,
            AgeState.Adult to 0.8f,
            AgeState.Old to 1f
        )

        private val petsPsych = mapOf(
            PetType.Catus to commonPetPsychTable,
            PetType.Dogus to commonPetPsychTable,
            PetType.Frogus to commonPetPsychTable,
        )

        // Measured in seconds
        private val petsActiveSleepTimes = mapOf(
            PetType.Catus to mapOf(
                SleepState.Active to 28_800L,
                SleepState.Sleep to 57_600L,
            ),
            PetType.Dogus to mapOf(
                SleepState.Active to 57_600L,
                SleepState.Sleep to 28_800L,
            ),
            PetType.Frogus to mapOf(
                SleepState.Active to 43_200L,
                SleepState.Sleep to 43_200L,
            ),
        )

        const val BASIC_CLOUDINESS_MULTIPLIER = 1f
        const val ACTIVE_TEMPERATURE_MULTIPLIER = 0.5f
        const val SLEEP_TEMPERATURE_MULTIPLIER = 0.25f
        const val DEATH_OF_OLD_AGE_POSSIBILITY_INC = 0.0005f
        const val MAXIMUM_ILLNESS_POSSIBILITY_ON_CREATION = 0.0167f
    }
}