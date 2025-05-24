package bav.petus.core.notification

import bav.petus.core.inventory.InventoryItem
import kotlinx.serialization.Serializable

@Serializable
sealed class UserNotification(val id: String = getUUID()) {
    @Serializable
    data class InventoryItemAdded(val item: InventoryItem) : UserNotification()
    @Serializable
    data class InventoryItemRemoved(val item: InventoryItem) : UserNotification()
}