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
    DragonEgg,
    Basket,
    TwoMeterRuler,
    TenCentimeterRuler,
    MathBook,
    MemoryOfMage,
    MemoryOfWarrior,
    MemoryOfBard,
    MemoryOfSmith,
    CurseOfMage,
    CurseOfWarrior,
    CurseOfBard,
    CurseOfSmith,
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
        InventoryItemId.DragonEgg -> StringId.DragonEgg
        InventoryItemId.Basket -> StringId.Basket
        InventoryItemId.TwoMeterRuler -> StringId.TwoMeterRuler
        InventoryItemId.TenCentimeterRuler -> StringId.TenCentimeterRuler
        InventoryItemId.MathBook -> StringId.MathBook
        InventoryItemId.MemoryOfMage -> StringId.MemoryOfMage
        InventoryItemId.MemoryOfWarrior -> StringId.MemoryOfWarrior
        InventoryItemId.MemoryOfBard -> StringId.MemoryOfBard
        InventoryItemId.MemoryOfSmith -> StringId.MemoryOfSmith
        InventoryItemId.CurseOfMage -> StringId.CurseOfMage
        InventoryItemId.CurseOfWarrior -> StringId.CurseOfWarrior
        InventoryItemId.CurseOfBard -> StringId.CurseOfBard
        InventoryItemId.CurseOfSmith -> StringId.CurseOfSmith
    }
}
