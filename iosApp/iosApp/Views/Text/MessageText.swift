import SwiftUI

struct MessageText: View {
    let text: String
    let alignLeft: Bool

    var body: some View {
        HStack {
            if alignLeft { Spacer().frame(width: 0) }
            Text(text)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Color.secondary.opacity(0.2))
                .cornerRadius(8)
            if !alignLeft { Spacer().frame(width: 0) }
        }
        .frame(maxWidth: .infinity, alignment: alignLeft ? .leading : .trailing)
    }
}
