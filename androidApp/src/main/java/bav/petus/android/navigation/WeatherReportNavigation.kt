package bav.petus.android.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import bav.petus.android.ui.weather_report.WeatherReportRoute
import bav.petus.viewModel.weatherReport.WeatherReportViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object WeatherReportScreenDestination

fun NavGraphBuilder.weatherReportScreen() {
    composable<WeatherReportScreenDestination> {
        val viewModel: WeatherReportViewModel = koinViewModel()
        ObserveNavigationEvents(viewModel.navigation) { _ ->

        }
        WeatherReportRoute(viewModel = viewModel)
    }
}