package bav.petus

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import bav.petus.cache.PetsDatabase
import bav.petus.cache.getDatabaseBuilder
import bav.petus.cache.getPetsDatabase
import bav.petus.core.datastore.createDataStore
import bav.petus.core.dialog.DialogNode
import bav.petus.core.dialog.DialogSystem
import bav.petus.core.engine.Engine
import bav.petus.core.engine.QuestSystem
import bav.petus.core.engine.UserProfileData
import bav.petus.core.engine.UserStats
import bav.petus.core.inventory.InventoryItem
import bav.petus.core.resources.StringId
import bav.petus.core.time.TimeRepository
import bav.petus.core.time.getTimestampSecondsSinceEpoch
import bav.petus.extension.epochTimeToString
import bav.petus.model.AgeState
import bav.petus.model.Pet
import bav.petus.model.PetType
import bav.petus.model.Place
import bav.petus.network.WeatherApi
import bav.petus.repo.PetsRepository
import bav.petus.repo.WeatherRepository
import bav.petus.useCase.WeatherAttitudeUseCase
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.dsl.module

class KoinHelper : KoinComponent {
    private val sdk: PetsSDK by inject<PetsSDK>()
    private val petsRepo: PetsRepository by inject<PetsRepository>()
    private val weatherRepo: WeatherRepository by inject<WeatherRepository>()
    private val timeRepo: TimeRepository by inject<TimeRepository>()
    private val engine: Engine by inject<Engine>()
    private val dialogSystem: DialogSystem by inject<DialogSystem>()
    private val userStats: UserStats by inject<UserStats>()
    private val questSystem: QuestSystem by inject<QuestSystem>()

    fun getAllPetsInZooFlow(): Flow<List<Pet>> {
        return petsRepo.getAllPetsInZooFlow()
    }

    fun getAllPetsInCemeteryFlow(): Flow<List<Pet>> {
        return petsRepo.getAllPetsInCemeteryFlow()
    }

    fun getPetByIdFlow(petId: Long): Flow<Pet?> {
        return petsRepo.getPetByIdFlow(petId)
    }

    fun isAllowedToFeedPet(pet: Pet): Boolean {
        return engine.isAllowedToFeedPet(pet)
    }

    suspend fun feedPet(pet: Pet) {
        engine.feedPet(pet)
    }

    fun isAllowedToPlayWithPet(pet: Pet): Boolean {
        return engine.isAllowedToPlayWithPet(pet)
    }

    suspend fun playWithPet(pet: Pet) {
        engine.playWithPet(pet)
    }

    fun isAllowedToCleanAfterPet(pet: Pet): Boolean {
        return engine.isAllowedToCleanAfterPet(pet)
    }

    suspend fun cleanAfterPet(pet: Pet) {
        engine.cleanAfterPet(pet)
    }

    fun isAllowedToHealPet(pet: Pet): Boolean {
        return engine.isAllowedToHealPet(pet)
    }

    suspend fun healPetIllness(pet: Pet) {
        engine.healPetIllness(pet)
    }

    fun isAllowedToWakeUpPet(pet: Pet): Boolean {
        return engine.isAllowedToWakeUpPet(pet)
    }

    suspend fun wakeUpPet(pet: Pet) {
        engine.wakeUpPet(pet)
    }

    suspend fun killPet(pet: Pet) {
        engine.killPet(pet)
    }

    suspend fun resurrectPet(pet: Pet) {
        engine.resurrectPet(pet)
    }

    suspend fun changePetAgeState(pet: Pet, state: AgeState) {
        engine.changePetAgeState(pet, state)
    }

    suspend fun changePetPlace(pet: Pet, place: Place) {
        engine.changePetPlace(pet, place)
    }

    suspend fun startDialog(pet: Pet): DialogNode? {
        return dialogSystem.startDialog(pet)
    }

