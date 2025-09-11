package bav.petus.core.inventory

import bav.petus.core.resources.StringId

enum class InventoryItemId {
    Necronomicon,
    PieceOfCloth,
    MysteriousBook,
    Fish,
    FrogusEgg,
    DogusEgg,
    CatusEgg,
    BoberEgg,
    Basket,
    TwoMeterRuler,
    TenCentimeterRuler,
    MathBook,
    ;
}

fun InventoryItemId.toStringId(): StringId {
    return when(this) {
        InventoryItemId.Necronomicon -> StringId.Necronomicon
        InventoryItemId.PieceOfCloth -> StringId.PieceOfCloth
        InventoryItemId.MysteriousBook -> StringId.MysteriousBook
        InventoryItemId.Fish -> StringId.Fish
        InventoryItemId.FrogusEgg -> StringId.FrogusEgg
        InventoryItemId.DogusEgg -> StringId.DogusEgg
        InventoryItemId.CatusEgg -> StringId.CatusEgg
        InventoryItemId.BoberEgg -> StringId.BoberEgg
        InventoryItemId.Basket -> StringId.Basket
        InventoryItemId.TwoMeterRuler -> StringId.TwoMeterRuler
        InventoryItemId.TenCentimeterRuler -> StringId.TenCentimeterRuler
        InventoryItemId.MathBook -> StringId.MathBook
    }
}
