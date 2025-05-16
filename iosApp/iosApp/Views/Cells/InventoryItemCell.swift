import SwiftUI
import shared

struct InventoryItemCell: View {
    let item: InventoryItem

    var body: some View {
        HStack {
            Text(item.id.name)
                .frame(maxWidth: .infinity, alignment: .leading)
                .layoutPriority(1)
            Text(item.amount.formatted())
                .frame(width: 80, alignment: .trailing) // Fixed width or proportional width
        }
    }
}
