package bav.petus.core.time

actual fun getTimestampSecondsSinceEpoch(): Long {
    return System.currentTimeMillis() / 1000L
}