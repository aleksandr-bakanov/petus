package bav.petus.android.ui.user_profile

import androidx.lifecycle.viewModelScope
import bav.petus.android.base.ViewModelWithNavigation
import bav.petus.android.ui.common.UiState
import bav.petus.core.engine.Ability
import bav.petus.core.engine.UserStats
import bav.petus.core.inventory.InventoryItem
import bav.petus.core.inventory.InventoryItemId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserProfileUiState(
    val languageKnowledgeCatus: String,
    val languageKnowledgeDogus: String,
    val languageKnowledgeFrogus: String,
    val inventory: List<InventoryItem>,
    val abilities: Set<Ability>,
)

class UserProfileScreenViewModel(
    private val userStats: UserStats,
) : ViewModelWithNavigation<UserProfileScreenViewModel.Navigation>() {

    val uiState: StateFlow<UiState<UserProfileUiState>> = userStats.getUserProfileFlow()
        .map { data ->
            UiState.Success(
                UserProfileUiState(
                    languageKnowledgeCatus = "${data.languageKnowledge.catus} / ${UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE}",
                    languageKnowledgeDogus = "${data.languageKnowledge.dogus} / ${UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE}",
                    languageKnowledgeFrogus = "${data.languageKnowledge.frogus} / ${UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE}",
                    inventory = data.inventory,
                    abilities = data.abilities,
                )
            )
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Initial)

    fun onAction(action: Action) {
        when (action) {
            Action.AddItem -> {
                viewModelScope.launch {
                    userStats.addInventoryItem(InventoryItem(
                        id = InventoryItemId.Necronomicon,
                        amount = 1,
                    ))
                }
            }
            Action.RemoveItem -> {
                viewModelScope.launch {
                    userStats.removeInventoryItem(InventoryItem(
                        id = InventoryItemId.Necronomicon,
                        amount = 1,
                    ))
                }
            }
        }
    }

    sealed interface Action {
        data object AddItem: Action
        data object RemoveItem: Action
    }

    sealed interface Navigation {
        data object CloseScreen : Navigation
    }
}