package bav.petus.android.ui.common

import android.content.Context
import bav.petus.android.R
import bav.petus.core.resources.StringId

class StringResourcesUseCase(
    private val context: Context,
) {

    fun getString(id: StringId): String = when (id) {
        is StringId.IWillDie -> context.getString(R.string.IWillDie, id.chance)
        is StringId.IWillDieLatin -> context.getString(R.string.IWillDieLatin, id.chance)
        is StringId.IWillDiePolish -> context.getString(R.string.IWillDiePolish, id.chance)
        else -> context.getString(id.toResId())
    }
}