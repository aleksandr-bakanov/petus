package bav.petus.viewModel.userProfile

import bav.petus.base.ViewModelWithNavigation
import bav.petus.core.engine.Ability
import bav.petus.core.engine.UserStats
import bav.petus.core.inventory.InventoryItem
import bav.petus.core.inventory.InventoryItemId
import com.rickclephas.kmp.observableviewmodel.launch
import com.rickclephas.kmp.observableviewmodel.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class UserProfileUiState(
    val languageKnowledgeCatus: String,
    val languageKnowledgeDogus: String,
    val languageKnowledgeFrogus: String,
    val inventory: List<InventoryItem>,
    val abilities: List<Ability>,
)

class UserProfileScreenViewModel : ViewModelWithNavigation<UserProfileScreenViewModel.Navigation>(), KoinComponent {

    private val userStats: UserStats by inject()

    val uiState: StateFlow<UserProfileUiState?> = userStats.getUserProfileFlow()
        .map { data ->
            UserProfileUiState(
                languageKnowledgeCatus = "${data.languageKnowledge.catus} / ${UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE}",
                languageKnowledgeDogus = "${data.languageKnowledge.dogus} / ${UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE}",
                languageKnowledgeFrogus = "${data.languageKnowledge.frogus} / ${UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE}",
                inventory = data.inventory,
                abilities = data.abilities.toList(),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

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