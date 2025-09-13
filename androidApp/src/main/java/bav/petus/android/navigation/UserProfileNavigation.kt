package bav.petus.android.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import bav.petus.android.ui.user_profile.UserProfileRoute
import bav.petus.viewModel.userProfile.UserProfileScreenViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object UserProfileScreenDestination

fun NavGraphBuilder.userProfileScreen() {
    composable<UserProfileScreenDestination> {
        val viewModel: UserProfileScreenViewModel = koinViewModel()
        ObserveNavigationEvents(viewModel.navigation) { navigation ->

        }
        UserProfileRoute(viewModel = viewModel)
    }
}