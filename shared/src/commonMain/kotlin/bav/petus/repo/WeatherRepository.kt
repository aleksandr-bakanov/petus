package bav.petus.repo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import bav.petus.core.time.TimeRepository
import bav.petus.entity.WeatherDto
import bav.petus.model.Pet
import bav.petus.network.WeatherApi
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Responsibilities:
 *   - Provides current weather dto when asked
 *   - Deals with API errors
 *   - Somehow informs that weather is inaccessible for any reason so it wasn't affect calculations
 */
class WeatherRepository(
    private val weatherApi: WeatherApi,
    private val timeRepository: TimeRepository,
    private val dataStore: DataStore<Preferences>,
) {

    suspend fun getWeather(
        latitude: Double = 52.3676,
        longitude: Double = 4.9041,
    ): WeatherDto {
        return fakeWeatherDto

//        val dto: WeatherDto?
//        if (timeRepository.isTimeToFetchWeather()) {
//            val result = weatherApi.getWeather(52.3676, 4.9041)
//            dto = result.getOrNull()
//            dataStore.edit { store ->
//                store[WEATHER_CACHE_KEY] = Json.encodeToString(dto)
//            }
//        } else {
//            dto = Json.decodeFromString<WeatherDto?>(
//                dataStore.data.first()[WEATHER_CACHE_KEY].orEmpty()
//            )
//        }
//        return dto
    }

    companion object {
        private val fakeWeatherDto = WeatherDto(
            cloudPercentage = 0,
            feelsLike = 20,
            humidity = 10,
            maxTemp = 25,
            minTemp = 18,
            sunrise = null,
            sunset = null,
            temperature = 21,
            windDegrees = 0,
            windSpeed = 0.0,
        )

        private val WEATHER_CACHE_KEY = stringPreferencesKey("weather_cache_key")
    }
}