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
        StringId.BeingBetter -> R.string.BeenBetter
        StringId.IShouldGo -> R.string.IShouldGo
        StringId.SeeYa -> R.string.SeeYa
        StringId.ByeBye -> R.string.ByeBye
        StringId.Sure -> R.string.Sure
        StringId.Ok -> R.string.Ok
        StringId.Thanks -> R.string.Thanks
        StringId.Use -> R.string.Use
        StringId.Destroy -> R.string.Destroy
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
        StringId.NecronomiconStage8AnswerOption0 -> R.string.NecronomiconStage8AnswerOption0
        StringId.NecronomiconStage8Dialog0 -> R.string.NecronomiconStage8Dialog0
        StringId.NecronomiconStage8Dialog1 -> R.string.NecronomiconStage8Dialog1
    }
}