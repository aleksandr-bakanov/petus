package bav.petus.android.helpers

import android.content.Context
import android.location.Location
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import bav.petus.cache.WeatherRecord
import bav.petus.core.location.LocationHelper
import bav.petus.core.location.getLocation
import bav.petus.core.time.TimeRepository
import bav.petus.core.time.getTimestampSecondsSinceEpoch
import bav.petus.entity.WeatherDto
import bav.petus.network.WeatherApi
import bav.petus.repo.PetsRepository
import bav.petus.repo.WeatherRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class WorkManagerHelper(
    private val context: Context,
) {
    fun enqueuePeriodicPetsUpdateWorker() {
        val periodicWorkRequest =
            PeriodicWorkRequestBuilder<PetsUpdateWorker>(PetsUpdateWorker.PERIOD_IN_HOURS, TimeUnit.HOURS)
                .setInitialDelay(1, TimeUnit.HOURS)
                .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PetsUpdateWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }
}

class PetsUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val locationHelper: LocationHelper by inject()
    private val weatherApi: WeatherApi by inject()
    private val weatherRepo: WeatherRepository by inject()
    private val timeRepo: TimeRepository by inject()

    override suspend fun doWork(): Result {
        val location: Location = locationHelper.getLocation() ?: return Result.success()

        val weatherDto: WeatherDto = weatherApi.getWeather(
            latitude = location.latitude,
            longitude = location.longitude
        ).getOrNull() ?: return Result.success()

        val timestamp = getTimestampSecondsSinceEpoch()

        weatherRepo.insertWeatherRecord(
            record = WeatherRecord(
                id = 0L,
                timestampSecondsSinceEpoch = timestamp,
                cloudPercentage = weatherDto.cloudPercentage,
                humidity = weatherDto.humidity,
                temperature = weatherDto.temperature,
                windSpeed = weatherDto.windSpeed,
                info = null
            )
        )

        timeRepo.saveLastTimestampOfWeatherRequest(timestamp)

        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "bav.petus.weather_update"
        const val PERIOD_IN_HOURS = 3L
    }
}