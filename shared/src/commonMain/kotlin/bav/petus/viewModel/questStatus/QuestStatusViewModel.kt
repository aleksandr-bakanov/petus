package bav.petus.viewModel.questStatus

import bav.petus.base.ViewModelWithNavigation
import bav.petus.core.engine.OBTAIN_DRAGON_HAS_NECRONOMICON_KEY
import bav.petus.core.engine.QuestSystem
import bav.petus.core.engine.UserStats
import bav.petus.core.inventory.InventoryItemId
import bav.petus.core.resources.StringId
import com.rickclephas.kmp.observableviewmodel.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

    val uiState: StateFlow<QuestStatusUiState> = questSystem.dataStore.data.map { preferences ->
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
                    .joinToString(separator = "\n- ", transform = convertStringIdToString)
                append(completedStages)
                if (necroStage == necroStageAmount) {
                    append("\n- ")
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
                    .joinToString(separator = "\n- ", transform = convertStringIdToString)
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
                    .joinToString(separator = "\n- ", transform = convertStringIdToString)
                append(completedStages)
            }
        )

        val fractalStage = questSystem.getQuestStage(QuestSystem.QUEST_TO_OBTAIN_FRACTAL)
        val fractalStageAmount =
            questSystem.getQuestStagesAmount(QuestSystem.QUEST_TO_OBTAIN_FRACTAL)
        val fractalQuest = QuestDescription(
            questName = convertStringIdToString(StringId.QuestNameObtainFractal),
            stagesDescription = getStagesCompletedString(
                convertStringIdToString = convertStringIdToString,
                currentStage = fractalStage,
                total = fractalStageAmount,
            ),
            questDescription = buildString {
                val completedStages = OBTAIN_FRACTAL_STAGES_DESCRIPTIONS
                    .take(fractalStage + 1)
                    .joinToString(separator = "\n- ", transform = convertStringIdToString)
                append(completedStages)
            }
        )

        val meditationStage = questSystem.getQuestStage(QuestSystem.QUEST_MEDITATION)
        val meditationStageAmount =
            questSystem.getQuestStagesAmount(QuestSystem.QUEST_MEDITATION)
        val meditationQuest = QuestDescription(
            questName = convertStringIdToString(StringId.QuestNameMeditation),
            stagesDescription = getStagesCompletedString(
                convertStringIdToString = convertStringIdToString,
                currentStage = meditationStage,
                total = meditationStageAmount,
            ),
            questDescription = buildString {
                val completedStages = MEDITATION_STAGES_DESCRIPTIONS
                    .take(meditationStage + 1)
                    .joinToString(separator = "\n- ", transform = convertStringIdToString)
                append(completedStages)
            }
        )

        val dragonStage = questSystem.getQuestStage(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
        val dragonStageAmount =
            questSystem.getQuestStagesAmount(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
        val dragonQuest = QuestDescription(
            questName = convertStringIdToString(StringId.QuestNameObtainDragon),
            stagesDescription = getStagesCompletedString(
                convertStringIdToString = convertStringIdToString,
                currentStage = dragonStage,
                total = dragonStageAmount,
            ),
            questDescription = buildString {
                // Starting from 25 stage there is condition on having Necronomicon
                if (dragonStage <= 24) {
                    val completedStages = OBTAIN_DRAGON_STAGES_DESCRIPTIONS
                        .take(dragonStage + 1)
                        .joinToString(separator = "\n- ", transform = convertStringIdToString)
                    append(completedStages)
                } else {
                    val completedStages = OBTAIN_DRAGON_STAGES_DESCRIPTIONS
                        .take(25)
                        .joinToString(separator = "\n- ", transform = convertStringIdToString)
                    append(completedStages)

                    val hasNecronomicon = preferences[OBTAIN_DRAGON_HAS_NECRONOMICON_KEY] ?: false
                    append("\n- ")
                    if (hasNecronomicon) {
                        append(convertStringIdToString(StringId.QuestDescObtainDragonStage25YesNecro))
                    } else {
                        append(convertStringIdToString(StringId.QuestDescObtainDragonStage25NoNecro))
                    }

                    if (dragonStage >= 26) {
                        append("\n- ")
                        append(convertStringIdToString(StringId.QuestDescObtainDragonStage26))
                    }
                    if (dragonStage >= 27) {
                        append("\n- ")
                        append(convertStringIdToString(StringId.QuestDescObtainDragonStage27))
                    }
                }
            }
        )

        QuestStatusUiState(
            quests = listOf(
                necroQuest,
                frogusQuest,
                boberQuest,
                fractalQuest,
                meditationQuest,
                dragonQuest,
            )
        )
    }.stateIn(
        viewModelScope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = QuestStatusUiState(quests = emptyList())
    )

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
        private val OBTAIN_FRACTAL_STAGES_DESCRIPTIONS = listOf(
            StringId.QuestDescObtainFractalStage0,
            StringId.QuestDescObtainFractalStage1,
            StringId.QuestDescObtainFractalStage2,
            StringId.QuestDescObtainFractalStage3,
            StringId.QuestDescObtainFractalStage4,
            StringId.QuestDescObtainFractalStage5,
            StringId.QuestDescObtainFractalStage6,
            StringId.QuestDescObtainFractalStage7,
            StringId.QuestDescObtainFractalStage8,
        )
        private val MEDITATION_STAGES_DESCRIPTIONS = listOf(
            StringId.QuestDescMeditationStage0,
            StringId.QuestDescMeditationStage1,
            StringId.QuestDescMeditationStage2,
            StringId.QuestDescMeditationStage3,
            StringId.QuestDescMeditationStage4,
        )
        private val OBTAIN_DRAGON_STAGES_DESCRIPTIONS = listOf(
            StringId.QuestDescObtainDragonStage0,
            StringId.QuestDescObtainDragonStage1,
            StringId.QuestDescObtainDragonStage2,
            StringId.QuestDescObtainDragonStage3,
            StringId.QuestDescObtainDragonStage4,
            StringId.QuestDescObtainDragonStage5,
            StringId.QuestDescObtainDragonStage6,
            StringId.QuestDescObtainDragonStage7,
            StringId.QuestDescObtainDragonStage8,
            StringId.QuestDescObtainDragonStage9,
            StringId.QuestDescObtainDragonStage10,
            StringId.QuestDescObtainDragonStage11,
            StringId.QuestDescObtainDragonStage12,
            StringId.QuestDescObtainDragonStage13,
            StringId.QuestDescObtainDragonStage14,
            StringId.QuestDescObtainDragonStage15,
            StringId.QuestDescObtainDragonStage16,
            StringId.QuestDescObtainDragonStage17,
            StringId.QuestDescObtainDragonStage18,
            StringId.QuestDescObtainDragonStage19,
            StringId.QuestDescObtainDragonStage20,
            StringId.QuestDescObtainDragonStage21,
            StringId.QuestDescObtainDragonStage22,
            StringId.QuestDescObtainDragonStage23,
            StringId.QuestDescObtainDragonStage24,
        )
    }

    sealed interface Navigation {
        data object CloseScreen : Navigation
    }
}