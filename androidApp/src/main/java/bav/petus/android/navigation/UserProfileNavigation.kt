package bav.petus.android.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import bav.petus.android.ui.user_profile.UserProfileRoute
import bav.petus.viewModel.main.MainViewModel
import bav.petus.viewModel.userProfile.UserProfileScreenViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.viewmodel.koinActivityViewModel

@Serializable
data object UserProfileScreenDestination

fun NavGraphBuilder.userProfileScreen(
    navController: NavHostController,
) {
    composable<UserProfileScreenDestination> {
        val viewModel: UserProfileScreenViewModel = koinViewModel()
        val mainViewModel: MainViewModel = koinActivityViewModel()
        ObserveNavigationEvents(viewModel.navigation) { navigation ->
            when (navigation) {
                is UserProfileScreenViewModel.Navigation.ShowInventoryItemDetails -> {
                    navController.navigate(ItemDetailsScreenDestination(navigation.inventoryItemId))
                }
                else -> Unit
            }
        }
        UserProfileRoute(
            viewModel = viewModel,
            mainViewModel = mainViewModel,
        )
    }
}