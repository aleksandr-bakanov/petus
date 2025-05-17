package bav.petus.core.time

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first

/**
 * Responsibilities:
 *   - Stores time of last tick
 */
class TimeRepository(
    private val dataStore: DataStore<Preferences>,
) {

    suspend fun getLastTimestamp(): Long {
        return dataStore.data.first()[LAST_TICK_KEY] ?: 0L
    }

    suspend fun saveLastTimestamp(value: Long) {
        dataStore.edit { store ->
            store[LAST_TICK_KEY] = value
        }
    }

    suspend fun getLastTimestampOfWeatherRequest(): Long {
        return dataStore.data.first()[LAST_TIMESTAMP_OF_WEATHER_REQUEST_KEY] ?: 0L
    }

    suspend fun saveLastTimestampOfWeatherRequest(value: Long) {
        dataStore.edit { store ->
            store[LAST_TIMESTAMP_OF_WEATHER_REQUEST_KEY] = value
        }
    }

    suspend fun isTimeToFetchWeather(): Boolean {
        val now = getTimestampSecondsSinceEpoch()
        val lastTime = getLastTimestampOfWeatherRequest()
        return (now - lastTime) > WEATHER_REQUEST_PERIOD_SEC
    }

    companion object {
        private val LAST_TICK_KEY = longPreferencesKey("last_tick_key")
        private val LAST_TIMESTAMP_OF_WEATHER_REQUEST_KEY = longPreferencesKey("last_timestamp_of_weather_request_key")
        const val CYCLE_PERIOD_IN_SECONDS = 5L * 60L
        const val WEATHER_REQUEST_PERIOD_SEC = 3L * 60L * 60L
    }
}