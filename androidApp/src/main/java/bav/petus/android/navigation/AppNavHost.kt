package bav.petus.android.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

data class TopLevelRoute<T : Any>(val name: String, val route: T, val icon: ImageVector)

private val topLevelRoutes = listOf(
    TopLevelRoute("Cemetery", CemeteryScreen, Icons.Default.Add),
    TopLevelRoute("Zoo", ZooScreen, Icons.Default.Home),
    TopLevelRoute("Weather", WeatherReportScreen, Icons.Default.Menu),
)

@Composable
fun AppWithBottomBar(
    requestBackgroundLocationPermission: () -> Unit,
    shouldShowBackgroundLocationPermissionRationale: () -> Boolean,
) {
    var navigationSelectedItem by remember {
        mutableIntStateOf(1)
    }
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                topLevelRoutes.forEachIndexed { index, topLevelRoute ->
                    NavigationBarItem(
                        icon = { Icon(topLevelRoute.icon, contentDescription = topLevelRoute.name) },
                        label = { Text(topLevelRoute.name) },
                        selected = index == navigationSelectedItem,
                        onClick = {
                            navigationSelectedItem = index
                            navController.navigate(topLevelRoute.route) {
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
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ZooScreen,
            modifier = Modifier.padding(innerPadding)
        ) {
            zooScreen(
                navController = navController,
                requestBackgroundLocationPermission = requestBackgroundLocationPermission,
                shouldShowBackgroundLocationPermissionRationale = shouldShowBackgroundLocationPermissionRationale,
            )
            petDetailsScreen(navController)
            petCreationScreen(navController)
            cemeteryScreen(navController)
            weatherReportScreen(navController)
        }
    }
}
