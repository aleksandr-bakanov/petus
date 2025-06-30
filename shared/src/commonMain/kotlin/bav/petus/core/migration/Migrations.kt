package bav.petus.core.migration

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import bav.petus.core.engine.UserStats
import bav.petus.core.inventory.InventoryItem
import bav.petus.core.inventory.InventoryItemId
import kotlinx.coroutines.flow.first

class Migrations(
    private val dataStore: DataStore<Preferences>,
    private val userStats: UserStats,
) {
    suspend fun doMigrationsIfRequired() {
        val currentAppMigrationVersion = dataStore.data.first()[MIGRATION_VERSION_KEY] ?: 0
        if (currentAppMigrationVersion < migrations.size) {
            for (i in currentAppMigrationVersion until migrations.size) {
                val lambda = migrations[i]
                lambda.invoke()
            }
            dataStore.edit { store -> store[MIGRATION_VERSION_KEY] = migrations.size }
        }
    }

    private suspend fun migration0to1() {
        userStats.addInventoryItem(InventoryItem(InventoryItemId.DogusEgg, 1))
        userStats.addInventoryItem(InventoryItem(InventoryItemId.CatusEgg, 1))
    }

    private val migrations = listOf(
        ::migration0to1,
    )

    companion object {
        private val MIGRATION_VERSION_KEY = intPreferencesKey("migration_version_key")
    }
}