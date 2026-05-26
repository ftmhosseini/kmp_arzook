package ca.arzook.shared.ui

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSTimeZone
import platform.Foundation.timeZoneWithName

actual fun currentDateString(): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "yyyy-MM-dd"
    return formatter.stringFromDate(NSDate())
}

actual fun utcToLocal(utcDateTime: String): String {
    return try {
        val parser = NSDateFormatter()
        parser.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
        parser.timeZone = NSTimeZone.timeZoneWithName("UTC")!!
        val date = parser.dateFromString(utcDateTime) ?: return utcDateTime
        val formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm"
        formatter.timeZone = NSTimeZone.localTimeZone
        formatter.stringFromDate(date)
    } catch (_: Exception) { utcDateTime }
}
