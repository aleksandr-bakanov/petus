package bav.petus.extension

import android.icu.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun Long.epochTimeToString(): String {
    if (this == 0L) return "unknown"
    val date = Date(this * 1000L)
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ROOT)
    return sdf.format(date)
}

actual fun Long.epochTimeToShortString(): String {
    if (this == 0L) return "unknown"
    val date = Date(this * 1000L)
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.ROOT)
    return sdf.format(date)
}
