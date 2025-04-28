package bav.petus.repo

import bav.petus.cache.PetsDatabase
import bav.petus.cache.WeatherRecord
import bav.petus.extension.epochTimeToString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Responsibilities:
 *   - Provides weather record closest to requested time
 */
class WeatherRepository(
    private val database: PetsDatabase,
) {

    /**
     * Provides approximated weather record closest to requested time
     */
    suspend fun getClosestWeather(timestamp: Long): WeatherRecord? {
        val allRecords = database.getDao().selectAllWeatherRecords().sortedBy { it.timestampSecondsSinceEpoch }
        return when {
            allRecords.isEmpty() -> null
            allRecords.size == 1 -> allRecords[0]
            else -> {
                val left = allRecords.lastOrNull { it.timestampSecondsSinceEpoch <= timestamp }
                val right = allRecords.firstOrNull { it.timestampSecondsSinceEpoch >= timestamp }

                when {
                    // All records timestamps > timestamp
                    left == null -> right
                    // All records timestamps < timestamp
                    right == null -> left
                    // Exactly left
                    left.timestampSecondsSinceEpoch == timestamp -> left
                    // Exactly right
                    right.timestampSecondsSinceEpoch == timestamp -> right
                    // Between left and right
                    else -> {
                        val timeLeft = left.timestampSecondsSinceEpoch
                        val timeRight = right.timestampSecondsSinceEpoch
                        val weight = (timestamp - timeLeft).toDouble() / (timeRight - timeLeft).toDouble()

                        val cloud = weightedMean(left.cloudPercentage, right.cloudPercentage, weight)
                        val humidify = weightedMean(left.humidity, right.humidity, weight)
                        val temperature = weightedMean(left.temperature, right.temperature, weight)
                        val windSpeed = weightedMean(left.windSpeed, right.windSpeed, weight)

                        WeatherRecord(
                            timestampSecondsSinceEpoch = timestamp,
                            cloudPercentage = cloud,
                            humidity = humidify,
                            temperature = temperature,
                            windSpeed = windSpeed,
                            info = null,
                        )
                    }
                }
            }
        }
    }

    /**
     * 0 < W < 1
     */
    private fun weightedMean(a: Double?, b: Double?, w: Double): Double? {
        return if (a == null || b == null) null
        else {
            val diff = abs(a - b)
            when {
                a < b -> a + w * diff
                a > b -> b + w * diff
                else -> a
            }
        }
    }

    private fun weightedMean(a: Int?, b: Int?, w: Double): Int? {
        return weightedMean(
            a = a?.toDouble(),
            b = b?.toDouble(),
            w = w,
        )?.roundToInt()
    }

    suspend fun insertWeatherRecord(record: WeatherRecord) {
        if (database.getDao().selectWeatherRecordByTimestamp(record.timestampSecondsSinceEpoch) == null) {
            database.getDao().insertWeatherRecord(record)
        }
    }

    fun getAllWeatherRecordsFlow(): Flow<List<String>> {
        return database.getDao().selectAllWeatherRecordsFlow()
            .map { list ->
                list.map { entity ->
                    entity.str()
                }.distinct()
            }
    }

    fun getLatestWeatherRecordFlow(): Flow<WeatherRecord?> {
        return database.getDao().selectAllWeatherRecordsFlow()
            .map { list ->
                list.maxByOrNull { it.timestampSecondsSinceEpoch }
            }
    }

    private fun WeatherRecord.str(): String {
        return buildString {
            append(id)
            append(" | ")
            append(timestampSecondsSinceEpoch.epochTimeToString())
            append(" | c:")
            append(cloudPercentage)
            append(" | h:")
            append(humidity)
            append(" | t:")
            append(temperature)
            append(" | w:")
            append(windSpeed?.toInt())
        }
    }
}
