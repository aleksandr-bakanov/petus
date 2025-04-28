package bav.petus.android.ui.pet_details

import androidx.lifecycle.viewModelScope
import bav.petus.android.base.ViewModelWithNavigation
import bav.petus.android.ui.common.PetImageUseCase
import bav.petus.android.ui.common.UiState
import bav.petus.core.engine.Engine
import bav.petus.extension.epochTimeToString
import bav.petus.model.Pet
import bav.petus.model.PetType
import bav.petus.repo.PetsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PetDetailsUiState(
    val title: String,
    val petType: PetType,
    val petImageResId: Int,
    val creationTime: String,
    val ageState: String,
    val sleepState: String,
    val satiety: String,
    val satietyFraction: Float,
    val psych: String,
    val psychFraction: Float,
    val health: String,
    val healthFraction: Float,
    val illness: String,
    val pooped: String,
    val timeOfDeath: String,
    val showPlayButton: Boolean,
    val showHealButton: Boolean,
    val showPoopButton: Boolean,
    val showFeedButton: Boolean,
    val showWakeUpButton: Boolean,
) {
    val isAnyButtonShown: Boolean
        get() = showPlayButton || showHealButton || showPoopButton || showFeedButton || showWakeUpButton
}

data class PetDetailsScreenViewModelArgs(
    val petId: Long,
)

class PetDetailsScreenViewModel(
    private val petsRepo: PetsRepository,
    private val engine: Engine,
    private val petImageUseCase: PetImageUseCase,
    private val args: PetDetailsScreenViewModelArgs,
) : ViewModelWithNavigation<PetDetailsScreenViewModel.Navigation>() {

    val uiState: StateFlow<UiState<PetDetailsUiState>> = petsRepo.getPetByIdFlow(args.petId)
        .map { pet ->
            if (pet != null) {
                currentPet = pet
                UiState.Success(
                    data = PetDetailsUiState(
                        title = "${pet.type} ${pet.name}",
                        petType = pet.type,
                        petImageResId = petImageUseCase.getPetImageResId(pet),
                        creationTime = "Born: ${pet.creationTime.epochTimeToString()}",
                        ageState = "Age state: ${pet.ageState.name}",
                        sleepState = getSleepActiveStateString(pet),
                        satiety = "Satiety: ${pet.satiety}",
                        satietyFraction = engine.getPetSatietyFraction(pet),
                        psych = "Psych: ${pet.psych}",
                        psychFraction = engine.getPetPsychFraction(pet),
                        health = "Health: ${pet.health}",
                        healthFraction = engine.getPetHealthFraction(pet),
                        illness = "Illness: ${pet.illness}",
                        pooped = "Pooped: ${pet.isPooped}",
                        timeOfDeath = "Time of death: ${pet.timeOfDeath.epochTimeToString()}",
                        showPlayButton = engine.isAllowedToPlayWithPet(pet),
                        showHealButton = engine.isAllowedToHealPet(pet),
                        showPoopButton = engine.isAllowedToCleanAfterPet(pet),
                        showFeedButton = engine.isAllowedToFeedPet(pet),
                        showWakeUpButton = engine.isAllowedToWakeUpPet(pet),
                    )
                )
            }
            else {
                UiState.Failure()
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Initial)

    private lateinit var currentPet: Pet

    fun onAction(action: Action) {
        when (action) {
            Action.TapFeedButton -> {
                viewModelScope.launch {
                    engine.feedPet(currentPet)
                }
            }
            Action.TapHealButton -> {
                viewModelScope.launch {
                    engine.healPetIllness(currentPet)
                }
            }
            Action.TapPlayButton -> {
                viewModelScope.launch {
                    engine.playWithPet(currentPet)
                }
            }
            Action.TapPoopButton -> {
                viewModelScope.launch {
                    engine.cleanAfterPet(currentPet)
                }
            }
            Action.TapWakeUpButton -> {
                viewModelScope.launch {
                    engine.wakeUpPet(currentPet)
                }
            }
        }
    }

    private fun getSleepActiveStateString(pet: Pet): String {
        return buildString {
            append("Sleep or active: ")
            append(pet.activeSleepState.name)
            if (pet.sleep)
                append(" (will awake at ")
            else
                append(" (will fall asleep at ")
            append(engine.getNextSleepStateChangeTimestamp(pet).epochTimeToString())
            append(")")
        }
    }

    sealed interface Action {
        data object TapPlayButton : Action
        data object TapHealButton : Action
        data object TapPoopButton : Action
        data object TapFeedButton : Action
        data object TapWakeUpButton : Action
    }

    sealed interface Navigation {
        data object CloseScreen : Navigation
    }
}
