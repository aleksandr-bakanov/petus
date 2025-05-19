package bav.petus.viewModel.dialog

import bav.petus.base.ViewModelWithNavigation
import bav.petus.core.dialog.DialogNode
import bav.petus.core.dialog.DialogSystem
import bav.petus.core.resources.StringId
import bav.petus.repo.PetsRepository
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import bav.petus.model.Pet
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

data class DialogScreenViewModelArgs(
    val petId: Long,
    val convertStringIdToString: (StringId) -> String,
)

data class DialogScreenUiState(
    val text: String,
    val answers: List<String>,
)

class DialogScreenViewModel(
    private val args: DialogScreenViewModelArgs,
) : ViewModelWithNavigation<DialogScreenViewModel.Navigation>(), KoinComponent {

    private val dialogSystem: DialogSystem by inject()
    private val petsRepo: PetsRepository by inject()

    private var pet: Pet? = null

    private val _uiState = MutableStateFlow<DialogScreenUiState?>(viewModelScope, null)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            pet = petsRepo.getPetByIdFlow(args.petId).first()
            pet?.let { p ->
                val node = dialogSystem.startDialog(p)
                _uiState.value = makeDialogUiState(node)
            }
        }
    }

    fun onAction(action: Action) {
        when (action) {
            is Action.ChooseDialogAnswer -> {
                viewModelScope.launch {
                    val node = dialogSystem.chooseAnswer(action.index)
                    if (node != null) {
                        _uiState.value = makeDialogUiState(node)
                    } else {
                        navigate(Navigation.CloseScreen)
                    }
                }
            }
        }
    }

    private suspend fun makeDialogUiState(node: DialogNode?): DialogScreenUiState? {
        return node?.let { n ->
            DialogScreenUiState(
                text = dialogSystem.censorDialogText(
                    petType = pet!!.type,
                    text = args.convertStringIdToString(n.text),
                ),
                answers = n.answers.map {
                    args.convertStringIdToString(it.text)
                }
            )
        }
    }

    sealed interface Action {
        data class ChooseDialogAnswer(val index: Int) : Action
    }

    sealed interface Navigation {
        data object CloseScreen : Navigation
    }
}