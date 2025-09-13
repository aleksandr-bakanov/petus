package bav.petus.android.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import bav.petus.android.ui.pet_creation.PetCreationRoute
import bav.petus.viewModel.petCreation.PetCreationScreenViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object PetCreationScreenDestination

fun NavGraphBuilder.petCreationScreen(
    navController: NavHostController,
) {
    composable<PetCreationScreenDestination> {
        val viewModel: PetCreationScreenViewModel = koinViewModel()
        ObserveNavigationEvents(viewModel.navigation) { navigation ->
            when (navigation) {
                PetCreationScreenViewModel.Navigation.CloseScreen -> {
                    navController.popBackStack()
                }
                PetCreationScreenViewModel.Navigation.PetCreationSuccess -> {
                    navController.popBackStack()
                }
            }
        }
        PetCreationRoute(viewModel = viewModel)
    }
}