package bav.petus

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import bav.petus.cache.PetsDatabase
import bav.petus.cache.getDatabaseBuilder
import bav.petus.cache.getPetsDatabase
import bav.petus.core.datastore.createDataStore
import bav.petus.core.dialog.DialogSystem
import bav.petus.core.engine.Engine
import bav.petus.core.engine.QuestSystem
import bav.petus.core.engine.UserStats
import bav.petus.core.migration.Migrations
import bav.petus.core.time.TimeRepository
import bav.petus.network.WeatherApi
import bav.petus.repo.HistoryRepository
import bav.petus.repo.PetsRepository
import bav.petus.repo.WeatherRepository
import bav.petus.useCase.PetImageUseCase
import bav.petus.useCase.WeatherAttitudeUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.dsl.module

class KoinHelper : KoinComponent {
    private val sdk: PetsSDK by inject<PetsSDK>()
    private val timeRepo: TimeRepository by inject<TimeRepository>()

    suspend fun retrieveWeatherInBackground(
        latitude: Double?,
        longitude: Double?,
        info: String?,
    ) {
        sdk.retrieveWeatherInBackground(latitude, longitude, info)
    }

    /**
     * Is called from iOS, similar to onResume
     */
    suspend fun applicationDidBecomeActive() {
        sdk.applicationDidBecomeActive()
    }

    suspend fun isTimeToFetchWeather(): Boolean {
        return timeRepo.isTimeToFetchWeather()
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
                    historyRepo = get(),
                )
            }

            single<PetsSDK> {
                PetsSDK(
                    weatherApi = get(),
                    weatherRepo = get(),
                    engine = get(),
                    timeRepo = get(),
                    migrations = get(),
                )
            }

            single<DialogSystem> {
                DialogSystem(
                    userStats = get(),
                    questSystem = get(),
                    engine = get(),
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

            single {
                PetImageUseCase(
                    engine = get(),
                )
            }

            single {
                HistoryRepository(
                    database = get(),
                )
            }

            single {
                Migrations(
                    dataStore = get(),
                    userStats = get(),
                    questSystem = get(),
                )
            }
        })
    }
}