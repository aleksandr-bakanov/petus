package bav.petus.core.dialog

import bav.petus.core.engine.QuestSystem
import bav.petus.core.engine.UserStats
import bav.petus.core.resources.StringId
import bav.petus.model.Pet

data class Answer(
    val text: StringId,
    val nextNode: String?, // null - means this answer ends dialog
    val action: (suspend (QuestSystem, UserStats, Pet?) -> Unit)? = null,
)
