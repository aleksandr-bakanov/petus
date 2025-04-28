package bav.petus.android.ui.weather_report

import androidx.lifecycle.viewModelScope
import bav.petus.android.base.ViewModelWithNavigation
import bav.petus.android.ui.common.UiState
import bav.petus.repo.WeatherRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class WeatherReportUiState(
    val records: List<String>,
)

class WeatherReportViewModel(
    private val weatherRepo: WeatherRepository
) : ViewModelWithNavigation<WeatherReportViewModel.Navigation>() {

    val uiState: StateFlow<UiState<WeatherReportUiState>> = weatherRepo.getAllWeatherRecordsFlow()
        .map { records ->
            UiState.Success(
                WeatherReportUiState(
                    records = records,
                )
            )
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Initial)

    fun onAction(action: Action) {

    }

    sealed interface Action {

    }

    sealed interface Navigation {

    }
}