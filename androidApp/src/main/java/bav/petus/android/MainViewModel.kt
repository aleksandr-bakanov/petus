package bav.petus.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bav.petus.android.ui.common.UiState
import bav.petus.repo.PetsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MainScreenUiState(
    val showCemetery: Boolean?,
)

class MainViewModel(
    petsRepo: PetsRepository,
): ViewModel() {

    private val deadPetsFlow: StateFlow<Boolean?> = petsRepo.getAllDeadPetsFlow()
        .map { deadPets -> deadPets.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val uiState: StateFlow<UiState<MainScreenUiState>> = deadPetsFlow
        .map { newValue ->
            val newState = if (newValue == null)
                UiState.Initial
            else
                UiState.Success(MainScreenUiState(showCemetery = newValue))

            newState
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Initial)
}