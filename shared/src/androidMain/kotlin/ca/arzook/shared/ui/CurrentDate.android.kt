package ca.arzook.shared.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

actual fun currentDateString(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

actual fun utcToLocal(utcDateTime: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(utcDateTime) ?: return utcDateTime
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        formatter.timeZone = TimeZone.getDefault()
        formatter.format(date)
    } catch (_: Exception) { utcDateTime }
}
