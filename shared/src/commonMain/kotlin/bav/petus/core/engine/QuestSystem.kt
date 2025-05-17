package bav.petus.core.engine

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import bav.petus.core.dialog.Answer
import bav.petus.core.dialog.DialogSystem
import bav.petus.core.engine.Engine.Companion.DAY
import bav.petus.core.engine.Engine.Companion.HOUR
import bav.petus.core.engine.QuestSystem.Companion.QUEST_NECRONOMICON
import bav.petus.core.inventory.InventoryItem
import bav.petus.core.inventory.InventoryItemId
import bav.petus.core.resources.StringId
import bav.petus.core.time.getTimestampSecondsSinceEpoch
import bav.petus.model.AgeState
import bav.petus.model.BurialType
import bav.petus.model.Pet
import bav.petus.model.PetType
import bav.petus.model.Place
import bav.petus.repo.PetsRepository
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class QuestSystem(
    val dataStore: DataStore<Preferences>,
    val petsRepo: PetsRepository,
    val userStats: UserStats,
) {

    suspend fun onEvent(e: Event) {
        val preferences = dataStore.data.first()
        quests.forEach { entity ->
            val quest = entity.value
            val currentStageIndex = preferences[quest.currentStageKey] ?: 0
            if (currentStageIndex < quest.stages.size) {
                val stage = quest.stages[currentStageIndex]
                val conditions = preferences[stage.conditionsKey] ?: stage.initialConditions
                val newConditions = conditions.filterNot { key ->
                    conditionLambdas[key]?.invoke(this, preferences, e) ?: false
                }
                if (newConditions.isNotEmpty() && newConditions.size != conditions.size) {
                    dataStore.edit { store -> store[stage.conditionsKey] = newConditions.toSet() }
                }
                if (newConditions.isEmpty()) {
                    stage.onFinish(this)
                    dataStore.edit { store -> store[quest.currentStageKey] = currentStageIndex + 1 }
                }
            }
        }

        // Necronomicon stage 6 check
        if (e is Event.PetDied) {
            val quest = quests[QUEST_NECRONOMICON]!!
            // If dog went on a journey to find the book but haven't returned yet (stage 6)
            if (preferences[quest.currentStageKey] == 6) {
                // If pet who died is this dog
                if (preferences[NECRONOMICON_PET_ID_KEY] == e.petId) {
                    // Then return piece of cloth to player and set quest stage to 5
                    userStats.addInventoryItem(
                        InventoryItem(
                            id = InventoryItemId.PieceOfCloth,
                            amount = 1,
                        )
                    )
                    dataStore.edit { store -> store[quest.currentStageKey] = 5 }
                }
            }
        }
    }

    suspend fun getAdditionalAnswers(pet: Pet, nodeKey: String): List<Answer> {
        val preferences = dataStore.data.first()
        val answers = mutableListOf<Answer>()
        quests.forEach { entity ->
            val quest = entity.value
            val currentStageIndex = preferences[quest.currentStageKey] ?: 0
            if (currentStageIndex < quest.stages.size) {
                val stage = quest.stages[currentStageIndex]
                stage.additionalAnswerOptions?.invoke(this, pet, nodeKey)?.let {
                    if (it.isNotEmpty()) answers.addAll(it)
                }
            }
        }
        return answers.toList()
    }

    suspend fun setQuestStage(questKey: String, newStageValue: Int) {
        quests[questKey]?.let { quest ->
            dataStore.edit { store -> store[quest.currentStageKey] = newStageValue }
        }
    }

    suspend fun setQuestStageToNext(questKey: String) {
        quests[questKey]?.let { quest ->
            val current = dataStore.data.first()[quest.currentStageKey] ?: 0
            dataStore.edit { store -> store[quest.currentStageKey] = current + 1 }
        }
    }

    sealed interface Event {
        data class PetMovedToPlace(val pet: Pet, val place: Place) : Event
        data class UserOpenPetDetails(val pet: Pet) : Event
        data class Tick(val secondsSinceEpoch: Long) : Event
        data class PetDied(val petId: Long) : Event
        data class LanguageKnowledgeChanged(val petType: PetType, val value: Int) : Event
    }

    companion object {
        const val QUEST_NECRONOMICON = "QUEST_NECRONOMICON"
    }
}