    suspend fun chooseDialogAnswer(index: Int): DialogNode? {
        return dialogSystem.chooseAnswer(index)
    }

    suspend fun maskDialogText(petType: PetType, text: String): String {
        return dialogSystem.maskDialogText(petType, text)
    }

    fun getPetTypeDescription(type: PetType): StringId {
        return engine.getPetTypeDescription(type)
    }

    fun getUserProfileFlow(): Flow<UserProfileData> {
        return userStats.getUserProfileFlow()
    }

    suspend fun getAvailablePetTypes(): Set<PetType> {
        return userStats.getAvailablePetTypes()
    }

    suspend fun addInventoryItem(item: InventoryItem) {
        userStats.addInventoryItem(item)
    }

    suspend fun removeInventoryItem(item: InventoryItem) {
        userStats.removeInventoryItem(item)
    }

    suspend fun emitQuestEvent(event: QuestSystem.Event) {
        questSystem.onEvent(event)
    }

    suspend fun createNewPet(
        name: String,
        type: PetType,
    ) {
        engine.createNewPet(name, type)
    }

    suspend fun retrieveWeatherInBackground(
        latitude: Double?,
        longitude: Double?,
        info: String?,
    ): Boolean {
        return sdk.retrieveWeatherInBackground(latitude, longitude, info)
    }

    /**
     * Is called from iOS, similar to onResume
     */
    suspend fun applicationDidBecomeActive() {
        sdk.applicationDidBecomeActive()
    }

    fun getAllWeatherRecordsFlow(): Flow<List<String>> {
        return weatherRepo.getAllWeatherRecordsFlow()
    }

    suspend fun isTimeToFetchWeather(): Boolean {
        val now = getTimestampSecondsSinceEpoch()
        val lastTime = timeRepo.getLastTimestampOfWeatherRequest()
        return (now - lastTime) > TimeRepository.WEATHER_REQUEST_PERIOD_SEC
    }

    fun getPetSatietyFraction(pet: Pet): Float {
        return pet.satiety / engine.getFullSatietyForPetType(pet.type)
    }

    fun getPetPsychFraction(pet: Pet): Float {
        return pet.psych / engine.getFullPsychForPetType(pet.type)
    }

    fun getPetHealthFraction(pet: Pet): Float {
        return pet.health / engine.getFullHealthForPetType(pet.type)
    }

    fun getNextSleepStateChangeTimestampString(pet: Pet): String {
        return engine.getNextSleepStateChangeTimestamp(pet).epochTimeToString()
    }
}

fun initKoin() {
    startKoin {
        modules(module {
            single<WeatherApi> {
                WeatherApi()
            }

            single<PetsDatabase> {
                getPetsDatabase(
                    builder = getDatabaseBuilder()
                )
            }

            single<DataStore<Preferences>> {
                createDataStore()
            }

            single<TimeRepository> {
                TimeRepository(dataStore = get())
            }

            single<PetsRepository> {
                PetsRepository(database = get())
            }

            single<WeatherRepository> {
                WeatherRepository(
                    database = get(),
                )
            }

            single<WeatherAttitudeUseCase> {
                WeatherAttitudeUseCase()
            }

            single<Engine> {
                Engine(
                    petsRepo = get(),
                    timeRepo = get(),
                    weatherRepo = get(),
                    weatherAttitudeUseCase = get(),
                    userStats = get(),
                    questSystem = get(),
                )
            }

            single<PetsSDK> {
                PetsSDK(
                    weatherApi = get(),
                    weatherRepo = get(),
                    engine = get(),
                    timeRepo = get(),
                )
            }

            single<DialogSystem> {
                DialogSystem(
                    userStats = get(),
                    questSystem = get(),
                )
            }

            single {
                UserStats(
                    dataStore = get(),
                )
            }

            single {
                QuestSystem(
                    dataStore = get(),
                    petsRepo = get(),
                    userStats = get(),
                )
            }
        })
    }
}