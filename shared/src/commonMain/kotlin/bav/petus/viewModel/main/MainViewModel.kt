package bav.petus.viewModel.main

import bav.petus.base.ViewModelWithNavigation
import bav.petus.core.engine.UserStats
import bav.petus.core.notification.UserNotification
import bav.petus.repo.PetsRepository
import com.rickclephas.kmp.observableviewmodel.launch
import com.rickclephas.kmp.observableviewmodel.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class MainScreenUiState(
    val showCemetery: Boolean?,
    val notifications: List<UserNotification>,
)

class MainViewModel : ViewModelWithNavigation<MainViewModel.Navigation>(), KoinComponent {

    private val petsRepo: PetsRepository by inject()
    private val userStats: UserStats by inject()

    private val deadPetsFlow: StateFlow<Boolean?> = petsRepo.getAllPetsInCemeteryFlow()
        .map { deadPets -> deadPets.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val uiState: StateFlow<MainScreenUiState?> = combine(
        deadPetsFlow,
        userStats.getUserNotificationsFlow(),
    ) { showCemetery, notifications ->
        if (showCemetery == null) {
            null
        } else {
            MainScreenUiState(
                showCemetery = showCemetery,
                notifications = notifications,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun onAction(action: Action) {
        when (action) {
            is Action.TapOnNotification -> {
                viewModelScope.launch {
                    userStats.removeNotification(id = action.id)
                }
            }
        }
    }

    sealed interface Action {
        data class TapOnNotification(val id: String) : Action
    }

    sealed interface Navigation {

    }
}