data class Quest(
    val currentStageKey: Preferences.Key<Int>,
    val stages: List<QuestStage>,
)

data class QuestStage(
    val conditionsKey: Preferences.Key<Set<String>>,
    val initialConditions: Set<String>,
    val onFinish: suspend (QuestSystem) -> Unit,
    val additionalAnswerOptions: (suspend (QuestSystem, Pet, String) -> List<Answer>)?,
)

private const val ALWAYS_FALSE_CONDITION = "ALWAYS_FALSE_CONDITION"
private const val IS_PET_MOVED_TO_CEMETERY = "IS_PET_MOVED_TO_CEMETERY"
private const val NECRONOMICON_STAGE_0_LAMBDA = "NECRONOMICON_STAGE_0_LAMBDA"
private const val NECRONOMICON_STAGE_1_LAMBDA = "NECRONOMICON_STAGE_1_LAMBDA"
private const val NECRONOMICON_STAGE_2_LAMBDA = "NECRONOMICON_STAGE_2_LAMBDA"
private const val NECRONOMICON_STAGE_6_LAMBDA = "NECRONOMICON_STAGE_6_LAMBDA"

private val conditionLambdas =
    mapOf<String, suspend (QuestSystem, Preferences, QuestSystem.Event) -> Boolean>(
        ALWAYS_FALSE_CONDITION to { _, _, _ -> false },
        IS_PET_MOVED_TO_CEMETERY to { _, _, event ->
            (event as? QuestSystem.Event.PetMovedToPlace)?.let { e -> e.place == Place.Cemetery }
                ?: false
        },
        NECRONOMICON_STAGE_0_LAMBDA to { questSystem, _, event ->
            (event as? QuestSystem.Event.LanguageKnowledgeChanged)?.let { e ->
                val catusKnowledge = questSystem.userStats.getLanguageKnowledge(PetType.Catus)
                val dogusKnowledge = questSystem.userStats.getLanguageKnowledge(PetType.Dogus)

                catusKnowledge >= UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE &&
                        dogusKnowledge >= UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE
            } ?: false
        },
        NECRONOMICON_STAGE_1_LAMBDA to { _, prefs, event ->
            (event as? QuestSystem.Event.Tick)?.let { e ->
                prefs[NECRONOMICON_TIMESTAMP_KEY]?.let { timestamp ->
                    e.secondsSinceEpoch - timestamp > DAY
                } ?: false
            } ?: false
        },
        NECRONOMICON_STAGE_2_LAMBDA to { _, _, event ->
            (event as? QuestSystem.Event.UserOpenPetDetails)?.let { e ->
                e.pet.burialType == BurialType.Exhumated
            } ?: false
        },
        NECRONOMICON_STAGE_6_LAMBDA to { _, prefs, event ->
            (event as? QuestSystem.Event.Tick)?.let { e ->
                prefs[NECRONOMICON_TIMESTAMP_KEY]?.let { timestamp ->
                    e.secondsSinceEpoch - timestamp > HOUR
                } ?: false
            } ?: false
        },
    )

val NECRONOMICON_TIMESTAMP_KEY = longPreferencesKey("NECRONOMICON_TIMESTAMP_KEY")
val NECRONOMICON_PET_ID_KEY = longPreferencesKey("NECRONOMICON_PET_ID_KEY")

