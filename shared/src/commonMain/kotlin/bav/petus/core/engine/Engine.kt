package bav.petus.core.engine

import bav.petus.cache.WeatherRecord
import bav.petus.core.inventory.InventoryItem
import bav.petus.core.resources.StringId
import bav.petus.core.time.TimeRepository
import bav.petus.core.time.getTimestampSecondsSinceEpoch
import bav.petus.model.AgeState
import bav.petus.model.BodyState
import bav.petus.model.BurialType
import bav.petus.model.FractalType
import bav.petus.model.HistoryEvent
import bav.petus.model.Pet
import bav.petus.model.PetType
import bav.petus.model.Place
import bav.petus.model.SleepState
import bav.petus.model.WeatherAttitude
import bav.petus.repo.HistoryRepository
import bav.petus.repo.PetsRepository
import bav.petus.repo.WeatherRepository
import bav.petus.useCase.WeatherAttitudeUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlin.random.Random

data class GameUpdateState(
    val fraction: Float,
)

class Engine(
    private val timeRepo: TimeRepository,
    private val petsRepo: PetsRepository,
    private val weatherRepo: WeatherRepository,
    private val weatherAttitudeUseCase: WeatherAttitudeUseCase,
    private val userStats: UserStats,
    private val questSystem: QuestSystem,
    private val historyRepo: HistoryRepository,
) {

    private val _gameStateUpdateFlow: MutableSharedFlow<GameUpdateState?> = MutableSharedFlow()
    val gameStateUpdateFlow = _gameStateUpdateFlow.asSharedFlow()

    suspend fun createNewPet(
        name: String,
        type: PetType,
    ) {
        val pet = Pet(
            name = name.trim(),
            type = type,
            fractalType = getRandomFractalType(),
            creationTime = getTimestampSecondsSinceEpoch(),
            illnessPossibility = getRandomIllnessPossibility(),
            health = getFullHealthForPetType(type),
            psych = getFullPsychForPetType(type),
            satiety = getFullSatietyForPetType(type),
        )
        petsRepo.insertPet(pet)
        historyRepo.getLatestPetId()?.let { id ->
            historyRepo.recordHistoryEvent(id, HistoryEvent.PetCreated)
        }
    }

    private fun getRandomFractalType(): FractalType {
        val randomIndex = Random.Default.nextInt(FractalType.entries.size)
        return FractalType.entries[randomIndex]
    }

    fun isAllowedToFeedPet(pet: Pet): Boolean {
        return pet.bodyState == BodyState.Alive &&
                pet.ageState != AgeState.Egg &&
                pet.satiety < getFullSatietyForPetType(pet.type) * 0.8f &&
                pet.sleep.not()
    }

    suspend fun feedPet(pet: Pet) {
        val newPet = pet.copy(
            satiety = getFullSatietyForPetType(pet.type)
        )
        petsRepo.updatePet(newPet)
        historyRepo.recordHistoryEvent(pet.id, HistoryEvent.PetFeed)
    }

    fun isAllowedToPlayWithPet(pet: Pet): Boolean {
        return pet.bodyState == BodyState.Alive &&
                pet.ageState != AgeState.Egg &&
                pet.psych < getFullPsychForPetType(pet.type) * 0.8f &&
                pet.sleep.not() &&
                isPetStillAngryAfterForcefulWakeUp(pet).not()
    }

    fun isPetStillAngryAfterForcefulWakeUp(pet: Pet): Boolean {
        val now = getTimestampSecondsSinceEpoch()
        return now < pet.timestampPlayAllowed
    }

    suspend fun playWithPet(pet: Pet) {
        val newPet = pet.copy(
            psych = getFullPsychForPetType(pet.type)
        )
        val currentLanguageKnowledge = userStats.getLanguageKnowledge(pet.type)
        val newLanguageKnowledge = currentLanguageKnowledge + LANGUAGE_KNOWLEDGE_INCREMENT
        userStats.saveLanguageKnowledge(pet.type, newLanguageKnowledge)
        petsRepo.updatePet(newPet)
        questSystem.onEvent(QuestSystem.Event.LanguageKnowledgeChanged(pet.type, newLanguageKnowledge))
        historyRepo.recordHistoryEvent(pet.id, HistoryEvent.PetPlay)
    }

    fun isAllowedToCleanAfterPet(pet: Pet): Boolean {
        return pet.isPooped
    }

    suspend fun cleanAfterPet(pet: Pet) {
        val newPet = pet.copy(
            isPooped = false,
        )
        petsRepo.updatePet(newPet)
        historyRepo.recordHistoryEvent(pet.id, HistoryEvent.PetCleanUp)
    }

    fun isAllowedToHealPet(pet: Pet): Boolean {
        return pet.illness && pet.bodyState == BodyState.Alive
    }

    suspend fun healPetIllness(pet: Pet) {
        val newPet = pet.copy(
            illness = false,
        )
        petsRepo.updatePet(newPet)
        historyRepo.recordHistoryEvent(pet.id, HistoryEvent.PetGetHealed)
    }

    fun isAllowedToWakeUpPet(pet: Pet): Boolean {
        return pet.sleep && pet.bodyState == BodyState.Alive && pet.ageState != AgeState.Egg
    }

    suspend fun wakeUpPet(pet: Pet) {
        val now = getTimestampSecondsSinceEpoch()
        val fullPsych = getFullPsychForPetType(pet.type)
        val newPsych = (pet.psych - fullPsych / 2f).coerceIn(0f, fullPsych)
        val newPet = pet.copy(
            activeSleepState = SleepState.Active,
            lastActiveSleepSwitchTimestamp = now,
            isPooped = true,
            psych = newPsych,
            timestampPlayAllowed = now + PET_NOT_ALLOWED_TO_PLAY_INTERVAL_SEC,
        )
        petsRepo.updatePet(newPet)
        historyRepo.recordHistoryEvent(pet.id, HistoryEvent.PetForciblyWakeUp)
    }

    fun isAllowedToBuryPet(pet: Pet): Boolean {
        return (pet.bodyState == BodyState.Dead || pet.bodyState == BodyState.Zombie) &&
                pet.place == Place.Zoo
    }

    suspend fun buryPet(pet: Pet) {
        val buriedPet = pet.copy(burialType = BurialType.Buried)
        changePetPlace(buriedPet, Place.Cemetery)
        historyRepo.recordHistoryEvent(pet.id, HistoryEvent.PetBuried)
    }

    suspend fun isAllowedToResurrectPet(pet: Pet): Boolean {
        val userData = userStats.getUserProfileFlow().first()
        val petsInZoo = petsRepo.getAllPetsInZooFlow().first()
        return userData.abilities.contains(Ability.Necromancy) &&
                pet.place == Place.Cemetery &&
                pet.burialType != BurialType.Exhumated &&
                petsInZoo.size < userData.zooSize
    }

    suspend fun isAllowedToForgetPet(pet: Pet): Boolean {
        val userData = userStats.getUserProfileFlow().first()
        return userData.abilities.contains(Ability.Meditation) &&
                pet.type == PetType.Fractal &&
                pet.place == Place.Zoo
    }

    suspend fun forgetPet(pet: Pet) {
        changePetPlace(pet, Place.Limbo)
        historyRepo.recordHistoryEvent(pet.id, HistoryEvent.PetForgotten)
    }

    suspend fun resurrectPetAsZombie(pet: Pet) {
        val now = getTimestampSecondsSinceEpoch()
        val newPet = pet.copy(
            lastActiveSleepSwitchTimestamp = now,
            activeSleepState = SleepState.Active,
            bodyState = BodyState.Zombie,
            place = Place.Zoo,
        )
        petsRepo.updatePet(newPet)
        historyRepo.recordHistoryEvent(pet.id, HistoryEvent.PetResurrected)
    }

    fun isAllowedToSpeakWithPet(pet: Pet): Boolean {
        return pet.sleep.not() &&
                pet.place == Place.Zoo &&
                (pet.bodyState == BodyState.Alive || pet.bodyState == BodyState.Zombie || pet.bodyState == BodyState.Spirit) &&
                pet.ageState != AgeState.Egg
    }

    suspend fun addItemToPetInventory(pet: Pet, item: InventoryItem) {
        petsRepo.updatePet(
            pet = pet.copy(
                inventory = pet.inventory.addItem(item)
            )
        )
    }

    suspend fun removeItemFromPetInventory(pet: Pet, item: InventoryItem) {
        val newInventory = pet.inventory.removeItem(item)
        if (newInventory != null) {
            petsRepo.updatePet(
                pet = pet.copy(
                    inventory = newInventory
                )
            )
        }
    }

    fun isPetSpeakLatin(pet: Pet): Boolean {
        return pet.bodyState == BodyState.Zombie || pet.bodyState == BodyState.Spirit
    }

    fun isPetSpeakPolish(pet: Pet): Boolean {
        return pet.type == PetType.Bober
    }

    fun isPetSpeakMath(pet: Pet): Boolean {
        return pet.type == PetType.Fractal
    }

    fun isPetHungry(pet: Pet): Boolean {
        return getPetSatietyFraction(pet) < 0.66f
    }

    fun isPetBored(pet: Pet): Boolean {
        return getPetPsychFraction(pet) < 0.66f
    }

    fun isPetLowHealth(pet: Pet): Boolean {
        return getPetHealthFraction(pet) < 0.5f
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

    suspend fun killPet(pet: Pet) {
        val now = getTimestampSecondsSinceEpoch()
        val deadPet = makePetDead(pet, now)
        petsRepo.updatePet(deadPet)
    }

    suspend fun resurrectPet(pet: Pet) {
        val newPet = pet.copy(bodyState = BodyState.Alive, timeOfDeath = 0L)
        petsRepo.updatePet(newPet)
    }

    suspend fun changePetPlace(pet: Pet, newPlace: Place) {
        val newPet = pet.copy(place = newPlace)
        petsRepo.updatePet(newPet)
        questSystem.onEvent(QuestSystem.Event.PetMovedToPlace(pet, newPlace))
    }

    suspend fun changePetAgeState(pet: Pet, newState: AgeState) {
        val newPet = pet.copy(ageState = newState)
        petsRepo.updatePet(newPet)
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

    fun getPetTypeDescription(type: PetType): StringId {
        return when (type) {
            PetType.Catus -> StringId.PetTypeDescriptionCatus
            PetType.Dogus -> StringId.PetTypeDescriptionDogus
            PetType.Frogus -> StringId.PetTypeDescriptionFrogus
            PetType.Bober -> StringId.PetTypeDescriptionBober
            PetType.Fractal -> StringId.PetTypeDescriptionFractal
        }
    }

    suspend fun updateGameState() {
        _gameStateUpdateFlow.emit(null)

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
        var pets = petsRepo.getAllPetsInZoo().filter {
            it.bodyState == BodyState.Alive
        }
        val periodsPassed =
            (currentTime - lastTimestamp) / TimeRepository.CYCLE_PERIOD_IN_SECONDS

        for (periodIndex in 0 until periodsPassed) {
            _gameStateUpdateFlow.emit(
                GameUpdateState(
                    fraction = periodIndex.toFloat() / periodsPassed.toFloat(),
                )
            )
            // periodTime is a start point of a certain period - this may matter only for selection
            // of weather record. For the rest of the calculations it shouldn't matter.
            val periodTimestamp =
                lastTimestamp + periodIndex * TimeRepository.CYCLE_PERIOD_IN_SECONDS
            val weatherRecord = weatherRepo.getClosestWeather(
                timestamp = periodTimestamp
            )
            // Update all pets
            pets = pets.map { updatePet(it, weatherRecord, periodTimestamp) }
        }

        // Save results in DB
        pets.forEach {
            petsRepo.updatePet(it)
        }

        _gameStateUpdateFlow.emit(null)

        // Saving latest periodTime as new lastTimestamp
        val newLastTimestamp = lastTimestamp + periodsPassed * TimeRepository.CYCLE_PERIOD_IN_SECONDS
        timeRepo.saveLastTimestamp(newLastTimestamp)
        questSystem.onEvent(QuestSystem.Event.Tick(newLastTimestamp))
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
    private suspend fun updatePet(
        pet: Pet,
        weatherRecord: WeatherRecord?,
        periodTimestamp: Long,
    ): Pet {
        if (pet.bodyState != BodyState.Alive) return pet

        var newPet = pet.copy()

        val petAgeInSeconds = periodTimestamp - pet.creationTime
        newPet = newPet.copy(
            ageState = getPetAgeState(newPet.type, petAgeInSeconds),
        )

        // If pet changed age state we need to record it in history
        if (pet.ageState != newPet.ageState) {
            val historyEvent = when (newPet.ageState) {
                AgeState.Egg -> null
                AgeState.NewBorn -> HistoryEvent.PetBecomeNewborn
                AgeState.Teen -> HistoryEvent.PetBecomeTeen
                AgeState.Adult -> HistoryEvent.PetBecomeAdult
                AgeState.Old -> HistoryEvent.PetBecomeOld
            }
            historyEvent?.let { event ->
                historyRepo.recordHistoryEvent(
                    petId = pet.id,
                    event = event,
                    timestamp = periodTimestamp,
                )
            }
        }

        // If pet became new born on this cycle we need to awake him
        // and remember this first time of awakening
        if (pet.ageState == AgeState.Egg && newPet.ageState == AgeState.NewBorn) {
            newPet = newPet.copy(
                activeSleepState = SleepState.Active,
                lastActiveSleepSwitchTimestamp = periodTimestamp,
            )
            historyRepo.recordHistoryEvent(pet.id, HistoryEvent.PetWakeUp, periodTimestamp)
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
                // Fractals can't die because of low health
                val minimumHealth = if (newPet.type == PetType.Fractal) 1f else 0f
                newPet = newPet.copy(
                    health = newHealth.coerceIn(minimumHealth, getFullHealthForPetType(pet.type))
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
                        if (newPet.illness) {
                            historyRepo.recordHistoryEvent(pet.id, HistoryEvent.PetGetIll, periodTimestamp)
                        }
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
                        val sleepActiveHistoryEvent = if (newPet.sleep) HistoryEvent.PetSleep else HistoryEvent.PetWakeUp
                        historyRepo.recordHistoryEvent(pet.id, sleepActiveHistoryEvent, periodTimestamp)

                        // If pet woke up then it immediately poops
                        if (newPet.activeSleepState == SleepState.Active) {
                            newPet = newPet.copy(isPooped = true)
                            historyRepo.recordHistoryEvent(pet.id, HistoryEvent.PetPoop, periodTimestamp)
                        }
                    }
                }
            }
        }

        // Additional checks for Old pets (except for fractals)
        if (newPet.ageState == AgeState.Old &&
            newPet.bodyState == BodyState.Alive &&
            newPet.type != PetType.Fractal
        ) {
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

    private suspend fun makePetDead(pet: Pet, periodTime: Long): Pet {
        questSystem.onEvent(QuestSystem.Event.PetDied(pet.id))
        historyRepo.recordHistoryEvent(pet.id, HistoryEvent.PetDied, periodTime)
        // TODO: save some info about cause of death
        return pet.copy(
            bodyState = BodyState.Dead,
            timeOfDeath = periodTime,
        )
    }

    private fun getPetAgeState(petType: PetType, petAgeInSeconds: Long): AgeState {
        var state = AgeState.Egg
        val ages = getPetAgeToSecondsTable(petType)
        for (pair in ages) {
            if (pair.value.contains(petAgeInSeconds)) {
                state = pair.key
                break
            }
        }
        return state
    }

    private fun getPetAgeToSecondsTable(petType: PetType): Map<AgeState, LongRange> {
        return when (petType) {
            PetType.Catus -> commonPetAgeToSecondsTable
            PetType.Dogus -> commonPetAgeToSecondsTable
            PetType.Frogus -> commonPetAgeToSecondsTable
            PetType.Bober -> commonPetAgeToSecondsTable
            PetType.Fractal -> fractalAgeToSecondsTable
        }
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
        if (pet.sleep.not()) resultPsych -= (basicPsych * BASIC_CLOUDINESS_MULTIPLIER * weatherAttitude.toCloudPercentage).toFloat()

        // TEMPERATURE
        if (pet.sleep) resultPsych -= (basicPsych * SLEEP_TEMPERATURE_MULTIPLIER * weatherAttitude.toTemperature).toFloat()
        else resultPsych -= (basicPsych * ACTIVE_TEMPERATURE_MULTIPLIER * weatherAttitude.toTemperature).toFloat()

        // HUMIDITY
        resultPsych -= (basicPsych * BASIC_HUMIDITY_MULTIPLIER * weatherAttitude.toHumidity).toFloat()

        // WIND SPEED
        resultPsych -= (basicPsych * BASIC_WIND_MULTIPLIER * weatherAttitude.toWindSpeed).toFloat()

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
        val hungerByAgeState = getPetsHungerByAgeState(pet.type)
        hungerByAgeState[pet.ageState]?.let { hunger = it }
        return hunger
    }

    private fun getPetsHungerByAgeState(petType: PetType): Map<AgeState, Float> {
        return when (petType) {
            PetType.Catus -> commonPetHungerTable
            PetType.Dogus -> commonPetHungerTable
            PetType.Frogus -> commonPetHungerTable
            PetType.Bober -> commonPetHungerTable
            PetType.Fractal -> commonPetHungerTable
        }
    }

    /**
     * Returns basic consumption of psych per one second based on pet type and age
     * presuming pet is active
     */
    private fun getPsychBasedOnPetTypeAndAgeState(pet: Pet): Float {
        var psych = 0f
        val psychByAgeState = getPetsPsychByAgeState(pet.type)
        psychByAgeState[pet.ageState]?.let { psych = it }
        return psych
    }

    private fun getPetsPsychByAgeState(petType: PetType): Map<AgeState, Float> {
        return when (petType) {
            PetType.Catus -> commonPetPsychTable
            PetType.Dogus -> commonPetPsychTable
            PetType.Frogus -> commonPetPsychTable
            PetType.Bober -> commonPetPsychTable
            PetType.Fractal -> commonPetPsychTable
        }
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
            PetType.Bober -> 500f
            PetType.Fractal -> 500f
        }
    }

    private fun getFullHealthForPetType(type: PetType): Float {
        return when (type) {
            PetType.Catus -> 300_000f
            PetType.Dogus -> 300_000f
            PetType.Frogus -> 200_000f
            PetType.Bober -> 400_000f
            PetType.Fractal -> 300_000f
        }
    }

    private fun getFullPsychForPetType(type: PetType): Float {
        return when (type) {
            PetType.Catus -> 50_000f
            PetType.Dogus -> 40_000f
            PetType.Frogus -> 30_000f
            PetType.Bober -> 50_000f
            PetType.Fractal -> 30_000f
        }
    }

    private fun getFullSatietyForPetType(type: PetType): Float {
        return when (type) {
            PetType.Catus -> 50_000f
            PetType.Dogus -> 50_000f
            PetType.Frogus -> 50_000f
            PetType.Bober -> 50_000f
            PetType.Fractal -> 50_000f
        }
    }

    /**
     * @return Amount in seconds
     */
    private fun getTimeInState(pet: Pet, state: SleepState): Long {
        return when (pet.type) {
            PetType.Catus -> {
                when (state) {
                    SleepState.Active -> HOUR
                    SleepState.Sleep -> HOUR * 2
                }
            }
            PetType.Dogus -> {
                when (state) {
                    SleepState.Active -> HOUR * 2
                    SleepState.Sleep -> HOUR
                }
            }
            PetType.Frogus -> {
                when (state) {
                    SleepState.Active -> (HOUR * 3) / 2
                    SleepState.Sleep -> (HOUR * 3) / 2
                }
            }
            PetType.Bober -> {
                when (state) {
                    SleepState.Active -> HOUR * 2
                    SleepState.Sleep -> HOUR
                }
            }

            PetType.Fractal -> {
                when (state) {
                    SleepState.Active -> (HOUR * 3) / 2
                    SleepState.Sleep -> (HOUR * 3) / 2
                }
            }
        }
    }

    /**
     * Returns random illness possibility in range 0 < p < MAXIMUM_ILLNESS_POSSIBILITY_ON_CREATION
     */
    private fun getRandomIllnessPossibility(): Float {
        return Random.Default.nextFloat() * MAXIMUM_ILLNESS_POSSIBILITY_ON_CREATION
    }

    companion object {
        const val HOUR: Long = 3600L
        const val DAY: Long = HOUR * 24L

        // Ranges in seconds
        private val commonPetAgeToSecondsTable = mapOf(
            AgeState.Egg to (0L until 60),                    // 1 minute
            AgeState.NewBorn to (60 until DAY * 7),           // 7 days (-1 hour)
            AgeState.Teen to (DAY * 7 until DAY * 7 + 1),       // Disable Teen state making it short
            AgeState.Adult to (DAY * 7 + 1 until DAY * 14),      // 7 days
            AgeState.Old to (DAY * 14 until Long.MAX_VALUE),
        )

        // Ranges in seconds
        private val fractalAgeToSecondsTable = mapOf(
            AgeState.Egg to (0L until 60),                    // 1 minute
            AgeState.NewBorn to (60 until DAY * 7),           // 7 days (-1 hour)
            AgeState.Teen to (DAY * 7 until DAY * 7 + 1),       // Disable Teen state making it short
            AgeState.Adult to (DAY * 7 + 1 until Long.MAX_VALUE - 1),      // Forever adult
            AgeState.Old to (Long.MAX_VALUE - 1 until Long.MAX_VALUE),
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

        // Represents 'psych exhaustion speed' how much brain cells
        // are burned in 1 second
        private val commonPetPsychTable = mapOf(
            AgeState.Egg to 0f,
            AgeState.NewBorn to 0.3f,
            AgeState.Teen to 0.6f,
            AgeState.Adult to 0.8f,
            AgeState.Old to 1f
        )

        const val BASIC_CLOUDINESS_MULTIPLIER = 2f
        const val BASIC_HUMIDITY_MULTIPLIER = 2f
        const val BASIC_WIND_MULTIPLIER = 2f
        const val ACTIVE_TEMPERATURE_MULTIPLIER = 0.5f
        const val SLEEP_TEMPERATURE_MULTIPLIER = 0.25f
        const val DEATH_OF_OLD_AGE_POSSIBILITY_INC = 0.000001f
        const val MAXIMUM_ILLNESS_POSSIBILITY_ON_CREATION = 0.00835f
        const val LANGUAGE_KNOWLEDGE_INCREMENT = 3
        const val PET_NOT_ALLOWED_TO_PLAY_INTERVAL_SEC = 12 * HOUR
    }
}