package bav.petus.android.ui.common

import android.content.Context
import bav.petus.core.resources.StringId

class StringResourcesUseCase(
    private val context: Context,
) {

    fun getString(id: StringId): String {
        return context.getString(id.toResId())
    }
}