package bav.petus.viewModel.petCreation

import bav.petus.base.ViewModelWithNavigation
import bav.petus.core.engine.Engine
import bav.petus.core.engine.UserStats
import bav.petus.core.resources.StringId
import bav.petus.model.PetType
import bav.petus.repo.PetsRepository
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class PetCreationUiState(
    val name: String,
    val type: PetType,
    val typeDescription: StringId,
    val availablePetTypes: List<PetType>,
)

class PetCreationScreenViewModel
    : ViewModelWithNavigation<PetCreationScreenViewModel.Navigation>(), KoinComponent {

    private val engine: Engine by inject()
    private val userStats: UserStats by inject()
    private val petsRepo: PetsRepository by inject()

    private val _uiState = MutableStateFlow<PetCreationUiState?>(viewModelScope, null)
    val uiState = _uiState.asStateFlow()

    private var petCreationInProgress: Boolean = false

    init {
        viewModelScope.launch {
            val nonFractalsInZoo = petsRepo.getAllPetsInZoo().filter { it.type != PetType.Fractal }
            val zooSize = userStats.getUserProfileFlow().first().zooSize
            val allAvailablePetTypes = userStats.getAvailablePetTypes()
            val availablePetTypes = if (nonFractalsInZoo.size < zooSize) {
                allAvailablePetTypes
            } else {
                allAvailablePetTypes.filter { it == PetType.Fractal }
            }
            val petTypes = availablePetTypes.toList()
            _uiState.value = PetCreationUiState(
                name = "",
                type = petTypes.first(),
                typeDescription = engine.getPetTypeDescription(petTypes.first()),
                availablePetTypes = availablePetTypes.toList(),
            )
        }
    }

    fun onAction(action: Action) {
        when (action) {
            Action.TapCreateButton -> {
                _uiState.value?.let {
                    if (it.name.isNotBlank()) {
                        if (!petCreationInProgress) {
                            petCreationInProgress = true
                            viewModelScope.launch {
                                engine.createNewPet(
                                    name = it.name,
                                    type = it.type,
                                )
                                navigate(Navigation.PetCreationSuccess)
                            }
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
            is Action.GetRandomName -> {
                val randomName = engine.getRandomName()
                updateUiState(name = randomName)
            }
        }
    }

    private fun updateUiState(
        name: String? = null,
        type: PetType? = null,
    ) {
        _uiState.value?.let { state ->
            _uiState.value = state.copy(
                name = name ?: state.name,
                type = type ?: state.type,
                typeDescription = type?.let { t -> engine.getPetTypeDescription(t) } ?: state.typeDescription,
            )
        }
    }

    sealed interface Action {
        data class UpdateName(val value: String) : Action
        data class UpdateType(val value: PetType) : Action
        data object GetRandomName : Action
        data object TapCreateButton : Action
    }

    sealed interface Navigation {
        data object CloseScreen : Navigation
        data object PetCreationSuccess : Navigation
    }
}