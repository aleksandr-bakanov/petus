package bav.petus.core.time

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Responsibilities:
 *   - Provides current tick when asked
 *   - Knows if it's time to request weather
 */
class TimeRepository(
    private val dataStore: DataStore<Preferences>,
) {

    fun getCurrentTick(): Long {
        return runBlocking(Dispatchers.IO) {
            dataStore.data.first()[GLOBAL_TICK_KEY] ?: 0L
        }
    }

    /**
     * Worker should be responsible for incrementing ticks.
     */
    fun incrementTick() {
        runBlocking(Dispatchers.IO) {
            dataStore.edit { store ->
                val currentTick = store[GLOBAL_TICK_KEY] ?: 0L
                store[GLOBAL_TICK_KEY] = currentTick + 1L
            }
        }
    }

    fun isTimeToFetchWeather(): Boolean {
        return (getCurrentTick() % WEATHER_FETCH_FREQUENCY) == 0L
    }

    companion object {
        private val GLOBAL_TICK_KEY = longPreferencesKey("global_tick_key")

        // Each WEATHER_FETCH_FREQUENCY tick weather should be fetched from API
        private const val WEATHER_FETCH_FREQUENCY = 3L
    }
}