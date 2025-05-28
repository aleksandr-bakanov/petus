import SwiftUI

struct StatBar: View {
    var color: Color
    var fraction: CGFloat

    var body: some View {
        ZStack {
            GeometryReader { geometry in
                let totalWidth = geometry.size.width

                Rectangle()
                    .fill(color)
                    .frame(width: totalWidth * fraction, height: 16)
                    .animation(.easeInOut(duration: 0.3), value: fraction)
            }
            .frame(height: 16) // fix height outside GeometryReader
        }
        .frame(height: 16)
        .frame(maxWidth: .infinity)
    }
}
