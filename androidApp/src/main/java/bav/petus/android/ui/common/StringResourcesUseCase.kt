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
        is StringId.QuestDescObtainBoberStage0 -> context.getString(R.string.QuestDescObtainBoberStage0, id.frogName)
        is StringId.QuestDescObtainFractalStage0 -> context.getString(R.string.QuestDescObtainFractalStage0, id.boberName, id.frogName)
        is StringId.QuestDescMeditationStage0 -> context.getString(R.string.QuestDescMeditationStage0, id.frogName)
        is StringId.QuestDescObtainDragonStage0 -> context.getString(R.string.QuestDescObtainDragonStage0, id.frogName, id.boberName)
        is StringId.QuestDescObtainAlienStage0 -> context.getString(R.string.QuestDescObtainAlienStage0, id.boberName, id.dragonName, id.fractalName)
        is StringId.QuestDescObtainAlienStage7 -> context.getString(R.string.QuestDescObtainAlienStage7, id.days)
        else -> context.getString(id.toResId())
    }
}