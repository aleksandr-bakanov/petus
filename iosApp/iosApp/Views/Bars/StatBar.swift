import SwiftUI

struct StatBar: View {
    var color: Color
    var fraction: CGFloat
    var tweenDuration: Float = 0.3
    var frameHeight: Int = 16

    var body: some View {
        ZStack {
            GeometryReader { geometry in
                let totalWidth = geometry.size.width

                Rectangle()
                    .fill(color)
                    .frame(width: totalWidth * fraction, height: CGFloat(frameHeight))
                    .animation(.easeInOut(duration: TimeInterval(tweenDuration)), value: fraction)
            }
            .frame(height: CGFloat(frameHeight)) // fix height outside GeometryReader
        }
        .frame(height: CGFloat(frameHeight))
        .frame(maxWidth: .infinity)
    }
}
