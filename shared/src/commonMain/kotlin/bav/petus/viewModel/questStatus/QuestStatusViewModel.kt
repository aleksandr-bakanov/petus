package bav.petus.viewModel.questStatus

import bav.petus.base.ViewModelWithNavigation
import bav.petus.core.engine.QuestSystem
import bav.petus.core.engine.UserStats
import bav.petus.core.inventory.InventoryItemId
import bav.petus.core.resources.StringId
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class QuestStatusUiState(
    val quests: List<QuestDescription>,
)

data class QuestDescription(
    val questName: String,
    val stagesDescription: String,
    val questDescription: String,
)

class QuestStatusViewModel(
    val convertStringIdToString: (StringId) -> String,
) : ViewModelWithNavigation<QuestStatusViewModel.Navigation>(), KoinComponent {

    private val questSystem: QuestSystem by inject()
    private val userStats: UserStats by inject()

    private val _uiState = MutableStateFlow(
        viewModelScope,
        QuestStatusUiState(
            emptyList()
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val necroStage = questSystem.getQuestStage(QuestSystem.QUEST_NECRONOMICON)
            val necroStageAmount = questSystem.getQuestStagesAmount(QuestSystem.QUEST_NECRONOMICON)
            val userHasNecronomicon = userStats.getUserProfileFlow().first().inventory.any { item ->
                item.id == InventoryItemId.Necronomicon
            }
            val necroQuest = QuestDescription(
                questName = convertStringIdToString(StringId.QuestNameNecronomicon),
                stagesDescription = getStagesCompletedString(
                    convertStringIdToString = convertStringIdToString,
                    currentStage = necroStage,
                    total = necroStageAmount,
                ),
                questDescription = buildString {
                    val completedStages = NECRONOMICON_STAGES_DESCRIPTIONS
                        .take(necroStage + 1)
                        .joinToString(separator = " ", transform = convertStringIdToString)
                    append(completedStages)
                    if (necroStage == necroStageAmount) {
                        append(" ")
                        if (userHasNecronomicon) {
                            append(convertStringIdToString(StringId.QuestDescNecronomiconStage9Use))
                        } else {
                            append(convertStringIdToString(StringId.QuestDescNecronomiconStage9Destroy))
                        }
                    }
                }
            )

            val frogusStage = questSystem.getQuestStage(QuestSystem.QUEST_TO_OBTAIN_FROGUS)
            val frogusStageAmount =
                questSystem.getQuestStagesAmount(QuestSystem.QUEST_TO_OBTAIN_FROGUS)
            val frogusQuest = QuestDescription(
                questName = convertStringIdToString(StringId.QuestNameObtainFrogus),
                stagesDescription = getStagesCompletedString(
                    convertStringIdToString = convertStringIdToString,
                    currentStage = frogusStage,
                    total = frogusStageAmount,
                ),
                questDescription = buildString {
                    val completedStages = OBTAIN_FROGUS_STAGES_DESCRIPTIONS
                        .take(frogusStage + 1)
                        .joinToString(separator = " ", transform = convertStringIdToString)
                    append(completedStages)
                }
            )

            val boberStage = questSystem.getQuestStage(QuestSystem.QUEST_TO_OBTAIN_BOBER)
            val boberStageAmount =
                questSystem.getQuestStagesAmount(QuestSystem.QUEST_TO_OBTAIN_BOBER)
            val boberQuest = QuestDescription(
                questName = convertStringIdToString(StringId.QuestNameObtainBober),
                stagesDescription = getStagesCompletedString(
                    convertStringIdToString = convertStringIdToString,
                    currentStage = boberStage,
                    total = boberStageAmount,
                ),
                questDescription = buildString {
                    val completedStages = OBTAIN_BOBER_STAGES_DESCRIPTIONS
                        .take(boberStage + 1)
                        .joinToString(separator = " ", transform = convertStringIdToString)
                    append(completedStages)
                }
            )

            _uiState.value = QuestStatusUiState(
                quests = listOf(
                    necroQuest,
                    frogusQuest,
                    boberQuest,
                )
            )
        }
    }

    private fun getStagesCompletedString(
        convertStringIdToString: (StringId) -> String,
        currentStage: Int,
        total: Int,
    ): String {
        return if (currentStage == total) {
            convertStringIdToString(StringId.QuestIsFinished)
        } else {
            buildString {
                append(currentStage + 1)
                append("/")
                append(total)
            }
        }
    }

    companion object {
        private val NECRONOMICON_STAGES_DESCRIPTIONS = listOf(
            StringId.QuestDescNecronomiconStage0,
            StringId.QuestDescNecronomiconStage1,
            StringId.QuestDescNecronomiconStage2,
            StringId.QuestDescNecronomiconStage3,
            StringId.QuestDescNecronomiconStage4,
            StringId.QuestDescNecronomiconStage5,
            StringId.QuestDescNecronomiconStage6,
            StringId.QuestDescNecronomiconStage7,
            StringId.QuestDescNecronomiconStage8,
        )
        private val OBTAIN_FROGUS_STAGES_DESCRIPTIONS = listOf(
            StringId.QuestDescObtainFrogusStage0,
            StringId.QuestDescObtainFrogusStage1,
            StringId.QuestDescObtainFrogusStage2,
            StringId.QuestDescObtainFrogusStage3,
            StringId.QuestDescObtainFrogusStage4,
            StringId.QuestDescObtainFrogusStage5,
            StringId.QuestDescObtainFrogusStage6,
        )
        private val OBTAIN_BOBER_STAGES_DESCRIPTIONS = listOf(
            StringId.QuestDescObtainBoberStage0,
            StringId.QuestDescObtainBoberStage1,
            StringId.QuestDescObtainBoberStage2,
            StringId.QuestDescObtainBoberStage3,
            StringId.QuestDescObtainBoberStage4,
            StringId.QuestDescObtainBoberStage5,
            StringId.QuestDescObtainBoberStage6,
            StringId.QuestDescObtainBoberStage7,
            StringId.QuestDescObtainBoberStage8,
            StringId.QuestDescObtainBoberStage9,
            StringId.QuestDescObtainBoberStage10,
            StringId.QuestDescObtainBoberStage11,
            StringId.QuestDescObtainBoberStage12,
        )
    }

    sealed interface Navigation {
        data object CloseScreen : Navigation
    }
}