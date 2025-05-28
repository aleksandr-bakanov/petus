package bav.petus.core.dialog

import androidx.datastore.preferences.core.edit
import bav.petus.core.engine.Ability
import bav.petus.core.engine.Engine
import bav.petus.core.engine.NECRONOMICON_EXHUMATED_PET_ID_KEY
import bav.petus.core.engine.NECRONOMICON_SEARCH_DOG_ID_KEY
import bav.petus.core.engine.NECRONOMICON_TIMESTAMP_KEY
import bav.petus.core.engine.NECRONOMICON_WISE_CAT_ID_KEY
import bav.petus.core.engine.OBTAIN_FROGUS_ASKING_CAT_ID_KEY
import bav.petus.core.engine.OBTAIN_FROGUS_TIMESTAMP_KEY
import bav.petus.core.engine.QuestSystem
import bav.petus.core.engine.UserStats
import bav.petus.core.inventory.InventoryItem
import bav.petus.core.inventory.InventoryItemId
import bav.petus.core.resources.StringId
import bav.petus.core.time.getTimestampSecondsSinceEpoch
import bav.petus.model.BodyState
import bav.petus.model.BurialType
import bav.petus.model.Pet
import bav.petus.model.PetType
import kotlinx.coroutines.flow.first

class DialogSystem(
    private val userStats: UserStats,
    private val questSystem: QuestSystem,
    private val engine: Engine,
) {

    private var currentNode: DialogNode? = null
    private var currentPet: Pet? = null

    suspend fun startDialog(pet: Pet): DialogNode? {
        currentPet = pet
        val standardBeginning = nodes[STANDARD_DIALOG_BEGINNING]!!
        val additionalAnswerOptions = questSystem.getAdditionalAnswers(pet, STANDARD_DIALOG_BEGINNING)
        val nodeWithAdditionalQuestAnswers = if (additionalAnswerOptions.isNotEmpty()) {
            standardBeginning.copy(
                answers = additionalAnswerOptions + standardBeginning.answers
            )
        } else {
            standardBeginning
        }
        val resultNode = if (engine.isPetSpeakLatin(pet)) {
            nodeWithAdditionalQuestAnswers.copy(
                text = listOf(StringId.WhatIsGoingOnWithYouLatin)
            )
        } else {
            nodeWithAdditionalQuestAnswers
        }
        currentNode = resultNode
        return currentNode
    }

    /**
     * @return If null, finish dialog
     */
    suspend fun chooseAnswer(index: Int): DialogNode? {
        return currentNode?.let { node ->
            if (index > node.answers.lastIndex) {
                null
            } else {
                val answer = node.answers[index]
                answer.action?.invoke(questSystem, userStats, currentPet)

                currentNode = answer.nextNode?.let { nextId ->
                    nodes[nextId]
                }

                if (answer.nextNode == PET_DESCRIPTION) {
                    currentNode = addPetDescription(currentPet, currentNode)
                }

                currentNode
            }
        }
    }

    private fun addPetDescription(pet: Pet?, node: DialogNode?): DialogNode? {
        if (pet == null || node == null) return node
        else {
            val isLatin = engine.isPetSpeakLatin(pet)
            val texts = buildList {
                val isSick = pet.illness
                val isHungry = engine.isPetHungry(pet)
                val isPooped = pet.isPooped
                val isBored = engine.isPetBored(pet)
                val isHalfHp = engine.isPetLowHealth(pet)
                val isGood = listOf(isSick, isHungry, isPooped, isBored, isHalfHp).none { it }

                if (isSick) add(if (isLatin) StringId.IAmSickLatin else StringId.IAmSick)
                if (isHungry) add(if (isLatin) StringId.IAmHungryLatin else StringId.IAmHungry)
                if (isPooped) add(if (isLatin) StringId.IPoopedLatin else StringId.IPooped)
                if (isBored) add(if (isLatin) StringId.IAmBoredLatin else StringId.IAmBored)
                if (isHalfHp) add(if (isLatin) StringId.IAmHalfHpLatin else StringId.IAmHalfHp)
                if (isGood) add(if (isLatin) StringId.IAmGoodLatin else StringId.IAmGood)
            }
            return node.copy(
                text = node.text + texts.shuffled()
            )
        }
    }

    suspend fun censorDialogText(petType: PetType, text: String): String {
        val maxKnowledge = UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE
        val knowledge = userStats.getLanguageKnowledge(petType).coerceIn(0, maxKnowledge)

        val ratio = knowledge.toFloat() / maxKnowledge.toFloat()
        val amountToCensor = text.length - (text.length * ratio).toInt()
        val indicesToCensor = text.indices.shuffled().take(amountToCensor)

        val maskSymbol = "\u258A"

        return buildString {
            for (i in text.indices) {
                append(if (i in indicesToCensor) maskSymbol else text[i])
            }
        }
    }

    companion object {
        const val STANDARD_DIALOG_BEGINNING = "STANDARD_DIALOG_BEGINNING"
        const val PET_DESCRIPTION = "PET_DESCRIPTION"
        const val BEING_BETTER = "BEING_BETTER"

        const val NECRONOMICON_STAGE_3_COMMON_DIALOG = "NECRONOMICON_STAGE_3_COMMON_DIALOG"
        const val NECRONOMICON_STAGE_3_DOG_DIALOG = "NECRONOMICON_STAGE_3_DOG_DIALOG"
        const val NECRONOMICON_STAGE_5_DOG_DIALOG = "NECRONOMICON_STAGE_5_DOG_DIALOG"
        const val NECRONOMICON_STAGE_6_DOG_DIALOG = "NECRONOMICON_STAGE_6_DOG_DIALOG"
        const val NECRONOMICON_STAGE_7_DOG_DIALOG_0 = "NECRONOMICON_STAGE_7_DOG_DIALOG_0"
        const val NECRONOMICON_STAGE_7_DOG_DIALOG_1 = "NECRONOMICON_STAGE_7_DOG_DIALOG_1"
        const val NECRONOMICON_STAGE_7_DOG_DIALOG_2 = "NECRONOMICON_STAGE_7_DOG_DIALOG_2"
        const val NECRONOMICON_STAGE_8_DIALOG_0 = "NECRONOMICON_STAGE_8_DIALOG_0"
        const val NECRONOMICON_STAGE_8_DIALOG_1 = "NECRONOMICON_STAGE_8_DIALOG_1"

        const val OBTAIN_FROGUS_STAGE_1_DIALOG_0 = "OBTAIN_FROGUS_STAGE_1_DIALOG_0"
        const val OBTAIN_FROGUS_STAGE_3_DIALOG_0 = "OBTAIN_FROGUS_STAGE_3_DIALOG_0"
        const val OBTAIN_FROGUS_STAGE_5_DIALOG_0 = "OBTAIN_FROGUS_STAGE_5_DIALOG_0"

        private val nodes: Map<String, DialogNode> = mapOf(
            STANDARD_DIALOG_BEGINNING to DialogNode(
                text = listOf(StringId.WhatIsGoingOnWithYou),
                answers = listOf(
                    Answer(
                        text = StringId.IAmOkayHowAreYou,
                        nextNode = PET_DESCRIPTION,
                    ),
                    Answer(
                        text = StringId.IShouldGo,
                        nextNode = null,
                    ),
                )
            ),
            PET_DESCRIPTION to DialogNode(
                text = emptyList(),
                answers = listOf(
                    Answer(
                        text = StringId.ByeBye,
                        nextNode = null,
                    )
                )
            ),
            BEING_BETTER to DialogNode(
                text = listOf(StringId.SeeYa),
                answers = listOf(
                    Answer(
                        text = StringId.ByeBye,
                        nextNode = null,
                    )
                )
            ),
            NECRONOMICON_STAGE_3_COMMON_DIALOG to DialogNode(
                text = listOf(StringId.NecronomiconStage3CommonDialog),
                answers = listOf(
                    Answer(
                        text = StringId.ByeBye,
                        nextNode = null,
                    )
                )
            ),
            NECRONOMICON_STAGE_3_DOG_DIALOG to DialogNode(
                text = listOf(StringId.NecronomiconStage3DogDialog),
                answers = listOf(
                    Answer(
                        text = StringId.NecronomiconStage3DogDialogAnswerOption0,
                        nextNode = null,
                        action = { questSystem, _, _, ->
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_NECRONOMICON)
                        }
                    )
                )
            ),
            NECRONOMICON_STAGE_5_DOG_DIALOG to DialogNode(
                text = listOf(StringId.NecronomiconStage5DogDialog),
                answers = listOf(
                    Answer(
                        text = StringId.Sure,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            userStats.removeInventoryItem(
                                InventoryItem(InventoryItemId.PieceOfCloth, 1)
                            )
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[NECRONOMICON_TIMESTAMP_KEY] = now
                                pet?.let { p -> store[NECRONOMICON_SEARCH_DOG_ID_KEY] = p.id }
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_NECRONOMICON)
                        }
                    )
                )
            ),
            NECRONOMICON_STAGE_6_DOG_DIALOG to DialogNode(
                text = listOf(StringId.NecronomiconStage6DogDialog),
                answers = listOf(
                    Answer(
                        text = StringId.Ok,
                        nextNode = null,
                    )
                )
            ),
            NECRONOMICON_STAGE_7_DOG_DIALOG_0 to DialogNode(
                text = listOf(StringId.NecronomiconStage7DogDialog0),
                answers = listOf(
                    Answer(
                        text = StringId.NecronomiconStage7AnswerOption0,
                        nextNode = NECRONOMICON_STAGE_7_DOG_DIALOG_1,
                        action = { _, userStats, _ ->
                            userStats.addInventoryItem(
                                InventoryItem(InventoryItemId.MysteriousBook, 1)
                            )
                        }
                    )
                )
            ),
            NECRONOMICON_STAGE_7_DOG_DIALOG_1 to DialogNode(
                text = listOf(StringId.NecronomiconStage7DogDialog1),
                answers = listOf(
                    Answer(
                        text = StringId.Thanks,
                        nextNode = NECRONOMICON_STAGE_7_DOG_DIALOG_2,
                    )
                )
            ),
            NECRONOMICON_STAGE_7_DOG_DIALOG_2 to DialogNode(
                text = listOf(StringId.NecronomiconStage7DogDialog2),
                answers = listOf(
                    Answer(
                        text = StringId.Sure,
                        nextNode = null,
                        action = { questSystem, _, _ ->
                            questSystem.dataStore.data.first()[NECRONOMICON_EXHUMATED_PET_ID_KEY]?.let { bodyPetId ->
                                questSystem.petsRepo.getPetByIdFlow(bodyPetId).first()?.let { pet ->
                                    questSystem.petsRepo.updatePet(
                                        pet = pet.copy(
                                            burialType = BurialType.Buried,
                                        )
                                    )
                                }
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_NECRONOMICON)
                        }
                    )
                )
            ),
            NECRONOMICON_STAGE_8_DIALOG_0 to DialogNode(
                text = listOf(StringId.NecronomiconStage8Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.Ok,
                        nextNode = null,
                    )
                )
            ),
            NECRONOMICON_STAGE_8_DIALOG_1 to DialogNode(
                text = listOf(StringId.NecronomiconStage8Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.Use,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            userStats.removeInventoryItem(
                                InventoryItem(InventoryItemId.MysteriousBook, 1)
                            )
                            userStats.addInventoryItem(
                                InventoryItem(InventoryItemId.Necronomicon, 1)
                            )
                            pet?.let { p ->
                                questSystem.dataStore.edit { store ->
                                    store[NECRONOMICON_WISE_CAT_ID_KEY] = p.id
                                }
                            }
                            userStats.addNewAbility(Ability.Necromancy)
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_NECRONOMICON)
                        }
                    ),
                    Answer(
                        text = StringId.Destroy,
                        nextNode = null,
                        action = { questSystem, userStats, _ ->
                            userStats.removeInventoryItem(
                                InventoryItem(InventoryItemId.MysteriousBook, 1)
                            )
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_NECRONOMICON)
                        }
                    )
                )
            ),
            OBTAIN_FROGUS_STAGE_1_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainFrogusStage1Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainFrogusStage1Answer1,
                        nextNode = null,
                        action = { questSystem, _, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_FROGUS_TIMESTAMP_KEY] = now
                                pet?.let { p -> store[OBTAIN_FROGUS_ASKING_CAT_ID_KEY] = p.id }
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_FROGUS)
                        }
                    ),
                )
            ),
            OBTAIN_FROGUS_STAGE_3_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainFrogusStage3Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainFrogusStage3Answer1,
                        nextNode = null,
                        action = { questSystem, _, _ ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_FROGUS_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_FROGUS)
                        }
                    ),
                )
            ),
            OBTAIN_FROGUS_STAGE_5_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainFrogusStage5Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainFrogusStage5Answer1,
                        nextNode = null,
                        action = { questSystem, userStats, _ ->
                            userStats.addNewAvailablePetType(PetType.Frogus)
                            userStats.addInventoryItem(
                                InventoryItem(InventoryItemId.FrogusEgg, 1)
                            )
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_FROGUS)
                        }
                    ),
                )
            ),
        )
    }
}