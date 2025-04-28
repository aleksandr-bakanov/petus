import Foundation

extension Int64 {
    
    var epochTimeToString: String {
        if (self == 0) {
            "unknown"
        } else {
            Date(timeIntervalSince1970: TimeInterval(self)).formatted()
        }
    }
}
