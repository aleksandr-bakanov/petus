package bav.petus.android.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import bav.petus.android.ui.weather_report.WeatherReportRoute
import bav.petus.android.ui.weather_report.WeatherReportViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object WeatherReportScreen

fun NavGraphBuilder.weatherReportScreen() {
    composable<WeatherReportScreen> {
        val viewModel: WeatherReportViewModel = koinViewModel()
        LaunchedEffect(Unit) {
            viewModel.navigation.collect { _ ->

            }
        }
        WeatherReportRoute(viewModel = viewModel)
    }
}