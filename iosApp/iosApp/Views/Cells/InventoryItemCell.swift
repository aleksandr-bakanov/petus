import SwiftUI
import shared

struct InventoryItemCell: View {
    let item: InventoryItem
    let onClick: () -> Void

    var body: some View {
        Image(item.id.toImageId().resId)
            .resizable()
            .scaledToFit()
            .frame(maxWidth: .infinity)
            .clipShape(
                RoundedRectangle(cornerRadius: 16, style: .continuous)
            )
            .onTapGesture {
                onClick()
            }
//        HStack {
//            Text(item.id.toStringId().localized)
//                .frame(maxWidth: .infinity, alignment: .leading)
//                .layoutPriority(1)
//            Text(item.amount.formatted())
//                .frame(width: 80, alignment: .trailing) // Fixed width or proportional width
//        }
    }
}
