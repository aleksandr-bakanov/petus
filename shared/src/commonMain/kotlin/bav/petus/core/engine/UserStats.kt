package bav.petus.core.engine

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import bav.petus.core.inventory.InventoryItem
import bav.petus.core.notification.UserNotification
import bav.petus.core.resources.StringId
import bav.petus.model.PetType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class LanguageKnowledge(
    val catus: Int,
    val dogus: Int,
    val frogus: Int,
    val bober: Int,
    val fractal: Int,
)

enum class Ability {
    Necromancy,
    Meditation,
    ;
}

fun Ability.toStringId(): StringId {
    return when (this) {
        Ability.Necromancy -> StringId.Necromancy
        Ability.Meditation -> StringId.Meditation
    }
}

data class UserProfileData(
    val languageKnowledge: LanguageKnowledge,
    val inventory: List<InventoryItem>,
    val abilities: Set<Ability>,
    val zooSize: Int,
)

class UserStats(
    private val dataStore: DataStore<Preferences>,
) {
    /**
     * Returns language level for provided pet type in range [0, [MAXIMUM_LANGUAGE_KNOWLEDGE]]
     */
    suspend fun getLanguageKnowledge(type: PetType): Int {
        return dataStore.data.first()[type.languageKnowledgeKey()] ?: 0
    }

    fun getUserProfileFlow(): Flow<UserProfileData> {
        return dataStore.data.map {
            UserProfileData(
                languageKnowledge = LanguageKnowledge(
                    catus = it.coercedKnowledge(LANGUAGE_KNOWLEDGE_CATUS_KEY),
                    dogus = it.coercedKnowledge(LANGUAGE_KNOWLEDGE_DOGUS_KEY),
                    frogus = it.coercedKnowledge(LANGUAGE_KNOWLEDGE_FROGUS_KEY),
                    bober = it.coercedKnowledge(LANGUAGE_KNOWLEDGE_BOBER_KEY),
                    fractal = it.coercedKnowledge(LANGUAGE_KNOWLEDGE_FRACTAL_KEY),
                ),
                inventory = it.getInventory(),
                abilities = it.getAbilities(),
                zooSize = it[ZOO_SIZE_KEY] ?: DEFAULT_ZOO_SIZE
            )
        }
    }

    private fun Preferences.getAbilities(): Set<Ability> {
        return this[AVAILABLE_ABILITIES_KEY]?.map { value -> Ability.valueOf(value) }?.toSet() ?: emptySet()
    }

    private fun Preferences.getInventory(): List<InventoryItem> {
        return this[USER_INVENTORY_KEY]?.let {
            Json.decodeFromString<List<InventoryItem>>(it)
        } ?: emptyList()
    }

    private fun Preferences.getUserNotifications(): List<UserNotification> {
        return this[USER_NOTIFICATIONS_KEY]?.let {
            Json.decodeFromString<List<UserNotification>>(it)
        } ?: emptyList()
    }

    fun getUserNotificationsFlow(): Flow<List<UserNotification>> {
        return dataStore.data.map { store ->
            store[USER_NOTIFICATIONS_KEY]?.let {
                Json.decodeFromString<List<UserNotification>>(it)
            } ?: emptyList()
        }
    }

    suspend fun addNotification(notification: UserNotification) {
        val notifications = dataStore.data.first().getUserNotifications()
        val updatedNotifications = listOf(notification) + notifications
        dataStore.edit { store -> store[USER_NOTIFICATIONS_KEY] = Json.encodeToString(updatedNotifications) }
    }

    suspend fun removeNotification(id: String) {
        val notifications = dataStore.data.first().getUserNotifications()
        val updatedNotifications = notifications.filterNot { it.id == id }
        if (notifications.size != updatedNotifications.size) {
            dataStore.edit { store -> store[USER_NOTIFICATIONS_KEY] = Json.encodeToString(updatedNotifications) }
        }
    }

    suspend fun addInventoryItem(item: InventoryItem) {
        val inventory = dataStore.data.first().getInventory()
        val newInventory = inventory.addItem(item)
        dataStore.edit { store -> store[USER_INVENTORY_KEY] = Json.encodeToString(newInventory) }
        addNotification(UserNotification.InventoryItemAdded(item))
    }

    suspend fun removeInventoryItem(item: InventoryItem) {
        val inventory = dataStore.data.first().getInventory()
        val newInventory = inventory.removeItem(item)
        if (newInventory != null) {
            dataStore.edit { store -> store[USER_INVENTORY_KEY] = Json.encodeToString(newInventory) }
            addNotification(UserNotification.InventoryItemRemoved(item))
        }
    }

    private fun Preferences.coercedKnowledge(key: Preferences.Key<Int>): Int {
        return (this[key] ?: 0).coerceIn(0, MAXIMUM_LANGUAGE_UI_KNOWLEDGE)
    }

    suspend fun saveLanguageKnowledge(type: PetType, value: Int) {
        val newValue = value.coerceIn(0, MAXIMUM_LANGUAGE_KNOWLEDGE)
        dataStore.edit { store ->
            store[type.languageKnowledgeKey()] = newValue
        }
    }

    private fun PetType.languageKnowledgeKey(): Preferences.Key<Int> {
        return when (this) {
            PetType.Catus -> LANGUAGE_KNOWLEDGE_CATUS_KEY
            PetType.Dogus -> LANGUAGE_KNOWLEDGE_DOGUS_KEY
            PetType.Frogus -> LANGUAGE_KNOWLEDGE_FROGUS_KEY
            PetType.Bober -> LANGUAGE_KNOWLEDGE_BOBER_KEY
            PetType.Fractal -> LANGUAGE_KNOWLEDGE_FRACTAL_KEY
        }
    }

    suspend fun getAvailablePetTypes(): Set<PetType> {
        val value = dataStore.data.first()[AVAILABLE_PET_TYPES_KEY] ?: setOf("Catus", "Dogus")
        return value.map { PetType.valueOf(it) }.toSet()
    }

    suspend fun addNewAvailablePetType(type: PetType) {
        val current = getAvailablePetTypes().toMutableSet()
        if (type !in current) {
            current.add(type)
            dataStore.edit { store ->
                store[AVAILABLE_PET_TYPES_KEY] = current.map { it.name }.toSet()
            }
        }
    }

    suspend fun getAvailableAbilities(): Set<Ability> {
        val value = dataStore.data.first()[AVAILABLE_ABILITIES_KEY] ?: emptySet()
        return value.map { Ability.valueOf(it) }.toSet()
    }

    suspend fun addNewAbility(value: Ability) {
        val current = getAvailableAbilities().toMutableSet()
        if (value !in current) {
            current.add(value)
            dataStore.edit { store ->
                store[AVAILABLE_ABILITIES_KEY] = current.map { it.name }.toSet()
            }
        }
    }

    companion object {
        const val MAXIMUM_LANGUAGE_KNOWLEDGE = 120
        const val MAXIMUM_LANGUAGE_UI_KNOWLEDGE = 100
        const val DEFAULT_ZOO_SIZE = 6

        private val LANGUAGE_KNOWLEDGE_CATUS_KEY = intPreferencesKey("language_knowledge_catus")
        private val LANGUAGE_KNOWLEDGE_DOGUS_KEY = intPreferencesKey("language_knowledge_dogus")
        private val LANGUAGE_KNOWLEDGE_FROGUS_KEY = intPreferencesKey("language_knowledge_frogus")
        private val LANGUAGE_KNOWLEDGE_BOBER_KEY = intPreferencesKey("language_knowledge_bober")
        private val LANGUAGE_KNOWLEDGE_FRACTAL_KEY = intPreferencesKey("language_knowledge_fractal")

        private val ZOO_SIZE_KEY = intPreferencesKey("zoo_size")

        private val AVAILABLE_PET_TYPES_KEY = stringSetPreferencesKey("available_pet_types")
        private val AVAILABLE_ABILITIES_KEY = stringSetPreferencesKey("available_abilities_types")

        private val USER_INVENTORY_KEY = stringPreferencesKey("user_inventory")
        private val USER_NOTIFICATIONS_KEY = stringPreferencesKey("user_notifications")
    }
}

fun List<InventoryItem>.addItem(item: InventoryItem): List<InventoryItem> {
    val existingItem = this.find { it.id == item.id }
    return if (existingItem != null) {
        this.map {
            if (it.id == existingItem.id) existingItem.copy(amount = existingItem.amount + 1)
            else it
        }
    }
    else {
        this + item
    }
}

fun List<InventoryItem>.removeItem(item: InventoryItem): List<InventoryItem>? {
    val existingItem = this.find { it.id == item.id }
    if (existingItem == null) {
        return null
    }
    else {
        val newInventory = if (existingItem.amount > 1) {
            this.map {
                if (it.id == existingItem.id) existingItem.copy(amount = existingItem.amount - 1)
                else it
            }
        }
        else {
            this - item
        }
        return newInventory
    }
}