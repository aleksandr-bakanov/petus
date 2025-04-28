package bav.petus.core.time

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun getTimestampSecondsSinceEpoch(): Long {
    return NSDate().timeIntervalSince1970.toLong()
}