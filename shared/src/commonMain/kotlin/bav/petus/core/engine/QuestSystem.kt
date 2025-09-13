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
import bav.petus.core.engine.QuestSystem.Companion.QUEST_TO_OBTAIN_BOBER
import bav.petus.core.engine.QuestSystem.Companion.QUEST_TO_OBTAIN_FRACTAL
import bav.petus.core.engine.QuestSystem.Companion.QUEST_TO_OBTAIN_FROGUS
import bav.petus.core.engine.UserStats.Companion.MAXIMUM_LANGUAGE_KNOWLEDGE
import bav.petus.core.inventory.InventoryItem
import bav.petus.core.inventory.InventoryItemId
import bav.petus.core.resources.StringId
import bav.petus.core.time.getTimestampSecondsSinceEpoch
import bav.petus.model.AgeState
import bav.petus.model.BodyState
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
                    dataStore.edit { store ->
                        store.remove(stage.conditionsKey)
                        store[quest.currentStageKey] = currentStageIndex + 1
                    }
                }
            }
        }

        // Checks if some pet involved in quest dies (it it's critical for quest)
        if (e is Event.PetDied) {
            // Necronomicon stage 6 check
            val necronomiconQuest = quests[QUEST_NECRONOMICON]!!
            // If dog went on a journey to find the book but haven't returned yet (stage 6)
            if (preferences[necronomiconQuest.currentStageKey] == 6) {
                // If pet who died is this dog
                if (preferences[NECRONOMICON_SEARCH_DOG_ID_KEY] == e.petId) {
                    // Then return piece of cloth to player and set quest stage to 5
                    userStats.addInventoryItem(
                        InventoryItem(
                            id = InventoryItemId.PieceOfCloth,
                            amount = 1,
                        )
                    )
                    dataStore.edit { store -> store[necronomiconQuest.currentStageKey] = 5 }
                }
            }

            // Obtain Frogus stages 2, 3, 4, 5 check
            val obtainFrogusQuest = quests[QUEST_TO_OBTAIN_FROGUS]!!
            val obtainFrogusCurrentStage = preferences[obtainFrogusQuest.currentStageKey]
            if (obtainFrogusCurrentStage in 2..5) {
                // If pet who died is this cat
                if (preferences[OBTAIN_FROGUS_ASKING_CAT_ID_KEY] == e.petId) {
                    // Then remove fish from user inventory (if any) and set stage to 1
                    userStats.removeInventoryItem(
                        InventoryItem(
                            id = InventoryItemId.Fish,
                            amount = 1,
                        )
                    )
                    dataStore.edit { store -> store[obtainFrogusQuest.currentStageKey] = 1 }
                }
            }

            // Obtain Bober stage 3, 4 check
            val obtainBoberQuest = quests[QUEST_TO_OBTAIN_BOBER]!!
            val obtainBoberCurrentStage = preferences[obtainBoberQuest.currentStageKey]
            if (obtainBoberCurrentStage in 3..4) {
                // If pet who died is this dog
                if (preferences[OBTAIN_BOBER_SEARCH_DOG_ID_KEY] == e.petId) {
                    // Then set stage to 2
                    dataStore.edit { store -> store[obtainBoberQuest.currentStageKey] = 2 }
                }
            }
            // Stages 7 and 8
            if (obtainBoberCurrentStage in 7..8) {
                // If pet who died is this dog
                if (preferences[OBTAIN_BOBER_SEARCH_DOG_ID_KEY] == e.petId) {
                    // Then set stage to 6
                    dataStore.edit { store -> store[obtainBoberQuest.currentStageKey] = 6 }
                }
            }
            // Stages 10 and 11
            if (obtainBoberCurrentStage in 10..11) {
                // If pet who died is this frog
                if (preferences[OBTAIN_BOBER_SEARCH_FROG_ID_KEY] == e.petId) {
                    // Then set stage to 9
                    dataStore.edit { store -> store[obtainBoberQuest.currentStageKey] = 9 }
                }
            }

            // Obtain Fractal stage 2..5 check
            val obtainFractalQuest = quests[QUEST_TO_OBTAIN_FRACTAL]!!
            val obtainFractalCurrentStage = preferences[obtainFractalQuest.currentStageKey]
            if (obtainFractalCurrentStage in 2..5) {
                // If pet who died is this frog
                if (preferences[OBTAIN_FRACTAL_FROG_ID_KEY] == e.petId) {
                    // Then set stage to 1
                    dataStore.edit { store -> store[obtainFractalQuest.currentStageKey] = 1 }
                    // And remove rulers from user inventory
                    userStats.removeInventoryItem(
                        InventoryItem(
                            id = InventoryItemId.TwoMeterRuler,
                            amount = 1,
                        )
                    )
                    userStats.removeInventoryItem(
                        InventoryItem(
                            id = InventoryItemId.TenCentimeterRuler,
                            amount = 1,
                        )
                    )
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

    suspend fun getQuestStage(questKey: String): Int {
        val preferences = dataStore.data.first()
        return quests[questKey]?.let { quest ->
            preferences[quest.currentStageKey] ?: 0
        } ?: 0
    }

    fun getQuestStagesAmount(questKey: String): Int {
        return quests[questKey]?.stages?.size ?: 0
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
        const val QUEST_TO_OBTAIN_FROGUS = "QUEST_TO_OBTAIN_FROGUS"
        const val QUEST_TO_OBTAIN_BOBER = "QUEST_TO_OBTAIN_BOBER"
        const val QUEST_TO_OBTAIN_FRACTAL = "QUEST_TO_OBTAIN_FRACTAL"
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

private const val OBTAIN_FROGUS_STAGE_0_LAMBDA = "OBTAIN_FROGUS_STAGE_0_LAMBDA"
private const val OBTAIN_FROGUS_STAGE_2_LAMBDA = "OBTAIN_FROGUS_STAGE_2_LAMBDA"
private const val OBTAIN_FROGUS_STAGE_4_LAMBDA = "OBTAIN_FROGUS_STAGE_4_LAMBDA"

private const val OBTAIN_BOBER_STAGE_0_LAMBDA = "OBTAIN_BOBER_STAGE_0_LAMBDA"
private const val OBTAIN_BOBER_STAGE_1_LAMBDA = "OBTAIN_BOBER_STAGE_1_LAMBDA"
private const val OBTAIN_BOBER_STAGE_3_LAMBDA = "OBTAIN_BOBER_STAGE_3_LAMBDA"
private const val OBTAIN_BOBER_STAGE_5_LAMBDA = "OBTAIN_BOBER_STAGE_5_LAMBDA"

private const val OBTAIN_FRACTAL_STAGE_0_LAMBDA = "OBTAIN_FRACTAL_STAGE_0_LAMBDA"
private const val OBTAIN_FRACTAL_STAGE_2_LAMBDA = "OBTAIN_FRACTAL_STAGE_2_LAMBDA"

private val conditionLambdas =
    mapOf<String, suspend (QuestSystem, Preferences, QuestSystem.Event) -> Boolean>(
        ALWAYS_FALSE_CONDITION to { _, _, _ -> false },
        IS_PET_MOVED_TO_CEMETERY to { _, _, event ->
            (event as? QuestSystem.Event.PetMovedToPlace)?.let { e -> e.place == Place.Cemetery }
                ?: false
        },
        NECRONOMICON_STAGE_0_LAMBDA to { questSystem, _, event ->
            (event as? QuestSystem.Event.LanguageKnowledgeChanged)?.let { _ ->
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
        OBTAIN_FROGUS_STAGE_0_LAMBDA to { questSystem, _, event ->
            (event as? QuestSystem.Event.LanguageKnowledgeChanged)?.let { _ ->
                val catusKnowledge = questSystem.userStats.getLanguageKnowledge(PetType.Catus)
                catusKnowledge >= UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE
            } ?: false
        },
        OBTAIN_FROGUS_STAGE_2_LAMBDA to { _, prefs, event ->
            (event as? QuestSystem.Event.Tick)?.let { e ->
                prefs[OBTAIN_FROGUS_TIMESTAMP_KEY]?.let { timestamp ->
                    e.secondsSinceEpoch - timestamp > DAY
                } ?: false
            } ?: false
        },
        OBTAIN_FROGUS_STAGE_4_LAMBDA to { _, prefs, event ->
            (event as? QuestSystem.Event.Tick)?.let { e ->
                prefs[OBTAIN_FROGUS_TIMESTAMP_KEY]?.let { timestamp ->
                    e.secondsSinceEpoch - timestamp > DAY
                } ?: false
            } ?: false
        },
        OBTAIN_BOBER_STAGE_0_LAMBDA to { questSystem, _, event ->
            (event as? QuestSystem.Event.LanguageKnowledgeChanged)?.let { _ ->
                val frogusKnowledge = questSystem.userStats.getLanguageKnowledge(PetType.Frogus)
                val dogusKnowledge = questSystem.userStats.getLanguageKnowledge(PetType.Dogus)

                frogusKnowledge >= UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE &&
                        dogusKnowledge >= UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE
            } ?: false
        },
        OBTAIN_BOBER_STAGE_1_LAMBDA to { _, prefs, event ->
            (event as? QuestSystem.Event.Tick)?.let { e ->
                prefs[OBTAIN_BOBER_TIMESTAMP_KEY]?.let { timestamp ->
                    e.secondsSinceEpoch - timestamp > HOUR
                } ?: false
            } ?: false
        },
        OBTAIN_BOBER_STAGE_3_LAMBDA to { _, prefs, event ->
            (event as? QuestSystem.Event.Tick)?.let { e ->
                prefs[OBTAIN_BOBER_TIMESTAMP_KEY]?.let { timestamp ->
                    e.secondsSinceEpoch - timestamp > DAY
                } ?: false
            } ?: false
        },
        OBTAIN_BOBER_STAGE_5_LAMBDA to { _, prefs, event ->
            (event as? QuestSystem.Event.Tick)?.let { e ->
                prefs[OBTAIN_BOBER_TIMESTAMP_KEY]?.let { timestamp ->
                    e.secondsSinceEpoch - timestamp > HOUR * 3
                } ?: false
            } ?: false
        },
        OBTAIN_FRACTAL_STAGE_0_LAMBDA to { questSystem, _, event ->
            (event as? QuestSystem.Event.LanguageKnowledgeChanged)?.let { _ ->
                val frogusKnowledge = questSystem.userStats.getLanguageKnowledge(PetType.Frogus)
                val boberKnowledge = questSystem.userStats.getLanguageKnowledge(PetType.Bober)

                frogusKnowledge >= UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE &&
                        boberKnowledge >= UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE
            } ?: false
        },
        OBTAIN_FRACTAL_STAGE_2_LAMBDA to { _, prefs, event ->
            (event as? QuestSystem.Event.Tick)?.let { e ->
                prefs[OBTAIN_FRACTAL_TIMESTAMP_KEY]?.let { timestamp ->
                    e.secondsSinceEpoch - timestamp > DAY
                } ?: false
            } ?: false
        },
    )

val NECRONOMICON_TIMESTAMP_KEY = longPreferencesKey("NECRONOMICON_TIMESTAMP_KEY")
val NECRONOMICON_EXHUMATED_PET_ID_KEY = longPreferencesKey("NECRONOMICON_EXHUMATED_PET_ID_KEY")
val NECRONOMICON_SEARCH_DOG_ID_KEY = longPreferencesKey("NECRONOMICON_SEARCH_DOG_ID_KEY")
val NECRONOMICON_WISE_CAT_ID_KEY = longPreferencesKey("NECRONOMICON_WISE_CAT_ID_KEY")

val OBTAIN_FROGUS_TIMESTAMP_KEY = longPreferencesKey("OBTAIN_FROGUS_TIMESTAMP_KEY")
val OBTAIN_FROGUS_ASKING_CAT_ID_KEY = longPreferencesKey("OBTAIN_FROGUS_ASKING_CAT_ID_KEY")

val OBTAIN_BOBER_TIMESTAMP_KEY = longPreferencesKey("OBTAIN_BOBER_TIMESTAMP_KEY")
val OBTAIN_BOBER_SEARCH_DOG_ID_KEY = longPreferencesKey("OBTAIN_BOBER_ASKING_DOG_ID_KEY")
val OBTAIN_BOBER_SEARCH_FROG_ID_KEY = longPreferencesKey("OBTAIN_BOBER_SEARCH_FROG_ID_KEY")

val OBTAIN_FRACTAL_TIMESTAMP_KEY = longPreferencesKey("OBTAIN_FRACTAL_TIMESTAMP_KEY")
val OBTAIN_FRACTAL_FROG_ID_KEY = longPreferencesKey("OBTAIN_FRACTAL_FROG_ID_KEY")

private val quests = mapOf(
    QUEST_NECRONOMICON to Quest(
        currentStageKey = intPreferencesKey("necronomicon_stage_key"),
        stages = listOf(
            // Stage 0 - make sure user understands cat and dog 100%, and any pet moved to cemetery
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
            // Stage 1 - wait 1 day and exhumate random grave
            QuestStage(
                conditionsKey = stringSetPreferencesKey("necronomicon_stage_1_conditions"),
                initialConditions = setOf(NECRONOMICON_STAGE_1_LAMBDA),
                onFinish = { questSystem ->
                    val deadPets = questSystem.petsRepo.getAllPetsInCemeteryFlow().first()
                    val pet = deadPets[Random.nextInt(deadPets.size)]
                    questSystem.dataStore.edit { store ->
                        store[NECRONOMICON_EXHUMATED_PET_ID_KEY] = pet.id
                    }
                    questSystem.petsRepo.updatePet(
                        pet = pet.copy(
                            burialType = BurialType.Exhumated,
                        )
                    )
                },
                additionalAnswerOptions = null,
            ),
            // Stage 2 - make sure user opened exhumated grave
            QuestStage(
                conditionsKey = stringSetPreferencesKey("necronomicon_stage_2_conditions"),
                initialConditions = setOf(NECRONOMICON_STAGE_2_LAMBDA),
                onFinish = {},
                additionalAnswerOptions = null,
            ),
            // Stage 3 - dialog stage - ask any dog if he knows something, dog will ask user to bring something from the grave
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
            // Stage 4 - open exhumated grave details to find piece of cloth
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
            // Stage 5 - show piece of cloth to any dog, he will go looking
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
            // Stage 6 - wait 1 hour for dog to find the book
            QuestStage(
                conditionsKey = stringSetPreferencesKey("necronomicon_stage_6_conditions"),
                initialConditions = setOf(NECRONOMICON_STAGE_6_LAMBDA),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    val searchingDogId = questSystem.dataStore.data.first()[NECRONOMICON_SEARCH_DOG_ID_KEY] ?: -1L
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
            // Stage 7 - dialog stage - receive the book from a dog
            QuestStage(
                conditionsKey = stringSetPreferencesKey("necronomicon_stage_7_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    val searchingDogId = questSystem.dataStore.data.first()[NECRONOMICON_SEARCH_DOG_ID_KEY] ?: -1L
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
            // Stage 8 - dialog stage - ask old cat about book - either use it or destroy
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
                                    nextNode = DialogSystem.NECRONOMICON_STAGE_8_DIALOG_1, // Different dialog
                                )
                            )
                        }
                    } else {
                        emptyList()
                    }
                }
            )
        )
    ),
    QUEST_TO_OBTAIN_FROGUS to Quest(
        currentStageKey = intPreferencesKey("obtain_frogus_stage_key"),
        stages = listOf(
            // Stage 0 - make sure user understands cat 100%
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_frogus_stage_0_conditions"),
                initialConditions = setOf(OBTAIN_FROGUS_STAGE_0_LAMBDA),
                onFinish = {},
                additionalAnswerOptions = null,
            ),
            // Stage 1 - dialog stage - adult cat should request fish
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_frogus_stage_1_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { _, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Catus &&
                        pet.ageState == AgeState.Adult &&
                        pet.bodyState == BodyState.Alive)
                    {
                        listOf(
                            Answer(
                                text = StringId.ObtainFrogusStage1Answer0,
                                nextNode = DialogSystem.OBTAIN_FROGUS_STAGE_1_DIALOG_0,
                            )
                        )
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 2 - wait for 1 day then add fish to user inventory
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_frogus_stage_2_conditions"),
                initialConditions = setOf(OBTAIN_FROGUS_STAGE_2_LAMBDA),
                onFinish = { questSystem ->
                    questSystem.userStats.addInventoryItem(
                        InventoryItem(
                            id = InventoryItemId.Fish,
                            amount = 1,
                        )
                    )
                },
                additionalAnswerOptions = null,
            ),
            // Stage 3 - dialog stage - give fish to cat
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_frogus_stage_3_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    val askingCatId = questSystem.dataStore.data.first()[OBTAIN_FROGUS_ASKING_CAT_ID_KEY] ?: -1L
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.id == askingCatId &&
                        pet.bodyState == BodyState.Alive)
                    {
                        listOf(
                            Answer(
                                text = StringId.ObtainFrogusStage3Answer0,
                                nextNode = DialogSystem.OBTAIN_FROGUS_STAGE_3_DIALOG_0,
                                action = { actionQuestSystem, _, _ ->
                                    actionQuestSystem.userStats.removeInventoryItem(
                                        InventoryItem(
                                            id = InventoryItemId.Fish,
                                            amount = 1,
                                        )
                                    )
                                }
                            )
                        )
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 4 - wait 1 day for cat to find frogus egg on the lake
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_frogus_stage_4_conditions"),
                initialConditions = setOf(OBTAIN_FROGUS_STAGE_4_LAMBDA),
                onFinish = {},
                additionalAnswerOptions = null,
            ),
            // Stage 5 - dialog stage - give frogus egg to user
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_frogus_stage_5_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    val askingCatId = questSystem.dataStore.data.first()[OBTAIN_FROGUS_ASKING_CAT_ID_KEY] ?: -1L
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.id == askingCatId &&
                        pet.bodyState == BodyState.Alive)
                    {
                        listOf(
                            Answer(
                                text = StringId.ObtainFrogusStage5Answer0,
                                nextNode = DialogSystem.OBTAIN_FROGUS_STAGE_5_DIALOG_0,
                            )
                        )
                    } else {
                        emptyList()
                    }
                },
            ),
        )
    ),
    QUEST_TO_OBTAIN_BOBER to Quest(
        currentStageKey = intPreferencesKey("obtain_bober_stage_key"),
        stages = listOf(
            // Stage 0 - make sure user understands dogus and frogus 100%
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_bober_stage_0_conditions"),
                initialConditions = setOf(OBTAIN_BOBER_STAGE_0_LAMBDA),
                onFinish = { questSystem ->
                    val now = getTimestampSecondsSinceEpoch()
                    questSystem.dataStore.edit { store ->
                        store[OBTAIN_BOBER_TIMESTAMP_KEY] = now
                    }
                },
                additionalAnswerOptions = null,
            ),
            // Stage 1 - wait 1 hour and remove all eggs
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_bober_stage_1_conditions"),
                initialConditions = setOf(OBTAIN_BOBER_STAGE_1_LAMBDA),
                onFinish = { questSystem ->
                    questSystem.userStats.removeInventoryItem(
                        InventoryItem(id = InventoryItemId.CatusEgg, amount = 1)
                    )
                    questSystem.userStats.removeInventoryItem(
                        InventoryItem(id = InventoryItemId.DogusEgg, amount = 1)
                    )
                    questSystem.userStats.removeInventoryItem(
                        InventoryItem(id = InventoryItemId.FrogusEgg, amount = 1)
                    )
                },
                additionalAnswerOptions = null,
            ),
            // Stage 2 - dialog with any dog - ask him to search for eggs
            //   - save timestamp to OBTAIN_BOBER_TIMESTAMP_KEY
            //   - save dog id to OBTAIN_BOBER_ASKING_DOG_ID_KEY
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_bober_stage_2_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { _, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.bodyState == BodyState.Alive)
                    {
                        if (pet.type == PetType.Dogus) {
                            listOf(
                                Answer(
                                    text = StringId.ObtainBoberStage2Answer0,
                                    nextNode = DialogSystem.OBTAIN_BOBER_STAGE_2_DIALOG_0,
                                )
                            )
                        } else {
                            listOf(
                                Answer(
                                    text = StringId.ObtainBoberStage2Answer0,
                                    nextNode = DialogSystem.OBTAIN_BOBER_STAGE_2_DIALOG_1,
                                )
                            )
                        }
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 3 - wait 1 day until dog finds the eggs
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_bober_stage_3_conditions"),
                initialConditions = setOf(OBTAIN_BOBER_STAGE_3_LAMBDA),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    val searchDogId = questSystem.dataStore.data.first()[OBTAIN_BOBER_SEARCH_DOG_ID_KEY] ?: -1L
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        searchDogId == pet.id &&
                        pet.bodyState == BodyState.Alive)
                    {
                        listOf(
                            Answer(
                                text = StringId.ObtainBoberStage3Answer0,
                                nextNode = DialogSystem.OBTAIN_BOBER_STAGE_3_DIALOG_0,
                            )
                        )
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 4 - dialog with dog - asked dog returns eggs
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_bober_stage_4_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    val searchDogId = questSystem.dataStore.data.first()[OBTAIN_BOBER_SEARCH_DOG_ID_KEY] ?: -1L
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        searchDogId == pet.id &&
                        pet.bodyState == BodyState.Alive)
                    {
                        listOf(
                            Answer(
                                text = StringId.ObtainBoberStage3Answer0, // Same question as in previous stage
                                nextNode = DialogSystem.OBTAIN_BOBER_STAGE_4_DIALOG_0, // But different dialog
                            )
                        )
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 5 - wait 3 hours until losing eggs again
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_bober_stage_5_conditions"),
                initialConditions = setOf(OBTAIN_BOBER_STAGE_5_LAMBDA),
                onFinish = { questSystem ->
                    questSystem.userStats.removeInventoryItem(
                        InventoryItem(id = InventoryItemId.CatusEgg, amount = 1)
                    )
                    questSystem.userStats.removeInventoryItem(
                        InventoryItem(id = InventoryItemId.DogusEgg, amount = 1)
                    )
                    questSystem.userStats.removeInventoryItem(
                        InventoryItem(id = InventoryItemId.FrogusEgg, amount = 1)
                    )
                },
                additionalAnswerOptions = null,
            ),
            // Stage 6 - ask any dog about lost eggs again
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_bober_stage_6_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { _, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.bodyState == BodyState.Alive)
                    {
                        if (pet.type == PetType.Dogus) {
                            listOf(
                                Answer(
                                    text = StringId.ObtainBoberStage6Answer0,
                                    nextNode = DialogSystem.OBTAIN_BOBER_STAGE_6_DIALOG_0,
                                )
                            )
                        } else {
                            listOf(
                                Answer(
                                    text = StringId.ObtainBoberStage6Answer0,
                                    nextNode = DialogSystem.OBTAIN_BOBER_STAGE_6_DIALOG_1,
                                )
                            )
                        }
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 7 - wait again until dog finds the eggs
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_bober_stage_7_conditions"),
                initialConditions = setOf(OBTAIN_BOBER_STAGE_3_LAMBDA), // Lambda can be the same as on stage 3 - wait for 1 day.
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    val searchDogId = questSystem.dataStore.data.first()[OBTAIN_BOBER_SEARCH_DOG_ID_KEY] ?: -1L
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        searchDogId == pet.id &&
                        pet.bodyState == BodyState.Alive)
                    {
                        listOf(
                            Answer(
                                text = StringId.ObtainBoberStage3Answer0,
                                nextNode = DialogSystem.OBTAIN_BOBER_STAGE_3_DIALOG_0,
                            )
                        )
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 8 - dog found eggs again
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_bober_stage_8_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    val searchDogId = questSystem.dataStore.data.first()[OBTAIN_BOBER_SEARCH_DOG_ID_KEY] ?: -1L
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        searchDogId == pet.id &&
                        pet.bodyState == BodyState.Alive)
                    {
                        listOf(
                            Answer(
                                text = StringId.ObtainBoberStage3Answer0, // Same question as in previous stage
                                nextNode = DialogSystem.OBTAIN_BOBER_STAGE_8_DIALOG_0, // But different dialog
                            )
                        )
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 9 - ask frog to help with eggs
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_bober_stage_9_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val searchDogId = questSystem.dataStore.data.first()[OBTAIN_BOBER_SEARCH_DOG_ID_KEY] ?: -1L
                        if (pet.type == PetType.Frogus) {
                            listOf(
                                Answer(
                                    text = StringId.ObtainBoberStage9Answer0,
                                    nextNode = DialogSystem.OBTAIN_BOBER_STAGE_9_DIALOG_0,
                                )
                            )
                        } else if (searchDogId == pet.id) {
                            listOf(
                                Answer(
                                    text = StringId.ObtainBoberStage9Answer0,
                                    nextNode = DialogSystem.OBTAIN_BOBER_STAGE_9_DIALOG_1,
                                )
                            )
                        } else {
                            listOf(
                                Answer(
                                    text = StringId.ObtainBoberStage9Answer0,
                                    nextNode = DialogSystem.OBTAIN_BOBER_STAGE_9_DIALOG_2,
                                )
                            )
                        }
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 10 - wait 1 day for frog to get bober egg and basket
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_bober_stage_10_conditions"),
                initialConditions = setOf(OBTAIN_BOBER_STAGE_3_LAMBDA), // Lambda can be the same as on stage 3 - wait for 1 day.
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    val searchFrogId = questSystem.dataStore.data.first()[OBTAIN_BOBER_SEARCH_FROG_ID_KEY] ?: -1L
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        searchFrogId == pet.id &&
                        pet.bodyState == BodyState.Alive)
                    {
                        listOf(
                            Answer(
                                text = StringId.ObtainBoberStage10Answer0,
                                nextNode = DialogSystem.OBTAIN_BOBER_STAGE_10_DIALOG_0,
                            )
                        )
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 11 - frog gives bober egg and basket
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_bober_stage_11_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    val searchDogId = questSystem.dataStore.data.first()[OBTAIN_BOBER_SEARCH_FROG_ID_KEY] ?: -1L
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        searchDogId == pet.id &&
                        pet.bodyState == BodyState.Alive)
                    {
                        listOf(
                            Answer(
                                text = StringId.ObtainBoberStage10Answer0, // Same question as in previous stage
                                nextNode = DialogSystem.OBTAIN_BOBER_STAGE_11_DIALOG_0, // But different dialog
                            )
                        )
                    } else {
                        emptyList()
                    }
                },
            ),
        )
    ),
    QUEST_TO_OBTAIN_FRACTAL to Quest(
        currentStageKey = intPreferencesKey("obtain_fractal_stage_key"),
        stages = listOf(
            // Stage 0 - make sure user understands frogus and bober 100%
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_fractal_stage_0_conditions"),
                initialConditions = setOf(OBTAIN_FRACTAL_STAGE_0_LAMBDA),
                onFinish = {},
                additionalAnswerOptions = null,
            ),
            // Stage 1 - dialog stage - frogus gives big meter and asks user to measure pond coastline
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_fractal_stage_1_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Frogus &&
                        pet.bodyState == BodyState.Alive)
                    {
                        listOf(
                            Answer(
                                text = StringId.ObtainFractalStage1Answer0,
                                nextNode = DialogSystem.OBTAIN_FRACTAL_STAGE_1_DIALOG_0,
                            )
                        )
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 2 - wait 1 day to measure pond coastline using 2-meter ruler
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_fractal_stage_2_conditions"),
                initialConditions = setOf(OBTAIN_FRACTAL_STAGE_2_LAMBDA),
                onFinish = {},
                additionalAnswerOptions = null,
            ),
            // Stage 3 - dialog stage - report to frogus about measurement, and go measure with 10 cm ruler
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_fractal_stage_3_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    val wiseFrogId = questSystem.dataStore.data.first()[OBTAIN_FRACTAL_FROG_ID_KEY] ?: -1L
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        wiseFrogId == pet.id &&
                        pet.bodyState == BodyState.Alive)
                    {
                        listOf(
                            Answer(
                                text = StringId.ObtainFractalStage3Answer0,
                                nextNode = DialogSystem.OBTAIN_FRACTAL_STAGE_3_DIALOG_0,
                            )
                        )
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 4 - wait 1 day to measure pond coastline using 10-centimeter ruler
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_fractal_stage_4_conditions"),
                initialConditions = setOf(OBTAIN_FRACTAL_STAGE_2_LAMBDA), // Same as in stage 2
                onFinish = {},
                additionalAnswerOptions = null,
            ),
            // Stage 5 - dialog stage - frogus explains coastline paradox and forward user to bober to acquire the book
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_fractal_stage_5_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    val wiseFrogId = questSystem.dataStore.data.first()[OBTAIN_FRACTAL_FROG_ID_KEY] ?: -1L
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        wiseFrogId == pet.id &&
                        pet.bodyState == BodyState.Alive)
                    {
                        listOf(
                            Answer(
                                text = StringId.ObtainFractalStage5Answer0,
                                nextNode = DialogSystem.OBTAIN_FRACTAL_STAGE_5_DIALOG_0,
                            )
                        )
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 6 - dialog stage - bober gives user math book
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_fractal_stage_6_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Bober &&
                        pet.bodyState == BodyState.Alive)
                    {
                        listOf(
                            Answer(
                                text = StringId.ObtainFractalStage6Answer0,
                                nextNode = DialogSystem.OBTAIN_FRACTAL_STAGE_6_DIALOG_0,
                            )
                        )
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 7 - wait for 1 day to read the book
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_fractal_stage_7_conditions"),
                initialConditions = setOf(OBTAIN_FRACTAL_STAGE_2_LAMBDA), // Same as in stage 2
                onFinish = { questSystem ->
                    questSystem.userStats.addNewAvailablePetType(PetType.Fractal)
                    questSystem.userStats.saveLanguageKnowledge(PetType.Fractal, MAXIMUM_LANGUAGE_KNOWLEDGE)
                },
                additionalAnswerOptions = null,
            ),
        )
    ),
)