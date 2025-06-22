import SwiftUI

struct DialogButton: View {
    let text: String
    var onClick: () -> Void = {}

    var body: some View {
        Button(action: onClick) {
            HStack(alignment: .top) {
                Text(text)
                    .font(.body)
                    .multilineTextAlignment(.leading)
                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
        }
        .frame(maxWidth: .infinity)
        .background(Color.secondary.opacity(0.2)) // containerColor with alpha
        .foregroundColor(.primary) // contentColor
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(Color.secondary.opacity(0.2), lineWidth: 2)
        )
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}
