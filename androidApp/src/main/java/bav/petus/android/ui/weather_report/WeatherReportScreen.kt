package bav.petus.android.ui.weather_report

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bav.petus.viewModel.weatherReport.WeatherReportUiState
import bav.petus.viewModel.weatherReport.WeatherReportViewModel

@Composable
fun WeatherReportRoute(
    viewModel: WeatherReportViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    uiState?.let {
        WeatherReportScreen(
            uiState = it,
            onAction = viewModel::onAction,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherReportScreen(
    uiState: WeatherReportUiState,
    onAction: (WeatherReportViewModel.Action) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    "Weather records",
                    style = MaterialTheme.typography.headlineLarge
                )
            })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.records.isEmpty()) {
                Text(
                    "No reports",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.records) { record: String ->
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            text = record,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}