import SwiftUI
import shared

struct WeatherRecordListCell: View {
    
    let record: String
    
    var body: some View {
        Text(record)
            .font(.caption)
    }
}
