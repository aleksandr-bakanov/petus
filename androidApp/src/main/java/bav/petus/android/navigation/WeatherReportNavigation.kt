package bav.petus.android.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import bav.petus.android.ui.weather_report.WeatherReportRoute
import bav.petus.viewModel.weatherReport.WeatherReportViewModel
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.weatherReportScreen() {
    composable<TopLevelRoutes.WeatherReportScreen> {
        val viewModel: WeatherReportViewModel = koinViewModel()
        LaunchedEffect(Unit) {
            viewModel.navigation.collect { _ ->

            }
        }
        WeatherReportRoute(viewModel = viewModel)
    }
}