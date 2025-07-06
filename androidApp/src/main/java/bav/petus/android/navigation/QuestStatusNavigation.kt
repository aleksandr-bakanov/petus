package bav.petus.android.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import bav.petus.android.ui.quest_status.QuestStatusRoute
import bav.petus.viewModel.questStatus.QuestStatusViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object QuestStatusScreenDestination

fun NavGraphBuilder.questStatusScreen() {
    composable<QuestStatusScreenDestination> {
        val viewModel: QuestStatusViewModel = koinViewModel()
        LaunchedEffect(Unit) {
            viewModel.navigation.collect { _ ->

            }
        }
        QuestStatusRoute(viewModel = viewModel)
    }
}