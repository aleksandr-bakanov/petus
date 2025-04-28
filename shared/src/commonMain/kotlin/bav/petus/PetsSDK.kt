package bav.petus

import bav.petus.cache.WeatherRecord
import bav.petus.core.engine.Engine
import bav.petus.core.time.TimeRepository
import bav.petus.core.time.getTimestampSecondsSinceEpoch
import bav.petus.entity.WeatherDto
import bav.petus.model.Pet
import bav.petus.network.WeatherApi
import bav.petus.repo.PetsRepository
import bav.petus.repo.WeatherRepository

class PetsSDK(
    private val weatherApi: WeatherApi,
    private val weatherRepo: WeatherRepository,
    private val engine: Engine,
    private val timeRepo: TimeRepository,
) {

    /**
     * Called from iOS side
     *
     * @return true if success
     */
    @Throws(Exception::class)
    suspend fun retrieveWeatherInBackground(
        latitude: Double?,
        longitude: Double?,
        info: String?,
    ): Boolean {
        val weatherDto: WeatherDto? = if (latitude != null && longitude != null) {
            val result = weatherApi.getWeather(latitude = latitude, longitude = longitude)
            result.getOrNull()
        } else {
            null
        }

        if (weatherDto != null) {
            val timestamp = getTimestampSecondsSinceEpoch()
            weatherRepo.insertWeatherRecord(
                record = WeatherRecord(
                    id = 0L,
                    timestampSecondsSinceEpoch = timestamp,
                    cloudPercentage = weatherDto.cloudPercentage,
                    humidity = weatherDto.humidity,
                    temperature = weatherDto.temperature,
                    windSpeed = weatherDto.windSpeed,
                    info = info,
                )
            )
            timeRepo.saveLastTimestampOfWeatherRequest(timestamp)
        }

        return weatherDto != null
    }

    suspend fun applicationDidBecomeActive() {
        engine.updateGameState()
    }
}