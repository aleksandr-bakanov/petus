package bav.petus.android.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import bav.petus.android.ui.pet_details.PetDetailsRoute
import bav.petus.viewModel.petDetails.PetDetailsScreenViewModel
import bav.petus.viewModel.petDetails.PetDetailsScreenViewModelArgs
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class PetDetailsScreen(
    val petId: Long,
)

fun NavGraphBuilder.petDetailsScreen(
    navController: NavHostController,
) {
    composable<PetDetailsScreen> { navBackStackEntry ->
        val args: PetDetailsScreen = navBackStackEntry.toRoute()
        val viewModel: PetDetailsScreenViewModel = koinViewModel(
            parameters = {
                parametersOf(
                    PetDetailsScreenViewModelArgs(
                        petId = args.petId,
                    )
                )
            }
        )
        LaunchedEffect(Unit) {
            viewModel.navigation.collect { navigation ->
                when (navigation) {
                    PetDetailsScreenViewModel.Navigation.CloseScreen -> {
                        navController.popBackStack()
                    }

                    is PetDetailsScreenViewModel.Navigation.OpenDialogScreen -> {
                        navController.navigate(DialogScreen(navigation.petId))
                    }
                }
            }
        }
        PetDetailsRoute(viewModel = viewModel)
    }
}