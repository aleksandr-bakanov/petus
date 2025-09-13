package bav.petus.android.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import bav.petus.android.ui.dialog.DialogRoute
import bav.petus.viewModel.dialog.DialogScreenViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class DialogScreenDestination(
    val petId: Long,
)

fun NavGraphBuilder.dialogScreen(
    navController: NavHostController,
) {
    composable<DialogScreenDestination> { navBackStackEntry ->
        val args: DialogScreenDestination = navBackStackEntry.toRoute()
        val viewModel: DialogScreenViewModel = koinViewModel(
            parameters = {
                parametersOf(args.petId)
            }
        )
        ObserveNavigationEvents(viewModel.navigation) { navigation ->
            when (navigation) {
                DialogScreenViewModel.Navigation.CloseScreen -> {
                    navController.popBackStack()
                }
            }
        }
        DialogRoute(viewModel = viewModel)
    }
}
