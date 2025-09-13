package bav.petus.android.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import bav.petus.android.ui.zoo.ZooRoute
import bav.petus.viewModel.zoo.ZooScreenViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object ZooScreenDestination

fun NavGraphBuilder.zooScreen(
    navController: NavHostController,
) {
    composable<ZooScreenDestination> { _ ->
        val viewModel: ZooScreenViewModel = koinViewModel()
        ObserveNavigationEvents(viewModel.navigation) { navigation ->
            when (navigation) {
                is ZooScreenViewModel.Navigation.ToDetails -> {
                    navController.navigate(PetDetailsScreenDestination(navigation.petId))
                }
                ZooScreenViewModel.Navigation.ToNewPetCreation -> {
                    navController.navigate(PetCreationScreenDestination)
                }
            }
        }
        ZooRoute(viewModel = viewModel)
    }
}