package bav.petus.core.resources

enum class StringId {
    //region Pet type description
    PetTypeDescriptionCatus,
    PetTypeDescriptionDogus,
    PetTypeDescriptionFrogus,
    //endregion

    //region Dialogs
    WhatIsGoingOnWithYou,
    BeingBetter,
    IShouldGo,
    SeeYa,
    ByeBye,
    Sure,
    Ok,
    Thanks,
    Use,
    Destroy,

    NecronomiconStage3AnswerOption0, // Do you know anything about exhumated grave?
    NecronomiconStage3CommonDialog, // Nope, don't know anything
    NecronomiconStage3DogDialog, // I don't know anything but we could investigate, bring me something from there.
    NecronomiconStage3DogDialogAnswerOption0, // Ok, I'll see what I can find
    NecronomiconStage5AnswerOption0, // Look what I've found near the grave
    NecronomiconStage5DogDialog, // Hmm, interesting, let me see where it'll lead me...
    NecronomiconStage6AnswerOption0, // Have you found anything?
    NecronomiconStage6DogDialog, // No, not yet, but I'm searching.
    NecronomiconStage7DogDialog0, // Look, I've found this strange book on ritual site. Take it.
    NecronomiconStage7AnswerOption0, // Thanks, do you know who can help me with it?
    NecronomiconStage7DogDialog1, // Try to ask catus they usually more experienced in wisdom stuff, the older the better.
    NecronomiconStage8AnswerOption0, // [Show the book] Do you know anything about this book?
    NecronomiconStage8Dialog0, // What's this? I can sense some magic but I'm not sure...
    NecronomiconStage8Dialog1, // Ahh, yes, I know what it is. This is Necronomicon. Do you with to use it or destroy it?
    //endregion
    ;
}