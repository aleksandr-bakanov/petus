package bav.petus.android.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import bav.petus.android.ui.user_profile.UserProfileRoute
import bav.petus.viewModel.userProfile.UserProfileScreenViewModel
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.userProfileScreen() {
    composable<TopLevelRoutes.UserProfileScreen> {
        val viewModel: UserProfileScreenViewModel = koinViewModel()
        LaunchedEffect(Unit) {
            viewModel.navigation.collect { _ ->

            }
        }
        UserProfileRoute(viewModel = viewModel)
    }
}