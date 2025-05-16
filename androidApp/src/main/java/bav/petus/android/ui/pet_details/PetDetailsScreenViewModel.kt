package bav.petus.android.ui.pet_details

import androidx.lifecycle.viewModelScope
import bav.petus.android.base.ViewModelWithNavigation
import bav.petus.android.ui.common.PetImageUseCase
import bav.petus.android.ui.common.StringResourcesUseCase
import bav.petus.android.ui.common.UiState
import bav.petus.core.dialog.DialogNode
import bav.petus.core.dialog.DialogSystem
import bav.petus.core.engine.Engine
import bav.petus.core.engine.QuestSystem
import bav.petus.extension.epochTimeToString
import bav.petus.model.AgeState
import bav.petus.model.Pet
import bav.petus.model.PetType
import bav.petus.model.Place
import bav.petus.repo.PetsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PetDetailsUiState(
    val title: String,
    val petType: PetType,
    val petImageResId: Int,
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
) {
    val isAnyButtonShown: Boolean
        get() = showPlayButton || showHealButton || showPoopButton || showFeedButton || showWakeUpButton
}

data class PetDetailsScreenViewModelArgs(
    val petId: Long,
)

data class DialogData(
    val text: String,
    val answers: List<String>,
)

class PetDetailsScreenViewModel(
    private val petsRepo: PetsRepository,
    private val engine: Engine,
    private val petImageUseCase: PetImageUseCase,
    private val stringResourcesUseCase: StringResourcesUseCase,
    private val dialogSystem: DialogSystem,
    private val questSystem: QuestSystem,
    private val args: PetDetailsScreenViewModelArgs,
) : ViewModelWithNavigation<PetDetailsScreenViewModel.Navigation>() {

    val uiState: StateFlow<UiState<PetDetailsUiState>> = petsRepo.getPetByIdFlow(args.petId)
        .map { pet ->
            if (pet != null) {
                currentPet = pet
                UiState.Success(
                    data = PetDetailsUiState(
                        title = pet.name,
                        petType = pet.type,
                        petImageResId = petImageUseCase.getPetImageResId(pet),
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
                    )
                )
            }
            else {
                UiState.Failure()
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Initial)

    private val _dialogNodeFlow = MutableStateFlow<DialogData?>(null)
    val dialogNodeFlow = _dialogNodeFlow.asStateFlow()

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
            Action.StartDialog -> {
                viewModelScope.launch {
                    currentPet?.let { pet ->
                        val node = dialogSystem.startDialog(pet)
                        _dialogNodeFlow.value = makeDialogData(node)
                    }
                }
            }
            is Action.ChooseDialogAnswer -> {
                viewModelScope.launch {
                    val node = dialogSystem.chooseAnswer(action.index)
                    _dialogNodeFlow.value = makeDialogData(node)
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
        }
    }

    private suspend fun makeDialogData(node: DialogNode?): DialogData? {
        return node?.let { n ->
            DialogData(
                text = dialogSystem.maskDialogText(
                    petType = currentPet!!.type,
                    text = stringResourcesUseCase.getString(n.text),
                ),
                answers = n.answers.map {
                    stringResourcesUseCase.getString(it.text)
                }
            )
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
        data object Kill : Action
        data object Resurrect : Action
        data class ChangePlace(val place: Place) : Action
        data object StartDialog : Action
        data class ChooseDialogAnswer(val index: Int) : Action
        data class ChangeAgeState(val state: AgeState) : Action
    }

    sealed interface Navigation {
        data object CloseScreen : Navigation
    }
}
