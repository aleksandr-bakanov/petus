package bav.petus.android.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import bav.petus.android.ui.zoo.ZooRoute
import bav.petus.viewModel.zoo.ZooScreenViewModel
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.zooScreen(
    navController: NavHostController,
) {
    composable<TopLevelRoutes.ZooScreen> { _ ->
        val viewModel: ZooScreenViewModel = koinViewModel()
        LaunchedEffect(Unit) {
            viewModel.navigation.collect { navigation ->
                when (navigation) {
                    is ZooScreenViewModel.Navigation.ToDetails -> {
                        navController.navigate(PetDetailsScreen(navigation.petId))
                    }
                    ZooScreenViewModel.Navigation.ToNewPetCreation -> {
                        navController.navigate(PetCreationScreen)
                    }
                }
            }
        }
        ZooRoute(viewModel = viewModel)
    }
}