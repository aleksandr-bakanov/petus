package bav.petus.android.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import bav.petus.viewModel.main.MainScreenUiState
import kotlinx.serialization.Serializable

sealed interface TopLevelRoutes {
    @Serializable
    data object CemeteryScreen : TopLevelRoutes

    @Serializable
    data object ZooScreen : TopLevelRoutes

    @Serializable
    data object WeatherReportScreen : TopLevelRoutes

    @Serializable
    data object UserProfileScreen : TopLevelRoutes
}

data class TopLevelRoute(val name: String, val route: TopLevelRoutes, val icon: ImageVector)

private val cemeteryTopRoute = TopLevelRoute("Cemetery", TopLevelRoutes.CemeteryScreen, Icons.Default.Add)
private val zooTopRoute = TopLevelRoute("Zoo", TopLevelRoutes.ZooScreen, Icons.Default.Home)
private val weatherTopRoute = TopLevelRoute("Weather", TopLevelRoutes.WeatherReportScreen, Icons.Default.Menu)
private val profileTopRoute = TopLevelRoute("Profile", TopLevelRoutes.UserProfileScreen, Icons.Default.Face)

@Composable
fun AppWithBottomBar(
    uiState: MainScreenUiState,
) {
    var navigationSelectedItemName by remember {
        mutableStateOf(zooTopRoute.name)
    }

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                if (uiState.showCemetery == true) {
                    TopLevelNavBarItem(
                        item = cemeteryTopRoute,
                        isSelected = navigationSelectedItemName == cemeteryTopRoute.name
                    ) { item ->
                        navigationSelectedItemName = item.name
                        navigateToTopLevel(navController, item.route)
                    }
                }

                TopLevelNavBarItem(
                    item = zooTopRoute,
                    isSelected = navigationSelectedItemName == zooTopRoute.name
                ) { item ->
                    navigationSelectedItemName = item.name
                    navigateToTopLevel(navController, item.route)
                }

                TopLevelNavBarItem(
                    item = profileTopRoute,
                    isSelected = navigationSelectedItemName == profileTopRoute.name
                ) { item ->
                    navigationSelectedItemName = item.name
                    navigateToTopLevel(navController, item.route)
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelRoutes.ZooScreen,
            modifier = Modifier.padding(innerPadding)
        ) {
            zooScreen(navController)
            petDetailsScreen(navController)
            petCreationScreen(navController)
            cemeteryScreen(navController)
            weatherReportScreen()
            userProfileScreen()
            dialogScreen(navController)
        }
    }
}

@Composable
private fun RowScope.TopLevelNavBarItem(
    item: TopLevelRoute,
    isSelected: Boolean,
    onClick: (item: TopLevelRoute) -> Unit,
) {
    NavigationBarItem(
        icon = { Icon(item.icon, contentDescription = item.name) },
        label = { Text(item.name) },
        selected = isSelected,
        onClick = {
            onClick(item)
        }
    )
}

private fun navigateToTopLevel(
    navController: NavController,
    route: TopLevelRoutes,
) {
    navController.navigate(route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }
}
