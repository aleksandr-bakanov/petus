package bav.petus.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import bav.petus.android.MyApplicationTheme
import bav.petus.android.R
import bav.petus.core.inventory.InventoryItem
import bav.petus.core.inventory.InventoryItemId
import bav.petus.core.inventory.toItemNameStringId
import bav.petus.core.notification.UserNotification

@Composable
fun UserNotificationCell(
    notification: UserNotification,
    onClick: (notificationId: String) -> Unit,
) {
    Surface(
        tonalElevation = 32.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = notification.notificationTitle(),
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        onClick(notification.id)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun UserNotification.notificationTitle(): String {
    return when (this) {
        is UserNotification.InventoryItemAdded, is UserNotification.InventoryItemRemoved -> {
            val item = (this as? UserNotification.InventoryItemAdded)?.item
                ?: (this as UserNotification.InventoryItemRemoved).item
            val itemName = stringResource(id = item.id.toItemNameStringId().toResId())
            val itemDescription = stringResource(R.string.InventoryItemPattern, itemName, item.amount)
            val resId = if (this is UserNotification.InventoryItemAdded)
                R.string.NotificationInventoryItemAdded
            else
                R.string.NotificationInventoryItemRemoved
            stringResource(resId, itemDescription)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserNotificationCellPreview() {
    MyApplicationTheme {
        UserNotificationCell(
            notification = UserNotification.InventoryItemAdded(
                item = InventoryItem(
                    id = InventoryItemId.Necronomicon,
                    amount = 3,
                )
            )
        ) { }
    }
}