package bav.petus.android.ui.pet_creation

import androidx.lifecycle.viewModelScope
import bav.petus.android.base.ViewModelWithNavigation
import bav.petus.android.ui.common.StringResourcesUseCase
import bav.petus.android.ui.common.UiState
import bav.petus.core.engine.Engine
import bav.petus.core.engine.UserStats
import bav.petus.model.PetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PetCreationUiState(
    val name: String,
    val type: PetType,
    val typeDescription: String,
    val availablePetTypes: Set<PetType>,
)

class PetCreationScreenViewModel(
    private val engine: Engine,
    private val userStats: UserStats,
    private val stringResourcesUseCase: StringResourcesUseCase,
) : ViewModelWithNavigation<PetCreationScreenViewModel.Navigation>() {

    private val _uiState = MutableStateFlow<UiState<PetCreationUiState>>(UiState.Initial)
    val uiState: StateFlow<UiState<PetCreationUiState>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val availablePetTypes = userStats.getAvailablePetTypes()
            _uiState.value = UiState.Success(
                data = PetCreationUiState(
                    name = "",
                    type = PetType.Dogus,
                    typeDescription = stringResourcesUseCase.getString(engine.getPetTypeDescription(PetType.Dogus)),
                    availablePetTypes = availablePetTypes,
                )
            )
        }
    }

    fun onAction(action: Action) {
        when (action) {
            Action.TapCreateButton -> {
                (_uiState.value as? UiState.Success)?.let {
                    if (it.data.name.isNotBlank()) {
                        viewModelScope.launch {
                            engine.createNewPet(
                                name = it.data.name,
                                type = it.data.type,
                            )
                            navigate(Navigation.PetCreationSuccess)
                        }
                    }
                }
            }
            is Action.UpdateName -> {
                updateUiState(name = action.value)
            }
            is Action.UpdateType -> {
                updateUiState(type = action.value)
            }
        }
    }

    private fun updateUiState(
        name: String? = null,
        type: PetType? = null,
    ) {
        (_uiState.value as? UiState.Success)?.let {
            val data = it.data
            _uiState.update {
                UiState.Success(
                    data.copy(
                        name = name ?: data.name,
                        type = type ?: data.type,
                        typeDescription = type?.let {
                            t -> stringResourcesUseCase.getString(engine.getPetTypeDescription(t))
                        } ?: data.typeDescription,
                    )
                )
            }
        }
    }

    sealed interface Action {
        data class UpdateName(val value: String) : Action
        data class UpdateType(val value: PetType) : Action
        data object TapCreateButton : Action
    }

    sealed interface Navigation {
        data object CloseScreen : Navigation
        data object PetCreationSuccess : Navigation
    }
}