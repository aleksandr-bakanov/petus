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
import bav.petus.android.ui.common.UiState

@Composable
fun WeatherReportRoute(
    viewModel: WeatherReportViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    WeatherReportScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherReportScreen(
    uiState: UiState<WeatherReportUiState>,
    onAction: (WeatherReportViewModel.Action) -> Unit,
) {
    when (uiState) {
        is UiState.Failure -> {}
        UiState.Initial -> {}
        UiState.Loading -> {}
        is UiState.Success -> {
            val state = uiState.data
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
                    if (state.records.isEmpty()) {
                        Text(
                            "No reports",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    else {
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(state.records) { record: String ->
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
    }
}