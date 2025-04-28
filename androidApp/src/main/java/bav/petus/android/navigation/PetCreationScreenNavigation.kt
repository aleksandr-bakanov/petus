package bav.petus.android.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import bav.petus.android.ui.pet_creation.PetCreationRoute
import bav.petus.android.ui.pet_creation.PetCreationScreenViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object PetCreationScreen

fun NavGraphBuilder.petCreationScreen(
    navController: NavHostController,
) {
    composable<PetCreationScreen> {
        val viewModel: PetCreationScreenViewModel = koinViewModel()
        LaunchedEffect(Unit) {
            viewModel.navigation.collect { navigation ->
                when (navigation) {
                    PetCreationScreenViewModel.Navigation.CloseScreen -> {
                        navController.popBackStack()
                    }
                    PetCreationScreenViewModel.Navigation.PetCreationSuccess -> {
                        navController.popBackStack()
                    }
                }
            }
        }
        PetCreationRoute(viewModel = viewModel)
    }
}