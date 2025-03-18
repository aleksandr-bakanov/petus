package bav.petus

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import bav.petus.cache.PetsDatabase
import bav.petus.cache.getDatabaseBuilder
import bav.petus.cache.getPetsDatabase
import bav.petus.core.datastore.createDataStore
import bav.petus.core.time.TimeRepository
import bav.petus.entity.WeatherDto
import bav.petus.model.Pet
import bav.petus.network.WeatherApi
import bav.petus.repo.PetsRepository
import bav.petus.repo.WeatherRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.dsl.module

class KoinHelper : KoinComponent {
    private val sdk: PetsSDK by inject<PetsSDK>()

    suspend fun getWeather(latitude: Double, longitude: Double): WeatherDto {
        return sdk.getWeather(latitude, longitude)
    }

    suspend fun getPets(): List<Pet> {
        return sdk.getPets()
    }

    suspend fun addPet(name: String) {
        sdk.addPet(Pet(name = name))
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
                    weatherApi = get(),
                    timeRepository = get(),
                    dataStore = get(),
                )
            }

            single<PetsSDK> {
                PetsSDK(
                    petsRepository = get(),
                    weatherRepository = get(),
                )
            }
        })
    }
}