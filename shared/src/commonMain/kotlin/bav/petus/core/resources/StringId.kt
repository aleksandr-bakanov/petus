package bav.petus.core.resources

sealed class StringId {
    data object ZooScreenTitle : StringId()
    data object CemeteryScreenTitle : StringId()
    data object WeatherScreenTitle : StringId()
    data object ProfileScreenTitle : StringId()

    //region Pet type description
    data object PetTypeDescriptionCatus : StringId()
    data object PetTypeDescriptionDogus : StringId()
    data object PetTypeDescriptionFrogus : StringId()
    //endregion

    //region Inventory items
    data object Necronomicon : StringId()
    data object PieceOfCloth : StringId()
    data object MysteriousBook : StringId()
    //endregion

    //region Dialogs
    data object WhatIsGoingOnWithYou : StringId()
    data object BeingBetter : StringId()
    data object IShouldGo : StringId()
    data object SeeYa : StringId()
    data object ByeBye : StringId()
    data object Sure : StringId()
    data object Ok : StringId()
    data object Thanks : StringId()
    data object Use : StringId()
    data object Destroy : StringId()

    data object NecronomiconStage3AnswerOption0 : StringId()
    data object NecronomiconStage3CommonDialog : StringId()
    data object NecronomiconStage3DogDialog : StringId()
    data object NecronomiconStage3DogDialogAnswerOption0 : StringId()
    data object NecronomiconStage5AnswerOption0 : StringId()
    data object NecronomiconStage5DogDialog : StringId()
    data object NecronomiconStage6AnswerOption0 : StringId()
    data object NecronomiconStage6DogDialog : StringId()
    data object NecronomiconStage7DogDialog0 : StringId()
    data object NecronomiconStage7AnswerOption0 : StringId()
    data object NecronomiconStage7DogDialog1 : StringId()
    data object NecronomiconStage8AnswerOption0 : StringId()
    data object NecronomiconStage8Dialog0 : StringId()
    data object NecronomiconStage8Dialog1 : StringId()
    //endregion
}