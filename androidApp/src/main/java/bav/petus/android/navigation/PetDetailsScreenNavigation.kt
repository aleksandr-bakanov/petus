package bav.petus.android.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import bav.petus.android.ui.pet_details.PetDetailsRoute
import bav.petus.viewModel.petDetails.PetDetailsScreenViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class PetDetailsScreenDestination(
    val petId: Long,
)

fun NavGraphBuilder.petDetailsScreen(
    navController: NavHostController,
) {
    composable<PetDetailsScreenDestination> { navBackStackEntry ->
        val args: PetDetailsScreenDestination = navBackStackEntry.toRoute()
        val viewModel: PetDetailsScreenViewModel = koinViewModel(
            parameters = {
                parametersOf(args.petId)
            }
        )
        LaunchedEffect(Unit) {
            viewModel.navigation.collect { navigation ->
                when (navigation) {
                    PetDetailsScreenViewModel.Navigation.CloseScreen -> {
                        navController.popBackStack()
                    }

                    is PetDetailsScreenViewModel.Navigation.OpenDialogScreen -> {
                        navController.navigate(DialogScreenDestination(navigation.petId))
                    }
                }
            }
        }
        PetDetailsRoute(viewModel = viewModel)
    }
}