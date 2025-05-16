package bav.petus.core.inventory

import kotlinx.serialization.Serializable

@Serializable
data class InventoryItem(
    val id: InventoryItemId,
    val amount: Int,
)
