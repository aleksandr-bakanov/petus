import SwiftUI

struct WeatherReportView: View {
    
    @StateObject var viewModel = WeatherReportViewModel()
    
    var body: some View {
        NavigationView {
            VStack {
                List(viewModel.records, id: \.self) { record in
                    WeatherRecordListCell(record: record)
                }
            }
            .navigationTitle("Weather records")
        }
        .task {
            await viewModel.loadRecords()
        }
    }
}
