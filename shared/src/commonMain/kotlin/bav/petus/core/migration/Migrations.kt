package bav.petus.core.migration

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import bav.petus.core.engine.QuestSystem
import bav.petus.core.engine.UserStats
import bav.petus.core.inventory.InventoryItem
import bav.petus.core.inventory.InventoryItemId
import bav.petus.model.PetType
import kotlinx.coroutines.flow.first

class Migrations(
    private val dataStore: DataStore<Preferences>,
    private val userStats: UserStats,
    private val questSystem: QuestSystem,
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

    private suspend fun migration1to2() {
        // Trigger fake LanguageKnowledgeChanged event to trigger possible quests stage changes
        // if they depends on language knowledge (usually very first stage of the quests)
        // value = 0 and pet type do not matter because it's ignored in quest stage lambdas -
        // instead actual value from userStats is taken and used.
        questSystem.onEvent(QuestSystem.Event.LanguageKnowledgeChanged(PetType.Catus, 0))
    }

    private val migrations = listOf(
        ::migration0to1,
        ::migration1to2,
    )

    companion object {
        private val MIGRATION_VERSION_KEY = intPreferencesKey("migration_version_key")
    }
}