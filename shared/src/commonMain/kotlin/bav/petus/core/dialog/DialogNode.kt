package bav.petus.core.dialog

import bav.petus.core.resources.StringId

data class DialogNode(
    val text: List<StringId>,
    val answers: List<Answer>,
)
