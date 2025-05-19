package bav.petus.viewModel.petDetails

import bav.petus.base.ViewModelWithNavigation
import bav.petus.core.engine.Engine
import bav.petus.core.engine.QuestSystem
import bav.petus.core.resources.ImageId
import bav.petus.extension.epochTimeToString
import bav.petus.model.AgeState
import bav.petus.model.Pet
import bav.petus.model.PetType
import bav.petus.model.Place
import bav.petus.repo.PetsRepository
import bav.petus.useCase.PetImageUseCase
import com.rickclephas.kmp.observableviewmodel.launch
import com.rickclephas.kmp.observableviewmodel.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class PetDetailsUiState(
    val title: String,
    val petType: PetType,
    val petImageResId: ImageId,
    val creationTime: String,
    val satietyFraction: Float,
    val psychFraction: Float,
    val healthFraction: Float,
    val timeOfDeath: String,
    val showPlayButton: Boolean,
    val showHealButton: Boolean,
    val showPoopButton: Boolean,
    val showFeedButton: Boolean,
    val showWakeUpButton: Boolean,
    val showBuryButton: Boolean,
    val showSpeakButton: Boolean,
    val showResurrectButton: Boolean,
) {
    val isAnyButtonShown: Boolean
        get() = showPlayButton || showHealButton || showPoopButton || showFeedButton ||
                showWakeUpButton || showBuryButton || showSpeakButton || showResurrectButton
}

data class PetDetailsScreenViewModelArgs(
    val petId: Long,
)

class PetDetailsScreenViewModel(
    private val args: PetDetailsScreenViewModelArgs,
) : ViewModelWithNavigation<PetDetailsScreenViewModel.Navigation>(), KoinComponent {

    private val petsRepo: PetsRepository by inject()
    private val engine: Engine by inject()
    private val petImageUseCase: PetImageUseCase by inject()
    private val questSystem: QuestSystem by inject()

    val uiState: StateFlow<PetDetailsUiState?> = petsRepo.getPetByIdFlow(args.petId)
        .map { pet ->
            if (pet != null) {
                currentPet = pet
                PetDetailsUiState(
                    title = pet.name,
                    petType = pet.type,
                    petImageResId = petImageUseCase.getPetImageId(pet),
                    creationTime = "Born: ${pet.creationTime.epochTimeToString()}",
                    satietyFraction = engine.getPetSatietyFraction(pet),
                    psychFraction = engine.getPetPsychFraction(pet),
                    healthFraction = engine.getPetHealthFraction(pet),
                    timeOfDeath = "Time of death: ${pet.timeOfDeath.epochTimeToString()}",
                    showPlayButton = engine.isAllowedToPlayWithPet(pet),
                    showHealButton = engine.isAllowedToHealPet(pet),
                    showPoopButton = engine.isAllowedToCleanAfterPet(pet),
                    showFeedButton = engine.isAllowedToFeedPet(pet),
                    showWakeUpButton = engine.isAllowedToWakeUpPet(pet),
                    showBuryButton = engine.isAllowedToBuryPet(pet),
                    showSpeakButton = engine.isAllowedToSpeakWithPet(pet),
                    showResurrectButton = engine.isAllowedToResurrectPet(pet),
                )
            }
            else {
                null
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private var currentPet: Pet? = null
        set(value) {
            if (field == null && value != null) {
                viewModelScope.launch {
                    questSystem.onEvent(QuestSystem.Event.UserOpenPetDetails(value))
                }
            }
            field = value
        }

    fun onAction(action: Action) {
        when (action) {
            Action.TapFeedButton -> {
                viewModelScope.launch {
                    currentPet?.let { pet -> engine.feedPet(pet) }
                }
            }
            Action.TapHealButton -> {
                viewModelScope.launch {
                    currentPet?.let { pet -> engine.healPetIllness(pet) }
                }
            }
            Action.TapPlayButton -> {
                viewModelScope.launch {
                    currentPet?.let { pet -> engine.playWithPet(pet) }
                }
            }
            Action.TapPoopButton -> {
                viewModelScope.launch {
                    currentPet?.let { pet -> engine.cleanAfterPet(pet) }
                }
            }
            Action.TapWakeUpButton -> {
                viewModelScope.launch {
                    currentPet?.let { pet -> engine.wakeUpPet(pet) }
                }
            }
            Action.Kill -> {
                viewModelScope.launch {
                    currentPet?.let { pet -> engine.killPet(pet) }
                }
            }
            is Action.ChangeAgeState -> {
                viewModelScope.launch {
                    currentPet?.let { pet -> engine.changePetAgeState(pet, action.state) }
                }
            }
            Action.Resurrect -> {
                viewModelScope.launch {
                    currentPet?.let { pet -> engine.resurrectPet(pet) }
                }
            }
            is Action.ChangePlace -> {
                viewModelScope.launch {
                    currentPet?.let { pet -> engine.changePetPlace(pet, action.place) }
                }
            }
            Action.TapBuryButton -> {
                viewModelScope.launch {
                    currentPet?.let { pet ->
                        engine.buryPet(pet)
                        navigate(Navigation.CloseScreen)
                    }
                }
            }
            Action.TapSpeakButton -> {
                currentPet?.let { pet -> navigate(Navigation.OpenDialogScreen(pet.id)) }
            }
            Action.TapResurrectButton -> {
                viewModelScope.launch {
                    currentPet?.let { pet ->
                        engine.resurrectPetAsZombie(pet)
                        navigate(Navigation.CloseScreen)
                    }
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
        data object TapBuryButton : Action
        data object TapSpeakButton : Action
        data object TapResurrectButton : Action
        data object Kill : Action
        data object Resurrect : Action
        data class ChangePlace(val place: Place) : Action
        data class ChangeAgeState(val state: AgeState) : Action
    }

    sealed interface Navigation {
        data object CloseScreen : Navigation
        data class OpenDialogScreen(val petId: Long) : Navigation
    }
}
