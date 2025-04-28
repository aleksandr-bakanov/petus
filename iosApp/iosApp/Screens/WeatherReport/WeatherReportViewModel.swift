import SwiftUI
import shared

@MainActor final class WeatherReportViewModel: ObservableObject {
    
    let koinHelper: KoinHelper = KoinHelper()
    
    @Published var records: [String] = []
    
    func loadRecords() async {
        let recordsFlow = koinHelper.getAllWeatherRecordsFlow()
        for await records in recordsFlow {
            self.records = records
        }
    }
}
