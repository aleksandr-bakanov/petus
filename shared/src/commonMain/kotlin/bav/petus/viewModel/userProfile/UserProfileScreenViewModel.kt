package bav.petus.viewModel.userProfile

import bav.petus.base.ViewModelWithNavigation
import bav.petus.core.engine.Ability
import bav.petus.core.engine.UserStats
import bav.petus.core.inventory.InventoryItem
import bav.petus.core.inventory.InventoryItemId
import bav.petus.extension.str
import bav.petus.repo.WeatherRepository
import com.rickclephas.kmp.observableviewmodel.launch
import com.rickclephas.kmp.observableviewmodel.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class UserProfileUiState(
    val latestWeather: String?,
    val languageKnowledgeCatus: String,
    val languageKnowledgeDogus: String,
    val languageKnowledgeFrogus: String,
    val languageKnowledgeBober: String,
    val languageKnowledgeFractal: String,
    val inventory: List<InventoryItem>,
    val abilities: List<Ability>,
    val zooSize: String,
)

class UserProfileScreenViewModel : ViewModelWithNavigation<UserProfileScreenViewModel.Navigation>(), KoinComponent {

    private val userStats: UserStats by inject()
    private val weatherRepo: WeatherRepository by inject()

    val uiState: StateFlow<UserProfileUiState?> = combine(
        userStats.getUserProfileFlow(),
        weatherRepo.getLatestWeatherRecordFlow()
    ) { userData, weatherRecord ->
        UserProfileUiState(
            latestWeather = weatherRecord?.str(),
            languageKnowledgeCatus = "${userData.languageKnowledge.catus} / ${UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE}",
            languageKnowledgeDogus = "${userData.languageKnowledge.dogus} / ${UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE}",
            languageKnowledgeFrogus = "${userData.languageKnowledge.frogus} / ${UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE}",
            languageKnowledgeBober = "${userData.languageKnowledge.bober} / ${UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE}",
            languageKnowledgeFractal = "${userData.languageKnowledge.fractal} / ${UserStats.MAXIMUM_LANGUAGE_UI_KNOWLEDGE}",
            inventory = userData.inventory,
            abilities = userData.abilities.toList(),
            zooSize = userData.zooSize.toString(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

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