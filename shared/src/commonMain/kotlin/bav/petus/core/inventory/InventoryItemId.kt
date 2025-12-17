package bav.petus.core.inventory

import bav.petus.core.resources.ImageId
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

fun InventoryItemId.toItemNameStringId(): StringId {
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

fun InventoryItemId.toItemDescriptionStringId(): StringId {
    return when(this) {
        InventoryItemId.Necronomicon -> StringId.ItemDescNecronomicon
        InventoryItemId.PieceOfCloth -> StringId.ItemDescPieceOfCloth
        InventoryItemId.MysteriousBook -> StringId.ItemDescMysteriousBook
        InventoryItemId.Fish -> StringId.ItemDescFish
        InventoryItemId.FrogusEgg -> StringId.ItemDescFrogusEgg
        InventoryItemId.DogusEgg -> StringId.ItemDescDogusEgg
        InventoryItemId.CatusEgg -> StringId.ItemDescCatusEgg
        InventoryItemId.BoberEgg -> StringId.ItemDescBoberEgg
        InventoryItemId.DragonEgg -> StringId.ItemDescDragonEgg
        InventoryItemId.Basket -> StringId.ItemDescBasket
        InventoryItemId.TwoMeterRuler -> StringId.ItemDescTwoMeterRuler
        InventoryItemId.TenCentimeterRuler -> StringId.ItemDescTenCentimeterRuler
        InventoryItemId.MathBook -> StringId.ItemDescMathBook
        InventoryItemId.MemoryOfMage -> StringId.ItemDescMemoryOfMage
        InventoryItemId.MemoryOfWarrior -> StringId.ItemDescMemoryOfWarrior
        InventoryItemId.MemoryOfBard -> StringId.ItemDescMemoryOfBard
        InventoryItemId.MemoryOfSmith -> StringId.ItemDescMemoryOfSmith
        InventoryItemId.CurseOfMage -> StringId.ItemDescCurseOfMage
        InventoryItemId.CurseOfWarrior -> StringId.ItemDescCurseOfWarrior
        InventoryItemId.CurseOfBard -> StringId.ItemDescCurseOfBard
        InventoryItemId.CurseOfSmith -> StringId.ItemDescCurseOfSmith
    }
}

fun InventoryItemId.toImageId(): ImageId {
    return when(this) {
        InventoryItemId.Necronomicon -> ImageId.Necronomicon
        InventoryItemId.PieceOfCloth -> ImageId.PieceOfCloth
        InventoryItemId.MysteriousBook -> ImageId.MysteriousBook
        InventoryItemId.Fish -> ImageId.Fish
        InventoryItemId.FrogusEgg -> ImageId.FrogusEgg
        InventoryItemId.DogusEgg -> ImageId.DogusEgg
        InventoryItemId.CatusEgg -> ImageId.CatusEgg
        InventoryItemId.BoberEgg -> ImageId.BoberEgg
        InventoryItemId.DragonEgg -> ImageId.DragonEgg
        InventoryItemId.Basket -> ImageId.Basket
        InventoryItemId.TwoMeterRuler -> ImageId.TwoMeterRuler
        InventoryItemId.TenCentimeterRuler -> ImageId.TenCentimeterRuler
        InventoryItemId.MathBook -> ImageId.MathBook
        InventoryItemId.MemoryOfMage -> ImageId.MemoryOfMage
        InventoryItemId.MemoryOfWarrior -> ImageId.MemoryOfWarrior
        InventoryItemId.MemoryOfBard -> ImageId.MemoryOfBard
        InventoryItemId.MemoryOfSmith -> ImageId.MemoryOfSmith
        InventoryItemId.CurseOfMage -> ImageId.CurseOfMage
        InventoryItemId.CurseOfWarrior -> ImageId.CurseOfWarrior
        InventoryItemId.CurseOfBard -> ImageId.CurseOfBard
        InventoryItemId.CurseOfSmith -> ImageId.CurseOfSmith
    }
}
