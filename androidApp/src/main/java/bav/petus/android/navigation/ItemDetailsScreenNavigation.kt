package bav.petus.android.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import bav.petus.android.ui.item_details.ItemDetailsRoute
import bav.petus.core.inventory.InventoryItemId
import bav.petus.viewModel.itemDetails.ItemDetailsScreenViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class ItemDetailsScreenDestination(
    val itemId: InventoryItemId,
)

fun NavGraphBuilder.itemDetailsScreen(
    navController: NavHostController,
) {
    composable<ItemDetailsScreenDestination> { navBackStackEntry ->
        val args: ItemDetailsScreenDestination = navBackStackEntry.toRoute()
        val viewModel: ItemDetailsScreenViewModel = koinViewModel(
            parameters = {
                parametersOf(args.itemId)
            }
        )
        ObserveNavigationEvents(viewModel.navigation) { navigation ->
            when (navigation) {
                ItemDetailsScreenViewModel.Navigation.CloseScreen -> {
                    navController.popBackStack()
                }
            }
        }
        ItemDetailsRoute(viewModel = viewModel)
    }
}