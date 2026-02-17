package bav.petus.viewModel.dialog

import bav.petus.base.ViewModelWithNavigation
import bav.petus.core.dialog.DialogNode
import bav.petus.core.dialog.DialogSystem
import bav.petus.core.dialog.Zalgoize
import bav.petus.core.dialog.Zalgoize.Range
import bav.petus.core.dialog.Zalgoize.ZalgoLevel
import bav.petus.core.resources.ImageId
import bav.petus.core.resources.StringId
import bav.petus.repo.PetsRepository
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import bav.petus.model.Pet
import bav.petus.model.PetType
import bav.petus.useCase.PetImageUseCase
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

data class DialogScreenViewModelArgs(
    val petId: Long,
    val convertStringIdToString: (StringId) -> String,
)

data class DialogMessage(
    val isImageAtStart: Boolean,
    val imageId: ImageId,
    val text: String,
)

data class DialogScreenUiState(
    val messages: List<DialogMessage>,
    val answers: List<String>,
)

class DialogScreenViewModel(
    private val args: DialogScreenViewModelArgs,
) : ViewModelWithNavigation<DialogScreenViewModel.Navigation>(), KoinComponent {

    private val dialogSystem: DialogSystem by inject()
    private val petsRepo: PetsRepository by inject()
    private val petImageUseCase: PetImageUseCase by inject()

    private var pet: Pet? = null

    private val _uiState = MutableStateFlow<DialogScreenUiState?>(viewModelScope, null)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            pet = petsRepo.getPetByIdFlow(args.petId).first()
            pet?.let { p ->
                val node = dialogSystem.startDialog(p)
                if (node != null) {
                    _uiState.value = makeDialogUiState(
                        currentMessages = emptyList(),
                        node = node,
                        answer = null,
                    )
                } else {
                    navigate(Navigation.CloseScreen)
                }
            }
        }
    }

    fun onAction(action: Action) {
        when (action) {
            is Action.ChooseDialogAnswer -> {
                viewModelScope.launch {
                    val node = dialogSystem.chooseAnswer(action.index)
                    if (node != null) {
                        _uiState.update { state ->
                            makeDialogUiState(
                                currentMessages = state?.messages ?: emptyList(),
                                node = node,
                                answer = state?.answers?.get(action.index),
                            )
                        }
                    } else {
                        navigate(Navigation.CloseScreen)
                    }
                }
            }
        }
    }

    /**
     * Initially introduced to zalgoize alien messages.
     */
    private fun processTextByPetType(
        text: String,
        petType: PetType,
    ): String {
        return when (petType) {
            PetType.Alien -> Zalgoize.encode(
                text = text,
                level = ZalgoLevel(Range(0, 3), Range(0, 1), Range(0, 3)),
                directions = listOf("up", "middle", "down"),
                ignoreChars = setOf(DialogSystem.MASK_SYMBOL)
            )
            else -> text
        }
    }

    private suspend fun makeDialogUiState(
        currentMessages: List<DialogMessage>,
        node: DialogNode,
        answer: String?,
    ): DialogScreenUiState {
        val petMessage = DialogMessage(
            isImageAtStart = true,
            imageId = petImageUseCase.getPetImageId(pet!!),
            text = processTextByPetType(
                text = dialogSystem.censorDialogText(
                    petType = pet!!.type,
                    text = node.text.joinToString(" ") {
                        args.convertStringIdToString(it)
                    },
                ),
                petType = pet!!.type,
            )
        )
        val userMessage: DialogMessage? = answer?.let {
            DialogMessage(
                isImageAtStart = false,
                imageId = ImageId.UserProfileAvatar,
                text = it,
            )
        }
        val newMessages = listOfNotNull(petMessage, userMessage) + currentMessages

        return DialogScreenUiState(
            messages = newMessages,
            answers = node.answers.map {
                args.convertStringIdToString(it.text)
            }
        )
    }

    sealed interface Action {
        data class ChooseDialogAnswer(val index: Int) : Action
    }

    sealed interface Navigation {
        data object CloseScreen : Navigation
    }
}