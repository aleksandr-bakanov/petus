package bav.petus.core.dialog

import androidx.datastore.preferences.core.edit
import bav.petus.core.engine.Ability
import bav.petus.core.engine.Engine
import bav.petus.core.engine.MEDITATION_EXERCISES_LEFT_KEY
import bav.petus.core.engine.MEDITATION_FROG_ID_KEY
import bav.petus.core.engine.MEDITATION_TIMESTAMP_KEY
import bav.petus.core.engine.MEDITATION_TOTAL_EXERCISES
import bav.petus.core.engine.NECRONOMICON_EXHUMATED_PET_ID_KEY
import bav.petus.core.engine.NECRONOMICON_SEARCH_DOG_ID_KEY
import bav.petus.core.engine.NECRONOMICON_TIMESTAMP_KEY
import bav.petus.core.engine.NECRONOMICON_WISE_CAT_ID_KEY
import bav.petus.core.engine.OBTAIN_BOBER_SEARCH_DOG_ID_KEY
import bav.petus.core.engine.OBTAIN_BOBER_SEARCH_FROG_ID_KEY
import bav.petus.core.engine.OBTAIN_BOBER_TIMESTAMP_KEY
import bav.petus.core.engine.OBTAIN_DRAGON_ASH_DECISION_KEY
import bav.petus.core.engine.OBTAIN_DRAGON_BOBER_ASKED_KEY
import bav.petus.core.engine.OBTAIN_DRAGON_BOBER_CHOICE
import bav.petus.core.engine.OBTAIN_DRAGON_BOBER_ID_KEY
import bav.petus.core.engine.OBTAIN_DRAGON_CATUS_ID_KEY
import bav.petus.core.engine.OBTAIN_DRAGON_CAT_ASKED_KEY
import bav.petus.core.engine.OBTAIN_DRAGON_CAT_CHOICE
import bav.petus.core.engine.OBTAIN_DRAGON_CAT_IS_SACRIFICE_KEY
import bav.petus.core.engine.OBTAIN_DRAGON_DOGUS_ID_KEY
import bav.petus.core.engine.OBTAIN_DRAGON_DOG_ASKED_KEY
import bav.petus.core.engine.OBTAIN_DRAGON_DOG_CHOICE
import bav.petus.core.engine.OBTAIN_DRAGON_FOREST_DECISION_KEY
import bav.petus.core.engine.OBTAIN_DRAGON_FROGUS_ID_KEY
import bav.petus.core.engine.OBTAIN_DRAGON_FROG_ASKED_KEY
import bav.petus.core.engine.OBTAIN_DRAGON_FROG_CHOICE
import bav.petus.core.engine.OBTAIN_DRAGON_MISTY_GORGE_DECISION_KEY
import bav.petus.core.engine.OBTAIN_DRAGON_STONE_DECISION_KEY
import bav.petus.core.engine.OBTAIN_DRAGON_TIMESTAMP_KEY
import bav.petus.core.engine.OBTAIN_FRACTAL_FROG_ID_KEY
import bav.petus.core.engine.OBTAIN_FRACTAL_TIMESTAMP_KEY
import bav.petus.core.engine.OBTAIN_FROGUS_ASKING_CAT_ID_KEY
import bav.petus.core.engine.OBTAIN_FROGUS_TIMESTAMP_KEY
import bav.petus.core.engine.QuestSystem
import bav.petus.core.engine.QuestSystem.Companion.QUEST_MEDITATION
import bav.petus.core.engine.QuestSystem.Companion.QUEST_NECRONOMICON
import bav.petus.core.engine.QuestSystem.Companion.QUEST_TO_OBTAIN_BOBER
import bav.petus.core.engine.QuestSystem.Companion.QUEST_TO_OBTAIN_DRAGON
import bav.petus.core.engine.QuestSystem.Companion.QUEST_TO_OBTAIN_FRACTAL
import bav.petus.core.engine.QuestSystem.Companion.QUEST_TO_OBTAIN_FROGUS
import bav.petus.core.engine.UserStats
import bav.petus.core.inventory.InventoryItem
import bav.petus.core.inventory.InventoryItemId
import bav.petus.core.resources.StringId
import bav.petus.core.time.getTimestampSecondsSinceEpoch
import bav.petus.model.AgeState
import bav.petus.model.BurialType
import bav.petus.model.FractalType
import bav.petus.model.Pet
import bav.petus.model.PetType
import bav.petus.model.Place
import kotlinx.coroutines.flow.first
import kotlin.random.Random

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

        val additionalAnswerOptions =
            questSystem.getAdditionalAnswers(pet, STANDARD_DIALOG_BEGINNING) +
                    getQuestResetAnswer(pet)

        val nodeWithAdditionalQuestAnswers = if (additionalAnswerOptions.isNotEmpty()) {
            standardBeginning.copy(
                answers = additionalAnswerOptions + standardBeginning.answers
            )
        } else {
            standardBeginning
        }
        val whatGoingOnStringId = getWhatGoingOnStringId(pet)
        val resultNode = when {
            whatGoingOnStringId != StringId.WhatIsGoingOnWithYou -> nodeWithAdditionalQuestAnswers.copy(
                text = listOf(whatGoingOnStringId)
            )
            else -> nodeWithAdditionalQuestAnswers
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

                when (answer.nextNode) {
                    PET_DESCRIPTION -> {
                        currentNode = addPetDescription(currentPet, currentNode)
                    }
                    MEDITATION_STAGE_3_DIALOG_1 -> {
                        // Add random meditation practice
                        currentNode = addRandomMeditationPractice(currentNode)
                    }
                }

                currentNode
            }
        }
    }

    private fun getQuestResetAnswer(pet: Pet): List<Answer> {
        return if (pet.type == PetType.Fractal) {
            listOf(
                Answer(
                    text = StringId.QuestResetAnswer0,
                    nextNode = QUEST_RESET_DIALOG_0,
                ),
            )
        } else {
            emptyList()
        }
    }

    private fun addPetDescription(pet: Pet?, node: DialogNode?): DialogNode? {
        if (pet == null || node == null) return node
        else {
            val texts = buildList {
                val isSick = pet.illness
                val isHungry = engine.isPetHungry(pet)
                val isPooped = pet.isPooped
                val isAngryAfterForceWakeUp = engine.isPetStillAngryAfterForcefulWakeUp(pet)
                val isBored = engine.isPetBored(pet) && isAngryAfterForceWakeUp.not()
                val isHalfHp = engine.isPetLowHealth(pet)
                val isGood = listOf(isSick, isHungry, isPooped, isBored, isHalfHp, isAngryAfterForceWakeUp).none { it }
                val isOld = pet.ageState == AgeState.Old

                if (isSick) add(getSickStringId(pet))
                if (isHungry) add(getHungryStringId(pet))
                if (isPooped) add(getPoopedStringId(pet))
                if (isBored) add(getBoredStringId(pet))
                if (isAngryAfterForceWakeUp) add(getAngryAfterForceWakeUpStringId(pet))
                if (isHalfHp) add(getHalfHpStringId(pet))
                if (isGood) add(getIAmGoodStringId(pet))
                if (isOld) add(getOldChancesOfDeathStringId(pet))
            }
            return node.copy(
                text = node.text + texts.shuffled()
            )
        }
    }

    private fun getWhatGoingOnStringId(pet: Pet): StringId {
        return when {
            engine.isPetSpeakLatin(pet) -> StringId.WhatIsGoingOnWithYouLatin
            engine.isPetSpeakPolish(pet) -> StringId.WhatIsGoingOnWithYouPolish
            engine.isPetSpeakMath(pet) -> {
                if (pet.ageState == AgeState.NewBorn) {
                    StringId.WhatIsGoingOnWithYouMathMandelbrot
                } else {
                    when (pet.fractalType) {
                        FractalType.Gosper -> StringId.WhatIsGoingOnWithYouMathGosper
                        FractalType.Koch -> StringId.WhatIsGoingOnWithYouMathKoch
                        FractalType.Sponge -> StringId.WhatIsGoingOnWithYouMathSponge
                    }
                }
            }
            else -> StringId.WhatIsGoingOnWithYou
        }
    }

    private fun getSickStringId(pet: Pet): StringId {
        return when {
            engine.isPetSpeakLatin(pet) -> StringId.IAmSickLatin
            engine.isPetSpeakPolish(pet) -> StringId.IAmSickPolish
            engine.isPetSpeakMath(pet) -> StringId.IAmSickMath
            else -> StringId.IAmSick
        }
    }

    private fun getHungryStringId(pet: Pet): StringId {
        return when {
            engine.isPetSpeakLatin(pet) -> StringId.IAmHungryLatin
            engine.isPetSpeakPolish(pet) -> StringId.IAmHungryPolish
            engine.isPetSpeakMath(pet) -> StringId.IAmHungryMath
            else -> StringId.IAmHungry
        }
    }

    private fun getPoopedStringId(pet: Pet): StringId {
        return when {
            engine.isPetSpeakLatin(pet) -> StringId.IPoopedLatin
            engine.isPetSpeakPolish(pet) -> StringId.IPoopedPolish
            engine.isPetSpeakMath(pet) -> StringId.IPoopedMath
            else -> StringId.IPooped
        }
    }

    private fun getBoredStringId(pet: Pet): StringId {
        return when {
            engine.isPetSpeakLatin(pet) -> StringId.IAmBoredLatin
            engine.isPetSpeakPolish(pet) -> StringId.IAmBoredPolish
            engine.isPetSpeakMath(pet) -> StringId.IAmBoredMath
            else -> StringId.IAmBored
        }
    }

    private fun getAngryAfterForceWakeUpStringId(pet: Pet): StringId {
        return when {
            engine.isPetSpeakLatin(pet) -> StringId.IAmStillAngryAfterForceWakeUpLatin
            engine.isPetSpeakPolish(pet) -> StringId.IAmStillAngryAfterForceWakeUpPolish
            engine.isPetSpeakMath(pet) -> StringId.IAmStillAngryAfterForceWakeUpMath
            else -> StringId.IAmStillAngryAfterForceWakeUp
        }
    }

    private fun getHalfHpStringId(pet: Pet): StringId {
        return when {
            engine.isPetSpeakLatin(pet) -> StringId.IAmHalfHpLatin
            engine.isPetSpeakPolish(pet) -> StringId.IAmHalfHpPolish
            engine.isPetSpeakMath(pet) -> StringId.IAmHalfHpMath
            else -> StringId.IAmHalfHp
        }
    }

    private fun getOldChancesOfDeathStringId(pet: Pet): StringId{
        val chance = pet.deathOfOldAgePossibility * 100
        return when {
            engine.isPetSpeakLatin(pet) -> StringId.IWillDieLatin(chance)
            pet.type == PetType.Bober -> StringId.IWillDiePolish(chance)
            else -> StringId.IWillDie(chance)
        }
    }

    private fun getIAmGoodStringId(pet: Pet): StringId {
        return when {
            engine.isPetSpeakLatin(pet) -> StringId.IAmGoodLatin
            engine.isPetSpeakPolish(pet) -> StringId.IAmGoodPolish
            engine.isPetSpeakMath(pet) -> StringId.IAmGoodMath
            else -> StringId.IAmGood
        }
    }

    private fun addRandomMeditationPractice(node: DialogNode?): DialogNode? {
        if (node == null) return node
        else {
            val randomIndex = Random.Default.nextInt(MEDITATION_EXERCISES.size)
            return node.copy(
                text = node.text + MEDITATION_EXERCISES[randomIndex]
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
        val MEDITATION_EXERCISES = listOf(
            StringId.MeditationExerciseBreathAwareness,
            StringId.MeditationExerciseBoxBreathing,
            StringId.MeditationExerciseBodyScan,
            StringId.MeditationExerciseLovingKindnessPhrases,
            StringId.MeditationExerciseCountingTheBreath,
            StringId.MeditationExerciseMantraRepetition,
            StringId.MeditationExerciseWalkingMeditation,
            StringId.MeditationExerciseCandleGazing,
            StringId.MeditationExerciseNotingPractice,
            StringId.MeditationExerciseFiveSensesAwareness,
            StringId.MeditationExerciseVisualization,
            StringId.MeditationExerciseMountainMeditation,
            StringId.MeditationExerciseSoundAwareness,
            StringId.MeditationExerciseTonglenExercise,
            StringId.MeditationExerciseGratitudeReflection,
            StringId.MeditationExerciseMindfulEating,
            StringId.MeditationExerciseProgressiveRelaxation,
            StringId.MeditationExerciseChakraFocusing,
            StringId.MeditationExerciseSelfInquiryQuestions,
            StringId.MeditationExerciseSilentSitting,
        )
        
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

        const val OBTAIN_BOBER_STAGE_2_DIALOG_0 = "OBTAIN_BOBER_STAGE_2_DIALOG_0"
        const val OBTAIN_BOBER_STAGE_2_DIALOG_1 = "OBTAIN_BOBER_STAGE_2_DIALOG_1"
        const val OBTAIN_BOBER_STAGE_3_DIALOG_0 = "OBTAIN_BOBER_STAGE_3_DIALOG_0"
        const val OBTAIN_BOBER_STAGE_4_DIALOG_0 = "OBTAIN_BOBER_STAGE_4_DIALOG_0"
        const val OBTAIN_BOBER_STAGE_6_DIALOG_0 = "OBTAIN_BOBER_STAGE_6_DIALOG_0"
        const val OBTAIN_BOBER_STAGE_6_DIALOG_1 = "OBTAIN_BOBER_STAGE_6_DIALOG_1"
        const val OBTAIN_BOBER_STAGE_8_DIALOG_0 = "OBTAIN_BOBER_STAGE_8_DIALOG_0"
        const val OBTAIN_BOBER_STAGE_9_DIALOG_0 = "OBTAIN_BOBER_STAGE_9_DIALOG_0"
        const val OBTAIN_BOBER_STAGE_9_DIALOG_1 = "OBTAIN_BOBER_STAGE_9_DIALOG_1"
        const val OBTAIN_BOBER_STAGE_9_DIALOG_2 = "OBTAIN_BOBER_STAGE_9_DIALOG_2"
        const val OBTAIN_BOBER_STAGE_10_DIALOG_0 = "OBTAIN_BOBER_STAGE_10_DIALOG_0"
        const val OBTAIN_BOBER_STAGE_11_DIALOG_0 = "OBTAIN_BOBER_STAGE_11_DIALOG_0"

        const val OBTAIN_FRACTAL_STAGE_1_DIALOG_0 = "OBTAIN_FRACTAL_STAGE_1_DIALOG_0"
        const val OBTAIN_FRACTAL_STAGE_1_DIALOG_1 = "OBTAIN_FRACTAL_STAGE_1_DIALOG_1"
        const val OBTAIN_FRACTAL_STAGE_3_DIALOG_0 = "OBTAIN_FRACTAL_STAGE_3_DIALOG_0"
        const val OBTAIN_FRACTAL_STAGE_5_DIALOG_0 = "OBTAIN_FRACTAL_STAGE_5_DIALOG_0"
        const val OBTAIN_FRACTAL_STAGE_6_DIALOG_0 = "OBTAIN_FRACTAL_STAGE_6_DIALOG_0"

        const val MEDITATION_STAGE_1_DIALOG_0 = "MEDITATION_STAGE_1_DIALOG_0"
        const val MEDITATION_STAGE_1_DIALOG_1 = "MEDITATION_STAGE_1_DIALOG_1"
        const val MEDITATION_STAGE_2_DIALOG_0 = "MEDITATION_STAGE_2_DIALOG_0"
        const val MEDITATION_STAGE_2_DIALOG_1 = "MEDITATION_STAGE_2_DIALOG_1"
        const val MEDITATION_STAGE_2_DIALOG_2 = "MEDITATION_STAGE_2_DIALOG_2"
        const val MEDITATION_STAGE_3_DIALOG_0 = "MEDITATION_STAGE_3_DIALOG_0"
        const val MEDITATION_STAGE_3_DIALOG_1 = "MEDITATION_STAGE_3_DIALOG_1"
        const val MEDITATION_STAGE_3_DIALOG_2 = "MEDITATION_STAGE_3_DIALOG_2"
        const val MEDITATION_STAGE_3_DIALOG_3 = "MEDITATION_STAGE_3_DIALOG_3"
        const val MEDITATION_STAGE_3_DIALOG_4 = "MEDITATION_STAGE_3_DIALOG_4"

        const val OBTAIN_DRAGON_STAGE_1_DIALOG_0 = "OBTAIN_DRAGON_STAGE_1_DIALOG_0"
        const val OBTAIN_DRAGON_STAGE_1_DIALOG_1 = "OBTAIN_DRAGON_STAGE_1_DIALOG_1"
        const val OBTAIN_DRAGON_STAGE_1_DIALOG_2 = "OBTAIN_DRAGON_STAGE_1_DIALOG_2"
        const val OBTAIN_DRAGON_STAGE_2_DIALOG_0 = "OBTAIN_DRAGON_STAGE_2_DIALOG_0"
        const val OBTAIN_DRAGON_STAGE_2_DIALOG_1 = "OBTAIN_DRAGON_STAGE_2_DIALOG_1"
        const val OBTAIN_DRAGON_STAGE_2_DIALOG_2 = "OBTAIN_DRAGON_STAGE_2_DIALOG_2"
        const val OBTAIN_DRAGON_STAGE_2_DIALOG_3 = "OBTAIN_DRAGON_STAGE_2_DIALOG_3"
        const val OBTAIN_DRAGON_STAGE_3_DIALOG_0 = "OBTAIN_DRAGON_STAGE_3_DIALOG_0"
        const val OBTAIN_DRAGON_STAGE_4_DIALOG_0 = "OBTAIN_DRAGON_STAGE_4_DIALOG_0"
        const val OBTAIN_DRAGON_STAGE_4_DIALOG_1 = "OBTAIN_DRAGON_STAGE_4_DIALOG_1"
        const val OBTAIN_DRAGON_STAGE_4_DIALOG_2 = "OBTAIN_DRAGON_STAGE_4_DIALOG_2"
        const val OBTAIN_DRAGON_STAGE_5_DIALOG_0 = "OBTAIN_DRAGON_STAGE_5_DIALOG_0"
        const val OBTAIN_DRAGON_STAGE_5_DIALOG_1 = "OBTAIN_DRAGON_STAGE_5_DIALOG_1"
        const val OBTAIN_DRAGON_STAGE_5_DIALOG_2 = "OBTAIN_DRAGON_STAGE_5_DIALOG_2"
        const val OBTAIN_DRAGON_STAGE_5_DIALOG_3 = "OBTAIN_DRAGON_STAGE_5_DIALOG_3"
        const val OBTAIN_DRAGON_STAGE_5_DIALOG_4 = "OBTAIN_DRAGON_STAGE_5_DIALOG_4"
        const val OBTAIN_DRAGON_STAGE_5_DIALOG_5 = "OBTAIN_DRAGON_STAGE_5_DIALOG_5"
        const val OBTAIN_DRAGON_STAGE_7_DIALOG_0 = "OBTAIN_DRAGON_STAGE_7_DIALOG_0"
        const val OBTAIN_DRAGON_STAGE_7_DIALOG_1 = "OBTAIN_DRAGON_STAGE_7_DIALOG_1"
        const val OBTAIN_DRAGON_STAGE_7_DIALOG_2 = "OBTAIN_DRAGON_STAGE_7_DIALOG_2"
        const val OBTAIN_DRAGON_STAGE_7_DIALOG_3 = "OBTAIN_DRAGON_STAGE_7_DIALOG_3"
        const val OBTAIN_DRAGON_STAGE_9_DIALOG_0 = "OBTAIN_DRAGON_STAGE_9_DIALOG_0"
        const val OBTAIN_DRAGON_STAGE_9_DIALOG_1 = "OBTAIN_DRAGON_STAGE_9_DIALOG_1"
        const val OBTAIN_DRAGON_STAGE_9_DIALOG_2 = "OBTAIN_DRAGON_STAGE_9_DIALOG_2"
        const val OBTAIN_DRAGON_STAGE_10_DIALOG_0 = "OBTAIN_DRAGON_STAGE_10_DIALOG_0"
        const val OBTAIN_DRAGON_STAGE_10_DIALOG_1 = "OBTAIN_DRAGON_STAGE_10_DIALOG_1"
        const val OBTAIN_DRAGON_STAGE_10_DIALOG_2 = "OBTAIN_DRAGON_STAGE_10_DIALOG_2"
        const val OBTAIN_DRAGON_STAGE_10_DIALOG_3 = "OBTAIN_DRAGON_STAGE_10_DIALOG_3"
        const val OBTAIN_DRAGON_STAGE_10_DIALOG_4 = "OBTAIN_DRAGON_STAGE_10_DIALOG_4"
        const val OBTAIN_DRAGON_STAGE_12_DIALOG_0 = "OBTAIN_DRAGON_STAGE_12_DIALOG_0"
        const val OBTAIN_DRAGON_STAGE_12_DIALOG_1 = "OBTAIN_DRAGON_STAGE_12_DIALOG_1"
        const val OBTAIN_DRAGON_STAGE_12_DIALOG_2 = "OBTAIN_DRAGON_STAGE_12_DIALOG_2"
        const val OBTAIN_DRAGON_STAGE_12_DIALOG_3 = "OBTAIN_DRAGON_STAGE_12_DIALOG_3"
        const val OBTAIN_DRAGON_STAGE_14_DIALOG_0 = "OBTAIN_DRAGON_STAGE_14_DIALOG_0"
        const val OBTAIN_DRAGON_STAGE_14_DIALOG_1 = "OBTAIN_DRAGON_STAGE_14_DIALOG_1"
        const val OBTAIN_DRAGON_STAGE_14_DIALOG_2 = "OBTAIN_DRAGON_STAGE_14_DIALOG_2"
        const val OBTAIN_DRAGON_STAGE_15_DIALOG_0 = "OBTAIN_DRAGON_STAGE_15_DIALOG_0"
        const val OBTAIN_DRAGON_STAGE_15_DIALOG_1 = "OBTAIN_DRAGON_STAGE_15_DIALOG_1"
        const val OBTAIN_DRAGON_STAGE_15_DIALOG_2 = "OBTAIN_DRAGON_STAGE_15_DIALOG_2"
        const val OBTAIN_DRAGON_STAGE_15_DIALOG_3 = "OBTAIN_DRAGON_STAGE_15_DIALOG_3"
        const val OBTAIN_DRAGON_STAGE_15_DIALOG_4 = "OBTAIN_DRAGON_STAGE_15_DIALOG_4"
        const val OBTAIN_DRAGON_STAGE_17_DIALOG_0 = "OBTAIN_DRAGON_STAGE_17_DIALOG_0"
        const val OBTAIN_DRAGON_STAGE_17_DIALOG_1 = "OBTAIN_DRAGON_STAGE_17_DIALOG_1"
        const val OBTAIN_DRAGON_STAGE_17_DIALOG_2 = "OBTAIN_DRAGON_STAGE_17_DIALOG_2"
        const val OBTAIN_DRAGON_STAGE_17_DIALOG_3 = "OBTAIN_DRAGON_STAGE_17_DIALOG_3"
        const val OBTAIN_DRAGON_STAGE_19_DIALOG_0 = "OBTAIN_DRAGON_STAGE_19_DIALOG_0"
        const val OBTAIN_DRAGON_STAGE_19_DIALOG_1 = "OBTAIN_DRAGON_STAGE_19_DIALOG_1"
        const val OBTAIN_DRAGON_STAGE_19_DIALOG_2 = "OBTAIN_DRAGON_STAGE_19_DIALOG_2"
        const val OBTAIN_DRAGON_STAGE_20_DIALOG_0 = "OBTAIN_DRAGON_STAGE_20_DIALOG_0"
        const val OBTAIN_DRAGON_STAGE_20_DIALOG_1 = "OBTAIN_DRAGON_STAGE_20_DIALOG_1"
        const val OBTAIN_DRAGON_STAGE_20_DIALOG_2 = "OBTAIN_DRAGON_STAGE_20_DIALOG_2"
        const val OBTAIN_DRAGON_STAGE_20_DIALOG_3 = "OBTAIN_DRAGON_STAGE_20_DIALOG_3"
        const val OBTAIN_DRAGON_STAGE_20_DIALOG_4 = "OBTAIN_DRAGON_STAGE_20_DIALOG_4"
        const val OBTAIN_DRAGON_STAGE_22_DIALOG_0 = "OBTAIN_DRAGON_STAGE_22_DIALOG_0"
        const val OBTAIN_DRAGON_STAGE_22_DIALOG_1 = "OBTAIN_DRAGON_STAGE_22_DIALOG_1"
        const val OBTAIN_DRAGON_STAGE_22_DIALOG_2 = "OBTAIN_DRAGON_STAGE_22_DIALOG_2"
        const val OBTAIN_DRAGON_STAGE_22_DIALOG_3 = "OBTAIN_DRAGON_STAGE_22_DIALOG_3"
        const val OBTAIN_DRAGON_STAGE_24_DIALOG_0 = "OBTAIN_DRAGON_STAGE_24_DIALOG_0"
        const val OBTAIN_DRAGON_STAGE_24_DIALOG_1 = "OBTAIN_DRAGON_STAGE_24_DIALOG_1"
        const val OBTAIN_DRAGON_STAGE_24_DIALOG_2 = "OBTAIN_DRAGON_STAGE_24_DIALOG_2"
        const val OBTAIN_DRAGON_STAGE_24_DIALOG_3 = "OBTAIN_DRAGON_STAGE_24_DIALOG_3"
        const val OBTAIN_DRAGON_STAGE_24_DIALOG_4 = "OBTAIN_DRAGON_STAGE_24_DIALOG_4"
        const val OBTAIN_DRAGON_STAGE_24_DIALOG_5 = "OBTAIN_DRAGON_STAGE_24_DIALOG_5"
        const val OBTAIN_DRAGON_STAGE_24_DIALOG_6 = "OBTAIN_DRAGON_STAGE_24_DIALOG_6"
        const val OBTAIN_DRAGON_STAGE_24_DIALOG_7 = "OBTAIN_DRAGON_STAGE_24_DIALOG_7"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_0 = "OBTAIN_DRAGON_STAGE_25_DIALOG_0"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_1 = "OBTAIN_DRAGON_STAGE_25_DIALOG_1"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_2 = "OBTAIN_DRAGON_STAGE_25_DIALOG_2"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_3 = "OBTAIN_DRAGON_STAGE_25_DIALOG_3"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_4 = "OBTAIN_DRAGON_STAGE_25_DIALOG_4"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_5 = "OBTAIN_DRAGON_STAGE_25_DIALOG_5"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_6 = "OBTAIN_DRAGON_STAGE_25_DIALOG_6"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_7 = "OBTAIN_DRAGON_STAGE_25_DIALOG_7"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_8 = "OBTAIN_DRAGON_STAGE_25_DIALOG_8"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_9 = "OBTAIN_DRAGON_STAGE_25_DIALOG_9"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_10 = "OBTAIN_DRAGON_STAGE_25_DIALOG_10"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_11 = "OBTAIN_DRAGON_STAGE_25_DIALOG_11"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_12 = "OBTAIN_DRAGON_STAGE_25_DIALOG_12"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_13 = "OBTAIN_DRAGON_STAGE_25_DIALOG_13"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_14 = "OBTAIN_DRAGON_STAGE_25_DIALOG_14"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_15 = "OBTAIN_DRAGON_STAGE_25_DIALOG_15"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_16 = "OBTAIN_DRAGON_STAGE_25_DIALOG_16"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_17 = "OBTAIN_DRAGON_STAGE_25_DIALOG_17"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_18 = "OBTAIN_DRAGON_STAGE_25_DIALOG_18"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_19 = "OBTAIN_DRAGON_STAGE_25_DIALOG_19"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_20 = "OBTAIN_DRAGON_STAGE_25_DIALOG_20"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_21 = "OBTAIN_DRAGON_STAGE_25_DIALOG_21"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_22 = "OBTAIN_DRAGON_STAGE_25_DIALOG_22"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_23 = "OBTAIN_DRAGON_STAGE_25_DIALOG_23"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_24 = "OBTAIN_DRAGON_STAGE_25_DIALOG_24"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_25 = "OBTAIN_DRAGON_STAGE_25_DIALOG_25"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_26 = "OBTAIN_DRAGON_STAGE_25_DIALOG_26"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_27 = "OBTAIN_DRAGON_STAGE_25_DIALOG_27"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_28 = "OBTAIN_DRAGON_STAGE_25_DIALOG_28"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_29 = "OBTAIN_DRAGON_STAGE_25_DIALOG_29"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_30 = "OBTAIN_DRAGON_STAGE_25_DIALOG_30"
        const val OBTAIN_DRAGON_STAGE_25_DIALOG_31 = "OBTAIN_DRAGON_STAGE_25_DIALOG_31"

        const val QUEST_RESET_DIALOG_0 = "QUEST_RESET_DIALOG_0"
        const val QUEST_RESET_DIALOG_1 = "QUEST_RESET_DIALOG_1"
        const val QUEST_RESET_DIALOG_2 = "QUEST_RESET_DIALOG_2"

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
            OBTAIN_BOBER_STAGE_2_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainBoberStage2Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainBoberStage2Answer1,
                        nextNode = null,
                        action = { questSystem, _, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_BOBER_TIMESTAMP_KEY] = now
                                pet?.let { p -> store[OBTAIN_BOBER_SEARCH_DOG_ID_KEY] = p.id }
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_BOBER)
                        }
                    ),
                )
            ),
            OBTAIN_BOBER_STAGE_2_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainBoberStage2Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.Ok,
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_BOBER_STAGE_3_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainBoberStage3Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.Ok,
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_BOBER_STAGE_4_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainBoberStage4Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.Thanks,
                        nextNode = null,
                        action = { questSystem, _, _ ->
                            with(questSystem.userStats) {
                                addInventoryItem(
                                    InventoryItem(id = InventoryItemId.CatusEgg, amount = 1)
                                )
                                addInventoryItem(
                                    InventoryItem(id = InventoryItemId.DogusEgg, amount = 1)
                                )
                                addInventoryItem(
                                    InventoryItem(id = InventoryItemId.FrogusEgg, amount = 1)
                                )
                            }
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_BOBER_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_BOBER)
                        }
                    ),
                )
            ),
            OBTAIN_BOBER_STAGE_6_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainBoberStage6Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.Thanks,
                        nextNode = null,
                        action = { questSystem, _, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_BOBER_TIMESTAMP_KEY] = now
                                pet?.let { p -> store[OBTAIN_BOBER_SEARCH_DOG_ID_KEY] = p.id }
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_BOBER)
                        }
                    ),
                )
            ),
            OBTAIN_BOBER_STAGE_6_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainBoberStage6Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.Ok,
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_BOBER_STAGE_8_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainBoberStage8Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainBoberStage8Answer0,
                        nextNode = null,
                        action = { questSystem, _, _ ->
                            with(questSystem.userStats) {
                                addInventoryItem(
                                    InventoryItem(id = InventoryItemId.CatusEgg, amount = 1)
                                )
                                addInventoryItem(
                                    InventoryItem(id = InventoryItemId.DogusEgg, amount = 1)
                                )
                                addInventoryItem(
                                    InventoryItem(id = InventoryItemId.FrogusEgg, amount = 1)
                                )
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_BOBER)
                        },
                    ),
                )
            ),
            OBTAIN_BOBER_STAGE_9_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainBoberStage9Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.Thanks,
                        nextNode = null,
                        action = { questSystem, _, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_BOBER_TIMESTAMP_KEY] = now
                                pet?.let { p -> store[OBTAIN_BOBER_SEARCH_FROG_ID_KEY] = p.id }
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_BOBER)
                        }
                    ),
                )
            ),
            OBTAIN_BOBER_STAGE_9_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainBoberStage9Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.Ok,
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_BOBER_STAGE_9_DIALOG_2 to DialogNode(
                text = listOf(StringId.ObtainBoberStage9Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.Ok,
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_BOBER_STAGE_10_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainBoberStage10Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.Ok,
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_BOBER_STAGE_11_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainBoberStage11Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainBoberStage11Answer0,
                        nextNode = null,
                        action = { questSystem, userStats, _ ->
                            userStats.addNewAvailablePetType(PetType.Bober)
                            userStats.addInventoryItem(InventoryItem(InventoryItemId.BoberEgg, 1))
                            userStats.addInventoryItem(InventoryItem(InventoryItemId.Basket, 1))
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_BOBER)
                        }
                    ),
                )
            ),
            OBTAIN_FRACTAL_STAGE_1_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainFractalStage1Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.Sure,
                        nextNode = OBTAIN_FRACTAL_STAGE_1_DIALOG_1,
                    ),
                )
            ),
            OBTAIN_FRACTAL_STAGE_1_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainFractalStage1Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainFractalStage1Answer1,
                        nextNode = null,
                        action = { questSystem, _, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_FRACTAL_TIMESTAMP_KEY] = now
                                pet?.let { p -> store[OBTAIN_FRACTAL_FROG_ID_KEY] = p.id }
                            }
                            questSystem.userStats.addInventoryItem(
                                item = InventoryItem(
                                    id = InventoryItemId.TwoMeterRuler,
                                    amount = 1,
                                )
                            )
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_FRACTAL)
                        }
                    ),
                )
            ),
            OBTAIN_FRACTAL_STAGE_3_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainFractalStage3Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainFractalStage3Answer1,
                        nextNode = null,
                        action = { questSystem, _, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_FRACTAL_TIMESTAMP_KEY] = now
                            }
                            questSystem.userStats.removeInventoryItem(
                                item = InventoryItem(
                                    id = InventoryItemId.TwoMeterRuler,
                                    amount = 1,
                                )
                            )
                            questSystem.userStats.addInventoryItem(
                                item = InventoryItem(
                                    id = InventoryItemId.TenCentimeterRuler,
                                    amount = 1,
                                )
                            )
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_FRACTAL)
                        }
                    ),
                )
            ),
            OBTAIN_FRACTAL_STAGE_5_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainFractalStage5Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainFractalStage5Answer1,
                        nextNode = null,
                        action = { questSystem, _, pet ->
                            questSystem.userStats.removeInventoryItem(
                                item = InventoryItem(
                                    id = InventoryItemId.TenCentimeterRuler,
                                    amount = 1,
                                )
                            )
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_FRACTAL)
                        }
                    ),
                )
            ),
            OBTAIN_FRACTAL_STAGE_6_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainFractalStage6Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainFractalStage6Answer1,
                        nextNode = null,
                        action = { questSystem, _, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_FRACTAL_TIMESTAMP_KEY] = now
                            }
                            questSystem.userStats.addInventoryItem(
                                item = InventoryItem(
                                    id = InventoryItemId.MathBook,
                                    amount = 1,
                                )
                            )
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_FRACTAL)
                        }
                    ),
                )
            ),
            MEDITATION_STAGE_1_DIALOG_0 to DialogNode(
                text = listOf(StringId.MeditationStage1Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.Sure,
                        nextNode = MEDITATION_STAGE_1_DIALOG_1,
                    ),
                )
            ),
            MEDITATION_STAGE_1_DIALOG_1 to DialogNode(
                text = listOf(StringId.MeditationStage1Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.MeditationStage1Answer1,
                        nextNode = null,
                        action = { questSystem, _, _ ->
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_MEDITATION)
                        }
                    ),
                )
            ),
            MEDITATION_STAGE_2_DIALOG_0 to DialogNode(
                text = listOf(StringId.MeditationStage2Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.Sure,
                        nextNode = MEDITATION_STAGE_2_DIALOG_2,
                    ),
                )
            ),
            MEDITATION_STAGE_2_DIALOG_1 to DialogNode(
                text = listOf(StringId.MeditationStage2Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.Ok,
                        nextNode = null,
                    ),
                )
            ),
            MEDITATION_STAGE_2_DIALOG_2 to DialogNode(
                text = listOf(StringId.MeditationStage2Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.Thanks,
                        nextNode = null,
                        action = { questSystem, _, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[MEDITATION_TIMESTAMP_KEY] = now
                                store[MEDITATION_EXERCISES_LEFT_KEY] = MEDITATION_TOTAL_EXERCISES - 1
                                pet?.let { p -> store[MEDITATION_FROG_ID_KEY] = p.id }
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_MEDITATION)
                        }
                    ),
                )
            ),
            MEDITATION_STAGE_3_DIALOG_0 to DialogNode(
                text = listOf(StringId.MeditationStage3Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.Ok,
                        nextNode = null,
                    ),
                )
            ),
            MEDITATION_STAGE_3_DIALOG_1 to DialogNode(
                text = listOf(StringId.MeditationStage3Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.Ok,
                        nextNode = MEDITATION_STAGE_3_DIALOG_3,
                    ),
                )
            ),
            MEDITATION_STAGE_3_DIALOG_2 to DialogNode(
                text = listOf(StringId.MeditationStage3Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.MeditationStage3Answer1,
                        nextNode = null,
                        action = { questSystem, _, pet ->
                            questSystem.setQuestStage(QuestSystem.QUEST_MEDITATION, 2)
                        }
                    ),
                )
            ),
            MEDITATION_STAGE_3_DIALOG_3 to DialogNode(
                text = listOf(StringId.MeditationStage2Dialog2), // same as in MEDITATION_STAGE_2_DIALOG_2
                answers = listOf(
                    Answer(
                        text = StringId.Ok,
                        nextNode = null,
                        action = { questSystem, _, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            val exercisesLeft = questSystem.dataStore.data.first()[MEDITATION_EXERCISES_LEFT_KEY] ?: MEDITATION_TOTAL_EXERCISES
                            questSystem.dataStore.edit { store ->
                                store[MEDITATION_TIMESTAMP_KEY] = now
                                store[MEDITATION_EXERCISES_LEFT_KEY] = exercisesLeft - 1
                            }
                        }
                    ),
                )
            ),
            MEDITATION_STAGE_3_DIALOG_4 to DialogNode(
                text = listOf(StringId.MeditationStage3Dialog4),
                answers = listOf(
                    Answer(
                        text = StringId.MeditationStage3Answer2,
                        nextNode = null,
                        action = { questSystem, userStats, _ ->
                            userStats.addNewAbility(Ability.Meditation)
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_MEDITATION)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_1_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainDragonStage1Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage1Answer1,
                        nextNode = OBTAIN_DRAGON_STAGE_1_DIALOG_1,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_1_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainDragonStage1Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage1Answer2,
                        nextNode = OBTAIN_DRAGON_STAGE_1_DIALOG_2,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_1_DIALOG_2 to DialogNode(
                text = listOf(StringId.ObtainDragonStage1Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage1Answer3,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                pet?.let { p ->
                                    questSystem.petsRepo.updatePet(
                                        pet = p.copy(
                                            questName = QuestSystem.QUEST_TO_OBTAIN_DRAGON,
                                        )
                                    )
                                    store[OBTAIN_DRAGON_CATUS_ID_KEY] = p.id
                                }
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_2_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainDragonStage2Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage2Answer1,
                        nextNode = OBTAIN_DRAGON_STAGE_2_DIALOG_1,
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage2Answer2,
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_2_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainDragonStage2Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage2Answer3,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                pet?.let { p ->
                                    questSystem.petsRepo.updatePet(
                                        pet = p.copy(
                                            questName = QuestSystem.QUEST_TO_OBTAIN_DRAGON,
                                        )
                                    )
                                    when (pet.type) {
                                        PetType.Dogus -> {
                                            store[OBTAIN_DRAGON_DOGUS_ID_KEY] = p.id
                                        }
                                        PetType.Frogus -> {
                                            store[OBTAIN_DRAGON_FROGUS_ID_KEY] = p.id
                                        }
                                        PetType.Bober -> {
                                            store[OBTAIN_DRAGON_BOBER_ID_KEY] = p.id
                                        }
                                        else -> Unit
                                    }
                                }
                            }
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_2_DIALOG_2 to DialogNode(
                text = listOf(StringId.ObtainDragonStage2Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage2Answer1,
                        nextNode = OBTAIN_DRAGON_STAGE_2_DIALOG_1,
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage2Answer2,
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_2_DIALOG_3 to DialogNode(
                text = listOf(StringId.ObtainDragonStage2Dialog3),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage2Answer5,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_3_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainDragonStage3Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_4_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainDragonStage4Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_4_DIALOG_1,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_4_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainDragonStage4Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_4_DIALOG_2,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_4_DIALOG_2 to DialogNode(
                text = listOf(StringId.ObtainDragonStage4Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_5_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainDragonStage5Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_CAT_ASKED_KEY] = true
                            }
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_5_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainDragonStage5Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_DOG_ASKED_KEY] = true
                            }
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_5_DIALOG_2 to DialogNode(
                text = listOf(StringId.ObtainDragonStage5Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_FROG_ASKED_KEY] = true
                            }
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_5_DIALOG_3 to DialogNode(
                text = listOf(StringId.ObtainDragonStage5Dialog3),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_BOBER_ASKED_KEY] = true
                            }
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_5_DIALOG_4 to DialogNode(
                text = listOf(StringId.ObtainDragonStage5Dialog4),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage5Answer2,
                        nextNode = OBTAIN_DRAGON_STAGE_5_DIALOG_5,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_MISTY_GORGE_DECISION_KEY] = OBTAIN_DRAGON_CAT_CHOICE
                            }
                        }
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage5Answer3,
                        nextNode = OBTAIN_DRAGON_STAGE_5_DIALOG_5,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_MISTY_GORGE_DECISION_KEY] = OBTAIN_DRAGON_DOG_CHOICE
                            }
                        }
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage5Answer4,
                        nextNode = OBTAIN_DRAGON_STAGE_5_DIALOG_5,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_MISTY_GORGE_DECISION_KEY] = OBTAIN_DRAGON_FROG_CHOICE
                            }
                        }
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage5Answer5,
                        nextNode = OBTAIN_DRAGON_STAGE_5_DIALOG_5,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_MISTY_GORGE_DECISION_KEY] = OBTAIN_DRAGON_BOBER_CHOICE
                            }
                        }
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage5Answer6,
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_5_DIALOG_5 to DialogNode(
                text = listOf(StringId.ObtainDragonStage5Dialog5),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_7_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainDragonStage7Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_7_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainDragonStage7Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_7_DIALOG_2 to DialogNode(
                text = listOf(StringId.ObtainDragonStage7Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_7_DIALOG_3 to DialogNode(
                text = listOf(StringId.ObtainDragonStage7Dialog3),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_9_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainDragonStage9Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_9_DIALOG_1,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_9_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainDragonStage9Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_9_DIALOG_2,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_9_DIALOG_2 to DialogNode(
                text = listOf(StringId.ObtainDragonStage9Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_CAT_ASKED_KEY] = false
                                store[OBTAIN_DRAGON_DOG_ASKED_KEY] = false
                                store[OBTAIN_DRAGON_FROG_ASKED_KEY] = false
                                store[OBTAIN_DRAGON_BOBER_ASKED_KEY] = false
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_10_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainDragonStage10Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_CAT_ASKED_KEY] = true
                            }
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_10_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainDragonStage10Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_DOG_ASKED_KEY] = true
                            }
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_10_DIALOG_2 to DialogNode(
                text = listOf(StringId.ObtainDragonStage10Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_FROG_ASKED_KEY] = true
                            }
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_10_DIALOG_3 to DialogNode(
                text = listOf(StringId.ObtainDragonStage10Dialog3),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_BOBER_ASKED_KEY] = true
                            }
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_10_DIALOG_4 to DialogNode(
                // Ok to reuse everything here except for OBTAIN_DRAGON_FOREST_DECISION_KEY
                text = listOf(StringId.ObtainDragonStage5Dialog4),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage5Answer2,
                        nextNode = OBTAIN_DRAGON_STAGE_5_DIALOG_5,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_FOREST_DECISION_KEY] = OBTAIN_DRAGON_CAT_CHOICE
                            }
                        }
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage5Answer3,
                        nextNode = OBTAIN_DRAGON_STAGE_5_DIALOG_5,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_FOREST_DECISION_KEY] = OBTAIN_DRAGON_DOG_CHOICE
                            }
                        }
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage5Answer4,
                        nextNode = OBTAIN_DRAGON_STAGE_5_DIALOG_5,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_FOREST_DECISION_KEY] = OBTAIN_DRAGON_FROG_CHOICE
                            }
                        }
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage5Answer5,
                        nextNode = OBTAIN_DRAGON_STAGE_5_DIALOG_5,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_FOREST_DECISION_KEY] = OBTAIN_DRAGON_BOBER_CHOICE
                            }
                        }
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage5Answer6,
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_12_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainDragonStage12Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_12_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainDragonStage12Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_12_DIALOG_2 to DialogNode(
                text = listOf(StringId.ObtainDragonStage12Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_12_DIALOG_3 to DialogNode(
                text = listOf(StringId.ObtainDragonStage12Dialog3),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_14_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainDragonStage14Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_14_DIALOG_1,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_14_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainDragonStage14Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_14_DIALOG_2,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_14_DIALOG_2 to DialogNode(
                text = listOf(StringId.ObtainDragonStage14Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_CAT_ASKED_KEY] = false
                                store[OBTAIN_DRAGON_DOG_ASKED_KEY] = false
                                store[OBTAIN_DRAGON_FROG_ASKED_KEY] = false
                                store[OBTAIN_DRAGON_BOBER_ASKED_KEY] = false
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_15_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainDragonStage15Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_CAT_ASKED_KEY] = true
                            }
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_15_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainDragonStage15Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_DOG_ASKED_KEY] = true
                            }
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_15_DIALOG_2 to DialogNode(
                text = listOf(StringId.ObtainDragonStage15Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_FROG_ASKED_KEY] = true
                            }
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_15_DIALOG_3 to DialogNode(
                text = listOf(StringId.ObtainDragonStage15Dialog3),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_BOBER_ASKED_KEY] = true
                            }
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_15_DIALOG_4 to DialogNode(
                // Ok to reuse everything here except for OBTAIN_DRAGON_STONE_DECISION_KEY
                text = listOf(StringId.ObtainDragonStage5Dialog4),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage5Answer2,
                        nextNode = OBTAIN_DRAGON_STAGE_5_DIALOG_5,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_STONE_DECISION_KEY] = OBTAIN_DRAGON_CAT_CHOICE
                            }
                        }
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage5Answer3,
                        nextNode = OBTAIN_DRAGON_STAGE_5_DIALOG_5,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_STONE_DECISION_KEY] = OBTAIN_DRAGON_DOG_CHOICE
                            }
                        }
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage5Answer4,
                        nextNode = OBTAIN_DRAGON_STAGE_5_DIALOG_5,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_STONE_DECISION_KEY] = OBTAIN_DRAGON_FROG_CHOICE
                            }
                        }
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage5Answer5,
                        nextNode = OBTAIN_DRAGON_STAGE_5_DIALOG_5,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_STONE_DECISION_KEY] = OBTAIN_DRAGON_BOBER_CHOICE
                            }
                        }
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage5Answer6,
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_17_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainDragonStage17Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_17_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainDragonStage17Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_17_DIALOG_2 to DialogNode(
                text = listOf(StringId.ObtainDragonStage17Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_17_DIALOG_3 to DialogNode(
                text = listOf(StringId.ObtainDragonStage17Dialog3),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_19_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainDragonStage19Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_19_DIALOG_1,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_19_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainDragonStage19Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_19_DIALOG_2,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_19_DIALOG_2 to DialogNode(
                text = listOf(StringId.ObtainDragonStage19Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_CAT_ASKED_KEY] = false
                                store[OBTAIN_DRAGON_DOG_ASKED_KEY] = false
                                store[OBTAIN_DRAGON_FROG_ASKED_KEY] = false
                                store[OBTAIN_DRAGON_BOBER_ASKED_KEY] = false
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_20_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainDragonStage20Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_CAT_ASKED_KEY] = true
                            }
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_20_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainDragonStage20Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_DOG_ASKED_KEY] = true
                            }
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_20_DIALOG_2 to DialogNode(
                text = listOf(StringId.ObtainDragonStage20Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_FROG_ASKED_KEY] = true
                            }
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_20_DIALOG_3 to DialogNode(
                text = listOf(StringId.ObtainDragonStage20Dialog3),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_BOBER_ASKED_KEY] = true
                            }
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_20_DIALOG_4 to DialogNode(
                // Ok to reuse everything here except for OBTAIN_DRAGON_ASH_DECISION_KEY
                text = listOf(StringId.ObtainDragonStage5Dialog4),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage5Answer2,
                        nextNode = OBTAIN_DRAGON_STAGE_5_DIALOG_5,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_ASH_DECISION_KEY] = OBTAIN_DRAGON_CAT_CHOICE
                            }
                        }
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage5Answer3,
                        nextNode = OBTAIN_DRAGON_STAGE_5_DIALOG_5,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_ASH_DECISION_KEY] = OBTAIN_DRAGON_DOG_CHOICE
                            }
                        }
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage5Answer4,
                        nextNode = OBTAIN_DRAGON_STAGE_5_DIALOG_5,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_ASH_DECISION_KEY] = OBTAIN_DRAGON_FROG_CHOICE
                            }
                        }
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage5Answer5,
                        nextNode = OBTAIN_DRAGON_STAGE_5_DIALOG_5,
                        action = { questSystem, userStats, pet ->
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_ASH_DECISION_KEY] = OBTAIN_DRAGON_BOBER_CHOICE
                            }
                        }
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage5Answer6,
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_22_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainDragonStage22Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_22_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainDragonStage22Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_22_DIALOG_2 to DialogNode(
                text = listOf(StringId.ObtainDragonStage22Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_22_DIALOG_3 to DialogNode(
                text = listOf(StringId.ObtainDragonStage22Dialog3),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_24_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainDragonStage24Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_24_DIALOG_1,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_24_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainDragonStage24Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_24_DIALOG_2,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_24_DIALOG_2 to DialogNode(
                text = listOf(StringId.ObtainDragonStage24Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.Excellent,
                        nextNode = OBTAIN_DRAGON_STAGE_24_DIALOG_3,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_24_DIALOG_3 to DialogNode(
                text = listOf(StringId.ObtainDragonStage24Dialog3),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage24Answer1,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            // User doesn't have Necronomicon
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_24_DIALOG_4 to DialogNode(
                text = listOf(StringId.ObtainDragonStage24Dialog0), // Same as in dialog 0
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_24_DIALOG_5,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_24_DIALOG_5 to DialogNode(
                text = listOf(StringId.ObtainDragonStage24Dialog1), // Same as in dialog 1
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_24_DIALOG_6,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_24_DIALOG_6 to DialogNode(
                text = listOf(StringId.ObtainDragonStage24Dialog2), // Same as in dialog 2
                answers = listOf(
                    Answer(
                        text = StringId.Excellent,
                        nextNode = OBTAIN_DRAGON_STAGE_24_DIALOG_7,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_24_DIALOG_7 to DialogNode(
                // Different dialog because user has Necronomicon
                text = listOf(StringId.ObtainDragonStage24Dialog7),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage24Answer1, // Same as answer 1
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            // User has Necronomicon
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_0 to DialogNode(
                text = listOf(StringId.ObtainDragonStage25Dialog0),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage25Answer2, // Choose cat
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_1,
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage25Answer3, // Choose dog
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_5,
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage25Answer4, // Choose frog
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_8,
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage25Answer5, // Choose bober
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_10,
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage25Answer7, // Let me think
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_1 to DialogNode(
                text = listOf(StringId.ObtainDragonStage25Dialog1),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage25Answer6,
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_2,
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage25Answer7,
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_2 to DialogNode(
                text = listOf(StringId.ObtainDragonStage25Dialog2),
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage25Answer8,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            // User choose to sacrifice Catus, the rest will be told by Dogus
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_CAT_IS_SACRIFICE_KEY] = true
                            }
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_3 to DialogNode(
                // Dogus tells the story of Catus sacrifice
                text = listOf(StringId.ObtainDragonStage25Dialog3),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_4,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_4 to DialogNode(
                text = listOf(StringId.ObtainDragonStage25Dialog4),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            // Move Catus to memory
                            val store = questSystem.dataStore.data.first()
                            val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                            catId?.let { id ->
                                questSystem.petsRepo.getPetByIdFlow(id).first()?.let { cat ->
                                    val newCat = cat.copy(
                                        questName = null,
                                        place = Place.Memory,
                                    )
                                    questSystem.petsRepo.updatePet(newCat)
                                }
                            }
                            // Add memory of a mage to inventory
                            userStats.addInventoryItem(
                                InventoryItem(
                                    id = InventoryItemId.MemoryOfMage,
                                    amount = 1,
                                )
                            )

                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_5 to DialogNode(
                text = listOf(StringId.ObtainDragonStage25Dialog1), // Same = Are you sure?
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage25Answer6, // Same = Yes
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_6,
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage25Answer7, // Same = No
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_6 to DialogNode(
                // Catus tells the story of Dogus sacrifice
                text = listOf(StringId.ObtainDragonStage25Dialog6),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_7,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_7 to DialogNode(
                text = listOf(StringId.ObtainDragonStage25Dialog4), // Same
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            // Move Dogus to memory
                            val store = questSystem.dataStore.data.first()
                            val dogId = store[OBTAIN_DRAGON_DOGUS_ID_KEY]
                            dogId?.let { id ->
                                questSystem.petsRepo.getPetByIdFlow(id).first()?.let { dog ->
                                    val newDog = dog.copy(
                                        questName = null,
                                        place = Place.Memory,
                                    )
                                    questSystem.petsRepo.updatePet(newDog)
                                }
                            }
                            // Add memory of a warrior to inventory
                            userStats.addInventoryItem(
                                InventoryItem(
                                    id = InventoryItemId.MemoryOfWarrior,
                                    amount = 1,
                                )
                            )

                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_8 to DialogNode(
                // Catus tells the story of Frogus sacrifice
                text = listOf(StringId.ObtainDragonStage25Dialog8),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_9,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_9 to DialogNode(
                text = listOf(StringId.ObtainDragonStage25Dialog4), // Same
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            // Move Frogus to memory
                            val store = questSystem.dataStore.data.first()
                            val frogId = store[OBTAIN_DRAGON_FROGUS_ID_KEY]
                            frogId?.let { id ->
                                questSystem.petsRepo.getPetByIdFlow(id).first()?.let { frog ->
                                    val newFrog = frog.copy(
                                        questName = null,
                                        place = Place.Memory,
                                    )
                                    questSystem.petsRepo.updatePet(newFrog)
                                }
                            }
                            // Add memory of a bard to inventory
                            userStats.addInventoryItem(
                                InventoryItem(
                                    id = InventoryItemId.MemoryOfBard,
                                    amount = 1,
                                )
                            )

                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_10 to DialogNode(
                // Catus tells the story of Bober sacrifice
                text = listOf(StringId.ObtainDragonStage25Dialog10),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_11,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_11 to DialogNode(
                text = listOf(StringId.ObtainDragonStage25Dialog4), // Same
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            // Move Bober to memory
                            val store = questSystem.dataStore.data.first()
                            val boberId = store[OBTAIN_DRAGON_BOBER_ID_KEY]
                            boberId?.let { id ->
                                questSystem.petsRepo.getPetByIdFlow(id).first()?.let { bober ->
                                    val newBober = bober.copy(
                                        questName = null,
                                        place = Place.Memory,
                                    )
                                    questSystem.petsRepo.updatePet(newBober)
                                }
                            }
                            // Add memory of a smith to inventory
                            userStats.addInventoryItem(
                                InventoryItem(
                                    id = InventoryItemId.MemoryOfSmith,
                                    amount = 1,
                                )
                            )

                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_12 to DialogNode(
                text = listOf(StringId.ObtainDragonStage25Dialog1), // Same = Are you sure?
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage25Answer6, // Same = Yes
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_13,
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage25Answer7, // Same = No
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_13 to DialogNode(
                // Catus tells the story of his escape and how everyone else died
                // About dogus
                text = listOf(StringId.ObtainDragonStage25Dialog13),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_14,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_14 to DialogNode(
                // Catus tells the story of his escape and how everyone else died
                // About frogus
                text = listOf(StringId.ObtainDragonStage25Dialog14),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_15,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_15 to DialogNode(
                // Catus tells the story of his escape and how everyone else died
                // About bober
                text = listOf(StringId.ObtainDragonStage25Dialog15),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_16,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_16 to DialogNode(
                // Catus tells the story of his escape and how everyone else died
                // Final
                text = listOf(StringId.ObtainDragonStage25Dialog16),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val store = questSystem.dataStore.data.first()
                            // Move Dogus to memory
                            val dogId = store[OBTAIN_DRAGON_DOGUS_ID_KEY]
                            dogId?.let { id ->
                                questSystem.petsRepo.getPetByIdFlow(id).first()?.let { dog ->
                                    val newDog = dog.copy(
                                        questName = null,
                                        place = Place.Memory,
                                    )
                                    questSystem.petsRepo.updatePet(newDog)
                                }
                            }
                            // Add memory of a warrior to inventory
                            userStats.addInventoryItem(
                                InventoryItem(
                                    id = InventoryItemId.MemoryOfWarrior,
                                    amount = 1,
                                )
                            )
                            // Move Frogus to memory
                            val frogId = store[OBTAIN_DRAGON_FROGUS_ID_KEY]
                            frogId?.let { id ->
                                questSystem.petsRepo.getPetByIdFlow(id).first()?.let { frog ->
                                    val newFrog = frog.copy(
                                        questName = null,
                                        place = Place.Memory,
                                    )
                                    questSystem.petsRepo.updatePet(newFrog)
                                }
                            }
                            // Add memory of a bard to inventory
                            userStats.addInventoryItem(
                                InventoryItem(
                                    id = InventoryItemId.MemoryOfBard,
                                    amount = 1,
                                )
                            )
                            // Move Bober to memory
                            val boberId = store[OBTAIN_DRAGON_BOBER_ID_KEY]
                            boberId?.let { id ->
                                questSystem.petsRepo.getPetByIdFlow(id).first()?.let { bober ->
                                    val newBober = bober.copy(
                                        questName = null,
                                        place = Place.Memory,
                                    )
                                    questSystem.petsRepo.updatePet(newBober)
                                }
                            }
                            // Add memory of a smith to inventory
                            userStats.addInventoryItem(
                                InventoryItem(
                                    id = InventoryItemId.MemoryOfSmith,
                                    amount = 1,
                                )
                            )

                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_17 to DialogNode(
                text = listOf(StringId.ObtainDragonStage25Dialog1), // Same = Are you sure?
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage25Answer6, // Same = Yes
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_18,
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage25Answer7, // Same = No
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_18 to DialogNode(
                // Dogus tells the story of his escape and how everyone else died
                // About catus
                text = listOf(StringId.ObtainDragonStage25Dialog18),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_19,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_19 to DialogNode(
                // Dogus tells the story of his escape and how everyone else died
                // About frogus
                text = listOf(StringId.ObtainDragonStage25Dialog14),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_20,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_20 to DialogNode(
                // Dogus tells the story of his escape and how everyone else died
                // About bober
                text = listOf(StringId.ObtainDragonStage25Dialog15),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_21,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_21 to DialogNode(
                // Dogus tells the story of his escape and how everyone else died
                // Final
                text = listOf(StringId.ObtainDragonStage25Dialog16),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val store = questSystem.dataStore.data.first()
                            // Move Catus to memory
                            val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                            catId?.let { id ->
                                questSystem.petsRepo.getPetByIdFlow(id).first()?.let { cat ->
                                    val newCat = cat.copy(
                                        questName = null,
                                        place = Place.Memory,
                                    )
                                    questSystem.petsRepo.updatePet(newCat)
                                }
                            }
                            // Add memory of a mage to inventory
                            userStats.addInventoryItem(
                                InventoryItem(
                                    id = InventoryItemId.MemoryOfMage,
                                    amount = 1,
                                )
                            )
                            // Move Frogus to memory
                            val frogId = store[OBTAIN_DRAGON_FROGUS_ID_KEY]
                            frogId?.let { id ->
                                questSystem.petsRepo.getPetByIdFlow(id).first()?.let { frog ->
                                    val newFrog = frog.copy(
                                        questName = null,
                                        place = Place.Memory,
                                    )
                                    questSystem.petsRepo.updatePet(newFrog)
                                }
                            }
                            // Add memory of a bard to inventory
                            userStats.addInventoryItem(
                                InventoryItem(
                                    id = InventoryItemId.MemoryOfBard,
                                    amount = 1,
                                )
                            )
                            // Move Bober to memory
                            val boberId = store[OBTAIN_DRAGON_BOBER_ID_KEY]
                            boberId?.let { id ->
                                questSystem.petsRepo.getPetByIdFlow(id).first()?.let { bober ->
                                    val newBober = bober.copy(
                                        questName = null,
                                        place = Place.Memory,
                                    )
                                    questSystem.petsRepo.updatePet(newBober)
                                }
                            }
                            // Add memory of a smith to inventory
                            userStats.addInventoryItem(
                                InventoryItem(
                                    id = InventoryItemId.MemoryOfSmith,
                                    amount = 1,
                                )
                            )

                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_22 to DialogNode(
                text = listOf(StringId.ObtainDragonStage25Dialog1), // Same = Are you sure?
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage25Answer6, // Same = Yes
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_23,
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage25Answer7, // Same = No
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_23 to DialogNode(
                // Frogus tells the story of his escape and how everyone else died
                // About catus
                text = listOf(StringId.ObtainDragonStage25Dialog18),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_24,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_24 to DialogNode(
                // Frogus tells the story of his escape and how everyone else died
                // About dogus
                text = listOf(StringId.ObtainDragonStage25Dialog13),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_25,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_25 to DialogNode(
                // Frogus tells the story of his escape and how everyone else died
                // About bober
                text = listOf(StringId.ObtainDragonStage25Dialog15),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_26,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_26 to DialogNode(
                // Frogus tells the story of his escape and how everyone else died
                // Final
                text = listOf(StringId.ObtainDragonStage25Dialog16),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val store = questSystem.dataStore.data.first()
                            // Move Catus to memory
                            val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                            catId?.let { id ->
                                questSystem.petsRepo.getPetByIdFlow(id).first()?.let { cat ->
                                    val newCat = cat.copy(
                                        questName = null,
                                        place = Place.Memory,
                                    )
                                    questSystem.petsRepo.updatePet(newCat)
                                }
                            }
                            // Add memory of a mage to inventory
                            userStats.addInventoryItem(
                                InventoryItem(
                                    id = InventoryItemId.MemoryOfMage,
                                    amount = 1,
                                )
                            )
                            // Move Dogus to memory
                            val dogId = store[OBTAIN_DRAGON_DOGUS_ID_KEY]
                            dogId?.let { id ->
                                questSystem.petsRepo.getPetByIdFlow(id).first()?.let { dog ->
                                    val newDog = dog.copy(
                                        questName = null,
                                        place = Place.Memory,
                                    )
                                    questSystem.petsRepo.updatePet(newDog)
                                }
                            }
                            // Add memory of a warrior to inventory
                            userStats.addInventoryItem(
                                InventoryItem(
                                    id = InventoryItemId.MemoryOfWarrior,
                                    amount = 1,
                                )
                            )
                            // Move Bober to memory
                            val boberId = store[OBTAIN_DRAGON_BOBER_ID_KEY]
                            boberId?.let { id ->
                                questSystem.petsRepo.getPetByIdFlow(id).first()?.let { bober ->
                                    val newBober = bober.copy(
                                        questName = null,
                                        place = Place.Memory,
                                    )
                                    questSystem.petsRepo.updatePet(newBober)
                                }
                            }
                            // Add memory of a smith to inventory
                            userStats.addInventoryItem(
                                InventoryItem(
                                    id = InventoryItemId.MemoryOfSmith,
                                    amount = 1,
                                )
                            )

                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_27 to DialogNode(
                text = listOf(StringId.ObtainDragonStage25Dialog1), // Same = Are you sure?
                answers = listOf(
                    Answer(
                        text = StringId.ObtainDragonStage25Answer6, // Same = Yes
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_28,
                    ),
                    Answer(
                        text = StringId.ObtainDragonStage25Answer7, // Same = No
                        nextNode = null,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_28 to DialogNode(
                // Bober tells the story of his escape and how everyone else died
                // About catus
                text = listOf(StringId.ObtainDragonStage25Dialog18),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_29,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_29 to DialogNode(
                // Bober tells the story of his escape and how everyone else died
                // About dogus
                text = listOf(StringId.ObtainDragonStage25Dialog13),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_30,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_30 to DialogNode(
                // Bober tells the story of his escape and how everyone else died
                // About frogus
                text = listOf(StringId.ObtainDragonStage25Dialog14),
                answers = listOf(
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = OBTAIN_DRAGON_STAGE_25_DIALOG_31,
                    ),
                )
            ),
            OBTAIN_DRAGON_STAGE_25_DIALOG_31 to DialogNode(
                // Bober tells the story of his escape and how everyone else died
                // Final
                text = listOf(StringId.ObtainDragonStage25Dialog16),
                answers = listOf(
                    Answer(
                        text = StringId.AllRight,
                        nextNode = null,
                        action = { questSystem, userStats, pet ->
                            val store = questSystem.dataStore.data.first()
                            // Move Catus to memory
                            val catId = store[OBTAIN_DRAGON_CATUS_ID_KEY]
                            catId?.let { id ->
                                questSystem.petsRepo.getPetByIdFlow(id).first()?.let { cat ->
                                    val newCat = cat.copy(
                                        questName = null,
                                        place = Place.Memory,
                                    )
                                    questSystem.petsRepo.updatePet(newCat)
                                }
                            }
                            // Add memory of a mage to inventory
                            userStats.addInventoryItem(
                                InventoryItem(
                                    id = InventoryItemId.MemoryOfMage,
                                    amount = 1,
                                )
                            )
                            // Move Dogus to memory
                            val dogId = store[OBTAIN_DRAGON_DOGUS_ID_KEY]
                            dogId?.let { id ->
                                questSystem.petsRepo.getPetByIdFlow(id).first()?.let { dog ->
                                    val newDog = dog.copy(
                                        questName = null,
                                        place = Place.Memory,
                                    )
                                    questSystem.petsRepo.updatePet(newDog)
                                }
                            }
                            // Add memory of a warrior to inventory
                            userStats.addInventoryItem(
                                InventoryItem(
                                    id = InventoryItemId.MemoryOfWarrior,
                                    amount = 1,
                                )
                            )
                            // Move Frogus to memory
                            val frogId = store[OBTAIN_DRAGON_FROGUS_ID_KEY]
                            frogId?.let { id ->
                                questSystem.petsRepo.getPetByIdFlow(id).first()?.let { frog ->
                                    val newFrog = frog.copy(
                                        questName = null,
                                        place = Place.Memory,
                                    )
                                    questSystem.petsRepo.updatePet(newFrog)
                                }
                            }
                            // Add memory of a bard to inventory
                            userStats.addInventoryItem(
                                InventoryItem(
                                    id = InventoryItemId.MemoryOfBard,
                                    amount = 1,
                                )
                            )

                            val now = getTimestampSecondsSinceEpoch()
                            questSystem.dataStore.edit { store ->
                                store[OBTAIN_DRAGON_TIMESTAMP_KEY] = now
                            }
                            questSystem.setQuestStageToNext(QuestSystem.QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                )
            ),
            QUEST_RESET_DIALOG_0 to DialogNode(
                text = listOf(StringId.QuestResetDialog0),
                answers = listOf(
                    Answer(
                        text = StringId.QuestNameNecronomicon,
                        nextNode = QUEST_RESET_DIALOG_1,
                        action = { questSystem, userStats, pet ->
                            // Reset necronomicon quest
                            questSystem.resetQuest(QUEST_NECRONOMICON)
                        }
                    ),
                    Answer(
                        text = StringId.QuestNameObtainFrogus,
                        nextNode = QUEST_RESET_DIALOG_1,
                        action = { questSystem, userStats, pet ->
                            // Reset obtain frogus quest
                            questSystem.resetQuest(QUEST_TO_OBTAIN_FROGUS)
                        }
                    ),
                    Answer(
                        text = StringId.QuestNameObtainBober,
                        nextNode = QUEST_RESET_DIALOG_1,
                        action = { questSystem, userStats, pet ->
                            // Reset obtain bober quest
                            questSystem.resetQuest(QUEST_TO_OBTAIN_BOBER)
                        }
                    ),
                    Answer(
                        text = StringId.Ellipsis,
                        nextNode = QUEST_RESET_DIALOG_2,
                    ),
                    Answer(
                        text = StringId.QuestResetAnswer1,
                        nextNode = null,
                    ),
                )
            ),
            QUEST_RESET_DIALOG_1 to DialogNode(
                text = listOf(StringId.QuestResetDialog1),
                answers = listOf(
                    Answer(
                        text = StringId.Thanks,
                        nextNode = null,
                    ),
                )
            ),
            QUEST_RESET_DIALOG_2 to DialogNode(
                text = listOf(StringId.QuestResetDialog0),
                answers = listOf(
                    Answer(
                        text = StringId.QuestNameObtainFractal,
                        nextNode = QUEST_RESET_DIALOG_1,
                        action = { questSystem, userStats, pet ->
                            // Reset obtain fractal quest
                            questSystem.resetQuest(QUEST_TO_OBTAIN_FRACTAL)
                        }
                    ),
                    Answer(
                        text = StringId.QuestNameMeditation,
                        nextNode = QUEST_RESET_DIALOG_1,
                        action = { questSystem, userStats, pet ->
                            // Reset meditation quest
                            questSystem.resetQuest(QUEST_MEDITATION)
                        }
                    ),
                    Answer(
                        text = StringId.QuestNameObtainDragon,
                        nextNode = QUEST_RESET_DIALOG_1,
                        action = { questSystem, userStats, pet ->
                            // Reset obtain dragon quest
                            questSystem.resetQuest(QUEST_TO_OBTAIN_DRAGON)
                        }
                    ),
                    Answer(
                        text = StringId.QuestResetAnswer1,
                        nextNode = null,
                    ),
                )
            ),
        )
    }
}