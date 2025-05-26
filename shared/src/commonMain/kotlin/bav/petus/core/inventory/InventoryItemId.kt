package bav.petus.core.inventory

import bav.petus.core.resources.StringId

enum class InventoryItemId {
    Necronomicon,
    PieceOfCloth,
    MysteriousBook,
    Fish,
    FrogusEgg,
    ;
}

fun InventoryItemId.toStringId(): StringId {
    return when(this) {
        InventoryItemId.Necronomicon -> StringId.Necronomicon
        InventoryItemId.PieceOfCloth -> StringId.PieceOfCloth
        InventoryItemId.MysteriousBook -> StringId.MysteriousBook
        InventoryItemId.Fish -> StringId.Fish
        InventoryItemId.FrogusEgg -> StringId.FrogusEgg
    }
}
