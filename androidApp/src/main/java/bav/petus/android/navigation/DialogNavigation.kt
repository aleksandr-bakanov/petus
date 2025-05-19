package bav.petus.android.navigation

import androidx.compose.runtime.LaunchedEffect
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
data class DialogScreen(
    val petId: Long,
)

fun NavGraphBuilder.dialogScreen(
    navController: NavHostController,
) {
    composable<DialogScreen> { navBackStackEntry ->
        val args: DialogScreen = navBackStackEntry.toRoute()
        val viewModel: DialogScreenViewModel = koinViewModel(
            parameters = {
                parametersOf(args.petId)
            }
        )
        LaunchedEffect(Unit) {
            viewModel.navigation.collect { navigation ->
                when (navigation) {
                    DialogScreenViewModel.Navigation.CloseScreen -> {
                        navController.popBackStack()
                    }
                }
            }
        }
        DialogRoute(viewModel = viewModel)
    }
}
