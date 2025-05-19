import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

struct WeatherReportScreen: View {
    
    @StateViewModel var viewModel: WeatherReportViewModel = WeatherReportViewModel()
    
    var body: some View {
        VStack {
            if let state = viewModel.uiState.value {
                List(state.records, id: \.self) { record in
                    WeatherRecordListCell(record: record)
                }
            }
        }
        .navigationTitle("Weather records")
    }
}
