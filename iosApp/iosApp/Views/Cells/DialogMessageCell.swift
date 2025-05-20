import SwiftUI
import shared

struct DialogMessageCell: View {
    let message: DialogMessage

    var body: some View {
        HStack(alignment: .top) {
            if message.isImageAtStart {
                Avatar(imageId: message.imageId)
                Spacer().frame(width: 8)
                MessageText(text: message.text, alignLeft: true)
                Spacer().frame(width: 16)
            } else {
                Spacer().frame(width: 16)
                MessageText(text: message.text, alignLeft: false)
                Spacer().frame(width: 8)
                Avatar(imageId: message.imageId)
            }
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
    }
}
