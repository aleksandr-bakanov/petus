import SwiftUI
import shared

struct AbilityItemCell: View {
    let item: AbilityItem
    let onClick: () -> Void

    var body: some View {
        Image(item.isAvailable ? item.ability.toImageId().resId: "question_mark")
            .resizable()
            .scaledToFit()
            .frame(maxWidth: .infinity)
            .clipShape(
                RoundedRectangle(cornerRadius: 16, style: .continuous)
            )
            .onTapGesture {
                if (item.isAvailable) {
                    onClick()
                }
            }
    }
}
