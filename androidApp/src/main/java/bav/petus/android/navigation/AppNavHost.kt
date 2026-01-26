package bav.petus.android.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import bav.petus.android.SatietyColor
import bav.petus.android.ui.common.StatBar
import bav.petus.android.ui.common.UserNotificationCell
import bav.petus.android.ui.common.toResId
import bav.petus.android.ui.onboarding.OnboardingBottomSheet
import bav.petus.core.resources.StringId
import bav.petus.viewModel.main.BottomSheetType
import bav.petus.viewModel.main.MainScreenUiState
import bav.petus.viewModel.main.MainViewModel

data class TopLevelRoute(
    val name: StringId,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val cemeteryTopRoute = TopLevelRoute(StringId.CemeteryScreenTitle, "CemeteryTab", Icons.Filled.Add, Icons.Outlined.Add)
private val zooTopRoute = TopLevelRoute(StringId.ZooScreenTitle, "ZooTab", Icons.Filled.Home, Icons.Outlined.Home)
private val weatherTopRoute = TopLevelRoute(StringId.WeatherScreenTitle, "WeatherReportTab", Icons.Filled.Menu, Icons.Outlined.Menu)
private val profileTopRoute = TopLevelRoute(StringId.ProfileScreenTitle, "UserProfileTab", Icons.Filled.Face, Icons.Outlined.Face)
private val questStatusTopRoute = TopLevelRoute(StringId.QuestsScreenTitle, "QuestStatusTab", Icons.Filled.Info, Icons.Outlined.Info)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppWithBottomBar(
    uiState: MainScreenUiState,
    onAction: (MainViewModel.Action) -> Unit,
) {
    val rootNavController = rememberNavController()
    val navBackStackEntry by rootNavController.currentBackStackEntryAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        bottomBar = {
            NavigationBar {
                if (uiState.showCemetery == true) {
                    TopLevelNavBarItem(
                        item = cemeteryTopRoute,
                        isSelected = navBackStackEntry.isTabSelected(cemeteryTopRoute.route)
                    ) { item ->
                        navigateToTopLevel(rootNavController, item.route)
                    }
                }

                TopLevelNavBarItem(
                    item = zooTopRoute,
                    isSelected = navBackStackEntry.isTabSelected(zooTopRoute.route)
                ) { item ->
                    navigateToTopLevel(rootNavController, item.route)
                }

                TopLevelNavBarItem(
                    item = profileTopRoute,
                    isSelected = navBackStackEntry.isTabSelected(profileTopRoute.route)
                ) { item ->
                    navigateToTopLevel(rootNavController, item.route)
                }

                TopLevelNavBarItem(
                    item = questStatusTopRoute,
                    isSelected = navBackStackEntry.isTabSelected(questStatusTopRoute.route)
                ) { item ->
                    navigateToTopLevel(rootNavController, item.route)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            NavHost(
                navController = rootNavController,
                startDestination = zooTopRoute.route,
            ) {
                composable(cemeteryTopRoute.route) {
                    CemeteryNavHost()
                }
                composable(zooTopRoute.route) {
                    ZooNavHost()
                }
                composable(profileTopRoute.route) {
                    UserProfileNavHost()
                }
                composable(questStatusTopRoute.route) {
                    QuestStatusNavHost()
                }
            }

            uiState.gameUpdateState?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                ) {
                    StatBar(
                        color = SatietyColor,
                        fraction = it.fraction,
                        icon = null,
                        tweenDuration = 100,
                    )
                }
            }

            if (uiState.notifications.isNotEmpty()) {
                Column {
                    uiState.notifications.forEach { notification ->
                        UserNotificationCell(notification) {
                            onAction(MainViewModel.Action.TapOnNotification(notification.id))
                        }
                    }
                }
            }

            if (uiState.bottomSheetType != null) {
                when (val sheet = uiState.bottomSheetType) {
                    is BottomSheetType.Onboarding -> {
                        ModalBottomSheet(
                            onDismissRequest = { onAction(MainViewModel.Action.HideBottomSheet) },
                            sheetState = sheetState,
                            dragHandle = null,
                        ) {
                            OnboardingBottomSheet(uiState = sheet)
                        }
                    }

                    else -> Unit
                }
            }
        }
    }
}


@Composable
private fun ZooNavHost() {
    val zooNavController = rememberNavController()
    NavHost(zooNavController, startDestination = ZooScreenDestination) {
        zooScreen(zooNavController)
        petCreationScreen(zooNavController)
        petDetailsScreen(zooNavController)
        dialogScreen(zooNavController)
    }
}

@Composable
private fun CemeteryNavHost() {
    val cemeteryNavController = rememberNavController()
    NavHost(cemeteryNavController, startDestination = CemeteryScreenDestination) {
        cemeteryScreen(cemeteryNavController)
        petDetailsScreen(cemeteryNavController)
    }
}

@Composable
private fun UserProfileNavHost() {
    val userProfileNavController = rememberNavController()
    NavHost(userProfileNavController, startDestination = UserProfileScreenDestination) {
        userProfileScreen(userProfileNavController)
        itemDetailsScreen(userProfileNavController)
    }
}

@Composable
private fun QuestStatusNavHost() {
    val questStatusNavController = rememberNavController()
    NavHost(questStatusNavController, startDestination = QuestStatusScreenDestination) {
        questStatusScreen()
    }
}

@Composable
private fun RowScope.TopLevelNavBarItem(
    item: TopLevelRoute,
    isSelected: Boolean,
    onClick: (item: TopLevelRoute) -> Unit,
) {
    NavigationBarItem(
        icon = {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = stringResource(item.name.toResId())
            )
        },
        label = { Text(stringResource(item.name.toResId())) },
        selected = isSelected,
        onClick = {
            onClick(item)
        }
    )
}

private fun navigateToTopLevel(
    navController: NavController,
    route: String,
) {
    navController.navigate(route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // re-selecting the same item
        launchSingleTop = true
        // Restore state when re-selecting a previously selected item
        restoreState = true
    }
}
