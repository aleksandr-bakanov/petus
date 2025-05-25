package bav.petus.extension

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterShortStyle

actual fun Long.epochTimeToString(): String {
    if (this == 0L) return "unknown"
    val secondsSince2001 = this - SECONDS_BETWEEN_EPOCH_AND_2001
    val date = NSDate(timeIntervalSinceReferenceDate = secondsSince2001.toDouble())
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateStyle = NSDateFormatterMediumStyle
    dateFormatter.timeStyle = NSDateFormatterMediumStyle
    return dateFormatter.stringFromDate(date)
}

actual fun Long.epochTimeToShortString(): String {
    if (this == 0L) return "unknown"
    val secondsSince2001 = this - SECONDS_BETWEEN_EPOCH_AND_2001
    val date = NSDate(timeIntervalSinceReferenceDate = secondsSince2001.toDouble())
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateStyle = NSDateFormatterShortStyle
    return dateFormatter.stringFromDate(date)
}

private const val      SECONDS_BETWEEN_EPOCH_AND_2001 = 978307200L
private const val  SECONDS_BETWEEN_EPOCH_AND_2001_MID = 978303600L
private const val SECONDS_BETWEEN_EPOCH_AND_2001_PLUS = 978307200L
private const val SECONDS_BETWEEN_EPOCH_AND_2001_MINU = 978300000L