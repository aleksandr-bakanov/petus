package bav.petus.core.engine

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import bav.petus.core.dialog.Answer
import bav.petus.core.dialog.DialogSystem
import bav.petus.core.engine.Engine.Companion.DAY
import bav.petus.core.engine.Engine.Companion.HOUR
import bav.petus.core.engine.QuestSystem.Companion.QUEST_MEDITATION
import bav.petus.core.engine.QuestSystem.Companion.QUEST_NECRONOMICON
import bav.petus.core.engine.QuestSystem.Companion.QUEST_TO_OBTAIN_BOBER
import bav.petus.core.engine.QuestSystem.Companion.QUEST_TO_OBTAIN_DRAGON
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

            // Meditation stage 3 check
            val meditationQuest = quests[QUEST_MEDITATION]!!
            val meditationCurrentStage = preferences[meditationQuest.currentStageKey]
            if (meditationCurrentStage == 3) {
                // If pet who died is this frog
                if (preferences[MEDITATION_FROG_ID_KEY] == e.petId) {
                    // Then set stage to 2
                    dataStore.edit { store ->
                        store[meditationQuest.currentStageKey] = 2
                    }
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

    suspend fun resetQuest(questName: String) {
        when (questName) {
            QUEST_NECRONOMICON -> {
                val necronomiconQuest = quests[QUEST_NECRONOMICON]!!
                // Remove all items
                userStats.removeInventoryItem(InventoryItem(id = InventoryItemId.PieceOfCloth, amount = 1), false)
                userStats.removeInventoryItem(InventoryItem(id = InventoryItemId.MysteriousBook, amount = 1), false)
                userStats.removeInventoryItem(InventoryItem(id = InventoryItemId.Necronomicon, amount = 1), false)
                // Remove all abilities
                userStats.removeAbility(Ability.Necromancy)
                // Remove available pet type (if applicable)

                // Fix possibly dug out grave
                dataStore.data.first()[NECRONOMICON_EXHUMATED_PET_ID_KEY]?.let { bodyPetId ->
                    petsRepo.getPetByIdFlow(bodyPetId).first()?.let { pet ->
                        if (pet.burialType == BurialType.Exhumated) {
                            petsRepo.updatePet(
                                pet = pet.copy(
                                    burialType = BurialType.Buried,
                                )
                            )
                        }
                    }
                }

                dataStore.edit { store ->
                    // Remove all stages conditions
                    for (index in 0 until necronomiconQuest.stages.size) {
                        store.remove(stringSetPreferencesKey("necronomicon_stage_${index}_conditions"))
                    }
                    // Remove all special keys
                    store.remove(NECRONOMICON_TIMESTAMP_KEY)
                    store.remove(NECRONOMICON_EXHUMATED_PET_ID_KEY)
                    store.remove(NECRONOMICON_SEARCH_DOG_ID_KEY)
                    store.remove(NECRONOMICON_WISE_CAT_ID_KEY)
                    // Reset stage to zero
                    store[necronomiconQuest.currentStageKey] = 0
                }
            }
            QUEST_TO_OBTAIN_FROGUS -> {
                val obtainFrogusQuest = quests[QUEST_TO_OBTAIN_FROGUS]!!
                // Remove all items
                userStats.removeInventoryItem(InventoryItem(id = InventoryItemId.Fish, amount = 1), false)
                userStats.removeInventoryItem(InventoryItem(id = InventoryItemId.FrogusEgg, amount = 1), false)
                // Remove all abilities

                // Remove available pet type (if applicable)
                userStats.removeAvailablePetType(PetType.Frogus)

                dataStore.edit { store ->
                    // Remove all stages conditions
                    for (index in 0 until obtainFrogusQuest.stages.size) {
                        store.remove(stringSetPreferencesKey("obtain_frogus_stage_${index}_conditions"))
                    }
                    // Remove all special keys
                    store.remove(OBTAIN_FROGUS_TIMESTAMP_KEY)
                    store.remove(OBTAIN_FROGUS_ASKING_CAT_ID_KEY)
                    // Reset stage to zero
                    store[obtainFrogusQuest.currentStageKey] = 0
                }
            }
            QUEST_TO_OBTAIN_BOBER -> {
                val obtainBoberQuest = quests[QUEST_TO_OBTAIN_BOBER]!!
                // Remove all items
                userStats.removeInventoryItem(InventoryItem(id = InventoryItemId.Basket, amount = 1), false)
                userStats.removeInventoryItem(InventoryItem(id = InventoryItemId.BoberEgg, amount = 1), false)
                // Remove all abilities

                // Remove available pet type (if applicable)
                userStats.removeAvailablePetType(PetType.Bober)

                dataStore.edit { store ->
                    // Remove all stages conditions
                    for (index in 0 until obtainBoberQuest.stages.size) {
                        store.remove(stringSetPreferencesKey("obtain_bober_stage_${index}_conditions"))
                    }
                    // Remove all special keys
                    store.remove(OBTAIN_BOBER_TIMESTAMP_KEY)
                    store.remove(OBTAIN_BOBER_SEARCH_DOG_ID_KEY)
                    store.remove(OBTAIN_BOBER_SEARCH_FROG_ID_KEY)
                    // Reset stage to zero
                    store[obtainBoberQuest.currentStageKey] = 0
                }
            }
            QUEST_TO_OBTAIN_FRACTAL -> {
                val obtainFractalQuest = quests[QUEST_TO_OBTAIN_FRACTAL]!!
                // Remove all items
                userStats.removeInventoryItem(InventoryItem(id = InventoryItemId.TwoMeterRuler, amount = 1), false)
                userStats.removeInventoryItem(InventoryItem(id = InventoryItemId.TenCentimeterRuler, amount = 1), false)
                userStats.removeInventoryItem(InventoryItem(id = InventoryItemId.MathBook, amount = 1), false)
                // Remove all abilities

                // Remove available pet type (if applicable)
                userStats.removeAvailablePetType(PetType.Fractal)

                dataStore.edit { store ->
                    // Remove all stages conditions
                    for (index in 0 until obtainFractalQuest.stages.size) {
                        store.remove(stringSetPreferencesKey("obtain_fractal_stage_${index}_conditions"))
                    }
                    // Remove all special keys
                    store.remove(OBTAIN_FRACTAL_TIMESTAMP_KEY)
                    store.remove(OBTAIN_FRACTAL_FROG_ID_KEY)
                    // Reset stage to zero
                    store[obtainFractalQuest.currentStageKey] = 0
                }
            }
            QUEST_MEDITATION -> {
                val meditationQuest = quests[QUEST_MEDITATION]!!
                // Remove all items

                // Remove all abilities
                userStats.removeAbility(Ability.Meditation)
                // Remove available pet type (if applicable)

                dataStore.edit { store ->
                    // Remove all stages conditions
                    for (index in 0 until meditationQuest.stages.size) {
                        store.remove(stringSetPreferencesKey("meditation_stage_${index}_conditions"))
                    }
                    // Remove all special keys
                    store.remove(MEDITATION_TIMESTAMP_KEY)
                    store.remove(MEDITATION_FROG_ID_KEY)
                    store.remove(MEDITATION_EXERCISES_LEFT_KEY)
                    // Reset stage to zero
                    store[meditationQuest.currentStageKey] = 0
                }
            }
            QUEST_TO_OBTAIN_DRAGON -> {
                val obtainDragonQuest = quests[QUEST_TO_OBTAIN_DRAGON]!!
                // Remove all items
                userStats.removeInventoryItem(InventoryItem(id = InventoryItemId.DragonEgg, amount = 1), false)
                // Don't delete achievements (such a Memories and Curses)

                // Remove all abilities

                // Remove available pet type (if applicable)
                userStats.removeAvailablePetType(PetType.Dragon)

                // Clear questName from all participants
                val store = dataStore.data.first()
                val keys = listOf(
                    OBTAIN_DRAGON_CATUS_ID_KEY,
                    OBTAIN_DRAGON_DOGUS_ID_KEY,
                    OBTAIN_DRAGON_FROGUS_ID_KEY,
                    OBTAIN_DRAGON_BOBER_ID_KEY
                )
                for (key in keys) {
                    store[key]?.let { petId ->
                        petsRepo.getPetByIdFlow(petId).first()?.let { pet ->
                            petsRepo.updatePet(pet = pet.copy(questName = null))
                        }
                    }
                }

                dataStore.edit { store ->
                    // Remove all stages conditions
                    for (index in 0 until obtainDragonQuest.stages.size) {
                        store.remove(stringSetPreferencesKey("obtain_dragon_stage_${index}_conditions"))
                    }
                    // Remove all special keys
                    store.remove(OBTAIN_DRAGON_CATUS_ID_KEY)
                    store.remove(OBTAIN_DRAGON_DOGUS_ID_KEY)
                    store.remove(OBTAIN_DRAGON_FROGUS_ID_KEY)
                    store.remove(OBTAIN_DRAGON_BOBER_ID_KEY)
                    store.remove(OBTAIN_DRAGON_TIMESTAMP_KEY)
                    store.remove(OBTAIN_DRAGON_DOG_ASKED_KEY)
                    store.remove(OBTAIN_DRAGON_FROG_ASKED_KEY)
                    store.remove(OBTAIN_DRAGON_BOBER_ASKED_KEY)
                    store.remove(OBTAIN_DRAGON_MISTY_GORGE_DECISION_KEY)
                    store.remove(OBTAIN_DRAGON_FOREST_DECISION_KEY)
                    store.remove(OBTAIN_DRAGON_STONE_DECISION_KEY)
                    store.remove(OBTAIN_DRAGON_ASH_DECISION_KEY)
                    store.remove(OBTAIN_DRAGON_HAS_NECRONOMICON_KEY)
                    store.remove(OBTAIN_DRAGON_CAT_IS_SACRIFICE_KEY)

                    // Reset stage to zero
                    store[obtainDragonQuest.currentStageKey] = 0
                }
            }
        }
        // Simulate language knowledge event because all quests require
        // language knowledge as the first step
        onEvent(Event.LanguageKnowledgeChanged(PetType.Catus, 0))
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
        const val QUEST_MEDITATION = "QUEST_MEDITATION"
        const val QUEST_TO_OBTAIN_DRAGON = "QUEST_TO_OBTAIN_DRAGON"
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

private const val MEDITATION_STAGE_0_LAMBDA = "MEDITATION_STAGE_0_LAMBDA"

private const val OBTAIN_DRAGON_STAGE_0_LAMBDA = "OBTAIN_DRAGON_STAGE_0_LAMBDA"
private const val OBTAIN_DRAGON_STAGE_3_LAMBDA = "OBTAIN_DRAGON_STAGE_3_LAMBDA"
private const val OBTAIN_DRAGON_STAGE_6_LAMBDA = "OBTAIN_DRAGON_STAGE_6_LAMBDA"

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
        MEDITATION_STAGE_0_LAMBDA to { questSystem, _, event ->
            (event as? QuestSystem.Event.LanguageKnowledgeChanged)?.let { _ ->
                val dogusKnowledge = questSystem.userStats.getLanguageKnowledge(PetType.Dogus)
                val frogusKnowledge = questSystem.userStats.getLanguageKnowledge(PetType.Frogus)

                dogusKnowledge >= UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE &&
                        frogusKnowledge >= UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE
            } ?: false
        },
        OBTAIN_DRAGON_STAGE_0_LAMBDA to { questSystem, _, event ->
            (event as? QuestSystem.Event.LanguageKnowledgeChanged)?.let { _ ->
                val catusKnowledge = questSystem.userStats.getLanguageKnowledge(PetType.Catus)
                val dogusKnowledge = questSystem.userStats.getLanguageKnowledge(PetType.Dogus)
                val frogusKnowledge = questSystem.userStats.getLanguageKnowledge(PetType.Frogus)
                val boberKnowledge = questSystem.userStats.getLanguageKnowledge(PetType.Bober)

                catusKnowledge >= UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE &&
                        dogusKnowledge >= UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE &&
                        frogusKnowledge >= UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE &&
                        boberKnowledge >= UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE
            } ?: false
        },
        OBTAIN_DRAGON_STAGE_3_LAMBDA to { _, prefs, event ->
            (event as? QuestSystem.Event.Tick)?.let { e ->
                prefs[OBTAIN_DRAGON_TIMESTAMP_KEY]?.let { timestamp ->
                    e.secondsSinceEpoch - timestamp > DAY
                } ?: false
            } ?: false
        },
        OBTAIN_DRAGON_STAGE_6_LAMBDA to { _, prefs, event ->
            (event as? QuestSystem.Event.Tick)?.let { e ->
                prefs[OBTAIN_DRAGON_TIMESTAMP_KEY]?.let { timestamp ->
                    e.secondsSinceEpoch - timestamp > HOUR
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

val MEDITATION_TIMESTAMP_KEY = longPreferencesKey("MEDITATION_TIMESTAMP_KEY")
val MEDITATION_FROG_ID_KEY = longPreferencesKey("MEDITATION_FROG_ID_KEY")
val MEDITATION_EXERCISES_LEFT_KEY = longPreferencesKey("MEDITATION_EXERCISES_LEFT_KEY")
const val MEDITATION_TOTAL_EXERCISES = 10L

val OBTAIN_DRAGON_CATUS_ID_KEY = longPreferencesKey("OBTAIN_DRAGON_CATUS_ID_KEY")
val OBTAIN_DRAGON_DOGUS_ID_KEY = longPreferencesKey("OBTAIN_DRAGON_DOGUS_ID_KEY")
val OBTAIN_DRAGON_FROGUS_ID_KEY = longPreferencesKey("OBTAIN_DRAGON_FROGUS_ID_KEY")
val OBTAIN_DRAGON_BOBER_ID_KEY = longPreferencesKey("OBTAIN_DRAGON_BOBER_ID_KEY")
val OBTAIN_DRAGON_TIMESTAMP_KEY = longPreferencesKey("OBTAIN_DRAGON_TIMESTAMP_KEY")
val OBTAIN_DRAGON_CAT_ASKED_KEY = booleanPreferencesKey("OBTAIN_DRAGON_CAT_ASKED_KEY")
val OBTAIN_DRAGON_DOG_ASKED_KEY = booleanPreferencesKey("OBTAIN_DRAGON_DOG_ASKED_KEY")
val OBTAIN_DRAGON_FROG_ASKED_KEY = booleanPreferencesKey("OBTAIN_DRAGON_FROG_ASKED_KEY")
val OBTAIN_DRAGON_BOBER_ASKED_KEY = booleanPreferencesKey("OBTAIN_DRAGON_BOBER_ASKED_KEY")
const val OBTAIN_DRAGON_CAT_CHOICE = "cat"
const val OBTAIN_DRAGON_DOG_CHOICE = "dog"
const val OBTAIN_DRAGON_FROG_CHOICE = "frog"
const val OBTAIN_DRAGON_BOBER_CHOICE = "bober"
val OBTAIN_DRAGON_MISTY_GORGE_DECISION_KEY = stringPreferencesKey("OBTAIN_DRAGON_MISTY_GORGE_DECISION_KEY")
val OBTAIN_DRAGON_FOREST_DECISION_KEY = stringPreferencesKey("OBTAIN_DRAGON_FOREST_DECISION_KEY")
val OBTAIN_DRAGON_STONE_DECISION_KEY = stringPreferencesKey("OBTAIN_DRAGON_STONE_DECISION_KEY")
val OBTAIN_DRAGON_ASH_DECISION_KEY = stringPreferencesKey("OBTAIN_DRAGON_ASH_DECISION_KEY")
val OBTAIN_DRAGON_HAS_NECRONOMICON_KEY = booleanPreferencesKey("OBTAIN_DRAGON_HAS_NECRONOMICON_KEY")
val OBTAIN_DRAGON_CAT_IS_SACRIFICE_KEY = booleanPreferencesKey("OBTAIN_DRAGON_CAT_IS_SACRIFICE_KEY")

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
    QUEST_MEDITATION to Quest(
        currentStageKey = intPreferencesKey("meditation_stage_key"),
        stages = listOf(
            // Stage 0 - make sure user understands dog and frog 100%
            QuestStage(
                conditionsKey = stringSetPreferencesKey("meditation_stage_0_conditions"),
                initialConditions = setOf(MEDITATION_STAGE_0_LAMBDA),
                onFinish = {},
                additionalAnswerOptions = null,
            ),
            // Stage 1 - dialog stage - ask adult dogus how he can stay always positive and relaxed
            QuestStage(
                conditionsKey = stringSetPreferencesKey("meditation_stage_1_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Dogus &&
                        pet.ageState == AgeState.Adult &&
                        pet.bodyState == BodyState.Alive)
                    {
                        listOf(
                            Answer(
                                text = StringId.MeditationStage1Answer0,
                                nextNode = DialogSystem.MEDITATION_STAGE_1_DIALOG_0,
                            )
                        )
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 2 - dialog stage - ask freshly born (not later then 1 day) frogus to start meditating
            QuestStage(
                conditionsKey = stringSetPreferencesKey("meditation_stage_2_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Frogus &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val now = getTimestampSecondsSinceEpoch()
                        if (pet.ageState == AgeState.NewBorn &&
                            now - pet.creationTime < DAY)
                        {
                            listOf(
                                Answer(
                                    text = StringId.MeditationStage2Answer0,
                                    nextNode = DialogSystem.MEDITATION_STAGE_2_DIALOG_0,
                                )
                            )
                        } else {
                            listOf(
                                Answer(
                                    text = StringId.MeditationStage2Answer0,
                                    nextNode = DialogSystem.MEDITATION_STAGE_2_DIALOG_1,
                                )
                            )
                        }
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 3 - dialog stage - continue meditation with the frogus
            QuestStage(
                conditionsKey = stringSetPreferencesKey("meditation_stage_3_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    val wiseFrogId = questSystem.dataStore.data.first()[MEDITATION_FROG_ID_KEY] ?: -1L
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.id == wiseFrogId &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val now = getTimestampSecondsSinceEpoch()
                        val timeOfLastMeditation = questSystem.dataStore.data.first()[MEDITATION_TIMESTAMP_KEY] ?: -1L
                        val timePassed = now - timeOfLastMeditation
                        when {
                            timePassed < DAY -> listOf(
                                Answer(
                                    text = StringId.MeditationStage3Answer0,
                                    nextNode = DialogSystem.MEDITATION_STAGE_3_DIALOG_0,
                                )
                            )
                            timePassed >= DAY && timePassed < DAY * 2 -> {
                                val exercisesLeft = questSystem.dataStore.data.first()[MEDITATION_EXERCISES_LEFT_KEY] ?: MEDITATION_TOTAL_EXERCISES
                                // If there is no more exercises left then finis training
                                if (exercisesLeft == 0L) {
                                    listOf(
                                        Answer(
                                            text = StringId.MeditationStage3Answer0,
                                            nextNode = DialogSystem.MEDITATION_STAGE_3_DIALOG_4,
                                        )
                                    )
                                } else {
                                    // Continue training
                                    listOf(
                                        Answer(
                                            text = StringId.MeditationStage3Answer0,
                                            nextNode = DialogSystem.MEDITATION_STAGE_3_DIALOG_1,
                                        )
                                    )
                                }
                            }
                            else -> listOf(
                                Answer(
                                    text = StringId.MeditationStage3Answer0,
                                    nextNode = DialogSystem.MEDITATION_STAGE_3_DIALOG_2,
                                )
                            )
                        }
                    } else {
                        emptyList()
                    }
                },
            ),
        )
    ),
    QUEST_TO_OBTAIN_DRAGON to Quest(
        currentStageKey = intPreferencesKey("obtain_dragon_stage_key"),
        stages = listOf(
            // Stage 0 - make sure user understands cat, dog, frog and bober 100%
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_0_conditions"),
                initialConditions = setOf(OBTAIN_DRAGON_STAGE_0_LAMBDA),
                onFinish = {},
                additionalAnswerOptions = null,
            ),
            // Stage 1 - dialog stage - ask catus about rumors of some disaster in the kingdom
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_1_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Catus &&
                        pet.bodyState == BodyState.Alive)
                    {
                        listOf(
                            Answer(
                                text = StringId.ObtainDragonStage1Answer0,
                                nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_1_DIALOG_0,
                            )
                        )
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 2 - dialog stage - ask dogus, frogus and bober to join you
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_2_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        val dogId = store[OBTAIN_DRAGON_DOGUS_ID_KEY]
                        val frogId = store[OBTAIN_DRAGON_FROGUS_ID_KEY]
                        val boberId = store[OBTAIN_DRAGON_BOBER_ID_KEY]
                        when (pet.type) {
                            PetType.Catus -> {
                                if (pet.id == catId &&
                                    dogId != null &&
                                    frogId != null &&
                                    boberId != null) {
                                    listOf(
                                        Answer(
                                            text = StringId.ObtainDragonStage2Answer4,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_2_DIALOG_3,
                                        )
                                    )
                                } else emptyList()
                            }
                            PetType.Dogus -> {
                                if (dogId == null) {
                                    listOf(
                                        Answer(
                                            text = StringId.ObtainDragonStage2Answer0,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_2_DIALOG_0,
                                        )
                                    )
                                } else emptyList()
                            }
                            PetType.Frogus -> {
                                if (frogId == null) {
                                    listOf(
                                        Answer(
                                            text = StringId.ObtainDragonStage2Answer0,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_2_DIALOG_0,
                                        )
                                    )
                                } else emptyList()
                            }
                            PetType.Bober -> {
                                if (boberId == null) {
                                    listOf(
                                        Answer(
                                            text = StringId.ObtainDragonStage2Answer0,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_2_DIALOG_2,
                                        )
                                    )
                                } else emptyList()
                            }
                            else -> emptyList()
                        }
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 3 - wait 1 day to get to Misty Gorge
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_3_conditions"),
                initialConditions = setOf(OBTAIN_DRAGON_STAGE_3_LAMBDA),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Catus &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        if (catId == pet.id) {
                            listOf(
                                Answer(
                                    text = StringId.ObtainDragonStage3Answer0,
                                    nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_3_DIALOG_0,
                                )
                            )
                        } else emptyList()
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 4 - dialog stage - adventure in Misty Gorge starts
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_4_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Catus &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        if (catId == pet.id) {
                            listOf(
                                Answer(
                                    text = StringId.ObtainDragonStage4Answer0,
                                    nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_4_DIALOG_0,
                                )
                            )
                        } else emptyList()
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 5 - dialog stage - ask team about their solutions for Misty Gorge
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_5_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        val dogId = store[OBTAIN_DRAGON_DOGUS_ID_KEY]
                        val frogId = store[OBTAIN_DRAGON_FROGUS_ID_KEY]
                        val boberId = store[OBTAIN_DRAGON_BOBER_ID_KEY]
                        val catAsked = store[OBTAIN_DRAGON_CAT_ASKED_KEY] ?: false
                        val dogAsked = store[OBTAIN_DRAGON_DOG_ASKED_KEY] ?: false
                        val frogAsked = store[OBTAIN_DRAGON_FROG_ASKED_KEY] ?: false
                        val boberAsked = store[OBTAIN_DRAGON_BOBER_ASKED_KEY] ?: false
                        when (pet.id) {
                            catId -> {
                                // Everyone being asked, user ready to make decision
                                if (catAsked && dogAsked && frogAsked && boberAsked) {
                                    listOf(
                                        Answer(
                                            text = StringId.ObtainDragonStage5Answer1,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_5_DIALOG_4,
                                        ),
                                        Answer(
                                            text = StringId.ObtainDragonStage5Answer0,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_5_DIALOG_0,
                                        )
                                    )
                                } else {
                                    listOf(
                                        Answer(
                                            text = StringId.ObtainDragonStage5Answer0,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_5_DIALOG_0,
                                        )
                                    )
                                }
                            }
                            dogId -> {
                                listOf(
                                    Answer(
                                        text = StringId.ObtainDragonStage5Answer0,
                                        nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_5_DIALOG_1,
                                    )
                                )
                            }
                            frogId -> {
                                listOf(
                                    Answer(
                                        text = StringId.ObtainDragonStage5Answer0,
                                        nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_5_DIALOG_2,
                                    )
                                )
                            }
                            boberId -> {
                                listOf(
                                    Answer(
                                        text = StringId.ObtainDragonStage5Answer0,
                                        nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_5_DIALOG_3,
                                    )
                                )
                            }
                            else -> emptyList()
                        }
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 6 - wait 1 hour to get through Misty Gorge
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_6_conditions"),
                initialConditions = setOf(OBTAIN_DRAGON_STAGE_6_LAMBDA),
                onFinish = {},
                additionalAnswerOptions = null
            ),
            // Stage 7 - dialog stage - catus reports results of Misty Gorge adventure
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_7_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Catus &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        if (catId == pet.id) {
                            val nextNode = when (val choice = store[OBTAIN_DRAGON_MISTY_GORGE_DECISION_KEY]) {
                                // Cat choice is correct for Misty Gorge
                                OBTAIN_DRAGON_CAT_CHOICE -> DialogSystem.OBTAIN_DRAGON_STAGE_7_DIALOG_0
                                // All other choices lead to Catus being cursed
                                OBTAIN_DRAGON_DOG_CHOICE -> DialogSystem.OBTAIN_DRAGON_STAGE_7_DIALOG_1
                                OBTAIN_DRAGON_FROG_CHOICE -> DialogSystem.OBTAIN_DRAGON_STAGE_7_DIALOG_2
                                OBTAIN_DRAGON_BOBER_CHOICE -> DialogSystem.OBTAIN_DRAGON_STAGE_7_DIALOG_3
                                else -> error("OBTAIN_DRAGON_STAGE_7 wrong choice: $choice")
                            }
                            listOf(
                                Answer(
                                    text = StringId.ObtainDragonStage7Answer0,
                                    nextNode = nextNode,
                                )
                            )
                        } else emptyList()
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 8 - wait 1 day to get to Forest of Singing Winds
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_8_conditions"),
                initialConditions = setOf(OBTAIN_DRAGON_STAGE_3_LAMBDA), // Same as in stage 3
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Catus &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        if (catId == pet.id) {
                            listOf(
                                Answer(
                                    text = StringId.ObtainDragonStage8Answer0,
                                    nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_3_DIALOG_0, // Same as in stage 3
                                )
                            )
                        } else emptyList()
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 9 - dialog stage - adventure in Forest of Singing Winds
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_9_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Catus &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        if (catId == pet.id) {
                            listOf(
                                Answer(
                                    text = StringId.ObtainDragonStage9Answer0,
                                    nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_9_DIALOG_0,
                                )
                            )
                        } else emptyList()
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 10 - dialog stage - ask team about their solutions for Forest of Singing Winds
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_10_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        val dogId = store[OBTAIN_DRAGON_DOGUS_ID_KEY]
                        val frogId = store[OBTAIN_DRAGON_FROGUS_ID_KEY]
                        val boberId = store[OBTAIN_DRAGON_BOBER_ID_KEY]
                        val catAsked = store[OBTAIN_DRAGON_CAT_ASKED_KEY] ?: false
                        val dogAsked = store[OBTAIN_DRAGON_DOG_ASKED_KEY] ?: false
                        val frogAsked = store[OBTAIN_DRAGON_FROG_ASKED_KEY] ?: false
                        val boberAsked = store[OBTAIN_DRAGON_BOBER_ASKED_KEY] ?: false
                        when (pet.id) {
                            catId -> {
                                // Everyone being asked, user ready to make decision
                                if (catAsked && dogAsked && frogAsked && boberAsked) {
                                    listOf(
                                        Answer(
                                            text = StringId.ObtainDragonStage10Answer1,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_10_DIALOG_4,
                                        ),
                                        Answer(
                                            text = StringId.ObtainDragonStage10Answer0,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_10_DIALOG_0,
                                        )
                                    )
                                } else {
                                    listOf(
                                        Answer(
                                            text = StringId.ObtainDragonStage10Answer0,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_10_DIALOG_0,
                                        )
                                    )
                                }
                            }
                            dogId -> {
                                listOf(
                                    Answer(
                                        text = StringId.ObtainDragonStage10Answer0,
                                        nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_10_DIALOG_1,
                                    )
                                )
                            }
                            frogId -> {
                                listOf(
                                    Answer(
                                        text = StringId.ObtainDragonStage10Answer0,
                                        nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_10_DIALOG_2,
                                    )
                                )
                            }
                            boberId -> {
                                listOf(
                                    Answer(
                                        text = StringId.ObtainDragonStage10Answer0,
                                        nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_10_DIALOG_3,
                                    )
                                )
                            }
                            else -> emptyList()
                        }
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 11 - wait 1 hour to get through Forest of Singing Winds
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_11_conditions"),
                initialConditions = setOf(OBTAIN_DRAGON_STAGE_6_LAMBDA), // ok to reuse it here
                onFinish = {},
                additionalAnswerOptions = null
            ),
            // Stage 12 - dialog stage - catus reports results of Forest of Singing Winds adventure
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_12_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Catus &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        if (catId == pet.id) {
                            val nextNode = when (val choice = store[OBTAIN_DRAGON_FOREST_DECISION_KEY]) {
                                // Frog choice is correct for Forest of Singing Winds
                                OBTAIN_DRAGON_FROG_CHOICE -> DialogSystem.OBTAIN_DRAGON_STAGE_12_DIALOG_0
                                // All other choices lead to Frog being cursed
                                OBTAIN_DRAGON_CAT_CHOICE -> DialogSystem.OBTAIN_DRAGON_STAGE_12_DIALOG_1
                                OBTAIN_DRAGON_DOG_CHOICE -> DialogSystem.OBTAIN_DRAGON_STAGE_12_DIALOG_2
                                OBTAIN_DRAGON_BOBER_CHOICE -> DialogSystem.OBTAIN_DRAGON_STAGE_12_DIALOG_3
                                else -> error("OBTAIN_DRAGON_STAGE_12 wrong choice: $choice")
                            }
                            listOf(
                                Answer(
                                    text = StringId.ObtainDragonStage12Answer0,
                                    nextNode = nextNode,
                                )
                            )
                        } else emptyList()
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 13 - wait 1 day to get to Stone Pass
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_13_conditions"),
                initialConditions = setOf(OBTAIN_DRAGON_STAGE_3_LAMBDA), // Same as in stage 3
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Catus &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        if (catId == pet.id) {
                            listOf(
                                Answer(
                                    text = StringId.ObtainDragonStage13Answer0,
                                    nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_3_DIALOG_0, // Same as in stage 3
                                )
                            )
                        } else emptyList()
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 14 - dialog stage - adventure in Stone Pass
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_14_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Catus &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        if (catId == pet.id) {
                            listOf(
                                Answer(
                                    text = StringId.ObtainDragonStage14Answer0,
                                    nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_14_DIALOG_0,
                                )
                            )
                        } else emptyList()
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 15 - dialog stage - ask team about their solutions for Stone Pass
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_15_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        val dogId = store[OBTAIN_DRAGON_DOGUS_ID_KEY]
                        val frogId = store[OBTAIN_DRAGON_FROGUS_ID_KEY]
                        val boberId = store[OBTAIN_DRAGON_BOBER_ID_KEY]
                        val catAsked = store[OBTAIN_DRAGON_CAT_ASKED_KEY] ?: false
                        val dogAsked = store[OBTAIN_DRAGON_DOG_ASKED_KEY] ?: false
                        val frogAsked = store[OBTAIN_DRAGON_FROG_ASKED_KEY] ?: false
                        val boberAsked = store[OBTAIN_DRAGON_BOBER_ASKED_KEY] ?: false
                        when (pet.id) {
                            catId -> {
                                // Everyone being asked, user ready to make decision
                                if (catAsked && dogAsked && frogAsked && boberAsked) {
                                    listOf(
                                        Answer(
                                            text = StringId.ObtainDragonStage15Answer1,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_15_DIALOG_4,
                                        ),
                                        Answer(
                                            text = StringId.ObtainDragonStage15Answer0,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_15_DIALOG_0,
                                        )
                                    )
                                } else {
                                    listOf(
                                        Answer(
                                            text = StringId.ObtainDragonStage15Answer0,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_15_DIALOG_0,
                                        )
                                    )
                                }
                            }
                            dogId -> {
                                listOf(
                                    Answer(
                                        text = StringId.ObtainDragonStage15Answer0,
                                        nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_15_DIALOG_1,
                                    )
                                )
                            }
                            frogId -> {
                                listOf(
                                    Answer(
                                        text = StringId.ObtainDragonStage15Answer0,
                                        nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_15_DIALOG_2,
                                    )
                                )
                            }
                            boberId -> {
                                listOf(
                                    Answer(
                                        text = StringId.ObtainDragonStage15Answer0,
                                        nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_15_DIALOG_3,
                                    )
                                )
                            }
                            else -> emptyList()
                        }
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 16 - wait 1 hour to get through Stone Pass
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_16_conditions"),
                initialConditions = setOf(OBTAIN_DRAGON_STAGE_6_LAMBDA), // ok to reuse it here
                onFinish = {},
                additionalAnswerOptions = null
            ),
            // Stage 17 - dialog stage - catus reports results of Stone Pass adventure
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_17_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Catus &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        if (catId == pet.id) {
                            val nextNode = when (val choice = store[OBTAIN_DRAGON_STONE_DECISION_KEY]) {
                                // Dog choice is correct for Stone Pass
                                OBTAIN_DRAGON_DOG_CHOICE -> DialogSystem.OBTAIN_DRAGON_STAGE_17_DIALOG_0
                                // All other choices lead to Dog being cursed
                                OBTAIN_DRAGON_CAT_CHOICE -> DialogSystem.OBTAIN_DRAGON_STAGE_17_DIALOG_1
                                OBTAIN_DRAGON_FROG_CHOICE -> DialogSystem.OBTAIN_DRAGON_STAGE_17_DIALOG_2
                                OBTAIN_DRAGON_BOBER_CHOICE -> DialogSystem.OBTAIN_DRAGON_STAGE_17_DIALOG_3
                                else -> error("OBTAIN_DRAGON_STAGE_17 wrong choice: $choice")
                            }
                            listOf(
                                Answer(
                                    text = StringId.ObtainDragonStage17Answer0,
                                    nextNode = nextNode,
                                )
                            )
                        } else emptyList()
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 18 - wait 1 day to get to Ash Caves
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_18_conditions"),
                initialConditions = setOf(OBTAIN_DRAGON_STAGE_3_LAMBDA), // Same as in stage 3
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Catus &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        if (catId == pet.id) {
                            listOf(
                                Answer(
                                    text = StringId.ObtainDragonStage18Answer0,
                                    nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_3_DIALOG_0, // Same as in stage 3
                                )
                            )
                        } else emptyList()
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 19 - dialog stage - adventure in Ash Caves
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_19_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Catus &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        if (catId == pet.id) {
                            listOf(
                                Answer(
                                    text = StringId.ObtainDragonStage19Answer0,
                                    nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_19_DIALOG_0,
                                )
                            )
                        } else emptyList()
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 20 - dialog stage - ask team about their solutions for Ash Caves
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_20_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        val dogId = store[OBTAIN_DRAGON_DOGUS_ID_KEY]
                        val frogId = store[OBTAIN_DRAGON_FROGUS_ID_KEY]
                        val boberId = store[OBTAIN_DRAGON_BOBER_ID_KEY]
                        val catAsked = store[OBTAIN_DRAGON_CAT_ASKED_KEY] ?: false
                        val dogAsked = store[OBTAIN_DRAGON_DOG_ASKED_KEY] ?: false
                        val frogAsked = store[OBTAIN_DRAGON_FROG_ASKED_KEY] ?: false
                        val boberAsked = store[OBTAIN_DRAGON_BOBER_ASKED_KEY] ?: false
                        when (pet.id) {
                            catId -> {
                                // Everyone being asked, user ready to make decision
                                if (catAsked && dogAsked && frogAsked && boberAsked) {
                                    listOf(
                                        Answer(
                                            text = StringId.ObtainDragonStage20Answer1,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_20_DIALOG_4,
                                        ),
                                        Answer(
                                            text = StringId.ObtainDragonStage20Answer0,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_20_DIALOG_0,
                                        )
                                    )
                                } else {
                                    listOf(
                                        Answer(
                                            text = StringId.ObtainDragonStage20Answer0,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_20_DIALOG_0,
                                        )
                                    )
                                }
                            }
                            dogId -> {
                                listOf(
                                    Answer(
                                        text = StringId.ObtainDragonStage20Answer0,
                                        nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_20_DIALOG_1,
                                    )
                                )
                            }
                            frogId -> {
                                listOf(
                                    Answer(
                                        text = StringId.ObtainDragonStage20Answer0,
                                        nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_20_DIALOG_2,
                                    )
                                )
                            }
                            boberId -> {
                                listOf(
                                    Answer(
                                        text = StringId.ObtainDragonStage20Answer0,
                                        nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_20_DIALOG_3,
                                    )
                                )
                            }
                            else -> emptyList()
                        }
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 21 - wait 1 hour to get through Ash Caves
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_21_conditions"),
                initialConditions = setOf(OBTAIN_DRAGON_STAGE_6_LAMBDA), // ok to reuse it here
                onFinish = {},
                additionalAnswerOptions = null
            ),
            // Stage 22 - dialog stage - catus reports results of Ash Caves adventure
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_22_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Catus &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        if (catId == pet.id) {
                            val nextNode = when (val choice = store[OBTAIN_DRAGON_ASH_DECISION_KEY]) {
                                // Bober choice is correct for Stone Pass
                                OBTAIN_DRAGON_BOBER_CHOICE -> DialogSystem.OBTAIN_DRAGON_STAGE_22_DIALOG_0
                                // All other choices lead to Bober being cursed
                                OBTAIN_DRAGON_CAT_CHOICE -> DialogSystem.OBTAIN_DRAGON_STAGE_22_DIALOG_1
                                OBTAIN_DRAGON_DOG_CHOICE -> DialogSystem.OBTAIN_DRAGON_STAGE_22_DIALOG_2
                                OBTAIN_DRAGON_FROG_CHOICE -> DialogSystem.OBTAIN_DRAGON_STAGE_22_DIALOG_3
                                else -> error("OBTAIN_DRAGON_STAGE_22 wrong choice: $choice")
                            }
                            listOf(
                                Answer(
                                    text = StringId.ObtainDragonStage22Answer0,
                                    nextNode = nextNode,
                                )
                            )
                        } else emptyList()
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 23 - wait 1 day to get to Ritual Site
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_23_conditions"),
                initialConditions = setOf(OBTAIN_DRAGON_STAGE_3_LAMBDA), // Same as in stage 3
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Catus &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        if (catId == pet.id) {
                            listOf(
                                Answer(
                                    text = StringId.ObtainDragonStage23Answer0,
                                    nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_3_DIALOG_0, // Same as in stage 3
                                )
                            )
                        } else emptyList()
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 24 - dialog stage - final battle
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_24_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.type == PetType.Catus &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]

                        if (catId == pet.id) {
                            val hasNecronomicon = store.getInventory()
                                .firstOrNull { it.id == InventoryItemId.Necronomicon } != null
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_HAS_NECRONOMICON_KEY] = hasNecronomicon
                            }
                            if (hasNecronomicon) {
                                listOf(
                                    Answer(
                                        text = StringId.ObtainDragonStage24Answer0,
                                        nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_24_DIALOG_4,
                                    )
                                )
                            } else {
                                listOf(
                                    Answer(
                                        text = StringId.ObtainDragonStage24Answer0,
                                        nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_24_DIALOG_0,
                                    )
                                )
                            }
                        } else emptyList()
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 25 - dialog stage - final choice
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_25_conditions"),
                initialConditions = setOf(ALWAYS_FALSE_CONDITION),
                onFinish = {},
                additionalAnswerOptions = { questSystem, pet, nodeKey ->
                    if (nodeKey == DialogSystem.STANDARD_DIALOG_BEGINNING &&
                        pet.bodyState == BodyState.Alive)
                    {
                        val store = questSystem.dataStore.data.first()
                        val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                        val dogId = store[OBTAIN_DRAGON_DOGUS_ID_KEY]
                        val frogId = store[OBTAIN_DRAGON_FROGUS_ID_KEY]
                        val boberId = store[OBTAIN_DRAGON_BOBER_ID_KEY]
                        val hasNecronomicon = store[OBTAIN_DRAGON_HAS_NECRONOMICON_KEY] ?: false

                        if (hasNecronomicon) {
                            // Choices are presented by catus
                            val isCatSacrifice = store[OBTAIN_DRAGON_CAT_IS_SACRIFICE_KEY] ?: false
                            if (isCatSacrifice.not()) {
                                // Cat wasn't chosen as sacrifice and he presents all the options
                                if (catId == pet.id) {
                                    listOf(
                                        Answer(
                                            text = StringId.ObtainDragonStage25Answer1,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_25_DIALOG_0,
                                        )
                                    )
                                } else {
                                    emptyList()
                                }
                            } else {
                                // Cat was chosen as sacrifice when presenting all the options
                                // and he asked player to speak with dogus so dogus can
                                // describe what happened with catus.
                                if (dogId == pet.id) {
                                    listOf(
                                        Answer(
                                            text = StringId.ObtainDragonStage25Answer9,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_25_DIALOG_3,
                                        )
                                    )
                                } else {
                                    emptyList()
                                }
                            }
                        } else {
                            // Choices are presented by each hero because
                            // they will be saved and they will tell the other's fate
                            when (pet.id) {
                                catId -> {
                                    listOf(
                                        Answer(
                                            text = StringId.ObtainDragonStage25Answer0,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_25_DIALOG_12,
                                        )
                                    )
                                }
                                dogId -> {
                                    listOf(
                                        Answer(
                                            text = StringId.ObtainDragonStage25Answer0,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_25_DIALOG_17,
                                        )
                                    )
                                }
                                frogId -> {
                                    listOf(
                                        Answer(
                                            text = StringId.ObtainDragonStage25Answer0,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_25_DIALOG_22,
                                        )
                                    )
                                }
                                boberId -> {
                                    listOf(
                                        Answer(
                                            text = StringId.ObtainDragonStage25Answer0,
                                            nextNode = DialogSystem.OBTAIN_DRAGON_STAGE_25_DIALOG_27,
                                        )
                                    )
                                }
                                else -> emptyList()
                            }
                        }
                    } else {
                        emptyList()
                    }
                },
            ),
            // Stage 26 - wait 1 day to get home
            QuestStage(
                conditionsKey = stringSetPreferencesKey("obtain_dragon_stage_26_conditions"),
                initialConditions = setOf(OBTAIN_DRAGON_STAGE_3_LAMBDA), // Same as in stage 3
                onFinish = { questSystem ->
                    val store = questSystem.dataStore.data.first()

                    val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                    val dogId = store[OBTAIN_DRAGON_DOGUS_ID_KEY]
                    val frogId = store[OBTAIN_DRAGON_FROGUS_ID_KEY]
                    val boberId = store[OBTAIN_DRAGON_BOBER_ID_KEY]

                    // Catus
                    catId?.let { id ->
                        questSystem.petsRepo.getPetByIdFlow(id).first()?.let { cat ->
                            // Remove out of quest
                            var newCat = cat.copy(
                                questName = null,
                            )
                            // If he is still alive (not in memory) he still may be cursed
                            if (newCat.place != Place.Memory) {
                                val mistyGorgeChoice = store[OBTAIN_DRAGON_MISTY_GORGE_DECISION_KEY]
                                if (mistyGorgeChoice != OBTAIN_DRAGON_CAT_CHOICE) {
                                    newCat = newCat.copy(
                                        place = Place.Memory,
                                    )
                                    // Add curse of a mage to inventory
                                    questSystem.userStats.addInventoryItem(
                                        InventoryItem(
                                            id = InventoryItemId.CurseOfMage,
                                            amount = 1,
                                        )
                                    )
                                }
                            }
                            questSystem.petsRepo.updatePet(newCat)
                        }
                    }

                    // Dogus
                    dogId?.let { id ->
                        questSystem.petsRepo.getPetByIdFlow(id).first()?.let { dog ->
                            // Remove out of quest
                            var newDog = dog.copy(
                                questName = null,
                            )
                            // If he is still alive (not in memory) he still may be cursed
                            if (newDog.place != Place.Memory) {
                                val stoneChoice = store[OBTAIN_DRAGON_STONE_DECISION_KEY]
                                if (stoneChoice != OBTAIN_DRAGON_DOG_CHOICE) {
                                    newDog = newDog.copy(
                                        place = Place.Memory,
                                    )
                                    // Add curse of a warrior to inventory
                                    questSystem.userStats.addInventoryItem(
                                        InventoryItem(
                                            id = InventoryItemId.CurseOfWarrior,
                                            amount = 1,
                                        )
                                    )
                                }
                            }
                            questSystem.petsRepo.updatePet(newDog)
                        }
                    }

                    // Frogus
                    frogId?.let { id ->
                        questSystem.petsRepo.getPetByIdFlow(id).first()?.let { frog ->
                            // Remove out of quest
                            var newFrog = frog.copy(
                                questName = null,
                            )
                            // If he is still alive (not in memory) he still may be cursed
                            if (newFrog.place != Place.Memory) {
                                val forestChoice = store[OBTAIN_DRAGON_FOREST_DECISION_KEY]
                                if (forestChoice != OBTAIN_DRAGON_FROG_CHOICE) {
                                    newFrog = newFrog.copy(
                                        place = Place.Memory,
                                    )
                                    // Add curse of a bard to inventory
                                    questSystem.userStats.addInventoryItem(
                                        InventoryItem(
                                            id = InventoryItemId.CurseOfBard,
                                            amount = 1,
                                        )
                                    )
                                }
                            }
                            questSystem.petsRepo.updatePet(newFrog)
                        }
                    }

                    // Bober
                    boberId?.let { id ->
                        questSystem.petsRepo.getPetByIdFlow(id).first()?.let { bober ->
                            // Remove out of quest
                            var newBober = bober.copy(
                                questName = null,
                            )
                            // If he is still alive (not in memory) he still may be cursed
                            if (newBober.place != Place.Memory) {
                                val ashChoice = store[OBTAIN_DRAGON_ASH_DECISION_KEY]
                                if (ashChoice != OBTAIN_DRAGON_BOBER_CHOICE) {
                                    newBober = newBober.copy(
                                        place = Place.Memory,
                                    )
                                    // Add curse of a Smith to inventory
                                    questSystem.userStats.addInventoryItem(
                                        InventoryItem(
                                            id = InventoryItemId.CurseOfSmith,
                                            amount = 1,
                                        )
                                    )
                                }
                            }
                            questSystem.petsRepo.updatePet(newBober)
                        }
                    }

                    // Add dragon egg to inventory
                    questSystem.userStats.addInventoryItem(
                        InventoryItem(
                            id = InventoryItemId.DragonEgg,
                            amount = 1,
                        )
                    )
                    // Make dragon pet type available
                    questSystem.userStats.addNewAvailablePetType(PetType.Dragon)
                },
                additionalAnswerOptions = null,
            ),
        )
    ),
)