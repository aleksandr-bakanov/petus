package bav.petus.viewModel.itemDetails

import bav.petus.base.ViewModelWithNavigation
import bav.petus.core.inventory.InventoryItemId
import bav.petus.core.inventory.toImageId
import bav.petus.core.inventory.toItemDescriptionStringId
import bav.petus.core.inventory.toItemNameStringId
import bav.petus.core.resources.ImageId
import bav.petus.core.resources.StringId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent

data class ItemDetailsUiState(
    val title: StringId,
    val imageId: ImageId,
    val descriptionId: StringId,
)

data class ItemDetailsScreenViewModelArgs(
    val itemId: InventoryItemId,
)

class ItemDetailsScreenViewModel(
    private val args: ItemDetailsScreenViewModelArgs,
) : ViewModelWithNavigation<ItemDetailsScreenViewModel.Navigation>(), KoinComponent {

    val uiState: StateFlow<ItemDetailsUiState?> = MutableStateFlow<ItemDetailsUiState?>(
        ItemDetailsUiState(
            title = args.itemId.toItemNameStringId(),
            imageId = args.itemId.toImageId(),
            descriptionId = args.itemId.toItemDescriptionStringId(),
        )
    )

    fun onAction(action: Action) {
        when (action) {
            Action.CloseScreen -> {
                navigate(Navigation.CloseScreen)
            }
        }
    }

    sealed interface Action {
        data object CloseScreen : Action
    }

    sealed interface Navigation {
        data object CloseScreen : Navigation
    }
}
