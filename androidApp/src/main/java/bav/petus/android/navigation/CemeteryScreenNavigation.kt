package bav.petus.android.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import bav.petus.android.ui.cemetery.CemeteryRoute
import bav.petus.viewModel.cemetery.CemeteryScreenViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object CemeteryScreenDestination

fun NavGraphBuilder.cemeteryScreen(
    navController: NavHostController,
) {
    composable<CemeteryScreenDestination> {
        val viewModel: CemeteryScreenViewModel = koinViewModel()
        LaunchedEffect(Unit) {
            viewModel.navigation.collect { navigation ->
                when (navigation) {
                    is CemeteryScreenViewModel.Navigation.ToDetails -> {
                        navController.navigate(PetDetailsScreenDestination(navigation.petId))
                    }
                }
            }
        }
        CemeteryRoute(viewModel = viewModel)
    }
}