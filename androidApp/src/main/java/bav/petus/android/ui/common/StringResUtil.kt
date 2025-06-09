package bav.petus.android.ui.common

import bav.petus.android.R
import bav.petus.core.resources.StringId

fun StringId.toResId(): Int {
    return when (this) {
        StringId.ZooScreenTitle -> R.string.ZooScreenTitle
        StringId.CemeteryScreenTitle -> R.string.CemeteryScreenTitle
        StringId.ProfileScreenTitle -> R.string.ProfileScreenTitle
        StringId.WeatherScreenTitle -> R.string.WeatherScreenTitle
        StringId.PetTypeDescriptionCatus -> R.string.PetTypeDescriptionCatus
        StringId.PetTypeDescriptionDogus -> R.string.PetTypeDescriptionDogus
        StringId.PetTypeDescriptionFrogus -> R.string.PetTypeDescriptionFrogus
        StringId.WhatIsGoingOnWithYou -> R.string.WhatIsGoingOnWithYou
        StringId.WhatIsGoingOnWithYouLatin -> R.string.WhatIsGoingOnWithYouLatin
        StringId.BeingBetter -> R.string.BeenBetter
        StringId.IShouldGo -> R.string.IShouldGo
        StringId.SeeYa -> R.string.SeeYa
        StringId.ByeBye -> R.string.ByeBye
        StringId.Sure -> R.string.Sure
        StringId.Ok -> R.string.Ok
        StringId.Thanks -> R.string.Thanks
        StringId.Use -> R.string.Use
        StringId.Destroy -> R.string.Destroy
        StringId.IAmOkayHowAreYou -> R.string.IAmOkayHowAreYou
        StringId.IAmSick -> R.string.IAmSick
        StringId.IAmSickLatin -> R.string.IAmSickLatin
        StringId.IAmHungry -> R.string.IAmHungry
        StringId.IAmHungryLatin -> R.string.IAmHungryLatin
        StringId.IPooped -> R.string.IPooped
        StringId.IPoopedLatin -> R.string.IPoopedLatin
        StringId.IAmBored -> R.string.IAmBored
        StringId.IAmBoredLatin -> R.string.IAmBoredLatin
        StringId.IAmStillAngryAfterForceWakeUp -> R.string.IAmStillAngryAfterForceWakeUp
        StringId.IAmStillAngryAfterForceWakeUpLatin -> R.string.IAmStillAngryAfterForceWakeUpLatin
        StringId.IAmHalfHp -> R.string.IAmHalfHp
        StringId.IAmHalfHpLatin -> R.string.IAmHalfHpLatin
        StringId.IAmGood -> R.string.IAmGood
        StringId.IAmGoodLatin -> R.string.IAmGoodLatin
        StringId.NecronomiconStage3AnswerOption0 -> R.string.NecronomiconStage3AnswerOption0
        StringId.NecronomiconStage3CommonDialog -> R.string.NecronomiconStage3CommonDialog
        StringId.NecronomiconStage3DogDialog -> R.string.NecronomiconStage3DogDialog
        StringId.NecronomiconStage3DogDialogAnswerOption0 -> R.string.NecronomiconStage3DogDialogAnswerOption0
        StringId.NecronomiconStage5AnswerOption0 -> R.string.NecronomiconStage5AnswerOption0
        StringId.NecronomiconStage5DogDialog -> R.string.NecronomiconStage5DogDialog
        StringId.NecronomiconStage6AnswerOption0 -> R.string.NecronomiconStage6AnswerOption0
        StringId.NecronomiconStage6DogDialog -> R.string.NecronomiconStage6DogDialog
        StringId.NecronomiconStage7DogDialog0 -> R.string.NecronomiconStage7DogDialog0
        StringId.NecronomiconStage7AnswerOption0 -> R.string.NecronomiconStage7AnswerOption0
        StringId.NecronomiconStage7DogDialog1 -> R.string.NecronomiconStage7DogDialog1
        StringId.NecronomiconStage7DogDialog2 -> R.string.NecronomiconStage7DogDialog2
        StringId.NecronomiconStage8AnswerOption0 -> R.string.NecronomiconStage8AnswerOption0
        StringId.NecronomiconStage8Dialog0 -> R.string.NecronomiconStage8Dialog0
        StringId.NecronomiconStage8Dialog1 -> R.string.NecronomiconStage8Dialog1
        StringId.MysteriousBook -> R.string.MysteriousBook
        StringId.Necronomicon -> R.string.Necronomicon
        StringId.PieceOfCloth -> R.string.PieceOfCloth
        StringId.Fish -> R.string.Fish
        StringId.FrogusEgg -> R.string.FrogusEgg
        StringId.HistoryEventPetBecomeAdult -> R.string.HistoryEventPetBecomeAdult
        StringId.HistoryEventPetBecomeNewborn -> R.string.HistoryEventPetBecomeNewborn
        StringId.HistoryEventPetBecomeOld -> R.string.HistoryEventPetBecomeOld
        StringId.HistoryEventPetBecomeTeen -> R.string.HistoryEventPetBecomeTeen
        StringId.HistoryEventPetBuried -> R.string.HistoryEventPetBuried
        StringId.HistoryEventPetCleanUp -> R.string.HistoryEventPetCleanUp
        StringId.HistoryEventPetCreated -> R.string.HistoryEventPetCreated
        StringId.HistoryEventPetDied -> R.string.HistoryEventPetDied
        StringId.HistoryEventPetFeed -> R.string.HistoryEventPetFeed
        StringId.HistoryEventPetForciblyWakeUp -> R.string.HistoryEventPetForciblyWakeUp
        StringId.HistoryEventPetGetHealed -> R.string.HistoryEventPetGetHealed
        StringId.HistoryEventPetGetIll -> R.string.HistoryEventPetGetIll
        StringId.HistoryEventPetPlay -> R.string.HistoryEventPetPlay
        StringId.HistoryEventPetPoop -> R.string.HistoryEventPetPoop
        StringId.HistoryEventPetResurrected -> R.string.HistoryEventPetResurrected
        StringId.HistoryEventPetSleep -> R.string.HistoryEventPetSleep
        StringId.HistoryEventPetWakeUp -> R.string.HistoryEventPetWakeUp
        StringId.Necromancy -> R.string.Necromancy
        StringId.ObtainFrogusStage1Answer0 -> R.string.ObtainFrogusStage1Answer0
        StringId.ObtainFrogusStage1Dialog0 -> R.string.ObtainFrogusStage1Dialog0
        StringId.ObtainFrogusStage1Answer1 -> R.string.ObtainFrogusStage1Answer1
        StringId.ObtainFrogusStage3Answer0 -> R.string.ObtainFrogusStage3Answer0
        StringId.ObtainFrogusStage3Dialog0 -> R.string.ObtainFrogusStage3Dialog0
        StringId.ObtainFrogusStage3Answer1 -> R.string.ObtainFrogusStage3Answer1
        StringId.ObtainFrogusStage5Answer0 -> R.string.ObtainFrogusStage5Answer0
        StringId.ObtainFrogusStage5Dialog0 -> R.string.ObtainFrogusStage5Dialog0
        StringId.ObtainFrogusStage5Answer1 -> R.string.ObtainFrogusStage5Answer1
    }
}