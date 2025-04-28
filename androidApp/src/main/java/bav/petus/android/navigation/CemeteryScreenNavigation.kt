package bav.petus.android.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import bav.petus.android.ui.cemetery.CemeteryRoute
import bav.petus.android.ui.cemetery.CemeteryScreenViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object CemeteryScreen

fun NavGraphBuilder.cemeteryScreen(
    navController: NavHostController,
) {
    composable<CemeteryScreen> {
        val viewModel: CemeteryScreenViewModel = koinViewModel()
        LaunchedEffect(Unit) {
            viewModel.navigation.collect { navigation ->
                when (navigation) {
                    is CemeteryScreenViewModel.Navigation.ToDetails -> {
                        navController.navigate(PetDetailsScreen(navigation.petId))
                    }
                }
            }
        }
        CemeteryRoute(viewModel = viewModel)
    }
}