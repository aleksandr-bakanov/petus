import SwiftUI
import shared

struct Avatar: View {
    let imageId: ImageId

    var body: some View {
        Image(imageId.resId)
            .resizable()
            .interpolation(.high)
            .antialiased(true)
            .scaledToFill()
            .frame(width: 84, height: 84)
            .clipShape(Circle())
    }
}
