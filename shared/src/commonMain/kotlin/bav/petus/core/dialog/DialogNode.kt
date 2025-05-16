package bav.petus.core.dialog

import bav.petus.core.resources.StringId

data class DialogNode(
    val text: StringId,
    val answers: List<Answer>,
)
