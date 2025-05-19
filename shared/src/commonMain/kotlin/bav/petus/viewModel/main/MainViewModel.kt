package bav.petus.viewModel.main

import bav.petus.base.ViewModelWithNavigation
import bav.petus.repo.PetsRepository
import com.rickclephas.kmp.observableviewmodel.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class MainScreenUiState(
    val showCemetery: Boolean?,
)

class MainViewModel : ViewModelWithNavigation<MainViewModel.Navigation>(), KoinComponent {

    private val petsRepo: PetsRepository by inject()

    private val deadPetsFlow: StateFlow<Boolean?> = petsRepo.getAllPetsInCemeteryFlow()
        .map { deadPets -> deadPets.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val uiState: StateFlow<MainScreenUiState?> = deadPetsFlow
        .map { newValue ->
            val newState = if (newValue == null)
                null
            else
                MainScreenUiState(showCemetery = newValue)

            newState
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    sealed interface Navigation {

    }
}