private val quests = mapOf(
    QUEST_NECRONOMICON to Quest(
        currentStageKey = intPreferencesKey("necronomicon_stage_key"),
        stages = listOf(
            QuestStage(
                conditionsKey = stringSetPreferencesKey("necronomicon_stage_0_conditions"),
                initialConditions = setOf(IS_PET_MOVED_TO_CEMETERY, NECRONOMICON_STAGE_0_LAMBDA),
                onFinish = { questSystem ->
                    val now = getTimestampSecondsSinceEpoch()
                    questSystem.dataStore.edit { store ->
                        store[NECRONOMICON_TIMESTAMP_KEY] = now
                    }
                },
                additionalAnswerOptions = null,
            ),
            QuestStage(
                conditionsKey = stringSetPreferencesKey("necronomicon_stage_1_conditions"),
                initialConditions = setOf(NECRONOMICON_STAGE_1_LAMBDA),
                onFinish = { questSystem ->
                    val deadPets = questSystem.petsRepo.getAllPetsInCemeteryFlow().first()
                    val pet = deadPets[Random.nextInt(deadPets.size)]
                    questSystem.petsRepo.updatePet(
                        pet = pet.copy(
                            burialType = BurialType.Exhumated,
                        )
                    )
                },
                additionalAnswerOptions = null,
            ),
            QuestStage(
                conditionsKey = stringSetPreferencesKey("necronomicon_stage_2_conditions"),
                initialConditions = setOf(NECRONOMICON_STAGE_2_LAMBDA),
                onFinish = {},
                additionalAnswerOptions = null,
            ),
            QuestStage(
                conditionsKey = stringSetPreferencesKey("necronomicon_stage_3_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { _, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING) {
                        listOf(
                            Answer(
                                text = StringId.NecronomiconStage3AnswerOption0,
                                nextNode = if (pet.type == PetType.Dogus) {
                                    DialogSystem.NECRONOMICON_STAGE_3_DOG_DIALOG
                                } else {
                                    DialogSystem.NECRONOMICON_STAGE_3_COMMON_DIALOG
                                },
                            )
                        )
                    } else {
                        emptyList()
                    }
                },
            ),
            QuestStage(
                conditionsKey = stringSetPreferencesKey("necronomicon_stage_4_conditions"),
                initialConditions = setOf(NECRONOMICON_STAGE_2_LAMBDA), // Same, just open exhumated pet details
                onFinish = { questSystem ->
                    questSystem.userStats.addInventoryItem(
                        InventoryItem(
                            id = InventoryItemId.PieceOfCloth,
                            amount = 1,
                        )
                    )
                },
                additionalAnswerOptions = null,
            ),
            QuestStage(
                conditionsKey = stringSetPreferencesKey("necronomicon_stage_5_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { _, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING && pet.type == PetType.Dogus) {
                        listOf(
                            Answer(
                                text = StringId.NecronomiconStage5AnswerOption0,
                                nextNode = DialogSystem.NECRONOMICON_STAGE_5_DOG_DIALOG,
                            )
                        )
                    } else {
                        emptyList()
                    }
                },
            ),
            QuestStage(
                conditionsKey = stringSetPreferencesKey("necronomicon_stage_6_conditions"),
                initialConditions = setOf(NECRONOMICON_STAGE_6_LAMBDA),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    val searchingDogId = questSystem.dataStore.data.first()[NECRONOMICON_PET_ID_KEY] ?: -1L
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING && pet.id == searchingDogId) {
                        listOf(
                            Answer(
                                text = StringId.NecronomiconStage6AnswerOption0,
                                nextNode = DialogSystem.NECRONOMICON_STAGE_6_DOG_DIALOG,
                            )
                        )
                    } else {
                        emptyList()
                    }
                }
            ),
            QuestStage(
                conditionsKey = stringSetPreferencesKey("necronomicon_stage_7_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    val searchingDogId = questSystem.dataStore.data.first()[NECRONOMICON_PET_ID_KEY] ?: -1L
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING && pet.id == searchingDogId) {
                        listOf(
                            Answer(
                                text = StringId.NecronomiconStage6AnswerOption0, // Same, have you found something?
                                nextNode = DialogSystem.NECRONOMICON_STAGE_7_DOG_DIALOG_0,
                            )
                        )
                    } else {
                        emptyList()
                    }
                }
            ),
            QuestStage(
                conditionsKey = stringSetPreferencesKey("necronomicon_stage_8_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { _, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING && pet.type == PetType.Catus) {
                        if (pet.ageState != AgeState.Old) {
                            listOf(
                                Answer(
                                    text = StringId.NecronomiconStage8AnswerOption0,
                                    nextNode = DialogSystem.NECRONOMICON_STAGE_8_DIALOG_0,
                                )
                            )
                        } else {
                            listOf(
                                Answer(
                                    text = StringId.NecronomiconStage8AnswerOption0, // Same as for not old catus
                                    nextNode = DialogSystem.NECRONOMICON_STAGE_8_DIALOG_1,
                                )
                            )
                        }
                    } else {
                        emptyList()
                    }
                }
            )
        )
    )
)