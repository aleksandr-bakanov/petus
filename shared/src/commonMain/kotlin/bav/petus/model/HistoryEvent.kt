package bav.petus.model

import bav.petus.core.resources.StringId

enum class HistoryEvent {
    PetCreated,
    PetWakeUp,
    PetForciblyWakeUp,
    PetSleep,
    PetGetIll,
    PetGetHealed,
    PetFeed,
    PetPlay,
    PetDied,
    PetCleanUp,
    PetBecomeNewborn,
    PetBecomeTeen,
    PetBecomeAdult,
    PetBecomeOld,
    PetBuried,
    PetResurrected,
    PetPoop,
    ;
}

fun HistoryEvent.toStringId(): StringId {
    return when (this) {
        HistoryEvent.PetCreated -> StringId.HistoryEventPetCreated
        HistoryEvent.PetWakeUp -> StringId.HistoryEventPetWakeUp
        HistoryEvent.PetForciblyWakeUp -> StringId.HistoryEventPetForciblyWakeUp
        HistoryEvent.PetSleep -> StringId.HistoryEventPetSleep
        HistoryEvent.PetGetIll -> StringId.HistoryEventPetGetIll
        HistoryEvent.PetGetHealed -> StringId.HistoryEventPetGetHealed
        HistoryEvent.PetFeed -> StringId.HistoryEventPetFeed
        HistoryEvent.PetPlay -> StringId.HistoryEventPetPlay
        HistoryEvent.PetDied -> StringId.HistoryEventPetDied
        HistoryEvent.PetCleanUp -> StringId.HistoryEventPetCleanUp
        HistoryEvent.PetBecomeNewborn -> StringId.HistoryEventPetBecomeNewborn
        HistoryEvent.PetBecomeTeen -> StringId.HistoryEventPetBecomeTeen
        HistoryEvent.PetBecomeAdult -> StringId.HistoryEventPetBecomeAdult
        HistoryEvent.PetBecomeOld -> StringId.HistoryEventPetBecomeOld
        HistoryEvent.PetBuried -> StringId.HistoryEventPetBuried
        HistoryEvent.PetResurrected -> StringId.HistoryEventPetResurrected
        HistoryEvent.PetPoop -> StringId.HistoryEventPetPoop
    }
}
