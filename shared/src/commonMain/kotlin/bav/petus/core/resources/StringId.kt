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
    data object Fish : StringId()
    data object FrogusEgg : StringId()
    //endregion

    //region Abilities names
    data object Necromancy : StringId()
    //endregion

    //region History events
    data object HistoryEventPetCreated : StringId()
    data object HistoryEventPetWakeUp : StringId()
    data object HistoryEventPetForciblyWakeUp : StringId()
    data object HistoryEventPetSleep : StringId()
    data object HistoryEventPetGetIll : StringId()
    data object HistoryEventPetGetHealed : StringId()
    data object HistoryEventPetFeed : StringId()
    data object HistoryEventPetPlay : StringId()
    data object HistoryEventPetDied : StringId()
    data object HistoryEventPetCleanUp : StringId()
    data object HistoryEventPetBecomeNewborn : StringId()
    data object HistoryEventPetBecomeTeen : StringId()
    data object HistoryEventPetBecomeAdult : StringId()
    data object HistoryEventPetBecomeOld : StringId()
    data object HistoryEventPetBuried : StringId()
    data object HistoryEventPetResurrected : StringId()
    data object HistoryEventPetPoop : StringId()
    //endregion

    //region Dialogs
    data object WhatIsGoingOnWithYou : StringId()
    data object WhatIsGoingOnWithYouLatin : StringId()
    data object BeingBetter : StringId()
    data object IShouldGo : StringId()
    data object SeeYa : StringId()
    data object ByeBye : StringId()
    data object Sure : StringId()
    data object Ok : StringId()
    data object Thanks : StringId()
    data object Use : StringId()
    data object Destroy : StringId()
    data object IAmOkayHowAreYou : StringId()

    data object IAmSick : StringId()
    data object IAmSickLatin : StringId()
    data object IAmHungry : StringId()
    data object IAmHungryLatin : StringId()
    data object IPooped : StringId()
    data object IPoopedLatin : StringId()
    data object IAmBored : StringId()
    data object IAmBoredLatin : StringId()
    data object IAmStillAngryAfterForceWakeUp : StringId()
    data object IAmStillAngryAfterForceWakeUpLatin : StringId()
    data object IAmHalfHp : StringId()
    data object IAmHalfHpLatin : StringId()
    data object IAmGood : StringId()
    data object IAmGoodLatin : StringId()

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
    data object NecronomiconStage7DogDialog2 : StringId()
    data object NecronomiconStage8AnswerOption0 : StringId()
    data object NecronomiconStage8Dialog0 : StringId()
    data object NecronomiconStage8Dialog1 : StringId()

    data object ObtainFrogusStage1Answer0 : StringId()
    data object ObtainFrogusStage1Dialog0 : StringId()
    data object ObtainFrogusStage1Answer1 : StringId()
    data object ObtainFrogusStage3Answer0 : StringId()
    data object ObtainFrogusStage3Dialog0 : StringId()
    data object ObtainFrogusStage3Answer1 : StringId()
    data object ObtainFrogusStage5Answer0 : StringId()
    data object ObtainFrogusStage5Dialog0 : StringId()
    data object ObtainFrogusStage5Answer1 : StringId()
    //endregion
}