package bav.petus

import bav.petus.cache.IOSDatabaseDriverFactory
import bav.petus.entity.WeatherDto
import bav.petus.network.WeatherApi
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

    fun addPet(name: String) {
        sdk.addPet(Pet(name = name))
    }
}

fun initKoin() {
    startKoin {
        modules(module {
            single<WeatherApi> { WeatherApi() }
            single<PetsSDK> {
                PetsSDK(
                    databaseDriverFactory = IOSDatabaseDriverFactory(),
                    api = get()
                )
            }
        })
    }
}