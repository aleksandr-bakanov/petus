package bav.petus.android.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import bav.petus.android.ui.pet_creation.PetCreationScreenViewModel
import bav.petus.android.ui.zoo.ZooRoute
import bav.petus.android.ui.zoo.ZooScreenViewModel
import bav.petus.android.ui.zoo.ZooScreenViewModelArgs
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data object ZooScreen

fun NavGraphBuilder.zooScreen(
    navController: NavHostController,
    requestBackgroundLocationPermission: () -> Unit,
    shouldShowBackgroundLocationPermissionRationale: () -> Boolean,
) {
    composable<ZooScreen> { navBackStackEntry ->
        val viewModel: ZooScreenViewModel = koinViewModel(
            parameters = {
                parametersOf(
                    ZooScreenViewModelArgs(
                        shouldShowBackgroundLocationPermissionRationale,
                    )
                )
            }
        )
        LaunchedEffect(Unit) {
            viewModel.navigation.collect { navigation ->
                when (navigation) {
                    ZooScreenViewModel.Navigation.OpenApplicationSettings -> {
                        requestBackgroundLocationPermission()
                    }
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