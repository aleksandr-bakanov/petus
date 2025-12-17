import SwiftUI
import shared

struct UserNotificationCell: View {
    let notification: UserNotification
    let onClick: (String) -> Void

    var body: some View {
        HStack {
            // Notification title
            Text(notification.notificationTitle)
                .padding(.leading, 4)
                .frame(maxWidth: .infinity, alignment: .leading)

            // Close icon
            Button(action: {
                onClick(notification.id)
            }) {
                Image(systemName: "xmark") // Close icon
                    .resizable()
                    .scaledToFit()
                    .frame(width: 16, height: 16)
                    .foregroundColor(.primary)
                    .padding(8)
                    .background(Color.clear) // transparent for tap area
            }
            .frame(width: 32, height: 32)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
        .background(
            Color(.systemBackground)
                .cornerRadius(8)
                .shadow(color: .black.opacity(0.2), radius: 8, x: 0, y: 4)
        )
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
    }
}

extension UserNotification {
    var notificationTitle: String {
        switch onEnum(of: self) {
        case .inventoryItemAdded, .inventoryItemRemoved:

            let item = (self as? UserNotification.InventoryItemAdded)?.item
                ?? (self as! UserNotification.InventoryItemRemoved).item

            // 1️⃣ Localized item name
            let itemName = item.id.toItemNameStringId().localized

            // 2️⃣ Description of the item (e.g., "Apple x2")
            let itemDescription = String(format: NSLocalizedString("InventoryItemPattern", comment: ""), itemName, item.amount)

            // 3️⃣ Title pattern
            let resId = (self is UserNotification.InventoryItemAdded)
                ? "NotificationInventoryItemAdded"
                : "NotificationInventoryItemRemoved"

            return String(format: NSLocalizedString(resId, comment: ""), itemDescription)
        }
    }
}
