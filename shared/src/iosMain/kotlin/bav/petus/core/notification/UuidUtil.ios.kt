package bav.petus.core.notification

import platform.Foundation.NSUUID

actual fun getUUID(): String {
    return NSUUID().UUIDString()
}