import SwiftUI

struct AnimatedImageButton: View {
    let imageName: String
    let title: String
    let action: () -> Void
    var size: CGFloat = 112

    @State private var isPressed = false

    var body: some View {
        VStack(alignment: .center, spacing: 4) {
            Image(imageName)
                .resizable()
                .interpolation(.high)
                .antialiased(true)
                .scaledToFit()
                .frame(width: size, height: size)
            Text(title)
                .font(.caption)
                .frame(width: size, alignment: .center)
                .multilineTextAlignment(.center)
        }
        .scaleEffect(isPressed ? 0.9 : 1.0)
        .animation(.spring(response: 0.3, dampingFraction: 0.5), value: isPressed)
        .onTapGesture {
            isPressed = true
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) { // 100ms delay
                isPressed = false
                action()
            }
        }
//        Image(imageName)
//            .resizable()
//            .interpolation(.high)
//            .antialiased(true)
//            .scaledToFit()
//            .frame(width: size, height: size)
//            .scaleEffect(isPressed ? 0.9 : 1.0)
//            .animation(.spring(response: 0.3, dampingFraction: 0.5), value: isPressed)
//            .onTapGesture {
//                isPressed = true
//                DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) { // 100ms delay
//                    isPressed = false
//                    action()
//                }
//            }
    }
}
