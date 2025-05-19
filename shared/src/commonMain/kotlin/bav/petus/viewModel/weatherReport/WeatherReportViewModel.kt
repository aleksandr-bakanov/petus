package bav.petus.viewModel.weatherReport

import bav.petus.base.ViewModelWithNavigation
import bav.petus.repo.WeatherRepository
import com.rickclephas.kmp.observableviewmodel.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class WeatherReportUiState(
    val records: List<String>,
)

class WeatherReportViewModel : ViewModelWithNavigation<WeatherReportViewModel.Navigation>(), KoinComponent {

    private val weatherRepo: WeatherRepository by inject()

    val uiState: StateFlow<WeatherReportUiState?> = weatherRepo.getAllWeatherRecordsFlow()
        .map { records ->
            WeatherReportUiState(
                records = records,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun onAction(action: Action) {

    }

    sealed interface Action {

    }

    sealed interface Navigation {

    }
}